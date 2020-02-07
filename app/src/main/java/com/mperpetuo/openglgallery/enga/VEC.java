package com.mperpetuo.openglgallery.enga;

 // used by physics3d, doubles as a vector and a quaternion
public class VEC {
     public float x;
     public float y;
     public float z;
     public float w;

     public VEC() {
     }

     public VEC(VEC a) {
         x = a.x;
         y = a.y;
         z = a.z;
         w = a.w;
     }

     public VEC(float a, float b, float c) {
         x = a;
         y = b;
         z = c;
     }

     public VEC(float a,float b,float c,float d) {
         x = a;
         y = b;
         z = c;
         w = d;
     }

     // convert 1 float[3] to a VEC
     public VEC(float[] av) {
         x = av[0];
         y = av[1];
         z = av[2];
         w = 0;
     }

     // convert 1 float[3] to a VEC with offset
     public VEC(float[] av,int vertOffset) {
         int vo3 = 3*vertOffset;
         x = av[vo3];
         y = av[vo3 + 1];
         z = av[vo3 + 2];
         w = 0;
     }

     // update this with arg passed in
     public void copy(VEC v) {
         x = v.x;
         y = v.y;
         z = v.z;
         w = v.w;
     }

     public void copy(float xa, float ya, float za) {
         x = xa;
         y = ya;
         z = za;
         w = 0;
     }

     public void copy(float xa, float ya, float za, float zw) {
         x = xa;
         y = ya;
         z = za;
         w = zw;
     }

     public void clear() {
         x = 0;
         y = 0;
         z = 0;
         w = 0;
     }

     public boolean equals(VEC rhs) {
         return x == rhs.x && y == rhs.y && z == rhs.z && w == rhs.w;
     }

     // copy VEC into float[3] array
     static public void copy3(VEC in, float[] out) {
         out[0] = in.x;
         out[1] = in.y;
         out[2] = in.z;
     }

     // copy VEC into float[4] array
     static public void copy4(VEC in, float[] out) {
         out[0] = in.x;
         out[1] = in.y;
         out[2] = in.z;
         out[3] = in.w;
     }

     // convert array of floats with stride of 3 to an array of VEC's
     static public VEC[] makeVECArray(float[] pnts) {
         if (pnts.length % 3 != 0) {
             Utils.alert("makeVECArray not multiple of 3 it's " + pnts.length);
         }
         VEC[] ret = new VEC[pnts.length/3];
         for (int i=0;i<ret.length;++i)
             ret[i] = new VEC(pnts,i);
         return ret;
     }

     // convert array of VEC's to an array of floats with stride of 3
     static public float[] makeFLOATArray(VEC[] pnts) {
         float[] ret = new float[pnts.length*3];
         for (int i=0;i<pnts.length;++i) {
             int j = i*3;
             ret[j] = pnts[i].x;
             ret[j + 1] = pnts[i].y;
             ret[j + 2] = pnts[i].z;
         }
         return ret;
     }

     @Override
     public String toString() {
         String xs = String.format("%.5f",x);
         String ys = String.format("%.5f",y);
         String zs = String.format("%.5f",z);
         return "(" + xs + "," + ys + "," + zs + ")";
     }

 }
