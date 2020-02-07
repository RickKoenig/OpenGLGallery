package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelFont;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import static com.mperpetuo.openglgallery.enga.GLUtil.globalmat;
import static com.mperpetuo.openglgallery.enga.ModelUtil.buildprism;
import static com.mperpetuo.openglgallery.enga.ModelUtil.buildsphere;

public class Lattice3D extends State {
    final String TAG = "Lattice3D";

    // draw lattice scene from 'framebuffer4' normally just to display, help develop interaceAPI

    // display, use mainvp
    Tree roottree; // SCENE, just render to display
    InterlaceAPI multiview;

    float[] oldspecpow;

    // animate some stuff
    float ang;
    float[] leftObjcenterTrans;
    float[] rightObjcenterTrans;
    Tree leftObj;
    Tree rightObj;
    float angStep = .005f;
    float angAmp = 3;

    // for lattice
    int latticeLevel = 4; // how many sticks in each direction
    float latticeSeparation = 1; // how far apart are the sticks

    float calcPos(int rank) {
        return latticeSeparation * (rank - .5f * (latticeLevel - 1));
    }

    Tree buildLattice() {
        int i, j;
        float stickWidth = .04f; // width of sticks
        Tree atreer = new Tree("Lattice");
        float stickScale = calcPos(latticeLevel - 1);

        // the master model
        Tree gpm = buildprism("gridPiece", new float[]{1, 1, 1}, "maptestnck.png", "tex");
        gpm.mod.flags |= Model.FLAG_DOUBLESIDED;
        float zft = .99f; // fight 'Z' fighting by making sticks slightly rectangular

        // do x
        Tree gpx = new Tree(gpm);
        gpx.scale = new float[]{stickScale, stickWidth * zft, stickWidth};
        for (j = 0; j < latticeLevel; ++j) {
            for (i = 0; i < latticeLevel; ++i) {
                Tree gp = new Tree(gpx);
                gp.trans = new float[]{0, calcPos(i), calcPos(j)};
                atreer.linkchild(gp);
            }
        }
        gpx.glFree();

        // do y
        Tree gpy = new Tree(gpm);
        gpy.scale = new float[]{stickWidth, stickScale, stickWidth * zft};
        for (j = 0; j < latticeLevel; ++j) {
            for (i = 0; i < latticeLevel; ++i) {
                Tree gp = new Tree(gpy);
                gp.trans = new float[]{calcPos(i), 0, calcPos(j)};
                atreer.linkchild(gp);
            }
        }
        gpy.glFree();

        // do z
        Tree gpz = new Tree(gpm);
        gpz.scale = new float[]{stickWidth * zft, stickWidth, stickScale};
        for (j = 0; j < latticeLevel; ++j) {
            for (i = 0; i < latticeLevel; ++i) {
                Tree gp = new Tree(gpz);
                gp.trans = new float[]{calcPos(i), calcPos(j), 0};
                atreer.linkchild(gp);
            }
        }
        gpz.glFree();

        // free master
        gpm.glFree();

        // return result fancy scene
        return atreer;
    }

    ;

    Tree buildWelcome() {
        // label fancy scene
        Tree ftree = new Tree("welcome sign");
        ModelFont scratchfontmodel = ModelFont.createmodel("reffont", "font3.png", "tex",
                1, 1,
                64, 8,
                true);
        scratchfontmodel.flags |= Model.FLAG_DOUBLESIDED;
        String str = "Welcome";
        scratchfontmodel.print(str);
        ftree.setmodel(scratchfontmodel);
        float scaledown = .1f;
        float signOffset = 2.4f; // 1.25 is golden!
        ftree.trans = new float[]{-1 * scaledown * str.length(), signOffset, -signOffset};
        ftree.scale = new float[]{.2f, .2f, .2f};
        return ftree;
    }

    ;

    Tree buildFancyScene() {
        Tree atreef = new Tree("fancyRoot");
        Tree lattice = buildLattice();
        atreef.linkchild(lattice);
        Tree welcome = buildWelcome();
        atreef.linkchild(welcome);
        Tree atreec = buildsphere("center", .25f, "Bark.png", "texc");
        atreec.mod.mat.put("color", new float[]{1, 1, 1, .6f});
        atreec.mod.flags |= Model.FLAG_HASALPHA;
        atreef.linkchild(atreec);
        return atreef;
    }

