package com.prtcdemo.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.polyv.prtc.sdkengine.IPRTCEngine;
import com.polyv.prtc.sdkengine.PRTCEnvHelper;
import com.polyv.prtc.sdkengine.define.PRTCCaptureMode;
import com.polyv.prtc.sdkengine.define.PRTCClientRole;
import com.polyv.prtc.sdkengine.define.PRTCErrorCode;
import com.polyv.prtc.sdkengine.define.PRTCMediaType;
import com.polyv.prtc.sdkengine.define.PRTCRecordType;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewRenderer;
import com.polyv.prtc.sdkengine.define.PRTCAudioDevice;
import com.polyv.prtc.sdkengine.define.PRTCAuthInfo;
import com.polyv.prtc.sdkengine.define.PRTCMediaServiceStatus;
import com.polyv.prtc.sdkengine.define.PRTCNetWorkQuality;
import com.polyv.prtc.sdkengine.define.PRTCChannelProfile;
import com.polyv.prtc.sdkengine.define.PRTCScaleType;
import com.polyv.prtc.sdkengine.define.PRTCStreamStatus;
import com.polyv.prtc.sdkengine.define.PRTCStreamInfo;
import com.polyv.prtc.sdkengine.define.PRTCStreamType;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewGroup;
import com.polyv.prtc.sdkengine.define.PRTCTrackType;
import com.polyv.prtc.sdkengine.define.PRTCVideoProfile;
import com.polyv.prtc.sdkengine.listener.IPRTCEngineEventHandler;
import com.polyv.prtc.sdkengine.listener.IPRTCRecordListener;
import com.polyv.prtc.sdkengine.openinterface.IPRTCDataProvider;
import com.polyv.prtc.sdkengine.openinterface.IPRTCDataReceiver;
import com.polyv.prtc.sdkengine.openinterface.IPRTCFirstFrameRendered;
import com.polyv.prtc.sdkengine.openinterface.IPRTCScreenShot;
import com.prtcdemo.R;
import com.prtcdemo.adpter.RemoteVideoAdapter;
import com.prtcdemo.utils.CommonUtils;
import com.prtcdemo.utils.ToastUtils;
import com.prtcdemo.utils.UiHelper;
import com.prtcdemo.utils.VideoListener;
import com.prtcdemo.utils.VideoPlayer;
import com.prtcdemo.view.CustomerClickListener;
import com.prtcdemo.view.SteamScribePopupWindow;
import com.prtcdemo.view.RTCVideoViewInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wrtca.record.MediaRecorderBase;
import org.wrtca.record.RtcRecordManager;
import org.wrtca.record.model.MediaObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import core.renderer.SurfaceViewGroup;
import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.polyv.prtc.sdkengine.define.PRTCErrorCode.NET_ERR_CODE_OK;
import static com.polyv.prtc.sdkengine.define.PRTCMediaType.MEDIA_TYPE_SCREEN;
import static com.polyv.prtc.sdkengine.define.PRTCMediaType.MEDIA_TYPE_VIDEO;
import static com.prtcdemo.activity.RoomActivity.BtnOp.OP_LOCAL_RECORD;

public class RoomActivity extends AppCompatActivity implements VideoListener {
    private static final String TAG = "RoomActivity";

    private String mUserid = "test001";
    private String mRoomid = "urtc1";
    private String mRoomToken = "test token";
    private String mAppid = "";
    private String mBucket = "urtc-test";
    private String mRegion = "cn-bj";
    private boolean mIsRecording = false;
    private boolean mIsMixing = false;
    private boolean mAtomOpStart = false;
    private boolean mIsPublished = false;
    private boolean mMixAddOrDel = true;

    TextView title = null;
//    PRTCSurfaceViewGroup localrenderview = null;
    PRTCSurfaceViewRenderer localrenderview = null;
    ProgressBar localprocess = null;

    final int COL_SIZE_P = 3;
    final int COL_SIZE_L = 6;
    private GridLayoutManager gridLayoutManager;
    private RemoteVideoAdapter mVideoAdapter;
    RecyclerView mRemoteGridView = null;
    IPRTCEngine sdkEngine = null;
    ImageButton mPublish = null;
    ImageButton mHangup = null;
    ImageButton mSwitchcam = null;
    ImageButton mMuteMic = null;
    ImageButton mLoudSpkeader = null;
    ImageButton mMuteCam = null;
    TextView mOpBtn = null;
    TextView mAddDelBtn = null;
    CheckBox  mCheckBoxMirror = null;
    private SteamScribePopupWindow mSpinnerPopupWindowScribe;
    private View mStreamSelect;
    private TextView mTextStream;
    //int mCaptureMode;
    int mVideoProfile;
    @CommonUtils.PubScribeMode
    int mPublishMode;
    @CommonUtils.PubScribeMode
    int mScribeMode;
    PRTCClientRole mRole;
    PRTCChannelProfile mClass;
    boolean isScreenCaptureSupport;
    boolean mCameraEnable;
    boolean mMicEnable;
    boolean mScreenEnable;
    private List<PRTCStreamInfo> mSteamList;
    private PRTCStreamInfo mLocalStreamInfo;
    private boolean mRemoteVideoMute;
    private boolean mRemoteAudioMute;
    private PRTCSurfaceViewGroup mMuteView = null;
    Chronometer timeshow;
    private int mPictureFlag = 0;
    private boolean mPFlag = false;
    private ArrayBlockingQueue<RGBSourceData> mQueue = new ArrayBlockingQueue(2);
    // 定义一个nv21 的
     private ArrayBlockingQueue<NVSourceData> mQueueNV = new ArrayBlockingQueue(2);
    private Thread mCreateImgThread;
    private Timer mTimerCreateImg = new Timer("createPicture");
    private boolean startCreateImg = true;
    private AtomicInteger memoryCount = new AtomicInteger(0);
    private List<String> userIds = new ArrayList<>();
    private boolean mLocalRecordStart = false;
    private PRTCMediaType mPublishMediaType;
    private VideoPlayer mVideoPlayer ;
    private PRTCSurfaceViewRenderer mRemoteRenderView;
    private boolean bigVolume = true;
    private FrameLayout testT ,testB;
    private AppCompatSeekBar mSeekBar;

    /**
     * SDK视频录制对象
     */
    private MediaRecorderBase mMediaRecorder;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    enum BtnOp{
        OP_LOCAL_RECORD,
        OP_REMOTE_RECORD,
        OP_SEND_MSG,
        OP_LOCAL_RESAMPLE,
        OP_MIX,
        OP_MIX_MANUAL
    }
    class RGBSourceData{
        Bitmap srcData;
        int width;
        int height;
        int type;

