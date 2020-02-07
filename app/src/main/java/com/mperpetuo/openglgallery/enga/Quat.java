package com.mperpetuo.openglgallery.enga;

public class Quat {
    static final float[] tqmul = new float[4]; // temp quat, synchronized
    static final float[] tqi = new float[4]; // temp quat inverse
    static final float[] tqinterp = new float[4]; // temp quat interp
    static final float[] tv0 = new float[4]; // temp vec3
    static final float[] tv1 = new float[4]; // temp vec3

    public static float[] create() {
        return new float[] {0,0,0,1};
    }

    public static void mul(float out[],float a[],float b[]) { // out = a * b
        synchronized (tqmul) {
            tqmul[0] = a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0];
            tqmul[1] = -a[0] * b[2] + a[1] * b[3] + a[2] * b[0] + a[3] * b[1];
            tqmul[2] = a[0] * b[1] - a[1] * b[0] + a[2] * b[3] + a[3] * b[2];
            tqmul[3] = -a[0] * b[0] - a[1] * b[1] - a[2] * b[2] + a[3] * b[3];
            System.arraycopy(tqmul, 0, out, 0, 4);
        }
    }

    // make a quat a unit quat
    public static void normalize(float out[]) { // make quat a into unit quat b
        float r = out[0]*out[0] + out[1]*out[1] + out[2]*out[2] + out[3]*out[3];
        if (r<NuMath.EPSILON*NuMath.EPSILON) {
            // too close to 0, use identity quat
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
            out[3] = 1;
        } else {
            r=1/(float)Math.sqrt(r);
            out[0] *= r;
            out[1] *= r;
            out[2] *= r;
            out[3] *= r;
        }
    }

    // invert a unit quat
    public static void conjugate(float[] qout,float[] qin) {
        qout[0] = -qin[0];
        qout[1] = -qin[1];
        qout[2] = -qin[2];
        qout[3] = qin[3];
    }

    public static void quatRot(float[] outVec,float[] inVec, float[] q) {
        synchronized (tqmul) {
            tv0[0] = inVec[0];
            tv0[1] = inVec[1];
            tv0[2] = inVec[2];
            tv0[3] = 0;
            conjugate(tqi,q);
            mul(tv1, q, tv0);
            mul(tv1,tv1, tqi);
            outVec[0] = tv1[0];
            outVec[1] = tv1[1];
            outVec[2] = tv1[2];
        }
    }

    // convert a quaternion to a 4 by 4 matrix
    public static void matFromQuat(float[] out, float[] q) {
        float x = q[0];
        float y = q[1];
        float z = q[2];
        float w = q[3];
        float x2 = 2*x;
        float y2 = 2*y;
        float z2 = 2*z;

        float xx = x * x2;
        float xy = x * y2;
        float xz = x * z2;
        float yy = y * y2;
        float yz = y * z2;
        float zz = z * z2;
        float wx = w * x2;
        float wy = w * y2;
        float wz = w * z2;

        out[0] = 1 - (yy + zz);
        out[1] = xy + wz;
        out[2] = xz - wy;
        out[3] = 0;

        out[4] = xy - wz;
        out[5] = 1 - (xx + zz);
        out[6] = yz + wx;
        out[7] = 0;

        out[8] = xz + wy;
        out[9] = yz - wx;
        out[10] = 1 - (xx + yy);
        out[11] = 0;

        out[12] = 0;
        out[13] = 0;
        out[14] = 0;
        out[15] = 1;
    }

    // calc a quat from normalized rot axis
    public static void rotAxisToQuat(float[] out,float[] axis,float ang) {
        ang = ang * 0.5f;
        float s = (float)Math.sin(ang);
        out[0] = s * axis[0];
        out[1] = s * axis[1];
        out[2] = s * axis[2];
        out[3] = (float)Math.cos(ang);
    }

    // returns angle
    public static float quatToRotAxis(float[] axis,float[] q) {
        float ang;
        float w2 = q[3]*q[3];
        if (w2 > 1)
            w2 = 1;
        float sina = (float)Math.sqrt(1-w2);
        if (sina > NuMath.EPSILON) {
            ang = (float)(Math.acos(q[3])*2.0f); // check
            if (ang < 0)
                sina =- sina;
            sina = 1/sina;
            axis[0] = q[0]*sina;
            axis[1] = q[1]*sina;
            axis[2] = q[2]*sina;
            if (ang > NuMath.PI)
                ang -= 2.0f*NuMath.PI;
            if (ang < 0) {
                axis[0] = -axis[0];
                axis[1] = -axis[1];
                axis[2] = -axis[2];
                ang = -ang;
            }
        } else {
            axis[0]=0;
            axis[1]=1;
            axis[2]=0;
            ang = 0;
        }
        return ang;
    }
