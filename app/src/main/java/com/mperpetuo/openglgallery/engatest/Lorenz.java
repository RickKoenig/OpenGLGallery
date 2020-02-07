package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.FrameBufferTexture;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Lights;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;

public class Lorenz extends State {

    boolean INVERT = true;
    private static final String TAG = "Lorenz";

    int NUM_LORENZ = 50;
    //int NUM_LORENZ = 1;
    //int NUM_LORENZ = 250;

    //Texture shadowtexture;
    FrameBufferTexture shadowtexture;

    float lightdist = 20;
    float[] lightloc = new float[] {0,lightdist,-lightdist};

    Tree roottree;
    Tree vectormaster;
    Tree vectormasterc;
    ArrayList<Tree> vectorlist = new ArrayList<>();

    //Sim sim = new CircleSim();
    Sim sim = new LorenzSim();

    static int vectorversion = 2;

    private ViewPort shadowvp;
    private ViewPort mvp;

    //int vectorversion = 2;

    Tree makevectormaster() {
        Tree arrowmaster = new Tree("arrow");
        float[] c = {
            (float)Math.random(),
            (float)Math.random(),
            (float)Math.random(),
            1.0f
        };
        // a modelpart
        //var fontAtree = buildsphere("fontAtree",.1,"Asphalt.png","diffusespecp");
        //fontAtree = buildconexz2t("tail",.125,.5,"maptestnck.png","shadowmap","shadowmapuse");
        Tree atree = ModelUtil.buildconexz2t("tail",.125f,.5f,"maptestnck.png","shadowmap","shadowmapuse");
        atree.trans = new float[3];
        //fontAtree.rot = [0,0,-Math.PI/2];
        atree.mat.put("color",c);//color = c;
        arrowmaster.linkchild(atree);
        //fontAtree = buildcylinderxz2t("mid",.0625,.5,"Asphalt.png","shadowmap","shadowmapuse");
        atree = ModelUtil.buildcylinderxz2t("mid",.0625f,.5f,"Asphalt.png","shadowmap","shadowmapuse");
        atree.trans = new float[] {0,.25f,0};
        //fontAtree.rot = [0,0,-Math.PI/2];
        atree.mat.put("color",c);
        arrowmaster.linkchild(atree);
        //fontAtree = buildconexz2t("head",.125,.5,"maptestnck.png","shadowmap","shadowmapuse");
        atree = ModelUtil.buildconexz2t("head",.125f,.5f,"maptestnck.png","shadowmap","shadowmapusec");
        atree.trans = new float[] {0,.75f,0};
        //fontAtree.rot = [0,0,-Math.PI/2];
        atree.mat.put("color",c);
        arrowmaster.linkchild(atree);
        //fontAtree.trans = [0,0,0];
        //fontAtree.rotvel = [.01,.05,0];
        return arrowmaster;
    }

    Tree makevectormasterc() {
        Tree arrowmaster = null;
        switch(vectorversion) {
        case 1:
            arrowmaster = new Tree("arrow");
            float[] c = {
                (float)Math.random(),
                (float)Math.random(),
                (float)Math.random(),
                1.0f
            };
            // a modelpart
            //var fontAtree = buildsphere("fontAtree",.1,"Asphalt.png","diffusespecp");
            //fontAtree = buildconexz2t("tailc",.125,.5,"maptestnck.png","shadowmap","shadowmapusec");
            Tree atree = ModelUtil.buildconexz2t("tailc",.125f,.5f,"maptestnck.png","shadowmap","shadowmapusec");
            atree.trans = new float[] {0,-.5f,0};
            //fontAtree.rot = [0,0,-Math.PI/2];
            atree.mat.put("color",c);
            arrowmaster.linkchild(atree);
            //fontAtree = buildcylinderxz2t("midc",.0625,.5,"Asphalt.png","shadowmap","shadowmapusec");
            atree = ModelUtil.buildcylinderxz2t("midc",.0625f,.5f,"Asphalt.png","shadowmap","shadowmapusec");
            atree.trans = new float[] {0,-.25f,0};
            //fontAtree.rot = [0,0,-Math.PI/2];
            atree.mat.put("color",c);
            arrowmaster.linkchild(atree);
            //fontAtree = buildconexz2t("headc",.125,.5,"maptestnck.png","shadowmap","shadowmapusec");
            atree = ModelUtil.buildconexz2t("headc",.125f,.5f,"maptestnck.png","shadowmap","shadowmapusec");
            atree.trans = new float[] {0,.25f,0};
            //fontAtree.rot = [0,0,-Math.PI/2];
            atree.mat.put("color",c);
            arrowmaster.linkchild(atree);
            //fontAtree.trans = [0,0,0];
            //fontAtree.rotvel = [.01,.05,0];
            break;
        case 2:
            arrowmaster = ModelUtil.buildpaperairplane("paperairplane","cvert");
            //arrowmaster.mat.color = [1,0,0,1];
            break;
        }
        return arrowmaster;
    }

