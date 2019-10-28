/*
                --DataGenerator--
    SchedulerTask is an entity object for single task for Scheduler Module.
*/

package szaman.datagenerator.view.generation.model;

//Other own classes:



//Other classes:

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Szaman
 */

public class SchedulerTask 
{
    private String chosenTable;
    private int numberOfRecords;
    private int incrementator;
    private List<String> chosenContentList;
    private List<String> columnDataTypesList;
    private List<String> imageDirectoryList;
    private List<String> fromTextFieldList;
    private List<String> toTextFieldList;
    private List<ForeignKey> chosenTableForeignKeys;
    private DatabaseConnection databaseConnection;
    private List<String> nullChanceList;
    private String primaryKeyColumnName;
    
    private StringProperty tableNameProperty;
    private StringProperty recordsCountProperty;
    
    public SchedulerTask() 
    {
        
    }
    
    public SchedulerTask(String chosenTable, int numberOfRecords, int incrementator, String primaryKeyColumnName, List<String> chosenContentList, List<String> columnDataTypesList, List<String> imageDirectoryList,  List<String> fromTextFieldList, List<String> toTextFieldList, List<ForeignKey> chosenTableForeignKeys, DatabaseConnection databaseConnection, List<String> nullChanceList)
    {
        this.chosenTable = chosenTable;
        this.numberOfRecords = numberOfRecords;
        this.incrementator = incrementator;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.chosenContentList = chosenContentList;
        this.columnDataTypesList = columnDataTypesList;
        this.imageDirectoryList = imageDirectoryList;
        this.fromTextFieldList = fromTextFieldList;
        this.toTextFieldList = toTextFieldList;
        this.chosenTableForeignKeys = chosenTableForeignKeys;
        this.databaseConnection = databaseConnection;
        this.nullChanceList = nullChanceList;
    }
    
    public String getChosenTable() 
    {
        return this.chosenTable;
    }
    
    public void setChosenTable(String chosenTable) 
    {
        this.chosenTable = chosenTable;
    }
    
    public int getNumberOfRecords() 
    {
        return this.numberOfRecords;
    }
    
    public void setNumberOfRecords(int numberOfRecords) 
    {
        this.numberOfRecords = numberOfRecords;
    }
    
    public int getIncrementator() 
    {
        return this.incrementator;
    }
    
    public void setIncrementator(int incrementator) 
    {
        this.incrementator = incrementator;
    }
    
    public String getPrimaryKeyColumnName() 
    {
        return this.primaryKeyColumnName;
    }
    
    public void setPrimaryKeyColumnName(String primaryKeyColumnName) 
    {
        this.primaryKeyColumnName = primaryKeyColumnName;
    }
    
    public List<String> getChosenContentList() 
    {
        return this.chosenContentList;
    }
    
    public void setChosenContentList(List<String> chosenContentList) 
    {
        this.chosenContentList = chosenContentList;
    }
    
    public List<String> getColumnDataTypesList() 
    {
        return this.columnDataTypesList;
    }
    
    public void setColumnDataTypesList(List<String> columnDataTypesList) 
    {
        this.columnDataTypesList = columnDataTypesList;
    }
    
    public List<String> getImageDirectoryList() 
    {
        return this.imageDirectoryList;
    }
    
    public void setImageDirectoryList(List<String> imageDirectoryList) 
    {
        this.imageDirectoryList = imageDirectoryList;
    }
    
    public List<String> getFromTextFieldList() 
    {
        return this.fromTextFieldList;
    }
    
    public void setFromTextFieldList(List<String> fromTextFieldList) 
    {
        this.fromTextFieldList = fromTextFieldList;
    }
    
    public List<String> getToTextFieldList() 
    {
        return this.toTextFieldList;
    }
    
    public void setToTextFieldList(List<String> toTextFieldList) 
    {
        this.toTextFieldList = toTextFieldList;
    }
    
    public List<ForeignKey> getChosenTableForeignKeys() 
    {
        return this.chosenTableForeignKeys;
    }
    
    public void setChosenTableForeignKeys(List<ForeignKey> chosenTableForeignKeys) 
    {
        this.chosenTableForeignKeys = chosenTableForeignKeys;
    }
           
    public DatabaseConnection getDatabaseConnection() 
    {
        return this.databaseConnection;
    }
    
    public void setDatabaseConnection(DatabaseConnection databaseConnection) 
    {
        this.databaseConnection = databaseConnection;
    }
    
    public List<String> getNullChanceList() 
    {
        return this.nullChanceList;
    }
    
    public void setNullChanceList(List<String> nullChanceList) 
    {
        this.nullChanceList = nullChanceList;
    }
    
    /*Property for Table*/
    
    public StringProperty tableNameProperty() 
    {
        tableNameProperty  = new SimpleStringProperty(chosenTable);
        return tableNameProperty;
    }
    
    public StringProperty recordsCountProperty() 
    {
        recordsCountProperty  = new SimpleStringProperty(Integer.toString(numberOfRecords));
        return recordsCountProperty;
    }
}
