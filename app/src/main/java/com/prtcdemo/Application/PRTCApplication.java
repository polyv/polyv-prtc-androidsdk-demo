package com.prtcdemo.Application;

import com.prtcdemo.BuildConfig;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.polyv.prtc.sdkengine.IPRTCEngine;
import com.polyv.prtc.sdkengine.PRTCEnvHelper;
import com.tencent.bugly.crashreport.CrashReport;
import com.polyv.prtc.sdkengine.define.PRTCLogLevel;
import com.polyv.prtc.sdkengine.define.PRTCMode;
import com.prtcdemo.utils.CommonUtils;
import com.prtcdemo.utils.UiHelper;

import core.define.CoreConstants;

public class PRTCApplication extends Application {

    private static final String TAG = "PRTCApplication";
    private static Context sContext;
    private static String sUserId;
    private static IPRTCEngine rtcSdkEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: " + this);
        if (TextUtils.equals(getCurrentProcessName(this), getPackageName())) {
//            Log.d(TAG, "init: ");
            init();//判断成功后才执行初始化代码
        }
    }

    private void init(){
        sContext = this;
        PRTCEnvHelper.initEnv(getApplicationContext());
        PRTCEnvHelper.setWriteToLogCat(true);
        PRTCEnvHelper.setLogReport(true);
        PRTCEnvHelper.setPushCodec(CoreConstants.PushCodec.PUSH_ENCODE_MODE_H264);
        PRTCEnvHelper.setLogLevel(PRTCLogLevel.LOG_LEVEL_INFO.ordinal());
        PRTCEnvHelper.setSdkMode(PRTCMode.MODE_TRIAL.ordinal());
        PRTCEnvHelper.setReConnectTimes(60);
        PRTCEnvHelper.setTokenSecKey(CommonUtils.APP_KEY);
        //推流方向
        //PRTCEnvHelper.setPushOrientation(PRTCPushOrientation.PUSH_LANDSCAPE_MODE);
        //视频输出模式
        //PRTCEnvHelper.setVideoOutputOrientation(PRTCVideoOutputOrientationMode.VIDEO_OUTPUT_FIXED_LANDSCAPE_MODE);
        //私有化部署
//        PRTCEnvHelper.setPrivateDeploy(true);
//        PRTCEnvHelper.setPrivateDeployRoomURL("wss://xxx:5005/ws");
        //无限重连
//        PRTCEnvHelper.setReConnectTimes(-1);
        //默认vp8编码，可以改成h264
//        PRTCEnvHelper.setEncodeMode(PRTCPushEncode.PUSH_ENCODE_MODE_H264);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        CommonUtils.mItemWidth = (outMetrics.widthPixels - UiHelper.dipToPx(this, 15)) / 3;
        CommonUtils.mItemHeight = CommonUtils.mItemWidth;
        CrashReport.initCrashReport(getApplicationContext(), "9a51ae062a", true);
//        BlockCanary.install(this, new AppContext()).start();
    }

    //参数设置
    public class AppContext extends BlockCanaryContext {
        private static final String TAG = "AppContext";

        @Override
        public String provideQualifier() {
            String qualifier = "";
            try {
                PackageInfo info = PRTCApplication.getAppContext().getPackageManager()
                        .getPackageInfo(PRTCApplication.getAppContext().getPackageName(), 0);
                qualifier += info.versionCode + "_" + info.versionName + "_YYB";
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "provideQualifier exception", e);
            }
            return qualifier;
        }

        @Override
        public int provideBlockThreshold() {
            return 500;
        }

        @Override
        public boolean displayNotification() {
            return BuildConfig.DEBUG;
        }

        @Override
        public boolean stopWhenDebugging() {
            return false;
        }
    }

    public static Context getAppContext() {
        return sContext;
    }

    public static PRTCApplication getInstance() {
        return (PRTCApplication) sContext;
    }

    public static String getUserId() {
        return sUserId;
    }

    public static void setUserId(String userId) {
        sUserId = userId;
    }

    private String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
