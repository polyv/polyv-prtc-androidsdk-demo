package com.prtcdemo.view;

import com.polyv.prtc.sdkengine.define.PRTCMediaType;
import com.polyv.prtc.sdkengine.define.PRTCTextureViewRenderer;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewRenderer;
import com.polyv.prtc.sdkengine.define.PRTCStreamInfo;
import com.polyv.prtc.sdkengine.define.PRTCSurfaceViewGroup;

public class RTCVideoViewInfo {
    private Object mRenderview ;
    private String mUid ;
    private boolean mEanbleVideo ;
    private boolean mEnableAudio;
    private PRTCMediaType mMediatype ;
    private String key;
    private PRTCStreamInfo mStreamInfo;
    public RTCVideoViewInfo(){
        mUid = "" ;
        mEanbleVideo = false ;
        mMediatype = PRTCMediaType.MEDIA_TYPE_NULL;
    }
    public RTCVideoViewInfo(PRTCSurfaceViewGroup view) {
        mRenderview = view ;
        mUid = "" ;
        mEanbleVideo = false ;
        mMediatype = PRTCMediaType.MEDIA_TYPE_NULL;
    }

    public RTCVideoViewInfo(PRTCStreamInfo info) {
        mRenderview = null ;
        mUid = info.getUId() ;
        mEanbleVideo = info.isHasVideo() ;
        mEnableAudio = info.isHasAudio();
        mMediatype = info.getMediaType();
        mStreamInfo = info;
    }

    public Object getmRenderview() {
        return mRenderview;
    }

    public void setmRenderview(Object mRenderview) {
        this.mRenderview = mRenderview;
    }

    public boolean isEnableAudio() {
        return mEnableAudio;
    }

    public void setEnableAudio(boolean enableAudio) {
        mEnableAudio = enableAudio;
    }

    public boolean ismEanbleVideo() {
        return mEanbleVideo;
    }

    public void setmEanbleVideo(boolean mEanbleVideo) {
        this.mEanbleVideo = mEanbleVideo;
    }

    public String getmUid() {
        return mUid;
    }

    public void setmUid(String mUid) {
        this.mUid = mUid;
    }

    public PRTCMediaType getmMediatype() {
        return mMediatype;
    }

    public void setmMediatype(PRTCMediaType mMediatype) {
        this.mMediatype = mMediatype;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setStreamInfo(PRTCStreamInfo streamInfo) {
        mStreamInfo = streamInfo;
    }

    public PRTCStreamInfo getStreamInfo() {
        return mStreamInfo;
    }

    public Object release() {
        if (mRenderview != null) {
            if(mRenderview instanceof PRTCSurfaceViewGroup){
                ((PRTCSurfaceViewGroup)mRenderview).refresh();
                ((PRTCSurfaceViewGroup)mRenderview).release();
            }else if(mRenderview instanceof PRTCSurfaceViewRenderer){
                ((PRTCSurfaceViewRenderer)mRenderview).release();
            }
            else if(mRenderview instanceof PRTCTextureViewRenderer){
                ((PRTCTextureViewRenderer)mRenderview).release();
            }
//            mRenderview = null ;

        }
        return mRenderview;
    }

}
