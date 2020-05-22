package com.taehyun.openglvertexattrib;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class CubeRenderer {
    int BYTE_PER_FLOAT = 4;
    int COORDS_PER_VERTEX = 3;
    int vertexStride = BYTE_PER_FLOAT * COORDS_PER_VERTEX;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    int vertexShader, fragmentShader, mProgram;

    int mPositionHandle;
    int mColorHandle;
    int mMVPMatrixHandle;

    public float [] vertex={
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f
    };
    public short[] vIndex={
            0, 1, 3, 3, 1, 2, // Front face.
            0, 1, 4, 4, 5, 1, // Bottom face.
            1, 2, 5, 5, 6, 2, // Right face.
            2, 3, 6, 6, 7, 3, // Top face.
            3, 7, 4, 4, 3, 0, // Left face.
            4, 5, 7, 7, 6, 5, // Rear face.
    };

    public float[] vColor={
            0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f
    };

    public final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "varying vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main(){" +
            "   gl_Position = uMVPMatrix * vPosition;" +
            "   vColor = aColor;" +
            "}";
    public final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main(){" +
            "   gl_FragColor = vColor;" +
            "}";

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader,shaderCode);

        GLES20.glCompileShader(shader);
        return shader;
    }

    public CubeRenderer(){
        ByteBuffer bb = ByteBuffer.allocateDirect(vertex.length * BYTE_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(vColor.length * BYTE_PER_FLOAT);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(vColor);
        colorBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(vIndex.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(vIndex);
        indexBuffer.position(0);

        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }
    public void draw(float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        //triangle vertex 속성을 활성화 시켜야 렌더링시 반영되서 그려짐
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // triangle vertex 속성을 vertexBuffer에 저장되어 있는 vertex 좌표들로 정의한다.
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);


        // program 객체로부터 fragment shader의 vColor 멤버에 대한 핸들을 가져옴
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");

        //triangle 렌더링시 사용할 색으로 color변수에 정의한 값을 사용한다.
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 16, colorBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vIndex.length , GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        //vertex 속성을 비활성화 한다.
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