        public RGBSourceData(Bitmap srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public Bitmap getSrcData() {
            return srcData;
        }

        public void setSrcData(Bitmap srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    class NVSourceData{
        ByteBuffer srcData;
        int width;
        int height;
        int type;

        public NVSourceData(ByteBuffer srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public ByteBuffer getSrcData() {
            return srcData;
        }

        public void setSrcData(ByteBuffer srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    private IPRTCDataProvider mIPRTCDataProvider = new IPRTCDataProvider() {
        private ByteBuffer cacheBuffer;
        private RGBSourceData rgbSourceData;
        private NVSourceData nvSourceData;

        @Override
        public ByteBuffer provideRGBData(List<Integer> params) {
            return null;
        }

        public void releaseBuffer(){
            if(rgbSourceData != null && !rgbSourceData.getSrcData().isRecycled()){
                rgbSourceData.getSrcData().recycle();
                rgbSourceData.srcData = null;
                rgbSourceData = null;
            }
            if(cacheBuffer != null){
                sdkEngine.getNativeOpInterface().realeaseNativeByteBuffer(cacheBuffer);
            }
        }
    };

    private IPRTCDataReceiver mIPRTCDataReceiver = new IPRTCDataReceiver() {
        private int limit = 0;
        private ByteBuffer cache;

        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
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
            }
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

    private IPRTCFirstFrameRendered mFirstFrameRendered = new IPRTCFirstFrameRendered() {
        @Override
        public void onFirstFrameRender(PRTCStreamInfo info, View view) {

        }
    };

//    private View.OnClickListener mSwapRemoteLocalListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (v instanceof PRTCSurfaceViewGroup) {
//                String key = ((RTCVideoViewInfo) v.getTag(R.id.index)).getKey();
//                if (mVideoAdapter.checkCanSwap(key)) {
//                    boolean state = mVideoAdapter.checkState(key);
//                    if (!state) {
//                        PRTCStreamInfo remoteStreamInfo = (PRTCStreamInfo) v.getTag();
//                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                        if (mLocalStreamInfo != null) {
//                            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), (PRTCSurfaceViewGroup) v,null, null);
//                            v.setTag(R.id.swap_info, mLocalStreamInfo);
//                        }
//                        sdkEngine.startRemoteView(remoteStreamInfo, (PRTCSurfaceViewGroup) localrenderview,null,null);
//                        ((PRTCSurfaceViewGroup) v).refreshRemoteOp(View.INVISIBLE);
//                        ((PRTCSurfaceViewGroup) localrenderview).refreshRemoteOp(View.VISIBLE);
//                        localrenderview.setTag(R.id.swap_info, remoteStreamInfo);
//                        if (mClass == PRTCChannelProfile.ROOM_LARGE) {
//                            localrenderview.setVisibility(View.VISIBLE);
//                            localrenderview.setTag(R.id.view_info, v);
//                            localrenderview.setBackgroundColor(Color.TRANSPARENT);
//                            v.setVisibility(View.INVISIBLE);
                            //和本地view截图功能触发重叠，App使用者可以另行定义触发
//                            localrenderview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    if (v instanceof PRTCSurfaceViewGroup) {
//                                        localrenderview.setVisibility(View.INVISIBLE);
//                                        PRTCStreamInfo remoteStreamInfo = (PRTCStreamInfo) v.getTag(R.id.swap_info);
//                                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                                        PRTCSurfaceViewGroup view = (PRTCSurfaceViewGroup) v.getTag(R.id.view_info);
//                                        view.setVisibility(View.VISIBLE);
//                                        view.refreshRemoteOp(View.VISIBLE);
//                                        sdkEngine.startRemoteView(remoteStreamInfo, view);
//                                        mVideoAdapter.reverseState(key);
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//                        //有交换过
//                        PRTCStreamInfo remoteStreamInfo = (PRTCStreamInfo) v.getTag();
//                        //停止交换过的大窗渲染远端
//                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                        //停止本地视频渲染
//                        sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                        sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
//                        sdkEngine.startRemoteView(remoteStreamInfo, (PRTCSurfaceViewGroup) v,null,null);
//                        ((PRTCSurfaceViewGroup) v).refreshRemoteOp(View.VISIBLE);
//                        ((PRTCSurfaceViewGroup) localrenderview).refreshRemoteOp(View.INVISIBLE);
//                        v.setTag(R.id.swap_info, null);
//                        localrenderview.setTag(R.id.swap_info, null);
//                    }
//                    mVideoAdapter.reverseState(key);
//                } else {
//                    ToastUtils.shortShow(RoomActivity.this, "其它窗口已经交换过，请先交换回来");
//                }
//            }
//        }
//    };

    private View.OnClickListener mScreenShotOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             addScreenShotCallBack(v);
        }
    };

    private View.OnClickListener mLocalChangeRenderMode =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            localrenderview.setScalingType(PRTCScaleType.SCALE_ASPECT_FIT);
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

//        @Override
//        public void onRemoteVideo(View v, PRTCSurfaceViewGroup parent) {
//            if (parent.getTag(R.id.swap_info) != null) {
//                PRTCStreamInfo swapStreamInfo = (PRTCStreamInfo) parent.getTag(R.id.swap_info);
//                sdkEngine.muteRemoteVideo(swapStreamInfo.getUId(), !mRemoteVideoMute);
//            } else if (parent.getTag() != null) {
//                PRTCStreamInfo streamInfo = (PRTCStreamInfo) parent.getTag();
//                sdkEngine.muteRemoteVideo(streamInfo.getUId(), !mRemoteVideoMute);
//            }
//            mMuteView = parent;
//        }
//
//        @Override
//        public void onRemoteAudio(View v, PRTCSurfaceViewGroup parent) {
//            if (parent.getTag(R.id.swap_info) != null) {
//                PRTCStreamInfo swapStreamInfo = (PRTCStreamInfo) parent.getTag(R.id.swap_info);
//                sdkEngine.muteRemoteAudio(swapStreamInfo.getUId(), !mRemoteAudioMute);
//            } else if (parent.getTag() != null) {
//                PRTCStreamInfo streamInfo = (PRTCStreamInfo) parent.getTag();
//                sdkEngine.muteRemoteAudio(streamInfo.getUId(), !mRemoteAudioMute);
//            }
//            mMuteView = parent;
//        }
    };

    private RemoteVideoAdapter.RemoveRemoteStreamReceiver mRemoveRemoteStreamReceiver = new RemoteVideoAdapter.RemoveRemoteStreamReceiver() {
        @Override
        public void onRemoteStreamRemoved(boolean swaped) {
            if (swaped) {
//                if (mClass == PRTCChannelProfile.ROOM_SMALL) {
//                    sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                    sdkEngine.startPreview(mLocalStreamInfo, localrenderview,null,null);
//                } else if (localrenderview.getTag(R.id.swap_info) != null) {
//                    PRTCStreamInfo remoteStreamInfo = (PRTCStreamInfo) localrenderview.getTag(R.id.swap_info);
//                    sdkEngine.stopRemoteView(remoteStreamInfo);
//                }
            }
        }
    };

    private void refreshStreamInfoText() {
        if (mSteamList == null || mSteamList.isEmpty()) {
            mTextStream.setText("当前没有流可以订阅");
        } else {
            mTextStream.setText(String.format("当前有%d路流可以订阅", mSteamList.size()));
        }
    }

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

    IPRTCEngineEventHandler eventListener = new IPRTCEngineEventHandler() {
        @Override
        public void onServerDisconnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerDisconnect: ");
                    ToastUtils.shortShow(RoomActivity.this, " 服务器已断开");
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
                        ToastUtils.shortShow(RoomActivity.this, " 加入房间成功");
//                        mOpBtn.setVisibility(View.VISIBLE);
                        startTimeShow();
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, " 加入房间失败 " +
                                code + " errmsg " + msg);
                        Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
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
                    ToastUtils.shortShow(RoomActivity.this, " 离开房间 " +
                            code + " errmsg " + msg);
//                    Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
                    onMediaServerDisconnect();
                    System.gc();
//                    startActivity(intent);
//                    finish();
                }
            });
        }

        @Override
        public void onRejoiningRoom(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "rejoining room");
                    ToastUtils.shortShow(RoomActivity.this, " 服务器重连中…… ");
                    stopTimeShow();
                }
            });
        }

        @Override
        public void onRejoinRoomResult(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, "服务器重连成功");
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
//                        ToastUtils.shortShow(RoomActivity.this, "发布视频成功");
                        mPublish.setImageResource(R.drawable.unpublish);
                        mIsPublished = true;
                        int mediatype = info.getMediaType().ordinal();
                        mPublishMediaType = PRTCMediaType.matchValue(mediatype);
                        if (mediatype == MEDIA_TYPE_VIDEO.ordinal()) {
                            if (!sdkEngine.isAudioOnlyMode()) {
                                localrenderview.setVisibility(View.VISIBLE);
                                localrenderview.setBackgroundColor(Color.TRANSPARENT);
//                                localrenderview.setScalingType(PRTCScaleType.SCALE_ASPECT_FIT);
                                sdkEngine.renderLocalView(info,
                                        localrenderview, PRTCScaleType.SCALE_ASPECT_FILL,null);

//                                PRTCSurfaceViewRenderer renderView = new PRTCSurfaceViewRenderer(RoomActivity.this);
//                                FrameLayout frameLayout = findViewById(R.id.local_parent);
//                                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(150,150);
//                                frameLayout.addView(renderView,0,layoutParams);
//                                renderView.init();
//                                sdkEngine.startPreview(info.getMediaType(),
//                                        renderView,PRTCScaleType.SCALE_ASPECT_FILL,null);
                                mLocalStreamInfo = info;
                                localrenderview.setTag(mLocalStreamInfo);
//                                localrenderview.refreshRemoteOp(View.INVISIBLE);
                                localrenderview.setOnClickListener(mScreenShotOnClickListener);
//                                localrenderview.setOnClickListener(mLocalChangeRenderMode);
                            }

                        } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN.ordinal()) {
                            //if (mCaptureMode == CommonUtils.screen_capture_mode) {
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
//                                localrenderview.setVisibility(View.VISIBLE);
                                sdkEngine.renderLocalView(info, localrenderview, PRTCScaleType.SCALE_ASPECT_FILL,null);
                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this,
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
                        mIsPublished = false;
                        if (info.getMediaType() == MEDIA_TYPE_VIDEO) {
                            if (localrenderview != null) {
//                                localrenderview.refresh();
                            }
                        } else if (info.getMediaType() == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                            //if (mCaptureMode == CommonUtils.screen_capture_mode) {
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
//                                if (localrenderview != null) {
//                                    localrenderview.refresh();
//                                }
                            }
                        }
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频成功");
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频失败 "
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
                    ToastUtils.shortShow(RoomActivity.this, " 用户 "
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
                    onUserLeave(uid);
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
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
                            mSpinnerPopupWindowScribe.notifyUpdate();
                            refreshStreamInfoText();
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
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                            info.getUId() + " 取消媒体流 " + info.getMediaType());
                    String mkey = info.getUId() + info.getMediaType().toString();
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(mkey);
                    }

                    mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                    refreshStreamInfoText();
                }
            });
        }

        @Override
        public void onSubscribeResult(int code, String msg, PRTCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        RTCVideoViewInfo vinfo = new RTCVideoViewInfo();
//                        PRTCSurfaceViewRenderer videoView = null;
                        PRTCSurfaceViewGroup videoView = null;
                        Log.d(TAG, " subscribe info: " + info);
                        if (info.isHasVideo()) {
                             //外部扩展输出，和默认输出二选一
//                            PRTCSurfaceViewGroup videoViewCallBack = new PRTCSurfaceViewGroup(getApplicationContext());
//                            videoViewCallBack.setFrameCallBack(mPRTCDataReceiver);
//                            videoViewCallBack.init(false);
//                            sdkEngine.startRemoteView(info, videoViewCallBack);

//                             PRTCSurfaceViewGroup 定义的viewgroup,内含polyvRtcRenderView
                            videoView = new PRTCSurfaceViewGroup(getApplicationContext());
                            videoView.init(false, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close,
                                    R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger,
                                    new int[]{R.id.remote_video, R.id.remote_audio}, null);
                            videoView.setTag(info);
                            videoView.setId(R.id.video_view);
                            //设置交换
//                            videoView.setOnClickListener(mSwapRemoteLocalListener);
//                            //远端截图
                            videoView.setOnClickListener(mScreenShotOnClickListener);

                            //自定义的surfaceview
//                            videoView = new PRTCSurfaceViewRenderer(getApplicationContext());
//                            videoView.init();
//                            videoView.setTag(info);
//                            videoView.setOnClickListener(mScreenShotOnClickListener);
//                            mRemoteRenderView = new PRTCSurfaceViewRenderer(getApplicationContext());
//                            mRemoteRenderView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
//                            testT.addView(mRemoteRenderView);
//                            mRemoteRenderView.init();
//                            sdkEngine.startRemoteView(info, mRemoteRenderView,PRTCScaleType.SCALE_ASPECT_FIT,null);
//                            mRemoteRenderView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    testT.removeAllViews();
//                                    mRemoteRenderView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
//                                    testB.addView(mRemoteRenderView);
//                                    mRemoteRenderView.resetSurface();
//                                }
//                            });
//                            mRemoteRenderView.setTag(info);
//                            mRemoteRenderView.setOnClickListener(mScreenShotOnClickListener);
                        }
                        vinfo.setmRenderview(videoView);
                        vinfo.setmUid(info.getUId());
                        vinfo.setmMediatype(info.getMediaType());
                        vinfo.setmEanbleVideo(info.isHasVideo());
                        vinfo.setEnableAudio(info.isHasAudio());
                        String mkey = info.getUId() + info.getMediaType().toString();
                        vinfo.setKey(mkey);
                        //默认输出，和外部输出代码二选一
                        if (mVideoAdapter != null) {
                            mVideoAdapter.addStreamView(mkey, vinfo, info);
                        }

                        if (vinfo != null && videoView != null) {
                            sdkEngine.startRemoteView(info, videoView, PRTCScaleType.SCALE_ASPECT_FILL,null);
//                            videoView.refreshRemoteOp(View.VISIBLE);
                        }
                        //如果订阅成功就删除待订阅列表中的数据
                        mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                        refreshStreamInfoText();
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, " 订阅用户  " +
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
                    ToastUtils.shortShow(RoomActivity.this, " 取消订阅用户 " +
                            info.getUId() + " 类型 " + info.getMediaType());
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(info.getUId() + info.getMediaType().toString());
                    }
                    //取消订阅又变成可订阅
                    mSpinnerPopupWindowScribe.addStreamInfo(info, true);
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
                                onMuteCamResult(mute);
                            }
                        } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                            onMuteCamResult(mute);
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
                        String mkey = uid + mediatype.toString();
                        Log.d(TAG, " onRemoteStreamMuteRsp " + mkey + " " + mVideoAdapter);
                        if (tracktype == PRTCTrackType.TRACK_TYPE_AUDIO) {
                            mRemoteAudioMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteAudio(mute);
                            }
