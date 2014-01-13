package jnode.altrssposter;

import jnode.store.XMLSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Manjago (kirill@temnenkov.com)
 */
public class Watermarks {

    private final String datafile;

    public Watermarks(String datafile) {
        this.datafile = datafile;
    }

    private Map<String, String> load() throws FileNotFoundException {
        synchronized (Watermarks.class){
            return internalLoad();
        }
    }

    @SuppressWarnings("unchecked")
	private Map<String, String> internalLoad() throws FileNotFoundException {
        return new File(datafile).exists() ?
                    (Map<String, String>) XMLSerializer.read(datafile) :
                    new HashMap<String, String>();
    }

    public String readValue(String key) throws FileNotFoundException {
        Map<String, String> watermarks = load();
        return watermarks.containsKey(key) ? watermarks.get(key) : null;
    }

    public void storeValue(String key, String value) throws FileNotFoundException {
        synchronized (Watermarks.class){
            Map<String, String> watermarks = internalLoad();
            watermarks.put(key, value);
            XMLSerializer.write(watermarks, datafile);
        }
    }

    public static void main(String[] args) {
        class TestThread extends Thread{

            @Override
            public void run() {
                try {
                    String key = ""+getId();
                    Watermarks w = new Watermarks("/temp/del/t.xml");

                    String value = null;

                    while(!"19".equals(value)){
                        value = w.readValue(key);
                        if (value == null){
                            value = "0";
                        }

                        sleep(1000);
                        int next = Integer.valueOf(value) + 1;
                        System.out.println(MessageFormat.format("thread {0} oldvalue {1} newvalue {2}",
                                key, value, next));
                        w.storeValue(key, ""+next);
                        sleep(1000);
                    }

                } catch (FileNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for(int i = 0; i < 20; ++i){
           new TestThread().start();
        }

    }
}
