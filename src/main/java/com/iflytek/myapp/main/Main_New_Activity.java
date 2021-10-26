package com.iflytek.myapp.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.ebook.ReadActivity;
import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.notebook.NoteListActivity;
import com.iflytek.myapp.radio.RadioActivity;
import com.iflytek.myapp.study.Main_Study_Activity;

/**
 * 这是开机的主界面，包含了一个文本框，然后通过调用基类BaseActivity中的语音识别可以获取用户说的话
 *
 * 识别完用户说的话之后，会调用回调函数speechCallBack，在里面做下一步操作。
 *
 * 下一步就调用了processJump函数来处理各个逻辑，这里的代码非常冗余，每个模块中都包含一个这样的模块，
 * 很难维护，如果需要添加一个什么功能，就需要修改所有模块的判断逻辑，建议想办法优化一下。
 */

public class Main_New_Activity extends BaseActivity implements GestureDetector.OnGestureListener {
    GestureDetector gestureDetector;
    EditText main_search_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        gestureDetector = new GestureDetector(this, this);
        main_search_edit = findViewById(R.id.main_search_edit);
        main_search_edit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getSpeechRecognizer();
                return true;
            }
        });
        MODE = SEARCH_MODE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        readText("长按屏幕说出指令");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        stopRead();
        playSound(R.raw.toggle);
        getSpeechRecognizer();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override

    public void speechCallBack() {
        main_search_edit.setText(SPEECH_RESULT);
        main_search_edit.setSelection(SPEECH_RESULT.length());
        processJump(SPEECH_RESULT);
    }

    /**
     * ins是用户说的话，然后通过很多if else来判断用户想要干什么，然后做出相应的跳转操作。
     * @param ins
     */

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            startActivity(new Intent(this, Main_News_Activity.class));

        } else if (isNoteIns(ins)) {
            startActivity(new Intent(this, NoteListActivity.class));

        } else if (isRadioIns(ins)) {
            startActivity(new Intent(this, RadioActivity.class));

        } else if (isStudyIns(ins)) {
            startActivity(new Intent(this, Main_Study_Activity.class));

        } else if (isReadIns(ins)) {
            startActivity(new Intent(this, BookActivity.class));

        } else if (isWeatherIns(ins)) {
            broadcastWeather();

        } else if (isAddSpeedIns(ins)) {
            addSpeed();

        } else if (isDelSpeedIns(ins)) {
            delSpeed();

        } else if (isTimeIns(ins)) {
            broadcastTime();

        } else if(isBackIns(ins)){
            readText("您已经在最开始的界面啦");

        } else {
            readText("没听明白");

        }
    }
}