//                            if (mMuteView == localrenderview) {
//                                int position = mVideoAdapter.getPositionByKey(mkey);
//                                View view = mRemoteGridView.getChildAt(position);
//                                PRTCSurfaceViewGroup videoView = view.findViewById(R.id.video_view);
//                                videoView.refreshRemoteAudio(mute);
//                            } else {
//                                localrenderview.refreshRemoteAudio(mute);
//                            }
                        } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                            mRemoteVideoMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteVideo(mute);
                            }
//                            if (mMuteView == localrenderview) {
//                                int position = mVideoAdapter.getPositionByKey(mkey);
//                                View view = mRemoteGridView.getChildAt(position);
//                                PRTCSurfaceViewGroup videoView = view.findViewById(R.id.video_view);
//                                videoView.refreshRemoteVideo(mute);
//                            } else {
//                                localrenderview.refreshRemoteVideo(mute);
//                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "mute " + mediatype + "failed with code: " + code);
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
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 麦克风");
                        } else if (tracktype == PRTCTrackType.TRACK_TYPE_VIDEO) {
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 摄像头");
                        }

                    } else if (mediatype == PRTCMediaType.MEDIA_TYPE_SCREEN) {
                        String cmd = mute ? "关闭" : "打开";
                        ToastUtils.shortShow(RoomActivity.this, " 用户 " +
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
                    localprocess.setProgress(volume);
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
                    ToastUtils.longShow(RoomActivity.this, " 被踢出会议 code " +
                            code);
                    Log.d(TAG, " user kickoff reason " + code);
                    Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
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
                        ToastUtils.shortShow(RoomActivity.this, "sdp swap failed");
                    }
                }
            });
        }

        @Override
        public void onRecordStop(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RoomActivity.this, "录制结束: " + (code == NET_ERR_CODE_OK.ordinal()?"成功":"失败: "+ code));
                    if(mIsRecording){
                        mIsRecording = false;
                        mOpBtn.setText("start record");
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
            if(status == PRTCMediaServiceStatus.RECORD_STATUS_START){
                String videoPath = "http://"+ mBucket + "."+ mRegion +".ufileos.com/" + fileName;
                Log.d(TAG,"remote record path: " +  videoPath+".mp4");
                ToastUtils.longShow(RoomActivity.this, "观看地址: " +videoPath );
                mIsRecording = true;
                mOpBtn.setText("stop record");
                if(mAtomOpStart)
                    mAtomOpStart = false;
            }else if(status == PRTCMediaServiceStatus.RECORD_STATUS_STOP_REQUEST_SEND){
                ToastUtils.longShow(RoomActivity.this, "录制结束: " + (code == NET_ERR_CODE_OK.ordinal()?"成功":"失败: "+ code));
                if(mIsRecording){
                    mIsRecording = false;
                    mOpBtn.setText("start record");
                }
            }else {
                ToastUtils.longShow(RoomActivity.this, "录制异常: 原因：" +code );
            }
        }

        @Override
        public void onRelayStatusNotify(PRTCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String[] pushUrls) {
            if(status == PRTCMediaServiceStatus.RELAY_STATUS_START){
                mIsMixing = true;
                mOpBtn.setText("stop mix");
                if(mAtomOpStart)
                    mAtomOpStart = false;
            }else if(status == PRTCMediaServiceStatus.RELAY_STATUS_STOP_REQUEST_SEND){
                Log.d(TAG,"onMixStop: " + code + "msg: "+ msg + " pushUrl: "+ pushUrls);
                if(mIsMixing){
                    mIsMixing = false;
                    mOpBtn.setText("mix");
                }
            }else{
                ToastUtils.longShow(RoomActivity.this, "转推异常: 原因：" +code );
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

        }

        @Override
        public void onRecordStart(int code, String fileName) {

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
                mLoudSpkeader.setImageResource(R.mipmap.loudspeaker);
                mSpeakerOn = true;
            } else {
                mSpeakerOn = false;
                mLoudSpkeader.setImageResource(R.mipmap.loudspeaker_disable);
            }
        }

        @Override
        public void onPeerLostConnection(int type, PRTCStreamInfo info) {
            Log.d(TAG, "onPeerLostConnection: type: " + type + "info: " + info);
        }

        @Override
        public void onNetWorkQuality(String userId, PRTCStreamType streamType, PRTCMediaType mediaType, PRTCNetWorkQuality quality) {
            Log.d(TAG, "onNetWorkQuality: userid: " + userId + "streamType: " + streamType + "mediatype : "+ mediaType + " quality: " + quality);
        }

        @Override
        public void onAudioFileFinish() {
            Log.d(TAG, "onAudioFileFinish" );
        }
    };
    private int mSelectPos;

    private void onUserLeave(String uid) {
//        if (mVideoAdapter != null) {
//            mVideoAdapter.removeStreamView(uid + _MEDIA_TYPE_VIDEO);
//            mVideoAdapter.removeStreamView(uid + PRTCMediaType.MEDIA_TYPE_SCREEN);
//        }
    }

    private void onMediaServerDisconnect() {
        localrenderview.release();
        clearGridItem();
//        IPRTCEngine.destory();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);
        testT = findViewById(R.id.test_t);
        testB = findViewById(R.id.test_bottom);
        mSeekBar = findViewById(R.id.seek_volume);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sdkEngine.adjustRecordVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//        mVideoPlayer = findViewById(R.id.playView);
//        mVideoPlayer.setVideoListener(this);
//        mVideoPlayer.setPath("http://video.zhihuishu.com/zhs_yufa_150820/aidedteaching/COURSE_FOLDER/202002/47dd76d15b5348839fcfa78b104e886e_64.mp3");
//        try {
//            mVideoPlayer.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        timeshow = findViewById(R.id.timer);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);
        //mCaptureMode = preferences.getInt(CommonUtils.capture_mode, CommonUtils.camera_capture_mode);
        mVideoProfile = preferences.getInt(CommonUtils.videoprofile, CommonUtils.videoprofilesel);
        mRemoteGridView = findViewById(R.id.remoteGridView);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_L);
        } else {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_P);
        }
        mRemoteGridView.setLayoutManager(gridLayoutManager);
        mVideoAdapter = new RemoteVideoAdapter(this);
        mVideoAdapter.setRemoveRemoteStreamReceiver(mRemoveRemoteStreamReceiver);
        mRemoteGridView.setAdapter(mVideoAdapter);
        sdkEngine = IPRTCEngine.create(eventListener);
