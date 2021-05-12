package com.prtcdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polyv.prtc.sdkengine.IPRTCEngine;
import com.polyv.prtc.sdkengine.PRTCEnvHelper;
import com.polyv.prtc.sdkengine.define.PRTCAuthInfo;
import com.polyv.prtc.sdkengine.define.PRTCCaptureMode;
import com.polyv.prtc.sdkengine.define.PRTCChannelProfile;
import com.polyv.prtc.sdkengine.define.PRTCClientRole;
import com.polyv.prtc.sdkengine.define.PRTCMediaType;
import com.polyv.prtc.sdkengine.define.PRTCMixProfile;
import com.polyv.prtc.sdkengine.define.PRTCTextureViewRenderer;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewRenderer;
import com.polyv.prtc.sdkengine.define.PRTCScaleType;
import com.polyv.prtc.sdkengine.define.PRTCStreamInfo;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewGroup;
import com.polyv.prtc.sdkengine.define.PRTCTrackType;
import com.polyv.prtc.sdkengine.define.PRTCVideoProfile;
import com.polyv.prtc.sdkengine.listener.IPRTCEngineEventHandler;
import com.polyv.prtc.sdkengine.listener.IPRTCRecordListener;
import com.polyv.prtc.sdkengine.openinterface.IPRTCDataReceiver;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.polyv.prtc.sdkengine.define.PRTCAudioDevice;
import com.polyv.prtc.sdkengine.define.PRTCErrorCode;
import com.polyv.prtc.sdkengine.define.PRTCMediaServiceStatus;
import com.polyv.prtc.sdkengine.define.PRTCNetWorkQuality;
import com.polyv.prtc.sdkengine.define.PRTCStreamStatus;
import com.polyv.prtc.sdkengine.define.PRTCStreamType;
import com.polyv.prtc.sdkengine.listener.IPRTCEngineBaseEvent;
import com.polyv.prtc.sdkengine.openinterface.IPRTCDataProvider;
import com.polyv.prtc.sdkengine.openinterface.IPRTCScreenShot;
import com.prtcdemo.R;
import com.prtcdemo.adpter.RemoteHasViewVideoAdapter;
import com.prtcdemo.service.RTCForeGroundService;
import com.prtcdemo.utils.CommonUtils;
import com.prtcdemo.utils.StatusBarUtils;
import com.prtcdemo.utils.ToastUtils;
import com.prtcdemo.utils.VideoProfilePopupWindow;
import com.prtcdemo.view.RTCVideoViewInfo;

import org.wrtca.record.RtcRecordManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.polyv.prtc.sdkengine.define.PRTCErrorCode.NET_ERR_CODE_OK;
import static com.polyv.prtc.sdkengine.define.PRTCMediaType.MEDIA_TYPE_SCREEN;
import static com.polyv.prtc.sdkengine.define.PRTCMediaType.MEDIA_TYPE_VIDEO;

/**
 * @author ciel
 * @create 2020/7/2
 * @Describe
 */
public class RTCLiveTextureActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, CameraDialog.CameraDialogParent, RemoteHasViewVideoAdapter.SwapInterface {
    private static final String TAG = "RTCLiveTextureActivity";
    private final String mBucket = "urtc-test";
    private final String mRegion = "cn-bj";
    private final int COL_SIZE_P = 3;
    private final int COL_SIZE_L = 6;

    private String mUserid = "test001";
    private String mRoomid = "urtc1";
    private String mRoomToken = "test token";
    private String mAppid = "";

    private boolean mSwitchCamera = false;
    private boolean mMuteMic = false;
    private boolean mMuteVideo = false;
    private boolean mSpeakerOn = true;
    private boolean mMirror = false;
    private boolean isScreenCaptureSupport = true;
    private boolean mCameraEnable = true;
    private boolean mMicEnable = true;
    private boolean mScreenEnable = false;
    private boolean mVideoIsPublished = false;
    private boolean mScreenIsPublished = false;
    private boolean mIsRemoteRecording = false;
    private boolean mIsLocalRecording = false;
    private boolean mAtomOpStart = false;
    private boolean mIsMixing = false;
    private boolean mLocalViewFullScreen = false;
    private boolean mRemoteVideoMute;
    private boolean mRemoteAudioMute;
    private boolean mIsPreview = false;
    @CommonUtils.PubScribeMode
    private int mPublishMode;
    @CommonUtils.PubScribeMode
    private int mScribeMode;
    private int mVideoProfileSelect;
    private int localViewWidth;
    private int localViewHeight;
    private int screenWidth;
    private int screenHeight;
    private boolean mExtendCameraCapture;
    private int mExtendVideoFormat;
    private int mUVCCameraFormat;
    private int mRTCVideoFormat;

    IPRTCEngine sdkEngine = null;
    private PRTCChannelProfile mClass;
    private PRTCStreamInfo mLocalStreamInfo;
    private PRTCStreamInfo mSwapStreamInfo;
    private PRTCAudioDevice defaultAudioDevice;
    private List<PRTCStreamInfo> mSteamList;
    private List<String> mResolutionOption = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private TextureView mLocalVideoView = null; //TextureView
    private PRTCTextureViewRenderer mLocalRender;
    private PRTCMediaType mPublishMediaType;

    private GridLayoutManager gridLayoutManager;
    private RemoteHasViewVideoAdapter mVideoAdapter;
//    private RemoteNoCacheTextureVideoAdapter mVideoAdapter;
    private RecyclerView mRemoteGridView = null;
    private DrawerLayout mDrawer;
    private ViewGroup mDrawerContent;
    private FrameLayout mDrawerMenu;
    private LinearLayout mTitleBar;
    private LinearLayout mToolBar;
    private ImageButton mImgBtnMore;
    private ImageButton mImgBtnSwitchCam;
    private ImageButton mImgBtnMuteMic;
    private ImageButton mImgBtnMuteVideo;
    private ImageButton mImgBtnEndCall;
    private ImageButton mImgBtnMuteSpeaker;
    private ImageButton mImgBtnMirror;
    private Chronometer timeShow;
    private ImageView mImgMix;
    private TextView mTextMix;
    private ImageView mImgLocalRecord;
    private TextView mTextLocalRecord;
    private ImageView mImgScreenshot;
    private TextView mTextScreenshot;
    private ImageView mImgRemoteRecord;
    private TextView mTextRemoteRecord;
    private ImageView mImgManualPubVideo;
    private TextView mTextManualPubVideo;
    private ImageView mImgManualPubScreen;
    private TextView mTextManualPubScreen;
    private ImageView mImgLocalMixSound;
    private TextView mTextLocalMixSound;
    private ImageView mImgRemoteMixSound;
    private TextView mTextRemoteMixSound;
    private ImageView mImgControlMixSound;
    private TextView mTextControlMixSound;
    private TextView mTextRoomId;
    private TextView mTextUserId;
    private ImageView mImgPreview;
    private TextView mTextPreview;
    private TextView mTextResolution;
    private VideoProfilePopupWindow mResolutionPopupWindow;
    //音量图片
    private ImageView mImgSoundVolume = null;
    private ImageView mImgMicSts = null;
    //UVCCamera
    private USBMonitor mUSBMonitor = null;
    private UVCCamera mUVCCamera = null;
    private final Object mSync = new Object();
    private boolean isActive, isPreview;
    private boolean mLeaveRoomFlag;
    private boolean mIsLocalMixingSound = false;
    private boolean mIsRemoteMixingSound = false;
    private boolean mIsPauseMixingSound = false;

    //外部摄像数据读取
    private ArrayBlockingQueue<ByteBuffer> mQueueByteBuffer = new ArrayBlockingQueue(8);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);
        sdkEngine = IPRTCEngine.create(eventListener);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;

        setContentView(R.layout.activity_living_texture);
        mVideoProfileSelect = preferences.getInt(CommonUtils.videoprofile, CommonUtils.videoprofilesel);
        mRemoteGridView = findViewById(R.id.remoteGridView);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_L);
        } else {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_P);
        }
        mRemoteGridView.setLayoutManager(gridLayoutManager);
        //点击remoteView交换
        mVideoAdapter = new RemoteHasViewVideoAdapter(this,sdkEngine,this);
//        mVideoAdapter = new RemoteNoCacheTextureVideoAdapter(this,sdkEngine,this);
        //点击remoteView截图
        //mVideoAdapter = new RemoteHasViewVideoAdapter(this,sdkEngine,null);
