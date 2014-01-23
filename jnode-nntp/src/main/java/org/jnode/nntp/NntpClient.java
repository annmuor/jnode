package org.jnode.nntp;

import jnode.logger.Logger;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.exception.EndOfSessionException;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.NntpCommand;
import org.jnode.nntp.model.NntpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static java.util.Collections.EMPTY_LIST;

public class NntpClient implements Runnable {

    private static final Logger logger = Logger.getLogger(NntpClient.class);

    private static final String DELIMITER = " ";

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

        send(NntpResponse.InitialGreetings.READY);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String command = read();
                String response = process(command);
                send(response);
                // if command = quit - mark thread as interrupted
            } catch (UnknownCommandException uce) {
                logger.l4("Unknown command.");
            } catch (EndOfSessionException eose) {
                logger.l4("Client cancel session.");
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                logger.l4("Unknown problem during command processing.", e);
                Thread.currentThread().interrupt();
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
        if (line == null) {
            throw new EndOfSessionException();
        }
        logger.l4("--->" + line);
        return line;
    }

    private String process(String command) {

        NntpCommand parsedCommand = findCommand(command);
        if (parsedCommand != null) {
            return ProcessorResolver.processor(parsedCommand).process(parsedCommand.getParams());
        }

        throw new UnknownCommandException();
    }

    private NntpCommand findCommand(String command) {
        String[] parts = StringUtils.split(command, DELIMITER);
        NntpCommand foundedCommand;

        if (parts == null) {
            throw new UnknownCommandException();
        }

        if (parts.length == 1) {
            foundedCommand = NntpCommand.find(command);
            if (foundedCommand == null) {
                throw new UnknownCommandException();
            }
            foundedCommand.setParams(EMPTY_LIST);
            return foundedCommand;
        }

        foundedCommand = NntpCommand.find(parts[0] + DELIMITER + parts[1]);
        if (foundedCommand == null) {
            foundedCommand = findCommand(parts[0]);
            if (foundedCommand == null) {
                throw new UnknownCommandException();
            } else {
                foundedCommand.setParams(prepareParams(parts, true));
            }
        } else {
            foundedCommand.setParams(prepareParams(parts, false));
        }
        return foundedCommand;
    }

    private Collection<String> prepareParams(String[] params, boolean isOnePartCommand) {
        Collection<String> collection = new LinkedList<>();
        Collections.addAll(collection, params);
        collection.remove(collection.iterator().next());
        if (!isOnePartCommand) {
            collection.remove(collection.iterator().next());
        }
        return collection;
    }
}
