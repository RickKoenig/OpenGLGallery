package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import android.view.View;

import com.mperpetuo.openglgallery.enga.FrameBufferTexture;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Texture;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;

import static android.opengl.GLES20.*;

/**
 * Created by cyberrickers on 6/25/2016.
 */
public class RenderTargets extends State {
    final String TAG = "RenderTargets";

    Tree roottree;
    Tree roottree1; // the one with the off screen render target
    Tree roottree2; // and this one
    FrameBufferTexture datatex1; // framebuffertexture 1
    FrameBufferTexture datatex2; // framebuffertexture 2
    Texture datatexd; // data texture

    float[] testdir;
    Tree atreep; // paper airplane

    ViewPort texvp1; // viewport for framebuffer 1
    ViewPort texvp2; // viewport for framebuffer 2

    //var testdirarea = null; // print paper airplane directions in UI

    // paper airplane update direction
    void morex() {
        testdir[0] += .125;
        //updatedir();
    }

    void lessx() {
        testdir[0] -= .125;
        //updatedir();
    }

    void morey() {
        testdir[1] += .125;
        //updatedir();
    }

    void lessy() {
        testdir[1] -= .125;
        //updatedir();
    }

    void morez() {
        testdir[2] += .125;
        //updatedir();
    }

    void lessz() {
        testdir[2] -= .125;
        //updatedir();
    }

    //function updatedir() {
    //    printareadraw(testdirarea,"Dir : " + testdir[0].toFixed(3) + ",  " + testdir[1].toFixed(3) + ",  " + testdir[2].toFixed(3));
    //}

