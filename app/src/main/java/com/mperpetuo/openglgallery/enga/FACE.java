package com.mperpetuo.openglgallery.enga;

/**
 * Created by cyberrickers on 11/11/2016.
 */

// used by physics3d
public class FACE {
    public short vertidx[] = new short[3];
    public short fmatidx; // this is redundant with the group structure, but pads size to power of 2 and might be handy

    public FACE() {
    }

    public FACE(FACE a) {
        vertidx[0] = a.vertidx[0];
        vertidx[1] = a.vertidx[1];
        vertidx[2] = a.vertidx[2];
        fmatidx = a.fmatidx;
    }

    public FACE(short a, short b, short c) {
        vertidx[0] = a;
        vertidx[1] = b;
        vertidx[2] = c;
    }

    public FACE(short a,short b,short c,short d) {
        vertidx[0] = a;
        vertidx[1] = b;
        vertidx[2] = c;
        fmatidx = d;
    }

    // convert 1 short[3] to a FACE
    public FACE(short[] av) {
        vertidx[0] = av[0];
        vertidx[1] = av[1];
        vertidx[2] = av[2];
    }

    // convert 1 short[3] to a FACE with offset
    public FACE(short[] af,int faceOffset) {
        int fo3 = 3*faceOffset;
        vertidx[0] = af[fo3];
        vertidx[1] = af[fo3 + 1];
        vertidx[2] = af[fo3 + 2];
    }

    // convert array of shorts with stride of 3 to an array of FACE's
    public static FACE[] makeFACEArray(short[] faces) {
        if (faces.length % 3 != 0) {
            Utils.alert(("makeFACEArray not multiple of 3 it's " + faces.length));
        }
        FACE[] ret = new FACE[faces.length/3];
        for (int i=0;i<ret.length;++i)
            ret[i] = new FACE(faces,i);
        return ret;
    }
}
