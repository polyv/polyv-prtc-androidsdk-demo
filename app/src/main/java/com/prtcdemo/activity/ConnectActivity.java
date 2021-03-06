package com.prtcdemo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polyv.prtc.sdkengine.IPRTCEngine;
import com.polyv.prtc.sdkengine.PRTCEnvHelper;
import com.prtcdemo.Application.PRTCApplication;
import com.prtcdemo.R;
import com.prtcdemo.utils.CommonUtils;
import com.prtcdemo.utils.PermissionUtils;
import com.prtcdemo.utils.StatusBarUtils;
import com.prtcdemo.utils.ToastUtils;
import com.polyv.prtc.sdkengine.define.PRTCMode;
import com.prtcdemo.utils.RTCFileUtil;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

import core.define.CoreEnvHelper;

import static core.define.CoreConstants.SdkMode.MODE_TRIAL;

public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "ConnectActivity";

    private EditText userEditText,roomEditText;
    private String mUserId = "";
    private String mRoomid = "";
    private String mAppid = "";
    private String mRoomToken = "";
    private View connectButton;
    private View exportButton;
    private ImageButton setButton;
    private TextView mTextSDKVersion;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private boolean mStartSuccess = false;
    private ImageView mAnimal;

    @Override
    @TargetApi(21)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "??????????????????????????????",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (requestCode != IPRTCEngine.SCREEN_CAPTURE_REQUEST_CODE) {
            Toast.makeText(this, "??????????????????????????????",
                    Toast.LENGTH_LONG).show();
            return;
        }
        IPRTCEngine.onScreenCaptureResult(data);
//        startRoomActivity();
        startLivingActivity();
//        startRoomTextureActivity();
//        startWebViewActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_connect);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);
        mAppid = preferences.getString(CommonUtils.APP_ID_TAG, CommonUtils.APP_ID);
        PRTCEnvHelper.setLogReport(true);
        mAnimal = findViewById(R.id.userporta);
        //((AnimationDrawable) mAnimal.getBackground()).start();

        setButton = findViewById(R.id.setting_btn);
        userEditText = findViewById(R.id.user_edittext);
        roomEditText = findViewById(R.id.room_edittext);
        roomEditText.requestFocus();
        mTextSDKVersion = findViewById(R.id.tv_sdk_version);
        mTextSDKVersion.setText(getString(R.string.app_name) + "\n" + IPRTCEngine.getSdkVersion());
        connectButton = findViewById(R.id.connect_button);
        exportButton = findViewById(R.id.log_output_button);
        exportButton.setVisibility(View.GONE);
        StatusBarUtils.setAndroidNativeLightStatusBar(this,true);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoomid = roomEditText.getText().toString();
                if (mRoomid.isEmpty()) {
                    ToastUtils.shortShow(getApplicationContext(), "??????id ????????????");
                } else {
                    //???????????????SDK????????????token
                    if (CoreEnvHelper.getSdkMode() == MODE_TRIAL) {
                        mRoomToken = "testoken";
                        Log.d(TAG, " appid " + mAppid);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            IPRTCEngine.requestScreenCapture(ConnectActivity.this);

                        } else {
                            startRoomActivity();
//                        startRoomTextureActivity();
//                            startWebViewActivity();
                        }
                    } else {
                        //??????????????????????????????????????????????????????userId,roomId,appId????????????????????????????????????token
                        ToastUtils.shortShow(ConnectActivity.this, "??????????????????????????????????????????token");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//
//                                String result = AppHttpUtil.getInstance().getTestRoomToken(mUserId, mRoomid, mAppid) ;
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Log.d(TAG, " gettokenresult "+ result) ;
//                                        if (result != null && result.length()>0) {
//                                            try {
//                                                JSONObject jsonObject = new JSONObject(result);
//                                                if (jsonObject != null) {
//                                                    JSONObject data = jsonObject.getJSONObject("data") ;
//                                                    if (data != null) {
//                                                        mRoomToken = data.getString("access_token" );
//                                                        Log.d(TAG, " token "+ mRoomToken) ;
//                                                        if (mRoomToken.length()>0) {
//                                                            IPRTCEngine.requestScreenCapture(ConnectActivity.this);
//                                                        }
//                                                    }
//                                                }
//                                            }catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//
//                                        }else {
//                                            ToastUtils.shortShow(getApplicationContext(),"??????token ??????");
//                                        }
//                                    }
//                                });
//                            }catch (Exception e) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        ToastUtils.shortShow(getApplicationContext(),"???????????? "+ e.getMessage());
//                                    }
//                                });
//
//                            }
                    }
