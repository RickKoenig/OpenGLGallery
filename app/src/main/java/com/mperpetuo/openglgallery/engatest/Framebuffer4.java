package com.mperpetuo.openglgallery.engatest;

import android.opengl.Matrix;
import android.util.Log;

import com.mperpetuo.openglgallery.enga.FrameBufferTexture;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelFont;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mperpetuo.openglgallery.enga.GLUtil.globalmat;
import static com.mperpetuo.openglgallery.enga.ModelUtil.buildplanexyNt;
import static com.mperpetuo.openglgallery.enga.ModelUtil.buildsphere;

public class Framebuffer4 extends State {
    final String TAG = "Framebuffer4";
// display, use mainvp
    Tree roottree; // SCENE, just render FB n to display

// frame buffer FBn
    Tree roottreen; // SCENE, the one with the off screen render target FB n
    Tree roottreenpp; // SCENE, render to off screen pixel perfect FB n
    ViewPort frametexnvp; // VIEWPORT, viewport for FB n
    ViewPort pixelPerfectVp; // VIEWPORT, test pixel perfect SCENE on FB n

    float minGain;
    float maxGain ;
    float gain;
    float gainMul;

    // convergence
    float minConvergence ;
    float maxConvergence;
    float convergence;
    float convergenceMul;

    float[] oldspecpow;

    // number of views/targets
    int numTargets = 4;
    ArrayList<FrameBufferTexture> frametexn= new ArrayList<FrameBufferTexture>(); // RENDER TARGETS, FB n
    // boolean, toggle flycam between mainvp and FB n viewport
    // which flycam to use, true use the framebuffer, false use the display
    boolean flycamFBn = true;
    HashMap<Integer,String> mergeShaders = new HashMap<>();
    boolean set1 = false;
    boolean set2 = false;
    boolean set3 = true;

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
    int latticeSeparation = 1; // how far apart are the sticks


// tree planexy of multi FB, to ajust asp
    Tree fbnPlaneXY;

    String mixShader;

    float calcPos(int rank) {
//	return -1 + rank*2/(framebuffer4.latticeLevel - 1);
        return latticeSeparation*(rank - .5f*(latticeLevel - 1));
    }

