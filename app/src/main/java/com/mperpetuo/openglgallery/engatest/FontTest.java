package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

public class FontTest extends State {
    final String TAG = "FontTest";
    Tree roottree; // top level tree
    ModelFont fontbig;
    ModelFont fontsmall;

    int frame;
    ViewPort mvp = new ViewPort();
    StringBuilder strb = new StringBuilder();

    @Override
    public void init() {
        Log.i(TAG, "entering FontTest");
        frame = 0;
        // main scene
        roottree = new Tree("backgroundtree");

        //float depth = Main3D.viewHeight/2;
        float depth = 32*96;
        //float depth = 3;
        Tree treef1;

        fontbig = ModelFont.createmodel("amodf1big","font3.png","tex",16,32,100,100,true);
        fontbig.flags |= Model.FLAG_DOUBLESIDED;
        //fontbig =  ModelUtil.buildplanexymodel("fontbigmodel",1,1,"maptestnck.png","tex");
        treef1 = new Tree("amodf1big");
        treef1.trans = new float[] {0,0,depth};
        //treef1.trans = [-depth,-32*4,depth];
        treef1.setmodel(fontbig);
        roottree.linkchild(treef1);

        fontsmall = ModelFont.createmodel("amodtim","font0.png","tex",64,64,100,100,true);
        //fontsmall =  ModelUtil.buildplanexymodel("smallfontmodel",1,1,"panel.jpg","tex");
        //amodtim.print("hey ho!");
        treef1 = new Tree("amodtim");
        treef1.trans = new float[] {-64*7,64,depth};
        //treef1.trans = new float[] {-3,3,depth};
        treef1.setmodel(fontsmall);
        roottree.linkchild(treef1);

        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        //mvp.changeKeyState(ViewPort.UP,true);
        mvp.setupViewportUI(16.0f);
        //mvp.trans = new float[] {0,0,-1000};
        //mvp.setupViewportUI(1.0f/2048.0f);
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        // proc
        //StringBuilder str = new StringBuilder("Test string ready to use.");
        //strb = new StringBuilder();
        strb.setLength(0);

        int i,j;
        int size = 96;
        //int size = 2;
        for (j=0;j<size;++j) {
            for (i=0;i<size;++i) {
                int x = i - size/2;
                int y = j - size/2;
                float r = (float)Math.floor(Math.sqrt(x*x + y*y));
                strb.append((char)((r + 96*100000 - frame)%96 + 32));
            }
            strb.append('\n');
        }

/*         //66 65 68
        int b = 66;
        str.append((char)b);
        str.append((char)65);
        str.append((char)68); */
        //Log.e(TAG,"string = " + str.toString());
        fontbig.print(strb.toString());
        String smallstring = "frm " + frame;
        fontsmall.print(smallstring);
        //strb.setLength(0);
        //strb.append("frm " + frame);
        //fontsmall.print(strb.toString());
        roottree.proc(); // do animation and user proc if any
        mvp.doflycam(ir);
        //Log.e(TAG,"frame = " + frame);
        ++frame;
    }

    @Override
    public void draw() {
        // setup camera
        mvp.beginscene();
        // draw scene from camera
        roottree.draw();
    }

    @Override
    public void exit() {
        SimpleUI.clearbuts("viewport");
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
