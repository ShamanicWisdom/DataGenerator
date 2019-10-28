/*
        --DataGenerator--
    GeneratorController class.
*/

package szaman.datagenerator.view.generation;

//Other own classes:

import szaman.datagenerator.Core;
import szaman.datagenerator.utility.Utility;
import szaman.datagenerator.utility.GenerationMethods;
import szaman.datagenerator.utility.DataPoolUtility;
import szaman.datagenerator.view.generation.model.DatabaseConnection;

//Other classes:

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Szaman
 */

public class GeneratorPanelController
{    
    @FXML
    private Label buildInformer;
    @FXML
    private Label noRecordsLabel;
    
    
    @FXML
    private TextField numberOfRecordsField;
        
    @FXML
    private Button runButton;
    @FXML
    private Button testAddButton;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private GridPane gridPane;
    
    @FXML
    private CheckBox includeHeaderBox;
    @FXML
    private CheckBox includeIncrementationBox;
    
    //Radio buttons.
    @FXML
    private RadioButton excelRadioButton;
    @FXML
    private RadioButton csvRadioButton;
    @FXML
    private RadioButton jsonRadioButton;
    
    final ToggleGroup radioButtonToggleGroup = new ToggleGroup();    
    
    String chosenToggle = "Excel";
    
    private Core core;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    List<String> allContentList = new ArrayList<>();
    List<String> methodsList = new ArrayList<>();
    List<String> tablesList = new ArrayList<>();    
    
    List<String> chosenContentList = new ArrayList<>();
    
    String chosenContent = null;

