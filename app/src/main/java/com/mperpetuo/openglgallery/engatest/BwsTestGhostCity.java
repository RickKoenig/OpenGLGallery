package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.*;

/**
 * Created by cyberrickers on 9/10/2016.
 */
public class BwsTestGhostCity extends State {
    private static final String TAG = "BwsTestGhostCity";

    Tree roottree;
    private InterlaceAPI multiview;

    @Override
    public void init() {
        Log.i(TAG,"entering webgl ghostcity\n");
        multiview = new InterlaceAPI();

        // create root tree
        roottree = new Tree("root");

        // create directional light and attach to root tree
        Tree lt = new Tree("dirlight");
        lt.rot = new float[] {NuMath.PI/4,0,0}; // point light down 45 degrees
        lt.rotvel = new float[] {0,1,0}; // spin on vertical axis
        lt.flags |= Tree.FLAG_DIRLIGHT;
        Lights.addlight(lt);
        roottree.linkchild(lt);

        // create scene and attach to root tree
        Utils.pushandsetdir("ghostcity");
        Tree bwstree = new Tree("ghostcity.BWS");
        Utils.popdir();
        roottree.linkchild(bwstree);

        // set camera orientation
        ViewPort.mainvp.trans = new float[] {-20.8f,51.6f,-45.1f};
        ViewPort.mainvp.rot = new float[] {-.098f,1.59f,0}; // TODO: make flycam start with non
        // zero rots
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        ViewPort.mainvp.setupViewportUI(1.0f/16.0f);
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
        //ViewPort.mainvp.beginscene();
        //roottree.draw();
        multiview.beginsceneAndDraw(ViewPort.mainvp, roottree);
    }

    @Override
    public void onResize() {
        multiview.onResize();
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
        multiview.glFree();
        multiview = null;
        GLUtil.logrc(); // show all allocated resources, should be clean
    }

}