    // data for data texture
    byte texdataarr[] = {
            -1,0, 0,-1, -1,0, 0,-1,  0,-1, 0,-1,  0,-1, 0,-1,
            -1,0, 0,-1, -1,0, 0,-1,  0,-1, 0,-1,  0,-1, 0,-1,
             0,0,-1,-1,  0,0,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
             0,0,-1,-1,  0,0,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1
    };
    @Override
    public void init() {
//	gl_mode(true);
//	if (!gl)
//		return;
        Log.i(TAG,"entering RenderTargets\n");
//// build render target
        //datatex1 = Texture.createTexture("rendertex1","maptestnck.png");
        datatex1 = FrameBufferTexture.createTexture("rendertex1",1024,1024);
        //datatex2 = Texture.createTexture("rendertex2","maptestnck.png");
        datatex2 = FrameBufferTexture.createTexture("rendertex2",1024,1024);

        // create sub viewport 1
        texvp1 = new ViewPort();
        texvp1.target = datatex1;
        texvp1.zoom = 1;
        texvp1.clearflags = GL_DEPTH_BUFFER_BIT;
        //texvp1.clearflags = GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT;
        texvp1.clearcolor = new float[] {.45f,.45f,0,1};
        texvp1.trans = new float[] {0,0,-5};
        texvp1.rot = new float[3];
        texvp1.near = .002f;
        texvp1.far = 10000;

        // create sub viewport 2
        texvp2 = new ViewPort();
        texvp2.target = datatex2;
        texvp2.zoom = 1;
        //texvp2.clearflags = GL_DEPTH_BUFFER_BIT;
        texvp2.clearflags = GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT;
        texvp2.clearcolor = new float[] {.55f,.35f,0,1};
        texvp2.trans = new float[] {0,0,-5};
        texvp2.rot = new float[3];
        texvp2.near = .002f;
        texvp2.far = 10000;
        testdir = new float[] {0,0,1};

        datatexd = Texture.createTexture("datatex",4,4,texdataarr);

        //// build the off screen scene 1
        roottree1 = new Tree("root1");
        //roottree1.rotvel = [0,.1,0];
        // a modelpart
        Tree atree2 = ModelUtil.buildsphere("atree2sp",1,"panel.jpg","diffusespecp");
        atree2.trans = new float[] {3,0,0};
        //atree2.rotvel = [.1,0,0];
        roottree1.linkchild(atree2);

        atree2 = ModelUtil.buildprism("atree2sq1",new float[] {1,1,1},"maptestnck.png","tex");
        atree2.trans = new float[] {-3,0,0};
        //atree2.rotvel = [.1,0,0];
        roottree1.linkchild(atree2);

//// build the off screen scene 2
        roottree2 = new Tree("root2");
        roottree2.rotvel = new float[] {0,.5f,0};
        // a modelpart
        atree2 = ModelUtil.buildsphere("atree2sp",1,"panel.jpg","diffusespecp");
        atree2.trans = new float[] {3,0,0};
        //atree2.rotvel = [.1,0,0];
        roottree2.linkchild(atree2);

        atree2 = ModelUtil.buildprism("atree2sq2",new float[] {1,1,1},"rendertex1","tex");
        atree2.trans = new float[] {-3,0,0};
        //atree2.rotvel = [.1,0,0];
        roottree2.linkchild(atree2);

//// build the main screen scene
        roottree = new Tree("root");
        //backgroundtree.rotvel = [0,.1,0];

        // a modelpart
        Tree atree = ModelUtil.buildprism("atree2prtd",new float[] {1,1,1},"datatex","tex");
        atree.trans = new float[] {0,3,0};
        //pendpce0.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        roottree.linkchild(atree);

        atree = ModelUtil.buildprism("atree2prt",new float[] {1,1,1},"rendertex1","tex");
        atree.trans = new float[] {0,0,0};
        //pendpce0.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        roottree.linkchild(atree);

        atree = ModelUtil.buildprism("atree2prt2",new float[] {1,1,1},"rendertex2","tex");
        atree.trans = new float[] {-3,0,0};
        //pendpce0.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        roottree.linkchild(atree);

        atree = ModelUtil.buildprism("atree2prm",new float[] {1,1,1},"maptestnck.png","tex");
        atree.trans = new float[] {3,0,0};
        //pendpce0.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        roottree.linkchild(atree);

        atreep = ModelUtil.buildpaperairplane("paperairplane","cvert");
        //fontAtree.mat.color = [1,0,0,1];
        atreep.trans = new float[] {3,3,0};
        atreep.rot = new float[3];
        //atreep.scale = [20,2,2];
        //atreep.rot = [2,0,0];
        //pendpce0.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        roottree.linkchild(atreep);

//// set the camera
        //mainvp.trans = [0,0,-15]; // flycam
        ViewPort.mainvp.trans = new float[] {0,0,-5}; // flycam
        ViewPort.mainvp.rot = new float[] {0,0,0}; // flycam
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true); // move forward
/*
        // ui
        setbutsname('test');
        // less,more,reset for paperairplane
        testdirarea = makeaprintarea('dir: ');
        makeabut("-x",null,lessx);
        makeabut("+x",null,morex);
        makeabr();
        makeabut("-y",null,lessy);
        makeabut("+y",null,morey);
        makeabr();
        makeabut("-z",null,lessz);
        makeabut("+z",null,morez);
        updatedir(); */
    }


    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        //atreep.rot = dir2rot(testdir);
        //float s = vec3.length(testdir);
        float s = NuMath.dir2rot(atreep.rot,testdir);
        s *= 2; // make bigger
        atreep.scale =new float[] {s, s, s};
        if (ir.touch > 0) {
            roottree1.rot = new float[]{0, ir.x * NuMath.TWOPI/ Main3D.viewWidth, 0};
            //Log.e(TAG,"roty = " + roottree1.rot[1]);
        }
        //roottree1.rot = [0,Math.PI*3/2,0];
        roottree2.proc();
        roottree1.proc();
        roottree.proc();
        //ViewPort.mainvp.doflycam(ir); // modify the trs of vp
    }

    @Override
    public void draw() {
        texvp1.beginscene();
        roottree1.draw();
        texvp2.beginscene();
        roottree2.draw();
        ViewPort.mainvp.beginscene();
        roottree.draw();
    }

    @Override
    public void exit() {
        // reset main ViewPort to default
        ViewPort.mainvp = new ViewPort();
        // buildLog the viewports
        Log.i(TAG,"backgroundtree buildLog\n");
        roottree.log();
        Log.i(TAG,"roottree1 buildLog\n");
        roottree1.log();
        Log.i(TAG,"roottree2 buildLog\n");
        roottree2.log();
        GLUtil.logrc();

        Log.i(TAG,"after backgroundtree and roottree1 and roottree2 glfree\n");
        roottree.glFree();
        roottree1.glFree();
        roottree2.glFree();
        datatexd.glFree();
        datatexd = null; // these nulls are not really necessary, the class is being destroyed/dereferenced
        datatex1.glFree();
        datatex1 = null;
        datatex2.glFree();
        datatex2 = null;
        GLUtil.logrc();
        roottree = null;
        roottree1 = null;
        roottree2 = null;
        Log.i(TAG,"exiting RenderTargets\n");
        //clearbuts('test');
    }


}
