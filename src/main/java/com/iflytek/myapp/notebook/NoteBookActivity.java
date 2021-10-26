package com.iflytek.myapp.notebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 做笔记模块的代码逻辑有点复杂，我的实现也有一点小问题还没有解决。
 * 在这里我就说一下预期的功能效果
 *
 * 从除了主界面的任何模块进入笔记模块，都是进入这个类，也就是做笔记的类，从主界面直接进入笔记模块会进入笔记列表的函数，
 * 也就是NoteListActivity类。
 *
 * 因为：我们假设人们在看新闻或看电子书或听歌等时候进入笔记模式，是因为听到某些东西，所以想去做笔记，而不是听电子书听了
 * 一半，突然想去听自己做的笔记。
 *
 * 在笔记模式下，如果没有手动保存，那么下次进入笔记模式，会加载之前的草稿内容，并且接着上次继续做笔记。如果上次保存了
 * 笔记内容，那么这次就直接开启一个新的笔记，开始做笔记。默认情况下笔记的标题是当时的时间（精确到秒），而且为了方便，
 * 保存这条笔记内容的文件的名称也是笔记标题名称（在本地有一个文件夹保存了每一条笔记，一条笔记就是一个文件，文件名就是
 * 笔记的标题）。
 *
 * 用户通过长按屏幕唤出语音识别，然后说保存，那么这条笔记就保存了。或者，用户长按屏幕唤出语音识别，说修改标题，若识别出
 * 是修改标题，那么先说一段提示音：“请说出标题名称”，然后代码再次自动调用语音识别来接收用户说的标题名称，完成标题的修改。
 * 修改标题默认也保存内容，下次进入笔记模式就是做新的笔记。
 *
 * 这个模块的功能有点复杂，可以后期开发一点一点修改，先实现个只做笔记的功能，包括做笔记和删除笔记。最后所有模块开发完成
 * 再来完善这些细节。
 *
 * 我设定的手势含义：
 * 1、点击屏幕表示做笔记，唤出语音识别，来识别用户要说的笔记内容。
 * 2、长按屏幕表示指令，用户可以在这个情况下说返回，或者保存，或修改标题，来完成一些动作，否则无法区分这句话是笔记内容还是想要操作
 * 3、手指向上滑表示删除一句话
 * 4、手指向下滑表示阅读本条笔记所有的内容
 * 5、音量键可以控制笔记前后阅读（锦上添花，后期有精力可以实现）
 *
 * 笔记的实现方法：
 * 使用一个List<String>来存储笔记。用户每说一句话，就向List中添加一个String，用户要删除一句话，就将List的最后一个
 * String删除掉。保存的时候，就将该List的内容写入到本地的文本文件中。载入草稿的时候就从上次记录的笔记的文件中加载
 * 所有的String进入这个List中，以统一操作。
 *
 * 是否新建笔记或载入上次的草稿，是通过一个变量”save“来判断的，如果保存了笔记，就将这个变量设置为true，否则设置为false。
 * 若是false，再通过一个变量存储当前笔记的文件名，下次载入笔记就从这个文件中载入。每次都要及时修改这个变量的状态，否则
 * 很容易出bug。
 *
 * 由于笔记模块不仅可以从各个模块中进入，直接开始做笔记，还可以从笔记列表中选择某条笔记，进入听笔记，或者修改笔记，所以
 * 这里又存在一个优先级问题，若是从笔记列表进入笔记模块，那么不能载入上次的草稿，应该展示用户想看的那条笔记内容。但下次
 * 从别的模块进入做笔记的时候，还是需要载入上次的草稿。
 *
 * 这里的逻辑非常复杂，也可以不按照我的逻辑，自己考虑一下怎么操作方便来实现。建议放在最后考虑，所有模块都完成了再来优化
 * 这些细节。我的代码也很混乱，我自己也都快弄不清楚了，所以建议不要看我的代码实现。
 *
 */
public class NoteBookActivity extends BaseActivity implements View.OnTouchListener {

    TextView textView;
    String[] noteList;
    int lens;
    String title;
    boolean isModified;
    File rootDir;

