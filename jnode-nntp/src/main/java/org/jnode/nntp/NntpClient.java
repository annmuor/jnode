package org.jnode.nntp;

import jnode.logger.Logger;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.response.InitialGreeting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NntpClient implements Runnable {

    private static final Logger logger = Logger.getLogger(NntpClient.class);

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public NntpClient(Socket socket) {
        try {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            logger.l1("NNTP client can't be initialised.", e);
        }
    }

    @Override
    public void run() {

        /*

22-01-2014 18:08:53 [00000001] NntpModule           New client accepted.
22-01-2014 18:08:53 [00000011] NntpClient           LIST EXTENSIONS
22-01-2014 18:08:53 [00000011] NntpClient           MODE READER
22-01-2014 18:08:53 [00000011] NntpClient           XOVER

         */

        send(new InitialGreeting().response());

        while (!Thread.currentThread().isInterrupted()) {


            try {
                String command = read();
                String response = process(command);
                send(response);
                // if command = quit - mark thread as interrupted
            } catch (UnknownCommandException uce) {
                logger.l4("Unknown command.");
            } catch (Throwable e) {
                Thread.currentThread().interrupt();
                logger.l4("Unknown problem during command processing.", e);
            }
        }


        out.close();

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void send(String message) {
        logger.l4("<---" + message);
        out.println(message);
    }

    private String read() throws IOException {
        String line = in.readLine();
        logger.l4("--->" + line);
        return line;
    }

    private String process(String command) {
        // todo parse command
        NntpCommand parsedCommand = NntpCommand.find(command);
        if (parsedCommand != null) {
            return ProcessorResolver.processor(parsedCommand).process();
        }

        return ".";
    }
}
