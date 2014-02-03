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
        // todo use query
        Echoarea area = Collections2.filter(echoareaDAO.getAll(), new Predicate<Echoarea>() {
            @Override
            public boolean apply(Echoarea input) {
                return input.getName().equalsIgnoreCase(groupName);
            }
        }).iterator().next();
        return convert(area);
    }

    private NewsGroup convert(Echoarea area) {
        NewsGroup newsGroup = new NewsGroup();
        newsGroup.setId(area.getId());
        newsGroup.setName(area.getName());
        newsGroup.setReportedLowWatermark(countLowWatermark(area.getId()));
        newsGroup.setReportedHighWatermark(countHighWatermark(area.getId()));
        newsGroup.setNumberOfArticles(newsGroup.getReportedHighWatermark() - newsGroup.getReportedLowWatermark());


        return newsGroup;
    }

    private Long countHighWatermark(Long areaId) {

        long watermark = 0;

        // todo use query
        for (Echomail echomail : echomailDao.getAll()) {
            if (echomail.getArea().getId().equals(areaId) && echomail.getId() > watermark) {
                watermark = echomail.getId();
            }
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private long countLowWatermark(Long areaId) {

        long watermark = 0;

        // todo use query
        for (Echomail echomail : echomailDao.getAll()) {
            if (echomail.getArea().getId().equals(areaId) && echomail.getId() < watermark) {
                watermark = echomail.getId();
            }
        }

        // +1 because client didn't recognize id 0
        return watermark + 1;
    }

    private int countArticles(final Long areaId) {
        // todo use query
        return Collections2.filter(echomailDao.getAll(), new Predicate<Echomail>() {
            @Override
            public boolean apply(Echomail input) {
                return input.getArea().getId().equals(areaId);
            }
        }).size();
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
        return Collections2.transform(Collections2.filter(echomailDao.getAll(), new Predicate<Echomail>() {
            @Override
            public boolean apply(Echomail input) {
                return input.getArea().getName().equalsIgnoreCase(groupName);
            }
        }), new Function<Echomail, NewsMessage>() {
            @Override
            public NewsMessage apply(Echomail input) {
                return convert(input);
            }
        });
    }

    private NewsMessage convert(Echomail echomail) {
        NewsMessage newsMessage = new NewsMessage();

        newsMessage.setId(echomail.getId());
        newsMessage.setGroupName(echomail.getArea().getName());
        newsMessage.setFrom(echomail.getFromName() + " " + echomail.getFromFTN());
        newsMessage.setSubject(echomail.getSubject());
        newsMessage.setCreatedDate(echomail.getDate());
        newsMessage.setBody(echomail.getText());

        return newsMessage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<NewsMessage> messagesByIdRange(String fromId, String toId, final long groupId) {
        // -1 because id was incremented during watermark counting
        final Range range = Range.closed(Long.valueOf(fromId) - 1, Long.valueOf(toId) - 1);
        return Collections2.transform(Collections2.filter(echomailDao.getAll(), new Predicate<Echomail>() {
            @Override
            public boolean apply(Echomail input) {
                return input.getArea().getId().equals(groupId) && range.contains(input.getId());
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
}
