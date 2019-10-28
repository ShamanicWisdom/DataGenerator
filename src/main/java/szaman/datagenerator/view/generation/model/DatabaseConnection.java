/*
                --DataGenerator--
    DatabaseConnection is an entity object for chosen database.
*/

package szaman.datagenerator.view.generation.model;

//Other own classes:



//Other classes:

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Szaman
 */

public class DatabaseConnection 
{    
    private String serverName;
    private String databaseName;
    private String port;
    private String username;
    private String password;
    
    private boolean connectionCorrectness = false;
    
    private List<String> connectionCredentialsList = new ArrayList<>();
    
    public DatabaseConnection() 
    {
        
    }
    
    public boolean getConnectionCorrectness()
    {
        return connectionCorrectness;
    }
    
    public void setConnectionCorrectness(boolean connectionCorrectness)
    {
        this.connectionCorrectness = connectionCorrectness;
    }
    
    public String getServerName() 
    {
        return this.serverName;
    }
    
    public void setServerName(String serverName) 
    {
        this.serverName = serverName;
    }
    
    public String getDatabaseName() 
    {
        return this.databaseName;
    }
    
    public void setDatabaseName(String databaseName) 
    {
        this.databaseName = databaseName;
    }
    
    public String getPort() 
    {
        return this.port;
    }
    
    public void setPort(String port) 
    {
        this.port = port;
    }
    
    public String getUsername() 
    {
        return this.username;
    }
    
    public void setUsername(String username) 
    {
        this.username = username;
    }
    
    public String getPassword() 
    {
        return this.password;
    }
    
    public void setPassword(String password) 
    {
        this.password = password;
    }
    
    public List<String> getConnectionCredentials()
    {
        return connectionCredentialsList;
    }
    
    public void setConnectionCredentials()
    {
        connectionCredentialsList.add(serverName);
        connectionCredentialsList.add(port);
        connectionCredentialsList.add(databaseName);
        connectionCredentialsList.add(username);
        connectionCredentialsList.add(password);
    }
    
    public void purgeConnectionCredentials()
    {
        serverName = "";
        port = "";
        databaseName = "";
        username = "";
        password = "";
        connectionCorrectness = false;
    }
    
    public String getConnectionURL()
    {
        return "jdbc:sqlserver://" + getServerName() + ":" + getPort() + ";databaseName=" + getDatabaseName() + ";user=" + getUsername() + ";password=" + getPassword() + ";";
    }
}
