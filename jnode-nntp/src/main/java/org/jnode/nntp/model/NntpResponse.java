package org.jnode.nntp.model;

public class NntpResponse {

    public static final String END_OF_RESPONSE = ".";

    public static class InitialGreetings {
        public static final String READY = "200 NNTP Service Ready, posting permitted.";
    }

    public static class List {
        public static final String LIST_OF_NEWSGROUPS = "215 list of newsgroups follows";
        public static final String SYNTAX_ERROR = "501 Syntax Error";
    }

    public static class Group {
        public static final String GROUP_SUCCESSFULLY_SELECTED = "211";
        public static final String NO_SUCH_NEWSGROUP = "411 No such newsgroup";
    }

    public static class Head {
        public static final String HEADERS_FOLLOW = " 221 {message-number} {message-id}";

    }

    public static class Article {
        public static final String OK = "220";
    }
}