    Runnable toggleViews = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"toggleviews");;
            toggleFlycam();
        }
    };

    Tree buildLattice() {
        int i,j;
        float stickWidth = .04f; // width of sticks
        Tree atreer = new Tree("Lattice");
        //atreer.scale = [2,2,2];
        float stickScale = calcPos(latticeLevel - 1);

        // the master model
        Tree gpm = ModelUtil.buildprism("gridPiece",new float[] {1,1,1},"maptestnck.png","tex");
        gpm.mod.flags |= Model.FLAG_DOUBLESIDED;
        float zft = .99f; // fight 'Z' fighting by making sticks slightly rectangular

        // do x
        Tree gpx = new Tree(gpm);
        gpx.scale = new float[] {stickScale,stickWidth*zft,stickWidth};
        for (j=0;j<latticeLevel;++j) {
            for (i=0;i<latticeLevel;++i) {
                Tree gp = new Tree(gpx);
                gp.trans = new float[] {0,calcPos(i),calcPos(j)};
                atreer.linkchild(gp);
            }
        }
        gpx.glFree();

        // do y
        Tree gpy = new Tree(gpm);
        gpy.scale = new float[] {stickWidth,stickScale,stickWidth*zft};
        for (j=0;j<latticeLevel;++j) {
            for (i=0;i<latticeLevel;++i) {
                Tree gp = new Tree(gpy);
                gp.trans = new float[] {calcPos(i),0,calcPos(j)};
                atreer.linkchild(gp);
            }
        }
        gpy.glFree();

        // do z
        Tree gpz = new Tree(gpm);
        gpz.scale = new float[] {stickWidth*zft,stickWidth,stickScale};
        for (j=0;j<latticeLevel;++j) {
            for (i=0;i<latticeLevel;++i) {
                Tree gp = new Tree(gpz);
                gp.trans = new float[] {calcPos(i),calcPos(j),0};
                atreer.linkchild(gp);
            }
        }
        gpz.glFree();

        // free master
        gpm.glFree();

        // return result fancy scene
        return atreer;
    };

    Tree buildWelcome() {
        // label fancy scene
        Tree ftree = new Tree("welcome sign");
        ModelFont scratchfontmodel = ModelFont.createmodel("reffont","font3.png","tex",
                1,1,
                64,8,
                true);
        String str = "Welcome";
        scratchfontmodel.print(str);
        ftree.setmodel(scratchfontmodel);
        float scaledown = .1f;
        float signOffset = 2.4f; // 1.25 is golden!
        ftree.trans = new float[] {-1*scaledown*str.length(),signOffset,-signOffset};
        ftree.scale = new float[] {.2f,.2f,.2f};
        return ftree;
    };

    Tree buildFancyScene() {
        Tree atreef = new Tree("fancyRoot");
        Tree lattice = buildLattice();
        atreef.linkchild(lattice);
        Tree welcome = buildWelcome();
        atreef.linkchild(welcome);
        Tree atreec = buildsphere("center",.25f,"Bark.png","texc");
        atreec.mod.mat.put("color",new float[] {1,1,1,.6f});
        atreef.linkchild(atreec);
        return atreef;
    };

    void toggleFlycam() {
        flycamFBn = !flycamFBn;
    };

    void initRenderTargets() {
        for (int i=0;i<numTargets;++i) {
            frametexn.add(FrameBufferTexture.createTexture("rendertex" + i,Main3D.viewWidth,Main3D.viewHeight));
        }
    }

    void exitRenderTargets() {
        for (int i=0;i<numTargets;++i) {
            FrameBufferTexture fbt = frametexn.get(i);
            if (fbt == null)
                Log.e(TAG,"freeing a null FrameBufferTexture !!!");
            else
                fbt.glFree();
        }
        frametexn.clear();
    }

    @Override
    public void init() {
        if (set1) {
            mergeShaders.put(1, "tex");
            mergeShaders.put(2, "blend2");
            mergeShaders.put(3, "blend3");
            mergeShaders.put(4, "blend4");
            mergeShaders.put(16, "blend16");
        }
        if (set2) {
            mergeShaders.put(1, "tex");
            mergeShaders.put(2, "redblue");
            mergeShaders.put(3, "blend3");
            mergeShaders.put(4, "blend4");
            mergeShaders.put(16, "blend16");
        }
        if (set3) {
            mergeShaders.put(1, "tex");
            mergeShaders.put(2, "redblue");
            mergeShaders.put(3, "blend3");
            mergeShaders.put(4, "interleave4");
            mergeShaders.put(16, "blend16");
        }
        // gain
        minGain = -.25f;
        maxGain = .25f;
        gain = .18f;//.75;
        gainMul = 100.0f;

        // convergence
        minConvergence = -1;
        maxConvergence = 1;
        convergence = .62f;//.75;
        convergenceMul = 100.0f;

        oldspecpow = globalmat.get("specpow");
        globalmat.put("specpow",new float[] {5000.0f});

        initRenderTargets();

        // build some viewports, off screen a 'view N' viewport
        frametexnvp = new ViewPort();
        frametexnvp.trans = new float[] {0,0,5};
        frametexnvp.target = frametexn.get(0);
        // clearflags:gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT, // default
        frametexnvp.clearcolor =  new float[] {0,1,0,.9375f}; // RGBA, some alpha leak thru, test frame buffer alpha blending
        // "trans":[0,0,0], // default
        // "rot":[0,0,0], // default
        frametexnvp.near = .002f; // close quarters
                // far:10000.0, // default
                // zoom:1, // default is 1
                //asp:gl.asp, // these viewports always have implied asp of gl.sap
                //xo:0, // TODO: implement, new viewport offsets, (skew / scroll / convergence)
                //yo:0,
                //xs:1, // for now always full size
                //ys:1

        // pixel perfect font viewport
        pixelPerfectVp = new ViewPort();

                // where to draw
        pixelPerfectVp.target = frametexn.get(0);;
        pixelPerfectVp.clearflags = 0;//:gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT,
        // view volume
        pixelPerfectVp.near = -100;
        pixelPerfectVp.far = 100;
        // asp:gl.asp, // always gl.asp
        pixelPerfectVp.isortho = true;
        pixelPerfectVp.ortho_size = Main3D.viewHeight/2; // make pixel perfect
        //xo:0, // TODO: NYI, please do implement skew offsets
        //yo:0,
        //xs:1, // not planned yet
        //ys:1

        // main display viewport
        ViewPort.mainvp = new ViewPort();
        ViewPort.setupViewportUI(1.0f/32.0f);
        SimpleUI.setbutsname("framebuffer4");
        SimpleUI.makeabut("tog view",toggleViews);


        //// build the off screen scene FB n
        roottreen = new Tree("rootn");
        roottreen.trans = new float[] {0,0,5};
        roottreen.rot = new float[] {0,0,0};

        // a modelpart
        rightObj = buildsphere("atree2sp",1,"panel.jpg","diffusespecp");
        rightObj.trans = new float[] {3,0,0,1};
        rightObjcenterTrans = rightObj.trans.clone();
        roottreen.linkchild(rightObj);

        leftObj = ModelUtil.buildprism("atree2sq1",new float[] {1,1,1},"maptestnck.png","tex");
        leftObj.trans = new float[] {-3,0,0,1};
        leftObjcenterTrans = leftObj.trans.clone();
        roottreen.linkchild(leftObj);

        ang = 0;

        Tree fancyScene = buildFancyScene();
        roottreen.linkchild(fancyScene);

        // build pixel perfect scene
        // build pixel perfect font model and tree for screen output
        ModelFont fontmodel = ModelFont.createmodel("pp model","smallfont.png","font2c",8,8,100,100,true);
        fontmodel.mat.put("fcolor",new float[] {0,1,0,1});
        fontmodel.mat.put("bcolor",new float[] {0,0,0,1});
        fontmodel.flags |= Model.FLAG_NOZBUFFER; // always in front when drawn
        fontmodel.print("Pixel perfect ###\\");
        roottreenpp = new Tree("pixel perfect roottree");
        roottreenpp.setmodel(fontmodel);

        // build the main screen scene last so we can hook up render targets, depends on FB 1
        roottree = new Tree("root");
        mixShader = mergeShaders.get(numTargets);
        Log.w(TAG,"MIXSHADER = '" + mixShader + "'\n");
        if (mixShader != null) { // if a shader can handle numTargets, build quad
            /*ArrayList<String> textureList = new ArrayList<>();
            for (int i = 0; i < numTargets; ++i) {
                String rt = "rendertex" + i;
                textureList.add(rt);
            }*/
            //fbnPlaneXY = buildplanexy("aplane",1,1,"maptestnck.tga","tex");
            //framebuffer4.fbnPlaneXY = buildplanexy("aplane",1,1,"xpar.png",mixShader);
            //fbnPlaneXY = buildplanexyNt("aplane",1,1,textureList,mixShader);
            //fbnPlaneXY = buildplanexy2t("aplane",1,1,textureList.get(0),textureList.get(1),mixShader);
            String[] textureList = new String[numTargets];
            for (int i = 0; i < numTargets; ++i) {
                String rt = "rendertex" + i;
                textureList[i] = rt;
            }
            fbnPlaneXY = buildplanexyNt("aplane",1,1,textureList,mixShader);
            //fbnPlaneXY.flags |= Tree.FLAG_DONTDRAWC;
            fbnPlaneXY.scale = new float[] {1,-1,1};
            //framebuffer4.fbnPlaneXY.mod.flags &= ~modelflagenums.HASALPHA; // draw backface since scale has a -1
            //fbnPlaneXY.mod.flags |= Model.FLAG_HASALPHA; // draw backface since scale has a -1
            fbnPlaneXY.mod.flags |= Model.FLAG_DOUBLESIDED; // draw backface since scale has a -1
            fbnPlaneXY.trans = new float[] {0,0,1};
            roottree.linkchild(fbnPlaneXY);
        }

        // add a couple more tree nodes to rootree
        // build a prism
        Tree cub =  ModelUtil.buildprism("abox",new float[] {.25f,.25f,.25f},"maptestnck.png","diffusespecp");
        cub.trans = new float[] {1,0,0};
        roottree.linkchild(cub);

        // build a sphere
        Tree sph =  buildsphere("asphere",.25f,"maptestnck.png","diffusespecp");
        sph.trans = new float[] {-1,0,0};
        roottree.linkchild(sph);

        // some more
        cub = new Tree(cub);
        cub.trans = new float[] {1,0,3};
        roottree.linkchild(cub);

        cub = new Tree(cub);
        cub.trans = new float[] {-1.0f/3.0f,0,3};
        cub.rotvel = new float[] {0,(float)(2.0f*Math.PI/10),0};
        roottree.linkchild(cub);

        sph = new Tree(sph);
        sph.trans = new float[] {-1,0,3};
        roottree.linkchild(sph);

        sph = new Tree(sph);
        sph.trans = new float[] {1.0f/3.0f,0,3};
        sph.rotvel = new float[] {0,(float)(2.0f*Math.PI/10.0f),0.0f};
        roottree.linkchild(sph);
        onResize();
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.getContext().show3DUI();
            }
        });
    }

    @Override
    public void proc() {
        gain = Utils.getContext().getGain();
        convergence = Utils.getContext().getConv();
        // get input
        InputState ir = Input.getResult();

        // proc everything
        roottree.proc();
        roottreen.proc();

        // toggle viewports
        /*if (input.key == "t".charCodeAt(0)) {
            toggleFlycam();
        }*/

        // move a couple of objects forward and back sinusoidally using ang
        leftObj.trans[2] = leftObjcenterTrans[2] + angAmp*(float)Math.sin(ang);

        rightObj.trans[2] = rightObjcenterTrans[2] - angAmp*(float)Math.sin(ang);

        // flycam either the FB's or the display viewport
        if (flycamFBn) {
            frametexnvp.doflycam(ir); // modify the trs of FB1 vp by flying
        } else {
            ViewPort.mainvp.doflycam(ir); // modify the trs of display vp by flying
        }


    }

    @Override
    public void draw() {
        // draw to frame buffers and main screen

        // draw to each frame buffer FB n
        // save viewport FB n's trans

        // this
        //var oldtrans = [0,0,0];
        //vec3.copy(oldtrans,framebuffer4.frametexnvp.trans);

        // or this
        float[] oldtrans = frametexnvp.trans.clone();

        // gain
        float[] transVec = new float[] {gain*2.0f/numTargets,0,0};

        // get viewport orientation matrix and move transVec to world space
        float[] tm = new float[16]; // matrix to spread cameras apart
        Matrix.setIdentityM(tm,0);
        Tree.buildtransrotscale(tm,frametexnvp.trans, frametexnvp.rot, null);
        NuMath.transformMat4Vec(transVec,transVec,tm);

        // now run through each render target and use appropriate viewport for each one for draw pass
        float nt = numTargets; // number of targets
        float gc = convergence*gain*2.0f/numTargets;
        for (int i = 0; i < nt; ++i) {
            FrameBufferTexture rt = frametexn.get(i); // render target
            // place cameras in camera space and convert to world space
            float scl = i - nt*.5f + .5f; // 0 : -.5,.5 : -1,0,1 : -1.5,-.5,.5,1.5 etc...
            float conv = (i - nt*.5f +.5f)*gc;
            float[] trans = frametexnvp.trans;
            frametexnvp.xo = conv;
            NuMath.scale(trans,transVec,scl);
            NuMath.add(trans,trans,oldtrans);
            // draw FBn scene
            frametexnvp.target = rt;
            frametexnvp.beginscene();
            roottreen.draw();

            // draw pixel perfect scene
            pixelPerfectVp.target = rt;
            pixelPerfectVp.beginscene();
            roottreenpp.draw();
        }
        frametexnvp.trans = oldtrans;
        //frametexnvp.xo = 0; // TODO: add xo and yo to viewport class


        // MAIN SCREEN
        // draw main vp
        ViewPort.mainvp.beginscene();
        if (fbnPlaneXY != null) {
            if (Main3D.viewAsp >= 1.0f) {
                fbnPlaneXY.scale = new float[]{Main3D.viewAsp, -1, 1};
            } else {
                fbnPlaneXY.scale = new float[]{1, -1/Main3D.viewAsp, 1};
            }
        }
        roottree.draw(); // depends on FB 1,2,3

        // animate the ang
        ang += angStep;
        if (ang > 2*Math.PI)
            ang -= 2*Math.PI;

    }

    @Override
    public void onResize() {
        int w = Main3D.viewWidth;
        int h = Main3D.viewHeight;
        Log.e(TAG,"the state 'Framebuffer4' resize event " + w + " " + h);
        //exitRenderTargets();
        //initRenderTargets();
        for (int i=0;i<numTargets;++i) {
            FrameBufferTexture rt = frametexn.get(i);
            rt.resize(w,h);
        }
        //ViewPort.mainvp = new ViewPort();
        if ("interleave4".equals(mixShader)) { // need resolution uniforms for this shader
            fbnPlaneXY.mat.put("resolution",new float[] {Main3D.viewWidth,Main3D.viewHeight});
        }

    }

    @Override
    public void exit() {
        globalmat.put("specpow",oldspecpow);

        // show everything in logs
        Log.i(TAG,"=== roottree log ===");
        roottree.log();
        Log.i(TAG,"=== roottree pp log ===");
        roottreenpp.log();
        Log.i(TAG,"=== roottreen log ===");
        roottreen.log();
        Log.i(TAG,"before roottree logs");

        // log resources used before glfree
        GLUtil.logrc();

        // cleanup scenes
        roottree.glFree();
        roottree = null;
        roottreen.glFree();
        roottreen = null;
        roottreenpp.glFree();
        roottreenpp = null;

        // cleanup render targets
        exitRenderTargets();

        Log.i(TAG,"after roottree glfree");

        // log resources used after glfree, should be empty (execpt for the global resources, fonts etc.)
        GLUtil.logrc();

        // put everything the way it was
        SimpleUI.clearbuts("viewport");
        ViewPort.mainvp = new ViewPort();
        SimpleUI.clearbuts("framebuffer4");
        Log.e(TAG,"done clearing framebuffer4");

        Log.i(TAG,"exiting webgl framebuffer4");
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.getContext().hide3DUI();
            }
        });
    }
}