//                    }).start() ;
                }
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RTCFileUtil.getInstance().copyFolder("/data/user/0/com.prtcdemo/app_bugly", "/sdcard/prtc/app_bugly");
                        RTCFileUtil.getInstance().copyFolder("/data/tombstones", "/sdcard/prtc/tombstones");
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.shortShow(ConnectActivity.this, "????????????");
                            }
                        });

                    }
                });
                thread.start();
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectActivity.this, NewSettingActivity.class);
                startActivity(intent);
            }
        });

        final RelativeLayout root = findViewById(R.id.id_rl_root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                        } else {
                            Log.e(TAG, "InputMethodManager is null !");
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermissionUtils.needsPermissions(this, permissions);
        Thread thread = new Thread(new CopyMixFileTask(this));
        thread.start();
    }

    private void startWebViewActivity(){
         Intent intent = new Intent(ConnectActivity.this, WebViewActivity.class);
         startActivity(intent);
         finish();
    }
    private void startRoomActivity() {
        if (!mStartSuccess) {
            mStartSuccess = true;
            final Intent intent = new Intent(ConnectActivity.this, RoomActivity.class);
            intent.putExtra("room_id", mRoomid);
            String autoGenUserId = "android_" + UUID.randomUUID().toString().replace("-", "");
            mUserId = PRTCApplication.getUserId() != null ? PRTCApplication.getUserId() : autoGenUserId;
            intent.putExtra("user_id", mUserId);
            intent.putExtra("app_id", mAppid);
            intent.putExtra("token", mRoomToken);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                    mStartSuccess = false;
                }
            }, 500);
        }
    }

    private void startLivingActivity() {
        if (!mStartSuccess) {
            mStartSuccess = true;
            final Intent intent = new Intent(ConnectActivity.this, RTCLiveActivity.class);
            intent.putExtra("room_id", mRoomid);
            String autoGenUserId = "android_" + UUID.randomUUID().toString().replace("-", "");
//            mUserId = PRTCApplication.getUserId() != null ? PRTCApplication.getUserId() : autoGenUserId;
            mUserId = !TextUtils.isEmpty(userEditText.getText()) ? userEditText.getText().toString() : autoGenUserId;
            intent.putExtra("user_id", mUserId);
            intent.putExtra("app_id", mAppid);
            intent.putExtra("token", mRoomToken);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    //finish();
                    mStartSuccess = false;
                }
            }, 500);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //((AnimationDrawable) mAnimal.getBackground()).start();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "activity onStop");
        super.onStop();
        //((AnimationDrawable) mAnimal.getBackground()).stop();
    }

    static class CopyMixFileTask implements Runnable {

        WeakReference<AppCompatActivity> context;

        public CopyMixFileTask(AppCompatActivity context) {
            this.context = new WeakReference<AppCompatActivity>(context);
        }

        @Override
        public void run() {
            if (context != null && context.get() != null) {
                String storageFileDir = context.get().getResources().getString(R.string.mix_file_dir);
                String storageFilePath = storageFileDir + File.separator + "dy.mp3";
                File fileStorage = new File(storageFilePath);
                boolean needCopy = false;
                if (!fileStorage.exists()) {
                    needCopy = true;
                }
                Handler handler = new Handler(Looper.getMainLooper());
                if (needCopy) {
                    File file = new File(storageFileDir);
                    if (!file.exists()) {//???????????????????????????????????????????????????
                        file.mkdirs();
                    }
                    readInputStream(storageFilePath, context.get().getResources().openRawResource(R.raw.dy));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (PRTCEnvHelper.getApplication() != null) {
                                ToastUtils.shortShow(PRTCEnvHelper.getApplication(), "default mix file copy success");
                            }
                        }
                    });
                } else {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtils.shortShow(PRTCEnvHelper.getApplication(),"mix file already exist");
//                        }
//                    });
                }
                context.clear();
                context = null;
            }
        }

        /**
         * ??????????????????????????????????????????
         *
         * @param storagePath ??????????????????
         * @param inputStream ?????????
         */
        public void readInputStream(String storagePath, InputStream inputStream) {
            File file = new File(storagePath);
            try {
                if (!file.exists()) {
                    // 1.??????????????????
                    FileOutputStream fos = new FileOutputStream(file);
                    // 2.??????????????????
                    byte[] buffer = new byte[1024];
                    // 3.???????????????
                    int length = 0;
                    while ((length = inputStream.read(buffer)) != -1) {// ????????????????????????buffer??????
                        // ???Buffer??????????????????outputStream?????????
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();// ???????????????
                    // 4.?????????
                    fos.close();
                    inputStream.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Log.d(TAG, "onBackPressed: destroy engine start");
//        PRTCApplication.getInstance().destroyEngine();
//        Log.d(TAG, "onBackPressed: destroy engine finish");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
