package org.jnode.nntp;

import org.jnode.nntp.model.NewsGroup;
import org.jnode.nntp.model.NewsMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class FakeDataProvider implements DataProvider {

    @Override
    public Collection<NewsGroup> newsGroups() {
        return Arrays.asList(generateNewsGroup(), generateNewsGroup(), generateNewsGroup());
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
        ng.setName("code:" + r);

        return ng;
    }
}
