/*
               --DataGenerator--
    This class contains all embedded database generation methods.
*/

package szaman.datagenerator.utility;

//Other own classes:



//Other classes:

import java.util.List;
import java.util.Random;

/**
 *
 * @author Szaman
 */

public class SingleDataUtility 
{
    public String getRandomContent(List<String> contentTable)
    {
        Random random = new Random();
        int randomRecordNumber = random.nextInt(contentTable.size() - 1);
        String content = contentTable.get(randomRecordNumber);
        return content;
    }
}
