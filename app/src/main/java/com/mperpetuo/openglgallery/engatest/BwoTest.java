package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

public class BwoTest extends State {
    final String TAG = "BwoTest";
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model

    @Override
    public void init() {
        Log.i(TAG, "entering BwoTest");
        // main scene
        roottree = new Tree("backgroundtree");
        // build a prism
        //atree =  new Tree("water.bwo");
        Utils.pushandsetdir("barn");
        atree =  new Tree("barn.bwo");
        Utils.popdir();
        //atree =  new Tree("Trex_head.bwo");
        //atree =  new Tree("chk18.bwo");
        //atree =  new Tree("checkpoint_01.bwo");
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree
        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        //ViewPort.mainvp.trans = new float[] {1.31321f,3.39566f,-3.53785f};
        ViewPort.mainvp.trans = new  float[] {0,0,-2};
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        ViewPort.mainvp.setupViewportUI(1.0f/64.0f);
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
        // setup camera
        ViewPort.mainvp.beginscene();
        // draw scene from camera
        roottree.draw();
    }

    @Override
    public void exit() {
        SimpleUI.clearbuts("viewport");
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
