//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.lvqingyang.scancloud.ar;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Base64;
import android.util.Log;

import com.lvqingyang.mylibrary.tool.MyJson;
import com.lvqingyang.scancloud.BuildConfig;
import com.lvqingyang.scancloud.R;
import com.lvqingyang.scancloud.ar.filter.AFilter;
import com.lvqingyang.scancloud.ar.filter.CameraFilter;
import com.lvqingyang.scancloud.ar.filter.WaterMarkFilter;
import com.lvqingyang.scancloud.ar.utils.EasyGlUtils;
import com.lvqingyang.scancloud.base.MyApplication;
import com.lvqingyang.scancloud.bean.TargetMeta;

import java.util.ArrayList;
import java.util.HashSet;

import javax.microedition.khronos.opengles.GL10;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.CloudRecognizer;
import cn.easyar.CloudStatus;
import cn.easyar.Frame;
import cn.easyar.FunctorOfVoidFromCloudStatus;
import cn.easyar.FunctorOfVoidFromCloudStatusAndListOfPointerOfTarget;
import cn.easyar.FunctorOfVoidFromPermissionStatusAndString;
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.FunctorOfVoidFromRecordStatusAndString;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.RecordProfile;
import cn.easyar.RecordStatus;
import cn.easyar.RecordVideoOrientation;
import cn.easyar.RecordZoomMode;
import cn.easyar.Recorder;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;

class _HelloAR {
    private static final String TAG = "HelloAR";
    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    /**
     * 一系列ImageTracker
     * ImageTarget表示平面图像的target，它可以被 ImageTracker 所跟踪。
     */
    private ArrayList<ImageTracker> mImageTrackers;
    private VideoRenderer mVideoRenderer;
    private Renderer mVideobgRenderer;
    private RecorderRenderer mRecorderRenderer;
    /**
     * Recorder 实现了对当前渲染环境的视频录制功能。
     * 当前Recorder 只在 Android（4.3 或更新）和 iOS的OpenGL ES 2.0 环境下工作。
     */
    private Recorder mRecorder;
    private int mTrackedTarget = 0;
    private int mActiveTarget = 0;
    private ARVideo mARVideo = null;
    private CloudRecognizer mCloudRecognizer;
    private boolean mViewportChanged = false;
    private Vec2I mViewSize = new Vec2I(0, 0);
    private int mRotation = 0;
    private Vec4I mViewport = new Vec4I(0, 0, 1280, 720);
    private boolean mIsRecordingStarted = false;
    private OnTargetChangeListener mOnTargetChangeListener;
    private boolean mIsBackCamera;
    //水印
    private WaterMarkFilter mWaterMarkFilter;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    /**控件的宽高*/
    private int width = 0,height = 0;
    private SurfaceTexture mSurfaceTextrue;
    private int textureID;
    /**后台绘制的filter*/
    private final AFilter drawFilter;


    _HelloAR() {
        mImageTrackers = new ArrayList<>();
        Resources resources=MyApplication.getContext().getResources();
        mWaterMarkFilter = new WaterMarkFilter(resources);
        mWaterMarkFilter.setWaterMark(BitmapFactory.decodeResource(resources, R.drawable.ic_logo));
        mWaterMarkFilter.setPosition(30,50,0,0);
        drawFilter = new CameraFilter(resources);
    }

    public void setOnTargetChangeListener(OnTargetChangeListener onTargetChangeListener) {
        mOnTargetChangeListener = onTargetChangeListener;
    }

    private void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadAllFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    try {
                        Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                    } catch (Throwable ex) {
                    }
                }
            });
        }
    }

    public boolean initialize(String cloud_server_address, String cloud_key, String cloud_secret) {
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);
        mCloudRecognizer = new CloudRecognizer();
        mCloudRecognizer.attachStreamer(streamer);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Back);
        camera.setSize(new Vec2I(1280, 720));
        mIsBackCamera=true;
        mCloudRecognizer.open(cloud_server_address, cloud_key, cloud_secret, new FunctorOfVoidFromCloudStatus() {
            @Override
            public void invoke(int status) {
                if (status == CloudStatus.Success) {
                    Log.i("HelloAR", "CloudRecognizerInitCallBack: Success");
                } else if (status == CloudStatus.Reconnecting) {
                    Log.i("HelloAR", "CloudRecognizerInitCallBack: Reconnecting");
                } else if (status == CloudStatus.Fail) {
                    Log.i("HelloAR", "CloudRecognizerInitCallBack: Fail");
                } else {
                    Log.i("HelloAR", "CloudRecognizerInitCallBack: " + Integer.toString(status));
                }
            }
        }, new FunctorOfVoidFromCloudStatusAndListOfPointerOfTarget() {
            private HashSet<String> uids = new HashSet<String>();

            @Override
            public void invoke(int status, ArrayList<Target> targets) {
                if (status == CloudStatus.Success) {
                    Log.i("HelloAR", "CloudRecognizerCallBack: Success");
                } else if (status == CloudStatus.Reconnecting) {
                    Log.i("HelloAR", "CloudRecognizerCallBack: Reconnecting");
                } else if (status == CloudStatus.Fail) {
                    Log.i("HelloAR", "CloudRecognizerCallBack: Fail");
                } else {
                    Log.i("HelloAR", "CloudRecognizerCallBack: " + Integer.toString(status));
                }
                synchronized (uids) {
                    for (Target t : targets) {
                        if (!uids.contains(t.uid())) {
                            Log.i("HelloAR", "add cloud target: " + t.uid());
                            uids.add(t.uid());
                            mImageTrackers.get(0).loadTarget(t, new FunctorOfVoidFromPointerOfTargetAndBool() {
                                @Override
                                public void invoke(Target target, boolean status) {
                                    Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                                }
                            });
                        }
                    }
                }
            }
        });

        if (!status) {
            return status;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);
        ////////////////////////////////////////////////////////////////
