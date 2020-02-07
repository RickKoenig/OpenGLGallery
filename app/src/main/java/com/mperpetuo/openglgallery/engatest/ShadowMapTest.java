package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.FrameBufferTexture;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Lights;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

public class ShadowMapTest extends State {
    final boolean INVERT = true;
    final String TAG = "Basic";
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model

    FrameBufferTexture shadowtexture;

    Tree atree1,atree1b,atree2,atree3,atree4,atree5,atree6,atree7;
    float frm;

    float lightdist = 20;
    float[] lightloc = {0,lightdist,-lightdist};

    ViewPort shadowvp;
    ViewPort mvp;

    float speedUp = 1.0f;

    @Override
    public void init() {
        frm = 0;
        Log.i(TAG, "entering shadowmap");

// build render target
        int shadowmapres = 2048;
        shadowtexture = FrameBufferTexture.createTexture("shadowmap",shadowmapres,shadowmapres);

// shadow viewport
        shadowvp = new ViewPort();
        shadowvp.target = shadowtexture;
        if (INVERT) {
            shadowvp.clearcolor = new float[] {0,0,0,1}; // Set clear color to yellow, fully opaque
        } else {
            shadowvp.clearcolor = new float[] {1,1,1,1}; // Set clear color to yellow, fully opaque
        }
        shadowvp.trans = lightloc.clone();
        shadowvp.rot = new float[] {NuMath.PI/4,0,0}; // part of lightdir
        shadowvp.near = .1f;
        shadowvp.far = 10000;
        shadowvp.isshadowmap = true;

// main viewport
        mvp = new ViewPort();
        mvp.clearcolor = new float[] {.15f,.25f,.75f,1}; // Set clear color to yellow, fully opaque
        boolean close = false;
        if (close) {
            mvp.trans = new float[] {7,0,-3.05f};

        } else {
            mvp.trans = lightloc.clone();
            mvp.rot = new float[] {NuMath.PI/4,0,0}; // part of lightdir
        }
        mvp.near = .1f;
        mvp.far = 10000;
        //mvp.changeKeyState(ViewPort.UP,true);
        mvp.setupViewportUI(1.0f/16.0f);

// build the  scene
        boolean lesscast = false;
        roottree = new Tree("root");
        //backgroundtree.rotvel = [0,.1,0];
        roottree.flags |= Tree.FLAG_DONTDRAW; // testin
        // a modelpart
        atree1 = ModelUtil.buildplanexy2t("planexy1",20,20,"maptestnck.png","shadowmap","shadowmapuse"); // tex
        atree1.trans = new float[] {0,0,20};
        //atree1.rot = [.3,.4,0];
        //atree1.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        atree1.flags |= Tree.FLAG_DONTCASTSHADOW;
        roottree.linkchild(atree1);

        atree1b = ModelUtil.buildplanexz2t("planexz1b",20,20,"maptestnck.png","shadowmap","shadowmapuse"); // tex
        atree1b.trans = new float[] {0,-20,0};
        //atree1.rot = [.3,.4,0];
        //atree1.rotvel = [.1,.5,0];
        //pendpce0.flags |= treeflagenums.ALWAYSFACING;
        atree1b.flags |= Tree.FLAG_DONTCASTSHADOW;
        roottree.linkchild(atree1b);

        // lots of medals
        Tree atree2p = new Tree("par");
        atree2p.trans = new float[] {0,0,-4};
        atree2p.rotvel = new float[] {0,0,.1f*speedUp};
        atree2 = ModelUtil.buildplanexy("planexy2",.5f,.5f,"wonMedal.png","tex");
        int i,j;
        for (j=0;j<7;++j) {
            for (i=0;i<7;++i) {
                Tree nt = new Tree(atree2);
                nt.trans = new float[] {2*(i-3),2*(j-3),-1};
                atree2p.linkchild(nt);
            }
        }
        atree2.glFree();
        roottree.linkchild(atree2p);

        // shadow map viewer
        atree3 = ModelUtil.buildplanexy("planexy3",2,2,"shadowmap","shadowmapshow"); // invert framebuffer renders, sigh
        atree3.trans = new float[] {7,0,-1};
        atree3.flags |= Tree.FLAG_DONTCASTSHADOW;
        roottree.linkchild(atree3);

        // some spheres

        // the light sphere
        atree4 = ModelUtil.buildsphere("sph4",.2f,null,"flat"); // this is where the light is
        atree4.mod.mat.put("color",new float[] {1,1,0,1});
        atree4.trans = new float[] {0,20,-20};
        atree4.flags |= Tree.FLAG_DONTCASTSHADOW;
        atree4.flags |= Tree.FLAG_DIRLIGHT;
        atree4.rot = new float[] {NuMath.PI/4,0,0};
        Lights.addlight(atree4);
        roottree.linkchild(atree4);

        // three moving spheres
        atree5 = ModelUtil.buildsphere("sph5",.2f,"wonMedal.png","tex");
        atree5.trans = new float[] {0,-.875f,-8};
        atree5.scale = new float[] {8,8,8};
        roottree.linkchild(atree5);

        atree6 = new Tree(atree5);
        atree6.trans = new float[] {-.75f,0,-8};
        atree6.scale = new float[] {8,8,8};
        atree6.rotvel = new float[] {.4f*speedUp,.25f*speedUp,0};
        roottree.linkchild(atree6);

        atree7 = new Tree(atree5);
        atree7.trans = new float[] {-.75f,-1.5f,-8};
        atree7.scale = new float[] {12,12,12};
        roottree.linkchild(atree7);

        // four rotating cylinders
        Tree ras = new Tree("cylr"); // 2 cyl linked together
        ras.rotvel = new float[] {.3f*speedUp,0,0};
        roottree.linkchild(ras);

        Tree as = ModelUtil.buildcylinderxz2t("cyl6",.4f,2.5f,"panel.jpg","shadowmap","shadowmapuse");
        //var as = buildsphere("sph6",.2,"panel.jpg","tex");
        as.scale = new float[] {2,2,2};
        as.rotvel = new float[] {0,0,.2f*speedUp};
        as.trans = new float[] {0,0,5};
        if (lesscast) {
            as.children.get(0).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(1).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(2).flags |= Tree.FLAG_DONTCASTSHADOW;
        }
        ras.linkchild(as);

        as = ModelUtil.buildcylinderxz2t("cyl6",.4f,2.5f,"panel.jpg","shadowmap","shadowmapuse");
        //var as = buildsphere("sph6",.2,"panel.jpg","tex");
        as.scale = new float[] {2,2,2};
        as.rotvel = new float[] {0,0,.3f*speedUp};
        as.trans = new float[] {0,0,-5};
        if (lesscast) {
            as.children.get(0).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(1).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(2).flags |= Tree.FLAG_DONTCASTSHADOW;
        }
        ras.linkchild(as);

        // two more linked to root
        as = ModelUtil.buildcylinderxz("cyl7",.4f,1.5f,"panel.jpg","tex");
        as.scale = new float[] {5,5,5};
        as.trans = new float[] {-5,-20,16};
        if (lesscast) {
            as.children.get(0).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(1).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(2).flags |= Tree.FLAG_DONTCASTSHADOW;
        }
        roottree.linkchild(as);

        as = ModelUtil.buildcylinderxz("cyl7",.4f,1.5f,"panel.jpg","tex");
        as.scale = new float[] {2,2,2};
        as.trans = new float[] {-1,13,-14};
        as.rotvel = new float[] {.4f*speedUp,.8f*speedUp,0};
        if (lesscast) {
            as.children.get(0).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(1).flags |= Tree.FLAG_DONTCASTSHADOW;
            as.children.get(2).flags |= Tree.FLAG_DONTCASTSHADOW;
        }
        roottree.linkchild(as);
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();

        // proc
        // move some spheres around
        atree7.trans[2] = 10 + 15*(float)Math.sin(2*frm);
        atree6.trans[2] = 10 + 15*(float)Math.sin(3*frm);
        atree5.trans[2] = 10 + 15*(float)Math.sin(5*frm);

        roottree.proc(); // do animation and user proc if any
        mvp.doflycam(ir); // modify the trs of the vp

        // update frame counter
        frm += .002*speedUp;
        if (frm >= 2*Math.PI)
            frm -= 2*Math.PI;
    }

    @Override
    public void draw() {

        // draw to shadowmap
        shadowvp.beginscene();
        roottree.draw();

        // draw main scene
        mvp.beginscene();
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
        shadowtexture.glFree();
        // show usage after cleanup
        Log.i(TAG, "after backgroundtree glFree");
        roottree = null;
        GLUtil.logrc(); // show all allocated resources, should be clean
    }

}
