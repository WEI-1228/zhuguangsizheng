package com.iflytek.myapp.notebook;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.iflytek.domain.adapter.Notes;
import com.iflytek.domain.adapter.NotesAdapter;
import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.radio.RadioActivity;
import com.iflytek.myapp.study.Main_Study_Activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 笔记列表模块
 * 在在主界面可以通过语音识别跳转到笔记列表，其他任何位置都只能跳转到直接做笔记。
 * 笔记功能的实现可以看一下计算机设计大赛文档中的一个函数流程图。
 *
 * 笔记列表会列出所有笔记，通过手势上下滑动选择不同的笔记，通过文字转语音来通知用户当前的笔记标题。
 *
 *    这里的操作包括：修改笔记标题，删除笔记，都是通过长按屏幕，然后用户说出指令来实现。修改笔记标题与在笔记内修改标题
 * 的操作类似。删除笔记就直接操作文件，将文件删除。选择到哪个文件，就修改或删除哪个文件。
 *    还可以进入笔记，点击屏幕就能进入这个笔记，进行修改等操作。
 *
 * 这里的选择实际上就是通过一个指针“cur_note”来操作的，将所有的笔记放到一个List中，然后把这个List的内容展示到界面上。用户通过手势
 * 来操作这个指针，实际上就是对cur_note进行加减，选择到哪个笔记就操作哪个笔记。当需要进入当前笔记的时候，就将笔记的目录
 * 位置传递给NoteBookActivity，并传递一个来自笔记列表的标志，表明目前是通过笔记列表进入笔记的。从笔记列表进入笔记和从
 * 别的模块进入笔记不同点在于：笔记列表里的笔记是已经存在的笔记，对其进行修改不需要手动保存，就能永久保存，并且不能影响
 * 笔记草稿的状态。
 *
 *
 * 这些其实也都是细节操作，建议留到最后写，先通过手指点击来实现进入笔记，最后再优化细节。
 */
public class NoteListActivity extends BaseActivity implements View.OnTouchListener {

    List<Notes> notesList;
    ListView listView;
    int cur_note = -1;
    NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_notes_list_activity);
        init();
        freshNote();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void init() {
        listView = findViewById(R.id.note_list_view);
        listView.setOnTouchListener(this);
    }

    String[] fileList;

    boolean flag = false;

    private void freshNote() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notesList = new ArrayList<>();
                File rootDir = new File(getFilesDir(), "Notebook");
                if (!rootDir.exists()) rootDir.mkdir();
                fileList = rootDir.list();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                if (!flag) {
                    if (fileList.length == 0) readText("您目前还没有笔记，点击屏幕开始做笔记吧！");
                    else readText("上下滑动选择您要阅读的笔记");
                    flag = true;
                }
                for (String s : fileList) {
                    Notes notes = new Notes();
                    if (s.startsWith("2021.")) {
                        String[] split = s.split("\\.");
                        notes.setTitle(split[0] + "年" + split[1] + "月" + split[2] + "日" + split[3] + "时" + split[4] + "分");
                    } else {
                        notes.setTitle(s);
                    }
                    File file = new File(rootDir, s);
                    long time = file.lastModified();
                    String format = simpleDateFormat.format(new Date(time));
                    notes.setDate(format);
                    notes.setLocation(file.toString());
                    notesList.add(notes);
                }
                adapter = new NotesAdapter(NoteListActivity.this, R.layout.note_type, notesList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    public void onPressed() {
        if (cur_note == -1) {
            stopRead();
            startActivityForResult(new Intent(this, NoteBookActivity.class), 1);
        } else {
            Intent intent = new Intent(this, NoteBookActivity.class);
            intent.putExtra("fromNote", "true");
            intent.putExtra("location", notesList.get(cur_note).getLocation());
            intent.putExtra("title", notesList.get(cur_note).getTitle());
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fileList == null) return;
        if (fileList.length == 0) readText("您目前还没有笔记，点击屏幕开始做笔记吧！");
        else readText("上下滑动选择您要阅读的笔记");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        freshNote();
        adapter.notifyDataSetChanged();
    }

    public void onLongPressed() {
        playSound(R.raw.toggle);
        getSpeechRecognizer();
    }

    public void getNextItem() {
        if (cur_note == notesList.size() - 1) return;
        cur_note++;
        listView.smoothScrollToPosition(cur_note);
        readText(notesList.get(cur_note).getTitle());
    }

    public void getBeforeItem() {
        if (cur_note == -1) return;
        cur_note--;
        if (cur_note == -1) {
            readText("新建笔记");
            return;
        }
        listView.smoothScrollToPosition(cur_note);
        readText(notesList.get(cur_note).getTitle());
    }

    public void speechCallBack() {
        processJump(SPEECH_RESULT);
    }

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            startActivity(new Intent(this, Main_News_Activity.class));
            finish();
        } else if (isNoteIns(ins)) {
            readText("滑动到最上方开始做笔记吧");
        } else if (isRadioIns(ins)) {
            startActivity(new Intent(this, RadioActivity.class));
            finish();
        } else if (isStudyIns(ins)) {
            startActivity(new Intent(this, Main_Study_Activity.class));
            this.finish();
        } else if (isReadIns(ins)) {
            startActivity(new Intent(this, BookActivity.class));
            this.finish();
        } else if (isWeatherIns(ins)) {
            broadcastWeather();
        } else if (isAddSpeedIns(ins)) {
            addSpeed();
        } else if (isDelSpeedIns(ins)) {
            delSpeed();
        } else if (isDelIns(ins)) {
            if (cur_note >= 0) {
                new File(notesList.get(cur_note).getLocation()).delete();
                playSound(R.raw.del);
                notesList.remove(cur_note);
                cur_note--;
                adapter.notifyDataSetChanged();
            } else {
                readText("请选择一条笔记删除");
            }
        } else if (isTimeIns(ins)) {
            broadcastTime();
        } else if (isBackIns(ins)) {
            finish();
        } else {
            readText("没听明白");
        }
    }
}
