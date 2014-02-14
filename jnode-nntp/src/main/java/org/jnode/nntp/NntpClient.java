package org.jnode.nntp;

import com.google.common.collect.Lists;
import jnode.event.IEvent;
import jnode.event.IEventHandler;
import jnode.event.Notifier;
import jnode.logger.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.event.ArticleSelectedEvent;
import org.jnode.nntp.event.AuthUserEvent;
import org.jnode.nntp.event.GroupSelectedEvent;
import org.jnode.nntp.event.PostEndEvent;
import org.jnode.nntp.event.PostStartEvent;
import org.jnode.nntp.exception.EndOfSessionException;
import org.jnode.nntp.exception.ProcessorNotFoundException;
import org.jnode.nntp.exception.UnknownCommandException;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpCommand;
import org.jnode.nntp.model.NntpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
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

    private Long selectedGroupId;
    private Long selectedArticleId;
    private Auth auth;

    private boolean isPost = false;
    private Collection<String> postParams;

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
        Notifier.INSTANSE.register(GroupSelectedEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                GroupSelectedEvent groupSelectedEvent = (GroupSelectedEvent) event;
                selectedGroupId = groupSelectedEvent.getSelectedGroup().getId();
            }
        });
        Notifier.INSTANSE.register(ArticleSelectedEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                selectedArticleId = ((ArticleSelectedEvent) event).getSelectedArticleId();
            }
        });
        Notifier.INSTANSE.register(AuthUserEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                auth = ((AuthUserEvent) event).getAuth();
            }
        });
        Notifier.INSTANSE.register(PostStartEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                isPost = true;
                postParams = Lists.newLinkedList();
            }
        });
        Notifier.INSTANSE.register(PostEndEvent.class, new IEventHandler() {
            @Override
            public void handle(IEvent event) {
                isPost = false;
            }
        });

        String line = StringUtils.EMPTY;

        try {
            // Send greetings. Posting is not implemented yet.
            send(Arrays.asList(NntpResponse.InitialGreetings.SERVICE_AVAILABLE_POSTING_ALLOWED));

            while ((line = in.readLine()) != null) {
                logger.l4("[C] " + line);

                Collection<String> response = Lists.newLinkedList();

                if (line.equalsIgnoreCase(NntpResponse.END)) {
                    // end of post
                    response.addAll(process(NntpCommand.POST, postParams));
                } else {
                    if (isPost) {
                        // post in progress
                        postParams.add(line);
                    } else {
                        // casual line
                        response.addAll(process(line));
                    }
                }

                send(response);

            }

        } catch (UnknownCommandException uce) {
            logger.l4("Unknown line '" + line + "'.");
        } catch (EndOfSessionException eose) {
            logger.l4("Client cancel session.");
        } catch (Throwable e) {
            logger.l4("Unknown problem during line processing.", e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(socket);
        }

    }

    private Collection<String> process(NntpCommand command, Collection<String> params) {
        Processor processor = ProcessorResolver.processor(command);
        if (processor == null) {
            logger.l4("Can't find processor for command '" + command + "'.");
            throw new ProcessorNotFoundException();
        }
        return processor.process(params, selectedGroupId, selectedArticleId, auth);
    }

    /**
     * Send response to client.
     *
     * @param response response.
     */
    private void send(Collection<String> response) throws IOException {
        for (String message : response) {
            logger.l4("[S] " + message);
            out.println(message);
        }
    }

    /**
     * Process command from client.
     *
     * @param command command.
     * @return response.
     */
    private Collection<String> process(String command) {
        NntpCommand parsedCommand = findCommand(command);
        if (parsedCommand != null) {
            Processor processor = ProcessorResolver.processor(parsedCommand);
            if (processor == null) {
                logger.l4("Can't find processor for command '" + command + "'.");
                throw new ProcessorNotFoundException();
            }
            return processor.process(parsedCommand.getParams(), selectedGroupId, selectedArticleId, auth);
        }

        throw new UnknownCommandException();
    }

    /**
     * Find command in supported commands.
     *
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
     *
     * @param params           command params.
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
