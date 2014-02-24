package org.jnode.nntp.processor;

import com.google.common.collect.Lists;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.Mail;
import jnode.dto.Netmail;
import jnode.event.Notifier;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import org.apache.commons.lang.StringUtils;
import org.jnode.nntp.Constants;
import org.jnode.nntp.DataProvider;
import org.jnode.nntp.DataProviderImpl;
import org.jnode.nntp.Processor;
import org.jnode.nntp.event.PostEndEvent;
import org.jnode.nntp.event.PostStartEvent;
import org.jnode.nntp.exception.NntpException;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NntpResponse;
import org.jnode.nntp.util.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class PostProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(PostProcessor.class);

    private DataProvider dataProvider = new DataProviderImpl();

    @Override
    public Collection<String> process(Collection<String> params, Long selectedGroupId, Long selectedArticleId, Auth auth) {

        Collection<String> response = Lists.newLinkedList();

        if (params.isEmpty()) {
            // start posting
            Notifier.INSTANSE.notify(new PostStartEvent());
            response.add(NntpResponse.Post.SEND_ARTICLE_TO_BE_POSTED);
        } else {
            // end posting
            Notifier.INSTANSE.notify(new PostEndEvent());
            response.add(NntpResponse.Post.ARTICLE_RECEIVED_OK);

            if (isNetmail(params)) {
                try {
                    Netmail netmail = convertToNetmail(params);
                    // todo validate
                    dataProvider.post(netmail);
                } catch (NntpException e) {
                    logger.l1("Can't save netmail.", e);
                }
            } else {
                try {
                    Echomail echomail = convertToEchomail(params);
                    // todo validate
                    dataProvider.post(auth, echomail);
                } catch (NntpException e) {
                    logger.l1("Can't save echomail.", e);
                }
            }
        }

        return response;
    }

    private Echomail convertToEchomail(Collection<String> params) {
        Echomail echomail = new Echomail();
        convertToMail(echomail, params);
        echomail.setArea(findEchoarea(params));
        echomail.setToName(findTo(params));
        echomail.setMsgid(echomail.getFromFTN() + " " + FtnTools.generate8d());
        return echomail;
    }

    private String findTo(Collection<String> params) {

        String to = null;
        for (String param : params) {
            if (StringUtils.startsWithIgnoreCase(param, Constants.TO)) {
                // +1 becaus of ":"
                to = StringUtils.trim(StringUtils.substring(param, Constants.TO.length() + 1));
            }
        }

        return to == null ? "All" : to;
    }

    private Echoarea findEchoarea(Collection<String> params) {
        Echoarea echoarea = null;
        String echoareaName = null;

        for (String param : params) {
            if (StringUtils.startsWithIgnoreCase(param, Constants.NEWSGROUPS)) {
                // +1 becaus of ":"
                echoareaName = StringUtils.trim(StringUtils.substring(param, Constants.NEWSGROUPS.length() + 1));
                echoarea = dataProvider.echoarea(echoareaName);
            }
        }

        if (echoarea == null) {
            if (StringUtils.isEmpty(echoareaName)) {
                logger.l1("Echoarea is empty.");
            } else {
                logger.l1("Can't find echoarea by name: " + echoareaName + ".");
            }

            throw new NntpException();
        }

        return echoarea;
    }

    private Mail convertToMail(Mail mail, Collection<String> params) {
        boolean isBody = false;
        StringBuilder message = new StringBuilder();
        for (String param : params) {
            if (StringUtils.startsWithIgnoreCase(param, Constants.FROM)) {
                String from = StringUtils.trim(StringUtils.substring(param, Constants.FROM.length() + 1));

                int ind1 = StringUtils.indexOf(from, "<");
                int ind2 = StringUtils.indexOf(from, ">");

                if (ind1 == -1 || ind2 == -1) {
                    logger.l1("Incorrect 'from' line: " + from + ".");
                    continue;
                }

                String name = StringUtils.substring(from, 0, ind1);
                String email = StringUtils.substring(from, ind1 + 1, ind2);

                mail.setFromName(name);
                mail.setFromFTN(Converter.convertEmailToFtn(email));

                continue;
            }
            if (StringUtils.startsWithIgnoreCase(param, Constants.ORGANIZATION)) {
                // ignore
                continue;
            }
            if (StringUtils.startsWithIgnoreCase(param, Constants.SUBJECT)) {
                String subject = StringUtils.trim(StringUtils.substring(param, Constants.SUBJECT.length() + 1));
                mail.setSubject(subject);
                continue;
            }
            if (StringUtils.trim(param).equalsIgnoreCase(StringUtils.EMPTY) && !isBody) {
                isBody = true;
                continue;
            }
            if (StringUtils.startsWithIgnoreCase(param, Constants.DATE)) {
                // +1 because of ":"
                String date = StringUtils.trim(StringUtils.substring(param, Constants.DATE.length() + 1));
                SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                try {
                    mail.setDate(f.parse(date));
                } catch (ParseException e) {
                    logger.l1("Can't parse date: " + date + ".");
                }
                continue;
            }

            if (isBody) {
                message.append(param).append("\n");
                continue;
            }

            logger.l4("Unknown message line: " + param + ".");
        }
        mail.setText(message.toString());
        return mail;

    }

    private Netmail convertToNetmail(Collection<String> params) {
        Netmail netmail = new Netmail();
        convertToMail(netmail, params);
        findTo(netmail, params);
        return netmail;
    }

    private void findTo(Netmail netmail, Collection<String> params) {

        // todo refactor
        for (String param : params) {
            if (StringUtils.startsWithIgnoreCase(param, Constants.TO)) {
                // +1 becaus of ":"
                String to = StringUtils.trim(StringUtils.substring(param, Constants.TO.length() + 1));

                int ind1 = StringUtils.indexOf(to, "<");
                int ind2 = StringUtils.indexOf(to, ">");

                if (ind1 == -1 || ind2 == -1) {
                    logger.l1("Incorrect 'to' line: " + to + ".");
                    continue;
                }

                String name = StringUtils.substring(to, 0, ind1);
                String email = StringUtils.substring(to, ind1 + 1, ind2);

                netmail.setToName(name);
                netmail.setToFTN(Converter.convertEmailToFtn(email));
            }
        }
    }

    // TODO refactor
    private boolean isNetmail(Collection<String> params) {
        for (String param : params) {
            if (StringUtils.startsWithIgnoreCase(param, "newsgroups:")) {
                String groupName = StringUtils.trim(StringUtils.substring(param, "newsgroups:".length()));
                return Constants.NETMAIL_NEWSGROUP_NAME.equalsIgnoreCase(groupName);
            }
        }
        return false;
    }
}
