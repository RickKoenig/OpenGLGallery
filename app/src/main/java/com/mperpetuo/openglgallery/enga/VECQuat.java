package com.mperpetuo.openglgallery.enga;

/**
 * Created by cyberrickers on 11/12/2016.
 */

public class VECQuat {
    //static VEC t = new VEC();

    // order is reversed
    public static void rotaxis2quat(VEC raIn,VEC qOut) {
        float a;
        float sina,cosa;
// convert to unit quat
        if (raIn.w > 10000 || raIn.w < -10000)
            Utils.alert("big angle in rotaxis2quat " + raIn.w);
        while(raIn.w <= NuMath.PI) // should be -PI
            raIn.w += NuMath.TWOPI;
        while (raIn.w > NuMath.PI)
            raIn.w -= NuMath.TWOPI;
        a = .5f*raIn.w;
        sina = (float)Math.sin(a);
        cosa = (float)Math.cos(a);
        qOut.x = raIn.x*sina;
        qOut.y = raIn.y*sina;
        qOut.z = raIn.z*sina;
        qOut.w = cosa;
    }

    public static void quatinverse(VEC in, VEC out) { // unit quats
        out.x = -in.x;
        out.y = -in.y;
        out.z = -in.z;
        out.w = in.w;
    }

    // c = a * b
    public static void quattimes(VEC a,VEC b,VEC c) {
        float x =  a.x*b.w + a.y*b.z - a.z*b.y + a.w*b.x;
        float y = -a.x*b.z + a.y*b.w + a.z*b.x + a.w*b.y;
        float z =  a.x*b.y - a.y*b.x + a.z*b.w + a.w*b.z;
        float w = -a.x*b.x - a.y*b.y - a.z*b.z + a.w*b.w;
        c.copy(x,y,z,w);
    }

    public static void quatrot(VEC q,VEC vi,VEC vo) {
        VEC qi = new VEC();
        VEC vi2 = new VEC(vi);
        vi2.w=0;
        quatinverse(q,qi);
        quattimes(q,vi2,vo);
        quattimes(vo,qi,vo);
    }

    public static void quatrots(VEC q,VEC[] vi,VEC[] vo,int npnts) {
        int i;
        for (i=0;i<npnts;i++)
            quatrot(q,vi[i],vo[i]);
    }

    public static void quatnormalize(VEC a,VEC b) { // make quat a into unit quat b
        float r = a.x*a.x + a.y*a.y + a.z*a.z + a.w*a.w;
        if (r < NuMath.EPSILON) {
            b.clear();
            b.w = 1;
        } else {
            r = 1/(float)Math.sqrt(r);
            b.x = r*a.x;
            b.y = r*a.y;
            b.z = r*a.z;
            b.w = r*a.w;
        }
    }

}
