package com.mperpetuo.openglgallery.engatest;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.ModelBase;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;
import java.util.Arrays;


public class Arrows extends State {
    final String TAG = "Arrows";

    int arrowcnt;
    int maxarrowcnt = 400;
    int printarrowcnt = -10000;

    Tree arrowmaster;
    Tree expmaster;
    float ang = 0;
    int dela = 0;

    float driftview = .15f;
    float maxacc = .1f;

    Tree roottree,arrowlist;
    float[] camvel = {0,0,0};

    SimpleUI.UIPrintArea arrowarea;
    //SimpleUI.UIButton arrowreset;
    SimpleUI.UIPrintArea batteryarea;



    Tree makearrowmaster() {
        Tree arrowmaster = new Tree("arrow");
        // a modelpart
        //var atree = buildsphere("atree",.1,"panel.jpg","diffusespecp");
        Tree atree = ModelUtil.buildconexz("tail",.375f,.5f,"maptestnck.png","diffusespecp");
        atree.trans = new float[] {-.5f,0,0};
        atree.rot = new float[] {0,0,-NuMath.PIOVER2};
        arrowmaster.linkchild(atree);
        atree = ModelUtil.buildcylinderxz("mid",.1875f,.5f,"maptestnck.png","diffusespecp");
        atree.trans = new float[] {-.25f,0,0};
        atree.rot = new float[] {0,0,-NuMath.PIOVER2};
        arrowmaster.linkchild(atree);
        atree = ModelUtil.buildconexz("head",.375f,.5f,"maptestnck.png","diffusespecp");
        atree.trans = new float[] {.25f,0,0};
        atree.rot = new float[] {0,0,-NuMath.PIOVER2};
        arrowmaster.linkchild(atree);
        //atree.trans = [0,0,0];
        //atree.rotvel = [.01,.05,0];
        return arrowmaster;
    }

    Tree makeexpmaster() {
        // a modelpart
        Tree atree = ModelUtil.buildsphere("exp",.75f,"maptestnck.png","texc");
        atree.mat.put("color",new float[] {1,0,0,1});
        atree.mod.flags |= ModelBase.FLAG_HASALPHA;
        Tree expmaster = atree;
        return expmaster;
    }

    void resetarrows() {
        Log.e(TAG,"reseting arrows !!");
        /*
        arrowcnt = 0;
        int i;
        var childcopy = arrowlist.children.slice(0);
        for (i=0;i<childcopy.length;++i) {
            childcopy[i].glFree();
            childcopy[i].unlinkchild();
        }
        */
        arrowcnt = 0;
        arrowlist.unlinkchild();
        arrowlist.glFree();
        arrowlist = new Tree("arrowlist");
        roottree.linkchild(arrowlist);

    }

    Runnable runResetArrows = new Runnable() {
        @Override
        public void run() {
            resetarrows();
        }
    };

    class Arrowuserproc extends Tree.UserProc {
        int cnt = 10;
        @Override
        public void proc(Tree t) {
            --cnt;
            if (cnt < 0) {
                float r = (float)Math.random();
                if (r < .5) {
                    cnt = 10;
                    return;
                }
                t.unlinkchild();
                t.glFree();
                r = (float)Math.random();
                if (r < .5 && arrowcnt < maxarrowcnt) {
                    r = t.rot[2];
                    t = makeanarrow(t.trans,NuMath.normalangrad(r + .2f));
                    arrowlist.linkchild(t);
                    t = makeanarrow(t.trans,NuMath.normalangrad(r - .2f));
                    arrowlist.linkchild(t);
                } else {
                    t = makeanexp(t.trans,t.transvel);
                    arrowlist.linkchild(t);
                }
                --arrowcnt;
            }

        }
    }
/*
        t.cnt = 10;
    function arrowuserproc(t) {
        --t.cnt;
        if (t.cnt < 0) {
            var r = Math.random();
            if (r < .5) {
                t.cnt = 10;
                return;
            }
            t.unlinkchild();
            t.glFree();
            r = Math.random();
            if (r < .5 && arrowcnt < maxarrowcnt) {
                r = t.rot[2];
                t = makeanarrow(t.trans,normalangrad(r + .2));
                arrowlist.linkchild(t);
                t = makeanarrow(t.trans,normalangrad(r - .2));
                arrowlist.linkchild(t);
            } else {
                t = makeanexp(t.trans,t.transvel);
                arrowlist.linkchild(t);
            }
            --arrowcnt;
        }
    }
    */

