package org.jnode.nntp.model;

public class NntpResponse {

    public static final String END = ".";

    public static class Post {
        public static final String SEND_ARTICLE_TO_BE_POSTED = "340 Send article to be posted";
        public static final String POSTING_NOT_PERMITTED = "440 Posting not permitted";
        public static final String ARTICLE_RECEIVED_OK = "240 Article received OK";
        public static final String POSTING_FAILED = "441 Posting failed";
    }

    public static class AuthInfo {
        public static final String AUTHENTIFICATION_ACCEPTED = "281 Authentication accepted";
        public static final String PASSWORD_REQUIRED = "381 Password required";
        public static final String AUTHENTIFICATION_FAILED_OR_REJECTED = "481 Authentication failed/rejected";
        public static final String AUTHENTIFICATION_COMMAND_ISSUED_OUT_OF_SEQUENCE = "482 Authentication commands issued out of sequence";
        public static final String COMMAND_UNAVAILABLE = "502 Command unavailable";
    }

    public static class InitialGreetings {
        public static final String SERVICE_AVAILABLE_POSTING_ALLOWED = "200 Service available, posting allowed";
        public static final String SERVICE_AVAILABLE_POSTING_PROHIBITED = "201 Service available, posting prohibited";
    }

    public static class List {
        public static final String LIST_OF_NEWSGROUPS = "215 list of newsgroups follows";
        public static final String SYNTAX_ERROR = "501 Syntax Error";
    }

    public static class Group {
        /**
         * group     Name of newsgroup
         * number    Estimated number of articles in the group
         * low       Reported low water mark
         * high      Reported high water mark
         */
        public static final String GROUP_SUCCESSFULLY_SELECTED = "211 {number} {low} {high} {group} Group successfully selected";

        public static final String NO_SUCH_NEWSGROUP = "411 No such newsgroup";
    }

    public static class Head {
        public static final String HEADERS_FOLLOW = "221 {n} {message-id}";
        public static final String NO_NEWSGROUP_SELECTED = "412 No newsgroup selected";
        public static final String NO_ARTICLE_WITH_THAT_NUMBER =  "423 No article with that number";
        public static final String CURRENT_ARTICLE_NUMBER_IS_INVALID = "420 Current article number is invalid";
    }

    public static class ModeReader {
        public static final String POSTING_ALLOWED = "200 Posting allowed";
        public static final String POSTING_PROHIBITED = "201 Posting prohibited";
        public static final String READING_SERVICE_PERMANENTLY_UNAVAILABLE = "502 Reading service permanently unavailable";
    }

    public static class Article {

        /**
         * n             Returned article number
         * message-id    Article message-id
         */
        public static final String ARTICLE_FOLLOWS_1 = "220 0 {message-id} Article follows (multi-line)";

        /**
         * n             Returned article number
         * message-id    Article message-id
         */
        public static final String ARTICLE_FOLLOWS_2 = "220 {n} {message-id} Article follows (multi-line)";

        public static final String NO_NEWSGROUP_SELECTED = "412 No newsgroup selected";
        public static final String CURRENT_ARTICLE_NUMBER_IS_INVALID = "420 Current article number is invalid";
        public static final String NO_ARTICLE_WITH_THAT_NUMBER = "423 No article with that number";
        public static final String NO_ARTICLE_WITH_THAT_MESSAGE_ID = "430 No article with that message-id";
    }

    public static class Xover {
        public static final String OVERVIEW_INFORMATION_FOLLOWS = "224 Overview information follows";
        public static final String NO_NEWS_GROUP_CURRENT_SELECTED = "412 No news group current selected";
        public static final String NO_ARTICLE_SELECTED = "420 No article(s) selected";
        public static final String NO_PERMISSION = "502 no permission";
    }
}
