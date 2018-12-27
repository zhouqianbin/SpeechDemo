package com.zhouqianbin.demo.ui;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.zhouqianbin.demo.AppSetting;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.utils.JsonParser;
import com.zhouqianbin.demo.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * AIUI
 */
public class AiuiActivity extends AppCompatActivity {

    private static final String TAG = AiuiActivity.class.getSimpleName();
    private RecognizerDialog mRecognizerDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private AIUIAgent mAIUIAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aiui);

        SpeechUtility.createUtility(this, "appid=" + AppSetting.APP_ID);

        //创建AIUIAgent
        mAIUIAgent = AIUIAgent.createAgent(this,
                getAIUIParams(),mAIUIListener);


        findViewById(R.id.aiui_btn_start_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
                if( AIUIConstant.STATE_WORKING != mAIUIState ){
                    AIUIMessage wakeupMsg = new AIUIMessage(
                            AIUIConstant.CMD_WAKEUP,
                            0,
                            0,
                            "",
                            null);
                    mAIUIAgent.sendMessage(wakeupMsg);
                }
                mRecognizerDialog = new RecognizerDialog(AiuiActivity.this,null);
                mRecognizerDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        handleResult(recognizerResult);
                    }

                    @Override
                    public void onError(SpeechError speechError) {

                    }
                });
                mRecognizerDialog.show();
            }
        });


    }

    private synchronized void handleResult(RecognizerResult results) {
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

        try {
            // 在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
            String params = "data_type=text,tag=text-tag";
            byte[] textData = resultBuffer.toString().getBytes("utf-8");
            AIUIMessage write = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
            mAIUIAgent.sendMessage(write);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    private int mAIUIState;
    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    //唤醒事件
                    Log.d( TAG,  "on event: "+ event.eventType );
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    //结果解析事件
                    Log.d(TAG,"info " + event.info);
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        Log.d(TAG,"bizParamJson " + bizParamJson.toString());
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        Log.d(TAG,"data " + data.toString());
                        JSONObject params = data.getJSONObject("params");
                        Log.d(TAG,"params " + params.toString());
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);
                        Log.d(TAG,"content " + content.toString());

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                            Log.d(TAG,"cntJson " + cntJson.toString());
                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                Log.d( TAG, "解析得到语义结果 " + resultStr );
                                JSONObject obj = new JSONObject(resultStr);
                                JSONObject user = obj.getJSONObject("answer");
                                String result = user.getString("text");
                                Log.d( TAG, "获取结果内容 " + result );

                                ToastUtils.showToast(AiuiActivity.this,"获取结果内容" + result);
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }



                } break;

                case AIUIConstant.EVENT_ERROR: {
                    //错误事件
                    Log.d( TAG,  "on event: "+ event.eventType );
                    Log.d(TAG, "错误: "+event.arg1+"\n"+event.info );
                } break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        //语音前端点
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        //语音后端点
                    }
                } break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.d( TAG,  "on event: "+ event.eventType );
                    //开始录音
                } break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.d( TAG,  "on event: "+ event.eventType );
                    // 停止录音
                } break;

                case AIUIConstant.EVENT_STATE: {
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == event.arg1) {
                        // 闲置状态，AIUI未开启
                    } else if (AIUIConstant.STATE_READY == event.arg1) {
                        // AIUI已就绪，等待唤醒
                    } else if (AIUIConstant.STATE_WORKING == event.arg1) {
                        // AIUI工作中，可进行交互
                    }
                } break;

                default:
                    break;
            }
        }
    };


}
