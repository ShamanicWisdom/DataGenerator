/*
        --DataGenerator--
    AddSingleDataController class.
*/

package szaman.datagenerator.view.datapool.data;

//Other own classes:

import szaman.datagenerator.view.datapool.model.PoolTable;
import szaman.datagenerator.utility.DataPoolUtility;

//Other classes:

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Szaman
 */

public class AddSingleDataController 
{    
    private Stage dialogStage;
    
    private PoolTable poolTable;
    
    String chosenToggle = "Excel";
    
    @FXML
    private Label chosenFileLabel;
    @FXML
    private Label choseDataLabel;
    
    @FXML
    private RadioButton excelRadioButton;
    @FXML
    private RadioButton csvRadioButton;
    @FXML
    private RadioButton jsonRadioButton;
    //@FXML
    //private RadioButton manualRadioButton;
    
    @FXML
    private CheckBox ignoreFirstRecordBox;
    
    List<List<String>> masterDataList = new ArrayList<>();
    List<String> dataList = new ArrayList<>();
    
    final ToggleGroup radioButtonToggleGroup = new ToggleGroup();
    
    FileChooser fileChooser = new FileChooser();
    
    File chosenFile;
    
    //List<File> chosenFilesList = new ArrayList<>();
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    private boolean okClicked = false;
    
    private String poolTableName;    

    @FXML
    private void initialize() 
    {
    }

