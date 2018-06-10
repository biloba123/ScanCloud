//================================================================================================================================
//
//  Copyright (c) 2015-2018 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.lvqingyang.scancloud.easy_ar;

import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import cn.easyar.Vec4I;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindRenderbuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFramebufferRenderbuffer;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGenFramebuffers;
import static android.opengl.GLES20.glGenRenderbuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glRenderbufferStorage;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class RecorderRenderer
{
    private float[] flatten(float[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        float[] l = new float[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private short[] flatten(short[][] a)
    {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        short[] l = new short[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private int generateOneBuffer()
    {
        int[] buffer = {0};
        glGenBuffers(1, buffer, 0);
        return buffer[0];
    }

    private int width = -1;
    private int height = -1;

    //stage states
    private int fboPost_ = -1;
    private int texturePost_ = -1;
    private int depthPost_ = -1;
    private int stencilPost_ = -1;

    //draw back buffer states
    private int program_bk = -1;
    private int vertex_vbo_bk = -1;
    private int uv_vbo_bk = -1;
    private int index_vbo_bk = -1;
    private int aVertex = -1;
    private int aUV = -1;
    private int uTexture = -1;

    private void getError(String name)
    {
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            Log.e("HelloAR", name + " " + Integer.toString(error));
        }
    }

    public void resize(int width, int height)
    {
        if (texturePost_ != -1) {
            //delete Post_ GL Resources
            glDeleteTextures(1, new int[]{ texturePost_ }, 0);
            texturePost_ = -1;
            glDeleteRenderbuffers(1, new int[]{ depthPost_ }, 0);
            depthPost_ = -1;
            glDeleteFramebuffers(1, new int[]{ fboPost_ }, 0);
            fboPost_ = -1;
            getError("glDeleteFramebuffers");
        }

        //re-create
        int[] buffer = {0};
        glGenFramebuffers(1, buffer, 0);
        fboPost_ = buffer[0];
        glActiveTexture(GL_TEXTURE0);
        glGenTextures(1, buffer, 0);
        texturePost_ = buffer[0];
        glBindTexture(GL_TEXTURE_2D, texturePost_);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        getError("glTexImage2D");

        glBindFramebuffer(GL_FRAMEBUFFER, fboPost_);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texturePost_, 0);
        getError("GL_COLOR_ATTACHMENT0");

        glGenRenderbuffers(1, buffer, 0);
        depthPost_ = buffer[0];
        glBindRenderbuffer(GL_RENDERBUFFER, depthPost_);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthPost_);
        getError("GL_DEPTH_COMPONENT16");

        this.width = width;
        this.height = height;
    }

    public void preRender()
    {
        if (width == -1 || height == -1) {
            return;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, fboPost_);
    }

    public void postRender(Vec4I viewport)
    {
        getError("glBindFramebuffer before");

        //draw to back buffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        getError("glBindFramebuffer");

        //use program_bk as flags
        if (program_bk == -1) {
            String vert_source = "#version 100\n"
                    + "attribute vec4 coord;\n"
                    + "attribute vec2 texCoord;\n"
                    + "varying vec2 texc; \n"
                    + "void main(void)\n"
                    + "{\n"
                    + "    gl_Position = coord;\n"
                    + "    texc = texCoord; \n"
                    + "}\n";
            String frag_source = "#version 100\n"
                    + "precision mediump float;\n"
                    + "varying vec2 texc;\n"
                    + "uniform sampler2D texture;\n"
                    + "void main(void)\n"
                    + "{\n"
                    + "    gl_FragColor = texture2D(texture, texc);\n"
                    + "}\n";

            int vertShader = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertShader, vert_source);
            getError("glShaderSource");

            glCompileShader(vertShader);
            getError("vertShader");

            int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragShader, frag_source);
            glCompileShader(fragShader);

            getError("fragShader");

            program_bk = glCreateProgram();
            glAttachShader(program_bk, vertShader);
            glAttachShader(program_bk, fragShader);
            glLinkProgram(program_bk);
            getError("glLinkProgram");

            glUseProgram(program_bk);
            getError("glUseProgram");
            glDeleteShader(fragShader);
            glDeleteShader(vertShader);

            aVertex = glGetAttribLocation(program_bk, "coord");
            aUV = glGetAttribLocation(program_bk, "texCoord");
            uTexture = glGetUniformLocation(program_bk, "texture");

            vertex_vbo_bk = generateOneBuffer();
            glBindBuffer(GL_ARRAY_BUFFER, vertex_vbo_bk);
            float vertices[][] = {{-1.0f, -1.0f, -1.0f},{1.0f, -1.0f, -1.0f,},{-1.0f, 1.0f, -1.0f},{1.0f, 1.0f, -1.0f}};
            FloatBuffer vertices_buffer = FloatBuffer.wrap(flatten(vertices));
            glBufferData(GL_ARRAY_BUFFER, vertices_buffer.limit() * 4, vertices_buffer, GL_STATIC_DRAW);
            getError("vertex_vbo_bk");

            uv_vbo_bk = generateOneBuffer();
            glBindBuffer(GL_ARRAY_BUFFER, uv_vbo_bk);
            float uvs[][] = {{0.0f, 0.0f},{1.0f, 0.0f},{0.0f, 1.0f},{1.0f, 1.0f}};
            FloatBuffer uvs_buffer = FloatBuffer.wrap(flatten(uvs));
            glBufferData(GL_ARRAY_BUFFER, uvs_buffer.limit() * 4, uvs_buffer, GL_STATIC_DRAW);
            getError("uv_vbo_bk");

            index_vbo_bk = generateOneBuffer();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, index_vbo_bk);
            short indices[][] = {{0, 1, 2},{1, 3, 2}};
            ShortBuffer indices_buffer = ShortBuffer.wrap(flatten(indices));
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices_buffer.limit() * 2, indices_buffer, GL_STATIC_DRAW);
            getError("index_vbo_bk");
        }

        glClearColor(1.f, 0.f, 0.f, 1.f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);

        glUseProgram(program_bk);
        glBindBuffer(GL_ARRAY_BUFFER, vertex_vbo_bk);
        glEnableVertexAttribArray(aVertex);
        glVertexAttribPointer(aVertex, 3, GL_FLOAT, false, 0, 0);
        getError("aVertex");

        glBindBuffer(GL_ARRAY_BUFFER, uv_vbo_bk);
        glEnableVertexAttribArray(aUV);
        glVertexAttribPointer(aUV, 2, GL_FLOAT, false, 0, 0);
        getError("uv_vbo_bk");

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texturePost_);
        getError("texturePost_");

        glUniform1i(uTexture, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, index_vbo_bk);
        getError("GL_ELEMENT_ARRAY_BUFFER");

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
        getError("glDrawElements");
    }

    public int getTextureId()
    {
        return texturePost_;
    }
}
