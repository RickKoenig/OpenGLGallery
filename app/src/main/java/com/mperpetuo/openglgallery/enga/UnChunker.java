package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by cyberrickers on 7/10/2016.
 */
public class UnChunker {
    private static final String TAG = "UnChunker";
    // read any chunker file and display everything about it
    int chunksize;
    int lastnumele;
    int lastdatasize;
    ByteBuffer fr;

    UnChunker(String ab) {
        chunksize = 0;
        lastnumele = 0;
        fr = Utils.getBinaryFromAsset(ab);
    }

    static class Vec2 {
        float x,y;
    }

    static class Vec3 {
        float x,y,z;
    }
    static class Vec4 {
        float x, y, z, w;

        public Vec4() {
        }

        public Vec4(float xa, float ya, float za, float wa) {
            x = xa;
            y = ya;
            z = za;
            w = wa;
        }
    }

    static class PosLin {
        float time;
        float x,y,z;
    }

    static class RotLin {
        float time;
        float x,y,z,w; // Quaternion
    }

    static class Vec3M { // point with a material index
        Vec3 ele;
        int matidx;
    }

    static class Idx3 { // face
        int[] idx; // 3 face indices
    }

    static class Idx3M { // face with a material index
        int[] idx; // 3 face indices
        int matidx;
    }

    class ChunkHeaderInfo {
        int cn;
        int ct;
        int datasize;
        int numele;
        int elesize;
    }

    static public enum ChunkNameEnum {
        UID_NONE, // 0, for I'm in 'no chunk'
        // chunks
        UID_OBJECT,
        UID_MATERIAL,
        // misc
        UID_VERSION,
        UID_COMMENT,
        UID_NAME, // 5
        // materials
        UID_DTEX,
        UID_ATEX,
// objects
        UID_ID,
        UID_PID,
        UID_POS, // 10
        UID_ROTo,
        UID_SCALE,
        UID_FL,
        UID_VL,
        UID_VN,  // 15, vertex normals
        UID_TV, // texture uvs obsolete switch to tv0 to tv15
        UID_TFo, // will be obsolete
        UID_ROT_ROTAXIS, // change to quat later
        UID_ROT_QUAT,
        UID_ROT_EULER, // 20
        UID_TV0, // use these soon, texture layers, 16 should be enough
        UID_TV1, // I want them to be contiguous
        UID_TV2,
        UID_TV3,
        UID_TV4, // 25
        UID_TV5,
        UID_TV6,
        UID_TV7,
        UID_TV8,
        UID_TV9, // 30
        UID_TV10,
        UID_TV11,
        UID_TV12,
        UID_TV13,
        UID_TV14, // 35
        UID_TV15,
        UID_FN,	// will be obsolete 3 normals per face.. go for VN later
        UID_FS, // for groups
        UID_FO,
        UID_VS, // 40
        UID_VO,
        UID_USERPROP,
        UID_MATRIX,
        UID_KIND, // GEOM,HELPER,BONE etc. look in objects.h
        UID_TARGET, // 45
    // camera
        UID_CAMERA_FOV,
    // light
        UID_LIGHT_COLOR,
        UID_LIGHT_INTENSITY,
        UID_LIGHT_HOTSIZE,
        UID_LIGHT_FALLSIZE, // 50
        UID_LIGHT_USE_NEAR_ATTEN,
        UID_LIGHT_NEAR_ATTEN_START,
        UID_LIGHT_NEAR_ATTEN_END,
        UID_LIGHT_USE_ATTEN,
        UID_LIGHT_ATTEN_START, // 55
        UID_LIGHT_ATTEN_END,
        UID_LIGHT_SHADOW,
        UID_LIGHT_OVERSHOOT,
    // keyframes
        UID_KEYFRAMEo, // obsolete
        UID_TRACKFLAGS, // 60
        UID_POS_BEZ, // now uses KID_SCL_BEZv2 5-7-05, pick this one
        UID_POS_TCB,
        UID_POS_LIN,
        UID_ROT_BEZ,
        UID_ROT_TCB, // 65
        UID_ROT_LIN,
        UID_SCL_BEZ, // now uses KID_SCL_BEZv2 5-7-05, pick this one
        UID_SCL_TCB,
        UID_SCL_LIN,
    // new 5-7-05
        UID_ROT_EULER_X, // 70, these three use KID_FLOAT_BEZv2, pick this one, go bez all the way..
        UID_ROT_EULER_Y,
        UID_ROT_EULER_Z,
    // new 12-2-05
        UID_DIFFUSE,
    // new 12-5-05
        UID_POS_SAMP, // uses KID of KID_ROT/POS/SCL_LIN
        UID_ROT_SAMP, // 75
        UID_SCL_SAMP,
    // new 12-7-05
        UID_WEIGHTS1, // non blended verts
    // new 12-19-05
        UID_WEIGHTS2, // blended verts, bone index
        UID_WEIGHTS2F, // blended verts, weight amount
    // new 12-23-05
        UID_AMBIENT, // 80
        UID_OPACITY, // a float
        UID_SPECULAR,
        UID_SHINE,
        UID_EMIT,
    // new 1-3-6
        UID_TILING, // 85, int with flags, 1 uwrap, 2 vwrap, etc...
    // new 12-29-8
        UID_VC,  // vertex colors
    // new 5-20-09
        UID_VIS_SAMP,  // visibility track
    // new 6-16-09
        UID_REFL_AMT,	// reflection amount
        UID_RTEX,		// reflection texture (NYI)
    }

