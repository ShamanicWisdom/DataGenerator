/*
        --DataGenerator--
    OnlineDataGenerator class contains generation methods for databases and additional references from: Apache Commons CSV.
*/

package szaman.datagenerator.utility;

//Other own classes:

import szaman.datagenerator.view.generation.model.DatabaseConnection;
import szaman.datagenerator.view.generation.model.ForeignKey;

//Other classes:

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author Szaman
 */

public class OnlineDataGenerator 
{
    private boolean okClicked = false;
    
    int numberOfRecords = 0;
    
    Utility utility = new Utility();
    GenerationMethods generationMethods = new GenerationMethods();
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    DatabaseUtility databaseUtil = new DatabaseUtility();
    SingleDataUtility singleDataUtil = new SingleDataUtility();
    
    DatabaseConnection databaseConnection = new DatabaseConnection();
    
    List<String> chosenContentList = new ArrayList<>();
    List<String> chosenContentTypeList = new ArrayList<>();
    
    List<String> columnDataTypesList = new ArrayList<>();
    List<String> imageDirectoryList = new ArrayList<>();
    
    List<List<String>> pulledContentsList = new ArrayList<>();
    List<String> fromTextFieldList = new ArrayList<>();
    List<String> toTextFieldList = new ArrayList<>();
    List<String> nullChanceList = new ArrayList<>();
    
    List<ForeignKey> chosenTableForeignKeys = new ArrayList<>();
        
    Task<Void> generationAndSavingTask = null;
    Thread generationAndSavingThread = null;
    FileOutputStream outputStream = null;
    
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Button dialogButton;
    
    int incrementator;
    int primaryKeyIncrementation = 0;
    
    String emptyForeignKeyName = "";
    String chosenTableName = "";
    
    String headerAlert = "";
    String contentAlert = "";
    
    Boolean recoveryModeSelected = false;
    
    String recoveryToggle = null;
    
    //If user clicks Cancel button, this variable will turn to true and it will halt and cancel the progress.
    private boolean cancelFired = false;
    
    private final String csvFileName = System.getProperty("user.dir") + "\\" + utility.pullProgramName() + " " + utility.getCurrentDate() + " output.csv";
    
    private Stage dialogStage;
    
    public void setDialogStage(Stage dialogStage, String chosenTableName, int numberOfRecords, int incrementator, List<String> chosenContentList, List<String> columnDataTypesList, List<String> imageDirectoryList, List<String> fromTextFieldList, List<String> toTextFieldList, List<ForeignKey> chosenTableForeignKeys, DatabaseConnection databaseConnection, Boolean recoveryModeSelected, String recoveryToggle, List<String> nullChanceList)
    {
        this.dialogStage = dialogStage;
        this.chosenTableName = chosenTableName;
        this.numberOfRecords = numberOfRecords;
        this.incrementator = incrementator;
        this.chosenContentList = chosenContentList;
        this.columnDataTypesList = columnDataTypesList;
        this.imageDirectoryList = imageDirectoryList;
        this.fromTextFieldList = fromTextFieldList;
        this.toTextFieldList = toTextFieldList;
        this.chosenTableForeignKeys = chosenTableForeignKeys;
        this.databaseConnection = databaseConnection;
        this.recoveryModeSelected = recoveryModeSelected;
        this.recoveryToggle = recoveryToggle;
        this.nullChanceList = nullChanceList;
        testConnection();
    }
    
    public void testConnection()
    {
        String connectionInfo = databaseUtil.createConnection(databaseConnection);
        if (connectionInfo.equals("")) 
        {
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("There is at least one problem!");
            alert.setContentText(connectionInfo);
            alert.showAndWait(); 
            dialogStage.close();  
        }
    }
    
    //Create worksheet.
    public void startGeneration(int numberOfRecords) throws InterruptedException
    {
        dataPoolUtil.createConnection();
        String checkTableName = "" + chosenTableName;
        if(checkTableName.equals("") || checkTableName.length() == 0 || checkTableName.equals("null"))
        {
            headerAlert = "No table is chosen!";
            contentAlert = "Please select a table!";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(headerAlert);
            alert.setContentText(contentAlert);
            alert.showAndWait();  
            dialogStage.close();
        }
        else
        {
            solveChosenContentTypes(chosenContentList);
            System.out.println("TYPES: " + chosenContentTypeList);
            generateValues(numberOfRecords);
        }
    }
    
