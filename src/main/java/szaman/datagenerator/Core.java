/*
              --DataGenerator--
    Core class is a main class of this project.
*/

package szaman.datagenerator;

//Other own classes:

import szaman.datagenerator.utility.Utility;
import szaman.datagenerator.utility.DataGenerator;
import szaman.datagenerator.view.MainWindowController;
import szaman.datagenerator.view.WelcomeController;
import szaman.datagenerator.view.generation.GeneratorPanelController;
import szaman.datagenerator.view.datapool.DataPoolController;
import szaman.datagenerator.view.datapool.model.PoolTable;
import szaman.datagenerator.view.datapool.AddPoolTableController;
import szaman.datagenerator.view.datapool.EditPoolTableController;
import szaman.datagenerator.view.datapool.data.AddSingleDataController;
import szaman.datagenerator.view.datapool.data.SingleDataController;
import szaman.datagenerator.utility.OnlineDataGenerator;
import szaman.datagenerator.utility.SchedulerGenerator;
import szaman.datagenerator.view.generation.model.DatabaseConnection;
import szaman.datagenerator.view.generation.model.ForeignKey;
import szaman.datagenerator.view.generation.AddTableController;
import szaman.datagenerator.view.generation.DatabaseConnectionController;
import szaman.datagenerator.view.generation.OnlineGeneratorPanelController;
import szaman.datagenerator.view.generation.model.SchedulerTask;

//Other classes:

import static javafx.application.Application.launch;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.stage.Modality;

public class Core extends Application 
{
    public static ServerSocket serverSocket;
    
    private Stage primaryStage;
    
    public BorderPane root;
    
