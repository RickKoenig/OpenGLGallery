package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import android.view.ViewGroup;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Lights;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

/**
 * Created by cyberrickers on 7/23/2016.
 */
public class BwsTestPrehistoric extends State {
    private static final String TAG = "BwsTestPrehistoric";

    Tree roottree;

    Tree resat1,resat2;

    @Override
    public void init() {
        Log.i(TAG, "entering BwsTestPrehistoric");
        // main scene
        roottree = new Tree("backgroundtree");

        Tree lt = new Tree("dirlight");
        lt.rot = new float[] {NuMath.PI/4,0,0};
        lt.rotvel = new float[] {0,1,0};
        lt.flags |= Tree.FLAG_DIRLIGHT;

        Lights.addlight(lt);
        roottree.linkchild(lt);

        // camera relative to dino body
        ViewPort.mainvp.trans = new float[] {0,.3f,.1f}; // flycam
        ViewPort.mainvp.rot = new float[] {NuMath.PI/8, NuMath.PI,0};
        ViewPort.mainvp.zoom = 1;

        // camera back alittle
//        ViewPort.mainvp.trans = new float[] {0,0,-2}; // flycam
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        SimpleUI.setbutsname("changeview");
        SimpleUI.makeabut("Change View", new Runnable() {
            @Override
            public void run() {
                if (ViewPort.mainvp.camattach == resat1)
                    ViewPort.mainvp.camattach = resat2;
                else
                    ViewPort.mainvp.camattach = resat1;
            }
        });
        ViewPort.mainvp.setupViewportUI(1.0f/512.0f);
        //var bwstree = new Tree2("fp7opt.BWS");
        Utils.pushandsetdir("prehistoric");
        Tree bwstree = new Tree("prehistoric.BWS");
        Utils.popdir();
        roottree.linkchild(bwstree);


        resat1 = roottree.findtree("Trex_body2.bwo");
        //resat2 = backgroundtree.findtree("PTbody.bwo");
        //resat = backgroundtree.findtree("Trex_torso.bwo");
        //resat2 = backgroundtree.findtree("PTjaw.bwo");
        resat2 = roottree.findtree("PTneck01.bwo");
        ViewPort.mainvp.camattach = resat1;
        ViewPort.mainvp.incamattach = true;

        Tree reslk = roottree.findtree("PTER_loco.bwo");
        //var reslk = backgroundtree.findtree("Trex_body2.bwo");
        ViewPort.mainvp.lookat = reslk;
        ViewPort.mainvp.inlookat = false;
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();

        // proc
        roottree.proc(); // do animation and user proc if any
        ViewPort.mainvp.doflycam(ir);
    }

    @Override
    public void draw() {
        ViewPort.mainvp.beginscene();
        roottree.draw();
    }

    @Override
    public void exit() {
        SimpleUI.clearbuts("viewport");
        SimpleUI.clearbuts("changeview");
        // reset main ViewPort to default
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG, "before backgroundtree glFree");
        roottree.log();
        GLUtil.logrc(); // show all allocated resources
        // cleanup
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after backgroundtree glFree");
        roottree = null;
        GLUtil.logrc(); // show all allocated resources, should be clean
    }

}
