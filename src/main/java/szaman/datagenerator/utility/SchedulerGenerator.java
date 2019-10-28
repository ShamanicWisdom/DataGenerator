/*
        --DataGenerator--
    SchedulerGenerator class contains generation methods similar to OnlineDataGenerator but optimised for Scheduler.
*/

package szaman.datagenerator.utility;

//Other own classes:

import szaman.datagenerator.view.generation.model.DatabaseConnection;
import szaman.datagenerator.view.generation.model.SchedulerTask;

//Other classes:

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class SchedulerGenerator
{
    private boolean okClicked = false;
    
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Button dialogButton;
    
    Utility utility = new Utility();
    GenerationMethods generationMethods = new GenerationMethods();
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    DatabaseUtility databaseUtil = new DatabaseUtility();
    SingleDataUtility singleDataUtil = new SingleDataUtility();
    
    
    List<List<String>> pulledContentsList = new ArrayList<>();
    
    DatabaseConnection databaseConnection = new DatabaseConnection();
    
    List<SchedulerTask> schedulerTaskList = new ArrayList<>();
    
    Task<Void> generationAndSavingTask = null;
    Thread generationAndSavingThread = null;
    FileOutputStream outputStream = null;
    
    String emptyForeignKeyName = "";
    
    String headerAlert = "";
    String contentAlert = "";
    
    int primaryKeyIncrementation = 0;
    
    //If user clicks Cancel button, this variable will turn to true and it will halt and cancel the progress.
    private boolean cancelFired = false;
    
    Boolean recoveryModeSelected = false;
    String recoveryToggle = null;
    
    private Stage dialogStage;
    
    private final String csvFileName = System.getProperty("user.dir") + "\\" + utility.pullProgramName() + " ";
    
    public void setDialogStage(Stage dialogStage, List<SchedulerTask> schedulerTaskList, DatabaseConnection databaseConnection, Boolean recoveryModeSelected, String recoveryToggle)
    {
        this.dialogStage = dialogStage;
        this.schedulerTaskList = schedulerTaskList;
        this.databaseConnection = databaseConnection;
        this.recoveryModeSelected = recoveryModeSelected;
        this.recoveryToggle = recoveryToggle;
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
    public void startGeneration() throws InterruptedException
    {
        dataPoolUtil.createConnection();
        List<List<String>> masterList = new ArrayList<>();
        generationAndSavingTask = new Task<Void>() 
        {
            long start = System.nanoTime();
            @Override public Void call() throws InterruptedException, IOException 
            {
                for(int i = 0; i < schedulerTaskList.size(); i++)
                {
                    masterList.clear();
                    SchedulerTask currentTask = schedulerTaskList.get(i);
                    List<String> chosenContentTypeList = new ArrayList<>();
                    for (int contentNumber = 0; contentNumber < currentTask.getChosenContentList().size(); contentNumber++)
                    {
                        if(currentTask.getChosenContentList().get(contentNumber).equals("primaryKey"))
                        {
                            chosenContentTypeList.add("PK");
                        }
                        else
                        {
                            if(isContentUppercase(currentTask.getChosenContentList().get(contentNumber)))
                            {
                                chosenContentTypeList.add("DB"); //Marks content from DB.
                            }
                            else
                            {
                                if(currentTask.getImageDirectoryList().contains(currentTask.getChosenContentList().get(contentNumber)))
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
                    //chosenContentTypeList.clear();
                    pulledContentsList.clear();
                    
                    
                    //solveChosenContentTypes(currentTask.getChosenContentList());
                    //System.out.println("types solved - " + chosenContentTypeList + " NAME: " + currentTask.getChosenTable() + "CONT LIST: " + currentTask.getChosenContentList());
                    
                    updateMessage("Preparing data pool... (" + (i + 1) + " - " + schedulerTaskList.size() + ")");                   
                    
                    try
                    {                        
                        DatabaseMetaData databaseMetaData = databaseUtil.getMetaData();
                        //System.out.println("PK Col name: " + currentTask.getPrimaryKeyColumnName());
                        if(currentTask.getPrimaryKeyColumnName().equals("") == false)
                        {
                            //System.out.println("TABLE: " + currentTask.getChosenTable());
                            ResultSet primaryKeysInfo = databaseMetaData.getColumns(null, null, currentTask.getChosenTable(), currentTask.getPrimaryKeyColumnName());
                            if(primaryKeysInfo.next())
                            {
                                String primaryKeyType = primaryKeysInfo.getString("TYPE_NAME");
                                //System.out.println("PK TYPE: " + primaryKeyType);
                                if(primaryKeyType.equals("int"))
                                {
                                    currentTask.setIncrementator(databaseUtil.getTableMaxPK(currentTask.getPrimaryKeyColumnName(), currentTask.getChosenTable()));
                                }
                                else
                                {
                                    currentTask.setIncrementator(0);
                                }
                            }
                        }
                    }                    
                    catch(SQLException exception)
                    {                        
                        headerAlert = "Exception error!";
                        contentAlert = "Cannot get Primary Key data from Database!\n" +
                                       "Error: " + exception.toString();
                        super.failed();
                    }
                    
                    //System.out.println("incrementator solved. - " + currentTask.getIncrementator());
                    
                    if(currentTask.getIncrementator() == 1)
                    {
                        primaryKeyIncrementation = currentTask.getIncrementator();
                    }
                    if(currentTask.getIncrementator() > 1)
                    {
                        primaryKeyIncrementation = currentTask.getIncrementator() + 1;
                    }
                    
                    //System.out.println("PK incrementation solved. - " + primaryKeyIncrementation);
                    
                    int foreignKeyIterator = 0;                    
                    
                    for(int j = 0; j < currentTask.getChosenContentList().size(); j++)
                    {
                        if(chosenContentTypeList.get(j).equals("DB"))
                        {
                            List<String> pulledContentList;
                            pulledContentList = dataPoolUtil.pullContentFromSingleDataTable(currentTask.getChosenContentList().get(j));
                            pulledContentsList.add(pulledContentList);
                        }    
                        if(chosenContentTypeList.get(j).equals("IMG"))
                        {
                            List<String> pulledPathsList = new ArrayList<>();
                            File[] imagesList;
                            //For obtaining program's absolutePath.
                            File program = new File("");
                            String programPath = program.getAbsolutePath() + "\\ImageContainer" + "\\" + currentTask.getChosenContentList().get(j); 
                            //System.out.println("PATH: " + programPath);
                            File chosenDirectory = new File(programPath);
                            imagesList = chosenDirectory.listFiles();
                            if(imagesList.length == 0)
                            {
                                headerAlert = "Lack of content!";
                                contentAlert = "Directory " + currentTask.getChosenContentList().get(j) + " is empty!\nCannot generate anything.";
                                failed();
                            }
                            for(File image : imagesList) 
                            {
                                pulledPathsList.add(image.getAbsolutePath());
                            }
                            pulledContentsList.add(pulledPathsList);
                        }
                        if(currentTask.getChosenContentList().get(j).equals("foreignKey"))
                        {
                            List<String> pulledContentList = databaseUtil.pullPrimaryKeysFromTable(currentTask.getChosenTableForeignKeys().get(foreignKeyIterator));
                            //System.out.println("SIZE: " + pulledContentList.size());
                            //Is primary key related to foreign key's is null (no records on related table) then generation will be marked as failed.
                            if(pulledContentList.isEmpty())
                            {
                                emptyForeignKeyName = currentTask.getChosenTableForeignKeys().get(foreignKeyIterator).getForeignKeyColumn();
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
                    
                    //System.out.println("DB and FK cases filled.");
                    
                    for (int record = 0; record < currentTask.getNumberOfRecords(); record++) 
                    {
                        List<String> rowList = new ArrayList<>();
                        //for inner content list iterator.
                        int dbContentListIterator = 0;
                        for (int contentNumber = 0; contentNumber < chosenContentTypeList.size(); contentNumber++)
                        {
                            if(chosenContentTypeList.get(contentNumber).equals("PK"))
                            {
                                if(currentTask.getIncrementator() == 0)
                                {
                                    rowList.add(null);
                                }
                                else
                                {
                                    if(currentTask.getIncrementator() >= 1)
                                    {
                                        rowList.add(Integer.toString(primaryKeyIncrementation));
                                        primaryKeyIncrementation++;
                                    }
                                }
                            }        
                            else
                            {
                                if(chosenContentTypeList.get(contentNumber).equals("IMG"))
                                {
                                    String generatedContent;
                                    int randomNullChance = generationMethods.intNumberWithinRange(0, 100);
                                    //% chance if this record will be null.
                                    if((Integer.parseInt(currentTask.getNullChanceList().get(contentNumber)) >= randomNullChance) && (currentTask.getNullChanceList().get(contentNumber).equals("0") == false))
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
                                    if((Integer.parseInt(currentTask.getNullChanceList().get(contentNumber)) >= randomNullChance) && (currentTask.getNullChanceList().get(contentNumber).equals("0") == false))
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
                                    if(currentTask.getChosenContentList().get(contentNumber).equals("foreignKey"))
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
                                                if(method.getName().equals(currentTask.getChosenContentList().get(contentNumber)))
                                                {
                                                    //0 parameters methods.
                                                    if(method.getParameterCount() == 0)
                                                    {
                                                        String generatedContent;
                                                        int randomNullChance = generationMethods.intNumberWithinRange(1, 100);
                                                        //% chance if this record will be null.
                                                        if((Integer.parseInt(currentTask.getNullChanceList().get(contentNumber)) >= randomNullChance) && (currentTask.getNullChanceList().get(contentNumber).equals("0") == false))
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
                                                        int randomNullChance = generationMethods.intNumberWithinRange(1, 100);
                                                        //% chance if this record will be null.
                                                        if((Integer.parseInt(currentTask.getNullChanceList().get(contentNumber)) >= randomNullChance) && (currentTask.getNullChanceList().get(contentNumber).equals("0") == false))
                                                        {
                                                            generatedContent = "";
                                                        }
                                                        else
                                                        {
                                                            Object returnValue = method.getReturnType();
                                                            returnValue = method.invoke(generationMethods, 
                                                                                        Integer.parseInt(currentTask.getFromTextFieldList().get(contentNumber)),
                                                                                        Integer.parseInt(currentTask.getToTextFieldList().get(contentNumber)));
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
                                            super.failed();
                                        }
                                    }

                                }
                            }
                            
                        }
                        masterList.add(rowList);
                        updateProgress(record + 1, currentTask.getNumberOfRecords());        
                        updateMessage("Generated " + (record + 1) + " / " + currentTask.getNumberOfRecords() + " elements. (" + (i + 1) + " - " + schedulerTaskList.size() + ")");
                    }
                    
                    String currentCsvFileName = csvFileName + " " + (i + 1) + " - " + schedulerTaskList.size() + ".csv";
                    List<String> imageTypeGeneratedFiles = new ArrayList<>();
                    
                    try 
                    {
                        //Direct image-injecting file creation.
                        if(currentTask.getColumnDataTypesList().contains("varbinary") || currentTask.getColumnDataTypesList().contains("image"))
                        {
                            updateMessage("Creating files... (" + (i + 1) + " - " + schedulerTaskList.size() + ")");  
                            String tableName;
                            Pattern whitespacePattern = Pattern.compile("\\s");
                            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
                            //Solving tableName.
                            Matcher whitespaceTableNameMatcher = whitespacePattern.matcher(currentTask.getChosenTable());
                            boolean isTableNameContainsWhitespace = whitespaceTableNameMatcher.find();
                            Matcher fullDigitTableNameMatcher = fullDigitNamePattern.matcher(currentTask.getChosenTable());
                            boolean isTableNameIsMadeOfFullDigits = fullDigitTableNameMatcher.find();
                            if(isTableNameContainsWhitespace == false && isTableNameIsMadeOfFullDigits == false)
                            {
                                tableName = currentTask.getChosenTable();
                            }
                            else
                            {
                                tableName = "\"" + currentTask.getChosenTable() + "\"";
                            }
                            //Max 1k records to generate - singleblob limit.
                            if(currentTask.getNumberOfRecords() <= 1000)
                            {
                                String query = "INSERT INTO " + tableName + " VALUES ";
                                for(int rowIterator = 0; rowIterator < masterList.size(); rowIterator++)
                                {
                                    //System.out.println(rowIterator + " / " + masterList.size());
                                    int dataTypeIterator = 0;
                                    query += "(";
                                    for(int columnIterator = 0; columnIterator < masterList.get(rowIterator).size(); columnIterator++)
                                    {                                    
                                        Boolean doNotPutTheComma = false;
                                        //System.out.println(masterList.get(rowIterator).get(columnIterator));
                                        if(currentTask.getChosenContentList().get(columnIterator).equals("primaryKey"))
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
                                            if(currentTask.getChosenContentList().get(columnIterator).equals("foreignKey"))
                                            {
                                                query += masterList.get(rowIterator).get(columnIterator) + "";
                                            }
                                            if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("bit") ||
                                            currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("tinyint") ||
                                            currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("smallint") || 
                                            currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("int") ||
                                            currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("bigint") ||
                                            currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("real"))
                                            {
                                                query += masterList.get(rowIterator).get(columnIterator) + "";
                                            }
                                            if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("varchar") ||
                                               currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("nvarchar") ||
                                               currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("text") ||
                                               currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("ntext"))
                                            {
                                                query += "'" + masterList.get(rowIterator).get(columnIterator) + "'";
                                            }
                                            if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("varbinary"))
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
                                double realRecordNumber = (double)currentTask.getNumberOfRecords() / (double)maxFileCapacity;
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
                                            if(currentTask.getChosenContentList().get(columnIterator).equals("primaryKey"))
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
                                                if(currentTask.getChosenContentList().get(columnIterator).equals("foreignKey"))
                                                {
                                                    query += masterList.get(masterIterator).get(columnIterator) + "";
                                                }
                                                if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("bit") ||
                                                currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("tinyint") ||
                                                currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("smallint") || 
                                                currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("int") ||
                                                currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("bigint") ||
                                                currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("real"))
                                                {
                                                    query += masterList.get(masterIterator).get(columnIterator) + "";
                                                }
                                                if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("varchar") ||
                                                   currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("nvarchar") ||
                                                   currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("text") ||
                                                   currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("ntext"))
                                                {
                                                    query += "'" + masterList.get(masterIterator).get(columnIterator) + "'";
                                                }
                                                if(currentTask.getColumnDataTypesList().get(dataTypeIterator).equals("varbinary"))
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
                                        if(masterIterator == currentTask.getNumberOfRecords() - 1)
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
                            try (FileWriter fileWriter = new FileWriter(currentCsvFileName)) 
                            {
                                updateMessage("Saving to file... (" + (i + 1) + " - " + schedulerTaskList.size() + ")");
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
                        super.failed();
                    } 
                    
                    try
                    {
                        updateMessage("Exporting data... (" + (i + 1) + " - " + schedulerTaskList.size() + ")");
                        if(imageTypeGeneratedFiles.isEmpty())
                        {
                            databaseUtil.exportData(currentTask.getChosenTable(), currentCsvFileName, recoveryModeSelected, recoveryToggle);
                        }
                        else
                        {
                            databaseUtil.exportDataWithImages(currentTask.getChosenTable(), imageTypeGeneratedFiles, recoveryModeSelected, recoveryToggle);
                            for(String fileName: imageTypeGeneratedFiles)
                            {
                                File file = new File(fileName);
                                file.delete();
                            }
                        }
                        updateMessage("Export completed. (" + (i + 1) + " - " + schedulerTaskList.size() + ")");
                    }
                    catch(Exception exception)
                    {
                        Logger.getLogger(OnlineDataGenerator.class.getName()).log(Level.SEVERE, null, exception);
                        headerAlert = "Exception error!";
                        contentAlert = "Something went wrong while exporting data to database!\n" +
                            "Please contact the developer! Error: " + exception.toString();     
                    }                    
                }
                long finish = System.nanoTime();
                long elapsedTime = finish - start;
                double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                updateMessage("All tasks completed in " + elapsedTimeInSecond + " seconds.");
                return null;
            }
            
            @Override protected void failed() 
            {                
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
    
    public boolean isOkClicked() 
    {
        return okClicked;
    }
}
