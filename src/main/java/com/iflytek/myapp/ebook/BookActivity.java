package com.iflytek.myapp.ebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.notebook.NoteBookActivity;
import com.iflytek.myapp.radio.RadioActivity;
import com.iflytek.myapp.study.Main_Study_Activity;
import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.utils.MyDatabaseHelper;

/**
 * 这是电子书阅读模块的主界面函数，各个函数的作用写在函数上方
 */

public class BookActivity extends BaseActivity implements OnClickListener, View.OnLongClickListener {

    private EditText mResultText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook);
        initLayout();
    }


    /**
     * 初始化Layout。
     *
     * databaseHelper对象是一个sqlite3数据库的操作对象，通过这个对象来操作数据库。
     * databaseHelper.getWritableDatabase()的作用是：如果数据库没有被初始化（创建）过，就会执行
     * databaseHelper函数中的OnCreate对象，来创建数据库，将所有的电子书内容写入手机的sd卡，
     * 如果之前已经初始化过数据库了，那什么都不会干。
     *
     * MODE设置为SEARCH_MODE，也就是不带标点。
     */
    private void initLayout() {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this, "book", null, 1);
        databaseHelper.getWritableDatabase();
        mResultText = findViewById(R.id.main_search_edit);
        findViewById(R.id.rel_layout).setOnClickListener(this);
        findViewById(R.id.rel_layout).setOnLongClickListener(this);
        MODE = SEARCH_MODE;
    }

    /**
     * 处理函数，在speechCallBack回调函数中调用这个处理函数，来判断这本书是否存在。
     * 这里的判断逻辑是这样的：在之前的数据库初始化过程中，我们就将所有的书籍记录在了本地，
     * 这个地方直接查看本地是否存在这个书籍，就能知道这本书是否存在。如果存在，就将书籍传递到
     * 小说的阅读活动中，进行下一步操作。
     */
    public void process() {
        String bookName = mResultText.getText().toString();
        SharedPreferences bookmap = getSharedPreferences("bookmap", MODE_PRIVATE);
        String book = bookmap.getString(bookName, "");
        if (book.isEmpty()) {
            Toast.makeText(this, "暂时没有这本书！", Toast.LENGTH_SHORT).show();
            readText("暂时没有" + bookName + "这本书哦");
            return;
        }
        //如果存在书的话，就将书传给小说页面
        Intent bookIntent = new Intent(this, ReadActivity.class);
        bookIntent.putExtra("bookname", book);
        startActivity(bookIntent);
    }

    boolean speech_mode = true;

    @Override
    public void speechCallBack() {
        if (speech_mode) {
            mResultText.setText(SPEECH_RESULT);
            process();
        } else {
            processJump(SPEECH_RESULT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readText("请点击屏幕说出书名");
    }

    //////////////////////// 下面是一些手势的判断模块以及跳转模块 ////////////////////

    /**
     * 单击屏幕，此时就是进行电子书的搜索，长按屏幕就表示要进行模块的跳转，
     * speech_mode就是用来标志是进行电子书搜索还是模块跳转，在process中进行判断操作
     *
     * 只需要知道逻辑即可，IOS与安卓的实现方法不一定相同
     */

    @Override
    public void onClick(View view) {
        //若点击了button就停止阅读
        switch (view.getId()) {
            case R.id.rel_layout:
                speech_mode = true;
                getSpeechRecognizer();
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        speech_mode = false;
        playSound(R.raw.toggle);
        getSpeechRecognizer();
        return true;
    }

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            startActivity(new Intent(this, Main_News_Activity.class));
            this.finish();
        } else if (isNoteIns(ins)) {
            startActivity(new Intent(this, NoteBookActivity.class));
        } else if (isRadioIns(ins)) {
            startActivity(new Intent(this, RadioActivity.class));
            this.finish();
        } else if (isStudyIns(ins)) {
            startActivity(new Intent(this, Main_Study_Activity.class));
            this.finish();
        } else if (isReadIns(ins)) {
            readText("您已经在看书啦");
        } else if (isWeatherIns(ins)) {
            broadcastWeather();
        } else if (isAddSpeedIns(ins)) {
            addSpeed();
        } else if (isDelSpeedIns(ins)) {
            delSpeed();
        } else if (isTimeIns(ins)) {
            broadcastTime();
        } else if (isBackIns(ins)) {
            finish();
        } else {
            readText("没听明白");
        }
    }
}
