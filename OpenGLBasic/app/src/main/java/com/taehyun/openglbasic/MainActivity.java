package com.taehyun.openglbasic;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private GLSurfaceView glView;
    private TriangleRenderer triangleRenderer;

    private float[] mMVPMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float angle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glView = findViewById(R.id.glView);

        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(8,8,8,8,16,0);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        triangleRenderer = new TriangleRenderer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float)width/height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio,ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        angle+=1;
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angle,0,0, 1);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mViewMatrix,0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix,0);

        triangleRenderer.draw(mMVPMatrix);
    }
}
