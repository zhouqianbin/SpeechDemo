package com.zhouqianbin.demo.ui;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 语音唤醒
 */
public class WakeUpActivity extends AppCompatActivity {

    private static final String TAG = WakeUpActivity.class.getSimpleName();
    //唤醒的阈值，就相当于门限值，当用户输入的语音的置信度大于这一个值的时候，才被认定为成功唤醒。
    private int curThresh = 1000;
    //是否持续唤醒
    private String keep_alive = "1";
    /**
     * 闭环优化网络模式有三种：
     * 模式0：关闭闭环优化功能
     *
     * 模式1：开启闭环优化功能，允许上传优化数据。需开发者自行管理优化资源。
     * sdk提供相应的查询和下载接口，请开发者参考API文档，具体使用请参考本示例
     * queryResource及downloadResource方法；
     *
     * 模式2：开启闭环优化功能，允许上传优化数据及启动唤醒时进行资源查询下载；
     * 本示例为方便开发者使用仅展示模式0和模式2；
     */
    private String ivwNetMode = "0";
    // 语音唤醒对象
    private VoiceWakeuper mVoiceWakeuper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up);

        SpeechUtility.createUtility(this, "appid=" + AppSetting.APP_ID);

        mVoiceWakeuper = VoiceWakeuper.createWakeuper(this, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG,"初始化失败，错误码：" + code);
                    ToastUtils.showToast(WakeUpActivity.this,"初始化失败，错误码：" + code);
                }else {
                    Log.d(TAG,"初始化成功");
                    ToastUtils.showToast(WakeUpActivity.this,"初始化成功");
                }
            }
        });

        initParame();

        findViewById(R.id.wake_up_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWakeuper();
            }
        });

        findViewById(R.id.wake_up_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWakeuper();
            }
        });

    }

    private void initParame() {
        //非空判断，防止因空指针使程序崩溃
        mVoiceWakeuper = VoiceWakeuper.getWakeuper();
        if(mVoiceWakeuper != null) {
            // 清空参数
            mVoiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mVoiceWakeuper.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ curThresh);
            // 设置唤醒模式
            mVoiceWakeuper.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mVoiceWakeuper.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mVoiceWakeuper.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mVoiceWakeuper.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            /*mVoiceWakeuper.setParameter( SpeechConstant.IVW_AUDIO_PATH,
                    Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
            mVoiceWakeuper.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );*/
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
        }
    }

    /**
     * 开启唤醒功能
     */
    public void startWakeuper() {
        mVoiceWakeuper.startListening(new MyWakeuperListener());
    }


    /**
     * 停止唤醒
     */
    public void stopWakeuper() {
        mVoiceWakeuper.stopListening();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁合成对象
        mVoiceWakeuper = VoiceWakeuper.getWakeuper();
        if (mVoiceWakeuper != null) {
            mVoiceWakeuper.destroy();
        }
    }

    /**
     * 获取唤醒词功能
     * @return 返回文件位置
     */
    private String getResource() {
        final String resPath = ResourceUtil.
                generateResourcePath(this,
                        ResourceUtil.RESOURCE_TYPE.assets,
                        "ivw/"+ AppSetting.APP_ID+".jet");
        return resPath;
    }


    private class MyWakeuperListener implements WakeuperListener {
        //开始说话
        @Override
        public void onBeginOfSpeech() {

        }

        //错误码返回
        @Override
        public void onError(SpeechError arg0) {

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }

        @Override
        public void onVolumeChanged(int i) {

        }

        @Override
        public void onResult(WakeuperResult result) {
            if (!"1".equalsIgnoreCase(keep_alive)) {
                //setRadioEnable(true);
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                Log.d(TAG,"唤醒结果 " + buffer.toString());
                ToastUtils.showToast(WakeUpActivity.this, "唤醒结果 " + buffer.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
