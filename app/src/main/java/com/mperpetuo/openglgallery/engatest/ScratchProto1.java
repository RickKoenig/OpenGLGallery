package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import java.util.Arrays;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.Quat;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.StateMan;
import com.mperpetuo.openglgallery.enga.Texture;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;

//import static com.mperpetuo.openglgallery.enga.NuMath.length2;

// test enga with scratch code
public class ScratchProto1 extends State {
    final String TAG = "ScratchProto1";

    float back = 10;
    Tree roottree;
    Tree atree,btree;
    Tree ptree;

    int frm;

    float[] dir = new float[3];

    // test enga
    void testStuff(InputState ir) {
        // test Quat.mul
        boolean testquatmul = false;
        if (testquatmul) {
            float[] axis = new float[]{0, 0, 1};
            float rad = NuMath.TWOPI * (1.0f / 8.0f);
            float[] q1 = new float[4];
            Quat.rotAxisToQuat(q1, axis, rad);

            axis = new float[]{0, 1, 0};
            rad = NuMath.TWOPI * (1.0f / 8.0f);
            float[] q2 = new float[4];
            Quat.rotAxisToQuat(q2, axis, rad);

            atree.qrot = new float[4];//] {0,0,0,1};
            Quat.mul(atree.qrot,q1,q2);
            //fontAtree.qrot = null;
        }


        // Quat.rotAxisToQuat(fontAtree.qrot,axis,rad);

        /*
        // test Matrix.multiplyMM
        // it's transposed
        float ma[] = {
                1.0f,0.0f,0.0f,3.0f,
                0.0f,0.0f,0.0f,0.0f,
                0.0f,0.0f,0.0f,0.0f,
                2.0f,0.0f,0.0f,4.0f};
        float[]  mb = {
                5.0f,0.0f,0.0f,7.0f,
                0.0f,0.0f,0.0f,0.0f,
                0.0f,0.0f,0.0f,0.0f,
                6.0f,0.0f,0.0f,8.0f};
        float[] mout = new float[16];
        Matrix.multiplyMM(mout,0,ma,0,mb,0);
        int i;
        for (i=0;i<16;++i) {
            Log.e(TAG,"matout " + i + " = " + mout[i]);
        }
        Log.e(TAG,"test mat mult done");
*/
/*
        // test Quat.rotAxisToQuat and Quat.quatRot
        float[] axis = {NuMath.SQRT3O3,NuMath.SQRT3O3,NuMath.SQRT3O3};
        //float[] axis = {0,0,1};
        float[] vecin = new float[] {1,0,0};
        float[] vecout = new float[3];
        float[] qrot = new float[4];
        float[] axis2 = new float[3];
        float[] qrot2 = new float[4];
        int i;
        int step = 12;
        for (i=0;i<=step;++i) {
            float ang = (float)i/step*NuMath.TWOPI;
            Quat.rotAxisToQuat(qrot,axis,ang);
            float ang2 = Quat.quatToRotAxis(axis2,qrot);
            Quat.rotAxisToQuat(qrot2,axis2,ang2);

            Quat.quatRot(vecout, vecin, qrot2);
            Log.e(TAG,"quat rot = " + Arrays.toString(vecout));
        }
        // test Quat.normalize
        float[] qa = new float[] {1,1,1,1};
        Quat.normalize(qa);
        Log.e(TAG, "quat = " + Arrays.toString(qa));
*/
        boolean testdir2quat = true;
        if (testdir2quat) {
            dir[0] = ir.fx;
            dir[1] = ir.fy;
            dir[2] = 0.0f;
            /*
            float len = NuMath.length(dir);
            if (len < NuMath.EPSILON)
                len = 1.0f; */
            float len = Quat.dir2quat(btree.qrot, dir);
            btree.scale[1] = 10 * len *.5f; // size is 1 it's really 2
        }
        boolean doptree = true;
        if (doptree) {
            ptree.trans[0] = ir.fx*10;
            ptree.trans[1] = ir.fy*10;
        }

        boolean testdir2rot = false;
        if (testdir2rot) {
            dir[0] = ir.fx;
            dir[1] = ir.fy;
            dir[2] = .125f;
            btree.qrot = null; // shunt
            //btree.rot[0] = ir.fx;

            float len = NuMath.length(dir);
            if (len < NuMath.EPSILON)
                len = 1.0f;
            //float len = Quat.dir2quat(btree.qrot, dir);
            NuMath.dir2rot(btree.rot,dir);
            //btree.scale[1] = len;
        }

        boolean testquatinterp = false;
        if (testquatinterp) {
            float[] quata = new float[] {0,0,NuMath.SQRT2O2,NuMath.SQRT2O2};
            float[] quatb = new float[] {NuMath.SQRT2O2,0,0,NuMath.SQRT2O2};
            float t = (ir.fx/Main3D.viewAsp)*.5f+.5f;
            //t = .5f;
            Quat.interp(btree.qrot,quata,quatb,t);
            Log.e(TAG,"quat interp = " + Arrays.toString(btree.qrot));
        }
        boolean testdet = false;
        if (testdet) {
            float[] m = new float[] {2,3,5,0,
                    7,11,13,0,
                    17,19,23,0,
                    0,0,0,1};
            float f = NuMath.determinant(m);
            Log.e(TAG,"det = " + f);
        }
        boolean testcross = false;
        if (testcross) {
            float[] a = {2,3,5};
            float[] b = {7,11,13};
            float[] c = new float[3];
            NuMath.cross(c,a,b);
            Log.e(TAG,"crs = " + Arrays.toString(c));
            float f = NuMath.dot(a,b);
            Log.e(TAG,"dot = " + f);
        }
        boolean testnorm = false;
        if (testnorm) {
            float[] a = {3,4,12};
            float len = NuMath.normalize(a);
            Log.e(TAG,"norm = " + Arrays.toString(a) + " orig len = " + len);
        }
    }

