package com.iflytek.myapp.study;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseFragment;
import com.iflytek.myapp.utils.StudyDatabaseHelper;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.notebook.NoteBookActivity;
import com.iflytek.myapp.radio.RadioActivity;

import java.util.HashMap;
import java.util.Map;

public class Fragment_Study_List extends BaseFragment implements View.OnTouchListener {

    String channel;
    LinearLayout layout;
    TextView content_textView;
    TextView title_textView;
    SQLiteDatabase database;
    int cur_chapter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int total_chapter;
    Map<String, String> map;
    boolean flag = true;
    String id = null, title = null, content = null;
    boolean isReading = false;
    EditText editText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (LinearLayout) inflater.inflate(R.layout.study_type, container, false);
        channel = getArguments().getString("channel");
        init();
        setCount();
        setContent();
        return layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        READ_MODE = ARTICLE_MODE;
        if (map == null) {
            map = new HashMap<>();
            map.put("dangshi", "党史");
            map.put("xinzhongguoshi", "新中国史");
            map.put("gaigekaifangshi", "改革开放史");
            map.put("shehuizhuyifazhanshi", "社会主义发展史");
            map.put("推荐", "推荐");
        }
        if (channel == null) return;
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            readText(map.get(channel));
        } else {
            stopRead();
        }
    }

    private void setCount() {
        Cursor query = database.query(channel, new String[]{"count(*)"}, null, null, null, null, null);
        query.moveToFirst();
        String s = query.getString(query.getColumnIndex("count(*)"));
        total_chapter = Integer.parseInt(s);
    }

    private void setContent() {
        Cursor query = database.query(channel, null, "id=?", new String[]{String.valueOf(cur_chapter)}, null, null, null);
        if (query.moveToFirst()) {
            id = query.getString(query.getColumnIndex("id"));
            title = query.getString(query.getColumnIndex("title"));
            content = query.getString(query.getColumnIndex("content"));
        }
        title_textView.setText("第" + id + "章 " + title);
        content_textView.setText(content);
        READ_MODE = ARTICLE_MODE;
    }

    private void init() {
        if (channel.equals("推荐") && flag) {
            flag = false;
            readText("推荐");
        }
        StudyDatabaseHelper databaseHelper = new StudyDatabaseHelper(getContext(), "sishi", null, 1);
        database = databaseHelper.getReadableDatabase();
        content_textView = layout.findViewById(R.id.study_content);
        content_textView.setOnTouchListener(this);
        title_textView = layout.findViewById(R.id.study_title);
        sharedPreferences = context.getSharedPreferences("study_history", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cur_chapter = sharedPreferences.getInt(channel, 1);
        LinearLayout linearLayout = activity.findViewById(R.id.new_include);
        editText = linearLayout.findViewById(R.id.all_top_edit);
    }


    public static Fragment_Study_List newInstance(String channel) {
        Fragment_Study_List fragment = new Fragment_Study_List();
        Bundle bundle = new Bundle();
        bundle.putString("channel", channel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        editor.putInt(channel, cur_chapter);
        editor.apply();
    }

    public void getNextItem() {
        if (cur_chapter < total_chapter) cur_chapter++;
        else return;
        setContent();
        readText(title_textView.getText().toString());
    }

    public void onPressed() {
        isReading = !isReading;
        if (!isReading) {
            stopRead();
            return;
        }
        READ_MODE = ARTICLE_MODE;
        readText(content);
    }

    public void getBeforeItem() {
        if (cur_chapter > 1) cur_chapter--;
        else return;
        setContent();
        readText(title_textView.getText().toString());
    }

    public void onLongPressed() {
        playSound(R.raw.toggle);
        stopRead();
        getSpeechRecognizer();
    }

    @Override
    public void speechCallBack() {
        editText.setText(SPEECH_RESULT);
        editText.setSelection(SPEECH_RESULT.length());
        processJump(SPEECH_RESULT);
    }

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            startActivity(new Intent(context, Main_News_Activity.class));
            activity.finish();
        } else if (isNoteIns(ins)) {
            startActivity(new Intent(context, NoteBookActivity.class));
        } else if (isRadioIns(ins)) {
            startActivity(new Intent(context, RadioActivity.class));
            activity.finish();
        } else if (isStudyIns(ins)) {
            readText("您已经在学习");
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
        } else if (isTimeIns(ins)) {
            broadcastTime();
        } else if (isBackIns(ins)) {
            activity.finish();
        } else {
            readText("没听明白");
        }
    }
}