    void changeavector(Tree t,float[] pos,float[] dir,float scl) {
        //t.trans = vec3.clone(pos);
        t.trans = new float[] {pos[0],pos[1],pos[2]};
        //t.rot = [0,0,r];
        if (dir == null)
            return;

        //if (t.qrot == null)
        //    t.qrot = new float[4];
        //float len = Quat.dir2quat(t.qrot,dir);
        if (t.rot == null)
            t.rot = new float[3];
        float len = NuMath.dir2rot(t.rot,dir);
        //t.rot = [0,0,ang];
        float s = len*scl;//*NuMath.length(dir);
        //float s = scl*len;
        t.scale = new float[] {s,s,s};
        //t.transvel = [5*Math.cos(a),5*Math.sin(a),0];
        //t.rot = [0,0,a];
        //t.cnt = 5;
        //t.userproc = arrowuserproc;

        }

    Tree makeavector(float[] pos,float[] dir,boolean center,float[] c) {
        if (c == null)
            c = new float[] {1,1,1,1};
        //++arrowcnt;
        Tree t;
        if (center)
            t = new Tree(vectormasterc);
        else
            t = new Tree(vectormaster);
        changeavector(t,pos,dir,.1f);
        if (t.children.size() > 0) {
            Tree tail = t.children.get(0);
            tail.children.get(0).mat.put("color",c);
            tail.children.get(1).mat.put("color",c);
            Tree mid = t.children.get(1);
            mid.children.get(0).mat.put("color",c);
            mid.children.get(1).mat.put("color",c);
            mid.children.get(2).mat.put("color",c);
            Tree head = t.children.get(2);
            head.children.get(0).mat.put("color",c);
            head.children.get(1).mat.put("color",c);
        }
        return t;
    }

    // where you are determines how fast you move
    abstract class Sim {
        abstract float[] sim(float[] pos); // returns vel
    }

    class CircleSim extends Sim {
        float step = .01f;
        @Override
        float[] sim(float[] pos) {
            float[] vel = new float[3];
            vel[0] = -step*pos[1];
            vel[1] = step*pos[0];
            vel[2] = -step*pos[2];
            return vel;
        }
    }

    class LorenzSim extends Sim {
        float sig = 10.0f;
        float beta = 8.0f/3.0f;
        float rho = 28.0f;
        float step = .01f;
        @Override
        float[] sim(float[] pos) {
            float[] vel = new float[3];
            vel[0] = sig*(pos[1]-pos[0]);
            vel[1] = pos[0]*(rho-(pos[2]+20)) - pos[1];
            vel[2] = pos[0]*pos[1] - beta*(pos[2]+20);
            NuMath.scale(vel,vel,step);
            return vel;
        }
    }


