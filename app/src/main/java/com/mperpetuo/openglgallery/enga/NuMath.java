package com.mperpetuo.openglgallery.enga;

import android.opengl.Matrix;

/**
 * Created by cyberrickers on 3/20/2016.
 */
public class NuMath {

    public static final float PI = (float)Math.PI;
    public static final float TWOPI = 2.0f*PI;
    public static final float PIOVER2 = .5f*PI;
    public static final float E = (float)Math.E;
    public static final float SQRT2 = (float)Math.sqrt(2.0);
    public static final float SQRT2O2 = (float)Math.sqrt(2.0)/2.0f;
    public static final float SQRT3 = (float)Math.sqrt(3.0);
    public static final float SQRT3O3 = (float)Math.sqrt(3.0)/3.0f;
    public static final float SQRT5 = (float)Math.sqrt(5.0);
    public static final float SQRT5O5 = (float)Math.sqrt(5.0)/5.0f;


    public static final float DEGREE2RAD = TWOPI/360.0f;
    public static final float RAD2DEGREE = 360.0f/TWOPI;

    public static final float EPSILON = 1e-20f;
    //public static final float EPSILON = 0.000001f;

    static final float[] mTempMat0 = new float[16];

// most matrices are assumed to have last row of 0 0 0 1
// except invert, make ortho, make persp, and mul

    // yes returns [-PI to PI)
// no  returns 0<=ang<2*PI eventually
    public static float normalangrad(float rad) {
        if ((rad > 1000000.0f) || (rad < -1000000.0))
            throw new RuntimeException("normalangrad getting too big! " + rad);
        int watch = 0;
        while (rad < -NuMath.PI) {
            rad += 2*NuMath.PI;
            ++watch;
            if (watch > 1000) {
                throw new RuntimeException("normalangrad too many while loops 1");
                //return rad;
            }
        }
        while (rad >= NuMath.PI) {
            rad -= 2*NuMath.PI;
            ++watch;
            if (watch > 1000) {
                throw new RuntimeException("normalangrad too many while loops 2");
                //return rad;
            }
        }
        return rad;
    }

    public static void mul(float[] out, float[] lhs, float[] rhs) {
        // use Android call but with guaranteed overlap support
        boolean method1 = false;
        boolean method2 = true;
        boolean method3 = false;
        if (method1) { // use synchronized on static member
            synchronized (mTempMat0) {
                Matrix.multiplyMM(mTempMat0, 0, lhs, 0, rhs, 0);
                System.arraycopy(mTempMat0, 0, out, 0, 16);
            }
        }
        if (method2) { // use local temp variable
            float[] tempMat0 = new float[16];
            //synchronized (tempMat0) {
                Matrix.multiplyMM(tempMat0, 0, lhs, 0, rhs, 0);
                System.arraycopy(tempMat0, 0, out, 0, 16);
            //}
        }
        if (method3) { // ignore overlap
            //synchronized (mTempMat0) {
                Matrix.multiplyMM(out, 0, lhs, 0, rhs, 0);
                //System.arraycopy(mTempMat0, 0, out, 0, 16);
            //}
        }
    }

    public static float determinant(float[] m) {
        float a00 = m[0];
        float a01 = m[1];
        float a02 = m[2];
        float a10 = m[4];
        float a11 = m[5];
        float a12 = m[6];
        float a20 = m[8];
        float a21 = m[9];
        float a22 = m[10];
        return    a00*a11*a22
                - a00*a12*a21
                - a01*a10*a22
                + a01*a12*a20
                + a02*a10*a21
                - a02*a11*a20;

    }

    public static float dot(float[] a, float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }

    public static void cross(float[] out,float[] a,float[] b) {
        out[0] = a[1]*b[2] - a[2]*b[1];
        out[1] = a[2]*b[0] - a[0]*b[2];
        out[2] = a[0]*b[1] - a[1]*b[0];
    }

    public static float length(float[] v) {
        return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }

    public static float length2(float[] v) {
        return v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
    }

    // returns original length
    public static float normalize(float[] v) {
        float len2 = length2(v);
        if (len2 < EPSILON*EPSILON) {
            v[0] = 0.0f;
            v[1] = 1.0f; // return something
            v[2] = 0.0f;
        }
        float len = (float)Math.sqrt(len2);
        float ilen = 1.0f/(float)Math.sqrt(len2);
        v[0] *= ilen;
        v[1] *= ilen;
        v[2] *= ilen;
        return len;
    }

    // do various operations in place on the RHS like matinout = matinout * operation

