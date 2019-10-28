/*
               --DataGenerator--
    Apache Derby's Pool Table Row Model class.
*/

package szaman.datagenerator.view.datapool.model;

//Other own classes:



//Other classes:

import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Szaman
 */

public class PoolData implements java.io.Serializable 
{
 
    private int poolDataID; 
    private String poolData;
    private Date lastModified;

    private IntegerProperty poolTableIDProperty = null;
    private StringProperty poolDataNameProperty = null;
    private StringProperty lastModifiedProperty = null;
    
    public PoolData() 
    {
        
    }
	
    /*Single string-column pool table*/
    public PoolData(int poolDataID, String poolData, Date lastModified) 
    {
        this.poolDataID = poolDataID;
        this.poolData = poolData;
        this.lastModified = lastModified;
    }
   
    public int getPoolDataID() 
    {
        return this.poolDataID;
    }
    
    public void setPoolDataID(int poolDataID) 
    {
        this.poolDataID = poolDataID;
    }
    
    public String getPoolData()
    {
        return this.poolData;
    }
    
    public void setPoolData(String poolData) 
    {
        if(poolData.equals(null))
        {
            this.poolData = "image";
        }
        this.poolData = poolData;
    }
    
    public Date getLastModified()
    {
        return this.lastModified;
    }
    
    public void setLastModified(Date lastModified) 
    {
        this.lastModified = lastModified;
    }
    
    /*Properties for Table*/
    
    public IntegerProperty poolDataIDProperty() 
    {
        poolTableIDProperty  = new SimpleIntegerProperty(poolDataID);
        return poolTableIDProperty;
    }
    
    public StringProperty poolDataProperty() 
    {
        poolDataNameProperty  = new SimpleStringProperty(poolData);
        return poolDataNameProperty;
    }
    
    public StringProperty lastModifiedProperty() 
    {
        lastModifiedProperty  = new SimpleStringProperty(lastModified.toString());
        return lastModifiedProperty;
    }
}