    static public enum ChunkTypeEnum {
        KID_I8(1), // 0
        KID_U16o(2),
        KID_I32(4),
        KID_S8o(1),
        KID_S16o(2),
        KID_S32o(4), // 5
        KID_VEC2(8),
        KID_VEC3(12),
        KID_VEC4(16),
        KID_CHUNK(0),
        KID_ENDCHUNK(0), // 10
        KID_ARR(0),
        KID_IDX2(8),
        KID_IDX3(12),
        KID_IDX3M(16),
        KID_FLOAT(4), // 15
        KID_VEC3M(16), // 3 floats and 1 int (for mat idx)
        // keyframe types
        KID_POS_BEZ(44),
        KID_POS_TCB(36),
        KID_POS_LIN(16),
        //	KID_ROT_EULER_X(28),
        //	KID_ROT_EULER_Y(28),
        //	KID_ROT_EULER_Z(28),
        KID_ROT_BEZ(20), // 20
        KID_ROT_TCB(40),
        KID_ROT_LIN(20),
        KID_SCL_BEZ(44),
        KID_SCL_TCB(36),
        KID_SCL_LIN(16), // 25

        KID_FLOAT_BEZv2(28),
        KID_POS_BEZv2(68),
        KID_ROT_BEZv2o(0), // don't use, use rot bez instead
        KID_SCL_BEZv2(68),
        // new 5-20-09
        KID_FLOAT_TCB(28), // 30
        KID_FLOAT_LINEAR(8),
        ;
        int bytesize;
        ChunkTypeEnum(int bsa) {
            bytesize = bsa;
        }
    }

    // for debugging and more
    final static ChunkTypeEnum[] tvalue = ChunkTypeEnum.values();
    final static ChunkNameEnum[] nvalue = ChunkNameEnum.values();

    String getchunkname_strs(int cnidx) {
        String unk = "?";
        if (cnidx<=0 || cnidx >= nvalue.length)
            return unk;
        return nvalue[cnidx].toString();
    }

    String getchunktype_strs(int ctidx) {
        if (ctidx<0 || ctidx>=tvalue.length)
            Utils.alert("bad chunktype typename " + ctidx);
        return tvalue[ctidx].toString();
    }

    int getchunktype_bytesize(int ctidx) {
        int length = tvalue.length;
        if (ctidx<0 || ctidx>=length)
            Utils.alert("bad chunktype bytesize " + ctidx);
        ChunkTypeEnum cte = tvalue[ctidx];
        return cte.bytesize;
    }