//        sdkEngine = PRTCApplication.getInstance().createRtcEngine(eventListener);
        mUserid = getIntent().getStringExtra("user_id");
        mRoomid = getIntent().getStringExtra("room_id");
        mRoomToken = getIntent().getStringExtra("token");
        mAppid = getIntent().getStringExtra("app_id");
        mHangup = findViewById(R.id.button_call_disconnect);
        mSwitchcam = findViewById(R.id.button_call_switch_camera);
        mMuteMic = findViewById(R.id.button_call_toggle_mic);
        mLoudSpkeader = findViewById(R.id.button_call_loundspeaker);
        mMuteCam = findViewById(R.id.button_call_toggle_cam);
        mStreamSelect = findViewById(R.id.stream_select);
        mTextStream = findViewById(R.id.stream_text_view);
        refreshStreamInfoText();
        mOpBtn = findViewById(R.id.opBtn);
        //user can chose the suitable type
//        mOpBtn.setTag(OP_SEND_MSG);
//        mOpBtn.setText("sendmsg");
        mOpBtn.setTag(OP_LOCAL_RECORD);
        mOpBtn.setText("lrecord");
//        mOpBtn.setTag(OP_REMOTE_RECORD);
//        mOpBtn.setText("record");
//        mOpBtn.setTag(OP_MIX);
//        mOpBtn.setText("mix");
        //mOpBtn.setTag(OP_MIX_MANUAL);
        //mOpBtn.setText("mix_manual");
        mAddDelBtn = findViewById(R.id.addDelBtn);
        mAddDelBtn.setText("add_st");
        mAddDelBtn.setVisibility(View.VISIBLE);
        mCheckBoxMirror = findViewById(R.id.cb_mirror);
        mCheckBoxMirror.setChecked(PRTCEnvHelper.isFrontCameraMirror());
        mCheckBoxMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PRTCEnvHelper.setFrontCameraMirror(isChecked);
            }
        });
        mOpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnOp btnOp = (BtnOp)mOpBtn.getTag();
                switch (btnOp){
                    case OP_SEND_MSG:
                        sdkEngine.messageNotify("hi");
                        break;
                    case OP_LOCAL_RECORD:
                        if(!mLocalRecordStart){
                            Log.d(TAG, " start local record: ");
//                        RtcRecordManager.getInstance().startRecord(PRTCRecordType.RECORD_TYPE_MP4,System.currentTimeMillis()+"",mLocalRecordListener,1000);
                            RtcRecordManager.getInstance().startRecord(PRTCRecordType.RECORD_TYPE_MP4.ordinal(),"mnt/sdcard/urtc/mp4/"+ System.currentTimeMillis()+".mp4",mLocalRecordListener,1000);
                            mLocalRecordStart = true;
                        }else{
                            Log.d(TAG, " stop local record: ");
                            RtcRecordManager.getInstance().stopRecord();
                            mLocalRecordStart = false;
                        }
                        break;
                    case OP_REMOTE_RECORD:
                        if (!mIsRecording) {
                            mAtomOpStart = true;
//                如果主窗口是当前用户
//                PRTCRecordProfile recordProfile = PRTCRecordProfile.getInstance().assembleRecordBuilder()
//                        .recordType(PRTCRecordProfile.RECORD_TYPE_VIDEO)
//                        .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
//                        .VideoProfile(PRTCVideoProfile.VIDEO_PROFILE_640_480.ordinal())
//                        .Average(PRTCRecordProfile.RECORD_UNEVEN)
//                        .WaterType(PRTCRecordProfile.RECORD_WATER_TYPE_IMG)
//                        .WaterPosition(PRTCRecordProfile.RECORD_WATER_POS_LEFTTOP)
//                        .WarterUrl("http://urtc-living-test.cn-bj.ufileos.com/test.png")
//                        .Template(PRTCRecordProfile.RECORD_TEMPLET_9)
//                        .build();
//                sdkEngine.startRecord(recordProfile);
//                            PRTCMixProfile recordAudioProfile = PRTCMixProfile.getInstance().assembleRecordMixParamsBuilder()
//                                    .type(PRTCMixProfile.RECORD_TYPE_AUDIO)
//                                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
//                                    .build();
//                            sdkEngine.startRecord(recordAudioProfile);

                            //如果主窗口不是当前推流用户，而是被订阅的用户
//                PRTCStreamInfo PolyvRtcSdkStreamInfo = mVideoAdapter.getStreamInfo(0);
//                if(PolyvRtcSdkStreamInfo != null){
//                    PRTCRecordProfile recordProfile = PRTCRecordProfile.getInstance().assembleRecordBuilder()
//                            .recordType(PRTCRecordProfile.RECORD_TYPE_VIDEO)
//                            .mainViewUserId(PolyvRtcSdkStreamInfo.getUId())
//                            .mainViewMediaType(PolyvRtcSdkStreamInfo.getMediaType().ordinal())
//                            .VideoProfile(PRTCVideoProfile.VIDEO_PROFILE_640_480.ordinal())
//                            .Average(PRTCRecordProfile.RECORD_UNEVEN)
//                            .WaterType(PRTCRecordProfile.RECORD_WATER_TYPE_IMG)
//                            .WaterPosition(PRTCRecordProfile.RECORD_WATER_POS_LEFTTOP)
//                            .WarterUrl("http://urtc-living-test.cn-bj.ufileos.com/test.png")
//                            .Template(PRTCRecordProfile.RECORD_TEMPLET_9)
//                            .build();
//                    sdkEngine.startRecord(recordProfile);
//                }
                        } else if(!mAtomOpStart){
                            mAtomOpStart = true;
                            sdkEngine.stopRecord();
                        }
                        break;
                    case OP_MIX:
                        if (!mIsMixing) {
                            mAtomOpStart = true;
                            //默认mix类型是3 MIX_TYPE_BOTH
                            JSONArray pushURL = new JSONArray();
//                        pushURL.put("rtmp://push.urtc.com.cn/" + mAppid + "/"+ mUserid);
//                        pushURL.put("rtmp://push.urtc.com.cn/live/URtc-h4r1txxy123131");
                            pushURL.put("rtmp://rtcpush.ugslb.com/rtclive/"+mRoomid);
//                            PRTCMixProfile mixProfile = PRTCMixProfile.getInstance().assembleRelayMixParamsBuilder()
//                                    .pushUrl(pushURL)
//                                    .mainViewUserId(mUserid)
//                                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
//                                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_AUTO)
////                                    .mimeType(PRTCMixProfile.MIME_TYPE_AUDIO)
//                                    .build();
//                            sdkEngine.startRelay(mixProfile);
                        } else if (!mAtomOpStart) {
                            mAtomOpStart = true;
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put("");
                            //sdkEngine.stopMix(PRTCMixProfile.MIX_TYPE_BOTH,"rtmp://rtcpush.ugslb.com/rtclive/"+mRoomid);
                        }
                        break;
                    case OP_MIX_MANUAL:
                        if (!mIsMixing) {
                            mAtomOpStart = true;
                            //如果主窗口是当前用户
                            JSONArray pushURL = new JSONArray();
//                        pushURL.put("rtmp://push.urtc.com.cn/" + mAppid + "/"+ mUserid);
//                        pushURL.put("rtmp://push.urtc.com.cn/live/URtc-h4r1txxy123131");
                            pushURL.put("rtmp://rtcpush.ugslb.com/rtclive/"+mRoomid);
                            JSONArray streams = new JSONArray();
                            JSONObject local = new JSONObject();
                            try {
                                local.put("user_id",mUserid);
                                local.put("media_type", MEDIA_TYPE_VIDEO.ordinal());
                                streams.put(local);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

//                            PRTCMixProfile mixProfile = PRTCMixProfile.getInstance().assembleRelayMixParamsBuilder()
//                                    .pushUrl(pushURL)
//                                    .streams(streams)
//                                    .mainViewUserId(mUserid)
//                                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
//                                    .addStreamMode(PRTCMixProfile.ADD_STREAM_MODE_MANUAL)
//                                    .build();
//                            sdkEngine.startRelay(mixProfile);
                        } else if (!mAtomOpStart) {
                            mAtomOpStart = true;
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put("");
                            //sdkEngine.stopMix(PRTCMixProfile.MIX_TYPE_BOTH,"rtmp://rtcpush.ugslb.com/rtclive/"+mRoomid);
                        }
//                    mVideoPlayer.start();
                        break;
                }
            }
        });
        //动态增加流或者删除混流
        mAddDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mMixAddOrDel){
                   mMixAddOrDel = false;
                   mAddDelBtn.setText("del_st");
                   PRTCStreamInfo info = mVideoAdapter.getStreamInfo(0);
                   Log.d(TAG, "add stream: " + info);
                   JSONArray streams = new JSONArray();
                   JSONObject remote = new JSONObject();
                   try {
                       remote.put("user_id",info.getUId());
                       remote.put("media_type",info.getMediaType().ordinal());
                       streams.put(remote);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
                   //sdkEngine.addMixStream(info.getUId(), info.getMediaType().ordinal());
               }else{
                   mMixAddOrDel = true;
                   mAddDelBtn.setText("add_st");
                   PRTCStreamInfo info = mVideoAdapter.getStreamInfo(0);
                   Log.d(TAG, "del stream: " + info);
                   JSONArray streams = new JSONArray();
                   JSONObject remote = new JSONObject();
                   try {
                       remote.put("user_id",info.getUId());
                       remote.put("media_type",info.getMediaType().ordinal());
                       streams.put(remote);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
                   //sdkEngine.delMixStream(info.getUId(), info.getMediaType().ordinal());
               }
            }
        });

        mTextStream.setOnClickListener(new CustomerClickListener() {
            @Override
            protected void onSingleClick() {
                showPopupWindow();
            }

            @Override
            protected void onFastClick() {

            }
        });
        mSteamList = new ArrayList<>();
        mSpinnerPopupWindowScribe = new SteamScribePopupWindow(this, mSteamList);
        mSpinnerPopupWindowScribe.setAnimationStyle(0);
        mSpinnerPopupWindowScribe.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
            }
        });
        ((SteamScribePopupWindow) mSpinnerPopupWindowScribe).setmOnSubScribeListener(mOnSubscribeListener);
        //手动发布
        mPublish = findViewById(R.id.button_call_pub);
        mPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsPublished) {
                    sdkEngine.setStreamRole(PRTCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    if (mScreenEnable && !mCameraEnable && !mMicEnable) {
                        if (isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        }
                        else {
                            errorMessage.append("设备不支持屏幕捕捉\n");
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                        }
                    }
                    else if (mScreenEnable || mCameraEnable || mMicEnable) {
                        if (mScreenEnable && isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        }
                        results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, mCameraEnable, mMicEnable).getErrorCode());
                    }
                    else {
                        errorMessage.append("Camera, Mic or Screen is disable!\n");
                    }
