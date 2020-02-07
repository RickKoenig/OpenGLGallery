package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.*;

public class Tester extends State {
    final String TAG = "Tester";
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model
    int frm; // frame counter

    @Override
    public void init() {
        Log.i(TAG, "entering tester");
        boolean testAssets = false;
        if (testAssets) {
            // test assets
            Utils.showAssets("");
            Utils.showAssets("textures");
            //FunctorTest.test();
        }
        // main scene
        roottree = new Tree("backgroundtree");
        // build a prism
        float us = ModelUtil.planepatchu;
        float vs = ModelUtil.planepatchv;
        ModelUtil.planepatchu = 2;
        ModelUtil.planepatchv = 2;
        Texture.setClampMode();
        atree =  ModelUtil.buildplanexy("aplane", 1, 1, "maptestnck.png", "texc"); // name, size, texture, color shader
        Texture.setWrapMode(); // back to defauilt
        ModelUtil.planepatchu = us;
        ModelUtil.planepatchv = vs;
        atree.trans = new float[] {0.0f,0.0f,1f}; // LHC, back away from camera (Z), input will translate X and Y (left handed coordinates)
        atree.rot = new float[3]; // will rotate with input, Euler angles
        atree.scale = new float[] {.4f,1.0f,1.0f}; // scrunched in X
        ((Model)atree.mod).mat.put("color",new float[] {1f,.5f,1f,1f}); // for the texc shader uniform, purple
        atree.mod.flags |= Model.FLAG_DOUBLESIDED; // can see both sides
        roottree.linkchild(atree); // hook and pass ownership to backgroundtree
        frm = 0;
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult(); // single finger input
        // allow input to modify 'fontAtree' position and rotation
        if (ir.touch > 0) {
            //Log.i(TAG, "input for tester.proc is FX = " + ir.fx + " FY = " + ir.fy + " Touch = " + ir.touch);
            atree.trans[0] = ir.fx; // matches, input is in center of object
            atree.trans[1] = ir.fy;
            atree.rot[1] = 2.0f* NuMath.PI*ir.fx;
            Log.d(TAG,"setting trans to " + ir.fx + " " + ir.fy);
        }
        roottree.proc(); // do animation and user proc if any
        ++frm;
        if (frm == 300) { // occasionally reset the state
            StateMan.changeState("Tester");
        }
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
        // reset main ViewPort to default if changed
        //ViewPort.mainvp = new ViewPort();
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
    }
}
