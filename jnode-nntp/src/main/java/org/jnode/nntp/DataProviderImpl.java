package org.jnode.nntp;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import jnode.dao.GenericDAO;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.dto.EchomailAwaiting;
import jnode.dto.Entity;
import jnode.dto.Link;
import jnode.dto.Netmail;
import jnode.dto.Subscription;
import jnode.orm.ORMManager;
import org.jnode.nntp.model.Auth;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public class DataProviderImpl implements DataProvider {

    private GenericDAO<Echoarea> echoareaDAO = ORMManager.get(Echoarea.class);
    private GenericDAO<Echomail> echomailDao = ORMManager.get(Echomail.class);
    private GenericDAO<EchomailAwaiting> awaitingEchomailDao = ORMManager.get(EchomailAwaiting.class);
    private GenericDAO<Netmail> netmailDao = ORMManager.get(Netmail.class);
    private GenericDAO<Link> linkDao = ORMManager.get(Link.class);
    private GenericDAO<Subscription> subscriptionDAO = ORMManager.get(Subscription.class);

    @Override
    public Echoarea echoarea(String echoareaName) {
        return echoareaDAO.getFirstAnd("name", "=", echoareaName);
    }

    @Override
    public NewsGroup newsGroup(final String groupName, Auth auth) {
        if (Constants.NETMAIL_NEWSGROUP_NAME.equalsIgnoreCase(groupName)) {
            return netmail(auth);
        }

        Echoarea area = echoareaDAO.getFirstAnd("name", "=", groupName);
        if (area == null) {
            // area not found
            return null;
        }
        return convert(area, auth);
    }

    private NewsGroup convert(Echoarea area, Auth auth) {

        Collection<Entity> entities = retrieveEntities(area.getId(), auth);

        NewsGroup newsGroup = new NewsGroup();
        newsGroup.setId(area.getId());
        newsGroup.setName(area.getName());
        newsGroup.setReportedLowWatermark(countLowWatermark(entities));
        newsGroup.setReportedHighWatermark(countHighWatermark(entities));
        newsGroup.setNumberOfArticles(countArticles(entities));
        return newsGroup;
    }

    private Long countHighWatermark(Collection<Entity> entities) {

        long watermark = 0;

        for (Entity entity : entities) {
            if (entity.getId() > watermark) {
                watermark = entity.getId();
            }
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private long countLowWatermark(Collection<Entity> entities) {

        long watermark = 0;

        for (Entity entity : entities) {
            if (entity.getId() < watermark) {
                watermark = entity.getId();
            }
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private Collection<Entity> retrieveEntities(Long areaId, Auth auth) {
        Collection<Entity> entities = Lists.newArrayList();

        if (Constants.NETMAIL_NEWSGROUP_ID.equals(areaId)) {
            entities.addAll(netmailDao.getOr(
                    "to_address", "=", auth.getFtnAddress(),
                    "from_address", "=", auth.getFtnAddress())
            );
        } else {
            entities.addAll(echomailDao.getAnd("echoarea_id", "=", areaId));
        }
        return entities;
    }

    private int countArticles(Collection<Entity> entities) {
        return entities.size();
    }

    @Override
    public Collection<NewsGroup> newsGroups(final Auth auth) {

        Collection<Subscription> subscriptions = subscriptionDAO.getAnd("link_id", "=", auth.getLinkId());
        Collection<Echoarea> echoareas = Lists.newArrayList();
        for (Subscription subscription : subscriptions) {
            echoareas.addAll(echoareaDAO.getAnd("id", "=", subscription.getArea().getId()));
        }
        return Collections2.transform(echoareas, new Function<Echoarea, NewsGroup>() {
            @Override
            public NewsGroup apply(Echoarea input) {
                return convert(input, auth);
            }
        });
    }

    private NewsMessage convert(Echomail echomail) {
        NewsMessage newsMessage = new NewsMessage();

        newsMessage.setId(echomail.getId());
        newsMessage.setPath(echomail.getPath());
        newsMessage.setGroupName(echomail.getArea().getName());
        newsMessage.setFrom(echomail.getFromName() + " " + echomail.getFromFTN());
        newsMessage.setSubject(echomail.getSubject());
        newsMessage.setCreatedDate(echomail.getDate());
        newsMessage.setBody(echomail.getText());
        newsMessage.setMessageId(echomail.getMsgid());

        return newsMessage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<NewsMessage> messagesByIdRange(String fromId, String toId, final long groupId, Auth auth) {

        // -1 because id was incremented during watermark counting
        long trueFromId = Long.valueOf(fromId) - 1;
        long trueToId = Long.valueOf(toId) - 1;

        if (Constants.NETMAIL_NEWSGROUP_ID.equals(groupId)) {
            return Collections2.transform(netmailDao.getOr(
                    "to_address", "=", auth.getFtnAddress(),
                    "from_address", "=", auth.getFtnAddress()), new Function<Netmail, NewsMessage>() {
                @Override
                public NewsMessage apply(Netmail input) {
                    return convert(input);
                }
            });

        } else {
            return Collections2.transform(echomailDao.getAnd(
                    "echoarea_id", "=", groupId,
                    "id", ">=", trueFromId,
                    "id", "<=", trueToId), new Function<Echomail, NewsMessage>() {
                @Override
                public NewsMessage apply(Echomail input) {
                    return convert(input);
                }
            });
        }
    }

    private NewsMessage convert(Netmail netmail) {
        NewsMessage newsMessage = new NewsMessage();

        newsMessage.setId(netmail.getId());
        newsMessage.setGroupName(Constants.NETMAIL_NEWSGROUP_NAME);
        newsMessage.setFrom(netmail.getFromName() + " " + netmail.getFromFTN());
        newsMessage.setSubject(netmail.getSubject());
        newsMessage.setCreatedDate(netmail.getDate());
        newsMessage.setBody(netmail.getText());

        return newsMessage;
    }

    @Override
    public NewsMessage messageById(String id, Long groupId) {
        if (Constants.NETMAIL_NEWSGROUP_ID.equals(groupId)) {
            return convert(netmailDao.getById(id));
        } else {
            return convert(echomailDao.getById(id));
        }
    }

    @Override
    public NewsMessage messageByMessageId(String messageId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NewsGroup netmail(Auth auth) {
        Collection<Entity> entities = retrieveEntities(Constants.NETMAIL_NEWSGROUP_ID, auth);

        NewsGroup newsGroup = new NewsGroup();
        newsGroup.setId(Constants.NETMAIL_NEWSGROUP_ID);
        newsGroup.setName(Constants.NETMAIL_NEWSGROUP_NAME);
        newsGroup.setNumberOfArticles(countArticles(entities));
        newsGroup.setReportedLowWatermark(countLowWatermark(entities));
        newsGroup.setReportedHighWatermark(countHighWatermark(entities));
        return newsGroup;
    }

    @Override
    public Link link(Auth auth, String pass) {
        return linkDao.getFirstAnd(
                "ftn_address", "=", auth.getFtnAddress(),
                "password", "=", pass);
    }

    @Override
    public void post(Netmail netmail) {
        netmailDao.save(netmail);
    }

    @Override
    public void post(Auth auth, Echomail echomail) {
        echomailDao.save(echomail);
        for (Subscription s : ORMManager.get(Subscription.class).getAnd(
                "echoarea_id", "=", echomail.getArea())) {
            ORMManager.get(EchomailAwaiting.class).save(new EchomailAwaiting(s.getLink(), echomail));
        }
    }
}