/*                    switch (mCaptureMode) {
                        //音频
                        case CommonUtils.audio_capture_mode:
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            break;
                        //视频
                        case CommonUtils.camera_capture_mode:
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            break;
                        //屏幕捕捉
                        case CommonUtils.screen_capture_mode:
                            if (isScreenCaptureSupport) {
                                results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                            } else {
                                errorMessage.append("设备不支持屏幕捕捉\n");
                                results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            }
                            break;
                        //音频+屏幕捕捉
                        case CommonUtils.screen_Audio_mode:
                            if (isScreenCaptureSupport) {
                                //推一路桌面一路音频,桌面流不需要带音频
                                results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, false, false).getErrorCode());
                                results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            } else {
                                results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            }
                            break;
                        //视频+屏幕捕捉
                        case CommonUtils.multi_capture_mode:
                            if (isScreenCaptureSupport) {
                                results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                                results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            } else {
                                results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            }
                            break;
                    }*/

//            List<Integer> errorCodes = results.stream()
//                    .filter(result -> result != 0)
//                    .collect(Collectors.toList());
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
                    if (errorMessage.length() > 0)
                        ToastUtils.shortShow(RoomActivity.this, errorMessage.toString());
                    else {
                        ToastUtils.shortShow(RoomActivity.this, "发布");
                    }
                } else {
                    sdkEngine.unPublish(mPublishMediaType);
                }
            }
        });
        mHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callHangUp();
            }
        });

        mSwitchcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        mMuteMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleMic();
            }
        });

        mLoudSpkeader.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 onLoudSpeaker(!mSpeakerOn);
                                             }
                                         }
        );

        mMuteCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleCamera();
            }
        });

        title = findViewById(R.id.text_room);
        title.setText("roomid: " + mRoomid);
        //title.setText("roomid: "+mRoomid+"\nuid: "+ mUserid);

        localrenderview = findViewById(R.id.localview);
