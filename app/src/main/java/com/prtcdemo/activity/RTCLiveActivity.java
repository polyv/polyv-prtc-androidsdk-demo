package com.prtcdemo.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.polyv.prtc.sdkengine.define.PRTCCaptureMode;
import com.polyv.prtc.sdkengine.define.PRTCChannelProfile;
import com.polyv.prtc.sdkengine.define.PRTCClientRole;
import com.polyv.prtc.sdkengine.define.PRTCMediaType;
import com.polyv.prtc.sdkengine.define.PRTCMixProfile;
import com.polyv.prtc.sdkengine.define.PRTCStreamInfo;
import com.polyv.prtc.sdkengine.define.PRTCStreamType;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewGroup;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewRenderer;
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
import com.polyv.prtc.sdkengine.define.PRTCAuthInfo;
import com.polyv.prtc.sdkengine.define.PRTCErrorCode;
import com.polyv.prtc.sdkengine.define.PRTCMediaServiceStatus;
import com.polyv.prtc.sdkengine.define.PRTCNetWorkQuality;
import com.polyv.prtc.sdkengine.define.PRTCScaleType;
import com.polyv.prtc.sdkengine.define.PRTCStreamStatus;
import com.polyv.prtc.sdkengine.listener.IPRTCEngineBaseEvent;
import com.polyv.prtc.sdkengine.openinterface.IPRTCDataProvider;
import com.polyv.prtc.sdkengine.openinterface.IPRTCNotification;
import com.polyv.prtc.sdkengine.openinterface.IPRTCScreenShot;
import com.prtcdemo.R;
import com.prtcdemo.adpter.RemoteVideoAdapter;
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

import core.renderer.SurfaceViewGroup;

import static com.polyv.prtc.sdkengine.define.PRTCErrorCode.NET_ERR_CODE_OK;
import static com.polyv.prtc.sdkengine.define.PRTCMediaType.MEDIA_TYPE_VIDEO;
import static com.prtcdemo.utils.CommonUtils.BUCKET;
import static com.prtcdemo.utils.CommonUtils.REGION;

/**
 * @author ciel
 * @create 2020/7/2
 * @Describe
 */
public class RTCLiveActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, CameraDialog.CameraDialogParent {
    private static final String TAG = "RTCLiveActivity";
    private final String mBucket = "urtc-test";
    private final String mRegion = "cn-bj";
    private final int COL_SIZE_P = 3;
    private final int COL_SIZE_L = 6;

    private String mUserid = "test001";
    private String mRoomid = "urtc1";
    private String mRoomToken = "test token";
    private String mAppid = "";
    private String mPriAddr = "";

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
    private boolean mIsPriDeploy = false;
    @CommonUtils.PubScribeMode
    private int mPublishMode;
    @CommonUtils.PubScribeMode
    private int mScribeMode;
    private int mVideoProfileSelect;
    private int localViewWidth_portrait;
    private int localViewHeight_portrait;
    private int localViewWidth_landscape;
    private int localViewHeight_landscape;
    private int screenWidth;
    private int screenHeight;
    private boolean mExtendCameraCapture;
    private int mExtendVideoFormat;
    private int mUVCCameraFormat;
    private int mRTCVideoFormat;

    IPRTCEngine sdkEngine = null;
    private PRTCChannelProfile mClass;
    private PRTCStreamInfo mLocalStreamInfo;
    private PRTCAudioDevice defaultAudioDevice;
    // private List<PRTCStreamInfo> mSteamList;
    private List<String> mResolutionOption = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;
    private PRTCSurfaceViewRenderer mLocalVideoView = null; //Surfaceview
    //private PRTCSurfaceViewGroup mLocalVideoView = null; //PRTCSurfaceViewGroup
    private PRTCSurfaceViewGroup mMuteView = null;
    private PRTCMediaType mPublishMediaType;

    private GridLayoutManager gridLayoutManager;
    private RemoteVideoAdapter mVideoAdapter;
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
    //????????????
    private ImageView mImgSoundVolume = null;
    private ImageView mImgMicSts = null;
    //UVCCamera
    private USBMonitor mUSBMonitor = null;
    private UVCCamera mUVCCamera = null;
    private final Object mSync = new Object();
    private boolean isActive, isPreview;
    private boolean mLeaveRoomFlag;
    private PRTCStreamInfo latestRemoteInfo;
    private PRTCStreamInfo mSwapStreamInfo;
    //????????????????????????
    private ByteBuffer videoSourceData = null;
    private final Object extendByteBufferSync = new Object();
    private boolean mIsLocalMixingSound = false;
    private boolean mIsRemoteMixingSound = false;
    private boolean mIsPauseMixingSound = false;

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

        // ???????????????
        setContentView(R.layout.activity_living);
        mVideoProfileSelect = preferences.getInt(CommonUtils.videoprofile, CommonUtils.videoprofilesel);
        mRemoteGridView = findViewById(R.id.remoteGridView);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_L);
        } else {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_P);
        }
        mRemoteGridView.setLayoutManager(gridLayoutManager);
        mVideoAdapter = new RemoteVideoAdapter(this);
//        mVideoAdapter.setRemoveRemoteStreamReceiver(mRemoveRemoteStreamReceiver);
        mRemoteGridView.setAdapter(mVideoAdapter);

        mLocalVideoView = findViewById(R.id.localvideoview);
        //Surfaceview ????????????
        mLocalVideoView.init();
        mLocalVideoView.setVisibility(View.INVISIBLE);