    boolean speech_mode;
    boolean fromNote;
    Intent intent;
    SharedPreferences sharedPreferences;
    int biaoti = 0;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_note_activity);
        System.out.println("=========================");
        init();
        title = getDataString();
        boolean save = sharedPreferences.getBoolean("save", true);
        intent = getIntent();
        fromNote = intent.getStringExtra("fromNote") != null;
        if (!save || fromNote) {
            if (!fromNote) title = sharedPreferences.getString("title", getDataString());
            else title = intent.getStringExtra("title");
            if (!save) isModified = true;
            String line;
            try {
                BufferedReader reader;
                if (!fromNote)
                    reader = new BufferedReader(new FileReader(new File(rootDir, title)));
                else reader = new BufferedReader(new FileReader(intent.getStringExtra("location")));
                while ((line = reader.readLine()) != null) {
                    noteList[lens++] = line;
                }
                reader.close();
                mergeNote();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        readText("开始做笔记吧");
    }


    private String getDataString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date date = new Date();
        return simpleDateFormat.format(date);
    }


    private void init() {
        noteList = new String[10000];
        lens = 0;
        textView = findViewById(R.id.note_textview);
        textView.setOnTouchListener(this);
        isModified = false;
        rootDir = new File(this.getFilesDir(), "Notebook");
        sharedPreferences = getSharedPreferences("note", MODE_PRIVATE);
        edit = sharedPreferences.edit();
    }

    private void saveNote() throws IOException {
        if (!rootDir.exists()) rootDir.mkdir();
        File note;
        if (fromNote) {
            String t = intent.getStringExtra("location");
            int i = t.lastIndexOf("/");
            note = new File(t.substring(0, i + 1) + title);
        } else note = new File(rootDir, title);
        if (!note.exists()) note.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(note));
        writer.write(textView.getText().toString());
        writer.flush();
        writer.close();
        if (title.startsWith("2021.") && noteList.length != 0 && !noteList[0].isEmpty()) {
            title = noteList[0];
            note.renameTo(new File(rootDir, title));
        }
        edit.putBoolean("save", !isModified);
        isModified = false;
    }


    public void mergeNote() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lens; i++) {
            builder.append(noteList[i]).append("\n");
        }
        if (!fromNote) isModified = true;
        textView.setText(builder);
    }

    @Override
    public void onReadDone() {
        if (biaoti == 1) {
            biaoti = 2;
            MODE = SEARCH_MODE;
            playSound(R.raw.click);
            getSpeechRecognizer();
        }
    }

    @Override
    public void speechCallBack() {
        if (biaoti == 2) {
            biaoti = 0;
            if (SPEECH_RESULT.isEmpty()) {
                readText("没听清哦");
            } else {
                File note;
                if (fromNote) note = new File(intent.getStringExtra("location"));
                else note = new File(rootDir, title);
                note.renameTo(new File(rootDir, SPEECH_RESULT));
                title = SPEECH_RESULT;
                edit.putBoolean("save", true);
                playSound(R.raw.done);
                try {
                    saveNote();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (speech_mode) {
            if (SPEECH_RESULT.contains("保存")) {
                try {
                    saveNote();
                    playSound(R.raw.done);
                } catch (IOException e) {
                    readText("出错了");
                    e.printStackTrace();
                }
            } else if (SPEECH_RESULT.contains("标题")) {
                biaoti = 1;
                readText("请在听到提示音后说出标题");
            } else if (SPEECH_RESULT.contains("返回")) {
                finish();
            } else {
                playSound(R.raw.error);
            }
        } else {
            noteList[lens++] = SPEECH_RESULT;
            readText(SPEECH_RESULT);
            mergeNote();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fromNote || isModified) {
            edit.putBoolean("save", !isModified);
            try {
                saveNote();
            } catch (IOException e) {
                e.printStackTrace();
            }
            edit.putString("title", title);
        } else {
            edit.putBoolean("save", true);
        }
        edit.apply();
        Intent intent = new Intent();
        intent.putExtra("isModify", isModified);
        setResult(RESULT_OK, intent);
    }

    public void onLongPressed() {
        stopRead();
        speech_mode = true;
        MODE = SEARCH_MODE;
        playSound(R.raw.toggle);
        getSpeechRecognizer();
    }

    public void onPressed() {
        stopRead();
        speech_mode = false;
        MODE = NOTE_MODE;
        playSound(R.raw.click);
        getSpeechRecognizer();
    }

    public void getNextItem() {
        if (lens > 0) lens--;
        mergeNote();
        if (lens > 0) readText(noteList[lens - 1]);
    }

    public void getBeforeItem() {
        if (lens == 0) return;
        readText(textView.getText().toString());
    }
}