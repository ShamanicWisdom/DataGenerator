/*
        --DataGenerator--
    DataGenerator class contains all generation methods and additional references from: Apache POI, Apache Commons CSV.
*/

package szaman.datagenerator.utility;

//Other own classes:



//Other classes:

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Szaman
 */

public class DataGenerator 
{
    private boolean okClicked = false;
    
    int numberOfRecords = 0;
        
    String chosenToggle;
    
    Utility utility = new Utility();
    GenerationMethods generationMethods = new GenerationMethods();
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    SingleDataUtility singleDataUtil = new SingleDataUtility();
    
    List<String> chosenContentList = new ArrayList<>();
    List<String> chosenContentTypeList = new ArrayList<>();
    
    List<List<String>> pulledContentsList = new ArrayList<>();
    List<String> fromTextFieldList = new ArrayList<>();
    List<String> toTextFieldList = new ArrayList<>();
    List<String> nullChanceList = new ArrayList<>();
    
    SXSSFWorkbook workbook = null;
    
    Task<Void> generationAndSavingTask = null;
    Thread generationAndSavingThread = null;
    FileOutputStream outputStream = null;
    
    Boolean includeHeaders;
    
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Button dialogButton;
    
    //If user clicks Cancel button, this variable will turn to true and it will halt and cancel the progress.
    private boolean cancelFired = false;
    
    //Independent home path + Desktop folder.
    String systemHome = System.getProperty("user.home") + "\\Desktop";
    //Fixed output file's name contains Program Name and creation date.
    private final String excelFileName = systemHome + "\\" + utility.pullProgramName() + "'s output " + utility.getCurrentDate() + ".xlsx";
    private final String csvFileName = systemHome + "\\" + utility.pullProgramName() + "'s output " + utility.getCurrentDate() + ".csv";
    private final String jsonFileName = systemHome + "\\" + utility.pullProgramName() + "'s output " + utility.getCurrentDate() + ".json";
    
    private Stage dialogStage;
    
    public void setDialogStage(Stage dialogStage, int numberOfRecords, String chosenToggle, Boolean includeHeaders, List<String> chosenContentList, List<String> fromTextFieldList, List<String> toTextFieldList, List<String> nullChanceList)
    {
        this.dialogStage = dialogStage;
        this.numberOfRecords = numberOfRecords;
        this.chosenToggle = chosenToggle;
        this.includeHeaders = includeHeaders;
        this.chosenContentList = chosenContentList;
        this.fromTextFieldList = fromTextFieldList;
        this.toTextFieldList = toTextFieldList;
        this.nullChanceList = nullChanceList;
    }
    
    //Create worksheet.
    public void startGeneration(int numberOfRecords) throws InterruptedException
    {
        dataPoolUtil.createConnection();
        solveChosenContentTypes(chosenContentList);
        System.out.println("TYPES: " + chosenContentTypeList);
        //Excel chosen.
        if(chosenToggle.equals("Excel"))
        {
            workbook = new SXSSFWorkbook();
            SXSSFSheet sheet = workbook.createSheet("Generated Values");
            generateExcelValues(workbook, sheet, numberOfRecords, includeHeaders);
        }
        //CSV chosen.
        if(chosenToggle.equals("CSV"))
        {
            generateCsvValues(numberOfRecords, includeHeaders);
        }
        //JSON chosen. JSON files need to have headers, so includeHeaders variable is ignored.
        if(chosenToggle.equals("JSON"))
        {
            generateJsonValues(numberOfRecords);
        }
    }
    
