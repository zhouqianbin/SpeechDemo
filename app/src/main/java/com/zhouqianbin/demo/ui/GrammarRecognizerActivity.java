package com.zhouqianbin.demo.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.utils.JsonParser;
import com.zhouqianbin.demo.utils.ToastUtils;

import java.io.InputStream;

/**
 * 语法识别
 */
public class GrammarRecognizerActivity extends AppCompatActivity {

    private static String TAG = GrammarRecognizerActivity.class.getSimpleName();
    // 语音识别对象
    private SpeechRecognizer mAsr;
    // 云端语法文件
    private String mCloudGrammar = null;
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_recognizer);

         editText = findViewById(R.id.grammar_et_text);
        SpeechUtility.createUtility(this, "appid=" + AppSetting.APP_ID);
        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        mCloudGrammar = readFile(this,"grammar_sample.abnf","utf-8");

        initParms();

        findViewById(R.id.grammar_btn_build).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(mCloudGrammar);
                //指定引擎类型
                mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
                mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
                int ret = mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mCloudGrammar, mCloudGrammarListener);
                if(ret != ErrorCode.SUCCESS){
                    ToastUtils.showToast(GrammarRecognizerActivity.this,"语法构建失败,错误码：" + ret);
                }
            }
        });


        findViewById(R.id.grammar_btn_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsr.startListening(mRecognizerListener);
            }
        });
    }

    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            ToastUtils.showToast(GrammarRecognizerActivity.this,"当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                Log.d(TAG, "recognizer result：" + result.getResultString());
                String text ;
                if("cloud".equalsIgnoreCase(mEngineType)){
                    text = JsonParser.parseGrammarResult(result.getResultString());
                }else {
                    text = JsonParser.parseLocalGrammarResult(result.getResultString());
                }
                Log.d(TAG,"识别结果 " + text);
                ToastUtils.showToast(GrammarRecognizerActivity.this,"识别结果 " + text);
                editText.setText(text);
            } else {
                Log.d(TAG, "recognizer result : null");
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            ToastUtils.showToast(GrammarRecognizerActivity.this,"结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            ToastUtils.showToast(GrammarRecognizerActivity.this,"开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            ToastUtils.showToast(GrammarRecognizerActivity.this,"onError Code："	+ error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };


    /**
     * 参数设置
     * @return
     */
    public boolean initParms(){
        boolean result = false;
        //设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果为json格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
       /*
       mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory()+"/msc/asr.wav");
                */
        return result;
    }


    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                ToastUtils.showToast(GrammarRecognizerActivity.this,"语法构建成功：" + grammarId);
                //设置云端识别使用的语法id
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
            }else{
                ToastUtils.showToast(GrammarRecognizerActivity.this,"语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };


    /**
     * 读取asset目录下文件。
     * @return content
     */
    public String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte []buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len  = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf,code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showToast(GrammarRecognizerActivity.this,"初始化失败,错误码："+code);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mAsr ){
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
    }
}
