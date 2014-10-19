package ai.autonumber.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Andrew on 19.10.2014.
 */
public class StreamUtils {
    public static void copyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {

            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                //Read byte from input stream

                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ignored){}
    }
}
