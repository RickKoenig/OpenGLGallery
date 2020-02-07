package com.mperpetuo.openglgallery.engatest.physics3d;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.VEC;

/**
 * Created by cyberrickers on 11/14/2016.
 */

public class ObjCollisions {


    // bbox code

    static final int MAX3DBOX=100;
    private static final String TAG = "ObjCollisions";

    static class bbox3d {
        VEC b,e;
        bbox3d() {
            b = new VEC();
            e = new VEC();
        }
    }

    static class colpair3d {
        int a = -1; // net yet set
        int b = -1;	// a < b
        void copy(colpair3d rhs) {
            a = rhs.a;
            b = rhs.b;
        }
    }

    static bbox3d[] bboxs3d;// = new bbox3d[MAX3DBOX];

    static int[][] colltab;
    static int[][] colltabidx;// = new int[MAX3DBOX][MAX3DBOX];

    static int[][] colltabx;
    static int[][] colltaby;
    static int[][] colltabz;

    static int ncolpairs;
    static colpair3d[] colpairs3d;// = new colpair3d[MAX3DBOX*MAX3DBOX];
    // odd is end
    // even is start
    // / 2 is boxnum
    static int[] sweepx;
    static int[] sweepy;
    static int[] sweepz;

    static int nboxes;

    static public void init3dbboxes(int nb) {
        int i;
        if (nb > MAX3DBOX)
            Utils.alert("set MAX3DBBOX higher, max " + MAX3DBOX + ", wanted " + nb);
        bboxs3d = new bbox3d[MAX3DBOX];
        colltabidx = new int[MAX3DBOX][MAX3DBOX];
        colpairs3d = new colpair3d[MAX3DBOX*MAX3DBOX];
        nboxes = nb;
        sweepx = new int[MAX3DBOX*2];
        sweepy = new int[MAX3DBOX*2];
        sweepz = new int[MAX3DBOX*2];
        for (i=0;i<nboxes*2;i++)
            sweepx[i] = i;
        colltabx = new int[MAX3DBOX][MAX3DBOX];
        for (i=0;i<nboxes*2;i++)
            sweepy[i] = i;
        colltaby = new int[MAX3DBOX][MAX3DBOX];
        for (i=0;i<nboxes*2;i++)
            sweepz[i] = i;
        colltabz = new int[MAX3DBOX][MAX3DBOX];
        colltab = new int[MAX3DBOX][MAX3DBOX];
        ncolpairs = 0;
        for (i=0;i<MAX3DBOX;++i)
            bboxs3d[i] = new bbox3d();
        for (i=0;i<MAX3DBOX*MAX3DBOX;++i) {
            colpairs3d[i] = new colpair3d();
        }
    }

