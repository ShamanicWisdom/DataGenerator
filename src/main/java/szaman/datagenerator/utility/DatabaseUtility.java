/*
        --DataGenerator--
    DatabaseUtility class contains all handling and references for regular database (Microsoft SQL).
*/

package szaman.datagenerator.utility;

//Other own classes:

import java.io.BufferedWriter;
import szaman.datagenerator.view.generation.model.DatabaseConnection;
import szaman.datagenerator.view.generation.model.ForeignKey;

//Other classes:

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Szaman
 */
public class DatabaseUtility 
{
    Connection connection = null;
    Statement statement = null;
    
    //for logging minimalisation.
    String databaseName = "";
    
    
    //Create a connection with database.
    public String testConnection(DatabaseConnection databaseConnection)
    {
        try
        {      
            databaseName = databaseConnection.getDatabaseName();
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");   
            connection = DriverManager.getConnection(databaseConnection.getConnectionURL());   
            connection.close();
            return "";
        }
        catch(SQLException | ClassNotFoundException exception)
        {
            databaseName = "";
            return "There is a problem with connection!\n"
                 + "Error: " + exception.toString();
        }
    }  
    
    //Create a connection with database.
    public String createConnection(DatabaseConnection databaseConnection)
    {
        try
        {  
            databaseName = databaseConnection.getDatabaseName();
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
            connection = DriverManager.getConnection(databaseConnection.getConnectionURL());   
            return "";
        }
        catch(SQLException | ClassNotFoundException exception)
        {
            databaseName = "";
            return "There is a problem with connection!\n"
                 + "Error: " + exception.toString();
        }
    } 
    