//        localrenderview.init(true, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio});
//        localrenderview.init(true);
        localrenderview.init();
        localrenderview.setZOrderMediaOverlay(false);
        localrenderview.setMirror(true);
        localprocess = findViewById(R.id.processlocal);
        isScreenCaptureSupport = PRTCEnvHelper.isSupportScreenCapture();
        mCameraEnable = preferences.getBoolean(CommonUtils.CAMERA_ENABLE, CommonUtils.CAMERA_ON);
        mMicEnable = preferences.getBoolean(CommonUtils.MIC_ENABLE, CommonUtils.MIC_ON);
        mScreenEnable = preferences.getBoolean(CommonUtils.SCREEN_ENABLE, CommonUtils.SCREEN_OFF);
//        Log.d(TAG, " mCaptureMode " + mCaptureMode);
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
/*            switch (mCaptureMode) {
            case CommonUtils.audio_capture_mode:
                sdkEngine.setAudioOnlyMode(true);
                sdkEngine.configLocalCameraPublish(false);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.camera_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                sdkEngine.configLocalCameraPublish(true);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.screen_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(false);
                } else {
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                    sdkEngine.configLocalScreenPublish(false);
                }
                break;
            case CommonUtils.screen_Audio_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
            case CommonUtils.multi_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
        }*/

        defaultAudioDevice = sdkEngine.getDefaultAudioDevice();
//        LogUtils.d(TAG,"AudioManager audio device room with: "+defaultAudioDevice);
        if (defaultAudioDevice == PRTCAudioDevice.AUDIO_DEVICE_SPEAKER) {
            mLoudSpkeader.setImageResource(R.mipmap.loudspeaker);
            mSpeakerOn = true;
        } else {
            mSpeakerOn = false;
            mLoudSpkeader.setImageResource(R.mipmap.loudspeaker_disable);
        }
        int role = preferences.getInt(CommonUtils.SDK_STREAM_ROLE, PRTCClientRole.CLIENT_ROLE_BROADCASTER.ordinal());
        mRole = PRTCClientRole.valueOf(role);
        sdkEngine.setStreamRole(mRole);
        int classType = preferences.getInt(CommonUtils.SDK_CLASS_TYPE, PRTCChannelProfile.ROOM_SMALL.ordinal());
        mClass = PRTCChannelProfile.valueOf(classType);
        sdkEngine.setClassType(mClass);
        mPublishMode = preferences.getInt(CommonUtils.PUBLISH_MODE, CommonUtils.AUTO_MODE);
        sdkEngine.setAutoPublish(mPublishMode == CommonUtils.AUTO_MODE ? true : false);
        mScribeMode = preferences.getInt(CommonUtils.SUBSCRIBE_MODE, CommonUtils.AUTO_MODE);
        if (mScribeMode == CommonUtils.AUTO_MODE) {
            mStreamSelect.setVisibility(View.GONE);
        } else {
            mStreamSelect.setVisibility(View.VISIBLE);
        }
        sdkEngine.setAutoSubscribe(mScribeMode == CommonUtils.AUTO_MODE ? true : false);
        //设置sdk 外部扩展模式及其采集的帧率，同时sdk内部会自动调整初始码率和最小码率
        //扩展模式只支持720p的分辨率及以下，若要自定义更高分辨率，请联系商务定制，否则sdk会抛出异常，终止运行。
