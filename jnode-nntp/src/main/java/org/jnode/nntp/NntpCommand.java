package org.jnode.nntp;

public enum NntpCommand {
    ARTICLE,
    BODY,
    CAPABILITIES,
    DATE,
    GROUP,
    HDR,
    HEAD,
    HELP,
    IHAVE,
    LAST,
    LIST,
    ACTIVE_TIMES,
    LIST_ACTIVE,
    LIST_DISTRIB_PATS,
    LIST_HEADERS,
    LIST_NEWSGROUPS,
    LIST_OVERVIEW_FMT,
    LISTGROUP,
    MODE_READER("MODE READER"),
    NEWGROUPS,
    NEWNEWS,
    NEXT,
    OVER,
    POST,
    QUIT,
    STAT;

    private String command;

    NntpCommand(String command) {
        this.command = command;
    }

    NntpCommand() {
    }

    public String getCommand() {
        return command;
    }

    public static NntpCommand find(String command) {
        for (NntpCommand nntpCommand : NntpCommand.values()) {
             if (command.equalsIgnoreCase(nntpCommand.getCommand())) {
                  return nntpCommand;
             }
        }
        return null;
    }

}