    //Start generating CSV values.
    public void generateValues(int numberOfRecords) throws InterruptedException
    {
        List<List<String>> masterList = new ArrayList<>();
        if(incrementator == 1)
        {
            primaryKeyIncrementation = incrementator;
        }
        if(incrementator > 1)
        {
            primaryKeyIncrementation = incrementator + 1;
        }
        
        generationAndSavingTask = new Task<Void>() 
        {
            long start = System.nanoTime();
            @Override public Void call() throws InterruptedException, IOException 
            {
                updateMessage("Preparing data pool...");
                int foreignKeyIterator = 0;
                for(int i = 0; i < chosenContentList.size(); i++)
                {
                    if(chosenContentTypeList.get(i).equals("DB"))
                    {
                        List<String> pulledContentList;
                        pulledContentList = dataPoolUtil.pullContentFromSingleDataTable(chosenContentList.get(i));
                        pulledContentsList.add(pulledContentList);
                    }    
                    if(chosenContentTypeList.get(i).equals("IMG"))
                    {
                        List<String> pulledPathsList = new ArrayList<>();
                        File[] imagesList;
                        //For obtaining program's absolutePath.
                        File program = new File("");
                        String programPath = program.getAbsolutePath() + "\\ImageContainer" + "\\" + chosenContentList.get(i); 
                        System.out.println("PATH: " + programPath);
                        File chosenDirectory = new File(programPath);
                        imagesList = chosenDirectory.listFiles();
                        if(imagesList.length == 0)
                        {
                            headerAlert = "Lack of content!";
                            contentAlert = "Directory " + chosenContentList.get(i) + " is empty!\nCannot generate anything.";
                            failed();
                        }
                        for(File image : imagesList) 
                        {
                            pulledPathsList.add(image.getAbsolutePath());
                        }
                        pulledContentsList.add(pulledPathsList);
                    }
                    if(chosenContentList.get(i).equals("foreignKey"))
                    {
                        List<String> pulledContentList = databaseUtil.pullPrimaryKeysFromTable(chosenTableForeignKeys.get(foreignKeyIterator));
                        //Is primary key related to foreign key's is null (no records on related table) then generation will be marked as failed.
                        if(pulledContentList.isEmpty())
                        {
                            emptyForeignKeyName = chosenTableForeignKeys.get(foreignKeyIterator).getForeignKeyColumn();
                            headerAlert = "Database error!";
                            contentAlert = "No records related to given Foreign Key (" + emptyForeignKeyName + ")!";
                            failed();                    
                        }
                        else
                        {
                            pulledContentsList.add(pulledContentList);
                        }
                    }
                }
                for (int record = 0; record < numberOfRecords; record++) 
                {
                    List<String> rowList = new ArrayList<>();
                    //for inner content list iterator.
                    int dbContentListIterator = 0;
                    for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                    {
                        if(chosenContentTypeList.get(contentNumber).equals("PK"))
                        {
                            if(incrementator == 0)
                            {
                                rowList.add(null);
                            }
                            else
                            {
                                if(incrementator >= 1)
                                {
                                    rowList.add(Integer.toString(primaryKeyIncrementation));
                                    primaryKeyIncrementation++;
                                }
                            }
                            
                        } 
                        if(chosenContentTypeList.get(contentNumber).equals("IMG"))
                        {
                            String generatedContent;
                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                            //% chance if this record will be null.
                            if((Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance) && (nullChanceList.get(contentNumber).equals("0") == false))
                            {
                                generatedContent = "";
                            }
                            else
                            {
                                generatedContent = singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator));
                            }
                            rowList.add(generatedContent);
                            dbContentListIterator++;
                        }
                        if(chosenContentTypeList.get(contentNumber).equals("DB"))
                        {
                            String generatedContent;
                            int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                            //% chance if this record will be null.
                            if((Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance) && (nullChanceList.get(contentNumber).equals("0") == false))
                            {
                                generatedContent = "";
                            }
                            else
                            {
                                generatedContent = singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator));
                            }
                            replaceCommaToDot(generatedContent);
                            rowList.add(generatedContent);
                            dbContentListIterator++;
                        }
                        else
                        {
                            if(chosenContentList.get(contentNumber).equals("foreignKey"))
                            {
                                String generatedContent = singleDataUtil.getRandomContent(pulledContentsList.get(dbContentListIterator));
                                replaceCommaToDot(generatedContent);
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
                                                if((Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance) && (nullChanceList.get(contentNumber).equals("0") == false))
                                                {
                                                    generatedContent = "";
                                                }
                                                else
                                                {
                                                    Object returnValue = method.getReturnType();   
                                                    returnValue = method.invoke(generationMethods);
                                                    generatedContent = returnValue.toString();
                                                }
                                                replaceCommaToDot(generatedContent);
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
                                                if((Integer.parseInt(nullChanceList.get(contentNumber)) >= randomNullChance) && (nullChanceList.get(contentNumber).equals("0") == false))
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
                                                }
                                                replaceCommaToDot(generatedContent);
                                                rowList.add(generatedContent);
                                                break;
                                            }     
                                        }
                                    } 
                                }
                                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                                {
                                    Logger.getLogger(OnlineDataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                                    headerAlert = "Exception error!";
                                    contentAlert = "Something went wrong while generating CSV data!\n" +
                                                   "Please contact the developer! Error: " + exception.toString();
                                }
                            }
                        }
                    }
                    masterList.add(rowList);
                    updateProgress(record + 1, numberOfRecords);        
                    updateMessage("Generated " + (record + 1) + " / " + numberOfRecords + " elements.");
                }
                List<String> imageTypeGeneratedFiles = new ArrayList<>();
                try 
                {
                    //Direct image-injecting file creation.
                    if(columnDataTypesList.contains("varbinary") || columnDataTypesList.contains("image"))
                    {
                        updateMessage("Creating files...");
                        String tableName;
                        Pattern whitespacePattern = Pattern.compile("\\s");
                        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
                        //Solving tableName.
                        Matcher whitespaceTableNameMatcher = whitespacePattern.matcher(chosenTableName);
                        boolean isTableNameContainsWhitespace = whitespaceTableNameMatcher.find();
                        Matcher fullDigitTableNameMatcher = fullDigitNamePattern.matcher(chosenTableName);
                        boolean isTableNameIsMadeOfFullDigits = fullDigitTableNameMatcher.find();
                        if(isTableNameContainsWhitespace == false && isTableNameIsMadeOfFullDigits == false)
                        {
                            tableName = chosenTableName;
                        }
                        else
                        {
                            tableName = "\"" + chosenTableName + "\"";
                        }
                        System.out.println("MASTER: " + chosenContentList);
                        //Max 1k records to generate.
                        if(numberOfRecords <= 1000)
                        {
                            String query = "INSERT INTO " + tableName + " VALUES ";
                            for(int rowIterator = 0; rowIterator < masterList.size(); rowIterator++)
                            {
                                int dataTypeIterator = 0;
                                query += "(";
                                for(int columnIterator = 0; columnIterator < masterList.get(rowIterator).size(); columnIterator++)
                                {                                    
                                    Boolean doNotPutTheComma = false;
                                    //System.out.println(masterList.get(rowIterator).get(columnIterator));
                                    if(chosenContentList.get(columnIterator).equals("primaryKey"))
                                    {
                                        String checkIfPrimaryKeyIsNull = masterList.get(rowIterator).get(columnIterator);
                                        if(checkIfPrimaryKeyIsNull == null)
                                        {
                                            doNotPutTheComma = true;
                                        }
                                        else
                                        {
                                            query += masterList.get(rowIterator).get(columnIterator) + "";
                                        }
                                        dataTypeIterator--;
                                    }
                                    else
                                    {
                                        if(chosenContentList.get(columnIterator).equals("foreignKey"))
                                        {
                                            query += masterList.get(rowIterator).get(columnIterator) + "";
                                        }
                                        if(columnDataTypesList.get(dataTypeIterator).equals("bit") ||
                                        columnDataTypesList.get(dataTypeIterator).equals("tinyint") ||
                                        columnDataTypesList.get(dataTypeIterator).equals("smallint") || 
                                        columnDataTypesList.get(dataTypeIterator).equals("int") ||
                                        columnDataTypesList.get(dataTypeIterator).equals("bigint") ||
                                        columnDataTypesList.get(dataTypeIterator).equals("real"))
                                        {
                                            query += masterList.get(rowIterator).get(columnIterator) + "";
                                        }
                                        if(columnDataTypesList.get(dataTypeIterator).equals("varchar") ||
                                           columnDataTypesList.get(dataTypeIterator).equals("nvarchar") ||
                                           columnDataTypesList.get(dataTypeIterator).equals("text") ||
                                           columnDataTypesList.get(dataTypeIterator).equals("ntext"))
                                        {
                                            query += "'" + masterList.get(rowIterator).get(columnIterator) + "'";
                                        }
                                        if(columnDataTypesList.get(dataTypeIterator).equals("varbinary"))
                                        {
                                            query += "(select * from openrowset(bulk N'" + masterList.get(rowIterator).get(columnIterator) + "', single_blob) as x)";
                                        }
                                    }
                                    if((((columnIterator + 1) == masterList.get(rowIterator).size()) == false) && doNotPutTheComma == false)
                                    {
                                        query += ",";
                                    }
                                    dataTypeIterator++;
                                }
                                query += ")";
                                if(rowIterator + 1 < masterList.size())
                                {
                                    query += ",\n";
                                }
                            }
                            String fileName = "QueryFile.txt";
                            imageTypeGeneratedFiles.add(fileName);
                            try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)))
                            {
                                bufferedWriter.write(query);
                            }
                        }
                        else
                        {
                            int maxFileCapacity = 1000;
                            int fileNumber = 1;
                            //check how many files should be made.
                            double realRecordNumber = (double)numberOfRecords / (double)maxFileCapacity;
                            int countOfFiles = (int)realRecordNumber;

                            //check if there will be an additional not-fully capped file.    
                            if(realRecordNumber > (double)countOfFiles)
                            {
                                countOfFiles++;
                            }
                            int masterIterator = 0;
                            for(int fileIterator = 0; fileIterator < countOfFiles; fileIterator++)
                            {
                                String query = "INSERT INTO " + tableName + " VALUES ";
                                for(int partialDataIterator = 0; partialDataIterator < maxFileCapacity; partialDataIterator++)
                                {
                                    int dataTypeIterator = 0;
                                    query += "(";
                                    for(int columnIterator = 0; columnIterator < masterList.get(partialDataIterator).size(); columnIterator++)
                                    {
                                        Boolean doNotPutTheComma = false;
                                        //System.out.println(masterList.get(masterIterator).get(columnIterator));
                                        if(chosenContentList.get(columnIterator).equals("primaryKey"))
                                        {
                                            String checkIfPrimaryKeyIsNull = masterList.get(partialDataIterator).get(columnIterator);
                                            if(checkIfPrimaryKeyIsNull == null)
                                            {
                                                doNotPutTheComma = true;
                                            }
                                            else
                                            {
                                                query += masterList.get(masterIterator).get(columnIterator) + "";
                                            }
                                            dataTypeIterator--;
                                        }
                                        else
                                        {
                                            if(chosenContentList.get(columnIterator).equals("foreignKey"))
                                            {
                                                query += masterList.get(masterIterator).get(columnIterator) + "";
                                            }
                                            if(columnDataTypesList.get(dataTypeIterator).equals("bit") ||
                                            columnDataTypesList.get(dataTypeIterator).equals("tinyint") ||
                                            columnDataTypesList.get(dataTypeIterator).equals("smallint") || 
                                            columnDataTypesList.get(dataTypeIterator).equals("int") ||
                                            columnDataTypesList.get(dataTypeIterator).equals("bigint") ||
                                            columnDataTypesList.get(dataTypeIterator).equals("real"))
                                            {
                                                query += masterList.get(masterIterator).get(columnIterator) + "";
                                            }
                                            if(columnDataTypesList.get(dataTypeIterator).equals("varchar") ||
                                               columnDataTypesList.get(dataTypeIterator).equals("nvarchar") ||
                                               columnDataTypesList.get(dataTypeIterator).equals("text") ||
                                               columnDataTypesList.get(dataTypeIterator).equals("ntext"))
                                            {
                                                query += "'" + masterList.get(masterIterator).get(columnIterator) + "'";
                                            }
                                            if(columnDataTypesList.get(dataTypeIterator).equals("varbinary"))
                                            {
                                                query += "(select * from openrowset(bulk N'" + masterList.get(masterIterator).get(columnIterator) + "', single_blob) as x)";
                                            }
                                        }
                                        if((((columnIterator + 1) == masterList.get(partialDataIterator).size()) == false) && doNotPutTheComma == false)
                                        {
                                            query += ",";
                                        }
                                        dataTypeIterator++;
                                    }
                                    query += ")";
                                    
                                    //if reaching a dataList limit, then stop the loop and save.
                                    if(masterIterator == numberOfRecords - 1)
                                    {
                                        partialDataIterator = maxFileCapacity - 1;
                                    }
                                    if(partialDataIterator + 1 < maxFileCapacity)
                                    {
                                        query += ",\n";
                                    }
                                    if(partialDataIterator == maxFileCapacity - 1)
                                    {
                                        String fileName = "QueryFile" + fileNumber + ".txt";
                                        imageTypeGeneratedFiles.add(fileName);
                                        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)))
                                        {
                                            bufferedWriter.write(query);
                                        }
                                       fileNumber++;
                                    }    
                                    masterIterator++;
                                }
                            }
                        }
                    }
                    //Standard file creation.
                    else
                    {
                        //try (FileWriter fileWriter = new FileWriter(csvFileName)) 
                        try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFileName), StandardCharsets.UTF_8)))
                        {
                            updateMessage("Saving to file...");
                            try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|')))
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
                    }
                    
                } 
                catch (IOException exception)
                {
                    Logger.getLogger(OnlineDataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                    headerAlert = "Exception error!";
                    contentAlert = "Something went wrong while generating CSV data!\n" +
                                   "Please contact the developer! Error: " + exception.toString();   
                } 
                
                try
                {
                    updateMessage("Exporting data...");
                    
                    if(imageTypeGeneratedFiles.isEmpty() == true)
                    {
                        databaseUtil.exportData(chosenTableName, csvFileName, recoveryModeSelected, recoveryToggle);
                    }
                    else
                    {
                        databaseUtil.exportDataWithImages(chosenTableName, imageTypeGeneratedFiles, recoveryModeSelected, recoveryToggle);
                        for(String fileName: imageTypeGeneratedFiles)
                        {
                            File file = new File(fileName);
                            file.delete();
                        }
                    }
                    long finish = System.nanoTime();
                    long elapsedTime = finish - start;
                    double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                    System.out.println("DB time for " + numberOfRecords + " records: " + elapsedTime); 
                    updateMessage("Generation completed in " + elapsedTimeInSecond + " seconds.");
                }
                catch(Exception exception)
                {
                    Logger.getLogger(OnlineDataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                    headerAlert = "Exception error!";
                    contentAlert = "Something went wrong while exporting data to database!\n" +
                        "Please contact the developer! Error: " + exception.toString();     
                }
                /*
                long finish = System.nanoTime();
                long elapsedTime = finish - start;
                System.out.println("CSV time for " + numberOfRecords + " records: " + elapsedTime); 
                */
                return null;
            }
            
            @Override protected void failed() 
            {
                super.failed();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText(headerAlert);
                alert.setContentText(contentAlert);
                alert.showAndWait();  
                updateMessage("Failed!");
                dialogStage.close();
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
            if(chosenContentList.get(contentNumber).equals("primaryKey"))
            {
                chosenContentTypeList.add("PK");
            }
            else
            {
                if(isContentUppercase(chosenContentList.get(contentNumber)))
                {
                    chosenContentTypeList.add("DB"); //Marks content from DB.
                }
                else
                {
                    if(imageDirectoryList.contains(chosenContentList.get(contentNumber)))
                    {
                        chosenContentTypeList.add("IMG"); //Marks content from ImageContainer directory (Image).
                    }
                    else
                    {
                        chosenContentTypeList.add("EMB"); //Marks content from GenerationMethods class (Embedded).
                    }                    
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
