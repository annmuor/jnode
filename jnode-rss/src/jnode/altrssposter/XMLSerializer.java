package jnode.altrssposter;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public final class XMLSerializer {


    private XMLSerializer() {
    }

    public static void write(Object f, String filename) throws FileNotFoundException {
        XMLEncoder encoder =
                new XMLEncoder(
                        new BufferedOutputStream(
                                new FileOutputStream(filename)));
        encoder.writeObject(f);
        encoder.close();
    }

    public static Object read(String filename) throws FileNotFoundException {
        XMLDecoder decoder =
                new XMLDecoder(new BufferedInputStream(
                        new FileInputStream(filename)));
        Object o = decoder.readObject();
        decoder.close();
        return o;
    }
}
