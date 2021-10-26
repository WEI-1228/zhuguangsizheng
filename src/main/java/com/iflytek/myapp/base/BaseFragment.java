package com.iflytek.myapp.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.myapp.R;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * 与BaseActivity内容几乎一致
 */

public class BaseFragment extends Fragment implements TextToSpeech.OnInitListener, View.OnTouchListener {

    public Context context;
    final int MAX_READ_LEN = 50;
    String READ_TEXT;
    public String SPEECH_RESULT;
    private TextToSpeech mTextToSpeech;
    SharedPreferences sharedPreferences;
    int current = 0;
    public String MODE;
    public String SEARCH_MODE = "0";
    public String NOTE_MODE = "1";

    public int NEWS_MODE = 0;
    public int ARTICLE_MODE = 1;
    public int READ_MODE = ARTICLE_MODE;

    public FragmentActivity activity;

    int LONG_CLICK_TIME = 700;
    long last_time = 0;
    boolean isLongClick = false;
    boolean stoped = false;
    double before_press_Y;

    ////////////////////
    private static String TAG = BaseFragment.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private Toast mToast;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String language = "zh_cn";

    private String resultType = "json";

    private StringBuffer buffer = new StringBuffer();
    private SharedPreferences mSharedPreferences;
    ////////////////////


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        activity = getActivity();
        context = getContext();
        super.onCreate(savedInstanceState);
        sharedPreferences = activity.getSharedPreferences("setting", MODE_PRIVATE);

