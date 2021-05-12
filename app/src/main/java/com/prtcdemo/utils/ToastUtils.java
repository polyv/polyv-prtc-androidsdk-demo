package com.prtcdemo.utils;

import android.content.Context;
import android.widget.Toast;

import com.polyv.prtc.sdkengine.PRTCEnvHelper;

public class ToastUtils {
    public static void shortShow(Context context, String msg) {
        if(PRTCEnvHelper.getApplication() != null)
        Toast.makeText(PRTCEnvHelper.getApplication().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void longShow(Context context, String msg) {
        if(PRTCEnvHelper.getApplication() != null)
        Toast.makeText(PRTCEnvHelper.getApplication().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
