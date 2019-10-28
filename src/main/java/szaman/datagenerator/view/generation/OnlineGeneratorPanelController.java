/*
        --DataGenerator--
    OnlineGeneratorController class.
*/

package szaman.datagenerator.view.generation;

//Other own classes:

import szaman.datagenerator.view.generation.model.SchedulerTask;
import szaman.datagenerator.Core;
import szaman.datagenerator.utility.Utility;
import szaman.datagenerator.utility.GenerationMethods;
import szaman.datagenerator.utility.DataPoolUtility;
import szaman.datagenerator.utility.DatabaseUtility;
import szaman.datagenerator.view.generation.model.DatabaseConnection;
import szaman.datagenerator.view.generation.model.ForeignKey;

//Other classes:

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Szaman
 */

public class OnlineGeneratorPanelController
{    
    @FXML
    private Label buildInformer;
    @FXML
    private Label connectionLabel;    
    
    @FXML
    private TextField numberOfRecordsField;
    
    @FXML
    private GridPane gridPane;
    
    @FXML
    private ScrollPane scrollPane;
    
    DatabaseUtility databaseUtil = new DatabaseUtility();    
    
    @FXML
    private ChoiceBox tableNamesBox;
    
    @FXML
    private Button generateNowButton;
    @FXML
    private Button addToSchedulerButton;
    
    Boolean generateNow;
    
    //Scheduler's FX objects:
    @FXML
    private Label schedulerLabel;
    @FXML
    private TableView<SchedulerTask> schedulerTable;
    @FXML
    private TableColumn<SchedulerTask, String> schedulerFirstColumn;
    @FXML
    private TableColumn<SchedulerTask, String> schedulerSecondColumn;
    @FXML
    private Button deleteSingleTaskButton;
    @FXML
    private Button deleteAllTasksButton;
    @FXML
    private Button executeSchedulerButton;
    
    private final ObservableList<SchedulerTask> dataGatherer = FXCollections.observableArrayList();
    
    //final ToggleGroup radioButtonToggleGroup = new ToggleGroup();     
    
    String primaryKeyColumnName = "";
    
    DatabaseConnection databaseConnection = new DatabaseConnection();
    
    List<List<String>> fixedList = new ArrayList<>();
    
    private Core core;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    List<String> databaseTableNames = new ArrayList<>();
    
    List<String> allContentList = new ArrayList<>();
    List<String> methodsList = new ArrayList<>();
    List<String> tablesList = new ArrayList<>(); 
    List<String> imageDirectoryList = new ArrayList<>(); 
    
    List<String> columnDataTypesList = new ArrayList<>();
    
    List<String> chosenContentList = new ArrayList<>();
    
    List<ForeignKey> chosenTableForeignKeys = new ArrayList<>(); 
    
    final List<SchedulerTask> schedulerTaskList = new ArrayList<>();
    
    String chosenTaskTableName = "";
    SchedulerTask chosenTask;
    
    Utility utility = new Utility();
    
    String chosenContent = null;
    String chosenTable = null;
    
    //Database logging minimalisation.
    @FXML
    private CheckBox recoveryModeCheckBox;
    Boolean recoveryMode = false;
    
    String chosenToggle = null;
    
    //Radio buttons.
    @FXML
    private RadioButton fullRecovery;
    @FXML
    private RadioButton bulkRecovery;
    @FXML
    private RadioButton simpleRecovery;
    
    final ToggleGroup radioButtonToggleGroup = new ToggleGroup();   
    
    @FXML
    Circle askCircle;
    
    int incrementator;

    public void setCore(Core core, DatabaseConnection databaseConnection)
    {
        this.core = core;
        this.databaseConnection = databaseConnection;
        testConnection();
        initializeTableNamesChoiceBox();
        if(tableNamesBox.getItems().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("List is empty!");
            alert.setHeaderText("List is empty!");
            alert.setContentText
            (
                "There is no table inside connected database!\n"
                + "Add a table if you want to proceed :)"
            );
            alert.showAndWait(); 
        }
        
        showBuildInfo();     
        //Adding embedded methods to methodList
        for (Method method: GenerationMethods.class.getDeclaredMethods()) 
        {
            String name = method.getName();
            methodsList.add(name);
        }
        //Adding database pool tables to tablesList.
        dataPoolUtil.createConnection();
        tablesList = dataPoolUtil.getNonEmptyTableNames(tablesList);
        
        //Adding image pools to imageDirectoryList.
        File program = new File("");
        String programPath = program.getAbsolutePath() + "\\ImageContainer"; 
        program = new File(programPath);
        File test[]; // = new ArrayList<>();
        test = program.getAbsoluteFile().listFiles();
        for(int i = 0; i < test.length; i++)
        {
            if(test[i].isDirectory())
            {
                imageDirectoryList.add(test[i].getName());
            }
        }
        
        //Concatenate both types of activities.
        allContentList.addAll(methodsList);
        allContentList.addAll(tablesList);    
        allContentList.addAll(imageDirectoryList);
        
        schedulerLabel.setVisible(false);
        schedulerFirstColumn.setCellValueFactory(cellData -> cellData.getValue().tableNameProperty());
        schedulerSecondColumn.setCellValueFactory(cellData -> cellData.getValue().recordsCountProperty());
        schedulerTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> focusOnSelectedTask(newValue));
        schedulerTable.setVisible(false);
        deleteSingleTaskButton.setVisible(false);
        deleteSingleTaskButton.setDisable(true);
        deleteAllTasksButton.setVisible(false);
        deleteAllTasksButton.setDisable(true);     
        executeSchedulerButton.setVisible(false);
        executeSchedulerButton.setDisable(true);
        