    public void setCore(Core core)
    {
        this.core = core;
        
        //setting click behavior.
        excelRadioButton.setToggleGroup(radioButtonToggleGroup);        
        excelRadioButton.setUserData("Excel");
        excelRadioButton.setSelected(true);
        csvRadioButton.setToggleGroup(radioButtonToggleGroup);
        csvRadioButton.setUserData("CSV");
        jsonRadioButton.setToggleGroup(radioButtonToggleGroup);
        jsonRadioButton.setUserData("JSON");
        
        radioButtonToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) 
            {
                if (radioButtonToggleGroup.getSelectedToggle() != null) 
                {
                    //changing the value of chosenToggle variable.
                    chosenToggle = radioButtonToggleGroup.getSelectedToggle().getUserData().toString();
                    System.out.println("Chosen Toggle: " + chosenToggle);
                    if(chosenToggle.equals("Excel") || chosenToggle.equals("CSV"))
                    {
                        includeHeaderBox.setDisable(false);
                        includeHeaderBox.setVisible(true);
                    }
                    else
                    {
                        includeHeaderBox.setDisable(true);
                        includeHeaderBox.setVisible(false);
                        includeHeaderBox.setSelected(false);
                    }
                }                
            }
        });
        
        showBuildInfo();     
        //Adding embedded methods to methodList
        for (Method method: GenerationMethods.class.getDeclaredMethods()) 
        {
            String name = method.getName();
            methodsList.add(name);
        }
        //Adding database pool tables to methodList.
        dataPoolUtil.createConnection();
        tablesList = dataPoolUtil.getNonEmptyTableNames(tablesList);
        
        //Concatenate both types of activities.
        allContentList.addAll(methodsList);
        allContentList.addAll(tablesList);
        
        //first column initialization.
        try 
        {
            initializeFirstColumnToGrid();
        } 
        catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
        {
            Logger.getLogger(GeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
        }
    }
    
    Utility utility = new Utility();
    
    public void showBuildInfo()
    {
        buildInformer.setText(utility.pullBuildNumber());
    }
    
    //constructor.
    public GeneratorPanelController() 
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
                            Logger.getLogger(GeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
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
                            Logger.getLogger(GeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
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
                    if(nullChanceList.get(i).equals(""))
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
                                int fromTextTest = Integer.parseInt(fromTextFieldList.get(i));
                                int toTextTest = Integer.parseInt(toTextFieldList.get(i));
                                //Testing if range is correct.
                                if(fromTextTest >= toTextTest)
                                {
                                    errorMessage += "Range is incorrect in Column " + (i + 1) + " \n(FROM field must contain smaller number than TO field).\n";
                                }
                            }
                            catch(NumberFormatException exception)
                            {
                                errorMessage += "One or more arguments are not integer values in Column " + (i+1) + "!\n";
                            }
                        }
                    }                    
                }
                
                if(includeIncrementationBox.isSelected() == true)
                {
                    if(chosenContentList.get(0).equals("incrementation") == false)
                    {
                        chosenContentList.add(0, "incrementation");                        
                        System.out.println("CHOSEN CONTENT LIST: " + chosenContentList);
                    }         
                    fromTextFieldList.add(0, "NONE");
                    toTextFieldList.add(0, "NONE");
                    nullChanceList.add(0, "0");
                }
                else
                {
                    if(chosenContentList.get(0).equals("incrementation") == true)
                    {
                        chosenContentList.remove(0);
                        System.out.println("CHOSEN CONTENT LIST: " + chosenContentList);
                    }              
                }
                
                if(errorMessage.equals(""))
                {
                    Boolean includeHeaders = includeHeaderBox.isSelected();
                    System.out.println("NULL CHANCES: " + nullChanceList);
                    core.generationProgress(Integer.parseInt(numberOfRecordsField.getText()), chosenToggle, includeHeaders, chosenContentList, fromTextFieldList, toTextFieldList, nullChanceList);
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
    
    //Database connection button behavior's method.
    @FXML
    private void handleDatabaseConnection(ActionEvent event) throws IOException, SQLException, ClassNotFoundException 
    {
        //DatabaseConnection databaseConnection = new DatabaseConnection();
        DatabaseConnection databaseConnection = core.databaseConnection();
        //System.out.println("CATCH: " + databaseConnection.getConnectionURL());
        if(databaseConnection.getConnectionCorrectness()) 
        {
            core.showOnlineGeneratorPanel(databaseConnection);
        }
    }
    
    //Add column to GridPane.
    @FXML
    public void addColumnToGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching columns count in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfColumns");
        method.setAccessible(true);
        Integer columnsCount = (Integer) method.invoke(gridPane);
        
        Label label = new Label("Column " + (columnsCount + 1));
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
        
        ChoiceBox choiceBox = new ChoiceBox();
        initializeChoiceBox(choiceBox, label.getText(), fromTextField, toTextField, nullChanceField);   
        
        gridPane.addColumn(columnsCount, label, choiceBox, nullChanceField, fromTextField, toTextField);
        
        columnsCount = (Integer) method.invoke(gridPane);
        if(columnsCount > 7)
        {
            scrollPane.setPrefHeight(157);
        }
        //System.out.println("Columns: " + columnsCount);        
    }
    
    //Delete column from GridPane.
    @FXML
    public void deleteColumnFromGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching number of columns in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfColumns");
        method.setAccessible(true);
        Integer columnsCount = (Integer) method.invoke(gridPane);
        
        ObservableList<Node> childrenList = gridPane.getChildren();
        List<Node> killOrderList = new ArrayList<>();
        
        for(int i = 0; i < childrenList.size(); i++)
        {
            if(GridPane.getColumnIndex(childrenList.get(i)) != null)
            {
                if(GridPane.getColumnIndex(childrenList.get(i)) == (columnsCount - 1))
                {
                    killOrderList.add(childrenList.get(i));
                }
            }
        }
        if(columnsCount != 1)
        {
            gridPane.getChildren().removeAll(killOrderList);
            
            //Deleting newest record from chosenContentList.
            chosenContentList.remove((columnsCount - 1));   
        }             
                
        columnsCount = (Integer) method.invoke(gridPane);
        if(columnsCount < 8)
        {
            scrollPane.setPrefHeight(145);
        }
        //System.out.println("Columns: " + columnsCount);
        //System.out.println(chosenContentList);
    }
    
    //Add first column to GridPane.
    public void initializeFirstColumnToGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching columns count in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfColumns");
        method.setAccessible(true);
        Integer columnsCount = (Integer) method.invoke(gridPane);
        
        Label label = new Label("Column " + (columnsCount));
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
        
        ChoiceBox choiceBox = new ChoiceBox();
        initializeChoiceBox(choiceBox, label.getText(), fromTextField, toTextField, nullChanceField); 
        
        gridPane.addColumn(0, label, choiceBox, nullChanceField, fromTextField, toTextField);
        scrollPane.setPrefHeight(145);   
    }
    
    void initializeChoiceBox(ChoiceBox choiceBox, String labelTextSource, TextField fromTextField, TextField toTextField, TextField nullChanceField)
    {
        labelTextSource = labelTextSource.replaceAll("\\D+","");
        int columnNumber = Integer.parseInt(labelTextSource);
                
        choiceBox.setMaxWidth(125);
        
        choiceBox.setTooltip(new Tooltip("List of available generation data")); //Tooltip's string.
        
        choiceBox.getItems().addAll(allContentList);
        
        nullChanceField.setMaxWidth(125);
        nullChanceField.setPromptText("Chance for null value");
        nullChanceField.setTooltip(new Tooltip("Insert value between 0 and 100 (if nothing is inserted, default value is 0)"));
                                
        for (Method method: GenerationMethods.class.getDeclaredMethods()) 
        {
            if(method.getName().equals(allContentList.get(0)))
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
        
        choiceBox.getSelectionModel().selectFirst();
        
        //Creating new slot in activities list.
        chosenContentList.add(allContentList.get(0));
        
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
                chosenContent = allContentList.get(newContentValue.intValue());
                chosenContentList.set((columnNumber - 1), chosenContent);
                System.out.println("Chosen Content: " + chosenContentList);
                
                if(!isContentUppercase(allContentList.get(newContentValue.intValue())))
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
       
    //Back button behavior's method.
    @FXML
    public void handleBack()
    {
        core.showWelcome(); 
    }
}