//        mVideoAdapter.setRemoveRemoteStreamReceiver(mRemoveRemoteStreamReceiver);
        mRemoteGridView.setAdapter(mVideoAdapter);

        mLocalVideoView = findViewById(R.id.localvideoview);
        mLocalRender = new PRTCTextureViewRenderer(mLocalVideoView);
        //Surfaceview 打开注释
        //mLocalVideoView.init(true);
        //mLocalVideoView.setZOrderMediaOverlay(false);
        //mLocalVideoView.setMirror(true);
        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.setScrimColor(0x00ffffff);
        mDrawerContent = findViewById(R.id.drawer_content);
        mDrawerMenu = findViewById(R.id.menu_drawer);
        timeShow = findViewById(R.id.timer);
        mDrawerContent.setPadding(0,StatusBarUtils.getStatusBarHeight(this),0,0);
        mTitleBar= findViewById(R.id.title_bar);
        mToolBar = findViewById(R.id.tool_bar);
        mImgBtnMore = findViewById(R.id.img_btn_more);
        mImgBtnSwitchCam = findViewById(R.id.img_btn_switch_camera);
        mImgBtnMuteMic = findViewById(R.id.img_btn_toggle_mic);
        mImgBtnMuteVideo = findViewById(R.id.img_btn_toggle_video);
        mImgBtnMuteSpeaker = findViewById(R.id.img_btn_speaker);
        mImgBtnMirror = findViewById(R.id.img_btn_mirror);
        mImgBtnEndCall = findViewById(R.id.img_btn_endcall);
        StatusBarUtils.setColor(this,getResources().getColor(R.color.color_7F04A5EB));
        mImgMix = findViewById(R.id.mix_pic);
        mTextMix = findViewById(R.id.mix_text);
        mImgLocalRecord = findViewById(R.id.local_record_pic);
        mTextLocalRecord = findViewById(R.id.local_record_text);
        mImgScreenshot = findViewById(R.id.screenshot_pic);
        mTextScreenshot = findViewById(R.id.screenshot_text);
        mImgRemoteRecord = findViewById(R.id.remote_record_pic);
        mTextRemoteRecord = findViewById(R.id.remote_record_text);
        mImgManualPubVideo = findViewById(R.id.manual_publish_pic);
        mTextManualPubVideo = findViewById(R.id.manual_publish_text);
        mImgManualPubScreen = findViewById(R.id.manual_publish_screen_pic);
        mTextManualPubScreen = findViewById(R.id.manual_publish_screen_text);
        mImgSoundVolume = findViewById(R.id.sound_volume_img);
        mImgMicSts = findViewById(R.id.mic_status_img);
        mTextResolution = findViewById(R.id.resolution_text);
        mImgPreview = findViewById(R.id.preview_pic);
        mTextPreview = findViewById(R.id.preview_text);
        mImgLocalMixSound = findViewById(R.id.local_mix_pic);
        mTextLocalMixSound = findViewById(R.id.local_mix_text);
        mImgRemoteMixSound = findViewById(R.id.remote_mix_pic);
        mTextRemoteMixSound = findViewById(R.id.remote_mix_text);
        mImgControlMixSound = findViewById(R.id.control_mix_pic);
        mTextControlMixSound = findViewById(R.id.control_mix_text);

        mUserid = getIntent().getStringExtra("user_id");
        mRoomid = getIntent().getStringExtra("room_id");
        mRoomToken = getIntent().getStringExtra("token");
        mAppid = getIntent().getStringExtra("app_id");
        isScreenCaptureSupport = PRTCEnvHelper.isSupportScreenCapture();
        mCameraEnable = preferences.getBoolean(CommonUtils.CAMERA_ENABLE, CommonUtils.CAMERA_ON);
        mMicEnable = preferences.getBoolean(CommonUtils.MIC_ENABLE, CommonUtils.MIC_ON);
        mScreenEnable = preferences.getBoolean(CommonUtils.SCREEN_ENABLE, CommonUtils.SCREEN_OFF);
        int classType = preferences.getInt(CommonUtils.SDK_CLASS_TYPE, PRTCChannelProfile.ROOM_SMALL.ordinal());
        mClass = PRTCChannelProfile.valueOf(classType);
        mPublishMode = preferences.getInt(CommonUtils.PUBLISH_MODE, CommonUtils.AUTO_MODE);
        mScribeMode = preferences.getInt(CommonUtils.SUBSCRIBE_MODE, CommonUtils.AUTO_MODE);
        mExtendCameraCapture = preferences.getBoolean(CommonUtils.CAMERA_CAPTURE_MODE, false);
        mExtendVideoFormat = preferences.getInt(CommonUtils.EXTEND_CAMERA_VIDEO_FORMAT, CommonUtils.i420_format);
        updateVideoFormat(mExtendVideoFormat);
        mSteamList = new ArrayList<>();

        //房间号
        mTextRoomId = findViewById(R.id.roomid_text);
        mTextRoomId.setText("房间号:" + mRoomid);
        mMirror = PRTCEnvHelper.isFrontCameraMirror();
        mImgBtnMirror.setImageResource(mMirror ? R.mipmap.mirror_on :
                R.mipmap.mirror);
        //分辨率选择菜单
        String[] resolutions = getResources().getStringArray(R.array.videoResolutions);
        mResolutionOption.addAll(Arrays.asList(resolutions));

        //用户ID
        mTextUserId = findViewById(R.id.userid_text);
        mTextUserId.setText("用户ID:" + mUserid);

        Log.d(TAG, " Camera enable is: " + mCameraEnable + " Mic enable is: " + mMicEnable + " ScreenShare enable is: " + mScreenEnable);
        if (!mScreenEnable && !mCameraEnable && mMicEnable) {
            sdkEngine.setAudioOnlyMode(true);
        }
        else {
            sdkEngine.setAudioOnlyMode(false);
        }
        sdkEngine.configLocalCameraPublish(mCameraEnable);
        sdkEngine.configLocalAudioPublish(mMicEnable);
        if (isScreenCaptureSupport) {
            sdkEngine.configLocalScreenPublish(mScreenEnable);
        }
        else {
            sdkEngine.configLocalScreenPublish(false);
            mImgManualPubScreen.setVisibility(View.GONE);
            mTextManualPubScreen.setVisibility(View.GONE);
        }
        defaultAudioDevice = sdkEngine.getDefaultAudioDevice();
        if (defaultAudioDevice == PRTCAudioDevice.AUDIO_DEVICE_SPEAKER) {
            mImgBtnMuteSpeaker.setImageResource(R.mipmap.speaker);
            mSpeakerOn = true;
        } else {
            mSpeakerOn = false;
            mImgBtnMuteSpeaker.setImageResource(R.mipmap.speaker_off);
        }
        sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
        sdkEngine.setClassType(mClass);
        sdkEngine.setAutoPublish(mPublishMode == CommonUtils.AUTO_MODE ? true : false);
        sdkEngine.setAutoSubscribe(mScribeMode == CommonUtils.AUTO_MODE ? true : false);
        sdkEngine.setVideoProfile(PRTCVideoProfile.matchValue(mVideoProfileSelect));
        sdkEngine.setScreenProfile(PRTCVideoProfile.VIDEO_PROFILE_1280_720);

        //分辨率菜单显示
        mTextResolution.setText(mResolutionOption.get(mVideoProfileSelect));
        mAdapter = new ArrayAdapter<String>(this, R.layout.videoprofile_item, mResolutionOption);

        mResolutionPopupWindow = new VideoProfilePopupWindow(this);
        mResolutionPopupWindow.setOnSpinnerItemClickListener(mOnResulutionOptionClickListener);
        if (mExtendCameraCapture) {
            //扩展摄像头方式
            PRTCEnvHelper.setCaptureMode(
                    PRTCCaptureMode.CAPTURE_MODE_EXTEND.ordinal());
            mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
            IPRTCEngine.onRGBCaptureResult(mIPRTCDataProvider);
            mTextResolution.setVisibility(View.GONE);
            mImgBtnSwitchCam.setVisibility(View.GONE);
        }
        else {
            PRTCEnvHelper.setCaptureMode(
                    PRTCCaptureMode.CAPTURE_MODE_LOCAL.ordinal());
        }
        if (mPublishMode == CommonUtils.AUTO_MODE) {
            mImgPreview.setVisibility(View.GONE);
            mTextPreview.setVisibility(View.GONE);
            mImgControlMixSound.setVisibility(View.GONE);
            mTextControlMixSound.setVisibility(View.GONE);
        } else {
            //手动发布时，按钮隐藏
            //mImgManualPub.setVisibility(View.VISIBLE);
            //mTextManualPub.setVisibility(View.VISIBLE);
            setIconStats(false);
        }
        if (!mCameraEnable && !mMicEnable) {
            mImgManualPubVideo.setVisibility(View.GONE);
            mTextManualPubVideo.setVisibility(View.GONE);
        }
        if (!mScreenEnable) {
            mImgManualPubScreen.setVisibility(View.GONE);
            mTextManualPubScreen.setVisibility(View.GONE);
        }
        mImgBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        mImgBtnSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        mTextResolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });

        mImgBtnMuteMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteMic();
            }
        });

        mImgBtnMuteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteVideo();
            }
        });

        mImgBtnMuteSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteSpeaker(!mSpeakerOn);
            }
        });

        mImgBtnMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mirrorSwitch();
            }
        });

        mImgBtnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });

        mImgMix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMix();
            }
        });

        mImgLocalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLocalRecord();
            }
        });

        mImgScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addScreenShotCallBack(mLocalVideoView);
            }
        });

        mImgRemoteRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRemoteRecord();
            }
        });

        //手动发布
        //手动发布视频
        mImgManualPubVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoIsPublished) {
                    sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    // 重新刷新配置
                    refreshSettings();
                    if ( mCameraEnable || mMicEnable) {
                        if (!mVideoIsPublished) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, mCameraEnable, mMicEnable).getErrorCode());
                        }
                    }
                    else {
                        errorMessage.append("Camera or Mic is disable!\n");
                    }

                    List<Integer> errorCodes = new ArrayList<>();
                    for (Integer result : results) {
                        if (result != 0)
                            errorCodes.add(result);
                    }
                    if (!errorCodes.isEmpty()) {
                        for (Integer errorCode : errorCodes) {
                            if (errorCode != NET_ERR_CODE_OK.ordinal())
                                errorMessage.append("RTC_SDK_ERROR_CODE:" + errorCode + "\n");
                        }
                    }
                    if (errorMessage.length() > 0) {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, errorMessage.toString());
                    }
                    else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, "发布");
                    }
                }
                else {
                    sdkEngine.unPublish(MEDIA_TYPE_VIDEO);
                }
            }
        });

        //手动发布屏幕
        mImgManualPubScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScreenIsPublished) {
                    sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    // 重新刷新配置
                    refreshSettings();
                    if (mScreenEnable && !mScreenIsPublished) {
                        if (isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        }
                        else {
                            errorMessage.append("设备不支持屏幕捕捉\n");
                            //results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                        }
                    }
                    else {
                        errorMessage.append("Screen is disable!\n");
                    }

                    List<Integer> errorCodes = new ArrayList<>();
                    for (Integer result : results) {
                        if (result != 0)
                            errorCodes.add(result);
                    }
                    if (!errorCodes.isEmpty()) {
                        for (Integer errorCode : errorCodes) {
                            if (errorCode != NET_ERR_CODE_OK.ordinal())
                                errorMessage.append("RTC_SDK_ERROR_CODE:" + errorCode + "\n");
                        }
                    }
                    if (errorMessage.length() > 0) {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, errorMessage.toString());
                    }
                    else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, "发布");
                    }
                }
                else {
                    sdkEngine.unPublish(MEDIA_TYPE_SCREEN);
                }
            }
        });

        //预览控制
        mImgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreview(!mIsPreview);
                mIsPreview = !mIsPreview;
                if (mIsPreview) {
                    mTextPreview.setText(R.string.stop_preview);
                    mLocalVideoView.setVisibility(View.VISIBLE);
                }
                else {
                    mTextPreview.setText(R.string.start_preview);
                    mLocalVideoView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mImgLocalMixSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMixingSound(false);
            }
        });

        mImgRemoteMixSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMixingSound(true);
            }
        });

        mImgControlMixSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControlMixingSound();
            }
        });

        PRTCAuthInfo info = new PRTCAuthInfo();
        info.setAppId(mAppid);
        info.setToken(mRoomToken);
        info.setRoomId(mRoomid);
        info.setUId(mUserid);
        Log.d(TAG, " roomtoken = " + mRoomToken);
        initRecordManager();
        // 加入房间
        if (sdkEngine.joinChannel(info) == PRTCErrorCode.NET_ERR_SECKEY_NULL
                || mAppid.length() == 0) {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "加入房间失败，AppKey或AppId没有设置");
            endCall();
        }
        setVolumeControlStream(android.media.AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
            if (mUVCCamera != null) {
                mUVCCamera.startPreview();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (mSync) {
            if (mUVCCamera != null) {
                //mUVCCamera.stopPreview();
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        Log.d(TAG, "on Stop");
        if((mVideoIsPublished || mScreenIsPublished) && !mLeaveRoomFlag){
            Intent service = new Intent(this, RTCForeGroundService.class);
            startService(service);
//            sdkEngine.controlAudio(false);
//            if (!mExtendCameraCapture) {
//                sdkEngine.controlLocalVideo(false);
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Intent service = new Intent(this, RTCForeGroundService.class);
        stopService(service);
//        sdkEngine.controlAudio(true);
//        if (!mExtendCameraCapture) {
//            sdkEngine.controlLocalVideo(true);
//        }
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
            if (mUVCCamera != null) {
                //mUVCCamera.startPreview();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "activity destory");
        super.onDestroy();
        System.gc();
    }

    private IPRTCEngineEventHandler eventListener = new IPRTCEngineEventHandler() {
        @Override
        public void onServerDisconnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerDisconnect: ");
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 服务器已断开");
                    stopTimeShow();
                    onMediaServerDisconnect();
                }
            });
        }

        @Override
        public void onJoinRoomResult(int code, String msg, String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, " 加入房间成功");
                        startTimeShow();
                    } else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, " 加入房间失败 " +
                                code + " errmsg " + msg);
                        Intent intent = new Intent(RTCLiveTextureActivity.this, ConnectActivity.class);
                        onMediaServerDisconnect();
                        startActivity(intent);
                        finish();
                    }

                }
            });
        }

        @Override
        public void onLeaveRoomResult(int code, String msg, String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeaveRoomFlag = true;
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 离开房间 " +
                            code + " errmsg " + msg);
                    onMediaServerDisconnect();
                    releaseExtendCamera();
                    finish();
                }
            });
        }

        @Override
        public void onRejoiningRoom(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "rejoining room");
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 服务器重连中…… ");
                    stopTimeShow();
                }
            });
        }

        @Override
        public void onRejoinRoomResult(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, "服务器重连成功");
                    startTimeShow();
                }
            });
        }

        @Override
        public void onLocalPublish(int code, String msg, PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {

                        int mediatype = info.getMediaType().ordinal();
                        mPublishMediaType = PRTCMediaType.matchValue(mediatype);
                        if(mLocalRender != null){
                        if (mediatype == MEDIA_TYPE_VIDEO.ordinal()) {
                                mLocalRender.init();
                            mImgManualPubVideo.setImageResource(R.mipmap.stop);
                            mTextManualPubVideo.setText(R.string.pub_cancel_video);
                            if (!sdkEngine.isAudioOnlyMode()) {
                                // PRTCSurfaceViewGroup打开
                                //mLocalVideoView.init(false);
                                // Surfaceview打开
                                //mLocalVideoView.setBackgroundColor(Color.TRANSPARENT);


                                localViewWidth = mLocalVideoView.getMeasuredWidth();
                                localViewHeight = mLocalVideoView.getMeasuredHeight();
                                if (!mIsPreview) {
                                    if (mExtendCameraCapture) {
                                            sdkEngine.renderLocalView(info,mLocalRender, PRTCScaleType.SCALE_ASPECT_FIT, null);
                                    } else {
                                            sdkEngine.renderLocalView(info,mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL, null);
                                    }
                                    //if (mPublishMode != CommonUtils.AUTO_MODE) {
//                                        setIconStats(true);
                                    //}
                                }
                                else {
//                                    setIconStats(true);
                                }
                                mVideoIsPublished = true;
                                mLocalStreamInfo = info;
                                mSwapStreamInfo = info;
                                mLocalVideoView.setTag(mLocalStreamInfo);
//                                mLocalVideoView.setOnClickListener(mToggleScreenOnClickListener);
                                mLocalVideoView.setOnClickListener(mScreenShotOnClickListener);
                            }
                        }
                        else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN.ordinal()) {
                            mScreenIsPublished = true;
                            mImgManualPubScreen.setImageResource(R.mipmap.stop);
                            mTextManualPubScreen.setText(R.string.pub_cancel_screen);
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
                                //sdkEngine.startPreview(info.getMediaType(), mLocalVideoView,PRTCScaleType.SCALE_ASPECT_FILL,null);
                            }
                        }
                        setIconStats(true);
                       }
                    }
                    else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this,
                                "发布视频失败 " + code + " errmsg " + msg);
                    }

                }
            });
        }

        @Override
        public void onLocalUnPublish(int code, String msg, PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (info.getMediaType() == MEDIA_TYPE_VIDEO) {
                            if (mPublishMode == CommonUtils.AUTO_MODE) {
                                mImgManualPubVideo.setVisibility(View.GONE);
                                mTextManualPubVideo.setVisibility(View.GONE);
                            }
                            else {
                                mImgManualPubVideo.setImageResource(R.mipmap.publish);
                                mTextManualPubVideo.setText(R.string.pub_video);
                            }
                            if (mLocalVideoView != null) {
//                                localrenderview.refresh();
                            }
                            mVideoIsPublished = false;
                            mLocalVideoView.setVisibility(View.INVISIBLE);
                            if (mIsLocalMixingSound) {
                                toggleMixingSound(false);
                            }
                            if (mIsRemoteMixingSound) {
                                toggleMixingSound(true);
                            }
                        } else if (info.getMediaType() == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                            mScreenIsPublished = false;
                            if (mPublishMode == CommonUtils.AUTO_MODE) {
                                mImgManualPubScreen.setVisibility(View.GONE);
                                mTextManualPubScreen.setVisibility(View.GONE);
                            }
                            else {
                                mImgManualPubScreen.setImageResource(R.mipmap.publish_screen);
                                mTextManualPubScreen.setText(R.string.pub_screen);
                            }
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
//                                if (localrenderview != null) {
//                                    localrenderview.refresh();
//                                }
                            }
                            if (mIsLocalMixingSound) {
                                toggleMixingSound(false);
                            }
                        }
                        if (!mScreenIsPublished && !mVideoIsPublished) {
                            setIconStats(false);
                            setPreview(false);
                            mIsPreview = false;
                            mTextPreview.setText(R.string.start_preview);
                        }
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, "取消发布成功");
                    }
                    else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, "取消发布失败 "
                                + code + " errmsg " + msg);
                    }
                }
            });
        }


        @Override
        public void onRemoteUserJoin(String uid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 "
                            + uid + " 加入房间 ");
                }
            });
        }

        @Override
        public void onRemoteUserLeave(String uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "remote user " + uid + "leave ,reason: " + reason);
                    //onUserLeave(uid);
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 " +
                            uid + " 离开房间，离开原因： " + reason);
                }
            });
        }

        @Override
        public void onRemotePublish(PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //特殊情况下，譬如客户端在断网情况下离开房间，服务端可能还持有流，并没有超时，客户端就会收到自己的userid,
                    // 如果客户端是固定userid就可以过滤掉，如果不是，等待服务端超时也会删除流
                    Log.d(TAG, "onRemotePublish: " + info.getUId() + " me : " + mUserid);
                    if(!mUserid.equals(info.getUId())){
                        mSteamList.add(info);
                        if (!sdkEngine.isAutoSubscribe()) {
                            sdkEngine.subscribe(info);
                        } else {
                            //mSpinnerPopupWindowScribe.notifyUpdate();
                            //refreshStreamInfoText();
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteUnPublish(PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, " onRemoteUnPublish " + info.getMediaType() + " " + info.getUId());
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 " +
                            info.getUId() + " 取消媒体流 " + info.getMediaType());
                    String mkey = info.getUId() + info.getMediaType().toString();
                    sdkEngine.stopRemoteView(info);
                    if(mSwapStreamInfo!= null && mSwapStreamInfo.getUId().equals(info.getUId()) && mSwapStreamInfo.getMediaType().toString().equals(info.getMediaType().toString())) {
//                        sdkEngine.stopRemoteView(mSwapStreamInfo);
                        int localIndex = mVideoAdapter.getPositionByKey(mUserid + mPublishMediaType.toString());
                        if (localIndex >= 0) {
                            Log.d(TAG, " onRemoteUnPublish localIndex " + localIndex);
                            mkey = mUserid + mPublishMediaType.toString();
                            sdkEngine.stopPreview(mPublishMediaType);
                            sdkEngine.renderLocalView(mLocalStreamInfo, mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL, null);
                            mSwapStreamInfo = mLocalStreamInfo;
                        }
                    }
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(mkey);
                    }
                }
            });
        }

        @Override
        public void onSubscribeResult(int code, String msg, PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        Log.d(TAG, " subscribe info: " + info);
                        RTCVideoViewInfo vinfo = new RTCVideoViewInfo();
                        vinfo.setmUid(info.getUId());
                        vinfo.setmMediatype(info.getMediaType());
                        vinfo.setmEanbleVideo(info.isHasVideo());
                        vinfo.setEnableAudio(info.isHasAudio());



                        String mkey = info.getUId() + info.getMediaType().toString();
                        vinfo.setKey(mkey);
                        //默认输出，和外部输出代码二选一
                        if (mVideoAdapter != null) {
                            vinfo.setStreamInfo(info);
                            mVideoAdapter.addStreamView(mkey, vinfo);
                        }

                        //textureview动态生成
//                        TextureView textureView = new TextureView(getApplicationContext());
//                        vinfo.setmRenderview(textureView);
//                        textureView.setTag(info);
//                        textureView.setId(R.id.video_view);
//                        if (vinfo != null && textureView != null) {
//                            sdkEngine.startRemoteView(info, textureView,PRTCScaleType.SCALE_ASPECT_FILL,null);
//                        }
                        //如果订阅成功就删除待订阅列表中的数据
                        //mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                        //refreshStreamInfoText();
                    } else {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, " 订阅用户  " +
                                info.getUId() + " 流 " + info.getMediaType() + " 失败 " +
                                " code " + code + " msg " + msg);
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(int code, String msg, PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveTextureActivity.this, " 取消订阅用户 " +
                            info.getUId() + " 类型 " + info.getMediaType());
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(info.getUId() + info.getMediaType().toString());
                    }
                    //取消订阅又变成可订阅
                    //mSpinnerPopupWindowScribe.addStreamInfo(info, true);
                }
            });
        }

        @Override
        public void onLocalStreamMuteRsp(int code, String msg, PRTCMediaType mediatype, PRTCTrackType tracktype, boolean mute) {
            Log.d(TAG, " code " + code + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (mediatype == MEDIA_TYPE_VIDEO) {
                            if (tracktype == PRTCTrackType.TRACK_TYPE_AUDIO) {
                                onMuteMicResult(mute);
                            } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                                onMuteVideoResult(mute);
                            }
                        } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                            onMuteVideoResult(mute);
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteStreamMuteRsp(int code, String msg, String uid, PRTCMediaType mediatype, PRTCTrackType tracktype, boolean mute) {
            Log.d(TAG, " code " + code + " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        Log.d(TAG, " onRemoteStreamMuteRsp " + code + "msg " + msg);
                    }
                }
            });
        }

        @Override
        public void onRemoteTrackNotify(String uid, PRTCMediaType mediatype, PRTCTrackType tracktype, boolean mute) {
            Log.d(TAG, " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediatype == MEDIA_TYPE_VIDEO) {
                        String cmd = mute ? "关闭" : "打开";
                        if (tracktype == PRTCTrackType.TRACK_TYPE_AUDIO) {
                            ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 " +
                                    uid + cmd + " 麦克风");
                        } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                            ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 " +
                                    uid + cmd + " 摄像头");
                        }

                    } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                        String cmd = mute ? "关闭" : "打开";
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, " 用户 " +
                                uid + cmd + " 桌面流");
                    }
                }
            });
        }

        @Override
        public void onSendRTCStatus(PRTCStreamStatus rtstats) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onRemoteRTCStatus(PRTCStreamStatus rtstats) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onLocalAudioLevel(int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //localprocess.setProgress(volume);
                    if (!mMuteMic) {
                        setVolume(volume);
                    }
                }
            });
        }

        @Override
        public void onRemoteAudioLevel(String uid, int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVideoAdapter != null) {
                        String mkey = uid + MEDIA_TYPE_VIDEO.toString();
                    }
                }
            });
        }

        @Override
        public void onKickoff(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RTCLiveTextureActivity.this, " 被踢出会议 code " +
                            code);
                    Log.d(TAG, " user kickoff reason " + code);
                    Intent intent = new Intent(RTCLiveTextureActivity.this, ConnectActivity.class);
                    releaseExtendCamera();
                    onMediaServerDisconnect();
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onWarning(int warn) {

        }

        @Override
        public void onError(int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error == PRTCErrorCode.NET_ERR_SDP_SWAP_FAIL.ordinal()) {
                        ToastUtils.shortShow(RTCLiveTextureActivity.this, "sdp swap failed");
                    }
                }
            });
        }

        @Override
        public void onRecordStop(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RTCLiveTextureActivity.this, "录制结束: " + (code == NET_ERR_CODE_OK.ordinal()?"成功":"失败: "+ code));
                    if(mIsRemoteRecording){
                        mIsRemoteRecording = false;
                        mImgRemoteRecord.setImageResource(R.mipmap.remote_record);
                        mTextRemoteRecord.setText(R.string.start_remote_record);
                    }
                }
            });
        }

        @Override
        public void onQueryMix(int code, String msg, int type, String mixId, String fileName) {
            Log.d(TAG, "onQueryMix: "+ code + " msg: "+ msg + " type: "+ type);
        }

        @Override
        public void onRecordStatusNotify(PRTCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String fileName) {
            if (status == PRTCMediaServiceStatus.RECORD_STATUS_START) {
                String videoPath = "http://" + mBucket + "." + mRegion + ".ufileos.com/" + fileName;
                Log.d(TAG, "remote record path: " + videoPath + ".mp4");
                ToastUtils.longShow(RTCLiveTextureActivity.this, "观看地址: " + videoPath);
                mIsRemoteRecording = true;
                mImgRemoteRecord.setImageResource(R.mipmap.stop);
                mTextRemoteRecord.setText(R.string.remote_recording);
                if (mAtomOpStart)
                    mAtomOpStart = false;
            } else if (status == PRTCMediaServiceStatus.RECORD_STATUS_STOP_REQUEST_SEND) {
                if (mIsRemoteRecording) {
                    mIsRemoteRecording = false;
                    mImgRemoteRecord.setImageResource(R.mipmap.remote_record);
                    mTextRemoteRecord.setText(R.string.start_remote_record);
                }
            } else {
                ToastUtils.longShow(RTCLiveTextureActivity.this, "录制异常: 原因：" + code);
            }
        }

        @Override
        public void onRelayStatusNotify(PRTCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String[] pushUrls) {
            if (status == PRTCMediaServiceStatus.RELAY_STATUS_START) {
                // ulive cdn watch address: http://rtchls.ugslb.com/rtclive/roomid.flv
                mIsMixing = true;
                mImgMix.setImageResource(R.mipmap.stop);
                mTextMix.setText(R.string.mixing);
                if (mAtomOpStart)
                    mAtomOpStart = false;
            } else if (status == PRTCMediaServiceStatus.RELAY_STATUS_STOP_REQUEST_SEND) {
                if (mIsMixing) {
                    mIsMixing = false;
                    mImgMix.setImageResource(R.mipmap.mix);
                    mTextMix.setText(R.string.start_mix);
                } else {
                    ToastUtils.longShow(RTCLiveTextureActivity.this, "转推异常: 原因：" + code);
                }
            }
        }

        @Override
        public void onAddStreams(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onAddStreams: "+ code + msg);
                }
            });
        }

        @Override
        public void onDelStreams(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDelStreams: "+ code + msg);
                }
            });
        }

        @Override
        public void onLogOffUsers(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onLogOffUsers: "+ code + " msg: "+ msg);
                }
            });
        }

        @Override
        public void onMsgNotify(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onMsgNotify: code: " + code + "msg: " + msg);
                }
            });
        }

        @Override
        public void onLogOffNotify(int cmdType, String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onLogOffUsers: "+ cmdType + " userId: "+ userId);
                }
            });
        }

        @Override
        public void onRecordStart(int code, String fileName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onRecordStart: " + code + " fileName: " + fileName);
                }
            });
        }

        @Override
        public void onServerBroadCastMsg(String uid, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerBroadCastMsg: uid: " + uid + "msg: " + msg);
                }
            });
        }

        @Override
        public void onAudioDeviceChanged(PRTCAudioDevice device) {
            defaultAudioDevice = device;
//            LogUtils.d(TAG,"AudioManager: room change device to "+ defaultAudioDevice);
            if (defaultAudioDevice == PRTCAudioDevice.AUDIO_DEVICE_SPEAKER) {
                mImgBtnMuteSpeaker.setImageResource(R.mipmap.speaker);
                mSpeakerOn = true;
            } else {
                mSpeakerOn = false;
                mImgBtnMuteSpeaker.setImageResource(R.mipmap.speaker_off);
            }
        }

        @Override
        public void onPeerLostConnection(int type, PRTCStreamInfo info) {
            Log.d(TAG, "onPeerLostConnection: type: " + type + "info: " + info);
        }

        @Override
        public void onNetWorkQuality(String userId, PRTCStreamType streamType, PRTCMediaType mediaType, PRTCNetWorkQuality quality) {
//            Log.d(TAG, "onNetWorkQuality: userid: " + userId + "streamType: " + streamType + "mediatype : "+ mediaType + " quality: " + quality);
        }

        @Override
        public void onAudioFileFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onAudioFileFinish" );

                    if ( mIsLocalMixingSound){
                        // 本地混音中
                        mImgLocalMixSound.setImageResource(R.mipmap.local_mix_sound);
                        mTextLocalMixSound.setText(R.string.start_local_mix_sound);
                        mImgRemoteMixSound.setVisibility(View.VISIBLE);
                        mTextRemoteMixSound.setVisibility(View.VISIBLE);
                        mIsLocalMixingSound = false;
                    }
                    else if (mIsRemoteMixingSound) {
                        // 远端混音中
                        mImgLocalMixSound.setVisibility(View.VISIBLE);
                        mTextLocalMixSound.setVisibility(View.VISIBLE);
                        mImgRemoteMixSound.setImageResource(R.mipmap.remote_mix_sound);
                        mTextRemoteMixSound.setText(R.string.start_remote_mix_sound);
                        mIsRemoteMixingSound = false;
                    }
                    mIsPauseMixingSound = false;
                    mImgControlMixSound.setImageResource(R.mipmap.pause);
                    mTextControlMixSound.setText(R.string.pause_mixing_sound);
                    mImgControlMixSound.setVisibility(View.GONE);
                    mTextControlMixSound.setVisibility(View.GONE);
                }
            });
        }
    };

    IPRTCRecordListener mLocalRecordListener = new IPRTCRecordListener() {
        @Override
        public void onLocalRecordStart(String path, int code,String msg) {
            Log.d(TAG, "onLocalRecordStart: " + path + " code: "+ code + " msg: " + msg);
        }

        @Override
        public void onLocalRecordStop(String path, long fileLength, int code) {
            Log.d(TAG, "onLocalRecordStop: " + path + "fileLength: "+ fileLength + "code: "+ code);
        }

        @Override
        public void onRecordStatusCallBack(long duration, long fileSize) {
            Log.d(TAG, "onRecordStatusCallBack duration: " + duration + " fileSize: "+ fileSize);
        }
    };

    private View.OnClickListener mToggleScreenOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleFullScreen();
        }
    };

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Log.v(TAG, "onAttach:");
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "USB摄像头已连接");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUSBMonitor != null ) {
                            if (mUSBMonitor.getDeviceCount() > 0) {
                                mUSBMonitor.requestPermission(device);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.v(TAG, "onConnect:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        //final UVCCamera camera = initUVCCamera(ctrlBlock);
                        mUVCCamera = initUVCCamera(ctrlBlock);
                        isActive = true;
                        isPreview = true;
                    }
                }
            });
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.v(TAG, "onDisconnect:");
            // XXX you should check whether the comming device equal to camera device that currently using
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.stopPreview();
                            mUVCCamera.close();
                            mUVCCamera.destroy();
