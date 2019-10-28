/*
        --DataGenerator--
    WelcomeController class.
*/

package szaman.datagenerator.view;

//Other own classes:

import szaman.datagenerator.Core;
import szaman.datagenerator.utility.Utility;
import szaman.datagenerator.utility.DataPoolUtility;

//Other classes:

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Szaman
 */

public class WelcomeController
{    
    @FXML
    private Label buildInformer;
    @FXML 
    private Stage stage;
    
    private Core core;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();

    public void setCore(Core core, Stage primaryStage)
    {
        this.core = core;      
        this.stage = primaryStage;
        showBuildInfo();     
    }
    
    Utility utility = new Utility();
    
    public void showBuildInfo()
    {
        buildInformer.setText(utility.pullBuildNumber());
    }
    
    //WelcomeFXMLController() constructor.
    public WelcomeController() 
    {
    }
    
    //AvailableDataPool button behavior's method.
    @FXML
    private void handleDataPool(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("");
        alert.setContentText("Connecting to embedded database...");
        alert.show();
        core.showDataPool();
        alert.close();
    }
        
    //GenerateData button behavior's method.
    @FXML
    private void handleGenerate(ActionEvent event)
    {     
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("");
        alert.setContentText("Connecting to embedded database...");
        alert.show();   
        core.showGeneratorPanel();  
        alert.close();
    }
    
    //Image button behavior's method.
    @FXML
    private void handleImages(ActionEvent event)
    {
        List<File> chosenFilesList = new ArrayList<>();
        File file = new File("");
        String programPath = file.getAbsolutePath() + "\\ImageContainer"; 
        String readmePath = programPath + "\\readme.txt";        
        try
        {
            Desktop.getDesktop().open(new File(programPath));    
            //Check, if readme exists.
            File tempReadmeFile = new File(readmePath);
            //If not, create a new one.
            if(tempReadmeFile.exists() == false)
            {
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(readmePath))) 
                {
                    bufferedWriter.write("To add image pool into generation pool, simply create a new folder here and add images inside.");
                }    
            }   
        }
        //Folder deleted - need to create new one.
        catch(IOException | IllegalArgumentException exception)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error!");
            alert.setHeaderText("Directory missing!");
            alert.setContentText("Missing folder 'ImageContainer' needed for handling the images! \nCreating new folder...");
            alert.showAndWait();  
            try 
            {
                Path path = Paths.get(programPath);
                Files.createDirectories(path);
                Desktop.getDesktop().open(new File(programPath));
                //Creating readme.txt.
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(readmePath))) 
                {
                    bufferedWriter.write("To add image pool into generation pool, simply create a new folder here and add images inside.");
                } 
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(WelcomeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
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
    
    //About button behavior's method.
    @FXML
    private void handleAbout(ActionEvent event)
    {        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        //alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        alert.setHeaderText("About the program:");
        alert.setContentText
        (
            "DataGenerator" + "\n" + 
            utility.pullBuildNumberWithDate() + "\n" + "\n" +
            "Author:" + "\n" +
            utility.pullAuthorNickname()
        );
        alert.showAndWait();             
    }
    
    //Exit button behavior's method.
    @FXML
    public void handleExit()
    {
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(AlertType.CONFIRMATION, "", okButton, cancelButton);
        alert.setTitle("Exit");
        alert.setHeaderText(null);
        //alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == okButton)
        {
            System.exit(0);
        } 
        else 
        {
            alert.close();
        }
    }
}