    @Override
    public void init() {
        Log.e(TAG, "entering scratchProto1");
        // main tree
        roottree = new Tree("backgroundtree");

        // build child prism, now 2nd generation 'a'
        //Texture.setWrapMode(); // default
        atree =  ModelUtil.buildplanexy("aplane", 1, 1, "maptestnck.png", "texc");
        //Texture.setClampMode();
        atree.mod.flags |= Model.FLAG_DOUBLESIDED;
        atree.trans = new float[] {0.0f,1.0f,0.0f};
        // rot
        //fontAtree.rot = new float[3];// {0.0f,0.0f,0.0f}; // qrot overrides rot
        atree.scale = new float[] {.5f,1.0f,1.0f};
        //fontAtree.qrot = new float[4];

        //fontAtree.transvel = new float[] {0f,0f,1f};
        //fontAtree.rotvel = new float[] {0f,0f,NuMath.TWOPI/5f};
        //fontAtree.scalevel = new float[] {.5f,.5f,.5f};

        //scratch.tree0.mod.flags |= modelflagenums.HASALPHA;
        atree.mat.put("color",new float[] {1,.5f,1,.5f});
        atree.mod.flags |= Model.FLAG_HASALPHA; // turn on alpha blending for this model, shader does some alpha

        // user proc for Tree.proc call
        atree.userproc = new Tree.UserProc() {
            int cnt = 0;
            @Override
            public void proc(Tree t) {
                Log.e(TAG,"userproc of fontAtree, cnt = " + cnt++);
                if (t.rot != null)
                    Log.e(TAG,"rot = " + t.rot[0] + " " + t.rot[1] + " " + t.rot[2]);
            }
        };
        atree.userproc = null; // shunt userproc

        // 1st generation 'b'
        btree = new Tree("btree");
        //btree.trans = new float[] {0.0f,1.0f,0.0f};
        btree.qrot = new float[] {0,0,NuMath.SQRT2O2,-NuMath.SQRT2O2}; // roll to the right 90 degrees
        btree.rot = new float[3];
        btree.scale = new float[] {10.0f,10.0f,10.0f};

        // link fontAtree,btree to root
        btree.linkchild(atree);
        roottree.linkchild(btree); // backgroundtree -> btree -> fontAtree

        // ptree, first prism tree to root
        //Texture.setWrapMode(); // not default
        //ptree = ModelUtil.buildplanexy("aprism",1,1,"caution.png","tex");
        ptree = ModelUtil.buildprism("aprism",new float[] {1,9.0f/4.0f,1.0f/9.0f},"caution.png","tex");
        //ptree = ModelUtil.buildprism("aprism",null,"caution.png","tex");
        //ptree.mod.flags |= Model.FLAG_DOUBLESIDED;
        ptree.trans = new float[] {0,0,0};
        ptree.scale = new float[] {2f,2f,2f};
        ptree.rotvel = new float[] {0,.5f,0};
        //ptree.flags = Tree.FLAG_DONTDRAW;
        roottree.linkchild(ptree);

        // other trees
        Tree ot = ModelUtil.buildprism("bprism",new float[] {1,1,1},"maptestnck.png","tex");
        ot.trans = new float[] {1.5f,0,0};
        //ot.rotvel = new float[] {1,2,3};
        ptree.linkchild(ot);

        ot = ModelUtil.buildprism("bprism",new float[] {1,1,1},"maptestnck.png","tex");
        ot.trans = new float[] {3.75f,0,0};
        //ot.rotvel = new float[] {3,2,1};
        ptree.linkchild(ot);

        Tree base = new Tree("base");

        // create temp base space objects
        int i,j;
        int horzcount = 1; // 3
        int vertcount = 1; // 3
        for (j=-vertcount;j<=vertcount;++j) {
            for (i=-horzcount;i<=horzcount;++i) {
                Tree t;
                if (i == 0 && j == 0) {
                    t = ModelUtil.buildsphere("tsphere0",1, "maptestnck.png","tex");
                } else if (i == 1 && j == 0){
                    //t = new Tree("torus");
                    t = ModelUtil.buildtorusxz("ttorus0",.75f,.25f,"maptestnck.png","tex");
                } else {
                    t = ModelUtil.buildprism("tprism0",new float[]{1,1,1},"maptestnck.png","tex");
                }
                t.trans = new float[]{ 4*i,4*j, 0};
                t.rotvel = new float[]{.1f, .2f, .3f};
                base.linkchild(t);
            }
        }

        roottree.linkchild(base);

        boolean doClone = true;
        if (doClone) {
            // make a copy of base
            Tree baseClone = new Tree(base);
            //Tree baseClone = base;
            //float sc = 1.5f;
            //baseClone.scale = new float[]{sc, sc, sc};
            baseClone.trans = new float[]{.5f, 0, 0};
            baseClone.rotvel = new float[] {0, NuMath.TWOPI,0};
            roottree.linkchild(baseClone);
        }

        // move view back some using LHC
        ViewPort.mainvp.trans = new float[] {0,0,-10}; // camera // view -10 to +10
        //ViewPort.mainvp.rot = new float[3];
        //ViewPort.mainvp.rot = new float[] {0,0,0}; // camera
        //ViewPort.mainvp.isortho = true;
    }