    //Start generating Excel values.
    public void generateExcelValues(SXSSFWorkbook workbook, SXSSFSheet sheet, int numberOfRecords, Boolean includeHeaders) throws InterruptedException
    {  
        generationAndSavingTask = new Task<Void>() 
        {            
            @Override public Void call() throws InterruptedException, IOException 
            {
                long start = System.nanoTime(); 
                updateMessage("Preparing data pool...");
                for(int i = 0; i < chosenContentList.size(); i++)
                {
                    if(chosenContentTypeList.get(i).equals("DB"))
                    {
                        List<String> pulledContentList;
                        pulledContentList = dataPoolUtil.pullContentFromSingleDataTable(chosenContentList.get(i));
                        pulledContentsList.add(pulledContentList);
                    }                    
                }
                int totalRecordsNumber = numberOfRecords;
                Boolean headers = includeHeaders;
                Boolean areHeadersAdded = headers;
                if(headers == true)
                {
                    totalRecordsNumber++;
                }
                for (int record = 0; record < totalRecordsNumber; record++) 
                {
                    if(headers == true)
                    {
                        SXSSFRow row = sheet.createRow(record);
                        for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                        {
                            row.createCell(contentNumber).setCellValue(chosenContentList.get(contentNumber));
                        }
                        headers = false;
                    }
                    else
                    {
                        SXSSFRow row = sheet.createRow(record);
                        //for inner content list iterator.
                        int dbContentListIterator = 0;
                        for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                        {
                            if(chosenContentTypeList.get(contentNumber).equals("INC"))
                            {
                                int recordNumber;
                                if(areHeadersAdded == true)
                                {
                                    recordNumber = record;
                                }
                                else
                                {
                                    recordNumber = record + 1;
                                }
                                row.createCell(contentNumber).setCellValue(recordNumber);
                            }
                            if(chosenContentTypeList.get(contentNumber).equals("DB"))
                            {
                                int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                //% chance if this record will be null.
                                if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                {
                                    row.createCell(contentNumber).setCellValue("");
                                }
                                else
                                {
                                    row.createCell(contentNumber).setCellValue(singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator)));
                                }
                                dbContentListIterator++;
                            }
                            else
                            {
                                try 
                                {
                                    Method[] methods = generationMethods.getClass().getMethods(); 
                                    for (Method method : methods) 
                                    { 
                                        if(method.getName().equals(chosenContentList.get(contentNumber)))
                                        {
                                            //0 parameters methods.
                                            if(method.getParameterCount() == 0)
                                            {
                                                int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                                //% chance if this record will be null.
                                                if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                                {
                                                    row.createCell(contentNumber).setCellValue("");
                                                }
                                                else
                                                {
                                                    Object returnValue = method.getReturnType();   
                                                    returnValue = method.invoke(generationMethods);
                                                    row.createCell(contentNumber).setCellValue(returnValue.toString());
                                                }
                                                break;
                                            }
                                            //1 parameter methods.
                                            if(method.getParameterCount() == 1)
                                            {
                                                System.out.println("not yet implemented.\n");
                                                break;
                                            }
                                            //2 parameters methods.
                                            if(method.getParameterCount() == 2)
                                            {
                                                int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                                //% chance if this record will be null.
                                                if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                                {
                                                    row.createCell(contentNumber).setCellValue("");
                                                }
                                                else
                                                {
                                                    Object returnValue = method.getReturnType();
                                                    returnValue = method.invoke(generationMethods, 
                                                                                Integer.parseInt(fromTextFieldList.get(contentNumber)),
                                                                                Integer.parseInt(toTextFieldList.get(contentNumber)));
                                                    row.createCell(contentNumber).setCellValue(returnValue.toString());
                                                }
                                                break;
                                            }                                        
                                        }
                                    } 
                                }
                                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                                {
                                    Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error!");
                                    alert.setHeaderText("Exception error!");
                                    alert.setContentText
                                    (
                                        "Something went wrong while generating Excel data!\n" +
                                        "Please contact the developer! Error: " + exception.toString()                                    
                                    );
                                    alert.showAndWait();
                                }
                            }
                        }
                        updateProgress(record + 1, numberOfRecords);        
                        updateMessage("Generated " + (record + 1) + " / " + numberOfRecords + " elements.");
                    }
                }
                try 
                {
                    //File creation.
                    outputStream = new FileOutputStream(excelFileName);
                    updateMessage("Saving to file...");
                    workbook.write(outputStream);
                    workbook.close();                    
                    System.gc();
                    outputStream.flush();
                    outputStream.close();
                    outputStream = null;
                    long finish = System.nanoTime();
                    long elapsedTime = finish - start;
                    double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                    System.out.println("Excel time for " + numberOfRecords + " records: " + elapsedTime); 
                    updateMessage("Generation completed in " + elapsedTimeInSecond + " seconds.");
                } 
                catch (IOException exception)
                {
                    Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText("Exception error!");
                    alert.setContentText
                    (
                        "Something went wrong while generating CSV data!\n" +
                        "Please contact the developer! Error: " + exception.toString()                                    
                    );
                    alert.showAndWait();
                } 
                return null;
            }
            
        };       
        
        progressLabel.setText("Starting generation...");
        progressBar.setProgress(0);
        //Binds allow us to connect JavaFX elements with Task for additional interaction and smooth user experience.
        progressBar.progressProperty().bind(generationAndSavingTask.progressProperty());   
        progressLabel.textProperty().bind(generationAndSavingTask.messageProperty());

        //Set Thread to handle generation & save task.
        generationAndSavingThread = new Thread(generationAndSavingTask);
        generationAndSavingThread.setDaemon(true);
        generationAndSavingThread.start();
        
        if(progressBar.getProgress() == 100)
        {
            dialogButton.setText("Close");
        }   
                 
    }
    
    //Start generating CSV values.
    public void generateCsvValues(int numberOfRecords, Boolean includeHeaders) throws InterruptedException
    {
          
        List<List<String>> masterList = new ArrayList<>();
        generationAndSavingTask = new Task<Void>() 
        {
            long start = System.nanoTime();
            @Override public Void call() throws InterruptedException, IOException 
            {
                updateMessage("Preparing data pool...");
                for(int i = 0; i < chosenContentList.size(); i++)
                {
                    if(chosenContentTypeList.get(i).equals("DB"))
                    {
                        List<String> pulledContentList;
                        pulledContentList = dataPoolUtil.pullContentFromSingleDataTable(chosenContentList.get(i));
                        pulledContentsList.add(pulledContentList);
                    }                    
                }
                if(includeHeaders == true)
                {
                    masterList.add(chosenContentList);
                }
                for (int record = 0; record < numberOfRecords; record++) 
                {
                    List<String> rowList = new ArrayList<>();
                    //for inner content list iterator.
                    int dbContentListIterator = 0;
                    for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                    {
                        if(chosenContentTypeList.get(contentNumber).equals("INC"))
                        {
                            rowList.add(Integer.toString(record + 1));
                        }
                        if(chosenContentTypeList.get(contentNumber).equals("DB"))
                        {
                            String generatedContent;
                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                            //% chance if this record will be null.
                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                            {
                                generatedContent = "";
                            }
                            else
                            {
                                generatedContent = singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator));
                                replaceCommaToDot(generatedContent);
                            }
                            rowList.add(generatedContent);
                            dbContentListIterator++;
                        }
                        else
                        {
                            try 
                            {
                                Method[] methods = generationMethods.getClass().getMethods(); 
                                for (Method method : methods) 
                                { 
                                    if(method.getName().equals(chosenContentList.get(contentNumber)))
                                    {
                                        //0 parameters methods.
                                        if(method.getParameterCount() == 0)
                                        {
                                            String generatedContent;
                                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                            //% chance if this record will be null.
                                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                            {
                                                generatedContent = "";
                                            }
                                            else
                                            {
                                                Object returnValue = method.getReturnType();   
                                                returnValue = method.invoke(generationMethods);
                                                generatedContent = returnValue.toString();
                                                replaceCommaToDot(generatedContent);
                                            }
                                            rowList.add(generatedContent);
                                            break;
                                        }
                                        //1 parameter methods.
                                        if(method.getParameterCount() == 1)
                                        {
                                            System.out.println("not yet implemented.\n");
                                            break;
                                        }
                                        //2 parameters methods.
                                        if(method.getParameterCount() == 2)
                                        {
                                            String generatedContent;
                                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                            //% chance if this record will be null.
                                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                            {
                                                generatedContent = "";
                                            }
                                            else
                                            {
                                                Object returnValue = method.getReturnType();
                                                returnValue = method.invoke(generationMethods, 
                                                                            Integer.parseInt(fromTextFieldList.get(contentNumber)),
                                                                            Integer.parseInt(toTextFieldList.get(contentNumber)));
                                                generatedContent = returnValue.toString();
                                                replaceCommaToDot(generatedContent);
                                            }
                                            rowList.add(generatedContent);
                                            break;
                                        }                                        
                                    }
                                } 
                            }
                            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                            {
                                Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error!");
                                alert.setHeaderText("Exception error!");
                                alert.setContentText
                                (
                                    "Something went wrong while generating CSV data!\n" +
                                    "Please contact the developer! Error: " + exception.toString()                                    
                                );
                                alert.showAndWait();
                            }
                        }
                    }
                    masterList.add(rowList);
                    updateProgress(record + 1, numberOfRecords);        
                    updateMessage("Generated " + (record + 1) + " / " + numberOfRecords + " elements.");
                }
                try 
                {
                    //File creation.
                    try (FileWriter fileWriter = new FileWriter(csvFileName)) 
                    {
                        updateMessage("Saving to file...");
                        try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT))
                        {
                            for(int rowIterator = 0; rowIterator < masterList.size(); rowIterator++)
                            {                          
                                for(int columnIterator = 0; columnIterator < masterList.get(rowIterator).size(); columnIterator++)
                                {
                                    csvPrinter.print(masterList.get(rowIterator).get(columnIterator));
                                }
                                csvPrinter.println();
                            }
                        }
                        System.gc();
                    }
                    long finish = System.nanoTime();
                    long elapsedTime = finish - start;
                    double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                    System.out.println("CSV time for " + numberOfRecords + " records: " + elapsedTime); 
                    updateMessage("Generation completed in " + elapsedTimeInSecond + " seconds.");
                } 
                catch (IOException exception)
                {
                    Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText("Exception error!");
                    alert.setContentText
                    (
                        "Something went wrong while generating CSV data!\n" +
                        "Please contact the developer! Error: " + exception.toString()                                    
                    );
                    alert.showAndWait();
                } 
                return null;
            }
            
            
        };       
        
        progressLabel.setText("Starting generation...");
        progressBar.setProgress(0);
        //Binds allow us to connect JavaFX elements with Task for additional interaction and smooth user experience.
        progressBar.progressProperty().bind(generationAndSavingTask.progressProperty());   
        progressLabel.textProperty().bind(generationAndSavingTask.messageProperty());

        //Set Thread to handle generation & save task.
        generationAndSavingThread = new Thread(generationAndSavingTask);
        generationAndSavingThread.setDaemon(true);
        generationAndSavingThread.start();
        
        if(progressBar.getProgress() == 100)
        {
            dialogButton.setText("Close");
        } 
    }
    
    //Start generating JSON values.
    public void generateJsonValues(int numberOfRecords) throws InterruptedException
    {         
        JSONArray jsonRecordList = new JSONArray();
        generationAndSavingTask = new Task<Void>() 
        {
            @Override public Void call() throws InterruptedException, IOException 
            {
                long start = System.nanoTime(); 
                updateMessage("Preparing data pool...");
                for(int i = 0; i < chosenContentList.size(); i++)
                {
                    if(chosenContentTypeList.get(i).equals("DB"))
                    {
                        List<String> pulledContentList;
                        pulledContentList = dataPoolUtil.pullContentFromSingleDataTable(chosenContentList.get(i));
                        pulledContentsList.add(pulledContentList);
                    }                    
                }
                for (int record = 0; record < numberOfRecords; record++) 
                {
                    JSONObject jsonRecord = new JSONObject();
                    JSONObject jsonSingleData = new JSONObject();
                    //for inner content list iterator.
                    int dbContentListIterator = 0;                    
                    for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                    {
                        if(chosenContentTypeList.get(contentNumber).equals("INC"))
                        {
                            String generatedContent = Integer.toString(record + 1);
                            jsonSingleData.put(chosenContentList.get(contentNumber), generatedContent);
                        }
                        if(chosenContentTypeList.get(contentNumber).equals("DB"))
                        {
                            String generatedContent;
                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                            //% chance if this record will be null.
                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                            {
                                generatedContent = "";
                            }
                            else
                            {
                                generatedContent = singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator));
                                replaceCommaToDot(generatedContent);
                            }
                            jsonSingleData.put(chosenContentList.get(contentNumber), generatedContent);
                            dbContentListIterator++;
                        }
                        else
                        {
                            try 
                            {
                                Method[] methods = generationMethods.getClass().getMethods(); 
                                for (Method method : methods) 
                                { 
                                    if(method.getName().equals(chosenContentList.get(contentNumber)))
                                    {
                                        //0 parameters methods.
                                        if(method.getParameterCount() == 0)
                                        {
                                            String generatedContent;
                                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                            //% chance if this record will be null.
                                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                            {
                                                generatedContent = "";
                                            }
                                            else
                                            {
                                                Object returnValue = method.getReturnType();   
                                                returnValue = method.invoke(generationMethods);
                                                generatedContent = returnValue.toString();
                                                replaceCommaToDot(generatedContent);
                                            }
                                            jsonSingleData.put(chosenContentList.get(contentNumber), generatedContent);
                                            break;
                                        }
                                        //1 parameter methods.
                                        if(method.getParameterCount() == 1)
                                        {
                                            System.out.println("not yet implemented.\n");
                                            break;
                                        }
                                        //2 parameters methods.
                                        if(method.getParameterCount() == 2)
                                        {
                                            String generatedContent;
                                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                            //% chance if this record will be null.
                                            if(Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance)
                                            {
                                                generatedContent = "";
                                            }
                                            else
                                            {
                                                Object returnValue = method.getReturnType();
                                                returnValue = method.invoke(generationMethods, 
                                                                            Integer.parseInt(fromTextFieldList.get(contentNumber)),
                                                                            Integer.parseInt(toTextFieldList.get(contentNumber)));
                                                generatedContent = returnValue.toString();
                                                replaceCommaToDot(generatedContent);
                                            }
                                            jsonSingleData.put(chosenContentList.get(contentNumber), generatedContent);
                                            break;
                                        }                                        
                                    }
                                } 
                            }
                            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                            {
                                Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error!");
                                alert.setHeaderText("Exception error!");
                                alert.setContentText
                                (
                                    "Something went wrong while generating JSON data!\n" +
                                    "Please contact the developer! Error: " + exception.toString()                                    
                                );
                                alert.showAndWait();
                            }
                        }
                        
                        jsonRecord.put("record", jsonSingleData);
                    }
                    jsonRecordList.add(jsonRecord);
                    
                    updateProgress(record + 1, numberOfRecords);        
                    updateMessage("Generated " + (record + 1) + " / " + numberOfRecords + " elements.");
                }
                try 
                {
                    //File creation.
                    try (OutputStream outputStream = new FileOutputStream(jsonFileName); Writer outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8")) 
                    {
                        updateMessage("Saving to file...");
                        outputStreamWriter.write(jsonRecordList.toJSONString());
                    }
                    System.gc();
                    long finish = System.nanoTime();
                    long elapsedTime = finish - start;
                    double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                    System.out.println("JSON time for " + numberOfRecords + " records: " + elapsedTime); 
                    updateMessage("Generation completed in " + elapsedTimeInSecond + " seconds.");
                } 
                catch (IOException exception)
                {
                    Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText("Exception error!");
                    alert.setContentText
                    (
                        "Something went wrong while generating CSV data!\n" +
                        "Please contact the developer! Error: " + exception.toString()                                    
                    );
                    alert.showAndWait();
                } 
                return null;
            }
             
        };
        
        progressLabel.setText("Starting generation...");
        progressBar.setProgress(0);
        //Binds allow us to connect JavaFX elements with Task for additional interaction and smooth user experience.
        progressBar.progressProperty().bind(generationAndSavingTask.progressProperty());   
        progressLabel.textProperty().bind(generationAndSavingTask.messageProperty());

        //Set Thread to handle generation & save task.
        generationAndSavingThread = new Thread(generationAndSavingTask);
        generationAndSavingThread.setDaemon(true);
        generationAndSavingThread.start();
        
        if(progressBar.getProgress() == 100)
        {
            dialogButton.setText("Close");
        }
    }
            
    public boolean isOkClicked() 
    {
        return okClicked;
    }
    
    public void solveChosenContentTypes(List<String> chosenContentList)
    {
        for (int contentNumber = 0; contentNumber < chosenContentList.size(); contentNumber++)
        {
            if(chosenContentList.get(contentNumber).equals("incrementation"))
            {
                chosenContentTypeList.add("INC");
            }
            else
            {
                if(isContentUppercase(chosenContentList.get(contentNumber)))
                {
                    chosenContentTypeList.add("DB"); //Marks content from DB.
                }
                else
                {
                    chosenContentTypeList.add("EMB"); //Marks content from GenerationMethods class (Embedded).
                }
            }            
        }
    }
    
    private static boolean isContentUppercase(String contentString)
    {
        //convert String to char array.
        char[] charArray = contentString.toCharArray();
        
        for(int i = 0; i < charArray.length; i++)
        {
            //if any character is lower case, return false (DB tablenames have only uppercase letters).
            if(Character.isLowerCase(charArray[i]))
            {
                return false;                
            }
        }        
        return true;
    }
    
    private void replaceCommaToDot(String contentString)
    {
        //convert String to char array.
        char[] charArray = contentString.toCharArray();
        
        for(int i = 0; i < charArray.length; i++)
        {
            //if comma, then replace to dot.
            if(charArray[i] == ',')
            {
                charArray[i] = '.';           
            }
        } 
        //reconvert to String.
        contentString = charArray.toString();
    }
    
    //Cancel button's behavior.
    @FXML
    private void handleExit() throws IOException, InterruptedException 
    {
        cancelFired = true;
        generationAndSavingTask.cancel();
        dialogStage.close();
    }  
}
