package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

public class Cubemaptest extends State {
    final String TAG = "Cubemaptest";
    // webgl, trees
    Tree roottree;

    String[] scenelistcmt = {
            "cubemap_mountains.jpg",
            "Skansen",
            "Footballfield",
            //"panel.jpg",
            //"xpar.png",
    };
    static int cursceneidx = 0;

    @Override
    public void init() {
        Log.i(TAG,"entering webgl cubemaptest\n");
        // build scene
        roottree = new Tree("root");

        Tree atree;
        String curscene = scenelistcmt[cursceneidx];
        //	fontAtree = buildskybox("aprism3",[1,1,1],"FishPond","tex"); // helper, builds 1 prism returns a Tree2
        atree = ModelUtil.buildskybox("aprism3",new float[] {1, 1, 1},curscene, "tex")
        ; // helper, builds 1 prism returns a Tree2
        //	fontAtree = buildskybox("aprism3",[1,1,1],"cube02.jpg","tex"); // helper, builds 1 prism returns a Tree2
        roottree.linkchild(atree);

        //fontAtree = buildprism("aprism5",[1,1,1],"panel.jpg","tex"); // use cubemap texture and shader
        atree = ModelUtil.buildprism("aprism5",new float[] {1, 1, 1},"CUB_" + curscene, "cubemaptest")
        ; // use cubemap texture and shader
        //fontAtree = buildprism("aprism5",[1,1,1],"CUB_FishPond","envmapp"); // use cubemap texture and shader
        atree.trans = new float[] {0, 0, 2};
        float ints = .85f;
        atree.mat.put("color",new float[] {ints, ints, ints, 1.0f});
        //fontAtree.trans = [6,7.5,0];
        //fontAtree.rotvel = [.02,.1,0];
        roottree.linkchild(atree);

        // reset camera
        ViewPort.mainvp.trans = new float[] {0, 0, 0}; // flycam
        ViewPort.mainvp.rot = new float[] {0, 0, 0}; // flycam
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true); // move/fly forward while touching
        ViewPort.mainvp.setupViewportUI(1.0f/16.0f);
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();

        // proc
        roottree.proc(); // animate
        ViewPort.mainvp.doflycam(ir); //  // modify the trs of the vp
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
        roottree.log();
        GLUtil.logrc();
        Log.i(TAG,"after backgroundtree glfree\n");
        roottree.glFree();

        // show usage after cleanup
        GLUtil.logrc();
        roottree = null;

        // when state reloads, switch to different skybox
        ++cursceneidx;
        if (cursceneidx >= scenelistcmt.length)
            cursceneidx = 0;

        Log.i(TAG,"exiting webgl cubemaptest\n");
    }
}