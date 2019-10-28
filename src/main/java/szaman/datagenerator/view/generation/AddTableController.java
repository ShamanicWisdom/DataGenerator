/*
        --DataGenerator--
    AddTableController class.
*/

package szaman.datagenerator.view.generation;

//Other own classes:

import szaman.datagenerator.utility.DatabaseUtility;
import szaman.datagenerator.view.generation.model.DatabaseConnection;

//Other classes:

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Szaman
 */

public class AddTableController 
{
    @FXML
    private TextField tableNameField;
    @FXML
    private GridPane gridPane;
    
    private Stage dialogStage;
    
    private boolean okClicked = false;
    
    List<String> typesList =  new ArrayList<String>(Arrays.asList("PK", "FK", "tinyint", "smallint", "int", "bigint", "float", "real", "varchar", "nvarchar", "text", "ntext", "varbinary"));
    
    DatabaseUtility databaseUtil = new DatabaseUtility();
    
    String chosenTableName = "";
    
    String chosenType = "";
    String chosenRelation = "";
    
    //Database tables List.
    List<String> databaseTablesList = new ArrayList<>();  
    
    //List of credentials needed for making a new table.
    List<String> chosenNamesList = new ArrayList<>();
    List<String> chosenTypesList = new ArrayList<>();
    List<String> chosenRelationsList = new ArrayList<>();
    
    DatabaseConnection databaseConnection;
    
    @FXML
    private void initialize() 
    {
    }

