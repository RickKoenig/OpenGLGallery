package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Mesh;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

/**
 * Created by cyberrickers on 8/4/2016.
 */
public class BwsTestFortPoint extends State {
    private static final String TAG = "BwsTestFortPoint";

    private static float[] ameshVerts = {
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
    };

    private static float[] ameshUvs = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private static short[] ameshFaces = {
            0, 1, 2,
            3, 2, 1
    };

    Tree roottree;
    private InterlaceAPI multiview;

    @Override
    public void init() {
        Log.i(TAG, "entering webgl state7\n");
        multiview = new InterlaceAPI();

// build model 0, test model, uvs and texture, 'tex' shader, test amp phase freq
        Model amod0 = Model.createmodel("mod0");
        if (amod0.refcount == 1) {
            amod0.setshader("tex");
            Mesh amod0mesh = new Mesh();
            amod0mesh.verts = ameshVerts;
            amod0mesh.uvs = ameshUvs;
            amod0mesh.faces = ameshFaces;
            amod0.setmesh(amod0mesh);
            amod0.settexture("maptestnck.png");
            amod0.flags |= Model.FLAG_DOUBLESIDED;
            amod0.commit();
        }

        roottree = new Tree("root");

        Tree tree0 = new Tree("right");
        tree0.trans = new float[]{2, 0, 0};
        tree0.scale = new float[]{.25f, .25f, .25f};
        tree0.rotvel = new float[]{0, 0, NuMath.TWOPI / 3};
        tree0.setmodel(amod0.newdup()); // this is the same model, refcount is incremented
        Tree tree0sub = new Tree("sub right");
        tree0sub.trans = new float[]{0, 3, 0};
        tree0sub.setmodel(amod0);
        tree0.linkchild(tree0sub);
        roottree.linkchild(tree0);

        Tree tree1 = new Tree(tree0);
        tree1.name = "left";
        tree1.trans = new float[]{-2, 0, 0};
        roottree.linkchild(tree1);

        boolean showCannon = true;
        if (showCannon)
            ViewPort.mainvp.trans = new float[]{7.02316f, 51.149f, 167.413f}; // flycam, near the cannon
        else // somewhat back from origin
            ViewPort.mainvp.trans = new float[]{0, 0, -5}; // flycam
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        ViewPort.mainvp.setupViewportUI(1.0f/16.0f);

        boolean showFortPoint = true;
        if (showFortPoint) {
            Utils.pushandsetdir("fortpoint");
            Tree bwstree = new Tree("fp7opt.BWS");
            roottree.linkchild(bwstree);
            Utils.popdir();
        }
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();

        // proc
        roottree.proc();
        ViewPort.mainvp.doflycam(ir); // modify the trs of vp
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
        roottree.log();
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
