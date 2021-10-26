package com.iflytek.myapp.radio;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.iflytek.domain.adapter.Radios;
import com.iflytek.domain.adapter.RadiosAdapter;
import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.news.Main_News_Activity;
import com.iflytek.myapp.notebook.NoteBookActivity;
import com.iflytek.myapp.study.Main_Study_Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台模块，这个模块非常简答，用一个ListView来展示每个电台即可，IOS与安卓的方法肯定不一样，具体细节也不介绍，
 * 在安卓里直接通过一个mediaPlayer播放下述的URL，就能获得电台的声音。最后重写touch方法，通过手势控制播放哪一个
 * 电台即可。先做一个通过触摸播放的电台模块，然后最后再改成通过手势操作的。
 */
public class RadioActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnTouchListener {
    final static String[] RadioList = {"中国之声", "经济之声", "音乐之声", "经典音乐广播", "中华之声", "神州之声", "民族之声", "文艺之声", "大湾区之声", "老年之声", "香港之声", "藏语广播", "哈萨克语广播", "中国交通广播", "中国乡村之声", "阅读广播", "维语广播", "南海之声", "CRI环球资讯"};
    final static String[] html = {
            "http://ngcdn001.cnr.cn/live/zgzs/index.m3u8",
            "http://ngcdn002.cnr.cn/live/jjzs/index.m3u8",
            "http://ngcdn003.cnr.cn/live/yyzs/index.m3u8",
            "http://ngcdn004.cnr.cn/live/dszs/index.m3u8",
            "http://ngcdn005.cnr.cn/live/zhzs/index.m3u8",
            "http://ngcdn006.cnr.cn/live/szzs/index.m3u8",
            "http://ngcdn009.cnr.cn/live/mzzs/index.m3u8",
            "http://ngcdn010.cnr.cn/live/wyzs/index.m3u8",  //文艺之声
            "http://ngcdn007.cnr.cn/live/hxzs/index.m3u8",  //大湾区之声
            "http://ngcdn011.cnr.cn/live/lnzs/index.m3u8",  //老年之声
            "http://ngcdn008.cnr.cn/live/xgzs/index.m3u8",  //香港之声
            "http://ngcdn012.cnr.cn/live/zygb/index.m3u8",  //藏语广播
            "http://ngcdn025.cnr.cn/live/hygb/index.m3u8",  //哈语广播
            "http://ngcdn016.cnr.cn/live/gsgljtgb/index.m3u8",  //中国交通广播
            "http://ngcdn017.cnr.cn/live/xczs/index.m3u8",  //乡村之声
            "http://ngcdn014.cnr.cn/live/ylgb/index.m3u8",  //
            "http://ngcdn013.cnr.cn/live/wygb/index.m3u8",
            "http://cnlive.cnr.cn/hls/nanhaizhisheng.m3u8",
            "http://cnlive.cnr.cn/hls/huanqiuzixunguangbo.m3u8",
    };
    int[] idList = {R.drawable.radio_zhongguo, R.drawable.radio_jingji, R.drawable.radio_yinyue, R.drawable.radio_jingdianyinyue, R.drawable.radio_zhonghua, R.drawable.radio_shenzhou, R.drawable.radio_minzu, R.drawable.radio_wenyi, R.drawable.radio_dawanqu, R.drawable.radio_laonian, R.drawable.radio_xianggang, R.drawable.radio_zangyu, R.drawable.radio_hasakeyu, R.drawable.radio_jiaotong, R.drawable.radio_xiangcun, R.drawable.radio_yuedu, R.drawable.radio_weiyu, R.drawable.radio_nanhai, R.drawable.radio_cri};

    List<Radios> radiosList;
    ListView listView;
    int currentPlayer = -1;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cur_radio_activity);
        readText("上下切换电台");
        initList();
        init();
    }

    private void initList() {
        radiosList = new ArrayList<>(70);
        for (int i = 0; i < RadioList.length; i++) {
            Radios radios = new Radios();
            radios.setImageId(idList[i]);
            radios.setTitle(RadioList[i]);
            radios.setUrl(html[i]);
            radiosList.add(radios);
        }
    }

    private void init() {
        listView = findViewById(R.id.radioListView);
        RadiosAdapter adapter = new RadiosAdapter(this, R.layout.radio_type, radiosList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }





    ////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////  手势控制之类的函数  ////////////////////////////////

    /**
     * 下面的函数是一些通过手势控制播放电台的函数，先不用管，先做一个正常的通过点击播放电台的模块，
     * 然后修改成手势控制即可。
     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listView.smoothScrollToPosition(position);
        readText(radiosList.get(position).getTitle());
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = null;
    }

    public void onLongPressed() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        playSound(R.raw.toggle);
        getSpeechRecognizer();
    }

    public void getNextItem() {
        if (currentPlayer == RadioList.length - 1) return;
        stopRead();
        currentPlayer++;
        listView.performItemClick(listView.getChildAt(currentPlayer), currentPlayer, listView.getItemIdAtPosition(currentPlayer));
        if (currentPlayer > 0) {
            int visiblePosition = listView.getFirstVisiblePosition();
            View view = listView.getChildAt(currentPlayer - 1 - visiblePosition);
            listView.getAdapter().getView(currentPlayer - 1, view, listView);
            ImageView imageView = view.findViewById(R.id.laba);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    public void onPressed() {
        if (currentPlayer == -1) return;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//加上这句话，注意位置
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mediaPlayer.setDataSource(html[currentPlayer]);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mediaPlayer.start();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            int visiblePosition = listView.getFirstVisiblePosition();
            View view = listView.getChildAt(currentPlayer - visiblePosition);
            listView.getAdapter().getView(currentPlayer, view, listView);
            ImageView imageView = view.findViewById(R.id.laba);
            imageView.setVisibility(View.VISIBLE);
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

    public void getBeforeItem() {
        if (currentPlayer == 0) return;
        stopRead();
        currentPlayer--;
        listView.performItemClick(listView.getChildAt(currentPlayer), currentPlayer, listView.getItemIdAtPosition(currentPlayer));
        if (currentPlayer < RadioList.length - 1) {
            int visiblePosition = listView.getFirstVisiblePosition();
            View view = listView.getChildAt(currentPlayer + 1 - visiblePosition);
            listView.getAdapter().getView(currentPlayer + 1, view, listView);
            ImageView imageView = view.findViewById(R.id.laba);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    public void speechCallBack() {
        processJump(SPEECH_RESULT);
    }

    private void processJump(String ins) {
        if (isNewsIns(ins)) {
            startActivity(new Intent(this, Main_News_Activity.class));
            finish();
        } else if (isNoteIns(ins)) {
            startActivity(new Intent(this, NoteBookActivity.class));
        } else if (isRadioIns(ins)) {
            readText("您已经在这了");
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
        } else if (isTimeIns(ins)) {
            broadcastTime();
        } else if (isBackIns(ins)) {
            finish();
        } else {
            readText("没听明白");
        }
    }
}