    // in place, translates on RHS (math)
    public static void translate(float[] m, float[] trans) {
        float tx = trans[0];
        float ty = trans[1];
        float tz = trans[2];
        m[12] += m[0]*tx + m[4]*ty + m[8]*tz;
        m[13] += m[1]*tx + m[5]*ty + m[9]*tz;
        m[14] += m[2]*tx + m[6]*ty + m[10]*tz;
        //m[15] += m[3]*trans[0] + m[7]*trans[1] + m[11]*trans[2];
    }

    // in place, translates on RHS (math)
    public static void scale(float[] m, float[] scale) {
        float sx = scale[0];
        float sy = scale[1];
        float sz = scale[2];
        m[0] *= sx;
        m[1] *= sx;
        m[2] *= sx;

        m[4] *= sy;
        m[5] *= sy;
        m[6] *= sy;

        m[8] *= sz;
        m[9] *= sz;
        m[10] *= sz;
    }

    // rotation matrices multiplied on the RHS
    public static void rotateX(float[] out, float rad) {
        float s = (float)Math.sin(rad);
        float c = (float)Math.cos(rad);
        float a10 = out[4];
        float a11 = out[5];
        float a12 = out[6];
        //float a13 = out[7];
        float a20 = out[8];
        float a21 = out[9];
        float a22 = out[10];
        //float a23 = out[11];
        // Perform axis-specific matrix multiplication
        out[4] = a10 * c + a20 * s;
        out[5] = a11 * c + a21 * s;
        out[6] = a12 * c + a22 * s;
        //out[7] = a13 * c + a23 * s;
        out[8] = a20 * c - a10 * s;
        out[9] = a21 * c - a11 * s;
        out[10] = a22 * c - a12 * s;
        //out[11] = a23 * c - a13 * s;
    }

    public static void rotateY(float[] out, float rad) {
        float s = (float)Math.sin(rad);
        float c = (float)Math.cos(rad);
        float a00 = out[0];
        float a01 = out[1];
        float a02 = out[2];
        //float a03 = out[3];
        float a20 = out[8];
        float a21 = out[9];
        float a22 = out[10];
        //float a23 = out[11];
        out[0] = a00 * c - a20 * s;
        out[1] = a01 * c - a21 * s;
        out[2] = a02 * c - a22 * s;
        //out[3] = a03 * c - a23 * s;
        out[8] = a00 * s + a20 * c;
        out[9] = a01 * s + a21 * c;
        out[10] = a02 * s + a22 * c;
        //out[11] = a03 * s + a23 * c;
    }

    public static void rotateZ(float[] out, float rad) {
        float s = (float)Math.sin(rad);
        float c = (float)Math.cos(rad);
        float a00 = out[0];
        float a01 = out[1];
        float a02 = out[2];
        //float a03 = out[3];
        float a10 = out[4];
        float a11 = out[5];
        float a12 = out[6];
        //float a13 = out[7];
        out[0] = a00 * c + a10 * s;
        out[1] = a01 * c + a11 * s;
        out[2] = a02 * c + a12 * s;
        //out[3] = a03 * c + a13 * s;
        out[4] = a10 * c - a00 * s;
        out[5] = a11 * c - a01 * s;
        out[6] = a12 * c - a02 * s;
        //out[7] = a13 * c - a03 * s;
    }

    /**
     * Converts Euler angles to a rotation matrix.
     * <p/>
     * fixed by Nicolas My, thanks.
     * The one in opengl.Matrix is buggy.
     *
     * /@param rm       returns the result
     * /@param rmOffset index into rm where the result matrix starts
     * /@param x        angle of rotation, in degrees
     * /@param y        angle of rotation, in degrees
     * /@param z        angle of rotation, in degrees
     */
    // still no good order is wrong, should be roll,pitch,yaw like O = Y*P*R*I code RPY, right to left
    /*
    public static void setRotateEulerM(float[] rm, int rmOffset,
                                       float x, float y, float z) {
        x *= (float) (Math.PI / 180.0f);
        y *= (float) (Math.PI / 180.0f);
        z *= (float) (Math.PI / 180.0f);
        float cx = (float) Math.cos(x);
        float sx = (float) Math.sin(x);
        float cy = (float) Math.cos(y);
        float sy = (float) Math.sin(y);
        float cz = (float) Math.cos(z);
        float sz = (float) Math.sin(z);
        float cxsy = cx * sy;
        float sxsy = sx * sy;

        rm[rmOffset    ] = cy * cz;
        rm[rmOffset + 1] = -cy * sz;
        rm[rmOffset + 2] = sy;
        rm[rmOffset + 3] = 0.0f;

        rm[rmOffset + 4] = sxsy * cz + cx * sz;
        rm[rmOffset + 5] = -sxsy * sz + cx * cz;
        rm[rmOffset + 6] = -sx * cy;
        rm[rmOffset + 7] = 0.0f;

        rm[rmOffset + 8] = -cxsy * cz + sx * sz;
        rm[rmOffset + 9] = cxsy * sz + sx * cz;
        rm[rmOffset + 10] = cx * cy;
        rm[rmOffset + 11] = 0.0f;

        rm[rmOffset + 12] = 0.0f;
        rm[rmOffset + 13] = 0.0f;
        rm[rmOffset + 14] = 0.0f;
        rm[rmOffset + 15] = 1.0f;
    }
*/
    public static void rotateEuler(float[] m, float[] ypr) {
        rotateY(m, ypr[1]);
        rotateX(m,ypr[0]);
        rotateZ(m, ypr[2]);
    }

