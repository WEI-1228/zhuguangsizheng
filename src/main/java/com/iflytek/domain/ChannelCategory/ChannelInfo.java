package com.iflytek.domain.ChannelCategory;

import java.util.List;

public class ChannelInfo {
    private int ret_code;
    private List<Channel> channelList;

    public int getRet_code() {
        return ret_code;
    }

    public void setRet_code(int ret_code) {
        this.ret_code = ret_code;
    }

    public List<Channel> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<Channel> channelList) {
        this.channelList = channelList;
    }
}
