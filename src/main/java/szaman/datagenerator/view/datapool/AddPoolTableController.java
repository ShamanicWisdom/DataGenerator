/*
        --DataGenerator--
    AddPoolTableController class.
*/

package szaman.datagenerator.view.datapool;

//Other own classes:

import szaman.datagenerator.utility.DataPoolUtility;

//Other classes:

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 *
 * @author Szaman
 */

public class AddPoolTableController 
{
    @FXML
    private TextField tableNameLabel;
    
    private Stage dialogStage;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    private boolean okClicked = false;

    @FXML
    private void initialize() 
    {
    }

    //Setting the Dialog Stage.
    public void setDialogStage(Stage dialogStage) 
    {
        this.dialogStage = dialogStage;
        tableNameLabel.setPromptText("Insert new Pool Table's name here.");         
    }

    //OK clicking button behavior's method logic.
    public boolean isOkClicked() 
    {
        return okClicked;
    }

    //Add Button behavior's method.
    @FXML
    private void handleAdd()
    {
        if (validator()) 
        {
            okClicked = true;
            dialogStage.close();
        }
    }

    //Input data validation.
    private boolean validator()
    {
        String errorMessage = ""; //Empty string for all catched errors.
        
        //Catching errors: 
        //1. No input detected.
        if (tableNameLabel.getText() == null || tableNameLabel.getText().length() == 0) 
        {
            errorMessage += "No Pool Table's name!" + "\n";
        }
        else
        {
            //2. Name already exists.
            if(dataPoolUtil.isPoolTableExists(tableNameLabel.getText()))
            {
                errorMessage += "Name is already existing!" + "\n";
            }
        }

        //If zero errors:
        if (errorMessage.length() == 0) 
        {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initOwner(dialogStage);
            alert.setTitle("Success!");
            alert.setHeaderText(null);
            dataPoolUtil.addPoolTable(tableNameLabel.getText());
            alert.setContentText("New Pool Table added successfully!");
            alert.showAndWait();
            return true;
        } 
        else 
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Error!");
            alert.setHeaderText("Error while adding a new Pool Table!");
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
