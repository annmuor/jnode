package integration;

import jnode.module.JnodeModuleException;
import org.jnode.rest.Main;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.GuestLoginRestCommand;
import rest.RestCommand;
import rest.RestResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuestLoginIT {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        EXECUTOR_SERVICE.execute(() -> {
            try {
                Main mainModule = new Main(Main.class.getResource("config-rest.properties").getPath());
                mainModule.startForTest();
            } catch (JnodeModuleException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(5000L);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        EXECUTOR_SERVICE.shutdownNow();
    }

    @Test
    public void login() throws Exception {
        RestCommand guestLoginCmd = new GuestLoginRestCommand();
        RestResult restResult = guestLoginCmd.execute();
        System.out.println(restResult);
    }
}
