package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import android.util.Log;
import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;

public class Test3D extends State {
    final String TAG = "Test3D";
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model
    Tree cub, sph;
    float scrollX = 0;
    float scrollY = 0;

    @Override
    public void init() {
        Log.i(TAG, "entering test3d");

        // build parent
        roottree = new Tree("root");
        roottree.trans = new float[3];

        // build a prism
        //test3d.fontAtree = buildprism("aprism",[.5,.5,.5],"maptestnck.png","texc"); // helper, builds 1 prism returns a Tree2
        cub = ModelUtil.buildprism("aplane", new float[]{.5f, .5f, .5f}, "maptestnck.png", "diffusespecp");
        //test3d.tree0.mod.flags |= modelflagenums.HASALPHA;
        cub.trans = new float[]{1, 0, 0};
        //test3d.cub.mat.color=[1,.5,1,1];
        //test3d.cub.rotvel = [.5,.4,.3];
        roottree.linkchild(cub);

        // build a sphere
        sph = ModelUtil.buildsphere("asphere", .5f, "maptestnck.png", "diffusespecp");
        //test3d.tree0.mod.flags |= modelflagenums.HASALPHA;
        sph.trans = new float[]{-1, 0, 0};
        //test3d.sph.mat.color=[1,.5,1,1];
        //test3d.sph.rotvel = [.5,.4,.3];
        roottree.linkchild(sph);

        // add a dir light
        Tree lt = new Tree("dirlight");
        //lt.rot = [0,0,0];
        lt.rotvel = new float[]{1,0,0};

        lt.flags |= Tree.FLAG_DIRLIGHT;
        Lights.addlight(lt);
        roottree.linkchild(lt);

        // move view back some using LHC
        ViewPort.mainvp.trans = new float[]{0, 0, -2}; // flycam
        //ViewPort.mainvp.rot = new float[]{0, 0, 0}; // flycam
    }

    @Override
    public void proc() {
        // get input, some
        InputState ir = Input.getResult();
        // proc
        if (ir.touch > 0) {
            scrollX += ir.dfx;
            scrollY += ir.dfy;
            scrollX = Utils.range(-2,scrollX,2);
            scrollY = Utils.range(-1,scrollY,1);
            //Log.e(TAG,"scroll = " + scrollX + " " + scrollY);
            roottree.trans[0] = 2 * scrollX;
            roottree.trans[1] = 2 * scrollY;
        }
        // proc
        roottree.proc(); // do animation and user proc if any
        //ViewPort.mainvp.doflycam(ir);
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