    ChunkHeaderInfo getchunkheader() {
        lastnumele = lastdatasize = 0;
        ChunkHeaderInfo ret;
        ret = new ChunkHeaderInfo();
        try {
            ret.cn = readI32();
        } catch (Exception e) {
            Log.d(TAG,"exp1");
            return null;
        }
        if (ret.cn == -1) {
            Log.d(TAG,"exp2");
            return null;
        }
        ret.ct = readI32();
        if (ret.ct == -1) {
            Log.d(TAG,"exp3");
            return null;
        }
        ret.datasize = getchunktype_bytesize(ret.ct);
        if (ret.datasize == 0) {
            final ChunkTypeEnum cte = tvalue[ret.ct];
            switch(cte) {
                case KID_CHUNK:
                    ret.datasize = readI32();
                    break;
                case KID_ENDCHUNK:
                    //Log.w(TAG,"in endchunk");
                    break;
                case KID_ARR:
                    ret.numele = this.readI32();
                    ret.ct = this.readI32();
                    ret.elesize = this.getchunktype_bytesize(ret.ct);
                    ret.datasize = ret.elesize*ret.numele;
                    int pad = ret.datasize & 3;
                    if (pad != 0)
                        ret.datasize += ( 4 - pad);
                    break;
                default:
                    ret.elesize = ret.datasize;
                    Utils.alert("unknown chunkkeyword " + ret.ct);
                    break;
            }
        }
        lastnumele = ret.numele;
        lastdatasize = ret.datasize;
        return ret;
    }

    int readI32() {
        int ret = fr.getInt();
        return ret;
    }

    float readF32() {
        float ret =  fr.getFloat();
        return ret;
    }

    Vec2 readVC2() {
        Vec2 ret = new Vec2();
        ret.x = fr.getFloat();
        ret.y = fr.getFloat();
        return ret;
    }

    Vec3 readVC3() {
        Vec3 ret = new Vec3();
        ret.x = fr.getFloat();
        ret.y = fr.getFloat();
        ret.z = fr.getFloat();
        return ret;
    }

    Vec4 readVC4() {
        Vec4 ret = new Vec4();
        ret.x = fr.getFloat();
        ret.y = fr.getFloat();
        ret.z = fr.getFloat();
        ret.w = fr.getFloat();
        return ret;
    }

// read bws matrix into mat4, read 12 floats in all
    // 4 columns of vec3
    float[] readmat4() {
        float[] ret = new float[16];
        int i,j;
        for (j=0;j<4;++j)
            for (i=0;i<3;++i)
                ret[j*4+i]=fr.getFloat();
        ret[15] = 1;
        return ret;
    }

    PosLin readPOS_LIN() {
        PosLin ret = new PosLin();
        ret.time = fr.getFloat();
        ret.x = fr.getFloat();
        ret.y = fr.getFloat();
        ret.z = fr.getFloat();
        return ret;
    }

    RotLin readROT_LIN() {
        RotLin ret = new RotLin();
        ret.time = fr.getFloat();
        ret.x = fr.getFloat();
        ret.y = fr.getFloat();
        ret.z = fr.getFloat();
        ret.w = fr.getFloat();
        return ret;
    }

// returns a string that was padded to a multiple of 4 bytes
    String readI8v() {
        if (lastnumele == 0)
            Utils.alert("not an array");
        StringBuilder sb = new StringBuilder();
        int i;
        //String ca = fr.get(lastnumele);//freadI8v(fr,lastnumele);
        for (i=0;i<lastnumele;++i) {
            //if (!ca[i])
            //    ;//alert("null in string");
            //else
            //    sb += String.fromCharCode(ca[i]);
            byte b = fr.get();
            if (b == 0)
                break;
            char c = (char)b;
            sb.append(c);
        }
        lastnumele&=3;
        if (lastnumele != 0) {
            lastnumele = 4 - lastnumele;
            fskip(lastnumele);
        }
        lastnumele=0;
        return sb.toString();
    }

    // read vec of int
    int[] readI32v() {
        int[] ret = new int[lastnumele];
        int i;
        for (i=0;i<lastnumele;++i)
            ret[i]= fr.getInt();
        lastnumele = 0;
        return ret;
    }

