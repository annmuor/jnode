package jnode.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class FileUtils {
    public static String readFile(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size());
            return Charset.forName("UTF8").decode(bb).toString();
        }
    }

}
