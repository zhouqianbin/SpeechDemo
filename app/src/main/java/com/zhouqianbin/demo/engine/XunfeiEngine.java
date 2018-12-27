package com.zhouqianbin.demo.engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.zhouqianbin.demo.utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @Copyright (C), 2018
 * @FileName: XunfeiEngine
 * @Author: 周千滨
 * @Date: 2018/12/24 10:13
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public class XunfeiEngine {

    private static final String TAG = XunfeiEngine.class.getSimpleName();
    private XunfeiEngine(){}
    public static XunfeiEngine getInstance(){
        return SingleHolder.INSTANCE;
    }
    private static class SingleHolder{
        public static final XunfeiEngine INSTANCE = new XunfeiEngine();
    }


    /**
     * 初始化引擎
     * @param context 上下文
     * @param appid   appid不解释
     */
    public void initEngine(final Context context, final String appid){
        SpeechUtility.createUtility(context, "appid=" + appid);
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG, "语音合成初始化失败,错误码：" + code);
                } else {
                    Log.d(TAG, "语音合成初始化成功");
                }
            }
        });

        mSpeechRecognizer = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG, "语音识别初始化失败,错误码：" + code);
                } else {
                    Log.d(TAG, "语音识别初始化成功");
                }
            }
        });

        mVoiceWakeuper = VoiceWakeuper.createWakeuper(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG,"唤醒初始化失败，错误码：" + code);
                }else {
                    Log.d(TAG,"唤醒初始化成功");
                }
            }
        });

        //创建AIUIAgent
        mAiuiAgent = AIUIAgent.createAgent(context,
                getAIUIParams(context),mAIUIListener);

    }



    /*--------------------------语音合成方法-------------------------------*/

    /**
     * 语音合成对象
     */
    private SpeechSynthesizer mSpeechSynthesizer;

    /**
     * 设置合成的参数
     * @param key   参数key
     * @param value  参数值
     */
    public void setSynthesizerParameter(String key,String value){
        Log.d(TAG,"setSynthesizerParameter " + key + " " + value);
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.setParameter(key,value);
    }

    /**
     * 开始合成
     * @param text   合成的文本
     */
    public void startSpeak(String text){
        Log.d(TAG,"startSpeak " + text);
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.startSpeaking(text,null);
    }

    /**
     * 开始合成
     * @param text   合成的文本
     * @param synthesizerListener  合成的状态
     */
    public void startSpeak(String text, SynthesizerListener synthesizerListener){
        Log.d(TAG,"startSpeak " + text);
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.startSpeaking(text,synthesizerListener);
    }

    /**
     * 是否正在合成
     * @return
     */
    public boolean isSpeaking(){
        Log.d(TAG,"isSpeaking ");
        if(null == mSpeechSynthesizer){
            return false;
        }
        return mSpeechSynthesizer.isSpeaking();
    }

    /**
     * 停止语音
     */
    public void stopSpeaking(){
        Log.d(TAG,"stopSpeaking ");
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.stopSpeaking();
    }

    /**
     * 对应的继续播放
     * 暂停播放
     */
    public void pauseSpeaking(){
        Log.d(TAG,"pauseSpeaking ");
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.pauseSpeaking();
    }

    /**
     * 恢复播放
     */
    public void resumeSpeaking(){
        Log.d(TAG,"resumeSpeaking ");
        if(null == mSpeechSynthesizer){
            return;
        }
        mSpeechSynthesizer.resumeSpeaking();
    }

    /**
     * 合成到文件 合成文本到一个音频文件，不播放
     * @param text 合成的文本
     * @param uri   合成的路径
     * @return
     */
    public int synthesizeToUri(String text, String uri,SynthesizerListener mSynthesizerListener ){
        Log.d(TAG,"synthesizeToUri ");
        if(null == mSpeechSynthesizer){
            return 0;
        }
        return mSpeechSynthesizer.synthesizeToUri(text,uri,mSynthesizerListener);
    }




    /*--------------------------语音听写-------------------------------*/

    /**
     * 语音听写对象
     */
    private SpeechRecognizer mSpeechRecognizer;

    /**
     * 语音听写对话框
     */
    private RecognizerDialog mRecognizerDialog;

    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    /**
     * 设置语音听写参数
     * @param key
     * @param value
     */
    public void setRecognizerParameter(String key,String value){
        Log.d(TAG,"setmRecognizerParameter " + key + " " + value);
        if(null == mSpeechRecognizer){
            return;
        }
        mSpeechRecognizer.setParameter(key,value);
    }


    /**
     * 开始听写（不带识别框）
     */
    public void startRecognizer(RecognizerListener recognizerListener){
        Log.d(TAG,"startRecognizer " );
        if(null == mSpeechRecognizer){
            return;
        }
        chageAiuiState();
        mSpeechRecognizer.startListening(recognizerListener);
    }

    /**
     * 开始听写（带识别框）
     */
    public void startRecognizerDialog(Context context, final SpeechRecongnizerResult recongnizerResult){
        Log.d(TAG,"startRecognizerDialog " );
        chageAiuiState();
        mRecognizerDialog = new RecognizerDialog(context, null);
        mRecognizerDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String result = handleResult(recognizerResult);
                if(null != recongnizerResult){
                    recongnizerResult.onResult(result);
                    mRecognizerDialog.dismiss();
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                if(null != recongnizerResult){
                    recongnizerResult.onError(speechError.getErrorDescription());
                }
            }
        });
        mRecognizerDialog.show();
    }


    /**
     * 停止识别
     */
    public void stopRecognizer(){
        Log.d(TAG,"stopRecognizer " );
        if(null == mSpeechRecognizer){
            return;
        }
        mSpeechRecognizer.stopListening();
    }

    /**
     * 是否正在识别
     * @return
     */
    public boolean isRecognizer(){
        Log.d(TAG,"isRecognizer " );
        if(null == mSpeechRecognizer){
           return false;
        }
        return mSpeechRecognizer.isListening();
    }


    /**
     * 处理识别结果
     * @param results
     * @param
     */
    public String handleResult(RecognizerResult results) {
        Log.d(TAG,"handleResult " + results);
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

        //写入数据
        try {
            // 在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
            String params = "data_type=text,tag=text-tag";
            byte[] textData = resultBuffer.toString().getBytes("utf-8");
            AIUIMessage write = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
            mAiuiAgent.sendMessage(write);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return resultBuffer.toString();
    }


   /*--------------------------语音唤醒-------------------------------*/

    /**
     * 语音唤醒对象
     */
    private VoiceWakeuper mVoiceWakeuper;

    /**
     * 设置唤醒参数
     * @param key
     * @param value
     */
    public void setWakeParame(String key,String value){
        if(null == mVoiceWakeuper){
            return;
        }
        mVoiceWakeuper.setParameter(key,value);
    }


    /**
     * 获取唤醒资源
     * @param context
     * @param appid
     * @return
     */
    public String getResource(Context context,String appid){
         String resPath = ResourceUtil.
                generateResourcePath(context,
                        ResourceUtil.RESOURCE_TYPE.assets,
                        "ivw/"+ appid+".jet");
         return resPath;
    }

    /**
     * 开启唤醒功能
     */
    public void startWakeuper(WakeuperListener wakeuperListener) {
        if(null == mVoiceWakeuper){
            return;
        }
        mVoiceWakeuper.startListening(wakeuperListener);
    }

    /**
     * 停止唤醒
     */
    public void stopWakeuper() {
        if(null == mVoiceWakeuper){
            return;
        }
        mVoiceWakeuper.stopListening();
    }

    /**
     * 处理唤醒结果
     * @param wakeuperResult
     */
    public WakeResult handleWakeResult(WakeuperResult wakeuperResult){
        Log.d(TAG,"handleWakeResult " + wakeuperResult);
        WakeResult wakeResult = new WakeResult();
        String text = wakeuperResult.getResultString();
        JSONObject object;
        try {
            object = new JSONObject(text);
            wakeResult.setSst(object.optString("sst"));
            wakeResult.setId(object.optString("id"));
            wakeResult.setScore(object.optString("score"));
            wakeResult.setBos(object.optString("bos"));
            wakeResult.setEos(object.optString("eos"));
            return wakeResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


   /*--------------------------AIUI------------------------------*/

   private AIUIAgent mAiuiAgent;

   private String getAIUIParams(Context context) {
        String params = "";
        AssetManager assetManager = context.getResources().getAssets();
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

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            if(null == mAiuiResultListn){
                return;
            }
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    //唤醒事件
                    Log.d( TAG,  "on event: "+ event.eventType );
                    mAiuiResultListn.onWakeUp();
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    //结果解析事件
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);
                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                Log.d( TAG, "解析得到语义结果 " + resultStr );
                                JSONObject obj = new JSONObject(resultStr);
                                JSONObject user = obj.getJSONObject("answer");
                                String result = user.getString("text");
                                Log.d( TAG, "获取结果内容 " + result );
                                mAiuiResultListn.onResult(result);
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        mAiuiResultListn.onResult("我还不知道您说什么,您可以到技能商店添加应用哦");
                    }
                } break;

                case AIUIConstant.EVENT_ERROR: {
                    //错误事件
                    Log.d( TAG,  "on event: "+ event.eventType );
                    Log.d(TAG, "错误: "+event.arg1+"\n"+event.info );
                    mAiuiResultListn.onError(event.arg1+"\n"+event.info );
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
                    mAiuiResultListn.onAiuiState(event.arg1);
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

   private AiuiResultListn mAiuiResultListn;
   public void setAiuiResultListn(AiuiResultListn aiuiResultListn){
       this.mAiuiResultListn = aiuiResultListn;
   }


    /**
     * 改变AIUI状态，使其接收写入的内容，
     * 但是到了指定的时间后自动关闭，可再
     * 设置参数中设置超时时间
     */
    private void chageAiuiState(){
        AIUIMessage wakeupMsg = new AIUIMessage(
                AIUIConstant.CMD_WAKEUP,
                0,
                0,
                "",
                null);
        mAiuiAgent.sendMessage(wakeupMsg);
    }


    /**
     * 释放资源
     */
    public void destory(){
        if(null != mSpeechSynthesizer){
            mSpeechSynthesizer.stopSpeaking();
            mSpeechSynthesizer.destroy();
            mSpeechSynthesizer = null;
        }
        if(null != mSpeechRecognizer){
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        if (mVoiceWakeuper != null) {
            mVoiceWakeuper.destroy();
            mVoiceWakeuper = null;
        }
        if(null != mAiuiAgent){
            mAiuiAgent.destroy();
            mAiuiAgent = null;
        }
    }


}