    // read array of vec2
    Vec2[] readVC2v() {
        int i;
        Vec2[] ret = new Vec2[lastnumele];
        for (i=0;i<lastnumele;++i) {
            Vec2 ele = new Vec2();
            ele.x = fr.getFloat();
            ele.y = fr.getFloat();
            ret[i] = ele;
        }
        lastnumele = 0;
        return ret;
    }

    // read array of vec3
    Vec3[] readVC3v() {
        int i;
        Vec3[] ret = new Vec3[lastnumele];
        for (i=0;i<lastnumele;++i) {
            Vec3 ele = new Vec3();
            ele.x = fr.getFloat();
            ele.y = fr.getFloat();
            ele.z = fr.getFloat();
            ret[i] = ele;
        }
        lastnumele = 0;
        return ret;
    }

    // read point with material index
    Vec3M[] readVC3Mv() {
        int i;
        Vec3M[] ret = new Vec3M[lastnumele];
        for (i=0;i<lastnumele;++i) {
            Vec3M vec3m = new Vec3M();
            vec3m.ele = readVC3();
            vec3m.matidx = fr.getInt();
            ret[i] = vec3m;
        }
        lastnumele = 0;
        return ret;
    }

    // read faces
    Idx3[] readIDX3v() {
        int i;
        Idx3[] ret = new Idx3[lastnumele];
        for (i=0;i<lastnumele;++i) {
            Idx3 ele = new Idx3();
            ele.idx = new int[3];
            ele.idx[0] = fr.getInt();
            ele.idx[1] = fr.getInt();
            ele.idx[2] = fr.getInt();
            ret[i] = ele;
        }
        lastnumele = 0;
        return ret;
    }

    // read faces with material index
    Idx3M[] readIDX3Mv() {
        int i;
        Idx3M[] ret = new Idx3M[lastnumele];
        for (i=0;i<lastnumele;++i) {
            Idx3M ele = new Idx3M();
            ele.idx = new int[3];
            ele.idx[0] = fr.getInt();
            ele.idx[1] = fr.getInt();
            ele.idx[2] = fr.getInt();
            ele.matidx = fr.getInt();
            ret[i] = ele;
        }
        lastnumele = 0;
        return ret;
    }

    public void fskip(int amount) {
        int curPos = fr.position();
        curPos += amount;
        fr.position(curPos);
    }

    public void skipdata() {
        fskip(lastdatasize);
        lastdatasize = lastnumele = 0;
    }