        generateNowButton.setOnAction(e -> 
        {
            generateNow = true;
            try 
            {
                runTheGeneration();
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(OnlineGeneratorPanelController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        addToSchedulerButton.setOnAction(e -> 
        {
            generateNow = false;
            try 
            {
                runTheGeneration();
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(OnlineGeneratorPanelController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Image image = new Image(classLoader.getResource("images/ask.png").toString());
        askCircle.setFill(new ImagePattern(image));
        
        String recoveryDescription = "Quick description: \n\n "
                                 + "FULL - preserving full transactional log for backup. \n "
                                 + "Default database backup option.\n "
                                 + "BULK_LOGGED - minimalise transactional log for bulk operations (like generating data). \n "
                                 + "Highly recommended.\n "
                                 + "SIMPLE - will maintain only minimal amount of information for transactional log.\n "
                                 + "Recommended only when handling with test databases, where data is easily recreatable.";
        
        Tooltip tooltip = new Tooltip(recoveryDescription);
        Tooltip.install(askCircle, tooltip);
        
        fullRecovery.setDisable(true);
        fullRecovery.setVisible(false);
        bulkRecovery.setDisable(true);
        bulkRecovery.setVisible(false);
        simpleRecovery.setDisable(true);
        simpleRecovery.setVisible(false);
        askCircle.setVisible(false);
        askCircle.setDisable(true);
        chosenToggle = "FULL";        
        
        //setting click behavior.
        fullRecovery.setToggleGroup(radioButtonToggleGroup);        
        fullRecovery.setUserData("FULL");
        fullRecovery.setSelected(true);
        bulkRecovery.setToggleGroup(radioButtonToggleGroup);
        bulkRecovery.setUserData("BULK_LOGGED");
        simpleRecovery.setToggleGroup(radioButtonToggleGroup);
        simpleRecovery.setUserData("SIMPLE");
        
        recoveryModeCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() 
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
            {
                if(recoveryModeCheckBox.isSelected())
                {
                    fullRecovery.setDisable(false);
                    fullRecovery.setVisible(true);
                    fullRecovery.setSelected(true);
                    chosenToggle = radioButtonToggleGroup.getSelectedToggle().getUserData().toString();
                    bulkRecovery.setDisable(false);
                    bulkRecovery.setVisible(true);
                    simpleRecovery.setDisable(false);
                    simpleRecovery.setVisible(true);
                    askCircle.setVisible(true);
                    askCircle.setDisable(false);                    
                }
                else
                {
                    fullRecovery.setDisable(true);
                    fullRecovery.setVisible(false);
                    bulkRecovery.setDisable(true);
                    bulkRecovery.setVisible(false);
                    simpleRecovery.setDisable(true);
                    simpleRecovery.setVisible(false);
                    askCircle.setVisible(false);
                    askCircle.setDisable(true);
                }
            }
        });
                
        radioButtonToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) 
            {
                if (radioButtonToggleGroup.getSelectedToggle() != null) 
                {
                    //changing the value of chosenToggle variable.
                    chosenToggle = radioButtonToggleGroup.getSelectedToggle().getUserData().toString();
                    System.out.println("Chosen Toggle: " + chosenToggle);
                }                
            }
        });
    }
    
    //Pool table details..
    private void focusOnSelectedTask(SchedulerTask task) 
    {
        if (task != null) 
        {
            chosenTaskTableName = task.getChosenTable();
            chosenTask = task;
            System.out.println(chosenTask + " selected");
        } 
        else 
        {
            chosenTask = null;
        }
    }
    
    public void showBuildInfo()
    {
        buildInformer.setText(utility.pullBuildNumber());
    }
    
    //constructor.
    public OnlineGeneratorPanelController() 
    {
    }
    
    //Run button behavior's method.
    @FXML
    public void runTheGeneration() throws InterruptedException
    {
        boolean isInputANumber = false;
        try
        {
            //Testing if user's input is a proper number. If not, NumberFormatException should be thrown.
            int test = Integer.parseInt(numberOfRecordsField.getText());
            isInputANumber = true;
        }
        catch(NumberFormatException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            //needs to be specified.
            alert.setHeaderText("Not a number!");
            alert.setContentText
            (
                "Please enter a proper number!"
            );
            alert.showAndWait();  
        }
        //tbc. Consider adding more securities!
        if(isInputANumber == true)
        {
            //System.out.println("Number: " + numberOfRecordsField.getText());
            if(Integer.parseInt(numberOfRecordsField.getText()) > 1000000 || Integer.parseInt(numberOfRecordsField.getText()) <= 0)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("Not a proper number!");
                alert.setContentText
                (
                    "Please enter a number between 1 and 1.000.000!"
                );
                alert.showAndWait(); 
            }
            //check if all parameters are filled.
            else
            {
                ObservableList<Node> childrenList = gridPane.getChildren();
                List<String> fromTextFieldList = new ArrayList<>();
                List<String> toTextFieldList = new ArrayList<>();
                List<String> nullChanceList = new ArrayList<>();
                //Helper list for additional security for null chance list.
                List<Boolean> isEditableList = new ArrayList<>();
                int textFieldIterator = 0;
                for(int i = 0; i < childrenList.size(); i++)
                {
                    textFieldIterator++;
                    if(textFieldIterator == 3)
                    {
                        Object textField = childrenList.get(i);
                        try 
                        {
                            Method textFieldMethod = textField.getClass().getMethod("getText");
                            String textFieldText = (String) textFieldMethod.invoke(textField);
                            nullChanceList.add(textFieldText);
                            
                            //additional security.
                            Method isEditableMethod = textField.getClass().getMethod("isEditable");
                            Boolean isEditableValue = (Boolean) isEditableMethod.invoke(textField);
                            isEditableList.add(isEditableValue);
                        } 
                        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                        {
                            Logger.getLogger(OnlineGeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText("Exception error!");
                            alert.setContentText
                            (
                                "Something went wrong while extracting data from JavaFX Table!\n" +
                                "Please contact the developer! Error: " + exception.toString()                                    
                            );
                            alert.showAndWait(); 
                        }
                    }
                    if(textFieldIterator == 4)
                    {
                        Object textField = childrenList.get(i);
                        try 
                        {
                            Method textFieldMethod = textField.getClass().getMethod("getText");
                            String textFieldText = (String) textFieldMethod.invoke(textField);
                            fromTextFieldList.add(textFieldText);
                        } 
                        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                        {
                            Logger.getLogger(OnlineGeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText("Exception error!");
                            alert.setContentText
                            (
                                "Something went wrong while extracting data from JavaFX Table!\n" +
                                "Please contact the developer! Error: " + exception.toString()                                    
                            );
                            alert.showAndWait(); 
                        }
                    }
                    if(textFieldIterator == 5)
                    {
                        textFieldIterator = 0;
                        Object textField = childrenList.get(i);
                        try 
                        {
                            Method textFieldMethod = textField.getClass().getMethod("getText");
                            String textFieldText = (String) textFieldMethod.invoke(textField);
                            toTextFieldList.add(textFieldText);
                        } 
                        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
                        {
                            Logger.getLogger(OnlineGeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText("Exception error!");
                            alert.setContentText
                            (
                                "Something went wrong while extracting data from JavaFX Table!\n" +
                                "Please contact the developer! Error: " + exception.toString()                                    
                            );
                            alert.showAndWait(); 
                        }
                    }                       
                } 
                String errorMessage = "";
                for(int i = 0; i < fromTextFieldList.size(); i++)
                {
                    if(nullChanceList.get(i).equals("") || isEditableList.get(i).equals(false))
                    {
                        nullChanceList.set(i, "0");
                    }
                    try
                    {
                        int nullChanceTest = Integer.parseInt(nullChanceList.get(i));
                        if(nullChanceTest < 0 || nullChanceTest > 100)
                        {
                            errorMessage += "Null Chance is incorrect (must be equal or between 0 and 100).\n";
                        }
                    }
                    catch(NumberFormatException exception)
                    {
                        errorMessage += "One or more arguments are not correct values in Column " + (i+1) + "!\n";
                    }
                    if(fromTextFieldList.get(i).equals("") && toTextFieldList.get(i).equals(""))
                    {
                        errorMessage += "No argument is set in Column " + (i + 1) + "!\n";
                    }
                    else
                    {
                        if(fromTextFieldList.get(i).equals("") || toTextFieldList.get(i).equals(""))
                        {
                            errorMessage += "One or more arguments not set in Column " + (i + 1) + "!\n";
                        }
                        if(!fromTextFieldList.get(i).equals("NONE") || !toTextFieldList.get(i).equals("NONE"))
                        {
                            //Testing if user's input is a proper number. If not, NumberFormatException should be thrown.
                            try
                            {                                
                                long fromTextTest = Long.parseLong(fromTextFieldList.get(i));
                                long toTextTest = Long.parseLong(toTextFieldList.get(i));
                                //Testing if range is correct.
                                if(fromTextTest >= toTextTest)
                                {
                                    errorMessage += "Range is incorrect in Column " + (i + 1) + " \n(FROM field must contain smaller number than TO field).\n";
                                }
                                //Testing type ranges:
                                
                                //byte (1 byte)
                                if(columnDataTypesList.get(i).equals("tinyint"))
                                {
                                    if(fromTextTest < 0 || toTextTest > 255)
                                    {
                                        errorMessage += "Wrong range for tinyint (one byte) type (should be between 0 and 255)!\n";
                                    }
                                }
                                
                                //short(2 bytes)
                                if(columnDataTypesList.get(i).equals("smallint"))
                                {
                                    if(fromTextTest < (-32768) || toTextTest > 32767)
                                    {
                                        errorMessage += "Wrong range for smallint (two bytes) type (should be between -32.768 and 32.767)!\n";
                                    }
                                }
                                //int (4 bytes)
                                if(columnDataTypesList.get(i).equals("int"))
                                {
                                    if(fromTextTest < (-2147483648) || toTextTest > 2147483647)
                                    {
                                        errorMessage += "Wrong range for int (four bytes) type (should be between -2.147.483.648 and 2.147.483.647)!\n";
                                    }
                                }
                                //long (8 bytes)
                                if(columnDataTypesList.get(i).equals("bigint"))
                                {
                                    if(fromTextTest < -9223372036854775808L || toTextTest > 9223372036854775807L)
                                    {
                                        errorMessage += "Wrong range for bigint (eight bytes) type (should be between -9.223.372.036.854.775.808 and 9.223.372.036.854.775.807)!\n";
                                    }
                                }
                            }
                            catch(NumberFormatException exception)
                            {
                                errorMessage += "One or more arguments are not correct values in Column " + (i+1) + "!\n";
                            }
                        }
                    }                    
                }
                if(errorMessage.equals(""))
                {
                    if(chosenContentList.contains("primaryKey"))
                    {
                        int primaryKeySlotNumber = chosenContentList.indexOf("primaryKey");
                        fromTextFieldList.add(primaryKeySlotNumber, "NONE");
                        toTextFieldList.add(primaryKeySlotNumber, "NONE");
                        nullChanceList.add(primaryKeySlotNumber, "0");
                    }
                    recoveryMode = recoveryModeCheckBox.isSelected();
                    /*
                    System.out.println("==========================");
                    System.out.println("Chosen Content: " + chosenContentList);
                    System.out.println("Chosen records: " + Integer.parseInt(numberOfRecordsField.getText()));
                    System.out.println("FromTextFieldList: " + fromTextFieldList);
                    System.out.println("ToTextFieldList: " + toTextFieldList);
                    System.out.println("ChosenTableForeignKeys: " + chosenTableForeignKeys.size());
                    System.out.println("ColumnTypesList: " + columnDataTypesList);
                    System.out.println("Recovery Mode checked: " + recoveryMode);
                    System.out.println("Null chances: " + nullChanceList);
                    System.out.println("Editable list: " + isEditableList);
                    System.out.println("==========================");
                    */
                    try
                    {                        
                        DatabaseMetaData databaseMetaData = databaseUtil.getMetaData();
                        if(primaryKeyColumnName.equals("") == false)
                        {
                            ResultSet primaryKeysInfo = databaseMetaData.getColumns(null, null, chosenTable, primaryKeyColumnName);
                            if(primaryKeysInfo.next())
                            {
                                String primaryKeyType = primaryKeysInfo.getString("TYPE_NAME");
                                if(primaryKeyType.equals("int"))
                                {
                                    incrementator = databaseUtil.getTableMaxPK(primaryKeyColumnName, chosenTable);
                                }
                                else
                                {
                                    incrementator = 0;
                                }
                            }
                        }
                    }
                    catch(SQLException exception)
                    {
                        errorMessage += "Cannot get Primary Key data from Database!\n";
                    }
                }
                if(errorMessage.equals(""))
                {           
                    if(generateNow == true)
                    {
                        core.onlineGenerationProgress(chosenTable, Integer.parseInt(numberOfRecordsField.getText()), incrementator, chosenContentList, columnDataTypesList, imageDirectoryList, fromTextFieldList, toTextFieldList, chosenTableForeignKeys, databaseConnection, recoveryMode, chosenToggle, nullChanceList);
                    }
                    else
                    {
                        if(schedulerLabel.isVisible() == false)
                        {
                            schedulerLabel.setVisible(true);
                            schedulerTable.setVisible(true);
                            deleteSingleTaskButton.setVisible(true);
                            deleteSingleTaskButton.setDisable(false);
                            deleteAllTasksButton.setVisible(true);
                            deleteAllTasksButton.setDisable(false); 
                            executeSchedulerButton.setVisible(true);
                            executeSchedulerButton.setDisable(false);
                        }
                        
                        //Remedy for overriding previous tasks' contentLists...
                        Object[] arrayedChosenContentList = chosenContentList.toArray();
                        Object[] arrayedColumnDataTypesList = columnDataTypesList.toArray();
                        Object[] arrayedImageDirectoryList = imageDirectoryList.toArray();
                        //Remedy for overriding previous tasks' foreign keys...
                        List<ForeignKey> fixedForeignKeysList = new ArrayList<>();
                        for(int i = 0; i < chosenTableForeignKeys.size(); i++)
                        {
                            //Creating new Foreign Key element.
                            String foreignKeyColumn = chosenTableForeignKeys.get(i).getForeignKeyColumn();
                            String primaryKeyColumn = chosenTableForeignKeys.get(i).getPrimaryKeyColumn();
                            String foreignKeyTable = chosenTableForeignKeys.get(i).getForeignKeyTable();
                            String primaryKeyTable = chosenTableForeignKeys.get(i).getPrimaryKeyTable();
                            ForeignKey foreignKey = new ForeignKey();
                            foreignKey.setForeignKeyColumn(foreignKeyColumn);
                            foreignKey.setForeignKeyTable(foreignKeyTable);
                            foreignKey.setPrimaryKeyColumn(primaryKeyColumn);
                            foreignKey.setPrimaryKeyTable(primaryKeyTable);
                            fixedForeignKeysList.add(foreignKey); 
                        }
                        
                        SchedulerTask newTask = new SchedulerTask(chosenTable, Integer.parseInt(numberOfRecordsField.getText()), incrementator, primaryKeyColumnName, Arrays.asList(arrayedChosenContentList), Arrays.asList(arrayedColumnDataTypesList), Arrays.asList(arrayedImageDirectoryList), fromTextFieldList, toTextFieldList, fixedForeignKeysList, databaseConnection, nullChanceList);
                        
                        schedulerTable.getItems().clear(); //Clear old data.
                        schedulerTaskList.add(newTask);
                        
                        dataGatherer.addAll(schedulerTaskList);
                        schedulerTable.setItems(dataGatherer); //Load fresh data.
                        
                        if(schedulerTaskList.size() > 5)
                        {
                            schedulerTable.setPrefWidth(265);
                        }
                    }
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText("There is at least one problem!");
                    alert.setContentText(errorMessage);
                    alert.showAndWait(); 
                }                          
            }            
        }
    }
    
    public void executeScheduler() throws InterruptedException
    {
        System.out.println("TASKS TO DO: " + schedulerTaskList.size());
        
        recoveryMode = recoveryModeCheckBox.isSelected();
        
        core.schedulerGenerationProgress(schedulerTaskList, databaseConnection, recoveryMode, chosenToggle);
    }   
        
    public void testConnection()
    {
        String connectionInfo = databaseUtil.createConnection(databaseConnection);
        if (connectionInfo.equals("")) 
        {
            connectionLabel.setText("Connected to: " + databaseConnection.getDatabaseName());
            connectionLabel.setTextFill(Color.web("#006400"));
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("There is at least one problem!");
            alert.setContentText(connectionInfo);
            alert.showAndWait(); 
            core.showGeneratorPanel();  
        }
    }
    
    void initializeTableNamesChoiceBox()
    {
        tableNamesBox.getItems().clear();
        databaseTableNames.clear();
        databaseTableNames = databaseUtil.getTableNames();
        
        tableNamesBox.setMaxWidth(150);
        
        tableNamesBox.setTooltip(new Tooltip("List of available database tables")); //Tooltip's string.
        
        tableNamesBox.getItems().addAll(databaseTableNames);
        if(databaseTableNames.isEmpty())
        {
            
        }
        else
        {
            tableNamesBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() //ChoiceBox's listener.
            {
               public void changed(ObservableValue observableValue, Number tableValue, Number newTableValue) //change item listener.
                {
                    if(newTableValue.equals(-1))
                    {
                        chosenTable = null;
                        Number fixer = 1;
                        newTableValue = newTableValue.intValue() + fixer.intValue();
                        observableValue.removeListener(this);
                    }
                    chosenTable = databaseTableNames.get(newTableValue.intValue());
                    System.out.println("Chosen Table: " + chosenTable);
                    refreshGrid(chosenTable);
                } 
            });            
        }
    }
    
    //Refresh GridPane
    public void refreshGrid(String chosenTable)
    {
        deleteColumnsFromGrid();
        chosenContentList.clear();
        chosenTableForeignKeys.clear();
        initializeGrid(chosenTable);
        
    }
    
    //Delete columns from GridPane.
    public void deleteColumnsFromGrid()
    {
        try
        {
            //Catching number of columns in GridPane.
            Method method = gridPane.getClass().getDeclaredMethod("getNumberOfColumns");
            method.setAccessible(true);

            ObservableList<Node> childrenList = gridPane.getChildren();
            List<Node> killOrderList = new ArrayList<>();

            for(int i = 0; i < childrenList.size(); i++)
            {
                if(GridPane.getColumnIndex(childrenList.get(i)) != null)
                {
                    killOrderList.add(childrenList.get(i));
                }
            }
            gridPane.getChildren().removeAll(killOrderList);
        }
        catch(NoSuchMethodException e)
        {
            
        }
    }
    
    public void initializeGrid(String chosenTable)
    {
        try
        {
            DatabaseMetaData databaseMetaData = databaseUtil.getMetaData();
            ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, chosenTable);            
            primaryKeyColumnName = "";
            
            //Find primary key inside the table.
            if(primaryKeys.next())
            {
                primaryKeyColumnName = primaryKeys.getString(4);
            }
                        
            chosenTableForeignKeys.clear();
            columnDataTypesList.clear();
            
            //Gather informations about potential foreign keys within the table.
            ResultSet foreignKeys = databaseMetaData.getImportedKeys(null, null, chosenTable);
            while (foreignKeys.next()) 
            {
                ForeignKey foreignKey = new ForeignKey();
                foreignKey.setForeignKeyTable(foreignKeys.getString("FKTABLE_NAME"));
                foreignKey.setForeignKeyColumn(foreignKeys.getString("FKCOLUMN_NAME"));
                foreignKey.setPrimaryKeyTable(foreignKeys.getString("PKTABLE_NAME"));
                foreignKey.setPrimaryKeyColumn(foreignKeys.getString("PKCOLUMN_NAME"));
                chosenTableForeignKeys.add(foreignKey);
            }
                       
            ResultSetMetaData metaData = databaseUtil.getTableMetaData(chosenTable);
            int numberOfColumns = metaData.getColumnCount();
            int columnNumber = 0;
            Boolean firstColumn = true;
            for(int i = 0; i < numberOfColumns; i++)
            {
                String columnName = metaData.getColumnName(i + 1);
                if(columnName.equals(primaryKeyColumnName))
                {       
                    if(columnNumber <= 0 && firstColumn == true)
                    {
                        columnNumber--;  
                    }
                    chosenContentList.add("primaryKey");                    
                }
                else
                {     
                    if(metaData.getColumnName(1).equals(primaryKeyColumnName) == false && firstColumn == true)
                    {
                        columnNumber = -1;
                        firstColumn = false;
                    }
                    String columnDataType = metaData.getColumnTypeName(i + 1);
                    //Checking if column is nullable - 1 if yes, 0 if no.
                    int isNullable = metaData.isNullable(i + 1);
                    if(columnDataType.equals("varbinary") || columnDataType.equals("image"))
                    {
                        if(imageDirectoryList.isEmpty())
                        {
                            deleteColumnsFromGrid();
                            chosenContentList.clear();
                            chosenTableForeignKeys.clear();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("No images in generation pool!");
                            alert.setContentText("This table can contain images (has varbinary or image type column)!\nPlease add at least one directory with images into ImageContainer folder!\n(See: Manage Images option in Main Menu).");
                            alert.showAndWait();                            
                            break;
                        }
                    }
                    if(chosenTableForeignKeys.isEmpty() == false)
                    {
                        Boolean foreignKeyFound = false;
                        for(ForeignKey foreignKey: chosenTableForeignKeys)
                        {
                            if(foreignKey.getForeignKeyColumn().equals(columnName))
                            {
                                foreignKeyFound = true;
                                columnNumber++;
                                Label label = new Label(columnName);
                                label.setMaxWidth(125);
                                label.setAlignment(Pos.CENTER);
                                label.setTextAlignment(TextAlignment.CENTER);
                                
                                TextField nullChanceField = new TextField();
                                nullChanceField.setMaxWidth(125);
                                nullChanceField.setPromptText("Chance for null value");                                                             
                                TextField fromTextField = new TextField();
                                fromTextField.setMaxWidth(125);
                                fromTextField.setPromptText("FROM (int value)");
                                TextField toTextField = new TextField();
                                toTextField.setMaxWidth(125);
                                toTextField.setPromptText("TO (int value)");
                                                                                               
                                Label foreignKeyLabel = new Label(" (FK on " + foreignKey.getPrimaryKeyTable() + ") ");
                                foreignKeyLabel.setMaxWidth(125);
                                foreignKeyLabel.setAlignment(Pos.CENTER);
                                foreignKeyLabel.setTextAlignment(TextAlignment.CENTER);
                                   
                                nullChanceField.setVisible(true);                                
                                nullChanceField.setEditable(false);
                                nullChanceField.setText("Not-null column.");
                                nullChanceField.setTooltip(new Tooltip("This column has NOT NULL index."));
                                fromTextField.setVisible(false);
                                fromTextField.setDisable(true);                                
                                fromTextField.setText("NONE");
                                toTextField.setVisible(false);
                                toTextField.setDisable(true);
                                toTextField.setText("NONE");                                
                                
                                chosenContentList.add("foreignKey");
                                
                                gridPane.addColumn(columnNumber, label, foreignKeyLabel, nullChanceField, fromTextField, toTextField);
                                
                                columnDataTypesList.add("foreign key");
                            }
                        }
                        if(foreignKeyFound == false)
                        {
                            columnNumber++;
                            Label label = new Label(columnName);
                            label.setMaxWidth(125);
                            label.setAlignment(Pos.CENTER);
                            label.setTextAlignment(TextAlignment.CENTER);

                            TextField nullChanceField = new TextField();
                            nullChanceField.setMaxWidth(125);
                            nullChanceField.setPromptText("Chance for null value");
                            nullChanceField.setTooltip(new Tooltip("Insert value between 0 and 100 (if nothing is inserted, default value is 0)"));
                            TextField fromTextField = new TextField();
                            fromTextField.setMaxWidth(125);
                            fromTextField.setPromptText("FROM (int value)");
                            TextField toTextField = new TextField();
                            toTextField.setMaxWidth(125);
                            toTextField.setPromptText("TO (int value)");                            
                            
                            ChoiceBox choiceBox = new ChoiceBox();
                            initializeChoiceBox(choiceBox, columnDataType, i, fromTextField, toTextField, nullChanceField, isNullable); 

                            gridPane.addColumn(columnNumber, label, choiceBox, nullChanceField, fromTextField, toTextField);
                            columnDataTypesList.add(columnDataType);
                            System.out.println("COLUMN: " + columnDataTypesList);
                        }
                    }
                    else
                    {
                        columnNumber++;
                        Label label = new Label(columnName);
                        label.setMaxWidth(125);
                        label.setAlignment(Pos.CENTER);
                        label.setTextAlignment(TextAlignment.CENTER);

                        TextField nullChanceField = new TextField();
                        nullChanceField.setMaxWidth(125);
                        nullChanceField.setPromptText("Chance for null value");
                        nullChanceField.setTooltip(new Tooltip("Insert value between 0 and 100 (if nothing is inserted, default value is 0)"));
                        TextField fromTextField = new TextField();
                        fromTextField.setMaxWidth(125);
                        fromTextField.setPromptText("FROM (int value)");
                        TextField toTextField = new TextField();
                        toTextField.setMaxWidth(125);
                        toTextField.setPromptText("TO (int value)");                        

                        ChoiceBox choiceBox = new ChoiceBox();
                        initializeChoiceBox(choiceBox, columnDataType, i, fromTextField, toTextField, nullChanceField, isNullable); 

                        gridPane.addColumn(columnNumber, label, choiceBox, nullChanceField, fromTextField, toTextField);
                        
                        columnDataTypesList.add(columnDataType);
                        System.out.println("COLUMN: " + columnDataTypesList);
                    }                    
                }                
            }
            if(numberOfColumns > 7)
            {
                scrollPane.setPrefHeight(158);
            }
            else
            {
                scrollPane.setPrefHeight(145);
            }
        }
        catch(SQLException e)
        {
            
        }
    }
    
    void initializeChoiceBox(ChoiceBox choiceBox, String columnDataType, int columnNumber, TextField fromTextField, TextField toTextField, TextField nullChanceField, int isNullable)
    {
        choiceBox.setMaxWidth(125);
        
        choiceBox.setTooltip(new Tooltip("List of available generation data")); //Tooltip's string.
        
        final List<String> fixedContentList = initializeContentList(columnDataType);
        choiceBox.getItems().addAll(fixedContentList);
        for (Method method: GenerationMethods.class.getDeclaredMethods()) 
        {
            if(fixedContentList.isEmpty() == false)
            {
                if(method.getName().equals(fixedContentList.get(0)))
                {
                    if(method.getParameterCount() == 0)
                    {
                        fromTextField.setText("NONE");
                        fromTextField.setVisible(false);
                        fromTextField.setDisable(true);                                
                        toTextField.setText("NONE");
                        toTextField.setVisible(false);
                        toTextField.setDisable(true);
                    }
                    else
                    {
                        fromTextField.setText("");
                        fromTextField.setVisible(true);
                        fromTextField.setDisable(false);                                
                        toTextField.setText("");
                        toTextField.setVisible(true);
                        toTextField.setDisable(false);
                    }
                    break;
                }
            }            
        }
        
        //Image related columns cannot have from and toTextField (illogical).
        if(columnDataType.equals("varbinary") || columnDataType.equals("image"))
        {
            fromTextField.setText("NONE");
            fromTextField.setVisible(false);
            fromTextField.setDisable(true);                                
            toTextField.setText("NONE");
            toTextField.setVisible(false);
            toTextField.setDisable(true);
        }
        
        if(isNullable == 0)
        {
            nullChanceField.setVisible(true);
            nullChanceField.setText("Not-null column.");
            nullChanceField.setTooltip(new Tooltip("This column has NOT NULL index."));
            nullChanceField.setEditable(false);
        }        
        
        if(fixedContentList.isEmpty() == false)
        {
            choiceBox.getSelectionModel().selectFirst();
        }        
        
        //Creating new slot in activities list.
        chosenContentList.add(fixedContentList.get(0));
        
        //System.out.println(chosenContentList);
        
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() //ChoiceBox's listener.
        {
           public void changed(ObservableValue observableValue, Number contentValue, Number newContentValue) //change item listener.
            {
                if(newContentValue.equals(-1))
                {
                    chosenContent = null;
                    Number fixer = 1;
                    newContentValue = newContentValue.intValue() + fixer.intValue();
                    observableValue.removeListener(this);
                }
                chosenContent = fixedContentList.get(newContentValue.intValue());
                chosenContentList.set((columnNumber), chosenContent);
                System.out.println("Chosen Content: " + chosenContentList);
                
                if(!isContentUppercase(fixedContentList.get(newContentValue.intValue())))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getName().equals(chosenContent))
                        {
                            if(method.getParameterCount() == 0)
                            {
                                fromTextField.setText("NONE");
                                fromTextField.setVisible(false);
                                fromTextField.setDisable(true);                                
                                toTextField.setText("NONE");
                                toTextField.setVisible(false);
                                toTextField.setDisable(true);
                            }
                            else
                            {
                                fromTextField.setText("");
                                fromTextField.setVisible(true);
                                fromTextField.setDisable(false);                                
                                toTextField.setText("");
                                toTextField.setVisible(true);
                                toTextField.setDisable(false);
                            }
                            break;
                        }
                    }
                    if(imageDirectoryList.contains(fixedContentList.get(newContentValue.intValue())))
                    {
                        fromTextField.setText("NONE");
                        fromTextField.setVisible(false);
                        fromTextField.setDisable(true);                                
                        toTextField.setText("NONE");
                        toTextField.setVisible(false);
                        toTextField.setDisable(true);
                    }
                }   
                else
                {
                    fromTextField.setText("NONE");
                    fromTextField.setVisible(false);
                    fromTextField.setDisable(true);                                
                    toTextField.setText("NONE");
                    toTextField.setVisible(false);
                    toTextField.setDisable(true);
                }
            } 
        });
    }
    
    List<String> initializeContentList(String columnDataType)
    {
        List<String> fixedContentList = new ArrayList<>();
        
        //Varbinary (image) types.
        if(columnDataType.equals("varbinary") || columnDataType.equals("image"))
        {
            fixedContentList = imageDirectoryList;
            return fixedContentList;
        }
        
        //String types.
        if(columnDataType.equals("varchar") || columnDataType.equals("nvarchar") || columnDataType.equals("text") || columnDataType.equals("ntext"))
        {
            fixedContentList = allContentList;
            return fixedContentList;
        }
        
        //int types.        
        if(columnDataType.equals("tinyint"))
        {
            for(int i = 0; i < allContentList.size(); i++)
            {
                if(!isContentUppercase(allContentList.get(i)))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getReturnType().toString().equals(columnDataType) || method.getReturnType().toString().equals("byte"))
                        {
                            if(fixedContentList.contains(method.getName()) == false)
                            {
                                fixedContentList.add(method.getName());
                            } 
                        }
                    }
                }   
                else
                {
                    System.out.println("NOT YET");
                }
            }
            return fixedContentList;
        }
        
        if(columnDataType.equals("smallint"))
        {
            for(int i = 0; i < allContentList.size(); i++)
            {
                if(!isContentUppercase(allContentList.get(i)))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getReturnType().toString().equals(columnDataType) || method.getReturnType().toString().equals("short"))
                        {
                            if(fixedContentList.contains(method.getName()) == false)
                            {
                                fixedContentList.add(method.getName());
                            } 
                        }
                    }
                }   
                else
                {
                    System.out.println("NOT YET");
                }
            }
            return fixedContentList;
        }
        
        if(columnDataType.equals("int"))
        {
            for(int i = 0; i < allContentList.size(); i++)
            {
                if(!isContentUppercase(allContentList.get(i)))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getReturnType().toString().equals(columnDataType) || method.getReturnType().toString().equals("int") || method.getReturnType().toString().equals("Integer"))
                        {
                            if(fixedContentList.contains(method.getName()) == false)
                            {
                                fixedContentList.add(method.getName());
                            } 
                        }
                    }
                }   
                else
                {
                    System.out.println("NOT YET");
                }
            }
            return fixedContentList;
        }
        
        if(columnDataType.equals("bigint"))
        {
            for(int i = 0; i < allContentList.size(); i++)
            {
                if(!isContentUppercase(allContentList.get(i)))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getReturnType().toString().equals(columnDataType) || method.getReturnType().toString().equals("long"))
                        {
                            if(fixedContentList.contains(method.getName()) == false)
                            {
                                fixedContentList.add(method.getName());
                            } 
                        }
                    }
                }   
                else
                {
                    System.out.println("NOT YET");
                }
            }
            return fixedContentList;
        }
        
        //DB float types.
        if(columnDataType.equals("float") || columnDataType.equals("real"))
        {
            for(int i = 0; i < allContentList.size(); i++)
            {
                if(!isContentUppercase(allContentList.get(i)))
                {                        
                    for (Method method: GenerationMethods.class.getDeclaredMethods()) 
                    {
                        if(method.getReturnType().toString().equals(columnDataType) || method.getReturnType().toString().equals("float") || method.getReturnType().toString().equals("double"))
                        {
                            if(fixedContentList.contains(method.getName()) == false)
                            {
                                fixedContentList.add(method.getName());
                            }                            
                        }
                    }
                }   
                else
                {
                    System.out.println("NOT YET");
                }
            }
            return fixedContentList;
        }
        return null;
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
    
    //Delete task from scheduler:
    @FXML
    public void handleDeleteSingleTask()
    {        
        int selectedIndex = schedulerTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) 
        {
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", okButton, cancelButton);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure to delete selected task?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == okButton)
            {
                int selectedTaskIndex = schedulerTaskList.indexOf(chosenTask);
                schedulerTable.getItems().clear(); //Clear old data.
                schedulerTaskList.remove(selectedTaskIndex);
                dataGatherer.addAll(schedulerTaskList);
                schedulerTable.setItems(dataGatherer); //Load fresh data.
                
                if(schedulerTaskList.size() < 6)
                {
                    schedulerTable.setPrefWidth(252);
                }

                if(schedulerTaskList.isEmpty())
                {
                    schedulerLabel.setVisible(false);
                    schedulerTable.setVisible(false);
                    deleteSingleTaskButton.setVisible(false);
                    deleteSingleTaskButton.setDisable(true);
                    deleteAllTasksButton.setVisible(false);
                    deleteAllTasksButton.setDisable(true); 
                    executeSchedulerButton.setVisible(false);
                    executeSchedulerButton.setDisable(true);
                }
            } 
            else 
            {
                alert.close();
            } 
        }
        else 
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No task selected!");
            alert.setContentText("Please choose one of listed tasks!");
            alert.showAndWait();
        }
    }
    
    //Delete task from scheduler:
    @FXML
    public void handleDeleteAllTasks()
    {        
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", okButton, cancelButton);
        alert.setTitle("Exit");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure to delete all tasks?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okButton)
        {
            schedulerTable.getItems().clear(); //Clear old data.
            schedulerTaskList.clear();
            schedulerLabel.setVisible(false);
            schedulerTable.setVisible(false);
            deleteSingleTaskButton.setVisible(false);
            deleteSingleTaskButton.setDisable(true);
            deleteAllTasksButton.setVisible(false);
            deleteAllTasksButton.setDisable(true); 
            executeSchedulerButton.setVisible(false);
            executeSchedulerButton.setDisable(true);
        } 
        else 
        {
            alert.close();
        } 
    }
    
    //Back button behavior's method.
    @FXML
    public void handleDisconnect()
    {
        core.showGeneratorPanel();  
    }
    
    //Refresh button behavior's method.
    @FXML
    public void handleRefresh()
    {
        initializeTableNamesChoiceBox();
        deleteColumnsFromGrid();
        recoveryModeCheckBox.setSelected(false);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Success!");
        alert.setContentText("Tables refreshed!");
        alert.showAndWait(); 
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleAdd(ActionEvent event)
    {        
        boolean okClicked = core.showAddTable(databaseConnection);
        if(okClicked)
        {
            okClicked = false;
            initializeTableNamesChoiceBox();
            deleteColumnsFromGrid();
            recoveryModeCheckBox.setSelected(false);
        }      
    }
       
    //Back button behavior's method.
    @FXML
    public void handleBack()
    {
        core.showWelcome(); 
    }
}