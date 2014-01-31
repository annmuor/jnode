package org.jnode.nntp;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Range;
import jnode.dao.GenericDAO;
import jnode.orm.ORMManager;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Collection;

public class DataProviderImpl implements DataProvider {
    private GenericDAO<NewsGroup> newsGroupDao = ORMManager.get(NewsGroup.class);
    private GenericDAO<NewsMessage> newsMessageDao = ORMManager.get(NewsMessage.class);

    @Override
    public NewsGroup newsGroup(final String groupName) {
        return Collections2.filter(newsGroupDao.getAll(), new Predicate<NewsGroup>() {
            @Override
            public boolean apply(NewsGroup input) {
                return input.getName().equalsIgnoreCase(groupName);
            }
        }).iterator().next();
    }

    @Override
    public Collection<NewsGroup> newsGroups() {
        return newsGroupDao.getAll();
    }

    @Override
    public Collection<NewsMessage> messagesByGroupName(final String groupName) {
        return Collections2.filter(newsMessageDao.getAll(), new Predicate<NewsMessage>() {
            @Override
            public boolean apply(NewsMessage input) {
                return input.getGroupName().equalsIgnoreCase(groupName);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<NewsMessage> messagesByIdRange(String fromId, String toId) {
        final Range range = Range.closed(Long.valueOf(fromId), Long.valueOf(toId));
        return Collections2.filter(newsMessageDao.getAll(), new Predicate<NewsMessage>() {
            @Override
            public boolean apply(NewsMessage input) {
                return range.contains(input.getId());
            }
        });
    }

    @Override
    public NewsMessage messageById(String id) {
        return newsMessageDao.getById(id);
    }
}
