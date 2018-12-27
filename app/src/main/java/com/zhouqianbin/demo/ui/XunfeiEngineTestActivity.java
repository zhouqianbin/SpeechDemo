package com.zhouqianbin.demo.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.zhouqianbin.demo.engine.AiuiResultListn;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.engine.SpeechRecongnizerResult;
import com.zhouqianbin.demo.engine.WakeResult;
import com.zhouqianbin.demo.engine.XunfeiEngine;

public class XunfeiEngineTestActivity extends AppCompatActivity {

    private static final String TAG = XunfeiEngineTestActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xunfei_engine_test);

        new Thread(new Runnable() {
            @Override
            public void run() {
                XunfeiEngine.getInstance().initEngine(
                        XunfeiEngineTestActivity.this,
                        AppSetting.APP_ID);

                String resource = XunfeiEngine.getInstance().getResource(
                        XunfeiEngineTestActivity.this.getApplicationContext(),
                        AppSetting.APP_ID);
                // 清空参数
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.PARAMS, null);
                //唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.IVW_THRESHOLD, "0:"+ 1500);
                // 设置唤醒模式
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.IVW_SST, "wakeup");
                // 设置持续进行唤醒
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.KEEP_ALIVE, "1");
                // 设置闭环优化网络模式
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.IVW_NET_MODE, "0");
                // 设置唤醒资源路径
                XunfeiEngine.getInstance().setWakeParame(SpeechConstant.IVW_RES_PATH, resource);
            }
        }).start();


        final EditText editText = findViewById(R.id.engine_et_text);

        findViewById(R.id.engine_btn_start_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().startSpeak(editText.getText().toString().trim(), new SynthesizerListener() {
                    @Override
                    public void onSpeakBegin() {
                        Log.d(TAG,"onSpeakBegin");
                    }

                    @Override
                    public void onBufferProgress(int i, int i1, int i2, String s) {
                        Log.d(TAG,"onBufferProgress");
                    }

                    @Override
                    public void onSpeakPaused() {
                        Log.d(TAG,"onSpeakPaused");
                    }

                    @Override
                    public void onSpeakResumed() {
                        Log.d(TAG,"onSpeakResumed");
                    }

                    @Override
                    public void onSpeakProgress(int i, int i1, int i2) {
                        Log.d(TAG,"onSpeakProgress");
                    }

                    @Override
                    public void onCompleted(SpeechError speechError) {
                        Log.d(TAG,"onCompleted");
                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {
                        Log.d(TAG,"onEvent");
                    }
                });
            }
        });

        findViewById(R.id.engine_btn_stop_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().stopSpeaking();
            }
        });

        findViewById(R.id.engine_btn_pause_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().pauseSpeaking();
            }
        });

        findViewById(R.id.engine_btn_resume_syn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().resumeSpeaking();
            }
        });

        findViewById(R.id.engine_btn_start_recong_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().startRecognizerDialog(XunfeiEngineTestActivity.this, new SpeechRecongnizerResult() {
                    @Override
                    public void onResult(String result) {
                        if(TextUtils.isEmpty(result)){
                            return;
                        }
                        Toast.makeText(XunfeiEngineTestActivity.this,"带框识别结果 " + result,Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"带框识别结果 " + result);
                    }

                    @Override
                    public void onError(String errorMsg) {

                    }
                });
            }
        });

        findViewById(R.id.engine_btn_start_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().setRecognizerParameter(SpeechConstant.VAD_EOS,"10000");

                XunfeiEngine.getInstance().startRecognizer(new RecognizerListener() {
                    @Override
                    public void onVolumeChanged(int i, byte[] bytes) {

                    }

                    @Override
                    public void onBeginOfSpeech() {

                    }

                    @Override
                    public void onEndOfSpeech() {

                    }

                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String result = XunfeiEngine.getInstance().handleResult(recognizerResult);
                        if(TextUtils.isEmpty(result)){
                            return;
                        }
                        Toast.makeText(XunfeiEngineTestActivity.this,"不带框识别结果 " + result,Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"不带框识别结果 " + result);
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                    }
                });
            }
        });

        findViewById(R.id.engine_btn_stop_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().stopRecognizer();
            }
        });

        findViewById(R.id.engine_btn_start_wake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().startWakeuper(new WakeuperListener() {
                    @Override
                    public void onBeginOfSpeech() {

                    }

                    @Override
                    public void onResult(WakeuperResult wakeuperResult) {
                        WakeResult wakeResult = XunfeiEngine.getInstance().handleWakeResult(wakeuperResult);
                        Log.d(TAG,"唤醒结果 " + wakeResult.toString());
                        Toast.makeText(XunfeiEngineTestActivity.this,"唤醒结果 " +
                                wakeResult.toString(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }

                    @Override
                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                    }

                    @Override
                    public void onVolumeChanged(int i) {

                    }
                });
            }
        });


        findViewById(R.id.engine_btn_stop_wake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XunfeiEngine.getInstance().stopWakeuper();
            }
        });

        XunfeiEngine.getInstance().setAiuiResultListn(new AiuiResultListn() {
            @Override
            public void onWakeUp() {
                Log.d(TAG,"onWakeUp ");
            }

            @Override
            public void onError(String errorMsg) {
                Log.d(TAG,"onError " + errorMsg);
            }

            @Override
            public void onAiuiState(int state) {
                Log.d(TAG,"onAiuiState " + state);
            }

            @Override
            public void onResult(String result) {
                editText.setText("");
                editText.setText("AIUI结果:  " + "\n" + result);
                Log.d(TAG,"String " + result);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        XunfeiEngine.getInstance().destory();
        XunfeiEngine.getInstance().stopWakeuper();
    }


}
