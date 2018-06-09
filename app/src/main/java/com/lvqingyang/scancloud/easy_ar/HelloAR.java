//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.lvqingyang.scancloud.easy_ar;

import android.opengl.GLES20;
import android.util.Base64;
import android.util.Log;

import com.lvqingyang.scancloud.BuildConfig;

import java.util.ArrayList;
import java.util.HashSet;

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
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;

class HelloAR {
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
    private int mTrackedTarget = 0;
    private int mActiveTarget = 0;
    private ARVideo mARVideo = null;
    private CloudRecognizer mCloudRecognizer;
    private boolean mViewportChanged = false;
    private Vec2I mViewSize = new Vec2I(0, 0);
    private int mRotation = 0;
    private Vec4I mVec4I = new Vec4I(0, 0, 1280, 720);
    private OnTargetChangeListener mOnTargetChangeListener;

    HelloAR() {
        mImageTrackers = new ArrayList<>();
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
        status &= camera.open(CameraDeviceType.Default);
        camera.setSize(new Vec2I(1280, 720));
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

        mVideoRenderer = new VideoRenderer();
        mVideoRenderer.init();
        mVideobgRenderer = new Renderer();
    }

    public void resizeGL(int width, int height) {
        mViewSize = new Vec2I(width, height);
        mViewportChanged = true;
    }

    public void render() {
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
            if (BuildConfig.DEBUG) Log.d(TAG, "render: streamer == null");
            return;
        }
        Frame frame = streamer.peek();
        try {
            updateViewport();
            GLES20.glViewport(mVec4I.data[0], mVec4I.data[1], mVec4I.data[2], mVec4I.data[3]);

            if (mVideobgRenderer != null) {
                mVideobgRenderer.render(frame, mVec4I);
            }

            ArrayList<TargetInstance> targetInstances = frame.targetInstances();
            if (targetInstances.size() > 0) {
                TargetInstance targetInstance = targetInstances.get(0);
                Target target = targetInstance.target();
                int status = targetInstance.status();
                //target是跟踪状态
                if (status == TargetStatus.Tracked) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "render: Tracked");
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
                        //回调通知
                        targetChange(target);

                        String meta = new String(Base64.decode(target.meta(), Base64.URL_SAFE));
                        if (BuildConfig.DEBUG) Log.d(TAG, "render: meda:"+meta);
                        if (mARVideo == null && mVideoRenderer != null && meta != null) {
                            mARVideo = new ARVideo();
                            mARVideo.openStreamingVideo(meta, mVideoRenderer.texId());
                        }
                        if (mARVideo != null) {
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
            mVec4I = new Vec4I((mViewSize.data[0] - viewport_size.data[0]) / 2, (mViewSize.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened()) {
                mViewportChanged = false;
            }
        }
    }

    private void targetChange(Target target) {
        if (mOnTargetChangeListener != null) {
            mOnTargetChangeListener.targetChange(target);
        }
    }
}