/*                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }*/
                            isActive = isPreview = false;
                        }
                    }
                }
            });
        }

        @Override
        public void onDetach(final UsbDevice device) {
            Log.v(TAG, "onDetach:");
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "USB摄像头被移除");
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    private View.OnClickListener mScreenShotOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "takeScreenShot: ");
            takeScreenShot(true,mLocalStreamInfo);
        }
    };

    private View.OnClickListener mSwapRemoteLocalListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof TextureView) {
                    PRTCStreamInfo clickStreamInfo = (PRTCStreamInfo) v.getTag();
                    boolean swapLocal = mSwapStreamInfo.getUId().equals(mUserid);
                    boolean clickLocal = clickStreamInfo.getUId().equals(mUserid);
                    Log.d(TAG, "mSwapStreamInfo: "+ mSwapStreamInfo + " clickInfo: " + clickStreamInfo);
                    Log.d(TAG, "onClick swaplocal"+ swapLocal + " clickLocal: " + clickLocal);
                    if(swapLocal && !clickLocal){
                        sdkEngine.stopRemoteView(clickStreamInfo);
                        sdkEngine.stopPreview(mSwapStreamInfo.getMediaType());
//                        sdkEngine.renderLocalView(mSwapStreamInfo, v,PRTCScaleType.SCALE_ASPECT_FILL, null);
                        PRTCTextureViewRenderer remoteRender = (PRTCTextureViewRenderer)v.getTag(R.id.render);
                        sdkEngine.renderLocalView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL, null);
//                        sdkEngine.startRemoteView(clickStreamInfo, mLocalVideoView,PRTCScaleType.SCALE_ASPECT_FILL,null);
                        sdkEngine.startRemoteView(clickStreamInfo, mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    }else if(!swapLocal && clickLocal){
                        sdkEngine.stopRemoteView(mSwapStreamInfo);
                        sdkEngine.stopPreview(clickStreamInfo.getMediaType());
                        PRTCTextureViewRenderer remoteRender = (PRTCTextureViewRenderer)v.getTag(R.id.render);
                        sdkEngine.renderLocalView(clickStreamInfo, mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                        sdkEngine.startRemoteView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    }else if(!swapLocal && !clickLocal){
                        sdkEngine.stopRemoteView(mSwapStreamInfo);
                        sdkEngine.stopRemoteView(clickStreamInfo);
                        sdkEngine.startRemoteView(clickStreamInfo, mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                        PRTCTextureViewRenderer remoteRender = (PRTCTextureViewRenderer)v.getTag(R.id.render);
                        sdkEngine.startRemoteView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    }
                    v.setTag(mSwapStreamInfo);
                    mVideoAdapter.updateSwapInfo(clickStreamInfo,mSwapStreamInfo);
                    mSwapStreamInfo = clickStreamInfo;
            }
        }
    };

    private void switchCamera() {
        sdkEngine.switchCamera();
        ToastUtils.shortShow(this, "切换摄像头");
        mSwitchCamera = !mSwitchCamera;
    }

    private boolean muteMic() {
        sdkEngine.muteLocalMic(!mMuteMic);
        if (!mMuteMic) {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "关闭麦克风");
        } else {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "打开麦克风");
        }
        return false;
    }

    private boolean muteVideo() {
        if (mScreenEnable || mCameraEnable) {
            if (isScreenCaptureSupport && !mCameraEnable) {
                sdkEngine.muteLocalVideo(!mMuteVideo, PRTCMediaType.MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteVideo, MEDIA_TYPE_VIDEO);
            }
        }
        if (!mMuteVideo) {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "关闭摄像头");
        } else {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "打开摄像头");
        }
        return false;
    }

    private void muteSpeaker(boolean enable) {
        if (mSpeakerOn) {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "关闭喇叭");
        } else {
            ToastUtils.shortShow(RTCLiveTextureActivity.this, "打开喇叭");
        }
        mSpeakerOn = !mSpeakerOn;
        sdkEngine.setSpeakerOn(enable);
        mImgBtnMuteSpeaker.setImageResource(enable ? R.mipmap.speaker : R.mipmap.speaker_off);
    }

    private void onMuteVideoResult(boolean mute) {
        mMuteVideo = mute;
        mImgBtnMuteVideo.setImageResource(mMuteVideo ? R.mipmap.camera_off :
                R.mipmap.camera);
        if (mLocalVideoView.getTag(R.id.swap_info) != null) {
            PRTCStreamInfo remoteInfo = (PRTCStreamInfo) mLocalVideoView.getTag(R.id.swap_info);
            String mkey = remoteInfo.getUId() + remoteInfo.getMediaType().toString();
            View view = mRemoteGridView.getChildAt(mVideoAdapter.getPositionByKey(mkey));
            if (mute) {
                view.setVisibility(View.INVISIBLE);
                view.setBackgroundColor(Color.BLACK);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (mute) {
//                localrenderview.refresh();
                mLocalVideoView.setVisibility(View.INVISIBLE);
            }
            else {
                mLocalVideoView.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onMuteMicResult(boolean mute) {
        mMuteMic = mute;
        if (mMuteMic) {
            mImgSoundVolume.setVisibility(View.INVISIBLE);
        }
        else {
            mImgSoundVolume.setVisibility(View.VISIBLE);
        }
        mImgBtnMuteMic.setImageResource(mMuteMic ? R.mipmap.mic_off :
                R.mipmap.mic);
        mImgMicSts.setImageResource(mMuteMic ? R.mipmap.mic_disable :
                R.mipmap.mic_volume);
    }

    private void mirrorSwitch() {
        mMirror = !mMirror;
        PRTCEnvHelper.setFrontCameraMirror(mMirror);
        mImgBtnMirror.setImageResource(mMirror ? R.mipmap.mirror_on :
                R.mipmap.mirror);
    }

    private void endCall() {
        sdkEngine.leaveChannel().ordinal();
//        Intent intent = new Intent(RTCLiveTextureActivity.this, ConnectActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        releaseExtendCamera();
//        onMediaServerDisconnect();
//        startActivity(intent);
        finish();
    }
    private void onMediaServerDisconnect() {
        //mLocalVideoView.release();
        clearGridItem();
        IPRTCEngine.destroy();
    }
    private void clearGridItem() {
        mVideoAdapter.clearAll();
        mVideoAdapter.notifyDataSetChanged();
    }
    private void releaseExtendCamera() {
/*        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mSync) {
                    isActive = isPreview = false;
                    if (mUVCCamera != null) {
                        mUVCCamera.stopPreview();
                        mUVCCamera.close();
                        mUVCCamera = null;
                    }
                    if (mUSBMonitor != null) {
                        mUSBMonitor.destroy();
                        mUSBMonitor = null;
                    }
                }
            }
        });*/
        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
                mUVCCamera.close();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        if (mIPRTCDataProvider != null) {
            mIPRTCDataProvider = null;
        }
        if (mIPRTCDataReceiver != null) {
            mIPRTCDataReceiver = null;
        }
        if(PRTCEnvHelper.getCaptureMode() == PRTCCaptureMode.CAPTURE_MODE_EXTEND.ordinal()) {
            //这里回收一遍
            while(mQueueByteBuffer.size() != 0 ){
                ByteBuffer videoData = mQueueByteBuffer.poll();
                if(videoData != null){
                    videoData = null;
                }
            }
        }
    }
    private void startTimeShow() {
        timeShow.setBase(SystemClock.elapsedRealtime());
        timeShow.start();
    }

    private void stopTimeShow() {
        timeShow.stop();
    }

    public void toggleFullScreen() {
        if (!mLocalViewFullScreen) {
            setSystemUIVisible(false);
            //隐藏顶部标题和底部工具栏
            mTitleBar.setVisibility(View.GONE);
            mToolBar.setVisibility(View.GONE);
            StatusBarUtils.removeStatusView(this);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, screenHeight + mToolBar.getHeight());
            params.setMargins(0, 0, 0, 0);
            mLocalVideoView.setLayoutParams(params);
            //抽屉随回显拉长
            DrawerLayout.LayoutParams dl_params = (DrawerLayout.LayoutParams) mDrawerMenu.getLayoutParams();
            dl_params.topMargin = 0;
            dl_params.bottomMargin = 0;
            //隐藏麦克风状态图标
            mImgSoundVolume.setVisibility(View.INVISIBLE);
            mImgMicSts.setVisibility(View.INVISIBLE);
            Log.d(TAG, "Switch full screen width: " + params.width + " height: " + params.height);
        }
        else {
            setSystemUIVisible(true);
            //退出全屏
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(localViewWidth, localViewHeight);
            params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
            mLocalVideoView.setLayoutParams(params);
            //显示顶部标题和底部工具栏
            mTitleBar.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
            StatusBarUtils.addStatusView(this);
            //抽屉菜单还原
            DrawerLayout.LayoutParams dl_params = (DrawerLayout.LayoutParams) mDrawerMenu.getLayoutParams();
            dl_params.topMargin = mTitleBar.getHeight();
            dl_params.bottomMargin =  mToolBar.getHeight();
            //还原麦克风状态图标
            if (!mMuteMic) {
                mImgSoundVolume.setVisibility(View.VISIBLE);
            }
            mImgMicSts.setVisibility(View.VISIBLE);

            Log.d(TAG, "Quit full screen. width: " + params.width + " height: " + params.height);
        }
        mLocalViewFullScreen = !mLocalViewFullScreen;
    }

    private IPRTCScreenShot mIPRTCScreenShot = new IPRTCScreenShot() {
        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/rtcscreen_"+System.currentTimeMillis() +".jpg";
            File file = new File(name);
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "screen shoot : " + name);
            ToastUtils.shortShow(RTCLiveTextureActivity.this,"screen shoot : " + name);
        }
    };

    private void addScreenShotCallBack(View view){
        if (view instanceof PRTCSurfaceViewGroup) {
            ((PRTCSurfaceViewGroup)view).setScreenShotBack(mIPRTCScreenShot);
        }
        else if(view instanceof PRTCSurfaceViewRenderer) {
            ((PRTCSurfaceViewRenderer)view).setScreenShotBack(mIPRTCScreenShot);
        }
        else {
            if(view == mLocalVideoView){
                if(view.getTag() != null && view.getTag() instanceof PRTCStreamInfo){
                    takeScreenShot(true,(PRTCStreamInfo)view.getTag());
                }
            }
        }
    }

    //初始化视频录制
    private void initRecordManager() {
        RtcRecordManager.init("");
        Log.d(TAG, "initRecordManager: cache path:" + RtcRecordManager.getVideoCachePath());
    }

    private void toggleLocalRecord(){
        if (!mIsLocalRecording) {
            Log.d(TAG, " start local record: ");
            //URTCRecordManager.getInstance().startRecord(PRTCRecordType.RECORD_TYPE_MP4,"mnt/sdcard/urtc/mp4/"+ System.currentTimeMillis()+".mp4",mLocalRecordListener,1000);
            mIsLocalRecording = true;
            mImgLocalRecord.setImageResource(R.mipmap.stop);
            mTextLocalRecord.setText(R.string.local_recording);
        }
        else {
            Log.d(TAG, " stop local record: ");
            //URTCRecordManager.getInstance().stopRecord();
            mIsLocalRecording = false;
            mImgLocalRecord.setImageResource(R.mipmap.record);
            mTextLocalRecord.setText(R.string.start_local_record);
        }
    }

    private void toggleRemoteRecord(){
        if (!mIsRemoteRecording) {
            Log.d(TAG, " start remote record: ");
            mAtomOpStart = true;
            PRTCMixProfile mixProfile = (PRTCMixProfile) PRTCMixProfile.getInstance().assembleMixParamsBuilder()
                    .type(PRTCMixProfile.MIX_TYPE_RECORD)
                    //画面模式
                    .layout(PRTCMixProfile.LAYOUT_CLASS_ROOM_2)
                    //画面分辨率
                    .resolution(1280, 720)
                    //背景色
                    .bgColor(0, 0, 0)
                    //画面帧率
                    .frameRate(15)
                    //画面码率
                    .bitRate(1000)
                    //h264视频编码
                    .videoCodec(PRTCMixProfile.VIDEO_CODEC_H264)
                    //编码质量
                    .qualityLevel(PRTCMixProfile.QUALITY_H264_CB)
                    //音频编码
                    .audioCodec(PRTCMixProfile.AUDIO_CODEC_AAC)
                    //主讲人ID
                    .mainViewUserId(mUserid)
                    //主讲人媒体类型
                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                    //加流方式手动
                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_MANUAL)
                    //添加流列表，也可以后续调用MIX_TYPE_UPDATE 动态添加
                    .addStream(mUserid,MEDIA_TYPE_VIDEO.ordinal())
                    .build();
            sdkEngine.startRelay(mixProfile);

        }
        else if (!mAtomOpStart) {
            Log.d(TAG, " stop remote record: ");
            mAtomOpStart = true;
            sdkEngine.stopRecord();
        }
    }

    private void toggleMix(){
        if (!mIsMixing) {
            Log.d(TAG, " start mix: ");
            mAtomOpStart = true;
            PRTCMixProfile mixProfile = (PRTCMixProfile) PRTCMixProfile.getInstance().assembleMixParamsBuilder()
                    .type(PRTCMixProfile.MIX_TYPE_RELAY)
                    //画面模式
                    .layout(PRTCMixProfile.LAYOUT_CLASS_ROOM_2)
                    //画面分辨率
                    .resolution(1280, 720)
                    //背景色
                    .bgColor(0, 0, 0)
                    //画面帧率
                    .frameRate(15)
                    //画面码率
                    .bitRate(1000)
                    //h264视频编码
                    .videoCodec(PRTCMixProfile.VIDEO_CODEC_H264)
                    //编码质量
                    .qualityLevel(PRTCMixProfile.QUALITY_H264_CB)
                    //音频编码
                    .audioCodec(PRTCMixProfile.AUDIO_CODEC_AAC)
                    //主讲人ID
                    .mainViewUserId(mUserid)
                    //主讲人媒体类型
                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                    //加流方式手动
                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_MANUAL)
                    //添加流列表，也可以后续调用MIX_TYPE_UPDATE 动态添加
                    .addStream(mUserid,MEDIA_TYPE_VIDEO.ordinal())
                    //设置转推cdn 的地址
                    .addPushUrl("rtmp://rtcpush.ugslb.com/rtclive/" + mRoomid)
                    .build();
            sdkEngine.startRelay(mixProfile);
        }
        else if(!mAtomOpStart) {
            Log.d(TAG, " stop mix: ");
            mAtomOpStart = true;
            sdkEngine.stopRelay(null);
        }
    }

    // 播放本地或远端混音
    private void toggleMixingSound(boolean isRemotePlay) {
        if (!isRemotePlay) {
            // 本地混音
            if (!mIsLocalMixingSound) {
                if (!sdkEngine.startPlayAudioFile(sdkEngine.copyAssetsFileToSdcard("water.mp3"))) {
                    return;
                }
                else {
                    mImgLocalMixSound.setImageResource(R.mipmap.stop);
                    mTextLocalMixSound.setText(R.string.local_sound_mixing);
                    mImgRemoteMixSound.setVisibility(View.GONE);
                    mTextRemoteMixSound.setVisibility(View.GONE);
                    mImgControlMixSound.setVisibility(View.VISIBLE);
                    mTextControlMixSound.setVisibility(View.VISIBLE);
                }
            } else {
                sdkEngine.stopPlayAudioFile();
                mIsPauseMixingSound = false;
                mImgLocalMixSound.setImageResource(R.mipmap.local_mix_sound);
                mTextLocalMixSound.setText(R.string.start_local_mix_sound);
                mImgRemoteMixSound.setVisibility(View.VISIBLE);
                mTextRemoteMixSound.setVisibility(View.VISIBLE);
                mImgControlMixSound.setImageResource(R.mipmap.pause);
                mTextControlMixSound.setText(R.string.pause_mixing_sound);
                mImgControlMixSound.setVisibility(View.GONE);
                mTextControlMixSound.setVisibility(View.GONE);
            }
            mIsLocalMixingSound = !mIsLocalMixingSound;
        }
        else if (mVideoIsPublished && isRemotePlay){
            // 本地+远端混音
            if (!mIsRemoteMixingSound) {
                if (!sdkEngine.startPlayAudioFile(sdkEngine.copyAssetsFileToSdcard("water.mp3"), true, false)) {
                    return;
                }
                else {
                    mImgRemoteMixSound.setImageResource(R.mipmap.stop);
                    mTextRemoteMixSound.setText(R.string.remote_sound_mixing);
                    mImgLocalMixSound.setVisibility(View.GONE);
                    mTextLocalMixSound.setVisibility(View.GONE);
                    mImgControlMixSound.setVisibility(View.VISIBLE);
                    mTextControlMixSound.setVisibility(View.VISIBLE);
                }
            } else {
                sdkEngine.stopPlayAudioFile();
                mIsPauseMixingSound = false;
                mImgRemoteMixSound.setImageResource(R.mipmap.remote_mix_sound);
                mTextRemoteMixSound.setText(R.string.start_remote_mix_sound);
                mImgLocalMixSound.setVisibility(View.VISIBLE);
                mTextLocalMixSound.setVisibility(View.VISIBLE);
                mImgControlMixSound.setImageResource(R.mipmap.pause);
                mTextControlMixSound.setText(R.string.pause_mixing_sound);
                mImgControlMixSound.setVisibility(View.GONE);
                mTextControlMixSound.setVisibility(View.GONE);
            }
            mIsRemoteMixingSound = !mIsRemoteMixingSound;
        }
        else {
            Log.e(TAG, " Wrong mixing status.");
        }
    }

    private void toggleControlMixingSound() {
        if (mIsLocalMixingSound || mIsRemoteMixingSound) {
            if (mIsPauseMixingSound) {
                sdkEngine.resumeAudioFile();
                mImgControlMixSound.setImageResource(R.mipmap.pause);
                mTextControlMixSound.setText(R.string.pause_mixing_sound);
            } else {
                sdkEngine.pauseAudioFile();
                mImgControlMixSound.setImageResource(R.mipmap.play);
                mTextControlMixSound.setText(R.string.resume_mixing_sound);
            }
            mIsPauseMixingSound = !mIsPauseMixingSound;
        }
    }

    /**
     * 根据音量大小设置录音时的音量动画
     */
    private void setVolume(int voiceValue) {
        if (!mMuteMic) {
            if (voiceValue < 15) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_01);
            } else if (voiceValue > 15 && voiceValue < 30) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_02);
            } else if (voiceValue > 30 && voiceValue < 45) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_03);
            } else if (voiceValue > 45 && voiceValue < 60) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_04);
            } else if (voiceValue > 60 && voiceValue < 75) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_05);
            } else if (voiceValue > 75 && voiceValue < 90) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_06);
            } else if (voiceValue > 100) {
                mImgSoundVolume.setImageResource(R.mipmap.sound_volume_07);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    private void setSystemUIVisible(boolean show) {
        if (show) {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void showPopupWindow() {
        mResolutionPopupWindow.setAdapter(mAdapter);
        mResolutionPopupWindow.setWidth(mTextResolution.getWidth());
        mResolutionPopupWindow.showAsDropDown(mTextResolution);
    }

    private VideoProfilePopupWindow.OnSpinnerItemClickListener mOnResulutionOptionClickListener = new VideoProfilePopupWindow.OnSpinnerItemClickListener() {
        @Override
        public void onItemClick(int pos) {
            mVideoProfileSelect = pos;
            sdkEngine.changePushResolution(PRTCVideoProfile.matchValue(mVideoProfileSelect));
            mTextResolution.setText(mResolutionOption.get(mVideoProfileSelect));
            mResolutionPopupWindow.dismiss();
        }
    };

    private UVCCamera initUVCCamera(USBMonitor.UsbControlBlock ctrlBlock) {
        Log.d(TAG, "initUVCCamera-----mVideoProfileSelect:" + mVideoProfileSelect + " width:" + PRTCVideoProfile.matchValue(mVideoProfileSelect).getWidth()
                + " height:" + PRTCVideoProfile.matchValue(mVideoProfileSelect).getHeight());
        final UVCCamera camera = new UVCCamera();
        camera.open(ctrlBlock);
        camera.setPreviewSize(
                PRTCVideoProfile.matchValue(mVideoProfileSelect).getWidth(),
                PRTCVideoProfile.matchValue(mVideoProfileSelect).getHeight(),
                UVCCamera.FRAME_FORMAT_YUYV
        );

        //SurfaceTexture surface= mLocalVideoView.getSurfaceTexture();
        //PRTCSurfaceViewRenderer surface = mLocalVideoView.getSurfaceView();

        // Start preview to external GL texture
        // NOTE : this is necessary for callback passed to [UVCCamera.setFrameCallback]
        // to be triggered afterwards
        //camera.setPreviewTexture(surface);
        camera.startPreview();

        camera.setFrameCallback(new IFrameCallback() {
            @Override
            public void onFrame(ByteBuffer frame) {
                createFrameByteBuffer(frame);
            }
        },mUVCCameraFormat);
        return camera;
    }

    //外置数据输入监听
    private IPRTCDataProvider mIPRTCDataProvider = new IPRTCDataProvider() {
        private ByteBuffer cacheBuffer;
        private ByteBuffer videoSourceData;

        @Override
        public ByteBuffer provideRGBData(List<Integer> params) {
            videoSourceData = mQueueByteBuffer.poll();
            if (videoSourceData == null) {
                //Log.d("RTCLiveActivity", "provideRGBData: " + null);
                return null;
            } else {
                //Log.d("RTCLiveActivity", "provideRGBData: ! = null");
                params.add(mRTCVideoFormat);
                params.add(PRTCVideoProfile.matchValue(mVideoProfileSelect).getWidth());
                params.add(PRTCVideoProfile.matchValue(mVideoProfileSelect).getHeight());
                if (cacheBuffer == null) {
                    cacheBuffer = sdkEngine.getNativeOpInterface().
                            createNativeByteBuffer(1280 * 720 * 4);
                } else {
                    cacheBuffer.clear();
                }
                cacheBuffer = videoSourceData;
                videoSourceData = null;
                //Log.d("RTCLiveActivity", "provideRGBData finish" + Thread.currentThread());
                cacheBuffer.position(0);
                return cacheBuffer;
            }
        }

        @Override
        public void releaseBuffer() {
            if(videoSourceData != null){
                videoSourceData = null;
            }
            if(cacheBuffer != null){
                sdkEngine.getNativeOpInterface().realeaseNativeByteBuffer(cacheBuffer);
                cacheBuffer = null;
            }
        }
    };

    //摄像数据输出监听
    private IPRTCDataReceiver mIPRTCDataReceiver = new IPRTCDataReceiver() {
        //private int limit = 0;
        private ByteBuffer cache;

        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            //Log.d("MainActivity", "onReceiveRGBAData!");

/*            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/yuvrgba"+ limit+".jpg";
            if (limit++ < 5) {
                File file = new File(name);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }

        @Override
        public int getType() {
            return IPRTCDataReceiver.I420_TO_ABGR;
        }

        @Override
        public ByteBuffer getCacheBuffer() {
            if(cache == null){
                //根据需求来，设置最大的可能用到的buffersize，后续回调会复用这块内存
                int size = 4096*2160*4;
                cache = sdkEngine.getNativeOpInterface().
                        createNativeByteBuffer(4096*2160*4);
            }
            cache.clear();
            return cache;
        }

        @Override
        public void releaseBuffer() {
            if(cache != null)
                sdkEngine.getNativeOpInterface().realeaseNativeByteBuffer(cache);
            cache = null;
        }
    };

    private void createFrameByteBuffer(ByteBuffer frame){
        try {
            if(frame != null){
                //add videoSource
                mQueueByteBuffer.offer(frame, 1, TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            });
        }
    }

    private void updateVideoFormat(int videoFormat){
        switch (videoFormat) {
            case CommonUtils.nv21_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_NV21;
                mRTCVideoFormat = IPRTCDataProvider.NV21;
                break;
            case CommonUtils.nv12_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_YUV420SP;
                mRTCVideoFormat = IPRTCDataProvider.NV12;
                break;
            case CommonUtils.i420_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_I420;
                mRTCVideoFormat = IPRTCDataProvider.I420;
                break;
            case CommonUtils.rgba_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_RGBX;
                mRTCVideoFormat = IPRTCDataProvider.RGBA_TO_I420;
                break;
            case CommonUtils.argb_format:
                //UVCCamera不支持输出argb格式，测试用rgbx格式，输出时颜色会有偏差
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_ARGB;
                mRTCVideoFormat = IPRTCDataProvider.ARGB_TO_I420;
                break;
            case CommonUtils.rgb24_format:
                //UVCCamera的RGB888与libyuv的数据有大小端区别，所以UVCCamera输出使用BGR888,保证颜色正确
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_BGR888;
                mRTCVideoFormat = IPRTCDataProvider.RGB24_TO_I420;
                break;
            case CommonUtils.rgb565_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_RGB565;
                mRTCVideoFormat = IPRTCDataProvider.RGB565_TO_I420;
                break;
        }
    }

    private void setPreview(boolean onOff) {
        if (onOff) {
            mLocalRender.init();
            if (mExtendCameraCapture) {
                sdkEngine.startCameraPreview(
                        mLocalRender, PRTCScaleType.SCALE_ASPECT_FIT, null);
            } else {
                sdkEngine.startCameraPreview(
                        mLocalRender, PRTCScaleType.SCALE_ASPECT_FILL, null);
            }
        }
        else {
            sdkEngine.stopPreview(MEDIA_TYPE_VIDEO);
        }
    }

    private void setIconStats(boolean visible) {
        if (!visible) {
            //未发布时，按钮隐藏
            mImgPreview.setVisibility(View.VISIBLE);
            mTextPreview.setVisibility(View.VISIBLE);
            mImgMix.setVisibility(View.GONE);
            mTextMix.setVisibility(View.GONE);
            mImgLocalRecord.setVisibility(View.GONE);
            mTextLocalRecord.setVisibility(View.GONE);
            mImgScreenshot.setVisibility(View.GONE);
            mTextScreenshot.setVisibility(View.GONE);
            mImgRemoteRecord.setVisibility(View.GONE);
            mTextRemoteRecord.setVisibility(View.GONE);
            mImgBtnMuteMic.setVisibility(View.INVISIBLE);
            mImgBtnMuteVideo.setVisibility(View.INVISIBLE);
            mImgBtnMirror.setVisibility(View.INVISIBLE);
            mTextResolution.setVisibility(View.INVISIBLE);
            mImgLocalMixSound.setVisibility(View.GONE);
            mTextLocalMixSound.setVisibility(View.GONE);
            mImgRemoteMixSound.setVisibility(View.GONE);
            mTextRemoteMixSound.setVisibility(View.GONE);
            mImgControlMixSound.setVisibility(View.GONE);
            mTextControlMixSound.setVisibility(View.GONE);
        }
        else {
            mImgBtnMuteMic.setVisibility(View.VISIBLE);
            mImgBtnMuteVideo.setVisibility(View.VISIBLE);
            mTextResolution.setVisibility(View.VISIBLE);
            mImgBtnMirror.setVisibility(View.VISIBLE);
            mImgMix.setVisibility(View.VISIBLE);
            mTextMix.setVisibility(View.VISIBLE);
            mImgLocalRecord.setVisibility(View.VISIBLE);
            mTextLocalRecord.setVisibility(View.VISIBLE);
            mImgScreenshot.setVisibility(View.VISIBLE);
            mTextScreenshot.setVisibility(View.VISIBLE);
            mImgRemoteRecord.setVisibility(View.VISIBLE);
            mTextRemoteRecord.setVisibility(View.VISIBLE);
            mImgPreview.setVisibility(View.GONE);
            mTextPreview.setVisibility(View.GONE);
            mImgLocalMixSound.setVisibility(View.VISIBLE);
            mTextLocalMixSound.setVisibility(View.VISIBLE);
            mImgRemoteMixSound.setVisibility(View.VISIBLE);
            mTextRemoteMixSound.setVisibility(View.VISIBLE);
        }
    }

    private void refreshSettings() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);

        mCameraEnable = preferences.getBoolean(CommonUtils.CAMERA_ENABLE, CommonUtils.CAMERA_ON);
        mMicEnable = preferences.getBoolean(CommonUtils.MIC_ENABLE, CommonUtils.MIC_ON);
        mScreenEnable = preferences.getBoolean(CommonUtils.SCREEN_ENABLE, CommonUtils.SCREEN_OFF);
        int classType = preferences.getInt(CommonUtils.SDK_CLASS_TYPE, PRTCChannelProfile.ROOM_SMALL.ordinal());
        mClass = PRTCChannelProfile.valueOf(classType);
        mPublishMode = preferences.getInt(CommonUtils.PUBLISH_MODE, CommonUtils.AUTO_MODE);
        mScribeMode = preferences.getInt(CommonUtils.SUBSCRIBE_MODE, CommonUtils.AUTO_MODE);
        mExtendCameraCapture = preferences.getBoolean(CommonUtils.CAMERA_CAPTURE_MODE, false);
        mExtendVideoFormat = preferences.getInt(CommonUtils.EXTEND_CAMERA_VIDEO_FORMAT, CommonUtils.i420_format);
        updateVideoFormat(mExtendVideoFormat);

        //分辨率选择菜单
        String[] resolutions = getResources().getStringArray(R.array.videoResolutions);
        mResolutionOption.addAll(Arrays.asList(resolutions));

        Log.d(TAG, " Camera enable is: " + mCameraEnable + " Mic enable is: " + mMicEnable + " ScreenShare enable is: " + mScreenEnable);
        if (!mScreenEnable && !mCameraEnable && mMicEnable) {
            sdkEngine.setAudioOnlyMode(true);
        }
        else {
            sdkEngine.setAudioOnlyMode(false);
        }
        sdkEngine.configLocalCameraPublish(mCameraEnable);
        sdkEngine.configLocalAudioPublish(mMicEnable);
        if (isScreenCaptureSupport) {
            sdkEngine.configLocalScreenPublish(mScreenEnable);
        }
        else {
            sdkEngine.configLocalScreenPublish(false);
        }
    }

    @Override
    public View.OnClickListener provideSwapListener() {
        return mSwapRemoteLocalListener;
    }

    @Override
    public boolean isLocalStream(String uid) {
        return uid.equals(mUserid);
    }

    @Override
    public void stopRender(RTCVideoViewInfo info) {
        Log.d(TAG, "stop render: "+info.getmRenderview());
        if(info != null && info.getmRenderview()!= null){
            if(info.getStreamInfo().getUId().equals(mUserid)){
                Log.d(TAG, "stop old list cache local render: "+info.getmRenderview());
                sdkEngine.stopPreview(MEDIA_TYPE_VIDEO,info.getmRenderview());
            }else{
                Log.d(TAG, "stop old list cache remote render info: "+info.getStreamInfo());
                sdkEngine.stopRemoteView(info.getStreamInfo());
            }
        }
    }


    private void takeScreenShot(boolean isLocal, PRTCStreamInfo streamInfo){
        sdkEngine.takeSnapShot(isLocal,streamInfo,new IPRTCScreenShot() {
            @Override
            public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
                Log.d(TAG, "onReceiveRGBAData: rgbBuffer: " + rgbBuffer + " width: " + width + " height: " + height);
                final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);

                bitmap.copyPixelsFromBuffer(rgbBuffer);
                String name = "/mnt/sdcard/rtcscreen_" + System.currentTimeMillis() + "_local.jpg";
                File file = new File(name);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "screen shoot : " + name);
            }
        } );
    }
}

