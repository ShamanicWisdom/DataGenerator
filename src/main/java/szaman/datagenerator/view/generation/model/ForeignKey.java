/*
                --DataGenerator--
    ForeignKey is an entity object for chosen table's foreign key's relation.
*/

package szaman.datagenerator.view.generation.model;

//Other own classes:



//Other classes:

/**
 *
 * @author Szaman
 */

public class ForeignKey 
{
    private String foreignKeyTable;
    private String foreignKeyColumn;
    private String primaryKeyTable;
    private String primaryKeyColumn;
    
    public ForeignKey() 
    {
        
    }
    
    public String getForeignKeyTable() 
    {
        return this.foreignKeyTable;
    }
    
    public void setForeignKeyTable(String foreignKeyTable) 
    {
        this.foreignKeyTable = foreignKeyTable;
    }
    
    public String getForeignKeyColumn() 
    {
        return this.foreignKeyColumn;
    }
    
    public void setForeignKeyColumn(String foreignKeyColumn) 
    {
        this.foreignKeyColumn = foreignKeyColumn;
    }
    
    public String getPrimaryKeyTable() 
    {
        return this.primaryKeyTable;
    }
    
    public void setPrimaryKeyTable(String primaryKeyTable) 
    {
        this.primaryKeyTable = primaryKeyTable;
    }
    
    public String getPrimaryKeyColumn() 
    {
        return this.primaryKeyColumn;
    }
    
    public void setPrimaryKeyColumn(String primaryKeyColumn) 
    {
        this.primaryKeyColumn = primaryKeyColumn;
    }
    
    public void printForeignKeyInfo()
    {
        System.out.println("FKTab: " + foreignKeyTable + " FKCol: " + foreignKeyColumn + " PKTab: " + primaryKeyTable + " PKCol: " + primaryKeyColumn);
    }
}
