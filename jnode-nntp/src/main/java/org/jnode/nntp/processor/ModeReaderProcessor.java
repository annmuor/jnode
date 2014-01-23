package org.jnode.nntp.processor;

import org.jnode.nntp.Processor;

import java.util.Collection;

/**
 * <a href="http://tools.ietf.org/html/rfc3977#section-5.3">RFC 3977. Section 5.3.</a>
 * <p/>
 * 5.3.  MODE READER
 * <p/>
 * 5.3.1.  Usage
 * <p/>
 * Indicating capability: MODE-READER
 * <p/>
 * This command MUST NOT be pipelined.
 * <p/>
 * Syntax
 * MODE READER
 * <p/>
 * Responses
 * 200    Posting allowed
 * 201    Posting prohibited
 * 502    Reading service permanently unavailable [1]
 * <p/>
 * [1] Following a 502 response the server MUST immediately close the
 * connection.
 * <p/>
 * 5.3.2.  Description
 * <p/>
 * The MODE READER command instructs a mode-switching server to switch
 * modes, as described in Section 3.4.2.
 * <p/>
 * If the server is mode-switching, it switches from its transit mode to
 * its reader mode, indicating this by changing the capability list
 * accordingly.  It MUST then return a 200 or 201 response with the same
 * meaning as for the initial greeting (as described in Section 5.1.1).
 * Note that the response need not be the same as that presented during
 * the initial greeting.  The client MUST NOT issue MODE READER more
 * than once in a session or after any security or privacy commands are
 * issued.  When the MODE READER command is issued, the server MAY reset
 * its state to that immediately after the initial connection before
 * switching mode.
 * <p/>
 * If the server is not mode-switching, then the following apply:
 * <p/>
 * o  If it advertises the READER capability, it MUST return a 200 or
 * 201 response with the same meaning as for the initial greeting; in
 * this case, the command MUST NOT affect the server state in any
 * way.
 * <p/>
 * o  If it does not advertise the READER capability, it MUST return a
 * 502 response and then immediately close the connection.
 * <p/>
 * 5.3.3.  Examples
 * <p/>
 * Example of use of the MODE READER command on a transit-only server
 * (which therefore does not providing reading facilities):
 * <p/>
 * <pre>
 * [C] CAPABILITIES
 * [S] 101 Capability list:
 * [S] VERSION 2
 * [S] IHAVE
 * [S] .
 * [C] MODE READER
 * [S] 502 Transit service only
 * [Server closes connection.]
 * </pre>
 * <p/>
 * Example of use of the MODE READER command on a server that provides
 * reading facilities:
 * <p/>
 * [C] CAPABILITIES
 * [S] 101 Capability list:
 * [S] VERSION 2
 * [S] READER
 * [S] LIST ACTIVE NEWSGROUPS
 * [S] .
 * [C] MODE READER
 * [S] 200 Reader mode, posting permitted
 * [C] IHAVE <i.am.an.article.you.have@example.com>
 * [S] 500 Permission denied
 * [C] GROUP misc.test
 * [S] 211 1234 3000234 3002322 misc.test
 * <p/>
 * Note that in both of these situations, the client SHOULD NOT use MODE
 * READER.
 * <p/>
 * Example of use of the MODE READER command on a mode-switching server:
 * <p/>
 * <pre>
 * [C] CAPABILITIES
 * [S] 101 Capability list:
 * [S] VERSION 2
 * [S] IHAVE
 * [S] MODE-READER
 * [S] .
 * [C] MODE READER
 * [S] 200 Reader mode, posting permitted
 * [C] CAPABILITIES
 * [S] 101 Capability list:
 * [S] VERSION 2
 * [S] READER
 * [S] NEWNEWS
 * [S] LIST ACTIVE NEWSGROUPS
 * [S] STARTTLS
 * [S] .
 * </pre>
 * <p/>
 * In this case, the server offers (but does not require) TLS privacy in
 * its reading mode but not in its transit mode.
 * <p/>
 * Example of use of the MODE READER command where the client is not
 * permitted to post:
 * <p/>
 * <pre>
 * [C] MODE READER
 * [S] 201 NNTP Service Ready, posting prohibited
 * </pre>
 */
public class ModeReaderProcessor implements Processor {

    @Override
    public String process(Collection<String> params) {
        return "200 Posting allowed";
    }
}
