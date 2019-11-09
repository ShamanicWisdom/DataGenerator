/*
                --DataGenerator--
    Utility class contains minor cosmetic methods.
*/

package szaman.datagenerator.utility;

//Other own classes:



//Other classes:

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Szaman
 */

public class Utility 
{
    //Program's name.
    String programName = "DataGenerator";
    //Author's nickname.
    String authorNickname = "Szaman";
    //Recent build number, manually changed.
    String applicationBuildNumber = "0.9.89";
    
    //Pull program's name only.
    public String pullProgramName()
    {
        return programName;
    }
    
    //Pull author's nickname only.
    public String pullAuthorNickname()
    {
        return authorNickname;
    }
    
    //Pull build number only.
    public String pullBuildNumber()
    {
        String build = "Build: " + applicationBuildNumber;
        return build;
    }
    
    //Pull build number with a latest date (received from last modification date of Utility.java file).
    public String pullBuildNumberWithDate()
    {
        //Attempting to grip Utility.java file.
        File file = new File("src/main/java/szaman/datagenerator/utility/Utility.java");
        //Reading last modification date of Utility.java file.
        Date date = new Date(file.lastModified());  
        //Date formatting.
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm");
        
        String build = "";
        if(file.exists())
        {
            build = pullBuildNumber() + ", " + dateFormat.format(date);
        }
        else
        {
            build = pullBuildNumber();
        }
         
        return build;
    }
    
    //Get date - for output's name.
    public String getCurrentDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH;mm;ss");  
        Date date = new Date();   
        return formatter.format(date);
    }
}
