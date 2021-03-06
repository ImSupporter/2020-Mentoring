package com.taehyun.pointcloud.Utils;

public class VectorCal {

    static public float vectorSize(float[] a){
        return (float)Math.sqrt(a[0]*a[0] + a[1]*a[1] + a[2]*a[2]);
    }

    static public float inner(float[] a, float[] b){
        return (a[0]*b[0]) + (a[1]*b[1]) + (a[2]*b[2]);
    }
    static public float inner(float[] a, float[] b,int i){
        return (a[0]*b[0+i]) + (a[1]*b[1+i]) + (a[2]*b[2+i]);
    }

    static public float[] outer(float[] a, float[] b){
        float[] result = new float[3];
        result[0] = a[1]*b[2] - a[2]*b[1];
        result[1] = a[2]*b[0] - a[0]*b[2];
        result[2] = a[0]*b[1] - a[1]*b[0];

        return result;
    }

    static public void normalize(float[] a){
        a[0] /= VectorCal.vectorSize(a);
        a[1] /= VectorCal.vectorSize(a);
        a[2] /= VectorCal.vectorSize(a);
    }

    static public float[] IDP(float[] a, float[] b, int m, int n){  // 내분점 : internally dividing point
        float[] idp = new float[]{0,0,0};
        int mn = m+n;

        idp[0] = (m*b[0] + n*a[0]) / mn;
        idp[1] = (m*b[1] + n*a[1]) / mn;
        idp[2] = (m*b[2] + n*a[2]) / mn;

        return idp;
    }
}
