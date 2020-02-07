package com.mperpetuo.openglgallery.enga;

/**
 * Created by cyberrickers on 11/12/2016.
 */

public class VECNuMath {

    public static float length2(VEC v) {
        return v.x*v.x + v.y*v.y + v.z*v.z;
    }

    public static float dist3dsq(VEC a,VEC b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float dz = a.z - b.z;
        return dx*dx + dy*dy + dz*dz;
    }

    public static float normalize(VEC vin,VEC vout) {
        float len2 = length2(vin);
        if (len2 < NuMath.EPSILON*NuMath.EPSILON) {
            vout.x = 0.0f;
            vout.y = 1.0f; // return something
            vout.z = 0.0f;
            return 0;
        }
        float len = (float)Math.sqrt(len2);
        float ilen = 1.0f/len;
        vout.x = ilen * vin.x;
        vout.y = ilen * vin.y;
        vout.z = ilen * vin.z;
        return len;
    }

    public static float normalize(VEC v) {
        return normalize(v,v);
    }

    public static float dot3d(VEC a,VEC b) {
        return a.x*b.x + a.y*b.y + a.z*b.z;
    }

    public static void cross3d(VEC a, VEC b, VEC c) {
        float z = a.x * b.y - a.y * b.x;
        float x = a.y * b.z - a.z * b.y;
        float y = a.z * b.x - a.x * b.z;
        c.copy(x,y,z);
    }

}
