package com.taehyun.openglbasic;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

public class TriangleRenderer {
    private FloatBuffer vertexBuffer;
    private int COORDS_PER_VERTEX = 3;
    private int BYTE_PER_FLOAT = 4;
    private float[] vertex={
            0.0f,  0.622008459f, 0.0f, // 상단 vertex
            -0.5f, -0.311004243f, 0.0f, // 왼쪽 아래 vertex
            0.5f, -0.311004243f, 0.0f  // 오른쪽 아래 vertex
    };

    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    int vertexShader, fragmentShader, mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private final int vertexCount = vertex.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex



    private String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
                    "void main(){"+
                    "   gl_Position = uMVPMatrix * vPosition;"+
                    "}";
    private String fragmentShaderCode =
            "precision mediump float;"+
            "uniform vec4 vColor;"+
                    "void main(){"+
                    "   gl_FragColor=vColor;"+
                    "}";

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader,shaderCode);

        GLES20.glCompileShader(shader);
        return shader;
    }
    public TriangleRenderer(){
        ByteBuffer bb= ByteBuffer.allocateDirect(vertex.length*BYTE_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);

        vertexShader=loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

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
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        //triangle 렌더링시 사용할 색으로 color변수에 정의한 값을 사용한다.
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix,0);

        //vertex 갯수만큼 tiangle을 렌더링한다.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        //vertex 속성을 비활성화 한다.
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
