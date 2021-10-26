package com.iflytek.myapp.ebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.utils.MyDatabaseHelper;
import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.notebook.NoteBookActivity;
import com.iflytek.myapp.radio.RadioActivity;
import com.iflytek.myapp.study.Main_Study_Activity;

import java.util.HashMap;
import java.util.Map;


/**
 * 电子书的阅读活动，将从BookActivity传来的书名，去数据库中搜索，然后将内容展示在界面上
 */
public class ReadActivity extends BaseActivity {

    /**
     * 各个变量的含义：
     * myDatabaseHelper：操作数据库用的，通过其获取writableDatabase
     * content：展示小说的章节内容的控件
     * title：展示小说章节标题的控件（第一章、第二章等等）
     * writableDatabase：通过这个对象来执行数据库的crud操作，这里主要进行的是query操作
     * page：用户当前阅读到的页数
     * totalPageCount：这本小说的总页数
     * bookName：这本小说的名称
     * sharedPreferences：存储对象
     * isReading：用户是否暂停阅读
     */

    MyDatabaseHelper myDatabaseHelper;
    TextView content;
    TextView title;
    SQLiteDatabase writableDatabase;
    int page;
    int totalPageCount;
    String bookName;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor book_history;

    boolean isReading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_book_activity);
        //接收从BookActivity传来的书名
        bookName = getIntent().getStringExtra("bookname");
        //获取之前阅读到的历史页数，不要每次从第一页开始阅读
        sharedPreferences = getSharedPreferences("book_history", MODE_PRIVATE);
        book_history = sharedPreferences.edit();
        page = sharedPreferences.getInt(bookName, 1);
        initUI();
    }

    /**
     * 初始化界面的UI，以及获取数据库对象，我把存储书籍的数据库名称就写死成了book，不太灵活，不过也没有修改的需求。
     * 然后展示之前读到的页数。
     * 最后获取这本书的总页数，防止用户操作超过所有页码，或者负的页码
     */
    private void initUI() {
        //初始化ui并绑定事件
        content = findViewById(R.id.book_content);
        content.setOnTouchListener(this);
        title = findViewById(R.id.book_title);
        myDatabaseHelper = new MyDatabaseHelper(this, "book", null, 1);
        writableDatabase = myDatabaseHelper.getWritableDatabase();
        //展示当前page页
        flushPage(page);
        //获取总页数
        totalPageCount = getTotalPageCount(bookName);
    }

    /**
     * page表示需要阅读哪一页，这个函数的作用就是传入一个页码，然后在界面上显示该页的内容
     * @param page
     */
    private void flushPage(int page) {
        Map<String, String> text = getPage(bookName, page);
        this.title.setText(text.get("chapter"));
        this.content.setText(text.get("content"));
        readText(this.title.getText().toString());
    }


    /**
     * writableDatabase.query就相当于一条sql语句，第一个参数是数据表名称，第二个是查询的列，这里所有列都要，
     * 因此填null即可，第三个是查询的条件，第四个是与第三个条件中占位符对应的值。后面的都不需要，填null即可
     *
     * 后面的是固定的取出每列的代码。IOS估计不一样。
     * @param book 书籍的名称
     * @param page 书籍的页数
     * @return 一个Map，key为chapter和content，value是其中的内容，可以在flushPage中看到
     */
    public Map<String, String> getPage(String book, int page) {
        Map<String, String> map = new HashMap<>();
        Cursor query = writableDatabase.query(book, null, "id=?", new String[]{String.valueOf(page)}, null, null, null);
        if (query.moveToFirst()) {
            String title = query.getString(query.getColumnIndex("chapter"));
            String content = query.getString(query.getColumnIndex("content")).trim();
            map.put("chapter", title);
            map.put("content", content);
        }
        return map;
    }

    //获取总页数
    public int getTotalPageCount(String book) {
        Cursor query = writableDatabase.query(book, new String[]{"count(*)"}, null, null, null, null, null);
        query.moveToFirst();
        String count = query.getString(query.getColumnIndex("count(*)"));
        return Integer.parseInt(count);
    }


    /**
     * 翻页的函数，判断页码是否正确，不要越界了，然后每次都要将当前页码保存到本地，刷新一下历史页码。
     */
    public void pageUP() {
        if (page < totalPageCount) {
            page++;
            book_history.putInt(bookName, page);
            book_history.apply();
            stopRead();
            flushPage(page);
        }
    }

    public void pageDOWN() {
        if (page > 1) {
            page--;
            book_history.putInt(bookName, page);
            book_history.apply();
            stopRead();
            flushPage(page);
        }
    }

    /////////////////////////////  一些手势操作的函数   /////////////////////////////

    /**
     * 建议最后再做这部分，先使用两个按钮替代翻页功能，项目功能都做完了再阅读后面这部分代码
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!isReading) return true;
                readNext();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                readBack();
                if (!isReading) return true;
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override

    public void onPressed() {
        isReading = !isReading;
        if (!isReading) stopRead();
        else readText(content.getText().toString());
    }

    public void getNextItem() {
        pageUP();
    }

    public void getBeforeItem() {
        pageDOWN();
    }

    public void onLongPressed() {
        playSound(R.raw.toggle);
        stopRead();
        getSpeechRecognizer();
    }

    @Override
    public void speechCallBack() {
        stopRead();
        isReading = false;
        processJump(SPEECH_RESULT);
    }

    private void processJump(String ins) {
        ins = "电子书";
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