     //Application start method.
    @Override
    public void start(Stage primaryStage) throws Exception 
    {        
        serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(1044); //for only one instance of the program at a time!
            this.primaryStage = primaryStage;

            initializeMainWindow(); 
        }
        catch(IOException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Program is already running!");
            alert.setContentText
            (
                "Another instance of this program is already running!\n"
                + "Please close the instance and try again!\n\n"
                + "Error: " + exception.toString()
            );
            alert.showAndWait(); 
            System.exit(0);
        }
        
    }
    
    //Main window initialization.
    public void initializeMainWindow()
    {
        try //Attempting to load a file and point a proper controller Class..
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/MainWindow.fxml")); 
            root = (BorderPane) loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            Utility utility = new Utility();
            MainWindowController controller;
            controller = loader.getController();
            controller.setCore(this);
            primaryStage.show();                   
            primaryStage.setResizable(false);
            primaryStage.setTitle(utility.pullProgramName() + ", " + utility.pullBuildNumberWithDate()); 
            showWelcome();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    //Start scene.
    public void showWelcome()
    {
        try //Attempting to load a file, point a proper controller class and show it inside the window.
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/Welcome.fxml"));
            AnchorPane welcome = (AnchorPane) loader.load();
            root.setCenter(welcome);
            WelcomeController controller = loader.getController();
            controller.setCore(this, primaryStage);
        } 
        catch (IOException e) 
        {
           e.printStackTrace();
        }
    }
    
    /********** Data Pool Section **********/
    
    //DataPool's scene.
    public void showDataPool()
    {
        try //Attempting to load a file, point a proper controller class and show it inside the window.
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/datapool/DataPool.fxml"));
            AnchorPane welcome = (AnchorPane) loader.load();
            root.setCenter(welcome);
            DataPoolController controller = loader.getController();
            controller.setCore(this);
        } 
        catch (IOException e) 
        {
           e.printStackTrace();
        }
    }
    
    //Add Pool Table's Dialog Window.
    public boolean showAddPoolTable(PoolTable poolTable) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/datapool/AddPoolTable.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Adding new Data Pool");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            AddPoolTableController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    //Edit Pool Table's Dialog Window.
    public boolean showEditPoolTable(String poolTableName) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/datapool/EditPoolTable.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editing Data Pool");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            EditPoolTableController controller = loader.getController();
            controller.setDialogStage(dialogStage, poolTableName);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    /********** Single-data Table Content Section **********/
    
    //Single-data Table.
    public void showSingleDataTable(PoolTable chosenTable)
    {
        try //Attempting to load a file, point a proper controller class and show it inside the window.
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/datapool/data/SingleData.fxml"));
            AnchorPane welcome = (AnchorPane) loader.load();
            root.setCenter(welcome);
            SingleDataController controller = loader.getController();
            controller.setCore(this, chosenTable);
        } 
        catch (IOException e) 
        {
           e.printStackTrace();
        }
    }
    
    //Add Single-Data Pool's Dialog Window.
    public boolean showAddSingleData(PoolTable poolTable) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/datapool/data/AddSingleData.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Adding new Single-Data Pool");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            AddSingleDataController controller = loader.getController();
            controller.setDialogStage(dialogStage, poolTable);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    /********** Data Generation Section **********/
    
    //Generator panel's scene.
    public void showGeneratorPanel()
    {
        try //Attempting to load a file, point a proper controller class and show it inside the window.
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/GeneratorPanel.fxml"));
            AnchorPane welcome = (AnchorPane) loader.load();
            root.setCenter(welcome);
            GeneratorPanelController controller = loader.getController();
            controller.setCore(this);
        } 
        catch (IOException e) 
        {
           e.printStackTrace();
        }
    }
    
    //Generation progress' miniscene.
    public boolean generationProgress(int numberOfRecords, String chosenToggle, Boolean includeHeaders, List<String> chosenContentList, List<String> fromTextFieldList, List<String> toTextFieldList, List<String> nullChanceList) throws InterruptedException
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/GenerationProgress.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Generating...");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            DataGenerator controller = loader.getController();
            controller.setDialogStage(dialogStage, numberOfRecords, chosenToggle, includeHeaders, chosenContentList, fromTextFieldList, toTextFieldList, nullChanceList);
            dialogStage.show();
            controller.startGeneration(numberOfRecords);
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            return false;
        }
    }
    
    //Database Login Screen.
    public DatabaseConnection databaseConnection() 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/DatabaseConnection.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Database Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            DatabaseConnectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            return controller.returnConnection();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return null;
        }
    }
    
    //Generator panel's scene.
    public void showOnlineGeneratorPanel(DatabaseConnection databaseConnection)
    {
        try //Attempting to load a file, point a proper controller class and show it inside the window.
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/OnlineGeneratorPanel.fxml"));
            AnchorPane welcome = (AnchorPane) loader.load();
            root.setCenter(welcome);
            OnlineGeneratorPanelController controller = loader.getController();
            controller.setCore(this, databaseConnection);
        } 
        catch (IOException e) 
        {
           e.printStackTrace();
        }
    }
    //Online generation progress' miniscene.
    public boolean onlineGenerationProgress(String chosenTableName, int numberOfRecords, int incrementator, List<String> chosenContentList, List<String> columnDataTypesList, List<String> imageDirectoryList, List<String> fromTextFieldList, List<String> toTextFieldList, List<ForeignKey> chosenTableForeignKeys, DatabaseConnection databaseConnection, Boolean recoveryModeSelected, String recoveryToggle, List<String> nullChanceList) throws InterruptedException
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/OnlineGenerationProgress.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Generating...");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            OnlineDataGenerator controller = loader.getController();
            controller.setDialogStage(dialogStage, chosenTableName, numberOfRecords, incrementator, chosenContentList, columnDataTypesList, imageDirectoryList, fromTextFieldList, toTextFieldList, chosenTableForeignKeys, databaseConnection, recoveryModeSelected, recoveryToggle, nullChanceList);
            dialogStage.show();
            controller.startGeneration(numberOfRecords);
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            return false;
        }
    }
    
    //Scheduler generation progress' miniscene.
    public boolean schedulerGenerationProgress(List<SchedulerTask> schedulerTaskList, DatabaseConnection databaseConnection, Boolean recoveryModeSelected, String recoveryToggle) throws InterruptedException
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/SchedulerGenerationProgress.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Generating...");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            SchedulerGenerator controller = loader.getController();
            controller.setDialogStage(dialogStage, schedulerTaskList, databaseConnection, recoveryModeSelected, recoveryToggle);
            dialogStage.show();
            controller.startGeneration();
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            return false;
        }
    }
    
    //Add Pool Table's Dialog Window.
    public boolean showAddTable(DatabaseConnection databaseConnection) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Core.class.getResource("/fxml/view/generation/AddTable.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Adding new Database Table");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            AddTableController controller = loader.getController();
            controller.setDialogStage(dialogStage, databaseConnection);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
        
    /********** Main **********/

    public static void main(String[] args) 
    {
        launch(args);        
    }
}