    class Expuserproc extends Tree.UserProc {
        int cnt = 15;
        float color[] = new float[] {1,0,0,0};
        @Override
        public void proc(Tree t) {
            --cnt;
            if (cnt < 0) {
                t.unlinkchild();
                t.glFree();
            } else {
                color[3] = cnt/15.0f;
                //t.mat.put("color", new float[] {1,0,0,cnt/15.0f});
                t.mat.put("color",color);
                float s = 15 - cnt;
                s *= .05f;
                s += .5f;
                t.scale[0] = t.scale[1] = t.scale[2] = s;
            }
        }
    }

    /*
        t.cnt = 15;
    function expuserproc(t) {
        --t.cnt;
        if (t.cnt < 0) {
            t.unlinkchild();
            t.glFree();
            //t.cnt = 100;
            //t.transvel[0] = -t.transvel[0];
        } else {
            t.mat.color = [1,0,0,t.cnt/15.0];
            var s = 15 - t.cnt;
            s *= .05;
            s += .5;
            t.scale = [s,s,s];
        }
    }
*/

    Tree makeanarrow(float[] pos,float a) {
        ++arrowcnt;
        Tree t = new Tree(arrowmaster);
        t.trans = Arrays.copyOf(pos,pos.length);
        //t.trans = vec3.clone(pos);
        t.transvel = new float[] {(float)(2.5f*Math.cos(a)),(float)(2.5*Math.sin(a)),0};
        t.rot = new float[] {0,0,a};
        //t.cnt = 10;
        t.userproc = new Arrowuserproc();
        return t;
    }

    Tree makeanexp(float[] pos,float[] vel) {
        Tree t = new Tree(expmaster);
        t.trans = Arrays.copyOf(pos,pos.length);
        //t.trans = vec3.clone(pos);
        t.scale = new float[3];//[0,0,0];
        t.transvel = vel;
        //t.cnt = 15;
        t.userproc = new Expuserproc();
        return t;
    }


    float[] minpnt = new float[3];
    float[] maxpnt = new float[3];
    float[] dpos = new float[3]; // desired position
    float[] newpos = new float[3];
    float[] spread = new float[3];
    float[] dvel = new float[3];
    float[] acc = new float[3];
    void centerarrowsview() {
        boolean earlyRet = true;
        ArrayList<Tree> cld = arrowlist.children;
        float moveback = 2;
        if (cld.size() == 0) {
            dpos[0] = 0;
            dpos[1] = 0;
            dpos[2] = -moveback; // flycam
        } else if (cld.size() == 1) {
            Tree c = cld.get(0);
            dpos[0] = c.trans[0];
            dpos[1] = c.trans[1];
            dpos[2] = -moveback; // flycam
        } else {
            Tree c0 = cld.get(0);
            System.arraycopy(c0.trans,0,minpnt,0,3);
            System.arraycopy(minpnt,0,maxpnt,0,3);
            int i;
            for (i=1;i<cld.size();++i) {
                Tree c = cld.get(i);
                NuMath.min(minpnt,minpnt,c.trans);
                NuMath.max(maxpnt,maxpnt,c.trans);
            }
            NuMath.add(dpos,minpnt,maxpnt);
            NuMath.scale(dpos,dpos,.5f);
            NuMath.sub(spread,maxpnt,minpnt);
            dpos[2] = -.5f*Math.max(spread[0]/ Main3D.viewAsp,spread[1]) - moveback;
        }
        NuMath.interp(newpos,ViewPort.mainvp.trans,dpos,driftview);

        // now add inertia to the camera
        NuMath.sub(dvel,newpos,ViewPort.mainvp.trans);
        NuMath.sub(acc,dvel,camvel);
        float sl = NuMath.length2(acc);
        if (sl > maxacc*maxacc) {
            float scl = (float) (maxacc/Math.sqrt(sl));
            NuMath.scale(acc,acc,scl);
        }
        //if (earlyRet)
        //    return;
        NuMath.add(camvel,camvel,acc);
        NuMath.add(ViewPort.mainvp.trans,ViewPort.mainvp.trans,camvel);
        if (ViewPort.mainvp.trans[2] > -moveback+1)
            ViewPort.mainvp.trans[2] = -moveback+1;
    }

