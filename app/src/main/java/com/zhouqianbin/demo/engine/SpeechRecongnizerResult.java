package com.zhouqianbin.demo.engine;

/**
 * @Copyright (C), 2018
 * @FileName: SpeechRecongnizerResult
 * @Author: 周千滨
 * @Date: 2018/12/24 17:18
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public interface SpeechRecongnizerResult {

    void onResult(String result);

    void onError(String errorMsg);
}