    void procsim1(Sim f) {
        int i,n = vectorlist.size();
        Tree t;
        for (i=0;i<n;++i) {
            t = vectorlist.get(i);
            t.transvel = f.sim(t.trans);
            //Log.e(TAG,"trans = " + t.trans[0] + " " + t.trans[1] + " " + t.trans[2] + " vel " + t.transvel[0] + " " + t.transvel[1] + " " + t.transvel[2]);
            float[] dir = new float[3];
            NuMath.scale(dir,t.transvel,2);
            changeavector(t,t.trans,dir,3);
        }
    }

/*
        state19.load = function() {
        //if (!gl)
        //	return;
        preloadimg("../common/sptpics/maptestnck.png");
        preloadimg("fortpoint/Asphalt.png");
        preloadimg("../common/sptpics/wonMedal.png");
        };
        */
    @Override
    public void init() {
        //ViewPort.mainvp.clearcolor = new float[] {.25f,.25f,0,0};

        GLUtil.checkGlError("start of state19 init 1");
        Log.e(TAG,"entering webgl state19");
        //vectorlist = [];

        int shadowmapres = 2048;
        shadowtexture = FrameBufferTexture.createTexture("shadowmap",shadowmapres,shadowmapres);

        /*
// build render target
        var shadowmapres = 2048;
        shadowtexture = FrameBufferTexture.createtexture("shadowmap",shadowmapres,shadowmapres);

// shadow viewport
        state19.shadowvp = {
        target:shadowtexture,
        clearflags:gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT,
        clearcolor:[1,1,1,1],                    // Set clear color to yellow, fully opaque
        //	mat4.create();
        "trans":vec3.clone(state19.lightloc),
        "rot":[Math.PI/4,0,0], // part of lightdir
        //"scale":[1,1,1],
        near:.1,
        far:10000.0,
        zoom:1,
        asp:1,
        inlookat:false,
        isshadowmap:true
        };
*/
// shadow viewport
        shadowvp = new ViewPort();
        shadowvp.target = shadowtexture;
        if (INVERT)
            shadowvp.clearcolor = new float[] {0,0,0,1}; // Set clear color to yellow, fully opaque
        else
            shadowvp.clearcolor = new float[] {1,1,1,1}; // Set clear color to yellow, fully opaque
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
        mvp.setupViewportUI(1.0f/4.0f);

// main viewport for state 19
//        mvp = new ViewPort();
        //state19.mvp = {
        //clearflags:gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT,
        //clearcolor:[.15,.25,.75,1],                    // Set clear color to yellow, fully opaque
        mvp.clearcolor = new float[]{.15f,.25f,.75f,1};

        //trans:[-21,-3,3],
        mvp.trans = new float[] {-21,-3,3};
        //	"trans":vec3.clone(state19.lightloc),
        //"rot":[Math.PI/4,0,0], // part of lightdir
        mvp.rot = new float[] {NuMath.PI/4.0f,0,0};
        mvp.near = .05f;
        //"scale":[1,1,1],
/*        near:.1,
        far:10000.0,
        zoom:1,
        asp:gl.asp,
        inlookat:true */
        //};

// build a master vector
        vectormaster = makevectormaster();
        vectormasterc = makevectormasterc();

//// build the main screen scene

        roottree = new Tree("root");


        // back plane
        Tree atree1 = ModelUtil.buildplanexy2t("planexy1",20,20,"maptestnck.png","shadowmap","shadowmapuse"); // tex
        //Tree atree1 = ModelUtil.buildplanexy("planexy1",20,20,"maptestnck.png","tex"); // tex
        atree1.trans = new float[] {0,0,20};
        atree1.flags |= Tree.FLAG_DONTCASTSHADOW;
        roottree.linkchild(atree1);

        // ground plane
        Tree atree1b = ModelUtil.buildplanexz2t("planexz1",20,20,"maptestnck.png","shadowmap","shadowmapuse"); // tex
        //Tree atree1b = ModelUtil.buildplanexz("planexz1",20,20,"maptestnck.png","tex"); // tex
        atree1b.trans = new float[] {0,-20,0};
        atree1b.flags |= Tree.FLAG_DONTCASTSHADOW;
        roottree.linkchild(atree1b);

        int i;
        for (i=0;i<NUM_LORENZ;++i) {
            float sz = 10;
            float x = 2*sz*(float)Math.random()-sz;
            float y = 2*sz*(float)Math.random()-sz;
            float z = 2*sz*(float)Math.random()-sz;
            float[] pos = new float[] {x,y,z};
            float[] dir = new float[] {1,0,0};
            float[] color = new float[] {(float)Math.random()*.5f+.5f,(float)Math.random()*.5f+.5f,(float)Math.random()*.5f+.5f,1};
            Tree as = makeavector(pos,dir,true,color);
            //Tree as = makeavector([x,y,z],[1,0,0],true,[Math.random()*.5+.5,Math.random()*.5+.5,Math.random()*.5+.5,1]);
            //as.rotvel = [Math.random(),Math.random(),Math.random()];
            //as.scale = [4,4,4];
            roottree.linkchild(as);
            vectorlist.add(as);
        }
        // the light
        Tree atree4 = ModelUtil.buildsphere("sph4",.2f,null,"flat"); // this is where the light is for (point) shadowcasting
        ((Model)atree4.mod).mat.put("color",new float[] {1,1,.5f,1});
        atree4.trans = lightloc;//vec3.clone(state19.lightloc);
        atree4.flags |= Tree.FLAG_DONTCASTSHADOW;
        atree4.flags |= Tree.FLAG_DIRLIGHT;
        atree4.rot = new float[] {NuMath.PI/4.0f,0,0};
        Lights.addlight(atree4);
        roottree.linkchild(atree4);

        // set these in viewport after we get objects of interest
        boolean fromLight = false;
        if (fromLight) { // from light
            mvp.trans = lightloc.clone();
            mvp.rot = new float[] {NuMath.PI/4,0,0}; // part of lightdir
        } else { // follow an object
            mvp.lookat = vectorlist.get(0);
            mvp.inlookat = true;
            mvp.camattach = vectorlist.get(1);
            //mvp.incamattach = false;
        }



//// set the lights (directional)
        //lights.wlightdir = vec3.fromValues(0,-.7071,.7071);  // part of lightdir

//// set the camera
        //mainvp.trans = vec3.clone(state19.lightloc);
        //vec3.scale(mainvp.trans,mainvp.trans,.5);
        //mainvp.trans = [-21,-3,3];
        //mainvp.inlookat = 1;
//	mainvp.rot = [Math.PI/4,0,0]; // part of lightdir
        // build the scene

        //state19.mvp.lookat = vectorlist[0];
        //state19.mvp.inlookat = true;
        GLUtil.checkGlError("end of state19 init");
        //};

    }


