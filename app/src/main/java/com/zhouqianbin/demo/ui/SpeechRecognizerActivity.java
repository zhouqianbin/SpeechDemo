package com.zhouqianbin.demo.ui;

import android.os.Environment;
import android.speech.RecognitionListener;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.utils.JsonParser;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 语音听写
 */
public class SpeechRecognizerActivity extends AppCompatActivity {

    private static final String TAG = SpeechRecognizerActivity.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mSpeechRecognizer;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private boolean mTranslateEnable = true;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognizer);

        SpeechUtility.createUtility(this, "appid=" + AppSetting.APP_ID);
        mSpeechRecognizer = SpeechRecognizer.createRecognizer(this,mInitListener);
        initParam();

        findViewById(R.id.recong_btn_start_recong_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIatDialog = new RecognizerDialog(SpeechRecognizerActivity.this,
                        mInitListener);
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
            }
        });

        findViewById(R.id.recong_btn_start_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不显示听写对话框
                mSpeechRecognizer.startListening(mRecognizerListener);
            }
        });

        findViewById(R.id.recong_btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechRecognizer.stopListening();
            }
        });

        findViewById(R.id.recong_btn_canncel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechRecognizer.cancel();
            }
        });
    }

    /**
     * 参数设置
     * @return
     */
    public void initParam() {
        // 清空参数
        mSpeechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mSpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, "10000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        /*mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory()+"/msc/iat.wav");*/
    }

   /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG,"初始化失败，错误码：" + code);
                ToastUtils.showToast(SpeechRecognizerActivity.this,"初始化失败，错误码：" + code);
            }else {
                Log.d(TAG,"初始化成功");
                ToastUtils.showToast(SpeechRecognizerActivity.this,"初始化成功");
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {

        public void onResult(RecognizerResult results, boolean isLast) {
             handleResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Log.d( TAG,error.getErrorDescription());
        }

    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {
            ToastUtils.showToast(SpeechRecognizerActivity.this,"开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            ToastUtils.showToast(SpeechRecognizerActivity.this,"结束说话");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d(TAG, recognizerResult.getResultString());
            handleResult(recognizerResult);
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            ToastUtils.showToast(SpeechRecognizerActivity.this,error.getErrorDescription());
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void handleResult(RecognizerResult results) {
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
        Log.d(TAG,"听写结果 " + resultBuffer.toString());
        ToastUtils.showToast(this,"听写结果 " + resultBuffer.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mSpeechRecognizer ){
            // 退出时释放连接
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
        }
    }

}