/*
	var iab = quat.create();
	quat.invert(iab,a);
	quat.mul(iab,iab,b);
	var ra = vec4.create();
	quat.getAxisAngle(ra,iab);
	ra[3] *= t;
	quat.setAxisAngle(iab,ra,ra[3]);
	quat.mul(c,a,iab);
	quat.normalize(c,c);

 */
    public static void interp(float[] out,float[] a,float[] b, float t) {
        synchronized (tqmul) {
            /*tqinterp[0] = a[0];
            tqinterp[1] = a[1];
            tqinterp[2] = a[2];
            tqinterp[3] = a[3];*/
            conjugate(tqinterp,a);
            //quatinverse(a, tqi2);
            mul(tqinterp, tqinterp,b);
            //quattimes(tqi2, tqi2, b);
            float ang = quatToRotAxis(tv0, tqinterp);
            //quat2rotaxis(tqi2, tra);
            ang *= t;
            rotAxisToQuat(tqinterp, tv0, ang);
            mul(out, a, tqinterp);
            normalize(out);
        }
    }

    static final float vectornorm[] = new float[3]; // temp
    static final float vectorcross[] = new float[3];
    static final float vectormasterdir[] = {0,1,0};
    static final float ra[] = new float[4];
    // return a quat for rotating a vector pointing up i.e. (0,1,0) to a vector pointing in dir (not need to be normalized)
    public static float dir2quat(float[] out,float[] dir) { // 4 3
        //var out = quat.create();
        float len;
        synchronized (tqmul) {
            vectornorm[0] = dir[0];
            vectornorm[1] = dir[1];
            vectornorm[2] = dir[2];
            len = NuMath.normalize(vectornorm);
            NuMath.cross(vectorcross,vectormasterdir,vectornorm);
            float len2 = NuMath.length2(vectorcross);
            if (len2 < NuMath.EPSILON * NuMath.EPSILON) {
                if (dir[1] >= 0) {
                    out[0] = 0;
                    out[1] = 0;
                    out[2] = 0;
                    out[3] = 1;
                    //quatset(0, 0, 0, 1, out);
                } else {
                    out[0] = 0;
                    out[1] = 0;
                    out[2] = 1;
                    out[3] = 0;
                    //quatset(1, 0, 0, 0, out); // some quat at 180
                }
            } else {
                NuMath.normalize(vectorcross);
                float d = NuMath.dot(vectormasterdir, vectornorm);
                float ang = (float) Math.acos(d);
                //quatset(vectorcross[0], vectorcross[1], vectorcross[2], ang, ra);
                //rotaxis2quat(ra, out);
                //quat.setAxisAngle(out,vectorcross,ang);
                rotAxisToQuat(out,vectorcross,ang);
            }
            //return out;
        }
        return len;
    }

    public static float dot(float[] a, float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2] + a[3]*b[3];
    }

    public static void negate(float[] out, float[] in) {
        out[0] = -in[0];
        out[1] = -in[1];
        out[2] = -in[2];
        out[3] = -in[3];
    }

}