//        mLocalVideoView.setZOrderMediaOverlay(false);
//        mLocalVideoView.setMirror(true);
        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.setScrimColor(0x00ffffff);
        mDrawerContent = findViewById(R.id.drawer_content);
        mDrawerMenu = findViewById(R.id.menu_drawer);
        timeShow = findViewById(R.id.timer);
        mDrawerContent.setPadding(0, StatusBarUtils.getStatusBarHeight(this), 0, 0);
        mTitleBar = findViewById(R.id.title_bar);
        mToolBar = findViewById(R.id.tool_bar);
        mImgBtnMore = findViewById(R.id.img_btn_more);
        mImgBtnSwitchCam = findViewById(R.id.img_btn_switch_camera);
        mImgBtnMuteMic = findViewById(R.id.img_btn_toggle_mic);
        mImgBtnMuteVideo = findViewById(R.id.img_btn_toggle_video);
        mImgBtnMuteSpeaker = findViewById(R.id.img_btn_speaker);
        mImgBtnMirror = findViewById(R.id.img_btn_mirror);
        mImgBtnEndCall = findViewById(R.id.img_btn_endcall);
        StatusBarUtils.setColor(this, getResources().getColor(R.color.color_7F04A5EB));
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

        // ????????????????????????
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

        mIsPriDeploy = preferences.getBoolean(CommonUtils.PRIVATISATION_MODE, false);
        PRTCEnvHelper.setPrivateDeploy(mIsPriDeploy);
        mPriAddr = preferences.getString(CommonUtils.PRIVATISATION_ADDRESS, "");
        if (mIsPriDeploy) {
            PRTCEnvHelper.setPrivateDeployRoomURL(mPriAddr);
        }

        mExtendCameraCapture = preferences.getBoolean(CommonUtils.CAMERA_CAPTURE_MODE, false);
        mExtendVideoFormat = preferences.getInt(CommonUtils.EXTEND_CAMERA_VIDEO_FORMAT, CommonUtils.i420_format);
        updateVideoFormat(mExtendVideoFormat);
        // mSteamList = new ArrayList<>();

        //?????????
        mTextRoomId = findViewById(R.id.roomid_text);
        mTextRoomId.setText("?????????:" + mRoomid);
        mMirror = PRTCEnvHelper.isFrontCameraMirror();
        mImgBtnMirror.setImageResource(mMirror ? R.mipmap.mirror_on :
                R.mipmap.mirror);
        //?????????????????????
        String[] resolutions = getResources().getStringArray(R.array.videoResolutions);
        mResolutionOption.addAll(Arrays.asList(resolutions));

        //??????ID
        mTextUserId = findViewById(R.id.userid_text);
        mTextUserId.setText("??????ID:" + mUserid);

        Log.d(TAG, " Camera enable is: " + mCameraEnable + " Mic enable is: " + mMicEnable + " ScreenShare enable is: " + mScreenEnable);
        if (!mScreenEnable && !mCameraEnable && mMicEnable) {
            sdkEngine.setAudioOnlyMode(true);
        } else {
            sdkEngine.setAudioOnlyMode(false);
        }
        sdkEngine.configLocalCameraPublish(mCameraEnable);
        sdkEngine.configLocalAudioPublish(mMicEnable);
        if (isScreenCaptureSupport) {
            sdkEngine.configLocalScreenPublish(mScreenEnable);
            if (mScreenEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                IPRTCEngine.regScreenCaptureNotification(mScreenCaptureNotification);
            }
        } else {
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
        sdkEngine.setScreenProfile(PRTCVideoProfile.VIDEO_PROFILE_1920_1080);

        synchronized (extendByteBufferSync) {
            videoSourceData = sdkEngine.getNativeOpInterface().
                    createNativeByteBuffer(1280 * 720 * 4);
            videoSourceData.clear();
        }
        //?????????????????????
        mTextResolution.setText(mResolutionOption.get(mVideoProfileSelect));
        mAdapter = new ArrayAdapter<String>(this, R.layout.videoprofile_item, mResolutionOption);

        mResolutionPopupWindow = new VideoProfilePopupWindow(this);
        mResolutionPopupWindow.setOnSpinnerItemClickListener(mOnResulutionOptionClickListener);
        if (mExtendCameraCapture) {
            //?????????????????????
            PRTCEnvHelper.setCaptureMode(
                    PRTCCaptureMode.CAPTURE_MODE_EXTEND.ordinal());
            mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
            IPRTCEngine.onRGBCaptureResult(mIPRTCDataProvider);
            mTextResolution.setVisibility(View.GONE);
            mImgBtnSwitchCam.setVisibility(View.GONE);
        } else {
            PRTCEnvHelper.setCaptureMode(
                    PRTCCaptureMode.CAPTURE_MODE_LOCAL.ordinal());
        }
        if (mPublishMode == CommonUtils.AUTO_MODE) {
            mImgPreview.setVisibility(View.GONE);
            mTextPreview.setVisibility(View.GONE);
            mImgControlMixSound.setVisibility(View.GONE);
            mTextControlMixSound.setVisibility(View.GONE);
        } else {
            //??????????????????????????????
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
//                update(PRTCMixProfile.MIX_TYPE_UPDATE);
//                sdkEngine.queryMix();
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

        //??????????????????
        mImgManualPubVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoIsPublished) {
                    sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    // ??????????????????
                    refreshSettings();
                    if (mCameraEnable || mMicEnable) {
                        if (!mVideoIsPublished) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, mCameraEnable, mMicEnable).getErrorCode());
                        }
                    } else {
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
                        ToastUtils.shortShow(RTCLiveActivity.this, errorMessage.toString());
                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this, "??????");
                    }
                } else {
                    sdkEngine.unPublish(MEDIA_TYPE_VIDEO);
                }
            }
        });

        //??????????????????
        mImgManualPubScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScreenIsPublished) {
                    sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    // ??????????????????
                    refreshSettings();
                    if (mScreenEnable && !mScreenIsPublished) {
                        if (isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(PRTCMediaType.MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        } else {
                            errorMessage.append("???????????????????????????\n");
                            //results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                        }
                    } else {
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
                        ToastUtils.shortShow(RTCLiveActivity.this, errorMessage.toString());
                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this, "??????");
                    }
                } else {
                    sdkEngine.unPublish(PRTCMediaType.MEDIA_TYPE_SCREEN);
                }
            }
        });

        //????????????
        mImgPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreview(!mIsPreview);
                mIsPreview = !mIsPreview;
                if (mIsPreview) {
                    mTextPreview.setText(R.string.stop_preview);
                    mLocalVideoView.setVisibility(View.VISIBLE);
                } else {
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
        Log.d(TAG, " roomtoken = " + mRoomToken + "appid : "+ mAppid + " userid :"+ mUserid);
        initRecordManager();
        // ????????????
        if (sdkEngine.joinChannel(info) == PRTCErrorCode.NET_ERR_SECKEY_NULL
                || mAppid.length() == 0) {
            ToastUtils.shortShow(RTCLiveActivity.this, "?????????????????????AppKey???AppId????????????");
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
        if (mVideoIsPublished || mScreenIsPublished) {
            if (!mLeaveRoomFlag) {
                Intent service = new Intent(this, RTCForeGroundService.class);
                startService(service);
            }
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
        Intent service = new Intent(this, RTCForeGroundService.class);
        stopService(service);
        endCall();
        releaseExtendCamera();
        //onMediaServerDisconnect();
        System.gc();
    }

    private IPRTCEngineEventHandler eventListener = new IPRTCEngineEventHandler() {
        @Override
        public void onServerDisconnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerDisconnect: ");
                    ToastUtils.shortShow(RTCLiveActivity.this, " ??????????????????");
                    stopTimeShow();
                    onMediaServerDisconnect();
                }
            });
        }

        @Override
        public void onJoinRoomResult(int code, String msg, String roomid) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) { // ??????
                        ToastUtils.shortShow(RTCLiveActivity.this, " ??????????????????");
                        startTimeShow();
                    } else { // ??????
                        ToastUtils.shortShow(RTCLiveActivity.this, " ?????????????????? " +
                                code + " errmsg " + msg);
                        Intent intent = new Intent(RTCLiveActivity.this, ConnectActivity.class);
                        onMediaServerDisconnect();
                        startActivity(intent);
                        finish();
                    }

                }
            });
        }

        @Override
        public void onLeaveRoomResult(int code, String msg, String roomid) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveActivity.this, " ???????????? " +
                            code + " errmsg " + msg);