    ;

    public void init() {
        Log.i(TAG, "entering lattice 3d");

        multiview = new InterlaceAPI();
        // super sharp specular, remember old value
        oldspecpow = globalmat.get("specpow");
        globalmat.put("specpow", new float[]{5000.0f});

        ViewPort.mainvp = new ViewPort(); // VIEWPORT
        ViewPort.mainvp.trans = new float[]{0, 0, 5};

        //// build the scene
        roottree = new Tree("rootn");
        roottree.trans = new float[]{0, 0, 5};
        roottree.rot = new float[]{0, 0, 0};

        // a modelpart
        rightObj = buildsphere("atree2sp", 1, "panel.jpg", "diffusespecp");
        rightObj.trans = new float[]{3, 0, 0, 1};
        rightObjcenterTrans = rightObj.trans.clone();
        roottree.linkchild(rightObj);

        leftObj = buildprism("atree2sq1", new float[]{1, 1, 1}, "maptestnck.png", "tex");
        leftObj.trans = new float[]{-3, 0, 0, 1};
        leftObjcenterTrans = leftObj.trans.clone();
        roottree.linkchild(leftObj);

        ang = 0;

        Tree fancyScene = buildFancyScene();
        roottree.linkchild(fancyScene);
        ViewPort.setupViewportUI(1.0f/32.0f);

        //onresize();
    }

    public void proc() {
        // proc everything
        // get input
        InputState ir = Input.getResult();
        roottree.proc();

        // move a couple of objects forward and back sinusoidally using ang
        leftObj.trans[2] = (leftObjcenterTrans[2] + angAmp * (float) Math.sin(ang));
        //lattice2d.leftObjcenterTrans[0],
        //lattice2d.leftObjcenterTrans[1],
        //lattice2d.leftObjcenterTrans[2] + lattice2d.angAmp*Math.sin(lattice2d.ang)
        //];

        //lattice2d.rightObj.trans = [
        //lattice2d.rightObjcenterTrans[0],
        //lattice2d.rightObjcenterTrans[1],
        //lattice2d.rightObjcenterTrans[2] - lattice2d.angAmp*Math.sin(lattice2d.ang)
        //];
        rightObj.trans[2] = (rightObjcenterTrans[2] - angAmp * (float) Math.sin(ang));

        ViewPort.mainvp.doflycam(ir); // modify the trs of display vp by flying

        // animate the ang
        ang += angStep;
        if (ang > 2 * Math.PI)
            ang -= 2 * Math.PI;
    }

    ;

    @Override
    public void draw() {
        // MAIN SCREEN
        // draw main vp
        boolean useMultiView = true;
        if (useMultiView) { // use interleave and 4 views
            multiview.beginsceneAndDraw(ViewPort.mainvp,roottree);
        } else { // just normal 1 view drawing
            // setup camera
            ViewPort.mainvp.beginscene();
            // draw scene from camera
            roottree.draw(); // depends on FB 1,2,3
        }
    }

    @Override
    public void onResize() {
        int w = Main3D.viewWidth;
        int h = Main3D.viewHeight;
        Log.e(TAG,"the state 'Framebuffer4' resize event " + w + " " + h);
        multiview.onResize();
    }

    @Override
    public void exit() {
        globalmat.put("specpow",oldspecpow);

        // show everything in logs
        Log.i(TAG,"=== roottree log ===");
        roottree.log();
        Log.i(TAG,"before roottree logs");

        // log resources used before glfree
        GLUtil.logrc();

        // cleanup scenes
        roottree.glFree();
        roottree = null;

        Log.i(TAG,"after roottree glfree");
        multiview.glFree();

        // log resources used after glfree, should be empty (execpt for the global resources, fonts etc.)
        GLUtil.logrc();

        // put everything the way it was
        SimpleUI.clearbuts("viewport");
        ViewPort.mainvp = new ViewPort();
        Log.i(TAG,"exiting lattice 3d");
    }

}