    @Override
    public void proc() {
        // input
        InputState ir = Input.getResult();
        if (true) {
        //if (ir.touch > 0) { // only currently down
            testStuff(ir);
        //if (true) { // last down
            //ViewPort.mainvp.rot[2] = ir.fx;
            //btree.rot[2] = ir.fx * NuMath.TWOPI / Main3D.viewAsp;
            /*
            fontAtree.trans[0] = ir.fx;
            fontAtree.trans[1] = ir.fy;
            fontAtree.rot[0] = NuMath.PI * ir.fy;
            fontAtree.rot[1] = NuMath.PI * ir.fx; */
            //ViewPort.mainvp.rot[0] = NuMath.PI*ir.fx;
            //ViewPort.mainvp.rot[2] = NuMath.PI*ir.fy;
            //ViewPort.mainvp.trans[0] = ir.fx;
            //ViewPort.mainvp.trans[1] = ir.fy;
        }
        if ((frm % 100) == 0)
            ;//Log.e(TAG, "ScratchProto1 proc");
        ++frm;
        if (frm == 1800)
            StateMan.changeState("ScratchProto1");
        // proc
        roottree.proc();
        //ViewPort.mainvp.doflycam(); // modify the trs of vp using flycam,
        // needs controls up,down,left,right,forward,back,faster,slower,enable,disable,look around
    }

    @Override
    public void draw() {
        if ((frm % 100) == 0)
            ;//Log.e(TAG, "ScratchProto1 draw");
        // draw
        ViewPort.mainvp.beginscene();
        roottree.draw();
    }

    @Override
    public void exit() {
        Log.e(TAG,"exiting scratchProto1");
        // reset main ViewPort to default if changed
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG, "before backgroundtree glFree");
        roottree.log();
        GLUtil.logrc();
        // cleanup
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after backgroundtree glFree");
        roottree = null;
        GLUtil.logrc();
        //Texture.setClampMode(); // default
    }
}
