package com.zhouqianbin.demo.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.R;

/**
 * 语音合成
 */
public class SpeechSynthesizerActivity extends AppCompatActivity {

    private static final String TAG = SpeechSynthesizerActivity.class.getSimpleName();
    // 语音合成对象
    private SpeechSynthesizer mSpeechSynthesizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_synthesizer);

        final EditText editText = findViewById(R.id.syn_et_text);
        SpeechUtility.createUtility(this, "appid=" + AppSetting.APP_ID);

        // 初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(this, new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(TAG, "InitListener init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG, "初始化失败,错误码：" + code);
                } else {
                    isInitSuccess = true;
                    Log.d(TAG, "初始化成功");
                    // 初始化成功，之后可以调用startSpeaking方法
                    // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                    // 正确的做法是将onCreate中的startSpeaking调用移至这里
                }
            }
        });
        initParam();

        findViewById(R.id.syn_btn_start_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInitSuccess){
                    return;
                }
                String text = editText.getText().toString();
                int code;
                if(TextUtils.isEmpty(text)){
                    code = mSpeechSynthesizer.startSpeaking("请输出合成的内容",null);
                }else {
                    code  = mSpeechSynthesizer.startSpeaking(text, mTtsListener);
                }
                if (code != ErrorCode.SUCCESS) {
                   Log.d(TAG,"语音合成失败,错误码: " + code);
                }
            }
        });

        findViewById(R.id.syn_btn_stop_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechSynthesizer.stopSpeaking();
            }
        });

        findViewById(R.id.syn_btn_pause_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechSynthesizer.pauseSpeaking();
            }
        });

        findViewById(R.id.syn_btn_resume_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeechSynthesizer.resumeSpeaking();
            }
        });
    }

    /**
     * 参数设置
     * @return
     */
    private void initParam() {
        // 清空参数
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mSpeechSynthesizer.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        // 设置在线合成发音人
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        // 设置播放合成音频打断音乐播放，默认为true
        mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        /*
        mSpeechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mSpeechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/tts.pcm");
                */
    }

    boolean isInitSuccess;

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.d(TAG,"开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.d(TAG,"暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.d(TAG,"继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            Log.d(TAG,"合成进度 " + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Log.d(TAG,"播放进度 " + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.d(TAG,"播放完成");
            } else if (error != null) {
                Log.d(TAG,error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}

            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Log.e("MscSpeechLog", "buf is =" + buf);
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mSpeechSynthesizer){
            mSpeechSynthesizer.stopSpeaking();
            // 退出时释放连接
            mSpeechSynthesizer.destroy();
        }
    }

}
