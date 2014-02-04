package org.jnode.nntp;

import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.Notifier;
import jnode.logger.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.event.GroupSelectedEvent;
import org.jnode.nntp.exception.EndOfSessionException;
import org.jnode.nntp.exception.ProcessorNotFoundException;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NntpCommand;
import org.jnode.nntp.model.NntpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static java.util.Collections.EMPTY_LIST;

public class NntpClient implements Runnable {

    private static final Logger logger = Logger.getLogger(NntpClient.class);

    private static final String DELIMITER = " ";

    private OutputStream out;
    private InputStream in;
    private Socket socket;

    private NewsGroup selectedGroup;

    public NntpClient(Socket socket) {
        try {
            this.socket = socket;
            this.out = socket.getOutputStream();
            this.in = socket.getInputStream();
        } catch (IOException e) {
            logger.l1("NNTP client can't be initialised.", e);
        }
    }

    @Override
    public void run() {
        Notifier.INSTANSE.register(GroupSelectedEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                GroupSelectedEvent groupSelectedEvent = (GroupSelectedEvent) event;
                selectedGroup = groupSelectedEvent.getSelectedGroup();
            }
        });

        // Send greetings. Posting is not implemented yet.

        String command = StringUtils.EMPTY;

        try {
            send(Arrays.asList(NntpResponse.InitialGreetings.SERVICE_AVAILABLE_POSTING_PROHIBITED));

            while ((command = IOUtils.toString(in)) != null) {
                logger.l4("[C] " + command);
                Collection<String> response = process(command, selectedGroup);
                send(response);
            }

        } catch (UnknownCommandException uce) {
            logger.l4("Unknown command '" + command + "'.");
        } catch (EndOfSessionException eose) {
            logger.l4("Client cancel session.");
        } catch (Throwable e) {
            logger.l4("Unknown problem during command processing.", e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(socket);
        }

    }

    /**
     * Send response to client.
     *
     * @param response response.
     */
    private void send(Collection<String> response) throws IOException {
        for (String message : response) {
            logger.l4("[S] " + message);
            IOUtils.write(message, out);
        }
        out.flush();
    }

    /**
     * Process command from client.
     *
     * @param command command.
     * @param selectedGroup
     * @return response.
     */
    private Collection<String> process(String command, NewsGroup selectedGroup) {
        NntpCommand parsedCommand = findCommand(command);
        if (parsedCommand != null) {
            Processor processor = ProcessorResolver.processor(parsedCommand);
            if (processor == null) {
                logger.l4("Can't find processor for command '" + command + "'.");
                throw new ProcessorNotFoundException();
            }
            return processor.process(parsedCommand.getParams(), selectedGroup == null ? null : selectedGroup.getId());
        }

        throw new UnknownCommandException();
    }

    /**
     * Find command in supported commands.
     * @param command command.
     * @return supported command.
     */
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

    /**
     * Prepare command params for process by processor.
     * @param params command params.
     * @param isOnePartCommand is command include one part only.
     * @return collection of params.
     */
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
