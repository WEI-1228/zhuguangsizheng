package com.iflytek.domain.adapter;

public class News {
    String title;
    String date;
    String source;
    String imageUrl;
    Boolean havePic;
    String contentUrl;
    String content;
    String id;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public News(){

    }

    public News(String title, String date, String source, String imageUrl, Boolean havePic, String content) {
        this.title = title;
        this.date = date;
        this.source = source;
        this.imageUrl = imageUrl;
        this.havePic = havePic;
        this.contentUrl = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setHavePic(Boolean havePic) {
        this.havePic = havePic;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getHavePic() {
        return havePic;
    }

    public String getContentUrl() {
        return contentUrl;
    }
}
