package com.iflytek.domain.ChannelNews;

import java.util.List;

public class PageBean {
    int allNum;
    List<Content> contentlist;
    int maxResult;
    int currentPage;
    int allPages;

    public int getAllNum() {
        return allNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public List<Content> getContentlist() {
        return contentlist;
    }

    public void setContentlist(List<Content> contentlist) {
        this.contentlist = contentlist;
    }

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getAllPages() {
        return allPages;
    }

    public void setAllPages(int allPages) {
        this.allPages = allPages;
    }
}
