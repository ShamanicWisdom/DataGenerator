/*
               --DataGenerator--
    This class contains all generation methods.
*/

package szaman.datagenerator.utility;

//Other own classes:



//Other classes:


import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Szaman
 */

public class GenerationMethods 
{
    //BOOLEAN
    public boolean booleanValue()
    {
        return ThreadLocalRandom.current().nextBoolean();
    }   
    
    //BYTE
    public byte byteNumberWithinRange(int minimum, int maximum)
    {
        int value = ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
        return (byte)value;
    }
    
    public byte randomByteNumber()
    {
        int value = ThreadLocalRandom.current().nextInt(0, 255);
        return (byte)value;
    }
    
    //SHORT
    public short shortNumberWithinRange(int minimum, int maximum)
    {
        int value = ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
        return (short)value;
    }    
    
    public short randomShortNumber()
    {
        int value = ThreadLocalRandom.current().nextInt(-32768, 32767);
        return (short)value;
    } 
    
    //INTEGER
    public int intNumberWithinRange(int minimum, int maximum)
    {
        return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
    }
    
    public int randomIntNumber()
    {
        return ThreadLocalRandom.current().nextInt(-2147483648, 2147483647);
    }
        
    //LONG
    public long longNumberWithinRange(long minimum, long maximum)
    {
        long value = ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
        return value;
    }  
    
    public long randomLongNumber()
    {
        long value = ThreadLocalRandom.current().nextLong(-9223372036854775808L, 9223372036854775807L);
        return value;
    } 
    
    //FLOAT
    
    
    
    //DOUBLE
    public double unsignedFraction()
    {
        return ThreadLocalRandom.current().nextDouble();
    }
    
    public double realNumberWithinRange(int minimum, int maximum)
    {
        return ThreadLocalRandom.current().nextDouble(minimum, maximum + 1);
    }    
}
