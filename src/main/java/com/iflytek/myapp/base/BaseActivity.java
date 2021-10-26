package com.iflytek.myapp.base;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.myapp.ebook.BookActivity;
import com.iflytek.myapp.R;

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

public class BaseActivity extends FragmentActivity implements TextToSpeech.OnInitListener, View.OnTouchListener {
    //////////////////////////   一些全局变量，先不用管什么用    ////////////////////////
    final int MAX_READ_LEN = 50;
    String READ_TEXT;
    public String SPEECH_RESULT;
    private TextToSpeech mTextToSpeech;
    SharedPreferences sharedPreferences;
    int current = 0;
    boolean isReading = false;
    public String MODE;
    public String SEARCH_MODE = "0";
    public String NOTE_MODE = "1";

    public int NEWS_MODE = 0;
    public int ARTICLE_MODE = 1;
    public int READ_MODE = ARTICLE_MODE;

    int LONG_CLICK_TIME = 700;
    long last_time = 0;
    boolean isLongClick = false;
    boolean stoped = false;
    double before_press_Y;

    ////////////////////  下面的都是语音识别的一些变量，原始代码中的，不需要理解含义 ////////////////////
    private static String TAG = BookActivity.class.getSimpleName();
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
    ////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏模式
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        //记录调整的TTS参数，之前设置的语速什么的都记录在这个里面
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);


        ///////////////   初始化语音识别代码（原始代码）     //////////////
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        ///////////////////////////////////////////////////////////////\


        MODE = NOTE_MODE;

    }

    //////////////////////////////////  控制文本向前阅读还是向后阅读  ////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////  文本转语音代码   ///////////////////////////////////////
    /**
     * 1、由于安卓的文本转语音（TTS）一次性只能阅读4000个字，所以才有了以下这么繁琐的代码
     * 2、由于盲人听新闻或听电子书可能需要暂停，或往回翻，所以下面的代码更复杂了
     * 3、刚开始开发不要写的跟我这个一样复杂，只要能把文本转换成语音就好了，先不要考虑前后翻页的问题，
     * 所有的模块都开发完之后，再来做这些细节。
     * 4、IOS使用的文本转语音肯定不是一样的类，所有不要完全看懂，这个模块的主要目标就是，编写一个函数，参数是待阅读的
     * 文本，调用该函数软件就能播报对应的语音。然后再编写一个函数能停止播报就行了。
     * 5、readText(String text)实现的就是上述的功能。
     * <p>
     * 总的思路：
     * 1、用readList<String>保存所有需要阅读的文本，通过一个指针current，标识当前阅读到了第几句，
     * nextOrBefore用来判断用户是回看了还是快进了。
     * 2、需要将待阅读的文本进行分割，分成一句一句的话，装入readList中，由于这个app可以阅读新闻和电子书的文章，
     * 因此我用了两种不同的分割模式，如果是新闻，就直接通过标点（。，！：？）进行分割，如果是电子书，就按长度进行分割
     * 比如50个字分割一次，全局变量MAX_READ_LEN就是分割的长度。
     * 3、刚开始开发的时候不要做的这么复杂，不知道IOS有没有文本转语音字数的限制，反正先不要做这么复杂，能阅读文本
     * 就行，全部开发完后再优化这些细节。
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
        mTextToSpeech = new TextToSpeech(this, this);

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
            onReadDone();
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    public void onReadDone() {
        if (current < readList.size()) {
            //若还有下一段，取出下一段阅读
            READ_TEXT = readList.get(current++);
            mTextToSpeech.speak(READ_TEXT, TextToSpeech.QUEUE_FLUSH, null, "uniqueID");
        }
    }

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
                Toast.makeText(this, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
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
        isReading = false;
    }

    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public void onStop() {
        super.onStop();
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

    //////////////////////////////////////   语音模块   /////////////////////////////////////////

    /**
     * 该模块的主要任务是通过一个函数封装语音调用功能，getSpeechRecognizer()的作用就是如此。这个模块是在原始的
     * 讯飞语音听写demo基础上修改得到的，大部分都不需要修改，只要修改一下需要的地方就行。
     * <p>
     * 语音识别完成后，会自动调用一个回调函数，以便做一些操作，这里的回调函数是speechCallBack()，在不同的功能
     * 模块中重写speechCallBack()就能轻松实现各个模块中不同的回调操作。
     * <p>
     * 讯飞的语音识别模块有个小问题（安卓有这个问题，IOS不知道有没有），也就是语音识别会调用两次回调函数，第一次先
     * 识别所有的句子，第二次会识别最后的标点符号。因此，我使用一个speech_result来标志本次转换是不是最后的完整结果，
     * 如果是最后处理完的结果，那么才调用speechCallBack()回调函数。也就是说，speechCallBack是经过一次封装后的
     * 回调函数，真正回调的其实是下面的mRecognizerDialogListener类中的onResult方法。
     */

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
        /**
         * 语音识别模块的回调函数，一次语音识别会调用两次，第一次识别前面的句子，第二次识别最后的标点。
         * 因此使用speech_result来判断是第几次，如果是第二次，那么就执行各个模块自定义的speechCallBack()函数。
         *
         * 可能不太好理解，举个例子：在电子书模块，用户需要通过语音识别来识别用户说的书名，在识别完书名后，就需要告诉
         * 用户，本书是否存在，或者直接跳转到书的章节，开始阅读。这个时候就需要用到回调函数了，语音识别一完成，就
         * 进行跳转或告诉用户书不存在。讯飞的语音识别的回调函数会执行两次，所以肯定会出现问题，我在这就进行了一个判断，
         * 如果是第二次，再执行一个speechCallBack()函数，就相当于一次回调了。然后每个模块重写speechCallBack就能
         * 实现不同的回调功能了。
         *
         * 一次语音识别函数的调用过程：
         *
         * getSpeechRecognizer()->唤出语音识别->用户说话->语音上传到讯飞服务器进行第一次识别（识别前面的文本）->
         * 执行回调函数onResult()->onResult内执行printResult()->printResult函数内将第一次识别结果存放在
         * 全局变量SPEECH_RESULT中（不带末尾标点）->自动执行第二次识别（识别末尾标点）->再次调用回调函数执行回调函数onResult->
         * 再次执行printResult()函数，将完整的结果存放在SPEECH_RESULT中（带标点）->执行函数speechCallBack()，此时
         * 可以在这个函数中执行自己的逻辑代码
         *
         *
         * 由于该app的语音识别有两种不同的功能，第一种是做笔记，第二种就是通过语音识别来搜索某个东西。因此做笔记肯定是需要
         * 带上标点的，而搜索电子书等搜索功能肯定不能带标点。针对这个问题，api也提供了一个参数。在下面的setParam中就有一行
         * 代码是用来设置是否带标点的，因此这里用一个全局变量MODE来表示是搜索模式(全局变量SEARCH_MODE)还是笔记模式(全局变量NOTE_MODE)，
         * 搜索模式就不带标点，笔记模式带标点。因此在需要语音识别的时候，都需要设置一下MODE。
         *
         * @param results
         * @param isLast
         */
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

    /**
     * 语音识别的结果存放在全局变量SPEECH_RESULT中
     *
     * @param results
     */
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


    /////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////  重写onTouch函数  ///////////////////////////////////

    /**
     * 下面是重写手势识别的函数，不需要阅读，IOS只要按照IOS开发的文档来重写即可，与安卓的肯定不一样
     *
     * @param v
     * @param event
     * @return
     */

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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLongPressed();
                }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////    下面是一些“人工智能“”的判断模块     /////////////////////

    /**
     * ins为收到的用户说的指令，然后对其进行判断，比如对于isNewsIns来说，如果里面包含了新闻，就代表用户打算看新闻，
     *
     * 对于isReadIns来说，如果用户说的话中包含了电子书或读书或名著，那么代表这个人要看电子书。。。。。。
     *
     * 这部分的代码写的有些冗余，有优化的空间，不用模仿这个。
     */

    public boolean isNewsIns(String ins) {
        return ins.contains("新闻");
    }

    public boolean isReadIns(String ins) {
        return ins.contains("电子书") || ins.contains("读书") || ins.contains("名著");
    }

    public boolean isStudyIns(String ins) {
        return ins.contains("党史") || ins.contains("学") || ins.contains("学习");
    }

    public boolean isNoteIns(String ins) {
        return ins.contains("笔记") || ins.contains("写");
    }

    public boolean isRadioIns(String ins) {
        return ins.contains("电台") || ins.contains("歌") || ins.contains("音乐");
    }

    public boolean isWeatherIns(String ins) {
        return ins.contains("天气");
    }

    public boolean isAddSpeedIns(String ins) {
        return (ins.contains("语速") || ins.contains("速度") || ins.contains("说")) && (ins.contains("高") || ins.contains("快"));
    }

    public boolean isDelSpeedIns(String ins) {
        return (ins.contains("语速") || ins.contains("速度") || ins.contains("说")) && (ins.contains("低") || ins.contains("慢"));
    }

    public boolean isBackIns(String ins) {
        return ins.contains("返回");
    }

    public boolean isDelIns(String ins) {
        return ins.contains("删除");
    }

    public boolean isTimeIns(String ins) {
        return ins.contains("时间") || ins.contains("几点");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


    //////////////////////////  播报天气的小功能模块以及一些小功能模块  ///////////////////////////////////

    /**
     * 下面都是属于锦上添花的模块，不需要去看，等整个app搭建完成之后，再去考虑增加这些功能，看到这就不用往下看了，最后再做
     */


    public void broadcastTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日HH时mm分");
        String format = simpleDateFormat.format(new Date());
        readText("现在是    " + format);
    }

    public void broadcastWeather() {
        SharedPreferences sharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int progress = sharedPreferences.getInt("speechProgress", 4);
        if (progress > 0) progress--;
        float rate = 0.2f + progress * 0.2f;
        editor.putFloat("speechRate", rate);
        editor.putInt("speechProgress", progress);
        editor.apply();
        readText("这是一个很好的阅读软件");
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////  播放提示音模块  /////////////////////////////////////

    SoundPool soundPool;
    int soundID;

    public void playSound(int musicId) {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, musicId, 1);
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

    ///////////////////////////////////////////////////////////////////////////////////////

}