    static public void collide3dboxes() {
        int i,j,k,old;
        int ise0,ise1,bn0,bn1;
        float p0,p1;
    // do x
        //Log.e(TAG,"do x");
        for (k=0;k<nboxes*2-1;k++) {
            for (i=k;i>=0;i--) {
                bn0 = sweepx[i];
                bn1 = sweepx[i+1];
                ise0 = bn0&1;
                ise1 = bn1&1;
                bn0 >>= 1;
                bn1 >>= 1;
                if (ise0 != 0)
                    p0 = bboxs3d[bn0].e.x;
                else
                    p0 = bboxs3d[bn0].b.x;
                if (ise1 != 0)
                    p1 = bboxs3d[bn1].e.x;
                else
                    p1 = bboxs3d[bn1].b.x;
                if (p0 > p1) {
                    j = sweepx[i];
                    sweepx[i] = sweepx[i+1];
                    sweepx[i+1] = j;
                    if ((ise0^ise1) != 0) {
                        colltabx[bn0][bn1] ^= 1;
                        colltabx[bn1][bn0] ^= 1;
                        old = colltab[bn0][bn1];
                        colltab[bn0][bn1] = colltab[bn1][bn0] =
                            colltabx[bn0][bn1]&colltaby[bn0][bn1]&colltabz[bn0][bn1];
                        if (colltab[bn0][bn1] != 0 && old == 0) {
                            colltabidx[bn0][bn1] = colltabidx[bn1][bn0] = ncolpairs;
                            if (bn0 < bn1) {
                                colpairs3d[ncolpairs].a = bn0;
                                colpairs3d[ncolpairs].b = bn1;
                            } else {
                                colpairs3d[ncolpairs].a = bn1;
                                colpairs3d[ncolpairs].b = bn0;
                            }
                            ncolpairs++;
                        } else if (colltab[bn0][bn1] == 0 && old != 0) {
                            int oidx = colltabidx[bn0][bn1];
                            ncolpairs--;
                            colpair3d cp = colpairs3d[ncolpairs];
                            colpairs3d[oidx].copy(cp);
                            colltabidx[cp.a][cp.b] = colltabidx[cp.b][cp.a] = oidx;
                        }
                    }
                } else
                    break;
            }
        }
    // do y
        //Log.e(TAG,"do y");
        for (k=0;k<nboxes*2-1;k++) {
            for (i=k;i>=0;i--) {
                bn0 = sweepy[i];
                bn1 = sweepy[i+1];
                ise0 = bn0&1;
                ise1 = bn1&1;
                bn0 >>= 1;
                bn1 >>= 1;
                if (ise0 != 0)
                    p0 = bboxs3d[bn0].e.y;
                else
                    p0 = bboxs3d[bn0].b.y;
                if (ise1 != 0)
                    p1 = bboxs3d[bn1].e.y;
                else
                    p1 = bboxs3d[bn1].b.y;
                if (p0 > p1) {
                    j = sweepy[i];
                    sweepy[i] = sweepy[i+1];
                    sweepy[i+1] = j;
                    if ((ise0^ise1) != 0) {
                        colltaby[bn0][bn1] ^= 1;
                        colltaby[bn1][bn0] ^= 1;
                        old = colltab[bn0][bn1];
                        colltab[bn0][bn1] = colltab[bn1][bn0] =
                            colltabx[bn0][bn1]&colltaby[bn0][bn1]&colltabz[bn0][bn1];
                        if (colltab[bn0][bn1] != 0 && old == 0) {
                            colltabidx[bn0][bn1] = colltabidx[bn1][bn0] = ncolpairs;
                            if (bn0 < bn1) {
                                colpairs3d[ncolpairs].a = bn0;
                                colpairs3d[ncolpairs].b = bn1;
                            } else {
                                colpairs3d[ncolpairs].a = bn1;
                                colpairs3d[ncolpairs].b = bn0;
                            }
                            ncolpairs++;
                        } else if (colltab[bn0][bn1] == 0 && old != 0) {
                            int oidx = colltabidx[bn0][bn1];
                            ncolpairs--;
                            colpair3d cp = colpairs3d[ncolpairs];
                            colpairs3d[oidx].copy(cp);
                            colltabidx[cp.a][cp.b] = colltabidx[cp.b][cp.a] = oidx;
                        }
                    }
                } else
                    break;
            }
        }
    // do z
        //Log.e(TAG,"do z");
        for (k=0;k<nboxes*2-1;k++) {
            for (i=k;i>=0;i--) {
                bn0 = sweepz[i];
                bn1 = sweepz[i+1];
                ise0 = bn0&1;
                ise1 = bn1&1;
                bn0 >>= 1;
                bn1 >>= 1;
                if (ise0 != 0)
                    p0 = bboxs3d[bn0].e.z;
                else
                    p0 = bboxs3d[bn0].b.z;
                if (ise1 != 0)
                    p1 = bboxs3d[bn1].e.z;
                else
                    p1 = bboxs3d[bn1].b.z;
                if (p0 > p1) {
                    j = sweepz[i];
                    sweepz[i] = sweepz[i+1];
                    sweepz[i+1] = j;
                    if ((ise0^ise1) != 0) {
                        colltabz[bn0][bn1] ^= 1;
                        colltabz[bn1][bn0] ^= 1;
                        old = colltab[bn0][bn1];
                        colltab[bn0][bn1] = colltab[bn1][bn0] =
                            colltabx[bn0][bn1]&colltaby[bn0][bn1]&colltabz[bn0][bn1];
                        if (colltab[bn0][bn1] != 0 && old == 0) {
                            colltabidx[bn0][bn1] = colltabidx[bn1][bn0] = ncolpairs;
                            if (bn0 < bn1) {
                                colpairs3d[ncolpairs].a = bn0;
                                colpairs3d[ncolpairs].b = bn1;
                            } else {
                                colpairs3d[ncolpairs].a = bn1;
                                colpairs3d[ncolpairs].b = bn0;
                            }
                            ncolpairs++;
                        } else if (colltab[bn0][bn1] == 0 && old != 0) {
                            int oidx = colltabidx[bn0][bn1];
                            ncolpairs--;
                            colpair3d cp = colpairs3d[ncolpairs];
                            colpairs3d[oidx].copy(cp);
                            colltabidx[cp.a][cp.b] = colltabidx[cp.b][cp.a] = oidx;
                        }
                    }
                } else
                    break;
            }
        }
    }

}