    public static void rotateEulerInv(float[] m, float[] ypr) {
        rotateZ(m, -ypr[2]);
        rotateX(m,-ypr[0]);
        rotateY(m, -ypr[1]);
    }

    // return length of input vector and Euler angles for rotating a vector pointing up i.e. (0,1,0) to a vector pointing in dir (not need to be normalized)
    public static float dir2rot(float[] out,float dir[]) { // 3 3
        //var ret = vec3.create();
        float len = NuMath.length(dir);
        float lenxz = (float)Math.sqrt(dir[0]*dir[0] + dir[2]*dir[2]);
        if (lenxz < NuMath.EPSILON*len) {
            if (dir[1] >= 0) {
                out[0] = 0;
                out[1] = 0;
                out[2] = 0;
            } else {
                out[0] = NuMath.PI;
                out[1] = 0;
                out[2] = 0;
            }
        } else {
            out[0] = (float)Math.atan2(lenxz,dir[1]);
            out[1] = (float)Math.atan2(dir[0],dir[2]);
        }
        return len;
    }

/**
 * Generates a look-at matrix with the given eye position, focal point, and up axis
 *
 * @param {mat4} out mat4 frustum matrix will be written into
 * @param {vec3} eye Position of the viewer
 * @param {vec3} center Point the viewer is looking at
 * @param {vec3} up vec3 pointing up
 * @returns {mat4} out
 */
    public static void lookAtlhc(float[] out,float[] eye,float[] center,float[] up) {
        boolean test = false;
        if (test) {
            Matrix.setIdentityM(out,0);
            out[14] = 20;
            return;
        }
        float x0, x1, x2, y0, y1, y2, z0, z1, z2, len,
                eyex = eye[0],
                eyey = eye[1],
                eyez = eye[2],
                upx = up[0],
                upy = up[1],
                upz = up[2],
                centerx = center[0],
                centery = center[1],
                centerz = center[2];

        if (Math.abs(eyex - centerx) < EPSILON &&
                Math.abs(eyey - centery) < EPSILON &&
                Math.abs(eyez - centerz) < EPSILON) {
             Matrix.setIdentityM(out,0);
            //.identity(out);
        }

        z0 = -eyex + centerx;
        z1 = -eyey + centery;
        z2 = -eyez + centerz;

        len = 1 / (float)Math.sqrt(z0 * z0 + z1 * z1 + z2 * z2);
        z0 *= len;
        z1 *= len;
        z2 *= len;

        x0 = upy * z2 - upz * z1;
        x1 = upz * z0 - upx * z2;
        x2 = upx * z1 - upy * z0;
        len = (float)Math.sqrt(x0 * x0 + x1 * x1 + x2 * x2);
        if (len < NuMath.EPSILON) {
            x0 = 0;
            x1 = 0;
            x2 = 0;
        } else {
            len = 1 / len;
            x0 *= len;
            x1 *= len;
            x2 *= len;
        }

        y0 = z1 * x2 - z2 * x1;
        y1 = z2 * x0 - z0 * x2;
        y2 = z0 * x1 - z1 * x0;

        len = (float)Math.sqrt(y0 * y0 + y1 * y1 + y2 * y2);
        if (len < NuMath.EPSILON) {
            y0 = 0;
            y1 = 0;
            y2 = 0;
        } else {
            len = 1 / len;
            y0 *= len;
            y1 *= len;
            y2 *= len;
        }

        out[0] = x0;
        out[1] = y0;
        out[2] = z0;
        out[3] = 0;
        out[4] = x1;
        out[5] = y1;
        out[6] = z1;
        out[7] = 0;
        out[8] = x2;
        out[9] = y2;
        out[10] = z2;
        out[11] = 0;
        out[12] = -(x0 * eyex + x1 * eyey + x2 * eyez);
        out[13] = -(y0 * eyex + y1 * eyey + y2 * eyez);
        out[14] = -(z0 * eyex + z1 * eyey + z2 * eyez);
        out[15] = 1;
    }

    //// build projection matrices

