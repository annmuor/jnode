package org.jnode.nntp;

import jnode.dao.GenericDAO;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;
import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

public class DataProviderServiceTest {

    NewsGroup ng1;
    NewsGroup ng2;
    NewsGroup ng3;

    @Test
    public void prepare() throws JnodeModuleException {
        NntpModule module = new NntpModule("/tmp/bla.properties");

        prepareNewsGroups();
        prepareNewsMessages();
    }

    public void prepareNewsMessages() {
        GenericDAO<NewsMessage> dao = ORMManager.get(NewsMessage.class);
        dao.executeRaw("DELETE FROM news_message");

        generateMessages(dao, ng1);
        generateMessages(dao, ng2);
        generateMessages(dao, ng3);
    }

    private void generateMessages(GenericDAO<NewsMessage> dao, NewsGroup ng) {
        for (long i = ng.getReportedLowWatermark(); i <= ng.getReportedHighWatermark(); i++) {
            int userId = random2();

            NewsMessage message = new NewsMessage();
            message.setId(i);
            message.setGroupName(ng.getName());
            message.setFrom("User" + userId + " <user" + userId + "@xxx.com>");
            message.setSubject("Subject " + random());
            message.setBody("body " + random());
            message.setCreatedDate(new Date());
            dao.save(message);
        }
    }

    public void prepareNewsGroups() {
/*

        GenericDAO<NewsGroup> dao = ORMManager.get(NewsGroup.class);
        dao.executeRaw("DELETE FROM news_group WHERE name LIKE 'group%'");

        ng1 = new NewsGroup();
        ng1.setGroupUniquePrefix(random());
        ng1.setName("group1");
        ng1.setNumberOfArticles(1l);
        ng1.setReportedLowWatermark(ng1.getGroupUniquePrefix() + 100l);
        ng1.setReportedHighWatermark(ng1.getGroupUniquePrefix() + 100l);
        ng1.setCreatedDate(new Date());
        dao.save(ng1);

        ng2 = new NewsGroup();
        ng2.setGroupUniquePrefix(random());
        ng2.setName("group2");
        ng2.setNumberOfArticles(2l);
        ng2.setReportedLowWatermark(ng2.getGroupUniquePrefix() + 200l);
        ng2.setReportedHighWatermark(ng2.getGroupUniquePrefix() + 201l);
        ng2.setCreatedDate(new Date());
        dao.save(ng2);

        ng3 = new NewsGroup();
        ng3.setGroupUniquePrefix(random());
        ng3.setName("group3");
        ng3.setNumberOfArticles(3l);
        ng3.setReportedLowWatermark(ng3.getGroupUniquePrefix() + 300l);
        ng3.setReportedHighWatermark(ng3.getGroupUniquePrefix() + 302l);
        ng3.setCreatedDate(new Date());
        dao.save(ng3);

*/
    }

    private int random2() {
        return Math.abs(new Random().nextInt(2));
    }

    private int random() {
        return Math.abs(new Random().nextInt());
    }

}