    //Setting the Dialog Stage.
    public void setDialogStage(Stage dialogStage, PoolTable poolTable) 
    {
        this.dialogStage = dialogStage;
        this.poolTable = poolTable;
        System.out.println(poolTable.getPoolTableName());
        excelRadioButton.setToggleGroup(radioButtonToggleGroup);
        //setting click behavior.
        excelRadioButton.setUserData("Excel");
        excelRadioButton.setSelected(true);
        csvRadioButton.setToggleGroup(radioButtonToggleGroup);
        csvRadioButton.setUserData("CSV");
        jsonRadioButton.setToggleGroup(radioButtonToggleGroup);
        jsonRadioButton.setUserData("JSON"); 
        //manualRadioButton.setToggleGroup(radioButtonToggleGroup);
        //manualRadioButton.setUserData("Manual (Dummy)");    
        
        radioButtonToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) 
            {
                if (radioButtonToggleGroup.getSelectedToggle() != null) 
                {
                    //changing the value of chosenToggle variable.
                    chosenToggle = radioButtonToggleGroup.getSelectedToggle().getUserData().toString();
                    chosenFile = null;
                    chosenFileLabel.setText("");
                    if(chosenToggle.equals("Excel") || chosenToggle.equals("CSV"))
                    {
                        ignoreFirstRecordBox.setDisable(false);
                        ignoreFirstRecordBox.setVisible(true);
                    }
                    else
                    {
                        ignoreFirstRecordBox.setDisable(true);
                        ignoreFirstRecordBox.setVisible(false);
                        ignoreFirstRecordBox.setSelected(false);
                    }
                }                
            }
        });
    }
    
    //Config for fileChooser.
    private static void configureFileChooser(FileChooser fileChooser, String extensionName, String properExtensions) 
    {    
        List<String> imageExtensionList = new ArrayList<>();
        if(extensionName.equals("Image"))
        {
            imageExtensionList.add("*.jpg");
            imageExtensionList.add("*.jpeg");
            imageExtensionList.add("*.png");
            
        }
        fileChooser.setTitle("Choose your " + extensionName + "-Source file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));     
        if(imageExtensionList.isEmpty())
        {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionName + " Files", "*." + properExtensions));
        }
        else
        {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(extensionName + " Files", imageExtensionList));
        }        
    }

    //OK clicking button behavior's method logic.
    public boolean isOkClicked() 
    {
        return okClicked;
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleChoose()
    {
        if(chosenToggle.equals("Excel"))
        {
            fileChooser = new FileChooser();
            configureFileChooser(fileChooser, chosenToggle, "xlsx");
            chosenFile = fileChooser.showOpenDialog(dialogStage);
            if(chosenFile != null) 
            {
                chosenFileLabel.setText("Selected file: \n" + chosenFile.getName());
            }
        }
        if(chosenToggle.equals("CSV"))
        {
            fileChooser = new FileChooser();
            configureFileChooser(fileChooser, chosenToggle, "csv");
            chosenFile = fileChooser.showOpenDialog(dialogStage);
            if(chosenFile != null) 
            {
                chosenFileLabel.setText("Selected file: \n" + chosenFile.getName());
            }
        }
        if(chosenToggle.equals("JSON"))
        {
            fileChooser = new FileChooser();
            configureFileChooser(fileChooser, chosenToggle, "json");
            chosenFile = fileChooser.showOpenDialog(dialogStage);
            if(chosenFile != null) 
            {
                chosenFileLabel.setText("Selected file: \n" + chosenFile.getName());
            }
        }
        /*
        if(chosenToggle.equals("Manual"))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Adding the manual data!");
            alert.setContentText("Action not yet available.");
            alert.showAndWait();
            fileChooser = new FileChooser();
        }
        */
        /*
        if(chosenToggle.equals("Image"))
        {
            chosenFile = null;
            fileChooser = new FileChooser();
            configureFileChooser(fileChooser, chosenToggle, "image");
            chosenFilesList = fileChooser.showOpenMultipleDialog(dialogStage);
            if(chosenFilesList.isEmpty() == false)
            {
                if(chosenFilesList.size() == 1)
                {
                    chosenFileLabel.setText("Selected " + chosenFilesList.size() + " file.");
                }
                else
                {
                    chosenFileLabel.setText("Selected " + chosenFilesList.size() + " files.");
                }                
            }
        }
        */
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleAdd() throws InvalidFormatException
    {
        //EXCEL FILE
        if (chosenFile != null && chosenToggle.equals("Excel"))
        {
            try
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("In Progress...");
                alert.setHeaderText("");
                alert.setContentText("Converting data from Excel file...");
                alert.show();
                 //Workbook's object.
                try (Workbook workbook = WorkbookFactory.create(chosenFile)) 
                {
                    //Choosing a sheet from an Excel's file.
                    Sheet sheet = workbook.getSheetAt(0);
                    
                    int totalRowsNumber = sheet.getLastRowNum();
                    //Converting all data to string.
                    DataFormatter dataFormatter = new DataFormatter();
                    //Row iterator.
                    Iterator<Row> rowIterator = sheet.rowIterator();
                    
                    dataList.clear();
                    while (rowIterator.hasNext())
                    {
                        Row row = rowIterator.next();
                        Cell cell = row.getCell(0);
                        String cellValue = dataFormatter.formatCellValue(cell);
                        cellValue = cellValue.trim();
                        dataList.add(cellValue);
                    }
                    fileChooser = new FileChooser();
                }
                alert.close();
                Boolean ignoreFirstRecord = ignoreFirstRecordBox.isSelected();
                int rowsCounter;
                if(ignoreFirstRecord == true)
                {
                    rowsCounter = dataList.size() - 1;
                }
                else
                {
                    rowsCounter = dataList.size();
                }
                dataPoolUtil.createConnection();
                
                String fileName = "";
                List<String> fileNamesList = new ArrayList<>();
                
                if(rowsCounter <= 1000)
                {
                    dataPoolUtil.insertSingleDataUsingString(dataList, poolTable.getPoolTableName(), ignoreFirstRecord); 
                }
                else
                {
                    //100k rows max file capacity.
                    if(dataList.size() <= 100000)
                    {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data File...");
                        alert.show();
                        fileName = dataPoolUtil.createDelFile(dataList, poolTable.getPoolTableName(), ignoreFirstRecord, 0);                    
                        alert.close();

                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        dataPoolUtil.insertSingleDataUsingFile(fileName, poolTable.getPoolTableName(), rowsCounter); 
                        alert.close();
                    }
                    else
                    {
                        List<String> partialDataList = new ArrayList<>();
                        int maxFileCapacity = 100000;
                        int fileNumber = 1;
                        //check how many files should be made.
                        double realRecordNumber = (double)dataList.size() / (double)maxFileCapacity;
                        int countOfFiles = (int)realRecordNumber;
                        
                        //check if there will be an additional not-fully capped file.    
                        if(realRecordNumber > (double)countOfFiles)
                        {
                            countOfFiles++;
                        }
                        int masterIterator = 0;
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data Files...");
                        alert.show();
                        for(int fileIterator = 0; fileIterator < countOfFiles; fileIterator++)
                        {
                            for(int partialDataIterator = 0; partialDataIterator < maxFileCapacity; partialDataIterator++)
                            {
                                if(partialDataIterator == 0)
                                {
                                    partialDataList.clear();
                                }
                                partialDataList.add(dataList.get(masterIterator));
                                
                                //if reaching a dataList limit, then stop the loop and save.
                                if(masterIterator == dataList.size() - 1)
                                {
                                    partialDataIterator = maxFileCapacity - 1;
                                }
                                if(partialDataIterator == maxFileCapacity - 1)
                                {
                                   fileName = dataPoolUtil.createDelFile(partialDataList, poolTable.getPoolTableName(), ignoreFirstRecord, fileNumber);  
                                   System.out.println("Done: " + fileName);
                                   fileNamesList.add(fileName);
                                   //ignoring ONLY the first record, not every first record of file :)
                                   if(fileNumber == 1)
                                   {
                                       ignoreFirstRecord = false;
                                   }
                                   fileNumber++;
                                }    
                                masterIterator++;
                            }
                        }
                        alert.close();
                        
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.insertSingleDataUsingFile(singleFileName, poolTable.getPoolTableName(), rowsCounter);
                        }                         
                        alert.close();
                        
                    }
                }          
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Clearing...");                
                alert.show();
                
                if(rowsCounter > 1000)
                {
                    if(fileNamesList.isEmpty())
                    {
                        alert.setContentText("Deleting data file...");
                        dataPoolUtil.deleteDelFile(fileName);
                    }
                    else
                    {
                        alert.setContentText("Deleting data files...");
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.deleteDelFile(singleFileName);
                        }
                    }
                }
                                
                alert.close();
                
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Success!");
                alert.setContentText("Successfully added " + rowsCounter + " rows into " + poolTable.getPoolTableName().toUpperCase() + " table!");
                                               
                alert.showAndWait();
                okClicked = true;
                dialogStage.close();
                
            }
            catch(IOException | InvalidFormatException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("File corrupted!");
                alert.setContentText("Cannot load the chosen file!");
                alert.showAndWait();
            }
        }
        //CSV FILE
        if (chosenFile != null && chosenToggle.equals("CSV"))
        {
            try
            {                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("In Progress...");
                alert.setHeaderText("");
                alert.setContentText("Converting data from CSV file...");
                alert.show();

                Boolean ignoreFirstRecord = ignoreFirstRecordBox.isSelected();
                dataList.clear();

                Reader reader = new FileReader(chosenFile);
                Iterable<CSVRecord> sourceRecords;
                if(ignoreFirstRecord == true)
                {
                    sourceRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
                }
                else
                {
                    sourceRecords = CSVFormat.DEFAULT.parse(reader);
                }
                for (CSVRecord record : sourceRecords)
                {
                    dataList.add(record.get(0));
                }

                dataPoolUtil.createConnection();

                int rowsCounter = dataList.size();
                String fileName = "";
                List<String> fileNamesList = new ArrayList<>();
                if(rowsCounter <= 1000)
                {
                    dataPoolUtil.insertSingleDataUsingString(dataList, poolTableName, ignoreFirstRecord); 
                }
                else
                {
                    //100k rows max file capacity.
                    if(dataList.size() <= 100000)
                    {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data File...");
                        alert.show();
                        fileName = dataPoolUtil.createDelFile(dataList, poolTableName, ignoreFirstRecord, 0);                    
                        alert.close();

                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        dataPoolUtil.insertSingleDataUsingFile(fileName, poolTableName, rowsCounter); 
                        alert.close();
                    }
                    else
                    {
                        List<String> partialDataList = new ArrayList<>();
                        int maxFileCapacity = 100000;
                        int fileNumber = 1;
                        //check how many files should be made.
                        double realRecordNumber = (double)dataList.size() / (double)maxFileCapacity;
                        int countOfFiles = (int)realRecordNumber;

                        //check if there will be an additional not-fully capped file.    
                        if(realRecordNumber > (double)countOfFiles)
                        {
                            countOfFiles++;
                        }
                        int masterIterator = 0;
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data Files...");
                        alert.show();
                        for(int fileIterator = 0; fileIterator < countOfFiles; fileIterator++)
                        {
                            for(int partialDataIterator = 0; partialDataIterator < maxFileCapacity; partialDataIterator++)
                            {
                                if(partialDataIterator == 0)
                                {
                                    partialDataList.clear();
                                }
                                partialDataList.add(dataList.get(masterIterator));

                                //if reaching a dataList limit, then stop the loop and save.
                                if(masterIterator == dataList.size() - 1)
                                {
                                    partialDataIterator = maxFileCapacity - 1;
                                }
                                if(partialDataIterator == maxFileCapacity - 1)
                                {
                                   fileName = dataPoolUtil.createDelFile(partialDataList, poolTableName, ignoreFirstRecord, fileNumber);  
                                   System.out.println("Done: " + fileName);
                                   fileNamesList.add(fileName);
                                   //ignoring ONLY the first record, not every first record of file :)
                                   if(fileNumber == 1)
                                   {
                                       ignoreFirstRecord = false;
                                   }
                                   fileNumber++;
                                }    
                                masterIterator++;
                            }
                        }
                        alert.close();

                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.insertSingleDataUsingFile(singleFileName, poolTableName, rowsCounter);
                        }                         
                        alert.close();
                    }
                }          
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Clearing...");                
                alert.show();

                if(rowsCounter > 1000)
                {
                    if(fileNamesList.isEmpty())
                    {
                        alert.setContentText("Deleting data file...");
                        dataPoolUtil.deleteDelFile(fileName);
                    }
                    else
                    {
                        alert.setContentText("Deleting data files...");
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.deleteDelFile(singleFileName);
                        }
                    }
                }

                alert.close();

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Success!");
                alert.setContentText("Successfully added " + rowsCounter + " rows into " + poolTableName.toUpperCase() + " table!");

                alert.showAndWait();
                okClicked = true;
                dialogStage.close();                
            }
            catch(IOException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("File corrupted!");
                alert.setContentText("Cannot load the chosen file!");
                alert.showAndWait();
            }
        }
        //JSON FILE
        if (chosenFile != null && chosenToggle.equals("JSON"))
        {
            try
            {                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("In Progress...");
                alert.setHeaderText("");
                alert.setContentText("Converting data from JSON file...");
                alert.show();

                dataList.clear();                

                //Read compatibility with UTF-8.
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(chosenFile), "UTF-8"));
                
                JSONParser jsonParser = new JSONParser();
                Object jsonObject = jsonParser.parse(reader);
                JSONArray jsonRecordList = (JSONArray) jsonObject;
                jsonRecordList.forEach(record -> parseJsonRecord((JSONObject) record, dataList));
                
                alert.close();

                dataPoolUtil.createConnection();

                int rowsCounter = dataList.size();
                String fileName = "";
                List<String> fileNamesList = new ArrayList<>();
                
                //Just a dummy for avoiding code duplicates (coz JSON hasn't got any 'headers').
                Boolean ignoreFirstRecord = false;
                
                if(rowsCounter <= 1000)
                {
                    dataPoolUtil.insertSingleDataUsingString(dataList, poolTableName, ignoreFirstRecord); 
                }
                else
                {
                    //100k rows max file capacity.
                    if(dataList.size() <= 100000)
                    {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data File...");
                        alert.show();
                        fileName = dataPoolUtil.createDelFile(dataList, poolTableName, ignoreFirstRecord, 0);                    
                        alert.close();

                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        dataPoolUtil.insertSingleDataUsingFile(fileName, poolTableName, rowsCounter); 
                        alert.close();
                    }
                    else
                    {
                        List<String> partialDataList = new ArrayList<>();
                        int maxFileCapacity = 100000;
                        int fileNumber = 1;
                        //check how many files should be made.
                        double realRecordNumber = (double)dataList.size() / (double)maxFileCapacity;
                        int countOfFiles = (int)realRecordNumber;

                        //check if there will be an additional not-fully capped file.    
                        if(realRecordNumber > (double)countOfFiles)
                        {
                            countOfFiles++;
                        }
                        int masterIterator = 0;
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Creating Data Files...");
                        alert.show();
                        for(int fileIterator = 0; fileIterator < countOfFiles; fileIterator++)
                        {
                            for(int partialDataIterator = 0; partialDataIterator < maxFileCapacity; partialDataIterator++)
                            {
                                if(partialDataIterator == 0)
                                {
                                    partialDataList.clear();
                                }
                                partialDataList.add(dataList.get(masterIterator));

                                //if reaching a dataList limit, then stop the loop and save.
                                if(masterIterator == dataList.size() - 1)
                                {
                                    partialDataIterator = maxFileCapacity - 1;
                                }
                                if(partialDataIterator == maxFileCapacity - 1)
                                {
                                   fileName = dataPoolUtil.createDelFile(partialDataList, poolTableName, ignoreFirstRecord, fileNumber);  
                                   System.out.println("Done: " + fileName);
                                   fileNamesList.add(fileName);
                                   //ignoring ONLY the first record, not every first record of file :)
                                   if(fileNumber == 1)
                                   {
                                       ignoreFirstRecord = false;
                                   }
                                   fileNumber++;
                                }    
                                masterIterator++;
                            }
                        }
                        alert.close();

                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("In Progress...");
                        alert.setHeaderText("");
                        alert.setContentText("Inserting data into database...");
                        alert.show();
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.insertSingleDataUsingFile(singleFileName, poolTableName, rowsCounter);
                        }                         
                        alert.close();
                    }
                }          
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Clearing...");                
                alert.show();

                if(rowsCounter > 1000)
                {
                    if(fileNamesList.isEmpty())
                    {
                        alert.setContentText("Deleting data file...");
                        dataPoolUtil.deleteDelFile(fileName);
                    }
                    else
                    {
                        alert.setContentText("Deleting data files...");
                        for(String singleFileName: fileNamesList)
                        {
                            dataPoolUtil.deleteDelFile(singleFileName);
                        }
                    }
                }

                alert.close();

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Success!");
                alert.setContentText("Successfully added " + rowsCounter + " rows into " + poolTableName.toUpperCase() + " table!");

                alert.showAndWait();
                okClicked = true;
                dialogStage.close();                
            }
            catch(IOException | org.json.simple.parser.ParseException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("File corrupted!");
                alert.setContentText("Cannot load the chosen file!");
                alert.showAndWait();
            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No file chosen!");
            alert.setContentText("Please choose a fitting file.");
            alert.showAndWait();
        }
        
    }
    
    private void parseJsonRecord(JSONObject record, List<String> dataList)
    {
        //Get object name located between first and second ' " ' chars.
        String recordName = getJsonData(record, 1, 2);
        JSONObject recordObject = (JSONObject) record.get(recordName);
        
        //Get first record string name located between third and fourth ' " ' chars.
        String recordString = getJsonData(recordObject, 3, 4); 
        
        dataList.add(recordString);
    }
    
    private String getJsonData(JSONObject record, int from, int to)
    {
        //convert String to char array.
        char[] charArray = record.toString().toCharArray();
        String recordName = "";
        int counter = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int i = 0; i < charArray.length; i++)
        {
            //ignore first ' " '
            if(charArray[i] == '"')
            {
                i++;
                counter++;
            } 
            //saving recordName.
            if(counter == from)
            {
                recordName += charArray[i];
            }
            //recordName has ended.
            if(counter == to)
            {
                break;
            }  
        } 
        return recordName;
    }
    
    //Cancel Button behavior's method.
    @FXML
    private void handleCancel() 
    {
        dialogStage.close();
    }
}
