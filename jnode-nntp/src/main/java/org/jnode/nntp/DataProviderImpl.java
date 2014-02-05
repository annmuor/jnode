package org.jnode.nntp;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import jnode.dao.GenericDAO;
import jnode.dto.Echoarea;
import jnode.dto.Echomail;
import jnode.orm.ORMManager;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public class DataProviderImpl implements DataProvider {
    private GenericDAO<Echoarea> echoareaDAO = ORMManager.get(Echoarea.class);
    private GenericDAO<Echomail> echomailDao = ORMManager.get(Echomail.class);

    @Override
    public NewsGroup newsGroup(final String groupName) {
        Echoarea area = echoareaDAO.getFirstAnd("name", "=", groupName);
        if (area == null) {
            // area not found
            return null;
        }
        return convert(area);
    }

    private NewsGroup convert(Echoarea area) {
        NewsGroup newsGroup = new NewsGroup();
        newsGroup.setId(area.getId());
        newsGroup.setName(area.getName());
        newsGroup.setReportedLowWatermark(countLowWatermark(area.getId()));
        newsGroup.setReportedHighWatermark(countHighWatermark(area.getId()));
        newsGroup.setNumberOfArticles((long) countArticles(area.getId()));
        return newsGroup;
    }

    private Long countHighWatermark(Long areaId) {
        long watermark = 0;

        for (Echomail echomail : echomailDao.getAnd("echoarea_id", "=", areaId)) {
                watermark = echomail.getId();
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private long countLowWatermark(Long areaId) {

        long watermark = 0;

        for (Echomail echomail : echomailDao.getAnd("echoarea_id", "=", areaId)) {
                watermark = echomail.getId();
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private int countArticles(final Long areaId) {
        return echomailDao.getAnd("echoarea_id", "=", areaId).size();
    }

    @Override
    public Collection<NewsGroup> newsGroups() {
        return Collections2.transform(echoareaDAO.getAll(), new Function<Echoarea, NewsGroup>() {
            @Override
            public NewsGroup apply(Echoarea input) {
                return convert(input);
            }
        });
    }

    @Override
    public Collection<NewsMessage> messagesByGroupName(final String groupName) {
        Echoarea echoarea = echoareaDAO.getFirstAnd("name", "=", groupName);
        return Collections2.transform(echomailDao.getAnd("echoarea_id", "=", echoarea.getId()), new Function<Echomail, NewsMessage>() {
            @Override
            public NewsMessage apply(Echomail input) {
                return convert(input);
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
    public Collection<NewsMessage> messagesByIdRange(String fromId, String toId, final long groupId) {
        // -1 because id was incremented during watermark counting
        final Range range = Range.closed(Long.valueOf(fromId) - 1, Long.valueOf(toId) - 1);
        return Collections2.transform(Collections2.filter(echomailDao.getAnd("echoarea_id", "=", groupId), new Predicate<Echomail>() {
            @Override
            public boolean apply(Echomail input) {
                return range.contains(input.getId());
            }
        }), new Function<Echomail, NewsMessage>() {
            @Override
            public NewsMessage apply(Echomail input) {
                return convert(input);
            }
        });
    }

    @Override
    public NewsMessage messageById(String id) {
        return convert(echomailDao.getById(id));
    }

    @Override
    public NewsMessage messageByMessageId(String messageId) {
        throw new UnsupportedOperationException();
    }
}
