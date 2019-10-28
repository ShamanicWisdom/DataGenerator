/*
        --DataGenerator--
    DataPoolController class.
*/

package szaman.datagenerator.view.datapool.data;

//Other own classes:

import szaman.datagenerator.Core;
import szaman.datagenerator.utility.DataPoolUtility;
import szaman.datagenerator.view.datapool.model.PoolData;
import szaman.datagenerator.view.datapool.model.PoolTable;

//Other classes:

import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * 
 *
 * @author Szaman
 */

public class SingleDataController
{    
    @FXML
    private Label dataTableNameLabel;
    
    @FXML
    private Label dataIDLabel;
    @FXML
    private Label dataLabel;
    @FXML
    private Label lastModifiedLabel;
    
    
    @FXML
    private TableView<PoolData> singleDataTable;
    
    @FXML
    private TableColumn<PoolData, Integer> idColumn;
    @FXML
    private TableColumn<PoolData, String> dataColumn;
    
    private final ObservableList<PoolData> dataGatherer = FXCollections.observableArrayList();
    
    
    private Core core;
    
    private String poolTableName = null; 
    private String poolData = null;   
    private int poolID = 0;
    
    private PoolTable poolTable;
    
    DataPoolUtility dataPoolUtil = new DataPoolUtility();
    
    
    //Table initiallization.
    @FXML
    private void initialize() 
    {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().poolDataIDProperty().asObject());
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        dataColumn.setCellValueFactory(cellData -> cellData.getValue().poolDataProperty());
        singleDataTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> getPoolDataDetails(newValue));
    }
    
    public void setCore(Core core, PoolTable poolTable)
    {
        this.core = core;
        this.poolTable = poolTable;
        String dataTypeString;
        if(this.poolTable.getPoolTableType().equals("BLOB"))
        {
            dataTypeString = "Image";
        }
        else
        {
            dataTypeString = "String";
        }
        dataTableNameLabel.setText(this.poolTable.getPoolTableName() + " Content");// (" + dataTypeString + "-Type Table)");
        dataPoolUtil.createConnection();
        dataHarvester();
        singleDataTable.setItems(dataGatherer);
    }
        
    /*************************************************************************************************************************************/    
    
    //Harvesting data.
    public void dataHarvester()
    {
        List<PoolData> dataList = null;
        dataList = dataPoolUtil.getSingleDataFromTable(poolTable.getPoolTableName());
        dataGatherer.addAll(dataList);
    }    
    
    /*************************************************************************************************************************************/      
    
    //Pool table details.
    private void getPoolDataDetails(PoolData poolDataRow) 
    {
        if (poolDataRow != null) 
        {
            poolData = poolDataRow.getPoolData();
            poolID = poolDataRow.getPoolDataID();
            System.out.println(poolID + ": " + poolData + " selected");
            dataIDLabel.setText(Integer.toString(poolDataRow.getPoolDataID()));
            dataLabel.setText(poolDataRow.getPoolData());
            lastModifiedLabel.setText(poolDataRow.getLastModified().toString());
        } 
        else 
        {
            poolTableName = null;
            poolID = 0;
        }
    }
    
    //Add Button behavior's method.
    @FXML
    private void handleAdd(ActionEvent event)
    {        
        boolean okClicked = core.showAddSingleData(poolTable);
        if(okClicked)
        {
            System.out.println("AddDataOK detected!"); //terminal's control.
            singleDataTable.getItems().clear(); //Clear old data.
            dataHarvester(); //Harvest fresh data.
            singleDataTable.setItems(dataGatherer); //Load fresh data.
            okClicked = false;
        }
        System.out.println("AddDataCancel detected!"); //terminal's control.        
    }
       
    /*
    //Edit Button behavior's method.
    @FXML
    private void handleEdit(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Editing the data!");
        alert.setContentText("Action not yet available.");
        alert.showAndWait();
    }
    
    
    //Delete Button behavior's method.
    @FXML
    private void handleDelete(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Deleting the data!");
        alert.setContentText("Action not yet available.");
        alert.showAndWait();
    }
    */
    /*
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
    */
    //Back Button behavior's method.
    @FXML
    private void handleBack(ActionEvent event) throws IOException 
    {
        System.out.println("DataPoolBack detected!"); //terminal's control.
        core.showDataPool();
    }
}
