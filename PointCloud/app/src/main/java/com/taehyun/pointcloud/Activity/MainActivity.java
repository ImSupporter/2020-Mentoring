package com.taehyun.pointcloud.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.taehyun.pointcloud.R;
import com.taehyun.pointcloud.Renderer.BackgroundRenderer;
import com.taehyun.pointcloud.Renderer.PointCloudRenderer;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer{

    private boolean mUserRequestedInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

    //카메라 권한
    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA};
    private final int PERMISSION_REQUEST_CODE = 0; // PROTECTION_NORMAL

    private GLSurfaceView glView;
    BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private Session session;
    private Frame frame;

    private Button btn_record;
    private boolean recording = false;
    private int renderingMode = 0;  // 0:start, 1:recording, 2:recorded

    private float[] viewMatrix = new float[16];
    private float[] projMatrix = new float[16];
    private float[] vpMatrix = new float[16];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glView = findViewById(R.id.glView);
        glView.setPreserveEGLContextOnPause(true);
        glView.setEGLContextClientVersion(2);
        glView.setEGLConfigChooser(8,8,8,8,16,0);
        glView.setRenderer(this);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        btn_record = findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = !recording;
                if(recording) {
                    renderingMode = 1;
                    Toast.makeText(getApplicationContext(), "start recording", Toast.LENGTH_SHORT).show();
                    btn_record.setForeground(getApplicationContext().getDrawable(R.drawable.ic_recstop));
                }
                else {
                    if(renderingMode == 1) pointCloudRenderer.filterPoints();
                    renderingMode = 2;
                    Toast.makeText(getApplicationContext(), "stop recording", Toast.LENGTH_SHORT).show();
                    btn_record.setForeground(getApplicationContext().getDrawable(R.drawable.ic_recbutton));
                }
            }
        });

        for(String permission : REQUIRED_PERMISSSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null){
            glView.onPause();
            session.pause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(session == null){
            try{
                switch(ArCoreApk.getInstance().requestInstall(this,!mUserRequestedInstall)){
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = true;
                        return;
                    case INSTALLED:
                        break;
                }
                session = new Session(this);

                Config config = new Config(session);
                config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
                config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                config.setFocusMode(Config.FocusMode.AUTO);
                session.configure(config);

            }catch (Exception e){
                Log.d("ULTRA", e.getMessage());
                return;
            }
        }

        try{
            session.resume();
        }catch (CameraNotAvailableException e){
            e.printStackTrace();
            session = null;
            finish();
        }

        glView.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        try{
            backgroundRenderer.createOnGlThread(this);
            pointCloudRenderer.createOnGlThread(this);
        }catch (IOException e){
            e.getMessage();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        if(session == null){
            return;
        }
        if(mViewportChanged){
            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
        }
        try{
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            frame = session.update();
            Camera camera = frame.getCamera();
            backgroundRenderer.draw(frame);

            if(camera.getTrackingState() == TrackingState.TRACKING){
                camera.getViewMatrix(viewMatrix, 0);
                camera.getProjectionMatrix(projMatrix, 0, 0.1f,100.0f);

                pointCloudRenderer.update(frame.acquirePointCloud(), recording);
                Log.d("RMode", String.format("%b %d", recording, renderingMode));
                switch (renderingMode){
                    case 0:
                        pointCloudRenderer.draw(viewMatrix, projMatrix);
                        break;
                    case 1:
                        pointCloudRenderer.draw_conf(viewMatrix, projMatrix);
                        break;
                    case 2:
                        pointCloudRenderer.draw_final(viewMatrix, projMatrix);
                        Log.d("numPoints", String.valueOf(pointCloudRenderer.finalPointBuffer.remaining()));
                        break;
                }


            }

        }catch (CameraNotAvailableException e){
            finish();
        }
    }
}
