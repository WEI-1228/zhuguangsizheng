package com.iflytek.domain.ChannelNews;

import java.util.List;

//{
// "source":"中国新闻网",
// "channelId":"5572a109b3cdc86cf39001dc",
// "havePic":false,
// "channelName":"台湾最新",
// "desc":"　　中新社昆山3月5日电 题：台湾“老男孩”痴迷棒垒球 两岸家庭昆山带出“大山家族”",
// "link":"http://www.chinanews.com/tw/2021/03-05/9425091.shtml",
// "imageurls":[],
// "title":"台湾“老男孩”痴迷棒垒球 两岸家庭昆山带出“大山家族”",
// "pubDate":"2021-03-05 12:37:23",
// "id":"6e078ace52dec14c0e8baafc60275352"
// }
public class Content {
    String source;
    String channelId;
    boolean havePic;
    String channelName;
    String desc;
    String link;
    String title;
    String pubDate;
    List<NewsImages> imageurls;
    String id;
    String content;

    public List<NewsImages> getImageurls() {
        return imageurls;
    }

    public void setImageurls(List<NewsImages> imageurls) {
        this.imageurls = imageurls;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isHavePic() {
        return havePic;
    }

    public void setHavePic(boolean havePic) {
        this.havePic = havePic;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
