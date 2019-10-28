/*
        --DataGenerator--
    DataPoolUtility class contains all handling and references for embedded database (Apache Derby).
*/

package szaman.datagenerator.utility;

//Other own classes:


import szaman.datagenerator.view.datapool.model.PoolData;
import szaman.datagenerator.view.datapool.model.PoolTable;

//Other classes:

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
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

/**
 *
 * @author Szaman
 */
public class DataPoolUtility 
{
    //Embedded database's connection credentials
    String databaseName = "GENERATORDATABASE";
    String databaseUsername = "embeddedDB";
    String databasePassword = "embeddedDB";
    
    Connection connection = null;
    Statement statement = null;
    
    //Final database's connection URL.
    private final String derbyDatabaseURL = "jdbc:derby:" + databaseName + ";create=true;user=" + databaseUsername + ";password=" + databasePassword;
    
    //Create a connection with embedded database.
    public void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");            
            connection = DriverManager.getConnection(derbyDatabaseURL);    
        }
        catch(ClassNotFoundException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Embedded Database Error!");
            alert.setContentText
            (
                "There is a problem with Embedded Database!\n"
                + "(Apache Derby 10.14.1.0)\n"
                + "Please inform the developer about this!\n\n"
                + "Error: " + exception.toString()
            );
            alert.showAndWait(); 
        }
        catch(SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Embedded Database Error!");
            alert.setContentText
            (
                "There is a problem with JDBC Driver!\n"
                + "(Apache Derby 10.14.1.0)\n"
                + "Most known issue is started more than one instance of this program.\n"
                + "If not, please inform the developer about this!\n\n"
                + "Error: " + exception.toString()
            );
            alert.showAndWait(); 
        }
    }
    
    public List<PoolTable> getPoolTableData()
    {
        List<PoolTable> poolTableList = new ArrayList<>();
        try
        {
            statement = connection.createStatement();     
            
            DatabaseMetaData databaseMetaData = connection.getMetaData(); 
            
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) 
            {
                while(resultSet.next())
                {
                    PoolTable poolTable = new PoolTable();
                    poolTable.setPoolTableName(resultSet.getString(3));
                    poolTable.setPoolDataCount(getTableRowsCount(resultSet.getString(3)));
                    poolTable.setPoolTableType(getTableType(resultSet.getString(3)));
                    poolTableList.add(poolTable);

                    System.out.println(poolTable.getPoolTableName() + " (" + poolTable.getPoolTableType() + "): " + poolTable.getPoolDataCount());                                        
                }
                if(poolTableList.isEmpty())
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
        return poolTableList;
    }
    
    public int getTableRowsCount(String tableName)
    {
        int count = 0;
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(tableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "select count(*) from " + tableName;
        }
        else
        {
            sql = "select count(*) from " + "\"" + tableName + "\"";
        }
        try
        {
            statement = connection.createStatement();
            try (ResultSet result = statement.executeQuery(sql)) 
            {
                if(result.next())
                {
                    count = result.getInt(1);                                        
                }
            }
            statement.close();            
        }
        catch(SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
                (
                    "Something went wrong while getting specified table rowcount... " + exception.toString()
                );
            alert.showAndWait();
        }
        return count;
    }
    
    public String getTableType(String tableName)
    {
        String tableType = "";
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(tableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "select POOLDATA from " + tableName;
        }
        else
        {
            sql = "select POOLDATA from " + "\"" + tableName + "\"";
        }
        try
        {
            statement = connection.createStatement();
            
            
            
            try (ResultSet resultSet = statement.executeQuery(sql);) 
            {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                tableType = resultSetMetaData.getColumnTypeName(1); 
            }
            statement.close();            
        }
        catch(SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
                (
                    "Something went wrong while getting specified table rowcount... " + exception.toString()
                );
            alert.showAndWait();
        }
        return tableType;
    }
    
    public boolean isPoolTableExists(String poolTableName)
    {
        boolean isTableExists = false;
        try
        {
            createConnection();
            statement = connection.createStatement();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, poolTableName.toUpperCase(), new String[]{"TABLE"})) 
            {
                while(resultSet.next())
                {
                    System.out.println("Table " + poolTableName + " exists...");
                    isTableExists = true;
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
                    "Something went wrong while getting table data... " + exception.toString()
                );
            alert.showAndWait();
        }
        return isTableExists;
    }
    
    public void addPoolTable(String tableName) 
    {
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(tableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "CREATE TABLE " + tableName.toUpperCase()
                + "(ID int primary key generated BY DEFAULT as identity(start with 1, increment by 1), "
                + "POOLDATA varchar(500), LASTMODIFIED TIMESTAMP)";
        }
        else
        {
            sql = "CREATE TABLE " + "\"" + tableName.toUpperCase() + "\" " 
                + "(ID int primary key generated BY DEFAULT as identity(start with 1, increment by 1), "
                + "POOLDATA varchar(500), LASTMODIFIED TIMESTAMP)";
        }
        try 
        {
            //connection.createStatement().executeUpdate(sql);
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            setDefaultTimestamping(tableName.toUpperCase());
            
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while creating " + tableName + " table... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public void setDefaultTimestamping(String tableName) 
    {
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(tableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "ALTER TABLE " + tableName + " ALTER COLUMN LASTMODIFIED SET DEFAULT CURRENT_TIMESTAMP";
        }
        else
        {
            sql = "ALTER TABLE \"" + tableName + "\" ALTER COLUMN LASTMODIFIED SET DEFAULT CURRENT_TIMESTAMP";
        }
        try 
        {
            statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while setting up new table... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public void dropSelectedPoolTable(String poolTableName)
    {
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(poolTableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(poolTableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "DROP TABLE " + poolTableName;
        }
        else
        {
            sql = "DROP TABLE " + "\"" + poolTableName + "\"";
        }
        try 
        {
            connection.createStatement().executeUpdate(sql);
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while dropping a table... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public void changePoolTableName(String poolTableName, String poolTableNewName)
    {
        String oldTableName;
        String newTableName;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(poolTableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(poolTableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            oldTableName = poolTableName;
        }
        else
        {
            oldTableName = "\"" + poolTableName + "\"";
        }
        whitespaceMatcher = whitespacePattern.matcher(poolTableNewName);
        fullDigitNameMatcher = fullDigitNamePattern.matcher(poolTableNewName);
        isNameContainsWhitespace = whitespaceMatcher.find();
        isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            newTableName = poolTableNewName;
        }
        else
        {
            newTableName = "\"" + poolTableNewName + "\"";
        }
        try 
        {
            String sql = "RENAME TABLE " + oldTableName.toUpperCase() + " TO " + newTableName.toUpperCase();
            System.out.println(sql);
            connection.createStatement().executeUpdate(sql);
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while changing table's name... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public boolean checkIfTableDataIsSingle(String poolTableName)
    {
        boolean isTableDataIsSingle = false;
        try
        {
            createConnection();
            statement = connection.createStatement();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getColumns(null, null, poolTableName.toUpperCase(), null)) 
            {
                int columnCounter = 0;
                boolean isPoolDataColumnExists = false;
                while(resultSet.next())
                {
                    String name = resultSet.getString("COLUMN_NAME");
                    if(name.equals("POOLDATA"))
                    {
                        isPoolDataColumnExists = true;
                    }
                    columnCounter++;
                }
                if(isPoolDataColumnExists == true && columnCounter == 3)
                {
                    isTableDataIsSingle = true;
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
                "Something went wrong while getting table data... - " + exception.toString()
            );
            alert.showAndWait();
        }
        return isTableDataIsSingle;
    }
    
    public List<PoolData> getSingleDataFromTable(String poolTableName)
    {
        List<PoolData> poolDataList = new ArrayList<>();
        try
        {
            String sql;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Matcher whitespaceMatcher = whitespacePattern.matcher(poolTableName);
            boolean isNameContainsWhitespace = whitespaceMatcher.find();
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(poolTableName);
            boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
            if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
            {
                sql = "SELECT * FROM " + poolTableName;
            }
            else
            {
                sql = "SELECT * FROM " + "\"" + poolTableName + "\"";
            }
            statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(sql)) 
            {
                int i = 0;
                while(resultSet.next())
                {
                    PoolData dataRow = new PoolData();
                    dataRow.setPoolDataID(resultSet.getInt(1));
                    dataRow.setPoolData(resultSet.getString(2));
                    dataRow.setLastModified(resultSet.getDate(3));
                    poolDataList.add(dataRow);      
                }
                if(poolDataList.isEmpty())
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
                "Something went wrong while getting single-data table content... - " + exception.toString()
            );
            alert.showAndWait();
        }
        return poolDataList;
    }
    
    //INSERTING DATA
    public void insertSingleDataUsingString(List<String> dataList, String tableName, Boolean ignoreFirstRecord) 
    {
        System.out.println("TABLE NAME: " + tableName);
        String sql;
        Pattern whitespacePattern = Pattern.compile("\\s");
        Matcher whitespaceMatcher = whitespacePattern.matcher(tableName);
        boolean isNameContainsWhitespace = whitespaceMatcher.find();
        Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
        Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(tableName);
        boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();        
        int masterIterator;
        char apostropheSymbol = 0x0027;
        if(ignoreFirstRecord == true)
        {
            masterIterator = 1;
        }
        else
        {
            masterIterator = 0;
        }
        if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
        {
            sql = "INSERT INTO " + tableName.toUpperCase() + " VALUES";
            for(int i = masterIterator; i < dataList.size(); i++)
            {
                //Check, if data element contains ' symbol.
                if(dataList.get(i).contains("'"))
                {
                    String modifiedData = dataList.get(i);
                    int apostropheSymbolPosition = 1;                    
                    for(int apostropheCheckerIterator = 0; apostropheCheckerIterator < dataList.get(i).length(); apostropheCheckerIterator++)
                    {
                        if(dataList.get(i).charAt(apostropheCheckerIterator) == apostropheSymbol)
                        {
                            apostropheSymbolPosition += apostropheCheckerIterator;
                            modifiedData = new StringBuilder(modifiedData).insert(modifiedData.length() - apostropheSymbolPosition, "'").toString();
                        }
                    }
                    dataList.set(i, modifiedData);
                }
                if(i == dataList.size() - 1)
                {
                    sql += " (default, '" + dataList.get(i) + "', CURRENT_TIMESTAMP)";
                }
                else
                {
                    sql += " (default, '" + dataList.get(i) + "', CURRENT_TIMESTAMP),";
                }  
            }
        }
        else
        {
            sql = "INSERT INTO " + "\"" + tableName.toUpperCase() + "\" VALUES";
            for(int i = masterIterator; i < dataList.size(); i++)
            {
                //Check, if data element contains ' symbol.
                if(dataList.get(i).contains("'"))
                {
                    String modifiedData = dataList.get(i);
                    int apostropheSymbolPosition = 1;                    
                    for(int apostropheCheckerIterator = 0; apostropheCheckerIterator < dataList.get(i).length(); apostropheCheckerIterator++)
                    {
                        if(dataList.get(i).charAt(apostropheCheckerIterator) == apostropheSymbol)
                        {
                            apostropheSymbolPosition += apostropheCheckerIterator;
                            modifiedData = new StringBuilder(modifiedData).insert(modifiedData.length() - apostropheSymbolPosition, "'").toString();
                        }
                    }
                    dataList.set(i, modifiedData);
                }
                if(i == dataList.size() - 1)
                {
                    sql += " (default, '" + dataList.get(i) + "', CURRENT_TIMESTAMP)";
                }
                else
                {
                    sql += " (default, '" + dataList.get(i) + "', CURRENT_TIMESTAMP),";
                }    
            }
        }
        try 
        {
            int rowsCounter;
            if(masterIterator == 1)
            {
                rowsCounter = dataList.size() - 1;
            }
            else
            {
                rowsCounter = dataList.size();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Adding the data");
            alert.setContentText("Adding " + rowsCounter + " rows into " + tableName.toUpperCase() + " table...");
            alert.show();
            statement = connection.createStatement();
            statement.execute(sql);
            alert.close();
            statement.close();            
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while inserting data into " + tableName + " table... - " + exception.toString()
            );
            alert.showAndWait();
        }
    }
    
    public String createDelFile(List<String> dataList, String tableName, Boolean ignoreFirstRecord, int fileNumber)
    {      
        if(ignoreFirstRecord == true)
        {
            dataList.remove(0);
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        
        alert.setTitle("In Progress...");
        alert.setHeaderText("");
        alert.setContentText("Creating Data List...");
        alert.show();
        String fileName = "";
        alert.close();
        try
        {
            if(fileNumber == 0)
            {
                fileName = "InsertedData.del";
            }
            else
            {
                fileName = "InsertedData" + fileNumber + ".del";
            }
            
            Files.write(Paths.get(fileName), dataList);
            alert.close();
        }
        catch (IOException exception)
        {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while creating data file... " + exception.toString()
            );
            alert.showAndWait();
        }
        return fileName;
    }
    
    public void deleteDelFile(String fileName)
    {
        File file = new File(fileName);
        Alert alert;
        if(file.delete())
        {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Clearing...");
            alert.setContentText("Deleting data file...");
            alert.show();
        }
        else
        {
            
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while deleting data file... "
            );
            alert.showAndWait();
        }
        alert.close();
    }
    
    public void insertSingleDataUsingFile(String fileName, String tableName, int rowsCounter)
    {
        try 
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Adding the data");
            alert.setContentText("Adding " + rowsCounter + " rows into " + tableName + " table...");
            alert.show();
            
            CallableStatement callableStatement = connection.prepareCall("{CALL SYSCS_UTIL.SYSCS_IMPORT_DATA (?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            callableStatement.setString(1, null);
            callableStatement.setString(2, tableName);
            callableStatement.setString(3, "POOLDATA");
            callableStatement.setString(4, null);
            callableStatement.setString(5, fileName);
            callableStatement.setString(6, "|"); 
            callableStatement.setString(7, null); 
            callableStatement.setString(8, null); 
            callableStatement.setInt(9, 0);
                 
            callableStatement.execute();
            alert.close();
            callableStatement.close();  
        }
        catch (SQLException exception)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText
            (
                "Something went wrong while inserting data into " + tableName + " table... - " + exception.toString()
            );
            alert.showAndWait();
            exception.printStackTrace();
        }
    }
    
    //CATCHING DATA FOR GENERATION
    public List<String> getNonEmptyTableNames(List<String> methodsList)
    {
        try
        {
            DatabaseMetaData databaseMetaData = connection.getMetaData();   
            
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE"})) 
            {
                int i = 0;
                while(resultSet.next())
                {
                    if(getTableRowsCount(resultSet.getString(3)) != 0)
                    {
                        methodsList.add(resultSet.getString(3));
                        System.out.println(resultSet.getString(3) + " added.");
                    }                  
                }
            }
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
        return methodsList;
    }
    
    public List<String> pullContentFromSingleDataTable(String poolTableName)
    {
        List<String> poolDataList = new ArrayList<>();
        try
        {
            String sql;
            Pattern whitespacePattern = Pattern.compile("\\s");
            Matcher whitespaceMatcher = whitespacePattern.matcher(poolTableName);
            boolean isNameContainsWhitespace = whitespaceMatcher.find();
            Pattern fullDigitNamePattern = Pattern.compile("^[0-9]*$");
            Matcher fullDigitNameMatcher = fullDigitNamePattern.matcher(poolTableName);
            boolean isNameIsMadeOfFullDigits = fullDigitNameMatcher.find();
            if(isNameContainsWhitespace == false && isNameIsMadeOfFullDigits == false)
            {
                sql = "SELECT POOLDATA FROM " + poolTableName;
            }
            else
            {
                sql = "SELECT POOLDATA FROM " + "\"" + poolTableName + "\"";
            }
            statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(sql)) 
            {
                int i = 0;
                while(resultSet.next())
                {
                    poolDataList.add(resultSet.getString(1));      
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
                "Something went wrong while getting single-data table content... - " + exception.toString()
            );
            alert.showAndWait();
        }
        return poolDataList;
    }
}
