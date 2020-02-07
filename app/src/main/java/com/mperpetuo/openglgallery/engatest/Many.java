package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.StateMan;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.old_input.InputResult;

/**
 * Created by rickkoenig on 2/23/16.
 */
public class Many extends State {
    final String TAG = "Many";
    Tree roottree;
    Tree tree0;
    //int frm;
    static boolean toggleLookat;

    @Override
    public void init() {
        Log.e(TAG, "init");
        roottree = new Tree("backgroundtree");
        tree0 = ModelUtil.buildprism("aprism",new float[] {.5f,.5f,.5f}, "maptestnck.png","texc");
        tree0.mod.flags |= Model.FLAG_HASALPHA;
        Tree tree1 = ModelUtil.buildprism("aprism2",new float[] {.25f,.25f,.25f}, "maptestnck.png","tex"); // helper, builds 1 prism returns a Tree2
        tree1.trans = new float[] {2,0,0};
        //tree1.rotvel = new float[] {0,1,0}; //
        tree0.linkchild(tree1);

        int  i,j,k,n = 4;
        for (k=0;k<n;++k) {
            for (j=0;j<n;++j) {
                for (i=0;i<n;++i) {
                    Tree cld = new Tree(tree0);
                    cld.name = "dim" + k + j + i;
                    cld.mat.put("color",new float[] {(float)Math.random(),(float)Math.random(),(float)Math.random(),.5f}); // tree override for model color for flat
                    cld.trans = new float[] {2.0f*i,2.0f*j,2.0f*k};
                    cld.rotvel = new float[] {(float)Math.random()*.5f,(float)Math.random()*.5f,0};
                    if (Math.random() >= .5f)
                        cld.settexture("panel.jpg"); // override model texture with tree texture
                    roottree.linkchild(cld);
                }
            }
        }

        //tree0.qrot = new float[] {0,.7071f,0,.7071f};
        tree1.qrot = new float[] {.866f,0,0,.5f};
        tree0.trans = new float[] { -4,0,0};

        //tree1.trans = new float[] {-4,0,0};

        tree0.mat.put("color",new float[] {1f,0f,0f,1f});
        roottree.linkchild(tree0);

        //ViewPort.mainvp.trans = new float[] {0,0,-5}; // flycam
        ViewPort.mainvp.trans = new float[] {0,.3f,0}; // flycam test attach
        ViewPort.mainvp.rot = new float[] {0,0,0}; // flycam
        ViewPort.mainvp.camattach = roottree.children.get(3).children.get(0);
        ViewPort.mainvp.incamattach = true;
        ViewPort.mainvp.lookat = roottree.children.get(4).children.get(0);
        //ViewPort.mainvp.inlookat = toggleLookat;
        ViewPort.mainvp.inlookat = true;
        //toggleLookat = !toggleLookat;

    }

    @Override
    public void proc() {
        //if ((frm%100) == 0)
        //    Log.e(TAG,"Many proc");
        InputState ir = Input.getResult();
        /*++frm;
        if (frm == 3000)
            StateMan.changeState("Many");*/
        roottree.proc();
        ViewPort.mainvp.doflycam(ir);

    }

    @Override
    public void draw() {
        //if ((frm%100) == 0)
        //    Log.e(TAG,"Many draw");
        ViewPort.mainvp.beginscene();
        roottree.draw();
    }

    @Override
    public void exit() {
        Log.e(TAG,"exit");
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
    }

}
