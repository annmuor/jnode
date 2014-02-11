package org.jnode.nntp.model;

import java.util.Collection;

public enum NntpCommand {
    ARTICLE("ARTICLE"),
    AUTHINFO_USER("AUTHINFO user"),
    AUTHINFO_PASS("AUTHINFO pass"),
    BODY,
    CAPABILITIES,
    DATE,
    GROUP("GROUP"),
    HDR,
    HEAD("HEAD"),
    HELP,
    IHAVE,
    LAST,
    LIST("LIST"),
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
    XOVER("XOVER"),
    POST("POST"),
    QUIT("QUIT"),
    STAT;

    private String command;
    private Collection<String> params;

    NntpCommand(String command) {
        this.command = command;
    }

    NntpCommand() {
    }

    public String getCommand() {
        return command;
    }

    public Collection<String> getParams() {
        return params;
    }

    public void setParams(Collection<String> params) {
        this.params = params;
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