        /////////////////////////////
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(context, mInitListener);
        mSharedPreferences = activity.getSharedPreferences(IatSettings.PREFER_NAME,
                MODE_PRIVATE);
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        /////////////////////////////
        MODE = SEARCH_MODE;
    }

    boolean nextOrBefore = false;

    public void readBack() {
        if (current > 1) {
            current -= 2;
            nextOrBefore = true;
            readText(null);
        }
    }

    public void readNext() {
        if (current < readList.size()) {
            nextOrBefore = true;
            readText(null);
        }
    }

    public void appendRead(String text) {
        if (text == null || text.isEmpty())
            return;
        if (readList.size() == 0) readText(text);
        else readList.add(text);
    }

    /**
     * 阅读文本
     *
     * @param text
     */

    List<String> readList = new LinkedList<>();

    public void cutText(String text) {
        if (READ_MODE == ARTICLE_MODE) {
            while (true) {
                if (text.length() < MAX_READ_LEN + 1) {
                    readList.add(text);
                    break;
                }
                readList.add(text.substring(0, MAX_READ_LEN));
                text = text.substring(MAX_READ_LEN);
            }
        } else if (READ_MODE == NEWS_MODE) {
            String[] split = text.split("[。，！ ：？]");
            readList.addAll(Arrays.asList(split));
        }
    }

    public void readText(String text) {
        //下一次阅读前先结束正在阅读
        stopRead();
        if (!nextOrBefore) {
            current = 0;
            readList.clear();
            //切分字符串为MAX_READ_LEN
            cutText(text);
        } else nextOrBefore = false;
        //取出第一段待阅读文本
        this.READ_TEXT = readList.get(current++);
        // 参数Context,TextToSpeech.OnInitListener
        mTextToSpeech = new TextToSpeech(context, this);

        //下面是阅读完成的回调函数，通过该回调函数连续阅读Queue里的字符，就实现了连续阅读
        mTextToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        mTextToSpeech.setPitch(1.0f);
        // 设置语速
        mTextToSpeech.setSpeechRate(sharedPreferences.getFloat("speechRate", 0.8f));
    }

    UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if (current < readList.size()) {
                //若还有下一段，取出下一段阅读
                READ_TEXT = readList.get(current++);
                mTextToSpeech.speak(READ_TEXT, TextToSpeech.QUEUE_FLUSH, null, "uniqueID");
            }
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    /**
     * 在这个方法中阅读文本
     *
     * @param status
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
            } else {
                mTextToSpeech.speak(READ_TEXT, TextToSpeech.QUEUE_FLUSH, null, "uniqueID");
            }
        }
    }

    //关闭TTS并释放
    public void stopRead() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopRead();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRead();
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }


    public void getSpeechRecognizer() {
        buffer.setLength(0);
        mIatResults.clear();
        setParam();
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        TextView tv_textlink = mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        tv_textlink.setText("烛光思政");
        tv_textlink.setTextSize(25);
        tv_textlink.getPaint().setFlags(Paint.SUBPIXEL_TEXT_FLAG);//取消下划线
        tv_textlink.setEnabled(false);
        showTip(getString(R.string.text_begin));

    }

    public void speechCallBack() {

    }

    boolean speech_result = false;
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            if (SPEECH_RESULT.isEmpty()) return;
            if (speech_result) speechCallBack();
            speech_result = !speech_result;
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));

        }

    };

    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        SPEECH_RESULT = resultBuffer.toString();
    }

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);


        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", MODE));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stoped = false;
                before_press_Y = event.getY();
                last_time = System.currentTimeMillis();
                new MyThread().start();
                break;
            case MotionEvent.ACTION_UP:
                stoped = true;
                if (isLongClick) {
                    isLongClick = false;
                    return true;
                }
                double now_press_Y = event.getY();
                if (now_press_Y - before_press_Y > 50) {
                    getBeforeItem();
                } else if (now_press_Y - before_press_Y == 0) {
                    onPressed();
                } else if (before_press_Y - now_press_Y > 50) {
                    getNextItem();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                stoped = true;
                break;

        }
        return true;
    }

    public void onPressed() {

    }

    public void onLongPressed() {

    }

    public void getBeforeItem() {

    }

    public void getNextItem() {

    }

    class MyThread extends Thread {
        public void run() {

            while (System.currentTimeMillis() - last_time < LONG_CLICK_TIME && !stoped) ;
            if (stoped) return;
            isLongClick = true;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLongPressed();
                }
            });
        }
    }

    public boolean isNewsIns(String ins) {
        return ins.contains("新闻");
    }

    public boolean isReadIns(String ins) {
        return ins.contains("电子书") || ins.contains("读书") || ins.contains("名著");
    }

    public boolean isStudyIns(String ins) {
        return ins.contains("史") || ins.contains("学");
    }

    public boolean isNoteIns(String ins) {
        return ins.contains("笔记");
    }

    public boolean isRadioIns(String ins) {
        return ins.contains("电台") || ins.contains("歌") || ins.contains("音乐");
    }

    public boolean isWeatherIns(String ins) {
        return ins.contains("天气");
    }

    public boolean isAddSpeedIns(String ins) {
        return (ins.contains("语速") || ins.contains("速度")) && (ins.contains("高") || ins.contains("快"));
    }

    public boolean isDelSpeedIns(String ins) {
        return (ins.contains("语速") || ins.contains("速度")) && (ins.contains("低") || ins.contains("慢"));
    }

    public boolean isTimeIns(String ins) {
        return ins.contains("时间") || ins.contains("几点");
    }

    public boolean isBackIns(String ins) {
        return ins.contains("返回") || ins.contains("主界面");
    }

    public void broadcastTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日HH时mm分");
        String format = simpleDateFormat.format(new Date());
        readText("现在是    " + format);
    }


    public void broadcastWeather() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("message", MODE_PRIVATE);
        String temperature = sharedPreferences.getString("temperature", "error");
        String area = sharedPreferences.getString("area", "error");
        String wind_direction = sharedPreferences.getString("wind_direction", "error");
        String wind_power = sharedPreferences.getString("wind_power", "error");
        String province = sharedPreferences.getString("province", "error");
        String weather = sharedPreferences.getString("weather", "error");
        String builder = province + "省 " +
                area + "市    天气" +
                weather + "  " +
                temperature + "度  " +
                wind_direction + "  " +
                wind_power + "  ";
        readText(builder);
    }

    public void addSpeed() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int progress = sharedPreferences.getInt("speechProgress", 3);
        if (progress < 10) progress++;
        float rate = 0.2f + progress * 0.2f;
        editor.putFloat("speechRate", rate);
        editor.putInt("speechProgress", progress);
        editor.apply();
        readText("这是一个很好的阅读软件");
    }

    public void delSpeed() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int progress = sharedPreferences.getInt("speechProgress", 4);
        if (progress > 0) progress--;
        float rate = 0.2f + progress * 0.2f;
        editor.putFloat("speechRate", rate);
        editor.putInt("speechProgress", progress);
        editor.apply();
        readText("这是一个很好的阅读软件");
    }

    SoundPool soundPool;
    int soundID;

    public void playSound(int musicId) {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(activity, musicId, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(
                        soundID,
                        0.1f,      //左耳道音量【0~1】
                        0.5f,      //右耳道音量【0~1】
                        0,         //播放优先级【0表示最低优先级】
                        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                        1          //播放速度【1是正常，范围从0~2】
                );
            }
        });

    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                readNext();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                readBack();
                break;
            default:
                break;
        }
    }
}
