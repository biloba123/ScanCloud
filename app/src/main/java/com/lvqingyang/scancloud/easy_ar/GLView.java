//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.lvqingyang.scancloud.easy_ar;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import cn.easyar.Engine;

public class GLView extends GLSurfaceView {
    private HelloAR mHelloAR;
    private String cloud_server_address;
    private String cloud_key;
    private String cloud_secret;

    public GLView(Context context, String cloud_server_address, String cloud_key, String cloud_secret)
    {
        super(context);
        this.cloud_server_address = cloud_server_address;
        this.cloud_key = cloud_key;
        this.cloud_secret = cloud_secret;

        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());

        mHelloAR = new HelloAR();

        this.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                synchronized (mHelloAR) {
                    mHelloAR.initGL();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                synchronized (mHelloAR) {
                    mHelloAR.resizeGL(w, h);
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (mHelloAR) {
                    mHelloAR.render();
                }
            }
        });
        this.setZOrderMediaOverlay(true);
    }

    public void setOnTargetStatusChangeListener(OnTargetChangeListener onTargetStatusChangeListener) {
        mHelloAR.setOnTargetChangeListener(onTargetStatusChangeListener);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        synchronized (mHelloAR) {
            if (mHelloAR.initialize(cloud_server_address, cloud_key, cloud_secret)) {
                mHelloAR.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        synchronized (mHelloAR) {
            mHelloAR.stop();
            mHelloAR.dispose();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        Engine.onResume();
    }

    @Override
    public void onPause() {
        Engine.onPause();
        super.onPause();
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            EGLContext context;
            int[] attrib = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib);
            return context;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] attrib = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

            int[] num_config = new int[1];
            egl.eglChooseConfig(display, attrib, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0) {
                throw new IllegalArgumentException("fail to choose EGL configs");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config);

            for (EGLConfig config : configs) {
                int[] val = new int[1];
                int r = 0, g = 0, b = 0, a = 0, d = 0;
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val)) {
                    d = val[0];
                }
                if (d < 16) {
                    continue;
                }

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val)) {
                    r = val[0];
                }
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val)) {
                    g = val[0];
                }
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val)) {
                    b = val[0];
                }
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val)) {
                    a = val[0];
                }
                if (r == 8 && g == 8 && b == 8 && a == 0) {
                    return config;
                }
            }

            return configs[0];
        }
    }
}