    public static void ortho(float[] out, float size, float asp, float near, float far) {
        float lr, bt;
        if (asp > 1.0f) {
            lr = .5f / size / asp;
            bt = .5f / size;
        } else {
            lr = .5f / size;
            bt = .5f / size * asp;

        }
        float nf = 1.0f / (near - far);
        out[0] = 2.0f * lr;
        out[1] = 0;
        out[2] = 0;
        out[3] = 0;
        out[4] = 0;
        out[5] = 2.0f * bt;
        out[6] = 0;
        out[7] = 0;
        out[8] = 0;
        out[9] = 0;
        out[10] = 2.0f * nf;
        out[11] = 0;
        out[12] = 0;
        out[13] = 0;
        out[14] = 0;
        out[15] = 1;
    }

    public static void ortholhc(float[] out, float size, float asp, float near, float far) {
        ortho(out, size, asp, near, far);
        out[8] = -out[8];
        out[9] = -out[9];
        out[10] = -out[10];
        out[11] = -out[11];
    }

    public static void perspectivezf(float[] out, float zf, float aspect, float near, float far) {
        float nf = 1.0f / (near - far);
        if (aspect > 1.0f) {
            out[0] = zf / aspect;
            out[5] = zf;
        } else {
            out[0] = zf;
            out[5] = zf * aspect;
        }
        out[1] = 0;
        out[2] = 0;
        out[3] = 0;
        out[4] = 0;
        out[6] = 0;
        out[7] = 0;
        out[8] = 0;
        out[9] = 0;
        out[10] = (far + near) * nf;
        out[11] = -1;
        out[12] = 0;
        out[13] = 0;
        out[14] = (2 * far * near) * nf;
        out[15] = 0;
    }

    public static void perspectivelhczf(float[] out, float zf, float aspect, float near, float far) {
        perspectivezf(out, zf, aspect, near, far);
        out[8] = -out[8];
        out[9] = -out[9];
        out[10] = -out[10];
        out[11] = -out[11];
    }

    public static void scale(float[] out3,float[] in3,float scl) {
        out3[0] = scl * in3[0];
        out3[1] = scl * in3[1];
        out3[2] = scl * in3[2];
    }

    public static void negate(float[] negtrans, float[] trans) {
        negtrans[0] = -trans[0];
        negtrans[1] = -trans[1];
        negtrans[2] = -trans[2];
    }

    public static void inv(float[] invscale, float[] scale) {
        invscale[0] = 1.0f/scale[0];
        invscale[1] = 1.0f/scale[1];
        invscale[2] = 1.0f/scale[2];
    }

    public static void interp(float[] out,float[] a,float[] b, float t) {
        /*
        out[0] = a[0]*(1 - t) +  b[0]*t;
        out[1] = a[1]*(1 - t) +  b[1]*t;
        out[2] = a[2]*(1 - t) +  b[2]*t;
         */
        float ax = a[0];
        float ay = a[1];
        float az = a[2];
        out[0] = ax + t*(b[0] - ax);
        out[1] = ay + t*(b[1] - ay);
        out[2] = az + t*(b[2] - az);
    }

    public static void min(float[] out,float[] a,float[] b) {
        out[0] = Math.min(a[0],b[0]);
        out[1] = Math.min(a[1],b[1]);
        out[2] = Math.min(a[2],b[2]);
    }

    public static void max(float[] out,float[] a,float[] b) {
        out[0] = Math.max(a[0],b[0]);
        out[1] = Math.max(a[1],b[1]);
        out[2] = Math.max(a[2],b[2]);
    }

    public static void add(float[] out,float[] a,float[] b) {
        out[0] = a[0] + b[0];
        out[1] = a[1] + b[1];
        out[2] = a[2] + b[2];
    }

    public static void sub(float[] out,float[] a,float[] b) {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
        out[2] = a[2] - b[2];
    }

    // last element is implied to be '1', a point
    // transformMat4
    public static float[] transformMat4(float[] out,float[] a,float[] m) {
        //vec3.transformMat4 = function(out, a, m)
    //} {
        float x = a[0], y = a[1], z = a[2];
        out[0] = m[0] * x + m[4] * y + m[8] * z + m[12];
        out[1] = m[1] * x + m[5] * y + m[9] * z + m[13];
        out[2] = m[2] * x + m[6] * y + m[10] * z + m[14];
        return out;
    };

    // last element is implied to be '0', a vector
    // transformMat4Vec
    public static float[] transformMat4Vec(float[] out,float[] a,float[] m) {
    //vec3.transformMat4Vec = function(out, a, m) {
        float x = a[0], y = a[1], z = a[2];
        out[0] = m[0] * x + m[4] * y + m[8] * z;
        out[1] = m[1] * x + m[5] * y + m[9] * z;
        out[2] = m[2] * x + m[6] * y + m[10] * z;
        return out;
    };

}