    public static void unchunktest(String fname) {
        Log.w(TAG, "unchunktest with " + fname);
        int chunktestdepth = 10;
        int depth = 0;
        UnChunker uc = new UnChunker(fname);
        ChunkHeaderInfo chi;
        while((chi = uc.getchunkheader()) != null) {
            ChunkTypeEnum cte = tvalue[chi.ct];
            String indentstr = "";
            int i;
            for (i=0;i<depth;++i)
                indentstr += "    ";
            if (depth < 0)
                indentstr="???";
            if (chi.numele == 0)
                Log.w(TAG,indentstr +
                        "chunk: name '" + uc.getchunkname_strs(chi.cn) +
                        "', type '" + uc.getchunktype_strs(chi.ct) +
                        "', datasize " + chi.datasize +
                        ", ");
            else
                Log.w(TAG,indentstr +
                        "chunk: name '" + uc.getchunkname_strs(chi.cn) +
                        "', array[" + chi.numele +
                        "] of type '" + uc.getchunktype_strs(chi.ct) +
                        "', eledatasize " + chi.elesize +
                        ", totaldatasize " + chi.datasize +
                        ", ");
            //if (chi.ct == ChunkTypeEnum.KID_CHUNK.ordinal()) {	// don't skip subchunk data
            if (cte == ChunkTypeEnum.KID_CHUNK) {	// don't skip subchunk data
                if (depth<chunktestdepth) {
                    Log.w(TAG,"ignoring data size of chunk, entering chunk");
                    ++depth; // enter subchunk data
                    continue;
                }
            }
// else skip subchunk data
            if (chi.numele == 0) { // it's not an array
                switch(cte) {
                    case KID_I32: // object id's (maybe)
                        int i32 = uc.readI32();
                        Log.w(TAG,"DATA: I32 = " + i32);
                        break;
                    case KID_FLOAT: // object id's (maybe)
                        float f32 = uc.readF32();
                        Log.w(TAG,"DATA: F32 = " + f32);
                        break;
                    case KID_VEC2: // 2 floats (for uvs, maybe)
                        Vec2 vc2 = uc.readVC2();
                        Log.w(TAG,"DATA: VEC2 (" + vc2.x + " " + vc2.y + ")");
                        break;
                    case KID_VEC3: // 3 floats (for unpadded 3d points)
                        Vec3 vc3 = uc.readVC3();
                        Log.w(TAG,"DATA: VEC3 (" + vc3.x + " " + vc3.y + " " + vc3.z + ")");
                        break;
                    case KID_VEC4: // 4 floats (for quats or rotaxis)
                        Vec4 vc4 = uc.readVC4();
                        Log.w(TAG,"DATA: VEC4 (" + vc4.x + " " + vc4.y + " " + vc4.z + " " + vc4.z + ")");
                        break;
                    case KID_ENDCHUNK:
                        Log.w(TAG,"ENDCHUNK: DONE");
                        uc.skipdata();
                        --depth;
                        break;
                    case KID_CHUNK:
                        Log.w(TAG,"CHUNK: SKIPPING");
                        uc.skipdata();
                        break;
                    default:
                        Log.w(TAG,"DATA: SKIPPING");
                        uc.skipdata();
                        break;
                }
            } else { // it's an array
                int head = 10; // how much to print
                switch(cte) {
                    case KID_I8:
                        String i8v = uc.readI8v();
                        Log.w(TAG,"DATA ARRAY: I8 '" + i8v + "'");
                        break;
                    case KID_I32:
                        int[] i32v=uc.readI32v();
                        for (i=0;i<i32v.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: I32 " + i32v[i]);
                        }
                        break;
                    case KID_VEC2: // 2 floats (for uvs, maybe)
                        Vec2 vec2v[] =uc.readVC2v();
                        for (i=0;i<vec2v.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: VEC2 " + vec2v[i].x + " " + vec2v[i].y);
                        }
                        break;
                    case KID_VEC3: // 3 floats (for unpadded 3d points)
                        Vec3 vc3v[] =uc.readVC3v();
                        for (i=0;i<vc3v.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: VEC3 " + vc3v[i].x + " " + vc3v[i].y + " " + vc3v[i].z);
                        }
                        break;
                    case KID_IDX3: //%s for tfaces
                        Idx3[] idx3v=uc.readIDX3v();
                        for (i=0;i<idx3v.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: TFACE " + idx3v[i].idx[0] + " " + idx3v[i].idx[1] + " " + idx3v[i].idx[2]);
                        }
                        break;
                    case KID_IDX3M: // for faces
                        Idx3M[] idx3mv=uc.readIDX3Mv();
                        for (i=0;i<idx3mv.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: FACE with matidx " + idx3mv[i].idx[0] + " " + idx3mv[i].idx[1] + " " + idx3mv[i].idx[2] + " " + idx3mv[i].matidx);
                        }
                        break;
                    case KID_VEC3M: // 3 floats and 1 int (for mat idx)
                        Vec3M[] vc3mv = uc.readVC3Mv();
                        for (i=0;i<vc3mv.length;++i) {
                            if (i >= head)
                                break;
                            Log.w(TAG,indentstr + "   DATA ARRAY[" + i + "]: VERT with matidx " + vc3mv[i].ele.x + " " + vc3mv[i].ele.y + " " + vc3mv[i].ele.z + " " + vc3mv[i].matidx);
                        }
                        break;
                    default:
                        Log.w(TAG,"DATA ARRAY: SKIPPING");
                        uc.skipdata();
                        break;
                }
            }
        }
        Log.w(TAG,"done!");
    }
}