//        sdkEngine.setVideoProfile(PRTCVideoProfile.VIDEO_PROFILE_EXTEND.extendParams(30,640,480));
        sdkEngine.setVideoProfile(PRTCVideoProfile.matchValue(mVideoProfile));

        initButtonSize();
        PRTCAuthInfo info = new PRTCAuthInfo();
        info.setAppId(mAppid);
        info.setToken(mRoomToken);
        info.setRoomId(mRoomid);
        info.setUId(mUserid);
        Log.d(TAG, " roomtoken = " + mRoomToken);
        //普通摄像头捕获方式，与扩展模式二选一
        PRTCEnvHelper.setCaptureMode(
                PRTCCaptureMode.CAPTURE_MODE_LOCAL.ordinal());
        //rgb数据捕获，与普通捕获模式二选一
//        PRTCEnvHelper.setCaptureMode(
//                PRTCCaptureMode.CAPTURE_MODE_EXTEND);
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                try{
//                    RGBSourceData sourceData;
//                    Bitmap bitmap = null;
//                    int type;
//                    if(mPFlag){
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                        bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.img1_640,options);
//                        type = PRTCDataProvider.RGBA_TO_I420;
//                    }
//                    else{
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                        bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.img_640,options);
//                        type = PRTCDataProvider.RGBA_TO_I420;
//                    }
//                    mPFlag = !mPFlag;
////                            if(++mPictureFlag >50)
////                                mPictureFlag = 0;
//                    if(bitmap != null){
//                        sourceData = new RGBSourceData(bitmap,bitmap.getWidth(),bitmap.getHeight(),type);
//                        //add rgbdata
//                        mQueue.put(sourceData);
////                                Log.d(TAG, "create bitmap: " + bitmap + "count :" + memoryCount.incrementAndGet());
//                    }
////                            }
////                        }
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        };

        Runnable imgTask = new Runnable() {
            @Override
            public void run() {
                    while(startCreateImg){
                        try{
                            RGBSourceData sourceData;
                            Bitmap bitmap = null;
                            int type;
                            if(mPFlag){
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.pic_1080_1,options);
                                type = IPRTCDataProvider.RGBA_TO_I420;
                            }
                            else{
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.pic_1080_2,options);
                                type = IPRTCDataProvider.RGBA_TO_I420;
                            }
                            mPFlag = !mPFlag;
//                            if(++mPictureFlag >50)
//                                mPictureFlag = 0;
                            if(bitmap != null){
                                sourceData = new RGBSourceData(bitmap,bitmap.getWidth(),bitmap.getHeight(),type);
                                //add rgbdata
                                mQueue.put(sourceData);
//                                Log.d(TAG, "create bitmap: " + bitmap + "count :" + memoryCount.incrementAndGet());
                            }
//                            }
//                        }

                             Thread.sleep((int)(Math.random()*20));
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        //可以添加nv21 的数据,请根据实际情况拿到bytebuffer的数据,图像宽高
//                        try {
//                            ByteBuffer byteBuffer = null;
//                            NVSourceData nvSourceData = new NVSourceData(byteBuffer,1280,720,PRTCDataProvider.NV21);
//                            mQueueNV.put(nvSourceData);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                    //这里在回收一遍 防止队列不阻塞了在destroy以后又产生了bitmap没回收
                    while(mQueue.size() != 0 ){
                        RGBSourceData rgbSourceData = mQueue.poll();
                        if(rgbSourceData != null){
                            recycleBitmap(rgbSourceData.getSrcData());
                            rgbSourceData.srcData = null;
                            rgbSourceData = null;
                        }
                    }
            }
        };

        if(PRTCEnvHelper.getCaptureMode() == PRTCCaptureMode.CAPTURE_MODE_EXTEND.ordinal() &&
                (mRole == PRTCClientRole.CLIENT_ROLE_BROADCASTER ||
                        mRole == PRTCClientRole.CLIENT_ROLE_PUBLISHER)){

            mCreateImgThread = new Thread(imgTask);
            mCreateImgThread.setName("create picture");
            mCreateImgThread.start();
//            mTimerCreateImg.scheduleAtFixedRate(timerTask,0,10);
            IPRTCEngine.onRGBCaptureResult(mIPRTCDataProvider);
        }
        sdkEngine.joinChannel(info);
        initRecordManager();
    }

    private void recycleBitmap(Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
//            Log.d(TAG, "recycleBitmap: " + bitmap + "count: "+ (memoryCount.decrementAndGet()));
        }
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
            ToastUtils.shortShow(RoomActivity.this,"screen shoot : " + name);
        }
    };

    private void addScreenShotCallBack(View view){
        if(view instanceof PRTCSurfaceViewGroup){
            ((PRTCSurfaceViewGroup)view).setScreenShotBack(mIPRTCScreenShot);
        }else if(view instanceof PRTCSurfaceViewRenderer){
            ((PRTCSurfaceViewRenderer)view).setScreenShotBack(mIPRTCScreenShot);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
//        boolean hasSwap = false;
//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//            RTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
//            View videoView = info.getmRenderview();
//            PRTCStreamInfo videoViewStreamInfo = (PRTCStreamInfo) videoView.getTag();
//            PRTCStreamInfo videoViewSwapStreamInfo = (PRTCStreamInfo) videoView.getTag(R.id.swap_info);
//            if (videoView != null && videoViewStreamInfo != null) {
//                if (videoViewSwapStreamInfo != null) {
//                    //恢复交换后的小窗本地视频
//                    sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), videoView,null,null);
//                    //恢复交换后的大窗远程视频
//                    sdkEngine.startRemoteView(videoViewStreamInfo, localrenderview,null,null);
//                    hasSwap = true;
//                } else {
//                    sdkEngine.startRemoteView(videoViewStreamInfo, videoView,null,null);
//                }
//            }
//        }
//        if (!hasSwap) {
//            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
////            sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true);
//        } if (!hasSwap) {
//            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
////            sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
        if(mIsPublished){
//            Intent service = new Intent(this, RTCForeGroundService.class);
//            startService(service);
            sdkEngine.controlAudio(false);
            sdkEngine.controlLocalVideo(false);
        }

//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//            RTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
//            View videoView = info.getmRenderview();
//            PRTCStreamInfo videoViewStreamInfo = (PRTCStreamInfo) videoView.getTag();
//            if (videoView != null && videoViewStreamInfo != null) {
//                sdkEngine.stopRemoteView(videoViewStreamInfo);
//            }
//        }
//        if (mLocalStreamInfo != null)
//            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
    }


    //    SteamScribePopupWindow.OnSpinnerItemClickListener mOnSubscribe = new SteamScribePopupWindow.OnSpinnerItemClickListener() {
//        @Override
//        public void onItemClick(int pos) {
//            mSelectPos = pos;
//            mTextStream.setText(pos);
//            mSpinnerPopupWindowScribe.dismiss();
//        }
//    };

    //手动订阅
    SteamScribePopupWindow.OnSubscribeListener mOnSubscribeListener = new SteamScribePopupWindow.OnSubscribeListener() {
        @Override
        public void onSubscribe(List<PRTCStreamInfo> dataInfo) {
            for (PRTCStreamInfo streamInfo : dataInfo) {
                PRTCErrorCode result = sdkEngine.subscribe(streamInfo);
                if (result.ordinal() != NET_ERR_CODE_OK.ordinal()) {
                    ToastUtils.shortShow(RoomActivity.this, "RTC_SDK_ERROR_CODE:" + result.getErrorCode());
                }
            }
            mSpinnerPopupWindowScribe.dismiss();
        }
    };

    private void showPopupWindow() {
        if (!mSpinnerPopupWindowScribe.isShowing()) {
            mSpinnerPopupWindowScribe.setWidth(mTextStream.getWidth());
            mSpinnerPopupWindowScribe.showAsDropDown(mTextStream);
        }
    }

    private void initButtonSize() {
        int screenWidth = UiHelper.getScreenPixWidth(this);
        int leftRightMargin = UiHelper.dipToPx(this, 30 * 2);
        int gap = UiHelper.dipToPx(this, 8);
        int buttonSize;
        if (mPublishMode == CommonUtils.AUTO_MODE) {
            buttonSize = (screenWidth - leftRightMargin - gap * 4) / 5;
            mPublish.setVisibility(View.GONE);
        } else {
            buttonSize = (screenWidth - leftRightMargin - gap * 5) / 6;
            mPublish.setVisibility(View.VISIBLE);
            setButtonSize(mPublish, buttonSize);
        }
        setButtonSize(mHangup, buttonSize);
        setButtonSize(mLoudSpkeader, buttonSize);
        setButtonSize(mSwitchcam, buttonSize);
        setButtonSize(mMuteCam, buttonSize);
        setButtonSize(mMuteMic, buttonSize);
    }

    private void setButtonSize(View button, int buttonSize) {
        button.getLayoutParams().width = buttonSize;
        button.getLayoutParams().height = buttonSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent service = new Intent(this, RTCForeGroundService.class);
//        stopService(service);
        sdkEngine.controlAudio(true);
        sdkEngine.controlLocalVideo(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "activity destory");
        super.onDestroy();
        localrenderview.release();
        clearGridItem();
        mVideoAdapter.setRemoveRemoteStreamReceiver(null);
        mIPRTCDataProvider.releaseBuffer();
        mIPRTCDataProvider = null;
        mIPRTCDataReceiver.releaseBuffer();
        mIPRTCDataReceiver = null;
        if(PRTCEnvHelper.getCaptureMode() == PRTCCaptureMode.CAPTURE_MODE_EXTEND.ordinal() &&
                (mRole == PRTCClientRole.CLIENT_ROLE_BROADCASTER ||
                        mRole == PRTCClientRole.CLIENT_ROLE_PUBLISHER)) {
            startCreateImg = false;
            //这里回收一遍
            while(mQueue.size() != 0 ){
                RGBSourceData rgbSourceData = mQueue.poll();
                if(rgbSourceData != null){
                    recycleBitmap(rgbSourceData.getSrcData());
                    rgbSourceData.srcData = null;
                    rgbSourceData = null;
                }

            }
        }
//        IPRTCEngine.destory();
//        if(mVideoPlayer != null ){
//            mVideoPlayer.stop();
//        }
        System.gc();
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private void callHangUp() {
        int ret = sdkEngine.leaveChannel().ordinal();
//        if (ret != NET_ERR_CODE_OK.ordinal()) {
            Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
            onMediaServerDisconnect();
            startActivity(intent);
            finish();
//        }
    }

    boolean mSwitchCam = false;

    private void switchCamera() {
        sdkEngine.switchCamera();
        ToastUtils.shortShow(this, "切换摄像头");
//        mSwitchcam.setImageResource(mSwitchCam ? R.mipmap.camera_switch_front :
//                R.mipmap.camera_switch_end);
        mSwitchCam = !mSwitchCam;
    }

    boolean mMuteMicBool = false;

    private boolean onToggleMic() {
        sdkEngine.muteLocalMic(!mMuteMicBool);
        if (!mMuteMicBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭麦克风");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开麦克风");
        }
        return false;
    }

    boolean mMuteCamBool = false;

    private boolean onToggleCamera() {
/*        if (mCaptureMode == CommonUtils.camera_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, MEDIA_TYPE_VIDEO);
        } else if (mCaptureMode == CommonUtils.screen_capture_mode) {
            if (isScreenCaptureSupport) {
                sdkEngine.muteLocalVideo(!mMuteCamBool, PRTCMediaType.MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteCamBool, MEDIA_TYPE_VIDEO);
            }
        } else if (mCaptureMode == CommonUtils.multi_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, MEDIA_TYPE_VIDEO);
        }*/
        if (mScreenEnable || mCameraEnable) {
            if (isScreenCaptureSupport && !mCameraEnable) {
                sdkEngine.muteLocalVideo(!mMuteCamBool, PRTCMediaType.MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteCamBool, MEDIA_TYPE_VIDEO);
            }
        }
        if (!mMuteCamBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭摄像头");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开摄像头");
        }
        return false;
    }

    private void onMuteCamResult(boolean mute) {
        mMuteCamBool = mute;
        mMuteCam.setImageResource(mute ? R.mipmap.video_close : R.mipmap.video_open);
        if (localrenderview.getTag(R.id.swap_info) != null) {
            PRTCStreamInfo remoteInfo = (PRTCStreamInfo) localrenderview.getTag(R.id.swap_info);
            String mkey = remoteInfo.getUId() + remoteInfo.getMediaType().toString();
            View view = mRemoteGridView.getChildAt(mVideoAdapter.getPositionByKey(mkey));
            if (mute) {
                view.setVisibility(View.INVISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (mute) {
//                localrenderview.refresh();
                localrenderview.setVisibility(View.INVISIBLE);
            } else {
                localrenderview.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onMuteMicResult(boolean mute) {
        mMuteMicBool = mute;
        mMuteMic.setImageResource(mute ? R.mipmap.microphone_disable : R.mipmap.microphone);
    }

    boolean mSpeakerOn = true;
    PRTCAudioDevice defaultAudioDevice;

    private void onLoudSpeaker(boolean enable) {
        if (mSpeakerOn) {
            ToastUtils.shortShow(RoomActivity.this, "关闭喇叭");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开喇叭");
        }
        mSpeakerOn = !mSpeakerOn;
        sdkEngine.setSpeakerOn(enable);
        mLoudSpkeader.setImageResource(enable ? R.mipmap.loudspeaker : R.mipmap.loudspeaker_disable);
    }

    private void clearGridItem() {
        mVideoAdapter.clearAll();
        mVideoAdapter.notifyDataSetChanged();
    }

    private void startTimeShow() {
        timeshow.setBase(SystemClock.elapsedRealtime());
        timeshow.start();
    }

    private void stopTimeShow() {
        timeshow.stop();
    }

    //初始化视频
    public static void initRecordManager() {
        // 设置拍摄视频缓存路径
//        File dcim = Environment
//                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        RtcRecordManager.init("");
        Log.d(TAG, "initRecordManager: cache path:" + RtcRecordManager.getVideoCachePath());
    }


}
