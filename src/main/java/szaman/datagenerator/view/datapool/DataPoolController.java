/*
        --DataGenerator--
    DataPoolController class.
*/

package szaman.datagenerator.view.datapool;

//Other own classes:

import szaman.datagenerator.Core;
import szaman.datagenerator.utility.DataPoolUtility;
import szaman.datagenerator.view.datapool.model.PoolTable;

//Other classes:

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * 
 *
 * @author Szaman
 */

public class DataPoolController
{    
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    
    @FXML
    private TableView<PoolTable> dataPoolTable;
    
    @FXML
    private TableColumn<PoolTable, String> poolTableNameColumn;
    @FXML
    private TableColumn<PoolTable, String> poolDataCountColumn;
    
    private final ObservableList<PoolTable> dataGatherer = FXCollections.observableArrayList();
    
    
    private Core core;
    
    private String poolTableName = null; 
    private String poolDataCount = null;   
    
    private PoolTable chosenTable;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    
    //Table initiallization.
    @FXML
    private void initialize() 
    {
        poolTableNameColumn.setCellValueFactory(cellData -> cellData.getValue().poolTableNameProperty());
        poolDataCountColumn.setCellValueFactory(cellData -> cellData.getValue().poolDataCountProperty());
        dataPoolTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> getPoolTableDetails(newValue));
    }
    
    public void setCore(Core core)
    {
        this.core = core;        
        dataPoolUtil.createConnection();        
        dataHarvester();
        dataPoolTable.setItems(dataGatherer);
    }
        
    /*************************************************************************************************************************************/
    
    //Harvesting data.
    public void dataHarvester()
    {
        List<PoolTable> tableList = null;
        tableList = dataPoolUtil.getPoolTableData();
        dataGatherer.addAll(tableList);
    }    
    
    /*************************************************************************************************************************************/  
        
    //Pool table details.
    private void getPoolTableDetails(PoolTable poolTable) 
    {
        if (poolTable != null) 
        {
            poolTableName = poolTable.getPoolTableName();
            chosenTable = poolTable;
            System.out.println(chosenTable.getPoolTableName() + " selected");
        } 
        else 
        {
            chosenTable = null;
            poolTableName = null;
        }
    }
    
    //Manage Button behavior's method.
    @FXML
    private void handleManage(ActionEvent event)
    {
        int selectedIndex = dataPoolTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) 
        {
            System.out.println("ManageData detected!");
            Boolean isTableHasSingleData = false;
            isTableHasSingleData = dataPoolUtil.checkIfTableDataIsSingle(poolTableName);
            if(isTableHasSingleData == true)
            {
                core.showSingleDataTable(chosenTable);
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Multi Data Table!");
                alert.setContentText("Action not yet available.");
                alert.showAndWait();
            }
        }
        else 
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No pool table selected!");
            alert.setContentText("Please choose one of listed pool tables!");
            alert.showAndWait();
        }
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleAdd(ActionEvent event)
    {
        System.out.println("AddPoolTable detected!"); //terminal's control.
        PoolTable newPoolTable = new PoolTable();
        boolean okClicked = core.showAddPoolTable(newPoolTable);
        if (okClicked) 
        {
            System.out.println("AddPoolTableOK detected!"); //terminal's control.
            dataPoolTable.getItems().clear(); //Clear old data.
            dataHarvester(); //Harvest fresh data.
            dataPoolTable.setItems(dataGatherer); //Load fresh data.
            okClicked = false;
        }
        System.out.println("AddPoolTableCancel detected!"); //terminal's control.
    }
    
    //Edit Button behavior's method.
    @FXML
    private void handleEdit(ActionEvent event)
    {
        boolean okClicked = false;
        int selectedIndex = dataPoolTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) 
        {
            System.out.println("PoolTableEdit detected!");
            okClicked = core.showEditPoolTable(poolTableName);    
            dataPoolTable.getItems().clear(); //Clear old data.
            dataHarvester(); //Harvest fresh data.
            dataPoolTable.setItems(dataGatherer); //Load fresh data.
            okClicked = false;
        }
        else 
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No pool table selected!");
            alert.setContentText("Please choose one of listed pool tables!");
            alert.showAndWait();
        }
    } 
    
    //Delete button behavior's method.
    @FXML
    public void handleDelete()
    {
        int selectedIndex = dataPoolTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) 
        {
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(AlertType.CONFIRMATION, "", okButton, cancelButton);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure to delete selected pool table? (" + poolTableName + ")\n All data from this table will be lost!");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == okButton)
            {
                dataPoolUtil.dropSelectedPoolTable(poolTableName);
                System.out.println("DeletePoolTableOK detected!"); //terminal's control.
                dataPoolTable.getItems().clear(); //Clear old data.
                dataHarvester(); //Harvest fresh data.
                dataPoolTable.setItems(dataGatherer); //Load fresh data.
            } 
            else 
            {
                alert.close();
            } 
        }
        else 
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No pool table selected!");
            alert.setContentText("Please choose one of listed pool tables!");
            alert.showAndWait();
        }
    }
    
    //Back Button behavior's method.
    @FXML
    private void handleBack(ActionEvent event) throws IOException 
    {
        System.out.println("MenuBack detected!"); //terminal's control.
        core.showWelcome();
    }
}
