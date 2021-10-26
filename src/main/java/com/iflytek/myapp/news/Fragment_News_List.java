package com.iflytek.myapp.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.domain.ChannelNews.AllNewsReq;
import com.iflytek.domain.ChannelNews.Content;
import com.iflytek.domain.adapter.News;
import com.iflytek.domain.adapter.NewsAdapter;
import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseFragment;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.notebook.NoteBookActivity;
import com.iflytek.myapp.radio.RadioActivity;
import com.iflytek.myapp.study.Main_Study_Activity;
import com.show.api.ShowApiRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_News_List extends BaseFragment implements AdapterView.OnItemClickListener{
    String[] urls = {
            "http://jhsjk.people.cn/result?form=702&else=501",  //"重要活动"
            "http://jhsjk.people.cn/result?form=701&else=501",  //"重要会议"
            "http://jhsjk.people.cn/result?form=703&else=501",  //"重要考察"
    };
    List<News> newsList;
    private Map<String, String> channelId;
    LinearLayout layout;
    ListView listView;
    String channel;
    EditText editText;
    LinearLayout linearLayout;
    int curItem = -1;
    final int MAX_NEWS = 15;
    boolean isReading = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String city;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channel = getArguments().getString("channel");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_items, container, false);
        init();
        initNews();
        return layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            READ_MODE = ARTICLE_MODE;
            if (channel.equals("#学习")) {
                readText("学习推荐");
            } else if (channel.equals("#" + city)) {
                readText(city);
            } else {
                readText(channel);
            }
        } else {
            stopRead();
        }
    }

    private void initNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Content> contentlist;
                if (channel.equals("#学习")) {
                    contentlist = new ArrayList<>();
                    List<Content> list = new ArrayList<>();
                    for (String url : urls) {
                        Document document;
                        try {
                            document = Jsoup.connect(url).get();
                            Element ul = document.getElementsByClass("list_14 p1_2 clearfix").get(0);
                            Elements eleList = ul.getElementsByTag("li");
                            int cnt = 0;
                            for (Element elem : eleList) {
                                if (cnt >= MAX_NEWS + MAX_NEWS / 2) break;
                                cnt++;
                                Content content = new Content();
                                String text = elem.text();
                                text = text.substring(text.indexOf("[") + 1, text.length() - 1);
                                content.setHavePic(false);
                                content.setPubDate(text.split(" ")[0]);
                                content.setSource(text.split(" ")[1]);
                                content.setTitle(elem.getElementsByTag("a").get(0).text());
                                content.setLink("http://jhsjk.people.cn/" + elem.getElementsByTag("a").get(0).attr("href"));
                                list.add(content);
                            }
                            Random random = new Random(System.currentTimeMillis());
                            for (int j = 0; j < MAX_NEWS; j++) {
                                contentlist.add(list.get(random.nextInt(list.size())));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (channel.equals("推荐")) {
                    contentlist = new ArrayList<>();
                    double sum = 0;
                    int[] num = new int[5];
                    int i = 0;
                    for (String key : channelId.keySet()) {
                        int anInt = sharedPreferences.getInt(key, 1);
                        sum += anInt;
                        num[i++] = anInt;
                    }
                    for (int j = 0; j < 5; j++) {
                        num[j] = (int) Math.max(1, MAX_NEWS * num[j] / sum);
                    }
                    i = 0;
                    for (String key : channelId.keySet()) {
                        String result = new ShowApiRequest("http://route.showapi.com/109-35", getString(R.string.news_username), getString(R.string.news_password))
                                .addTextPara("channelId", channelId.get(key))
                                .addTextPara("page", "1")
                                .addTextPara("needContent", "0")
                                .addTextPara("needHtml", "0")
                                .addTextPara("needAllList", "0")
                                .addTextPara("maxResult", String.valueOf(num[i++]))
                                .post();
                        AllNewsReq allNewsReq = JSON.parseObject(result, AllNewsReq.class);
                        List<Content> list = allNewsReq.getShowapi_res_body().getPagebean().getContentlist();
                        contentlist.addAll(list);
                    }
                    Collections.shuffle(contentlist);
                } else {
                    String result;
                    if (channel.startsWith("#")) {
                        result = new ShowApiRequest("http://route.showapi.com/170-47", getString(R.string.news_username), getString(R.string.news_password))
                                .addTextPara("areaId", "")
                                .addTextPara("areaName", channel.substring(1))
                                .addTextPara("title", "")
                                .addTextPara("page", "1")
                                .post();
                    } else {
                        result = new ShowApiRequest("http://route.showapi.com/109-35", getString(R.string.news_username), getString(R.string.news_password))
                                .addTextPara("channelId", channelId.get(channel))
                                .addTextPara("page", "1")
                                .addTextPara("needContent", "0")
                                .addTextPara("needHtml", "0")
                                .addTextPara("needAllList", "0")
                                .addTextPara("maxResult", String.valueOf(MAX_NEWS))
                                .post();
                    }
                    AllNewsReq allNewsReq = JSON.parseObject(result, AllNewsReq.class);
                    contentlist = allNewsReq.getShowapi_res_body().getPagebean().getContentlist();
                }
                newsList = new ArrayList<>(MAX_NEWS * 3);
                for (Content content : contentlist) {
                    News news = new News();
                    news.setHavePic(content.isHavePic());
                    news.setTitle(content.getTitle());
                    news.setContentUrl(content.getLink());
                    news.setSource(content.getSource());
                    news.setId(content.getId());
                    if (news.getHavePic()) {
                        news.setImageUrl(content.getImageurls().get(0).getUrl());
                    }
                    newsList.add(news);
                    news.setDate(content.getPubDate().split(" ")[0]);
                }
                final NewsAdapter adapter = new NewsAdapter(context, R.layout.news_type1, R.layout.news_type2, newsList);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                        listView.setOnTouchListener(Fragment_News_List.this);
                    }
                });
            }
        }).start();
    }

    public void onLongPressed() {
        stopRead();
        isReading = false;
        playSound(R.raw.toggle);
        getSpeechRecognizer();
    }

    public void onPressed() {
        if (curItem < 0) return;
        isReading = !isReading;
        READ_MODE = NEWS_MODE;
        if (newsList.get(curItem).getContent() == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readNews();
                }
            }).start();
        } else {
            if (!isReading) stopRead();
            else {
                readText(newsList.get(curItem).getContent());
                if (!channel.startsWith("#")) {
                    editor.putInt(channel, sharedPreferences.getInt(channel, 1) + 1);
                    editor.apply();
                }
            }
        }
        int visiblePosition = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(curItem - visiblePosition);
        listView.getAdapter().getView(curItem, view, listView);
        ImageView imageView = view.findViewById(R.id.laba);
        imageView.setVisibility(View.VISIBLE);
    }

    public void readNews() {
        String result;
        if (channel.startsWith("#")) {
            result = new ShowApiRequest("http://route.showapi.com/883-1", getString(R.string.news_username), getString(R.string.news_password))
                    .addTextPara("url", newsList.get(curItem).getContentUrl())
                    .addTextPara("needHtml", "0")
                    .addTextPara("needContent", "0")
                    .addTextPara("needAll_list", "1")
                    .post();
            JSONObject parse = (JSONObject) JSON.parse(result);
            JSONObject showapi_res_body = (JSONObject) parse.get("showapi_res_body");
            JSONArray all_list = showapi_res_body.getJSONArray("all_list");
            StringBuilder content = new StringBuilder();
            for (Object o : all_list) {
                if (o.toString().startsWith("{")) continue;
                content.append(o).append("\n");
            }
            newsList.get(curItem).setContent(content.toString());
            readText(newsList.get(curItem).getContent());
        } else {
            result = new ShowApiRequest("http://route.showapi.com/109-35", getString(R.string.news_username), getString(R.string.news_password))
                    .addTextPara("needContent", "1")
                    .addTextPara("needHtml", "0")
                    .addTextPara("id", newsList.get(curItem).getId())
                    .post();
            AllNewsReq allNewsReq = JSON.parseObject(result, AllNewsReq.class);
            Content content_ = allNewsReq.getShowapi_res_body().getPagebean().getContentlist().get(0);
            newsList.get(curItem).setContent(content_.getContent());
            readText(newsList.get(curItem).getContent());
            editor.putInt(channel, sharedPreferences.getInt(channel, 1) + 1);
            editor.apply();
        }
    }


    public void getBeforeItem() {
        if (curItem <= 0) return;
        isReading = false;
        stopRead();
        curItem--;
        listView.performItemClick(listView.getChildAt(curItem), curItem, listView.getItemIdAtPosition(curItem));
        if (curItem < MAX_NEWS - 1) {
            int visiblePosition = listView.getFirstVisiblePosition();
            View view = listView.getChildAt(curItem + 1 - visiblePosition);
            listView.getAdapter().getView(curItem + 1, view, listView);
            ImageView imageView = view.findViewById(R.id.laba);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    public void getNextItem() {
        if (curItem == MAX_NEWS - 1) return;
        isReading = false;
        stopRead();
        curItem++;
        listView.performItemClick(listView.getChildAt(curItem), curItem, listView.getItemIdAtPosition(curItem));
        if (curItem > 0) {
            int visiblePosition = listView.getFirstVisiblePosition();
            View view = listView.getChildAt(curItem - 1 - visiblePosition);
            listView.getAdapter().getView(curItem - 1, view, listView);
            ImageView imageView = view.findViewById(R.id.laba);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    private void init() {
        sharedPreferences = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences shared = activity.getSharedPreferences("message", MODE_PRIVATE);
        city = shared.getString("province", "安徽");
        editor = sharedPreferences.edit();
        listView = layout.findViewById(R.id.news_list_view);
        listView.setOnItemClickListener(this);
        channelId = new HashMap<>();
        channelId.put("时政焦点", "5572a108b3cdc86cf39001cd");
        channelId.put("科技焦点", "5572a108b3cdc86cf39001d9");
        channelId.put("军事焦点", "5572a108b3cdc86cf39001cf");
        channelId.put("国际焦点", "5572a108b3cdc86cf39001ce");
        channelId.put("港澳台最新", "5572a109b3cdc86cf39001dd");
        linearLayout = activity.findViewById(R.id.new_include);
        editText = linearLayout.findViewById(R.id.all_top_edit);
    }


    public static Fragment_News_List newInstance(String channel) {
        Fragment_News_List fragment = new Fragment_News_List();
        Bundle bundle = new Bundle();
        bundle.putString("channel", channel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void speechCallBack() {
        editText.setText(SPEECH_RESULT);
        editText.setSelection(SPEECH_RESULT.length());
        processJump(SPEECH_RESULT);

    }

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            if (!channel.equals("#学习"))
                readText("您已经在看新闻");
            else startActivity(new Intent(context, Main_News_Activity.class));
        } else if (isNoteIns(ins)) {
            startActivity(new Intent(context, NoteBookActivity.class));
        } else if (isRadioIns(ins)) {
            startActivity(new Intent(context, RadioActivity.class));
            activity.finish();
        } else if (isStudyIns(ins)) {
            startActivity(new Intent(context, Main_Study_Activity.class));
            activity.finish();
        } else if (isReadIns(ins)) {
            startActivity(new Intent(context, BookActivity.class));
            activity.finish();
        } else if (isWeatherIns(ins)) {
            broadcastWeather();
        } else if (isAddSpeedIns(ins)) {
            addSpeed();
        } else if (isDelSpeedIns(ins)) {
            delSpeed();
        } else if (isTimeIns(ins)) {
            broadcastTime();
        } else if (isBackIns(ins)) {
            activity.finish();
        } else {
            readText("没听明白");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        READ_MODE = ARTICLE_MODE;
        listView.smoothScrollToPosition(position);
        News news = newsList.get(position);
        readText(news.getTitle());
    }

}
