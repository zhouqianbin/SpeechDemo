package com.zhouqianbin.demo.engine;

/**
 * @Copyright (C), 2018
 * @FileName: AiuiResultListn
 * @Author: 周千滨
 * @Date: 2018/12/27 14:53
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public interface AiuiResultListn {

    void onWakeUp();

    void onError(String errorMsg);

    void onAiuiState(int state);

    void onResult(String result);

}
