/*
               --DataGenerator--
    Apache Derby's Pool Table Model class.
*/

package szaman.datagenerator.view.datapool.model;

//Other own classes:



//Other classes:

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Szaman
 */

public class PoolTable implements java.io.Serializable 
{
 
    private String poolTableName;
    private String poolTableType;
    private int poolDataCount;

    private StringProperty poolTableNameProperty = null;
    private StringProperty poolDataCountProperty = null;
    private StringProperty poolTableTypeProperty = null;
    
    public PoolTable() 
    {
        
    }
	
    public PoolTable(String poolTableName, String poolTableType, int poolDataCount) 
    {
        this.poolTableName = poolTableName;
        this.poolTableType = poolTableType;
        this.poolDataCount = poolDataCount;
    }
   
    public String getPoolTableName() 
    {
        return this.poolTableName;
    }
    
    public void setPoolTableName(String poolTableName) 
    {
        this.poolTableName = poolTableName;
    }
    
    public String getPoolTableType() 
    {
        return this.poolTableType;
    }
    
    public void setPoolTableType(String poolTableType) 
    {
        this.poolTableType = poolTableType;
    }
    
    public int getPoolDataCount()
    {
        return this.poolDataCount;
    }
    
    public void setPoolDataCount(int poolDataCount) 
    {
        this.poolDataCount = poolDataCount;
    }
    
    /*Properties for Table*/
    
    public StringProperty poolTableNameProperty() 
    {
        poolTableNameProperty  = new SimpleStringProperty(poolTableName);
        return poolTableNameProperty;
    }
    
    public StringProperty poolTableTypeProperty() 
    {
        poolTableTypeProperty  = new SimpleStringProperty(poolTableType);
        return poolTableTypeProperty;
    }
    
    public StringProperty poolDataCountProperty() 
    {
        poolDataCountProperty  = new SimpleStringProperty(Integer.toString(poolDataCount));
        return poolDataCountProperty;
    }
}
