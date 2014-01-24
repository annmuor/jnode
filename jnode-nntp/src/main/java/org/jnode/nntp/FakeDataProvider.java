package org.jnode.nntp;

import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class FakeDataProvider implements DataProvider {

    @Override
    public Collection<NewsGroup> newsGroups() {
        NewsGroup ng = new NewsGroup();
        ng.setId(Integer.toString(1));
        ng.setName("name");

        return Arrays.asList(ng);
    }

    @Override
    public Collection<NewsMessage> messages(String newsGroupName) {
        return Arrays.asList(generateNewsMessage(newsGroupName));
    }

    private NewsMessage generateNewsMessage(String newsGroupName) {
        int r = Math.abs(new Random().nextInt());

        NewsMessage nm = new NewsMessage();
        nm.setNntpGroupCode(newsGroupName);
        nm.setBody("body:" + r);

        return nm;
    }

    private NewsGroup generateNewsGroup() {
        int r = Math.abs(new Random().nextInt());

        NewsGroup ng = new NewsGroup();
        ng.setId(Integer.toString(r));
        ng.setName("name:" + r);

        return ng;
    }
}