//                    releaseExtendCamera();
//                    onMediaServerDisconnect();
//                    System.gc();
                }
            });
        }

        @Override
        public void onRejoiningRoom(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "rejoining room");
                    ToastUtils.shortShow(RTCLiveActivity.this, " ???????????????????????? ");
                    stopTimeShow();
                }
            });
        }

        @Override
        public void onRejoinRoomResult(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveActivity.this, "?????????????????????");
                    startTimeShow();
                }
            });
        }

        @Override
        public void onLocalPublish(int code, String msg, PRTCStreamInfo info) {
            // ???????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        int mediatype = info.getMediaType().ordinal(); // ????????????????????????????????????????????????
                        mPublishMediaType = PRTCMediaType.matchValue(mediatype);
                        if (mediatype == MEDIA_TYPE_VIDEO.ordinal()) { // ????????????
                            mImgManualPubVideo.setImageResource(R.mipmap.stop); // ??????????????????
                            mTextManualPubVideo.setText(R.string.pub_cancel_video); // ??????????????????
                            mVideoIsPublished = true;
                            if (!sdkEngine.isAudioOnlyMode()) {  // ???????????????
                                // PRTCSurfaceViewGroup??????
                                //mLocalVideoView.init(false);
                                // Surfaceview??????

                                // ??????view????????????
                                mLocalVideoView.setBackgroundColor(Color.TRANSPARENT);
                                mLocalVideoView.setVisibility(View.VISIBLE);
                                // ???????????????????????????
                                if (RTCLiveActivity.this.getResources().getConfiguration().orientation
                                        == Configuration.ORIENTATION_LANDSCAPE) {
                                    Log.i("info", "landscape"); // ??????
                                    localViewWidth_landscape = mLocalVideoView.getMeasuredWidth();
                                    localViewHeight_landscape = mLocalVideoView.getMeasuredHeight();
                                    localViewWidth_portrait = screenWidth;
                                    localViewHeight_portrait = screenHeight - mToolBar.getHeight() - mTitleBar.getHeight();
                                }
                                else if (RTCLiveActivity.this.getResources().getConfiguration().orientation
                                        == Configuration.ORIENTATION_PORTRAIT) {
                                    Log.i("info", "portrait"); // ??????
                                    localViewWidth_portrait = mLocalVideoView.getMeasuredWidth();
                                    localViewHeight_portrait = mLocalVideoView.getMeasuredHeight();
                                    localViewWidth_landscape = screenHeight;
                                    localViewHeight_landscape = screenWidth - mTitleBar.getHeight() - mToolBar.getHeight();
                                }

                                if (!mIsPreview) {
                                    if (mExtendCameraCapture) { // ???????????????????????????
                                        sdkEngine.renderLocalView(info,
                                                mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FIT, null);
                                    } else { // ???????????????????????????
                                        sdkEngine.renderLocalView(info,
                                                mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FIT, null);
                                    }
                                    //if (mPublishMode != CommonUtils.AUTO_MODE) {
                                    // setIconStats(true);
                                    //}
                                } else {
                                    //setIconStats(true);
                                }
                                // ????????????
                                mLocalStreamInfo = info;
                                mSwapStreamInfo = info;
                                mLocalVideoView.setTag(mLocalStreamInfo);
                                mLocalVideoView.setOnClickListener(mToggleScreenOnClickListener);
                            }
                        } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN.ordinal()) { // ?????????
                            // ???????????????????????????
                            mScreenIsPublished = true;
                            mImgManualPubScreen.setImageResource(R.mipmap.stop);
                            mTextManualPubScreen.setText(R.string.pub_cancel_screen);
                            if (mScreenEnable) { // ???????????????????????????
                                //sdkEngine.startPreview(info.getMediaType(), mLocalVideoView,PRTCScaleType.SCALE_ASPECT_FILL,null);
                            }
                        }
                        // ????????????????????????
                        setIconStats(true);
                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this,
                                "?????????????????? " + code + " errmsg " + msg);
                    }

                }
            });
        }

        @Override
        public void onLocalUnPublish(int code, String msg, PRTCStreamInfo info) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (info.getMediaType() == MEDIA_TYPE_VIDEO) { // ????????????
                            // ????????????
                            if (mPublishMode == CommonUtils.AUTO_MODE) {
                                mImgManualPubVideo.setVisibility(View.GONE);
                                mTextManualPubVideo.setVisibility(View.GONE);
                            } else {
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
                        } else if (info.getMediaType() == PRTCMediaType.MEDIA_TYPE_SCREEN) { //?????????
                            mScreenIsPublished = false;
                            if (mPublishMode == CommonUtils.AUTO_MODE) {
                                mImgManualPubScreen.setVisibility(View.GONE);
                                mTextManualPubScreen.setVisibility(View.GONE);
                            } else {
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
                        ToastUtils.shortShow(RTCLiveActivity.this, "??????????????????");
                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this, "?????????????????? "
                                + code + " errmsg " + msg);
                    }
                }
            });
        }

        @Override
        public void onLocalUnPublishOnly(int code, String msg, PRTCStreamInfo info) {

        }

        @Override
        public void onRemoteUserJoin(String uid) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveActivity.this, " ?????? "
                            + uid + " ???????????? ");
                }
            });
        }

        @Override
        public void onRemoteUserLeave(String uid, int reason) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "remote user " + uid + "leave ,reason: " + reason);
                    //onUserLeave(uid);
                    ToastUtils.shortShow(RTCLiveActivity.this, " ?????? " +
                            uid + " ?????????????????????????????? " + reason);
                }
            });
        }

        @Override
        public void onRemotePublish(PRTCStreamInfo info) {
            // ?????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????userid,
                    // ????????????????????????userid????????????????????????????????????????????????????????????????????????
                    Log.d(TAG, "onRemotePublish: " + info.getUId() + " me : " + mUserid);
                    if (!mUserid.equals(info.getUId())) {
                        // mSteamList.add(info);
                        if (!sdkEngine.isAutoSubscribe()) { // ????????????????????????????????????????????????
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
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, " onRemoteUnPublish " + info.getMediaType() + " " + info.getUId());
                    ToastUtils.shortShow(RTCLiveActivity.this, " ?????? " +
                            info.getUId() + " ??????????????? " + info.getMediaType()); // ??????????????????
                    String mkey = info.getUId() + info.getMediaType().toString();
                    if(mSwapStreamInfo!= null && mSwapStreamInfo.getUId().equals(info.getUId()) && mSwapStreamInfo.getMediaType().toString().equals(info.getMediaType().toString())){
                        sdkEngine.stopRemoteView(mSwapStreamInfo); // ???????????????????????????
                        int localIndex  = mVideoAdapter.getPositionByKey(mUserid + mPublishMediaType.toString());
                        if(localIndex >= 0){
                            Log.d(TAG," onRemoteUnPublish localIndex "+ localIndex);
                            mkey = mUserid + mPublishMediaType.toString();
                            sdkEngine.stopPreview(mPublishMediaType);
                            sdkEngine.renderLocalView(mLocalStreamInfo,mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FILL,null);
                            mSwapStreamInfo = mLocalStreamInfo;
                        }
                    }else{
                        sdkEngine.stopRemoteView(info);
                    }
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(mkey);
                    }

                    //mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                    //refreshStreamInfoText();
                }
            });
        }

        @Override
        public void onSubscribeResult(int code, String msg, PRTCStreamInfo info) {
            // ??????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) { // ????????????
                        RTCVideoViewInfo vinfo = new RTCVideoViewInfo();
                        PRTCSurfaceViewGroup videoView = null;
                        // PRTCSurfaceViewGroup videoViewCallBack = null; // ????????????????????????

                        //PRTCSurfaceViewRenderer videoView = null;
                        Log.d(TAG, " subscribe info: " + info);
                        latestRemoteInfo = info;
                        if (info.isHasVideo()) { // ???????????????????????????
//                            PRTCSurfaceViewGroup ?????????viewgroup,RTCVideoViewInfo
                            videoView = new PRTCSurfaceViewGroup(getApplicationContext());
                            PRTCSurfaceViewRenderer surfaceViewRenderer = new PRTCSurfaceViewRenderer(getApplicationContext());
                            videoView.init(false, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio}, surfaceViewRenderer);
                            // PRTCSurfaceViewRenderer
                            //videoView = new PRTCSurfaceViewRenderer(getApplicationContext());// ?????????????????????
                            //videoView.init();
                            videoView.setTag(info);
                            videoView.setId(R.id.video_view);
                            //?????????????????????????????????????????????
                            //videoViewCallBack = new PRTCSurfaceViewGroup(getApplicationContext());
                            //videoViewCallBack.setFrameCallBack(mIPRTCDataReceiver);
                            //videoViewCallBack.init(false);
                            //????????????
                            //videoView.setOnClickListener(mScreenShotOnClickListener);
                            //????????????
                            videoView.setOnClickListener(mSwapRemoteLocalListener);
                        }
                        vinfo.setmRenderview(videoView);
                        vinfo.setmUid(info.getUId());
                        vinfo.setmMediatype(info.getMediaType());
                        vinfo.setmEanbleVideo(info.isHasVideo());
                        vinfo.setEnableAudio(info.isHasAudio());
                        String mkey = info.getUId() + info.getMediaType().toString();
                        vinfo.setKey(mkey);
                        //?????????????????????????????????????????????
                        if (mVideoAdapter != null) {
                            vinfo.setStreamInfo(info);
                            mVideoAdapter.addStreamView(mkey, vinfo, info);
                        }

                        if (videoView != null) {
                            sdkEngine.startRemoteView(info, videoView, PRTCScaleType.SCALE_ASPECT_FIT, null); // ???????????????
                            //videoView.refreshRemoteOp(View.VISIBLE);
                        }
                        //if (videoViewCallBack != null) {
                            // sdkEngine.startRemoteView(info, videoViewCallBack, PRTCScaleType.SCALE_ASPECT_FILL, null); // ???????????????????????????????????????
                        //}
                        //??????????????????????????????????????????????????????
                        //mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                        //refreshStreamInfoText();
                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this, " ????????????  " +
                                info.getUId() + " ??? " + info.getMediaType() + " ?????? " +
                                " code " + code + " msg " + msg);
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(int code, String msg, PRTCStreamInfo info) {
            // ????????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RTCLiveActivity.this, " ?????????????????? " +
                            info.getUId() + " ?????? " + info.getMediaType());
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(info.getUId() + info.getMediaType().toString()); // ?????????????????????
                    }
                    //??????????????????????????????
                    //mSpinnerPopupWindowScribe.addStreamInfo(info, true);
                }
            });
        }

        @Override
        public void onLocalStreamMuteRsp(int code, String msg, PRTCMediaType mediatype, PRTCTrackType tracktype, boolean mute) {
            // ?????????????????????
            Log.d(TAG, " code " + code + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) { // mute?????????????????????
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
            // ?????????????????????
            Log.d(TAG, " code " + code + " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {// mute?????????????????????
                        String mkey = uid + mediatype.toString();
                        Log.d(TAG, " onRemoteStreamMuteRsp " + mkey + " " + mVideoAdapter);
                        if (tracktype == PRTCTrackType.TRACK_TYPE_AUDIO) {
                            mRemoteAudioMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteAudio(mute);
                            }
                        } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                            mRemoteVideoMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteVideo(mute);
                            }
                        }

                    } else {
                        ToastUtils.shortShow(RTCLiveActivity.this, "mute " + mediatype + "failed with code: " + code);
                    }
                }
            });
        }

        @Override
        public void onRemoteTrackNotify(String uid, PRTCMediaType mediatype, PRTCTrackType tracktype, boolean mute) {
            // ???????????????????????????
            Log.d(TAG, " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // ???????????????????????????
                    if (mediatype == MEDIA_TYPE_VIDEO) {
                        String cmd = mute ? "??????" : "??????";
                        if (tracktype == PRTCTrackType.TRACK_TYPE_AUDIO) {
                            ToastUtils.shortShow(RTCLiveActivity.this, " ?????? " +
                                    uid + cmd + " ?????????");
                        } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                            ToastUtils.shortShow(RTCLiveActivity.this, " ?????? " +
                                    uid + cmd + " ?????????");
                        }

                    } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                        String cmd = mute ? "??????" : "??????";
                        ToastUtils.shortShow(RTCLiveActivity.this, " ?????? " +
                                uid + cmd + " ?????????");
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
            // ????????????????????????
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
            // ????????????????????????
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
            // ?????????????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RTCLiveActivity.this, " ??????????????? code " +
                            code);
                    Log.d(TAG, " user kickoff reason " + code);
                    Intent intent = new Intent(RTCLiveActivity.this, ConnectActivity.class);
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
                        ToastUtils.shortShow(RTCLiveActivity.this, "sdp swap failed");
                    }
                }
            });
        }

        @Override
        public void onRecordStop(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RTCLiveActivity.this, "????????????: " + (code == NET_ERR_CODE_OK.ordinal() ? "??????" : "??????: " + code));
                }
            });
        }

        @Override
        public void onQueryMix(int code, String msg, int type, String mixId, String fileName) {
            Log.d(TAG, "onQueryMix: "+ code + " msg: "+ msg + " type: "+ type);
        }

        @Override
        public void onRecordStatusNotify(PRTCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String fileName) {
            // ??????????????????
            Log.d(TAG, "onRecordStatusNotify " + status + " code: " + code + " msg: " + msg + " userid " + userId + " roomid: " + roomId + " mixId: " + mixId + "fileName: " + fileName);
            if(status == PRTCMediaServiceStatus.RECORD_STATUS_START_REQUEST_SEND){ // ?????????????????????
                Log.d(TAG, "???????????????????????????: ");
            }
            else if (status == PRTCMediaServiceStatus.RECORD_STATUS_START) { // ??????????????????
                String videoPath = "http://" + mBucket + "." + mRegion + ".ufileos.com/" + fileName; // ??????????????????
                Log.d(TAG, "remote record path: " + videoPath + ".mp4");
                // ?????????????????????
                ToastUtils.longShow(RTCLiveActivity.this, "????????????: " + videoPath);
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
            } else if (status == PRTCMediaServiceStatus.STATUS_UPDATE_REQUEST_SEND) {
                Log.d(TAG, "update ???????????????????????????: ");
            } else if (status == PRTCMediaServiceStatus.STATUS_UPDATE_ADD_STREAM_SUCCESS) {
                Log.d(TAG, "update ????????????: ");
            } else {
                ToastUtils.longShow(RTCLiveActivity.this, "????????????: ?????????" + code);
            }
        }

        @Override
        public void onRelayStatusNotify(PRTCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String[] pushUrls) {
            // ??????????????????
            Log.d(TAG, "onRelayStatusNotify " + status + " code: " + code + " msg: " + msg + " userid " + userId + " roomid: " + roomId + " mixId: " + mixId);
            if (pushUrls != null) {
                for (int i = 0; i < pushUrls.length; i++) {
                    Log.d(TAG, "onRelayStatusNotify: pushUrl " + pushUrls[i]); // ????????????
                }
            }
            if(status == PRTCMediaServiceStatus.RELAY_STATUS_START_REQUEST_SEND){
                Log.d(TAG, "???????????????????????????: ");
            }
            else if (status == PRTCMediaServiceStatus.RELAY_STATUS_START) { // ????????????
                // ulive cdn watch address: http://rtchls.ugslb.com/rtclive/roomid.flv
                // ?????????????????????
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
                }
            }
            else if (status == PRTCMediaServiceStatus.STATUS_UPDATE_REQUEST_SEND) {
                Log.d(TAG, "update ???????????????????????????: ");
            } else if (status == PRTCMediaServiceStatus.STATUS_UPDATE_ADD_STREAM_SUCCESS) {
                Log.d(TAG, "update ????????????: ");
            } else {
                ToastUtils.longShow(RTCLiveActivity.this, "????????????: ?????????" + code);
            }
        }

        @Override
        public void onAddStreams(int code, String msg) {
            // ????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onAddStreams: " + code + msg);
                }
            });
        }

        @Override
        public void onDelStreams(int code, String msg) {
            // ????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDelStreams: " + code + msg);
                }
            });
        }

        @Override
        public void onLogOffUsers(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onLogOffUsers: " + code + " msg: " + msg);
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
                    Log.d(TAG, "onLogOffUsers: " + cmdType + " userId: " + userId);
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
            // ????????????????????????
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
            // ??????????????????
//            Log.d(TAG, "onNetWorkQuality: userid: " + userId + " streamType: " + streamType + " mediatype : " + mediaType + " quality: " + quality);
        }

        @Override
        public void onAudioFileFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onAudioFileFinish" );

                    if ( mIsLocalMixingSound){
                        // ???????????????
                        mImgLocalMixSound.setImageResource(R.mipmap.local_mix_sound);
                        mTextLocalMixSound.setText(R.string.start_local_mix_sound);
                        if (mVideoIsPublished) {
                            mImgRemoteMixSound.setVisibility(View.VISIBLE);
                            mTextRemoteMixSound.setVisibility(View.VISIBLE);
                        }
                        mIsLocalMixingSound = false;
                    }
                    else if (mIsRemoteMixingSound) {
                        // ???????????????
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

    private PRTCSurfaceViewGroup.RemoteOpTrigger mOnRemoteOpTrigger = new PRTCSurfaceViewGroup.RemoteOpTrigger() {
        @Override
        public void onRemoteVideo(View v, SurfaceViewGroup parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                PRTCStreamInfo swapStreamInfo = (PRTCStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteVideo(swapStreamInfo.getUId(), !mRemoteVideoMute);
            } else if (parent.getTag() != null) {
                PRTCStreamInfo streamInfo = (PRTCStreamInfo) parent.getTag();
                sdkEngine.muteRemoteVideo(streamInfo.getUId(), !mRemoteVideoMute);
            }
            mMuteView = (PRTCSurfaceViewGroup) parent;
        }

        @Override
        public void onRemoteAudio(View v, SurfaceViewGroup parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                PRTCStreamInfo swapStreamInfo = (PRTCStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteAudio(swapStreamInfo.getUId(), !mRemoteAudioMute);
            } else if (parent.getTag() != null) {
                PRTCStreamInfo streamInfo = (PRTCStreamInfo) parent.getTag();
                sdkEngine.muteRemoteAudio(streamInfo.getUId(), !mRemoteAudioMute);
            }
            mMuteView = (PRTCSurfaceViewGroup) parent;
        }
    };

    private RemoteVideoAdapter.RemoveRemoteStreamReceiver mRemoveRemoteStreamReceiver = new RemoteVideoAdapter.RemoveRemoteStreamReceiver() {
        @Override
        public void onRemoteStreamRemoved(boolean swaped) {
            if (swaped) {
                if (mClass == PRTCChannelProfile.ROOM_SMALL) {
                    sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
                    sdkEngine.renderLocalView(mLocalStreamInfo, mLocalVideoView, null, null);
                } else if (mLocalVideoView.getTag(R.id.swap_info) != null) {
                    PRTCStreamInfo remoteStreamInfo = (PRTCStreamInfo) mLocalVideoView.getTag(R.id.swap_info);
                    sdkEngine.stopRemoteView(remoteStreamInfo);
                }
            }
        }
    };

    IPRTCRecordListener mLocalRecordListener = new IPRTCRecordListener() {
        @Override
        public void onLocalRecordStart(String path, int code, String msg) {
            Log.d(TAG, "onLocalRecordStart: " + path + " code: " + code + " msg: " + msg);
        }

        @Override
        public void onLocalRecordStop(String path, long fileLength, int code) {
            Log.d(TAG, "onLocalRecordStop: " + path + "fileLength: " + fileLength + "code: " + code);
        }

        @Override
        public void onRecordStatusCallBack(long duration, long fileSize) {
            Log.d(TAG, "onRecordStatusCallBack duration: " + duration + " fileSize: " + fileSize);
        }
    };

    private View.OnClickListener mToggleScreenOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // ????????????????????????
            toggleFullScreen();
        }
    };

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() { // ??????usb????????????
        @Override
        public void onAttach(final UsbDevice device) {
            Log.v(TAG, "onAttach:");
            ToastUtils.shortShow(RTCLiveActivity.this, "USB??????????????????");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUSBMonitor != null) {
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
            ToastUtils.shortShow(RTCLiveActivity.this, "USB??????????????????");
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    private View.OnClickListener mSwapRemoteLocalListener = new View.OnClickListener() { // ?????????????????????
        @Override
        public void onClick(View v) {
            if (v instanceof PRTCSurfaceViewGroup) {
                PRTCStreamInfo clickStreamInfo = (PRTCStreamInfo) v.getTag();
                boolean swapLocal = mSwapStreamInfo.getUId().equals(mUserid);
                boolean clickLocal = clickStreamInfo.getUId().equals(mUserid);
                Log.d(TAG, "mSwapStreamInfo: "+ mSwapStreamInfo + " clickInfo: " + clickStreamInfo);
                Log.d(TAG, "onClick swaplocal"+ swapLocal + " clickLocal: " + clickLocal);
                if(swapLocal && !clickLocal){
                    sdkEngine.stopRemoteView(clickStreamInfo);
                    sdkEngine.stopPreview(mSwapStreamInfo.getMediaType());
//                        sdkEngine.renderLocalView(mSwapStreamInfo, v,PRTCScaleType.SCALE_ASPECT_FILL, null);
                    PRTCSurfaceViewRenderer remoteRender = (PRTCSurfaceViewRenderer)v.getTag(R.id.render);
                    sdkEngine.renderLocalView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL, null);
//                        sdkEngine.startRemoteView(clickStreamInfo, mLocalVideoView,PRTCScaleType.SCALE_ASPECT_FILL,null);
                    sdkEngine.startRemoteView(clickStreamInfo, mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FILL,null);
                        ((PRTCSurfaceViewGroup) v).refreshRemoteOp(View.INVISIBLE);
                }else if(!swapLocal && clickLocal){
                    sdkEngine.stopRemoteView(mSwapStreamInfo);
                    sdkEngine.stopPreview(clickStreamInfo.getMediaType());
                    PRTCSurfaceViewRenderer remoteRender = (PRTCSurfaceViewRenderer)v.getTag(R.id.render);
                    sdkEngine.renderLocalView(clickStreamInfo, mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    sdkEngine.startRemoteView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                        ((PRTCSurfaceViewGroup) v).refreshRemoteOp(View.VISIBLE);
                }else if(!swapLocal && !clickLocal){
                    sdkEngine.stopRemoteView(mSwapStreamInfo);
                    sdkEngine.stopRemoteView(clickStreamInfo);
                    sdkEngine.startRemoteView(clickStreamInfo, mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    PRTCSurfaceViewRenderer remoteRender = (PRTCSurfaceViewRenderer)v.getTag(R.id.render);
                    sdkEngine.startRemoteView(mSwapStreamInfo, remoteRender, PRTCScaleType.SCALE_ASPECT_FILL,null);
                    }
                v.setTag(mSwapStreamInfo);
                mVideoAdapter.updateSwapInfo(clickStreamInfo,mSwapStreamInfo);
                mSwapStreamInfo = clickStreamInfo;
            }
        }
    };

    private void switchCamera() { // ????????????????????????
        sdkEngine.switchCamera();
        ToastUtils.shortShow(this, "???????????????");
        mSwitchCamera = !mSwitchCamera;
    }

    private boolean muteMic() { // ???????????????????????????
        sdkEngine.muteLocalMic(!mMuteMic);
        if (!mMuteMic) {
            ToastUtils.shortShow(RTCLiveActivity.this, "???????????????");
        } else {
            ToastUtils.shortShow(RTCLiveActivity.this, "???????????????");
        }
        return false;
    }

    private boolean muteVideo() { // ????????????????????????
        if (mScreenEnable || mCameraEnable) {
            if (isScreenCaptureSupport && !mCameraEnable) {
                sdkEngine.muteLocalVideo(!mMuteVideo, PRTCMediaType.MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteVideo, MEDIA_TYPE_VIDEO);
            }
        }
        if (!mMuteVideo) {
            ToastUtils.shortShow(RTCLiveActivity.this, "???????????????");
        } else {
            ToastUtils.shortShow(RTCLiveActivity.this, "???????????????");
        }
        return false;
    }

    private void muteSpeaker(boolean enable) { //??????????????????
        if (mSpeakerOn) {
            ToastUtils.shortShow(RTCLiveActivity.this, "????????????");
        } else {
            ToastUtils.shortShow(RTCLiveActivity.this, "????????????");
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
        } else {
            if (mute) {
//                localrenderview.refresh();
                mLocalVideoView.setVisibility(View.INVISIBLE);
            } else {
                mLocalVideoView.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onMuteMicResult(boolean mute) {
        mMuteMic = mute;
        if (mMuteMic) {
            mImgSoundVolume.setVisibility(View.INVISIBLE);
        } else {
            mImgSoundVolume.setVisibility(View.VISIBLE);
        }
        mImgBtnMuteMic.setImageResource(mMuteMic ? R.mipmap.mic_off :
                R.mipmap.mic);
        mImgMicSts.setImageResource(mMuteMic ? R.mipmap.mic_disable :
                R.mipmap.mic_volume);
    }

    private void mirrorSwitch() { // ???????????????????????????
        mMirror = !mMirror;
        PRTCEnvHelper.setFrontCameraMirror(mMirror);
        mImgBtnMirror.setImageResource(mMirror ? R.mipmap.mirror_on :
                R.mipmap.mirror);
    }

    private void endCall() { // ????????????
        sdkEngine.leaveChannel().ordinal();
        mLeaveRoomFlag = true;
//        Intent intent = new Intent(RTCLiveActivity.this, ConnectActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        releaseExtendCamera();
        onMediaServerDisconnect();
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

    private void releaseExtendCamera() { // ???????????????????????????
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
//        mVideoAdapter.setRemoveRemoteStreamReceiver(null);
        if (mIPRTCDataProvider != null) {
            mIPRTCDataProvider.releaseBuffer();
            mIPRTCDataProvider = null;
        }
        if (mIPRTCDataReceiver != null) {
            mIPRTCDataReceiver.releaseBuffer();
            mIPRTCDataReceiver = null;
        }
    }

    private void startTimeShow() {
        timeShow.setBase(SystemClock.elapsedRealtime());
        timeShow.start();
    }

    private void stopTimeShow() {
        timeShow.stop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            int tempScreen = 0;
            FrameLayout.LayoutParams params = null;
            // ??????????????????
            tempScreen = screenHeight;
            screenHeight = screenWidth;
            screenWidth = tempScreen;

            if (mLocalViewFullScreen) {
                if (mLocalVideoView.getScaleType() == PRTCScaleType.SCALE_ASPECT_FIT.ordinal()) {
                    mLocalVideoView.resetSurface();
                }
                else {
                    params = new FrameLayout.LayoutParams(screenWidth, screenHeight + mToolBar.getHeight());
                    params.setMargins(0, 0, 0, 0);
                    mLocalVideoView.setLayoutParams(params);
                }
            } else {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (mLocalVideoView.getScaleType() == PRTCScaleType.SCALE_ASPECT_FIT.ordinal()) {
                        mLocalVideoView.resetSurface();
                    }
                    else {
                        params = new FrameLayout.LayoutParams(localViewWidth_portrait, localViewHeight_portrait);
                        params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
                        mLocalVideoView.setLayoutParams(params);
                        Log.d(TAG, "PORTRAIT screen. localViewWidth: " + localViewWidth_portrait + " localViewHeight: " + localViewHeight_portrait);
                    }
                }
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (mLocalVideoView.getScaleType() == PRTCScaleType.SCALE_ASPECT_FIT.ordinal()) {
                        mLocalVideoView.resetSurface();
                    }
                    else {
                        params = new FrameLayout.LayoutParams(localViewWidth_landscape, localViewHeight_landscape);
                        params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
                        mLocalVideoView.setLayoutParams(params);
                        Log.d(TAG, "LANDSCAPE screen. localViewWidth: " + localViewWidth_landscape + " localViewHeight: " + localViewHeight_landscape);
                    }
                }
            }
            }
        }, 50);
/*        int tempScreen = 0;
        FrameLayout.LayoutParams params = null;
        // ??????????????????
        tempScreen = screenHeight;
        screenHeight = screenWidth;
        screenWidth = tempScreen;

        if (mLocalViewFullScreen) {
            params = new FrameLayout.LayoutParams(screenWidth, screenHeight + mToolBar.getHeight());
            params.setMargins(0, 0, 0, 0);
            mLocalVideoView.setLayoutParams(params);
        } else {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                params = new FrameLayout.LayoutParams(localViewWidth_portrait, localViewHeight_portrait);
                params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
                mLocalVideoView.setLayoutParams(params);
                Log.d(TAG, "PORTRAIT screen. localViewWidth: " + localViewWidth_portrait + " localViewHeight: " + localViewHeight_portrait);
            }
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                params = new FrameLayout.LayoutParams(localViewWidth_landscape, localViewHeight_landscape);
                params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
                mLocalVideoView.setLayoutParams(params);
                Log.d(TAG, "LANDSCAPE screen. localViewWidth: " + localViewWidth_landscape + " localViewHeight: " + localViewHeight_landscape);
            }
        }*/
    }

    public void toggleFullScreen() {
        FrameLayout.LayoutParams params = null;

        if (!mLocalViewFullScreen) {
            setSystemUIVisible(false);
            //????????????????????????????????????
            mTitleBar.setVisibility(View.GONE);
            mToolBar.setVisibility(View.GONE);
            StatusBarUtils.removeStatusView(this);

            if (mLocalVideoView.getScaleType() == PRTCScaleType.SCALE_ASPECT_FIT.ordinal()) {
                mLocalVideoView.resetSurface();
            }
            else {
                params = new FrameLayout.LayoutParams(screenWidth, screenHeight + mToolBar.getHeight());
                params.setMargins(0, 0, 0, 0);
                mLocalVideoView.setLayoutParams(params);
                Log.d(TAG, "Switch full screen in ASPECT_FILL width: " + params.width + " height: " + params.height);
            }
            //?????????????????????
            DrawerLayout.LayoutParams dl_params = (DrawerLayout.LayoutParams) mDrawerMenu.getLayoutParams();
            dl_params.topMargin = 0;
            dl_params.bottomMargin = 0;
            //???????????????????????????
            mImgSoundVolume.setVisibility(View.INVISIBLE);
            mImgMicSts.setVisibility(View.INVISIBLE);
        } else {
            setSystemUIVisible(true);
            //FrameLayout.LayoutParams params = null;
            //????????????
            // ???????????????????????????
            if (RTCLiveActivity.this.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                params = new FrameLayout.LayoutParams(localViewWidth_landscape, localViewHeight_landscape);
            }
            else {
                params = new FrameLayout.LayoutParams(localViewWidth_portrait, localViewHeight_portrait);
            }
            if (mLocalVideoView.getScaleType() == PRTCScaleType.SCALE_ASPECT_FIT.ordinal()) {
                mLocalVideoView.resetSurface();
            }
            else {
                params.setMargins(0, mTitleBar.getHeight(), 0, mToolBar.getHeight());
                mLocalVideoView.setLayoutParams(params);
            }
            //????????????????????????????????????
            mTitleBar.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
            StatusBarUtils.addStatusView(this);
            //??????????????????
            DrawerLayout.LayoutParams dl_params = (DrawerLayout.LayoutParams) mDrawerMenu.getLayoutParams();
            dl_params.topMargin = mTitleBar.getHeight();
            dl_params.bottomMargin = mToolBar.getHeight();
            //???????????????????????????
            if (!mMuteMic) {
                mImgSoundVolume.setVisibility(View.VISIBLE);
            }
            mImgMicSts.setVisibility(View.VISIBLE);

            Log.d(TAG, "Quit full screen. width: " + params.width + " height: " + params.height);
        }
        mLocalViewFullScreen = !mLocalViewFullScreen;
    }

    private IPRTCScreenShot mIPRTCScreenShot = new IPRTCScreenShot() { // ????????????
        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/rtcscreen_" + System.currentTimeMillis() + ".jpg";
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
            ToastUtils.shortShow(RTCLiveActivity.this, "screen shoot : " + name);
        }
    };

    private void addScreenShotCallBack(View view) {
        if (view instanceof PRTCSurfaceViewGroup) {
            ((PRTCSurfaceViewGroup) view).setScreenShotBack(mIPRTCScreenShot);
        } else if (view instanceof PRTCSurfaceViewRenderer) {
            ((PRTCSurfaceViewRenderer) view).setScreenShotBack(mIPRTCScreenShot);
        }
    }

    //?????????????????????
    private void initRecordManager() {
        RtcRecordManager.init("");
        Log.d(TAG, "initRecordManager: cache path:" + RtcRecordManager.getVideoCachePath());
    }

    private void toggleLocalRecord() { // ???????????????????????????????????????
        if (!mIsLocalRecording) {
            Log.d(TAG, " start local record: ");
            //RTCRecordManager.getInstance().startRecord(PRTCRecordType.RECORD_TYPE_MP4,"mnt/sdcard/rtc/mp4/"+ System.currentTimeMillis()+".mp4",mLocalRecordListener,1000);
            mIsLocalRecording = true;
            mImgLocalRecord.setImageResource(R.mipmap.stop);
            mTextLocalRecord.setText(R.string.local_recording);
        } else {
            Log.d(TAG, " stop local record: ");
            //RTCRecordManager.getInstance().stopRecord();
            mIsLocalRecording = false;
            mImgLocalRecord.setImageResource(R.mipmap.record);
            mTextLocalRecord.setText(R.string.start_local_record);
        }
    }

    private void toggleRemoteRecord() { // ????????????
        if (!mIsRemoteRecording) {
            Log.d(TAG, " start remote record: ");
            mAtomOpStart = true;
            // ??????????????????
            PRTCMixProfile mixProfile = PRTCMixProfile.getInstance().assembleRecordMixParamsBuilder()
                    .type(PRTCMixProfile.MIX_TYPE_RECORD)
                    //????????????
                    .layout(PRTCMixProfile.LAYOUT_AVERAGE_1)
                    //???????????????
                    .resolution(1280, 720)
                    //?????????
                    .bgColor(0, 0, 0)
                    //????????????
                    .frameRate(15)
                    //????????????
                    .bitRate(1000)
                    //h264????????????
                    .videoCodec(PRTCMixProfile.VIDEO_CODEC_H264)
                    //????????????
                    .qualityLevel(PRTCMixProfile.QUALITY_H264_CB)
                    //????????????
                    .audioCodec(PRTCMixProfile.AUDIO_CODEC_AAC)
                    //?????????ID
                    .mainViewUserId(mUserid)
                    //?????????????????????
                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                    //??????????????????
                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_AUTO)
                    //???????????????????????????????????????MIX_TYPE_UPDATE ????????????
                    .addStream(mUserid, MEDIA_TYPE_VIDEO.ordinal())
                    //??????????????????
                    .region(REGION)
                    //???????????????
                    .Bucket(BUCKET)
                    .build();
            sdkEngine.startRecord(mixProfile); // ????????????
        } else if (!mAtomOpStart) {
            Log.d(TAG, " stop remote record: ");
            mAtomOpStart = true;
            sdkEngine.stopRecord(); // ????????????
        }
    }

    private void toggleMix() { // ??????
        if (!mIsMixing) {
            Log.d(TAG, " start mix: ");
            mAtomOpStart = true;
            // ??????????????????
            PRTCMixProfile mixProfile = PRTCMixProfile.getInstance().assembleUpdateMixParamsBuilder()
                    .type(PRTCMixProfile.MIX_TYPE_RELAY)
                    //????????????
                    .layout(PRTCMixProfile.LAYOUT_CLASS_ROOM_2)
                    //???????????????
                    .resolution(1280, 720)
                    //?????????
                    .bgColor(0, 0, 0)
                    //????????????
                    .frameRate(15)
                    //????????????
                    .bitRate(1000)
                    //h264????????????
                    .videoCodec(PRTCMixProfile.VIDEO_CODEC_H264)
                    //????????????
                    .qualityLevel(PRTCMixProfile.QUALITY_H264_CB)
                    //????????????
                    .audioCodec(PRTCMixProfile.AUDIO_CODEC_AAC)
                    //?????????ID
                    .mainViewUserId(mUserid)
                    //?????????????????????
                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                    //??????????????????
                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_MANUAL)
                    //???????????????????????????????????????MIX_TYPE_UPDATE ????????????
                    .addStream(mUserid, MEDIA_TYPE_VIDEO.ordinal())
                    //????????????cdn ?????????
                    .addPushUrl("rtmp://rtcpush.ugslb.com/rtclive/" + mRoomid)
                    //????????????
                    .keyUser(mUserid)
                    //?????????
                    .layoutUserLimit(2)
                    //??????????????????????????????
                    .taskTimeOut(70)
                    //??????????????????
                    .region(REGION)
                    //???????????????
                    .Bucket(BUCKET)
                    .build();
            sdkEngine.updateMixConfig(mixProfile); // ????????????
        } else if (!mAtomOpStart) {
            Log.d(TAG, " stop mix: ");
            mAtomOpStart = true;
            sdkEngine.stopRelay(null); // ????????????
        }
    }

    private void update(int type) {
        Log.d(TAG, " start update: ");
        PRTCMixProfile mixProfile = PRTCMixProfile.getInstance().assembleMixParamsBuilder()
                .type(type)
                //????????????
                .layout(PRTCMixProfile.LAYOUT_CLASS_ROOM_2)
                //???????????????
                .resolution(1280, 720)
                //?????????
                .bgColor(0, 0, 0)
                //????????????
                .frameRate(15)
                //????????????
                .bitRate(1000)
                //h264????????????
                .videoCodec(PRTCMixProfile.VIDEO_CODEC_H264)
                //????????????
                .qualityLevel(PRTCMixProfile.QUALITY_H264_CB)
                //????????????
                .audioCodec(PRTCMixProfile.AUDIO_CODEC_AAC)
                //?????????ID
                .mainViewUserId(mUserid)
                //?????????????????????
                .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                //??????????????????
                .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_MANUAL)
                //???????????????????????????????????????MIX_TYPE_UPDATE ????????????
                .addStream(mUserid, MEDIA_TYPE_VIDEO.ordinal())
                .addStream(latestRemoteInfo.getUId(), latestRemoteInfo.getMediaType().ordinal())
                //????????????cdn ?????????
                .addPushUrl("rtmp://rtcpush.ugslb.com/rtclive/" + mRoomid)
                //????????????
                .keyUser(mUserid)
                //?????????
                .layoutUserLimit(2)
                //??????????????????????????????
                .taskTimeOut(70)
                //??????????????????
                .region(REGION)
                //???????????????
                .Bucket(BUCKET)
                .build();
        sdkEngine.updateMixConfig(mixProfile);
    }

    // ???????????????????????????
    private void toggleMixingSound(boolean isRemotePlay) {
        if (!isRemotePlay) {
            // ????????????
            if (!mIsLocalMixingSound) {
                if (!sdkEngine.startPlayAudioFile(
                        //"/sdcard/light.mp3",
                        sdkEngine.copyAssetsFileToSdcard("water.mp3"),
                        false, false)) {
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
                if (mVideoIsPublished) {
                    mImgRemoteMixSound.setVisibility(View.VISIBLE);
                    mTextRemoteMixSound.setVisibility(View.VISIBLE);
                }
                mImgControlMixSound.setImageResource(R.mipmap.pause);
                mTextControlMixSound.setText(R.string.pause_mixing_sound);
                mImgControlMixSound.setVisibility(View.GONE);
                mTextControlMixSound.setVisibility(View.GONE);
            }
            mIsLocalMixingSound = !mIsLocalMixingSound;
        }
        else if (mVideoIsPublished && isRemotePlay){
            // ??????+????????????
            if (!mIsRemoteMixingSound) {
                if (!sdkEngine.startPlayAudioFile(
                        //"/sdcard/light.mp3",
                        sdkEngine.copyAssetsFileToSdcard("water.mp3"),
                        true, false)) {
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

    private void toggleControlMixingSound() { // ????????????
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
     * ????????????????????????????????????????????????
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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    private UVCCamera initUVCCamera(USBMonitor.UsbControlBlock ctrlBlock) { // usb????????????????????????
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
/*                Log.d("RTCLiveActivity", "onFrame byteBuffer, frame.position: " + frame.position()
                                + " frame.limit: " + frame.limit());*/
                createFrameByteBuffer(frame);
            }
        }, mUVCCameraFormat);
        return camera;
    }

    //????????????????????????
    private IPRTCDataProvider mIPRTCDataProvider = new IPRTCDataProvider() {
        private ByteBuffer cacheBuffer;

        @Override
        public ByteBuffer provideRGBData(List<Integer> params) {
            if (videoSourceData == null ) {
                Log.d("RTCLiveActivity", "provideRGBData byteBuffer data is null");
                return null;
            } else {
                //Log.d("RTCLiveActivity", "provideRGBData: ! = null");
/*                Log.d("RTCLiveActivity", "provideRGBData byteBuffer, videoSourceData.position: " + videoSourceData.position()
                        + " videoSourceData.limit: " + videoSourceData.limit());*/
                params.add(mRTCVideoFormat);
                params.add(PRTCVideoProfile.matchValue(mVideoProfileSelect).getWidth());
                params.add(PRTCVideoProfile.matchValue(mVideoProfileSelect).getHeight());
                if (cacheBuffer == null) {
                    cacheBuffer = sdkEngine.getNativeOpInterface().
                            createNativeByteBuffer(1280 * 720 * 4);
                    Log.d("RTCLiveActivity", "byteBuffer createNativeByteBuffer call ");
                    cacheBuffer.clear();
                } else {
                    cacheBuffer.rewind();
                }
                synchronized (extendByteBufferSync) {
                    cacheBuffer.put(videoSourceData);
                    videoSourceData.rewind();
                }

                //cacheBuffer.position(0);
                cacheBuffer.flip();

                return cacheBuffer;
            }
        }

        @Override
        public void releaseBuffer() { // ????????????
            Log.d("RTCLiveActivity", "releaseBuffer");
            synchronized (extendByteBufferSync) {
                if (videoSourceData != null) {
                    videoSourceData.clear();
                    sdkEngine.getNativeOpInterface().releaseNativeByteBuffer(videoSourceData);
                    videoSourceData = null;
                }
            }
            if (cacheBuffer != null) {
                cacheBuffer.clear();
                sdkEngine.getNativeOpInterface().releaseNativeByteBuffer(cacheBuffer);
                cacheBuffer = null;
            }
        }
    };

    //????????????????????????
    private IPRTCDataReceiver mIPRTCDataReceiver = new IPRTCDataReceiver() {
        //private int limit = 0;
        private ByteBuffer cache;

        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            Log.d("RTCLiveActivity", "onReceiveRGBAData!");

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
            if (cache == null) {
                //????????????????????????????????????????????????buffersize????????????????????????????????????
                int size = 4096 * 2160 * 4;
                cache = sdkEngine.getNativeOpInterface().
                        createNativeByteBuffer(4096 * 2160 * 4);
            }
            cache.clear();
            return cache;
        }

        @Override
        public void releaseBuffer() {
            if (cache != null)
                sdkEngine.getNativeOpInterface().releaseNativeByteBuffer(cache);
            cache = null;
        }
    };

    //?????????????????????
    private IPRTCNotification mScreenCaptureNotification = new IPRTCNotification() {
        @Override
        public Notification createNotificationChannel() {
            Notification.Builder builder = new Notification.Builder(getApplicationContext()); //????????????Notification?????????
            Intent nfIntent = new Intent(getApplicationContext(), RTCLiveActivity.class); //???????????????????????????????????????????????????

            builder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, nfIntent, 0)) // ??????PendingIntent
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher)) // ??????????????????????????????(?????????)
                    //.setContentTitle("SMI InstantView") // ??????????????????????????????
                    .setSmallIcon(R.mipmap.ic_launcher) // ??????????????????????????????
                    .setContentText("screen capturing") // ?????????????????????
                    .setWhen(System.currentTimeMillis()); // ??????????????????????????????

            //????????????Android 8.0?????????
            //??????notification??????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId("notification_id");
            }
            //????????????notification??????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = builder.build(); // ??????????????????Notification
            notification.defaults = Notification.DEFAULT_SOUND; //????????????????????????

            return notification;
        }
    };

    private void createFrameByteBuffer(ByteBuffer frame) { // ????????????????????????
        try {
            if (frame != null) {
                synchronized (extendByteBufferSync) {
                    if (videoSourceData != null) {
                        videoSourceData.clear();
                        videoSourceData.put(frame);
                        videoSourceData.flip();
                    }
                }
            }
        } catch (Exception e) {
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

    private void updateVideoFormat(int videoFormat) { // ????????????????????????
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
                //UVCCamera???????????????argb??????????????????rgbx????????????????????????????????????
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_ARGB;
                mRTCVideoFormat = IPRTCDataProvider.ARGB_TO_I420;
                break;
            case CommonUtils.rgb24_format:
                //UVCCamera???RGB888???libyuv????????????????????????????????????UVCCamera????????????BGR888,??????????????????
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_BGR888;
                mRTCVideoFormat = IPRTCDataProvider.RGB24_TO_I420;
                break;
            case CommonUtils.rgb565_format:
                mUVCCameraFormat = UVCCamera.PIXEL_FORMAT_RGB565;
                mRTCVideoFormat = IPRTCDataProvider.RGB565_TO_I420;
                break;
        }
    }

    private void setPreview(boolean onOff) { //??????????????????
        if (onOff) {
            if (mExtendCameraCapture) {
                sdkEngine.startCameraPreview(
                        mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FIT, null);
            } else {
                sdkEngine.startCameraPreview(mLocalVideoView, PRTCScaleType.SCALE_ASPECT_FIT, null);
            }
        } else {
            sdkEngine.stopPreview(MEDIA_TYPE_VIDEO);
        }
    }

    private void setIconStats(boolean visible) { // ??????????????????
        if (!visible) {
            //???????????????????????????
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
            mImgRemoteMixSound.setVisibility(View.GONE);
            mTextRemoteMixSound.setVisibility(View.GONE);
            mImgControlMixSound.setVisibility(View.GONE);
            mTextControlMixSound.setVisibility(View.GONE);
        } else {
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
            mImgRemoteMixSound.setVisibility(View.VISIBLE);
            mTextRemoteMixSound.setVisibility(View.VISIBLE);
        }
    }

    private void refreshSettings() { // ????????????
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

        //?????????????????????
        String[] resolutions = getResources().getStringArray(R.array.videoResolutions);
        mResolutionOption.addAll(Arrays.asList(resolutions));

        Log.d(TAG, " Camera enable is: " + mCameraEnable + " Mic enable is: " + mMicEnable + " ScreenShare enable is: " + mScreenEnable);
        if (!mScreenEnable && !mCameraEnable && mMicEnable) {
            sdkEngine.setAudioOnlyMode(true);
        } else {
            sdkEngine.setAudioOnlyMode(false);
        }
        sdkEngine.configLocalCameraPublish(mCameraEnable);
        sdkEngine.configLocalAudioPublish(mMicEnable);
        if (isScreenCaptureSupport) {
            sdkEngine.configLocalScreenPublish(mScreenEnable);
        } else {
            sdkEngine.configLocalScreenPublish(false);
        }
    }
}