    public float getBatteryLevel() {
        Intent batteryIntent = Utils.getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float)level / (float)scale) * 100.0f;
    }

    @Override
    public void init() {
        Log.i(TAG, "entering arrows");

        // build the scene
        arrowmaster = makearrowmaster();
        expmaster = makeexpmaster();
        roottree = new Tree("root");
        float ppsaveu = ModelUtil.planepatchu;
        float ppsavev = ModelUtil.planepatchv;
        ModelUtil.planepatchu = 20;
        ModelUtil.planepatchv = 20;
        Tree pln = ModelUtil.buildplanexy("back",200,200,"panel.jpg","tex");
        ModelUtil.planepatchu = ppsaveu;
        ModelUtil.planepatchv = ppsavev;
        pln.trans = new float[] {0,0,5};
        roottree.linkchild(pln);

        arrowlist = new Tree("arrowlist");
        roottree.linkchild(arrowlist);

        resetarrows();

        // a modelpart
        ang = 0;
        dela = 0;
        //var atree = makeanarrow([0,0,0],ang);
        //arrowlist.linkchild(atree);
        //atree = makeanexp([0,0,0]);
        //backgroundtree.linkchild(atree);

        // set the lights
        //lights.wlightdir = vec3.fromValues(0,0,1);

        // set the camera
        //mainvp.trans = [0,0,-15]; // flycam
        ViewPort.mainvp.trans = new float[] {0,0,-25}; // flycam
        //ViewPort.mainvp.trans = new float[] {0,0,-25}; // flycam
        ViewPort.mainvp.rot = new float[] {0,0,0}; // flycam
        camvel = new float[3];

        // ui
        SimpleUI.setbutsname("arrow");
        // less,more,reset for pendu1
        arrowarea = SimpleUI.makeaprintarea("arrow: ");
        SimpleUI.makeabut("Reset Arrows", runResetArrows);
        batteryarea = SimpleUI.makeaprintarea("batteryarea = 50");
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        // proc
        roottree.proc(); // do animation and user proc if any
        //ViewPort.mainvp.doflycam(ir);

        if (ir.touch > 0 || arrowcnt == 0) {
            ++dela;
            if (dela == 6) {
                ang = NuMath.normalangrad(ang + .13f);
                Tree t = makeanarrow(new float[3],ang);
                arrowlist.linkchild(t);
                dela = 0;
            }
        }

        roottree.proc();
        centerarrowsview();
        updateArrowCount();
        if (Math.abs(arrowcnt - printarrowcnt) > 10) {
            Log.w(TAG,"arrow count = " + arrowcnt);
            printarrowcnt = arrowcnt;
        }
        batteryarea.draw("Battery at " + getBatteryLevel());

    }

    private void updateArrowCount() {
        arrowarea.draw("Arrows = " + arrowcnt);

    }

    @Override
    public void draw() {
        // setup camera
        ViewPort.mainvp.beginscene();
        // draw scene from camera
        roottree.draw();
    }

    @Override
    public void exit() {
        // reset main ViewPort to default
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG, "before backgroundtree glFree");
        roottree.log();
        GLUtil.logrc(); // show all allocated resources
        // cleanup
        arrowmaster.glFree();
        arrowmaster = null;
        expmaster.glFree();
        expmaster = null;
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after backgroundtree glFree");
        roottree = null;
        GLUtil.logrc(); // show all allocated resources, should be clean

        SimpleUI.clearbuts("arrow");
        //SimpleUI.removeUIPrintArea(arrowarea);
        //SimpleUI.removeUIButton(arrowreset);
    }

}