    //Get all table names from database.
    public List<String> getTableNames()
    {
        List<String> tableNamesList = new ArrayList<>();
        try
        {
            statement = connection.createStatement();     
            
            DatabaseMetaData databaseMetaData = connection.getMetaData(); 
            
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) 
            {
                int i = 0;
                while(resultSet.next())
                {
                    String tableName = resultSet.getString(3);
                    if(tableName.equals("trace_xe_action_map") || tableName.equals("trace_xe_event_map") || tableName.equals("sysdiagrams"))
                    {
                    }
                    else
                    {
                        tableNamesList.add(tableName);
                    }
                     
                }
                if(tableNamesList.isEmpty())
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("So empty right here!");
                    alert.setContentText
                    (
                        "Table list is empty!\n"
                        + "Add something :)\n"
                    );
                    alert.showAndWait();
                }
            }
            statement.close();
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went while getting table data... " + exception.toString()
            );
            alert.showAndWait();
        }
        return tableNamesList;
    }    
    
    //Get database meta data.
    public DatabaseMetaData getMetaData()
    {
        try
        {
            statement = connection.createStatement();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData;
        }
        catch(SQLException e)
        {
            return null;
        }
    }
    
    //Get table meta data.
    public ResultSetMetaData getTableMetaData(String chosenTable)
    {
        try
        {            
            statement = connection.createStatement();
            String sql;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Matcher whitespaceMatcher = whitespacePattern.matcher(chosenTable);
            boolean isNameContainsWhitespace = whitespaceMatcher.find();
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(chosenTable);
            boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
            if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
            {
                sql = "select * from " + chosenTable;
            }
            else
            {
                sql = "select * from " + "\"" + chosenTable + "\"";
            }
            ResultSet result = statement.executeQuery(sql);
            ResultSetMetaData metaData = result.getMetaData();
            return metaData;
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went while getting table data... " + exception.toString()
            );
            alert.showAndWait();
            return null;
        }
    }
    
    public int getTableMaxPK(String chosenColumn, String chosenTable)
    {
        int incrementator = 0;
        try
        {            
            statement = connection.createStatement();
            String fixedColumnName = "";
            String fixedTableName = "";
            String sql;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Matcher whitespaceTableNameMatcher = whitespacePattern.matcher(chosenTable);
            Matcher whitespaceColumnNameMatcher = whitespacePattern.matcher(chosenColumn);
            boolean isTableNameContainsWhitespace = whitespaceTableNameMatcher.find();
            boolean isColumnNameContainsWhitespace = whitespaceColumnNameMatcher.find();
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            Matcher fullDigitTableNameMatcher = fullDigitNamePattern.matcher(chosenTable);
            Matcher fullDigitColumnNameMatcher = fullDigitNamePattern.matcher(chosenColumn);
            boolean isTableNameIsMadeOfFullDigits = fullDigitTableNameMatcher.find();
            boolean isColumnNameIsMadeOfFullDigits = fullDigitColumnNameMatcher.find();
            if(isTableNameContainsWhitespace == false && isTableNameIsMadeOfFullDigits == false)
            {
                fixedTableName = chosenTable;
            }
            else
            {
                fixedTableName = "\"" + chosenTable + "\"";
            }
            if(isColumnNameContainsWhitespace == false && isColumnNameIsMadeOfFullDigits == false)
            {
                fixedColumnName = chosenColumn;
            }
            else
            {
                fixedColumnName = "\"" + chosenColumn + "\"";
            }
            sql = "select max(" + fixedColumnName + ") from " + fixedTableName;
            System.out.println("SQL: " + sql);
            ResultSet result = statement.executeQuery(sql);
            if(result.next())
            {
                String resultString = result.getString(1);
                try
                {
                    if(resultString.isEmpty() == false)
                    {
                        incrementator = Integer.parseInt(resultString);
                    }
                }
                catch(NullPointerException exception)
                {
                    incrementator = 1;
                }
            }
            
            return incrementator;
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went while getting table data... " + exception.toString()
            );
            alert.showAndWait();
            incrementator = -1;
            return incrementator;
        }
    }
    
    //Get primary keys.
    public List<String> pullPrimaryKeysFromTable(ForeignKey foreignKey)
    {
        List<String> poolDataList = new ArrayList<>();
        try
        {
            String sql;
            String tableName;
            String columnName = "";
            Pattern whitespacePattern = Pattern.compile("\\s");
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            //Solving tableName.
            Matcher whitespaceTableNameMatcher = whitespacePattern.matcher(foreignKey.getPrimaryKeyTable());
            boolean isTableNameContainsWhitespace = whitespaceTableNameMatcher.find();
            Matcher fullDigitTableNameMatcher = fullDigitNamePattern.matcher(foreignKey.getPrimaryKeyTable());
            boolean isTableNameIsMadeOfFullDigits = fullDigitTableNameMatcher.find();
            if(isTableNameContainsWhitespace == false && isTableNameIsMadeOfFullDigits == false)
            {
                tableName = foreignKey.getPrimaryKeyTable();
            }
            else
            {
                tableName = "\"" + foreignKey.getPrimaryKeyTable() + "\"";
            }
            //Solving columnName.
            Matcher whitespaceColumnNameMatcher = whitespacePattern.matcher(foreignKey.getPrimaryKeyColumn());
            boolean isColumnNameContainsWhitespace = whitespaceColumnNameMatcher.find();
            Matcher fullDigitColumnNameMatcher = fullDigitNamePattern.matcher(foreignKey.getPrimaryKeyColumn());
            boolean isColumnNameIsMadeOfFullDigits = fullDigitColumnNameMatcher.find();
            if(isColumnNameContainsWhitespace == false && isColumnNameIsMadeOfFullDigits == false)
            {
                columnName = foreignKey.getPrimaryKeyColumn();
            }
            else
            {
                columnName = "\"" + foreignKey.getPrimaryKeyColumn() + "\"";
            }
            //Making a proper SQL string.
            sql = "SELECT " + columnName + " FROM " + tableName;
            statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(sql)) 
            {
                while(resultSet.next())
                {
                    poolDataList.add(Integer.toString(resultSet.getInt(1)));      
                }  
                              
            }
            statement.close();
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while getting foreign key's related column content... - " + exception.toString()
            );
            alert.showAndWait();
        }
        return poolDataList;
    }
    
    public void exportData(String tableName, String csvFileName, Boolean recoveryModeSelected, String recoveryToggle)
    {
        try
        {
            String sql = "";
            String chosenTableName;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            //Solving tableName.
            Matcher whitespaceTableNameMatcher = whitespacePattern.matcher(tableName);
            boolean isTableNameContainsWhitespace = whitespaceTableNameMatcher.find();
            Matcher fullDigitTableNameMatcher = fullDigitNamePattern.matcher(tableName);
            boolean isTableNameIsMadeOfFullDigits = fullDigitTableNameMatcher.find();
            if(isTableNameContainsWhitespace == false && isTableNameIsMadeOfFullDigits == false)
            {
                chosenTableName = tableName;
            }
            else
            {
                chosenTableName = "\"" + tableName + "\"";
            }            
            
            Utility utility = new Utility();
            String csvErrorFileName = System.getProperty("user.dir") + "\\" + utility.pullProgramName() + " " + utility.getCurrentDate() + "Error output.csv";
            String recoveryModeName = "";
            
            if(recoveryModeSelected == true)
            {
                recoveryModeName = getRecoveryModeName(databaseName);
                System.out.println("RECOVERY: " + recoveryModeName + " SELECTED: " + recoveryToggle);     
                
                if(recoveryModeName.equals(recoveryToggle) == false)
                {
                    //Switching to BULK_LOGGED for logging minimalization.
                    sql += "ALTER DATABASE " + databaseName + " SET RECOVERY " + recoveryToggle + ";\n";
                    
                    //Making a proper SQL string.
                    sql += "BULK INSERT " + chosenTableName + " FROM '" + csvFileName + "' WITH ("
                          + "FIRSTROW = 1,"
                          + "FIELDTERMINATOR = '|',"
                          + "ROWTERMINATOR = '\r\n',"
                          + "ERRORFILE = '" + csvErrorFileName + "',"
                          + "TABLOCK)\n";
                    
                    //Switching back previous recovery state.
                    sql += "ALTER DATABASE " + databaseName + " SET RECOVERY " + recoveryModeName + ";\n";
                    
                    statement = connection.createStatement();
                    statement.execute(sql);
                    statement.close();
                }
                //same recovery mode.
                else
                {
                    //Making a proper SQL string.
                    sql += "BULK INSERT " + chosenTableName + " FROM '" + csvFileName + "' WITH ("
                          + "FIRSTROW = 1,"
                          + "FIELDTERMINATOR = '|',"
                          + "ROWTERMINATOR = '\r\n',"
                          + "ERRORFILE = '" + csvErrorFileName + "',"
                          + "TABLOCK)\n";
                    
                    statement = connection.createStatement();    
                    statement.execute(sql);
                    statement.close();
                }
            }
            else
            {
                //Making a proper SQL string.
                sql += "BULK INSERT " + chosenTableName + " FROM '" + csvFileName + "' WITH ("
                      + "FIRSTROW = 1,"
                      + "FIELDTERMINATOR = '|',"
                      + "ROWTERMINATOR = '\r\n',"
                      + "ERRORFILE = '" + csvErrorFileName + "',"
                      + "TABLOCK)\n";

                statement = connection.createStatement();  
                statement.execute(sql);
                statement.close();
            }
            
            deleteFile(csvFileName);
            deleteFile(csvErrorFileName);            
        }
        catch (SQLException exception)
        {
            
            try
                {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("error.txt"), StandardCharsets.UTF_8)))
                    {
                        bufferedWriter.write(exception.toString());
                    }
                }
                catch(IOException e)
                {

                }
            
            System.out.println("ERROR: " + exception.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while injecting generated data to database... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public void exportDataWithImages(String tableName, List<String> imageTypeGeneratedFiles, Boolean recoveryModeSelected, String recoveryToggle)
    {
        try
        {
            String recoveryModeName = "";
            String sql = "";
            statement = connection.createStatement();
            if(recoveryModeSelected == true)
            {
                recoveryModeName = getRecoveryModeName(databaseName);
                statement = connection.createStatement();
                System.out.println("RECOVERY: " + recoveryModeName + " SELECTED: " + recoveryToggle);
                if(recoveryModeName.equals(recoveryToggle) == false)
                {
                    //Switching to BULK_LOGGED for logging minimalization.
                    sql = "ALTER DATABASE " + databaseName + " SET RECOVERY " + recoveryToggle + ";\n";                    
                    statement.execute(sql);
                    
                    for(String fileName: imageTypeGeneratedFiles)
                    {
                        try
                        {
                            File file = new File(fileName);
                            sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);     
                            statement.execute(sql);
                        }
                        catch(IOException exception)
                        {
                            System.out.println("ERROR: " + exception.toString());
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText("Something went wrong!");
                            alert.setContentText
                            (
                                "Something went wrong while injecting generated data to database... - " + exception.toString()
                            );
                            alert.showAndWait();
                        }
                    }
                    
                    //Switching back previous recovery state.
                    sql = "ALTER DATABASE " + databaseName + " SET RECOVERY " + recoveryModeName + ";\n";
                    statement.execute(sql);
                    
                    //statement.close();
                }
                //same recovery mode.
                else
                {
                    //statement = connection.createStatement();
                    for(String fileName: imageTypeGeneratedFiles)
                    {
                        try
                        {
                            File file = new File(fileName);
                            sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);                        
                            statement.execute(sql);
                        }
                        catch(IOException exception)
                        {
                            System.out.println("ERROR: " + exception.toString());
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error!");
                            alert.setHeaderText("Something went wrong!");
                            alert.setContentText
                            (
                                "Something went wrong while injecting generated data to database... - " + exception.toString()
                            );
                            alert.showAndWait();
                        }
                    }
                    //statement.close();
                }
            }
            else
            {
                for(String fileName: imageTypeGeneratedFiles)
                {
                    try
                    {
                        File file = new File(fileName);
                        //statement = connection.createStatement();
                        sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                        //System.out.println("SQL?" + sql);
                        statement.execute(sql);
                        //statement.close();
                    }
                    catch(IOException exception)
                    {
                        System.out.println("ERROR: " + exception.toString());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setHeaderText("Something went wrong!");
                        alert.setContentText
                        (
                            "Something went wrong while injecting generated data to database... - " + exception.toString()
                        );
                        alert.showAndWait();
                    }
                }
            }
            statement.close();
        }
        catch (SQLException exception)
        {
            System.out.println("ERROR: " + exception.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while injecting generated data to database... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public boolean addTable(String tableName, List<String> chosenNamesList, List<String> chosenTypesList, List<String> chosenRelationsList, List<Boolean> allowNullsList)
    {
        try
        {
            String sql = "";
            String chosenTableName;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            //Solving tableName.
            Matcher whitespaceNameMatcher = whitespacePattern.matcher(tableName);
            boolean isNameContainsWhitespace = whitespaceNameMatcher.find();
            Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
            boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
            if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
            {
                chosenTableName = tableName;
            }
            else
            {
                chosenTableName = "\"" + tableName + "\"";
            }
            
            sql += "create table " + chosenTableName + " (";
            
            for(int i = 0; i < chosenNamesList.size(); i++)
            {
                String columnName = "";
                String typeName = "";
                whitespaceNameMatcher = whitespacePattern.matcher(chosenNamesList.get(i));
                isNameContainsWhitespace = whitespaceNameMatcher.find();
                fullDigitNameMatcher = fullDigitNamePattern.matcher(chosenNamesList.get(i));
                isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
                
                //Column Name:
                if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
                {
                    columnName = chosenNamesList.get(i);
                }
                else
                {
                    columnName = "\"" + chosenNamesList.get(i) + "\"";
                }
                
                //Column Types:
                switch(chosenTypesList.get(i))
                {
                    case "bit":
                    {
                        typeName = " bit";
                        break;
                    }
                    case "tinyint":
                    {
                        typeName = " tinyint";
                        break;
                    }
                    case "smallint":
                    {
                        typeName = " smallint";
                        break;
                    }
                    case "int":
                    {
                        typeName = " int";
                        break;
                    }
                    case "bigint":
                    {
                        typeName = " bigint";
                        break;
                    }
                    case "float":
                    {
                        typeName = " float";
                        break;
                    }
                    case "real":
                    {
                        typeName = " real";
                        break;
                    }
                    case "varchar":
                    {
                        typeName = " varchar(MAX)";
                        break;
                    }
                    case "nvarchar":
                    {
                        typeName = " nvarchar(MAX)";
                        break;
                    }
                    case "text":
                    {
                        typeName = " text";
                        break;
                    }
                    case "ntext":
                    {
                        typeName = " ntext";
                        break;
                    }
                    case "PK":
                    {
                        typeName = " integer primary key";
                        break;
                    }
                    case "FK":
                    {
                        typeName = " int foreign key references ";
                        break;
                    }
                    case "varbinary":
                    {
                        typeName = " varbinary(MAX)";
                        break;
                    }
                }
                                
                //Column relation:
                if(chosenRelationsList.get(i).equals("NONE") == false)
                {
                    DatabaseMetaData databaseMetaData = getMetaData();
                    ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null, null, chosenRelationsList.get(i));            
                    String primaryKeyColumnName = "";
                    //find primary key inside the table.
                    if(primaryKeys.next())
                    {
                        primaryKeyColumnName = primaryKeys.getString(4);
                    }
                    
                    //Names with whitespaces etc:
                    String relationTableName;
                    whitespaceNameMatcher = whitespacePattern.matcher(chosenRelationsList.get(i));
                    isNameContainsWhitespace = whitespaceNameMatcher.find();
                    fullDigitNameMatcher = fullDigitNamePattern.matcher(chosenRelationsList.get(i));
                    isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
                    if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
                    {
                        relationTableName = chosenRelationsList.get(i);
                    }
                    else
                    {
                        relationTableName = "\"" + chosenRelationsList.get(i) + "\"";
                    }
                    String relationColumnName;
                    whitespaceNameMatcher = whitespacePattern.matcher(primaryKeyColumnName);
                    isNameContainsWhitespace = whitespaceNameMatcher.find();
                    fullDigitNameMatcher = fullDigitNamePattern.matcher(primaryKeyColumnName);
                    isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
                    if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
                    {
                        relationColumnName = primaryKeyColumnName;
                    }
                    else
                    {
                        relationColumnName = "\"" + primaryKeyColumnName + "\"";
                    }
                    
                    typeName += relationTableName + "(" + relationColumnName + ")";
                }
                
                if(allowNullsList.get(i).equals(true))
                {
                    sql += columnName + typeName;
                }
                else
                {
                    sql += columnName + typeName + " NOT NULL";
                }
                
                //Adding a comma and the right parenthesis.
                if(i < chosenNamesList.size() - 1)
                {
                    sql += ", ";
                }
                else
                {
                    sql += ")";
                }                
            }
            
            System.out.println(sql);
                                
            statement = connection.createStatement();
            statement.execute(sql);
            statement.close();  
            return true;
        }
        catch (SQLException exception)
        {
            System.out.println("ERROR: " + exception.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while creating a new table in database... - " + exception.toString()
            );
            alert.showAndWait();
            return false;
        }
    }
    
    public void deleteFile(String fileName)
    {
        File file = new File(fileName);
        file.delete();
    }
    
    public String getRecoveryModeName(String databaseName)
    {
        String recoveryModeName = "";
        try
        {
            String sql = "";
            String chosenDatabaseName;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            //Solving tableName.
            Matcher whitespaceDatabaseNameMatcher = whitespacePattern.matcher(databaseName);
            boolean isDatabaseNameContainsWhitespace = whitespaceDatabaseNameMatcher.find();
            Matcher fullDigitDatabaseNameMatcher = fullDigitNamePattern.matcher(databaseName);
            boolean isDatabaseNameIsMadeOfFullDigits = fullDigitDatabaseNameMatcher.find();
            if(isDatabaseNameContainsWhitespace == false && isDatabaseNameIsMadeOfFullDigits == false)
            {
                chosenDatabaseName = databaseName;
            }
            else
            {
                chosenDatabaseName = "\"" + databaseName + "\"";
            }            
            
            //Making a proper SQL string.
            sql += "SELECT RECOVERY_MODEL_DESC FROM SYS.DATABASES WHERE NAME = '" + chosenDatabaseName + "'";
            statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(sql)) 
            {
                if(resultSet.next())
                {
                    recoveryModeName = resultSet.getString(1);      
                }  
                              
            }
            statement.close();           
        }
        catch (SQLException exception)
        {
            System.out.println("ERROR: " + exception.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while injecting generated data to database... - " + exception.toString()
            );
            alert.showAndWait();
        }
        return recoveryModeName;
    }
    
    public void testInsert()
    {
        try
        {
            //Making a proper SQL string.
            String sql = "INSERT INTO imagetabinc VALUES " +
            "('testowy', (select * from openrowset(bulk N'E:\\CODE WORKSTATION\\JAVA WORKSPACE\\Własne\\DG\\DataGenerator\\ImageContainer\\przyklad.jpg', single_blob) as img))";
            statement = connection.createStatement();
            statement.execute(sql);
            statement.close();      
        }
        catch (SQLException exception)
        {
            System.out.println("ERROR: " + exception.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while injecting test data to database... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
}