    @Override
    public void proc() {
        int i;
        InputState ir = Input.getResult();
        int simSteps = 10;
        for (i=0;i<simSteps;++i) {
            //procsim1(circlesim);
            procsim1(sim);
            roottree.proc();
        }
        mvp.doflycam(ir);
    }

    @Override
    public void draw() {
        shadowvp.beginscene();
        roottree.draw();
        mvp.beginscene();
        roottree.draw();
    }

/*
        state19.proc = function() {
        checkglerror("lorenz attractor proc start check gl error");
        state19.mvp.asp = gl.asp;
//	if (!gl)
//		return;

        // proc the simulation here

        // lookat the 0th arrow/vector
        doflycam(state19.mvp); // modify the trs of the vp

        // draw to shadowmap
        beginscene(state19.shadowvp);
        checkglerror("lorenz attractor draw start check gl error");
        backgroundtree.draw();
        checkglerror("lorenz attractor draw end check gl error");

        // draw main scene
        beginscene(state19.mvp);
        backgroundtree.draw();
        checkglerror("lorenz attractor proc end check gl error");
        };
        */

    @Override
    public void exit() {
        SimpleUI.clearbuts("viewport");
        // reset main ViewPort to default since we messed with it
        //ViewPort.mainvp = new ViewPort();
        GLUtil.checkGlError("lorenz attractor exit start check gl error");
        roottree.log();
        GLUtil.logrc();
        Log.i(TAG,"after backgroundtree glfree\n");

        // free everything
        roottree.glFree();
        shadowtexture.glFree();
        vectormaster.glFree();
        vectormasterc.glFree();

        // show freed state
        GLUtil.logrc();
        roottree = null;
        //state19.mvp.lookat = null;
        //state19.mvp.inlookat = false;
        Log.i(TAG,"exiting Lorenz");
        GLUtil.checkGlError("lorenz attractor exit end check gl error");

        switch(vectorversion) {
        case 1:
            vectorversion = 2;
            break;
        case 2:
            vectorversion = 1;
            break;
        }
    }

}