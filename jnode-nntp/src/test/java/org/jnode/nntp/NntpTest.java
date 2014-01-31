package org.jnode.nntp;

import jnode.logger.Logger;
import jnode.module.JnodeModuleException;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class NntpTest {

    private Logger logger = Logger.getLogger(NntpTest.class);

    @Test
    public void runServer() throws JnodeModuleException {
        NntpModule module = new NntpModule("/tmp/bla.properties");
        module.start();
    }


    @Test
    public void newsGroups() throws IOException {
        NNTPClient client = new NNTPClient();
        client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
        client.connect("127.0.0.1", 1119);


        for (NewsgroupInfo info : client.listNewsgroups()) {
            logger.l4("-> " + info.getNewsgroup());
        }


        NewsgroupInfo group = new NewsgroupInfo();
     //   client.selectNewsgroup("group1", group);

        BufferedReader br;
        String line;
        br = (BufferedReader) client.retrieveArticleHeader("10100");

        if (br != null) {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        }
        br = (BufferedReader) client.retrieveArticleBody("10100");

        if (br != null) {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        }

    }

}