//        loadAllFromJsonFile(tracker, "targets.json");
//        loadFromImage(tracker, "namecard.jpg");
        ////////////////////////////////////////////////////////////////
        mImageTrackers.add(tracker);

        return status;
    }

    public void dispose() {
        if (mRecorder != null) {
            mRecorder.dispose();
            mRecorder = null;
        }
        mRecorderRenderer = null;

        if (mARVideo != null) {
            mARVideo.dispose();
            mARVideo = null;
        }
        mTrackedTarget = 0;
        mActiveTarget = 0;

        for (ImageTracker tracker : mImageTrackers) {
            tracker.dispose();
        }
        mImageTrackers.clear();
        if (mCloudRecognizer != null) {
            mCloudRecognizer.dispose();
            mCloudRecognizer = null;
        }
        mVideoRenderer = null;
        if (mVideobgRenderer != null) {
            mVideobgRenderer.dispose();
            mVideobgRenderer = null;
        }
        if (streamer != null) {
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
    }

    public boolean start() {
        boolean status = true;
        status &= (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();
        status &= (mCloudRecognizer != null) && mCloudRecognizer.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        for (ImageTracker tracker : mImageTrackers) {
            status &= tracker.start();
        }
        return status;
    }

    public boolean stop() {
        boolean status = true;
        for (ImageTracker tracker : mImageTrackers) {
            status &= tracker.stop();
        }
        status &= (mCloudRecognizer != null) && mCloudRecognizer.stop();
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    public void initGL() {
        if (mActiveTarget != 0) {
            mARVideo.onLost();
            mARVideo.dispose();
            mARVideo = null;
            mTrackedTarget = 0;
            mActiveTarget = 0;
        }
        if (mVideobgRenderer != null) {
            mVideobgRenderer.dispose();
        }

        if (mRecorder != null) {
            mRecorder.dispose();
            mRecorder = null;
        }

        mVideoRenderer = new VideoRenderer();
        mVideoRenderer.init();
        mVideobgRenderer = new Renderer();
        mRecorderRenderer = new RecorderRenderer();
        mRecorder = new Recorder();

        //
        textureID = createTextureID();
        mSurfaceTextrue = new SurfaceTexture(textureID);

        drawFilter.create();
        drawFilter.setTextureId(textureID);
        mWaterMarkFilter.create();
    }

    public void resizeGL(int width, int height) {
        //清除遗留的
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteTextures(1, fTexture, 0);
        /**创建一个帧染缓冲区对象*/
        GLES20.glGenFramebuffers(1,fFrame,0);
        /**根据纹理数量 返回的纹理索引*/
        GLES20.glGenTextures(1, fTexture, 0);
       /* GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width,
                height);*/
        /**将生产的纹理名称和对应纹理进行绑定*/
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]);
        /**根据指定的参数 生产一个2D的纹理 调用该函数前  必须调用glBindTexture以指定要操作的纹理*/
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0,  GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        useTexParameter();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        mViewSize = new Vec2I(width, height);
        mViewportChanged = true;
        mWaterMarkFilter.setSize(width, height);
    }

    private void updateViewport() {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.mRotation) {
            this.mRotation = rotation;
            mViewportChanged = true;
        }
        if (mViewportChanged) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) mViewSize.data[0] / (float) size.data[0], (float) mViewSize.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            mViewport = new Vec4I((mViewSize.data[0] - viewport_size.data[0]) / 2, (mViewSize.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened()) {
                mViewportChanged = false;
            }

            if (mIsRecordingStarted) {
                mRecorderRenderer.resize(mViewSize.data[0], mViewSize.data[1]);
            }
        }
    }

    private void targetChange(Target target, TargetMeta targetMeta) {
        if (mOnTargetChangeListener != null) {
            mOnTargetChangeListener.targetChange(target, targetMeta);
        }
    }

    private void targetLost() {
        if (mOnTargetChangeListener != null) {
            mOnTargetChangeListener.targetLost();
        }
    }

    private void targetTrack() {
        if (mOnTargetChangeListener != null) {
            mOnTargetChangeListener.targetTrack();
        }
    }

    public void preRender() {
        if (!mIsRecordingStarted) {
            return;
        }
        mRecorderRenderer.preRender();
    }

    public void postRender() {
        if (!mIsRecordingStarted) {
            return;
        }
        mRecorderRenderer.postRender(mViewport);
        mRecorder.updateFrame();
    }

    public void render() {
        EasyGlUtils.bindFrameTexture(fFrame[0],fTexture[0]);
        GLES20.glViewport(0,0,mViewSize.data[0],mViewSize.data[1]);
        drawFilter.draw();
        EasyGlUtils.unBindFrameBuffer();

        mWaterMarkFilter.setTextureId(fTexture[0]);
        mWaterMarkFilter.draw();


        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mVideobgRenderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, mViewSize.data[0], mViewSize.data[1]);
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (mVideobgRenderer.renderErrorMessage(default_viewport)) {
                return;
            }
        }

        if (streamer == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "render: streamer == null");
            }
            return;
        }
        Frame frame = streamer.peek();
        try {
            updateViewport();
            GLES20.glViewport(mViewport.data[0], mViewport.data[1], mViewport.data[2], mViewport.data[3]);

            if (mVideobgRenderer != null) {
                mVideobgRenderer.render(frame, mViewport);
            }

            ArrayList<TargetInstance> targetInstances = frame.targetInstances();
            if (targetInstances.size() > 0) {
                TargetInstance targetInstance = targetInstances.get(0);
                Target target = targetInstance.target();
                int status = targetInstance.status();
                //target是跟踪状态
                if (status == TargetStatus.Tracked) {
//                    if (BuildConfig.DEBUG) Log.d(TAG, "render: Tracked");
                    //target id是运行时创建的整形数据。这个id是非0且全局递增的。
                    int id = target.runtimeID();
                    if (mActiveTarget != 0 && mActiveTarget != id) {
                        mARVideo.onLost();
                        mARVideo.dispose();
                        mARVideo = null;
                        mTrackedTarget = 0;
                        mActiveTarget = 0;
                    }
                    //新的target
                    if (mTrackedTarget == 0) {

                        String meta = new String(Base64.decode(target.meta(), Base64.URL_SAFE));
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "render: meda:" + meta);
                        }
                        TargetMeta targetMeta= MyJson.fromJson(meta, TargetMeta.class);
                        if (mARVideo == null && mVideoRenderer != null && targetMeta != null) {
                            //target改变，回调通知
                            targetChange(target, targetMeta);
                            mARVideo = new ARVideo();
                            mARVideo.openStreamingVideo(targetMeta.getVideoUrl(), mVideoRenderer.texId());
                        }
                        if (mARVideo != null) {
                            targetTrack();
                            mARVideo.onFound();
                            mTrackedTarget = id;
                            mActiveTarget = id;
                        }
                    }

                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    if (imagetarget != null) {
                        if (mVideoRenderer != null) {
                            mARVideo.update();
                            if (mARVideo.isRenderTextureAvailable()) {
                                mVideoRenderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());
                            }
                        }
                    }
                }
            } else {
                if (mTrackedTarget != 0) {
                    targetLost();
                    mARVideo.onLost();
                    mTrackedTarget = 0;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            frame.dispose();
        }
    }


    public void requestPermissions(FunctorOfVoidFromPermissionStatusAndString callback) {
        mRecorder.requestPermissions(callback);
    }

    public void startRecording(String path, final FunctorOfVoidFromRecordStatusAndString callback) {
        if (mIsRecordingStarted) {
            return;
        }
        mRecorder.setOutputFile(path);
        mRecorder.setZoomMode(RecordZoomMode.ZoomInWithAllContent);
        mRecorder.setProfile(RecordProfile.Quality_720P_Middle);
        mRecorderRenderer.resize(mViewSize.data[0], mViewSize.data[1]);
        mRecorder.setInputTexture(mRecorderRenderer.getTextureId(), mViewSize.data[0], mViewSize.data[1]);
        int mode = mViewSize.data[0] < mViewSize.data[1] ? RecordVideoOrientation.Portrait : RecordVideoOrientation.Landscape;
        mRecorder.setVideoOrientation(mode);
        mRecorder.open(new FunctorOfVoidFromRecordStatusAndString() {
            @Override
            public void invoke(int status, String value) {
                if (status == RecordStatus.OnStopped) {
                    mIsRecordingStarted = false;
                }
                callback.invoke(status, value);
            }
        });
        mRecorder.start();
        mIsRecordingStarted = true;
    }

    public void stopRecording() {
        if (!mIsRecordingStarted) {
            return;
        }
        mRecorder.stop();
        mIsRecordingStarted = false;
    }

    public void setFlashState(boolean isOn){
        if (mIsBackCamera) {
            camera.setFlashTorchMode(isOn);
        }
    }

    public void switchCamera(){
        camera.stop();
        camera.open(mIsBackCamera? CameraDeviceType.Front: CameraDeviceType.Back);
        camera.setSize(new Vec2I(1280, 720));
        camera.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        mIsBackCamera=!mIsBackCamera;
    }

    public  void useTexParameter(){
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**创建显示的texture*/
    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }
}
