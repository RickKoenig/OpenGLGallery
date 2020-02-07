package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;

public class Basic extends State {
    final String TAG = "Basic";
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model

    @Override
    public void init() {
        Log.i(TAG, "entering basic");
        // main scene
        roottree = new Tree("backgroundtree");
        // build a prism
        atree =  ModelUtil.buildplanexy("aplane", 1, 1, "maptestnck.png", "tex"); // name, size, texture, generic texture shader
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree
        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        ViewPort.mainvp.trans = new float[] {0,0,-1};
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
