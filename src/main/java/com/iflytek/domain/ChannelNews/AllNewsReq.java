package com.iflytek.domain.ChannelNews;

public class AllNewsReq {
    private String showapi_res_error;
    private String showapi_res_id;
    private int showapi_res_code;
    private AllNewsBody showapi_res_body;

    public String getShowapi_res_error() {
        return showapi_res_error;
    }

    public void setShowapi_res_error(String showapi_res_error) {
        this.showapi_res_error = showapi_res_error;
    }

    public String getShowapi_res_id() {
        return showapi_res_id;
    }

    public void setShowapi_res_id(String showapi_res_id) {
        this.showapi_res_id = showapi_res_id;
    }

    public int getShowapi_res_code() {
        return showapi_res_code;
    }

    public void setShowapi_res_code(int showapi_res_code) {
        this.showapi_res_code = showapi_res_code;
    }

    public AllNewsBody getShowapi_res_body() {
        return showapi_res_body;
    }

    public void setShowapi_res_body(AllNewsBody showapi_res_body) {
        this.showapi_res_body = showapi_res_body;
    }
}