    //Setting the Dialog Stage.
    public void setDialogStage(Stage dialogStage, DatabaseConnection databaseConnection)
    {
        this.dialogStage = dialogStage;
        this.databaseConnection = databaseConnection;
        
        testConnection();
        databaseTablesList = databaseUtil.getTableNames();
        
        //first column initialization.
        try 
        {
            initializeFirstRowToGrid();
        } 
        catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) 
        {
            Logger.getLogger(GeneratorPanelController.class.getName()).log(Level.SEVERE, null, exception);
        }
    }
    
    //OK clicking button behavior's method logic.
    public boolean isOkClicked() 
    {
        return okClicked;
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
    
    //Add row to GridPane.
    @FXML
    public void addRowToGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching columns count in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
        method.setAccessible(true);
        Integer rowsCount = (Integer) method.invoke(gridPane);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Row Name");
        
        ChoiceBox typeChoiceBox = new ChoiceBox();        
        ChoiceBox relationChoiceBox = new ChoiceBox();
        CheckBox allowNullsBox = new CheckBox();
        allowNullsBox.setAlignment(Pos.CENTER);
        allowNullsBox.setMaxWidth(200);
        initializeChoiceBoxes(typeChoiceBox, (rowsCount + 1), relationChoiceBox, allowNullsBox); 
        
        gridPane.addRow(rowsCount, nameField, typeChoiceBox, relationChoiceBox, allowNullsBox);  
    }
    
    //Delete row from GridPane.
    @FXML
    public void deleteRowFromGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching number of columns in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
        method.setAccessible(true);
        Integer rowsCount = (Integer) method.invoke(gridPane);
        
        ObservableList<Node> childrenList = gridPane.getChildren();
        List<Node> killOrderList = new ArrayList<>();
        
        for(int i = 0; i < childrenList.size(); i++)
        {
            if(GridPane.getRowIndex(childrenList.get(i)) != null)
            {
                if(GridPane.getRowIndex(childrenList.get(i)) == (rowsCount - 1))
                {
                    killOrderList.add(childrenList.get(i));
                }
            }
        }
        if(rowsCount != 1)
        {
            gridPane.getChildren().removeAll(killOrderList);            
            chosenTypesList.remove(chosenTypesList.size() - 1);
            chosenRelationsList.remove(chosenRelationsList.size() - 1);
        }  
    }
    
    //Add first column to GridPane.
    public void initializeFirstRowToGrid() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        //Catching columns count in GridPane.
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
        method.setAccessible(true);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Row Name");
        
        ChoiceBox typeChoiceBox = new ChoiceBox();
        ChoiceBox relationChoiceBox = new ChoiceBox();
        CheckBox allowNullsBox = new CheckBox();
        allowNullsBox.setAlignment(Pos.CENTER);
        allowNullsBox.setMaxWidth(200);
        
        initializeChoiceBoxes(typeChoiceBox, 1, relationChoiceBox, allowNullsBox);
        
        gridPane.addRow(0, nameField, typeChoiceBox, relationChoiceBox, allowNullsBox);     
    }
    
    //ChoiceBox initialization.
    void initializeChoiceBoxes(ChoiceBox choiceBox, int rowNumber, ChoiceBox relationChoiceBox, CheckBox allowNullsBox)
    {
        choiceBox.setMaxWidth(145);
        relationChoiceBox.setMaxWidth(145);
        
        relationChoiceBox.setVisible(false);
        relationChoiceBox.setDisable(true);
        
        allowNullsBox.setVisible(false);
        allowNullsBox.setDisable(true);
        
        choiceBox.setTooltip(new Tooltip("List of available column type")); //Tooltip's string.
        
        choiceBox.getItems().addAll(typesList);
        relationChoiceBox.getItems().addAll(databaseTablesList);
        
        if(typesList.isEmpty() == false)
        {
            choiceBox.getSelectionModel().selectFirst();
        }        
        
        //Creating new slot in activities list.
        chosenTypesList.add(typesList.get(0));
        chosenRelationsList.add("NONE");
        
        //System.out.println(chosenContentList);
        
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() //ChoiceBox's listener.
        {
           public void changed(ObservableValue observableValue, Number contentValue, Number newContentValue) //change item listener.
            {
                if(newContentValue.equals(-1))
                {
                    chosenType = null;
                    Number fixer = 1;
                    newContentValue = newContentValue.intValue() + fixer.intValue();
                    observableValue.removeListener(this);
                }
                chosenType = typesList.get(newContentValue.intValue());
                chosenTypesList.set((rowNumber - 1), chosenType);
                System.out.println("Chosen Types: " + chosenTypesList);
                
                //Behavior of relationChoiceBox.
                if(chosenType.equals("FK"))
                {
                    relationChoiceBox.setVisible(true);
                    relationChoiceBox.setDisable(false);
                    relationChoiceBox.getSelectionModel().selectFirst();
                    chosenRelationsList.set((rowNumber - 1), databaseTablesList.get(0));
                }
                else
                {
                    relationChoiceBox.setVisible(false);
                    relationChoiceBox.setDisable(true);
                    chosenRelationsList.set((rowNumber - 1), "NONE");
                }
                
                //Behavior of allowNullsBox.
                if(chosenType.equals("PK") || chosenType.equals("FK"))
                {
                    allowNullsBox.setSelected(false);
                    allowNullsBox.setVisible(false);
                    allowNullsBox.setDisable(true);
                }
                else
                {
                    allowNullsBox.setVisible(true);
                    allowNullsBox.setDisable(false);
                }
            } 
        });
        
        relationChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() //ChoiceBox's listener.
        {
           public void changed(ObservableValue observableValue, Number contentValue, Number newContentValue) //change item listener.
            {
                if(newContentValue.equals(-1))
                {
                    chosenType = null;
                    Number fixer = 1;
                    newContentValue = newContentValue.intValue() + fixer.intValue();
                    observableValue.removeListener(this);
                }
                chosenRelation = databaseTablesList.get(newContentValue.intValue());
                chosenRelationsList.set((rowNumber - 1), chosenRelation);
                System.out.println("Chosen Relations: " + chosenRelationsList);                
            } 
        });
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleAdd()
    {
        try
        {
            if (validator()) 
            {
                okClicked = true;
                dialogStage.close();
            }
        }
        catch(NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
        {
            
        }
        
    }
    
    private boolean validator() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
        method.setAccessible(true);
        Integer rowsCount = (Integer) method.invoke(gridPane);
        System.out.println("ROWS: " + rowsCount);
        ObservableList<Node> childrenList = gridPane.getChildren();
        List<String> chosenNamesList = new ArrayList<>();
        List<Boolean> allowNullsList = new ArrayList<>();
        int fieldIterator = 0;
        for(int i = 0; i < childrenList.size(); i++)
        {
            fieldIterator++;
            if(fieldIterator == 1)
            {
                Object nameField = childrenList.get(i);
                try 
                {
                    Method nameFieldMethod = nameField.getClass().getMethod("getText");
                    String nameFieldText = (String) nameFieldMethod.invoke(nameField);
                    chosenNamesList.add(nameFieldText);
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
            if(fieldIterator == 4)
            {
                Object checkBox = childrenList.get(i);
                try 
                {
                    Method checkBoxMethod = checkBox.getClass().getMethod("isSelected");
                    Boolean isCheckBoxSelected = (Boolean) checkBoxMethod.invoke(checkBox);
                    allowNullsList.add(isCheckBoxSelected);
                    fieldIterator = 0;
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
        
        System.out.println("======================");
        System.out.println("TableName: " + tableNameField.getText());
        System.out.println("Chosen Names: " + chosenNamesList);
        System.out.println("Chosen Types: " + chosenTypesList);
        System.out.println("Chosen Relations: " + chosenRelationsList);
        System.out.println("Allow Nulls: " + allowNullsList);
        System.out.println("======================");
        
        String errorMessage = ""; //Empty string for all catched errors.
        
        //1. No table name:
        if(tableNameField.getText() == null || tableNameField.getText().length() == 0)
        {
            errorMessage += "No Database Table's name!" + "\n";
        }
        //2. Table name exists in database.
        else
        {
            if(databaseTablesList.contains(tableNameField.getText()))
            {
                errorMessage += "Database Table's name exists!" + "\n";
            }
        }
        //3. Empty column names:
        for(int i = 0; i < chosenNamesList.size(); i++)
        {
            if(chosenNamesList.get(i).length() == 0)
            {
                errorMessage += "Column " + (i + 1) + " has no specified name!" + "\n";
            }
        }
        //4. Duplicated column names:
        Set<String> hashSet = new HashSet<>(); 
        int duplicationIterator = 0;
        for(String columnName : chosenNamesList) 
        { 
            duplicationIterator++;
            if(hashSet.add(columnName) == false) 
            { 
                errorMessage += "Duplicated Column Name: \'" + columnName + "\' in Column " + duplicationIterator + "!\n";                        
            } 
        } 
        //5. More than one PK:
        boolean isPKFound = false;
        for(int i = 0; i < chosenTypesList.size(); i++)
        {
            if(chosenTypesList.get(i).equals("PK"))
            {
                if(isPKFound == true)
                {
                    errorMessage += "Duplicated PK type in column: \'" + chosenNamesList.get(i) + "\'!\n";  
                }
                else
                {
                    isPKFound = true;
                }                
            }
        }
        //6. More than one relation for one table.
        Set<String> relationHashSet = new HashSet<>(); 
        int relationIterator = 0;
        for(String relationName : chosenRelationsList) 
        { 
            relationIterator++;
            if(relationName.equals("NONE") == false)
            {
                if(relationHashSet.add(relationName) == false) 
                { 
                    errorMessage += "Duplicated Relation: \'" + relationName + "\' in Column " + relationIterator + "!\n";                        
                } 
            }            
        }        
        
        //If zero errors:
        if (errorMessage.length() == 0) 
        {
            for(int i = 0; i < chosenNamesList.size(); i++)
            {
                String name = chosenNamesList.get(i);
                chosenNamesList.set(i, name.trim());
            }
            Boolean isTableAddedSuccessfully = databaseUtil.addTable(tableNameField.getText(), chosenNamesList, chosenTypesList, chosenRelationsList, allowNullsList);
            if(isTableAddedSuccessfully)
            {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.initOwner(dialogStage);
                alert.setTitle("Success!");
                alert.setHeaderText(null);
                
                alert.setContentText("New Database Table added successfully!");
                alert.showAndWait();
                return true;
            }
            return false;
        } 
        else 
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Error!");
            alert.setHeaderText("Error while adding a new Database Table!");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
    
    //Cancel Button behavior's method.
    @FXML
    private void handleCancel() 
    {
        dialogStage.close();
    }
}
