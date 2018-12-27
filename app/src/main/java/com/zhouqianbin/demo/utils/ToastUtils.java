package com.zhouqianbin.demo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @Copyright (C), 2018, 漳州科能电器有限公司
 * @FileName: ToastUtils
 * @Author: 周千滨
 * @Date: 2018/12/22 14:56
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public class ToastUtils {

    private static Toast toast;

    /**
     * 强大的吐司，能够连续弹的吐司
     * @param text
     */
    public static void showToast(Context context, String text){
        if(toast==null){
            toast = Toast.makeText(context, text,0);
        }else {
            toast.setText(text);//如果不为空，则直接改变当前toast的文本
        }
        toast.show();
    }

}
