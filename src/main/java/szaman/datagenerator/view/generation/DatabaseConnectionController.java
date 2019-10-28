/*
        --DataGenerator--
    DatabaseConnectionController class.
*/

package szaman.datagenerator.view.generation;

//Other own classes:

import szaman.datagenerator.utility.DatabaseUtility;
import szaman.datagenerator.view.generation.model.DatabaseConnection;

//Other classes:

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author Szaman
 */
public class DatabaseConnectionController 
{
    @FXML
    private TextField serverNameField;
    @FXML
    private TextField databaseNameField;
    @FXML
    private TextField portField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberBox;
    
    @FXML
    private Label testConnectionLabel;
    
    DatabaseConnection databaseConnection = new DatabaseConnection();
    
    DatabaseUtility databaseUtil = new DatabaseUtility();
    
    List<String> connectionCredentialsList = new ArrayList<>();
    
    private Stage dialogStage;
    
    private boolean okClicked = false;
    
    //Setting up the Dialog Stage.
    public void setDialogStage(Stage dialogStage) 
    {
        this.dialogStage = dialogStage;      
        try 
        {
            String fileName = "savedConnectionCredentials.txt";
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) 
            {
                connectionCredentialsList.add(line);
            }   
            bufferedReader.close();    
            
            serverNameField.setText(connectionCredentialsList.get(0));
            portField.setText(connectionCredentialsList.get(1));
            databaseNameField.setText(connectionCredentialsList.get(2));
            usernameField.setText(connectionCredentialsList.get(3));
            passwordField.setText(connectionCredentialsList.get(4));   
            
            rememberBox.setSelected(true);            
        }
        //No saved file detected.
        catch(IOException exception) 
        {               
        }
        
        serverNameField.setPromptText("Insert server name here");
        portField.setPromptText("Insert port number here");
        databaseNameField.setPromptText("Insert database name here");
        usernameField.setPromptText("Insert username here");
        passwordField.setPromptText("Insert password here");
    }
    
    //OkClicked logic.
    public DatabaseConnection returnConnection() 
    {
        return databaseConnection;
    }
    
    //TestConn button's behavior.
    @FXML
    private void handleTest(ActionEvent event)
    {
        if (validator()) 
        {
            testConnectionLabel.setText("Test Passed");
            testConnectionLabel.setTextFill(Color.web("#006400"));
        }
        else
        {
            testConnectionLabel.setText("Test Failed");
            testConnectionLabel.setTextFill(Color.web("#be0000"));
        }
    }
    
    //OK button's behavior.
    @FXML
    private void handleOk(ActionEvent event)
    {
        if (validator()) 
        {
            if(rememberBox.isSelected() == true)
            {
                saveConnectionCredentials();
            }
            else
            {
                deleteConnectionCredentialsFile();
            }
            dialogStage.close();
        }
    }
    
    private boolean validator()
    {
        String errorMessage = ""; //Empty string what will catch every error.
        
        if(serverNameField.getText() == null || serverNameField.getText().equals(""))
        {
            errorMessage += "Server name not given!" + "\n";
        }
        if(databaseNameField.getText() == null || databaseNameField.getText().equals(""))
        {
            errorMessage += "Database name not given!" + "\n";
        }
        if(portField.getText() == null || portField.getText().equals(""))
        {
            errorMessage += "Port not given!" + "\n";
        }
        if(usernameField.getText() == null || usernameField.getText().equals(""))
        {
            errorMessage += "Username not given!" + "\n";
        }
        if(passwordField.getText() == null || passwordField.getText().equals(""))
        {
            errorMessage += "Password not given!" + "\n";
        }
        if(errorMessage.length() == 0)
        {
            databaseConnection = new DatabaseConnection();
            databaseConnection.setServerName(serverNameField.getText());
            databaseConnection.setDatabaseName(databaseNameField.getText());
            databaseConnection.setPort(portField.getText());
            databaseConnection.setUsername(usernameField.getText());
            databaseConnection.setPassword(passwordField.getText());
            errorMessage += databaseUtil.testConnection(databaseConnection);
        }
        if(errorMessage.length() == 0)
        {
            databaseConnection.setConnectionCorrectness(true);
            return true;
        }
        else
        {
            databaseConnection.purgeConnectionCredentials();
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Error!");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
    
    private void saveConnectionCredentials()
    {
        try
        {
            databaseConnection.setConnectionCredentials();
            String fileName = "savedConnectionCredentials.txt";
            connectionCredentialsList = databaseConnection.getConnectionCredentials();
            Files.write(Paths.get(fileName), connectionCredentialsList);
        }
        catch(IOException exception)
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Error!");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
            alert.setContentText("Cannot save connection credentials!\n"
                                 + "Error: " + exception);
            alert.showAndWait();
        }
    }
    
    private void deleteConnectionCredentialsFile()
    {
        try
        {            
            String fileName = "savedConnectionCredentials.txt";
            Files.delete(Paths.get(fileName));
        }
        catch(IOException exception)
        {
        }
    }
    
    //Cancel button's behavior.
    @FXML
    private void handleCancel() 
    {
        dialogStage.close();
    }    
    
}
