package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Lights;
import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Mesh;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Texture;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;

public class ShaderTest extends State {

    private static final String TAG = "ShaderTest";
    // test webgl
    Tree roottree;
    Tree tree0, tree1, tree2, tree3, tree4, tree5, tree6, tree7, tree8, tree9, tree10, tree11, tree12; // many different shaders
    Tree tree13, tree14, tree15, tree16; // specular
    Tree tree17, tree18; // tree textures
    Tree tree19, tree20; // tree texture override
    Tree tree21, tree22; // change stuff on model
    Tree tree23;    // world
    Tree tree24; // no pers
    Tree lighttre; // light
    //float state11ang;
    static boolean closeToSphere = false;

    String state11texlist[] = {
        "maptestnck.png",
        "panel.jpg",
        "wonMedal.png",
        "coin_logo.png"
    };

    int frame;
    int oldcnt;

    ArrayList<Texture> cachedgltextures;

    void loadcachedtextures() {
        cachedgltextures = new ArrayList<>();
        int i;
        for (i=0;i<state11texlist.length;++i)
            cachedgltextures.add(Texture.createTexture(state11texlist[i]));
    }

    void freecachedtextures() {
        int i;
        for (i=0;i<cachedgltextures.size();++i)
            cachedgltextures.get(i).glFree();
        cachedgltextures = null;
    }

    public void init() {
        String curscene = "Skansen";
        //String curscene = "Footballfield";
        //String curscene = "cubemap_mountains.jpg";
        String curscenejpg = curscene;
        String cubcurscene = "CUB_" + curscenejpg;
        frame = 0;
        oldcnt = -1;
        Log.i(TAG,"entering webgl state11");
        loadcachedtextures();

        //state11ang = 0;
        roottree = new Tree("root");

        //tree3 = buildprism("aprism2",[1,1,1],"maptestnck.png","tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildskybox("aprism3",[1,1,1],"cube2.jpg","tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildskybox("aprism3",[1,1,1],"cubicmap.jpg","tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildskybox("aprism3",[1,1,1],"Footballfield.jpg","tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildskybox("aprism3",[1,1,1],"cube.jpg","tex"); // helper, builds 1 prism returns a Tree2
        tree3 = ModelUtil.buildskybox("aprism3",new float[] {1,1,1},curscene,"tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildskybox("aprism3",[1,1,1],"cubemap_mountains.jpg","tex"); // helper, builds 1 prism returns a Tree2
        //tree3 = buildprism("aprism3",[1,1,1],"POSY_cube.jpg","tex"); // helper, builds 1 prism returns a Tree2
        //tree3.trans = [2,-2.5,0];
        roottree.linkchild(tree3);

        //globaltexflags = textureflagenums.CLAMPU;
        tree9 = ModelUtil.buildprism("aprism9",new float[] {1,1,1},"maptestnck.png","diffusespecv"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree9.trans = new float[] {-10,7.5f,0};
        tree9.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree9);


        tree10 = ModelUtil.buildsphere("asphere10",1,"maptestnck.png","diffusespecv");
        tree10.trans = new float[] {-10,2.5f,0};
        tree10.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree10);

        //globaltexflags = textureflagenums.CLAMPU;
        tree0 = ModelUtil.buildprism("aprism",new float[] {1,1,1},"maptestnck.png","diffusev"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree0.trans = new float[] {-6,7.5f,0};
        tree0.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree0);


        tree1 = ModelUtil.buildsphere("asphere",1,"maptestnck.png","diffusev");
        tree1.trans = new float[] {-6,2.5f,0};
        tree1.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree1);

        //globaltexflags = textureflagenums.CLAMPU;
        tree13 = ModelUtil.buildprism("aprism13",new float[] {1,1,1},"maptestnck.png","diffusespecp"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree13.trans = new float[] {-10,-2.5f,0};
        tree13.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree13);


        tree14 = ModelUtil.buildsphere("asphere14",1,"maptestnck.png","diffusespecp");
        tree14.trans = new float[] {-10,-7.5f,0};
        tree14.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree14);

        //globaltexflags = textureflagenums.CLAMPU;
        tree15 = ModelUtil.buildprism("aprism15",new float[] {1,1,1},"maptestnck.png","diffusep"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree15.trans = new float[] {-6,-2.5f,0};
        tree15.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree15);


        tree16 = ModelUtil.buildsphere("asphere16",1,"maptestnck.png","diffusep");
        tree16.trans = new float[] {-6,-7.5f,0};
        tree16.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree16);

        //tree2 = buildprism("aprism2",[1,1,1],"maptestnck.png","tex"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = textureflagenums.CLAMPV;
        //tree2 = buildprism("aprism2",[1,1,1],"POSX_cube2.jpg","tex"); // helper, builds 1 prism returns a Tree2
        tree2 = ModelUtil.buildprism6("aprism2",new float[] {1,1,1},curscene,"tex"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree2.trans = new float[] {-2,7.5f,0};
        roottree.linkchild(tree2);

        tree4 = ModelUtil.buildsphere("asphere2",1,cubcurscene,"envmapv"); // use cubcurscene texture and shader
        tree4.trans = new float[] {2,2.5f,0};
        tree4.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree4);

        tree5 = ModelUtil.buildsphere("asphere3",1,cubcurscene,"envmapp"); // use cubcurscene texture and shader
        tree5.trans = new float[] {6,2.5f,0};
        tree5.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree5);

        tree8 = ModelUtil.buildsphere("asphere7",1,cubcurscene,"cubemap"); // use cubcurscene texture and shader
        tree8.trans = new float[] {-2,2.5f,0};
        //tree8.rotvel = [.02,.1,0];
        roottree.linkchild(tree8);

        tree6 = ModelUtil.buildprism("aprism4",new float[] {1,1,1},cubcurscene,"envmapv"); // use cubcurscene texture and shader
        tree6.trans = new float[] {2,7.5f,0};
        tree6.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree6);

        //tree7 = buildprism("aprism5",[1,1,1],"panel.jpg","tex"); // use cubcurscene texture and shader
        tree7 = ModelUtil.buildprism("aprism5",new float[] {1,1,1},cubcurscene,"envmapp"); // use cubcurscene texture and shader
        tree7.trans = new float[] {6,7.5f,0};
        tree7.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree7);

        tree11 = ModelUtil.buildsphere("asphere11",1,cubcurscene,"envmapghostv"); // use cubcurscene texture and shader
        //tree11.mod.flags |= modelflagenums.HASALPHA;
        tree11.trans = new float[] {10,2.5f,0};
        tree11.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree11);

        //tree7 = buildprism("aprism5",[1,1,1],"panel.jpg","tex"); // use cubcurscene texture and shader
        tree12 = ModelUtil.buildprism("aprism12",new float[] {1,1,1},cubcurscene,"envmapghostv"); // use cubcurscene texture and shader
        //tree12.mod.flags |= modelflagenums.HASALPHA;
        tree12.trans = new float[] {10,7.5f,0};
        tree12.rotvel = new float[] {.02f,.1f,0};
        roottree.linkchild(tree12);


        tree17 = ModelUtil.buildsphere("asphere17",1,null,"flat");
        tree17.mat.put("color",new float[] {1,0,0,.45f}); // treecolor
        //tree17.mod.hasalpha = true;
        tree17.mod.flags |= Model.FLAG_HASALPHA;
        tree17.trans = new float[] {-2,-2.5f,0};
        tree17.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree17);

        tree18 = ModelUtil.buildsphere("asphere18",1,null,"flat");
        tree18.mat.put("color",new float[] {0,1,0,.55f}); // treecolor
        //tree18.mod.hasalpha = true;
        tree18.mod.flags |= Model.FLAG_HASALPHA;
        tree18.trans = new float[] {-2,-7.5f,0};
        tree18.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree18);


        tree19 = ModelUtil.buildsphere("asphere19",1,"maptestnck.png","tex");
        tree19.trans = new float[] {2,-2.5f,0};
        tree19.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree19);

        tree20 = new Tree(tree19);
        tree20.settexture("panel.jpg");
        tree20.trans = new float[] {2,-7.5f,0};
        tree20.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree20);


        tree21 = ModelUtil.buildsphere("asphere21",1,"maptestnck.png","tex");
        tree21.trans = new float[] {6,-2.5f,0};
        tree21.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree21);

        tree22 = ModelUtil.buildsphere("asphere22",1,"maptestnck.png","tex");
        tree22.trans = new float[] {6,-7.5f,0};
        //tree22.rotvel = [.15,.4,0};
        roottree.linkchild(tree22);

        float psu = ModelUtil.spherepatchu;
        float psv = ModelUtil.spherepatchv;
        ModelUtil.spherepatchu = 1;
        ModelUtil.spherepatchv = 1;
        tree23 = ModelUtil.buildsphere2t("asphere23",1,"light.jpg","dark.jpg","daynight");
        //tree23.mod.mat.blend = .9175;
        tree23.trans = new float[] {10,-2.5f,0};
        tree23.rotvel = new float[] {.15f,.4f,0};
        ModelUtil.spherepatchu = psu;
        ModelUtil.spherepatchv = psv;
        //tree21.rotvel = [.15,.4,0];
        roottree.linkchild(tree23);

        //globaltexflags = textureflagenums.CLAMPU;
        tree23 = ModelUtil.buildprism("aprism23",new float[] {1,1,1},"maptestnck.png","nopers"); // helper, builds 1 prism returns a Tree2
        //globaltexflags = 0;
        tree23.trans = new float[] {10,-7.5f,0};
        tree23.rotvel = new float[] {.15f,.4f,0};
        roottree.linkchild(tree23);

        lighttre = new Tree("adirlight");
        lighttre.flags |= Tree.FLAG_DIRLIGHT;
        lighttre.rotvel = new float[] {0,1,0};
        Lights.addlight(lighttre);
        roottree.linkchild(lighttre);

        if (closeToSphere) {
            ViewPort.mainvp.trans = new float[] {5,-2.9f,-3}; // flycam
            ViewPort.mainvp.zoom = 3;
            //closeToSphere = false;
        } else {
            ViewPort.mainvp.trans = new float[] {0,0,-10}; // flycam
            ViewPort.mainvp.zoom = 1;
            //closeToSphere = true;
        }
        //ViewPort.mainvp.rot = new float[] {0,0,0}; // flycam
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        ViewPort.mainvp.setupViewportUI(1.0f/64.0f);
    }

    public void proc() {
        //if (proc11once) {
        // change verts and uvs in tree21
        InputState ir = Input.getResult();

        Mesh uvmesh = ModelUtil.buildspheremesh(1);
        //uvmesh = {"uvs":uvmesh.uvs,"verts":uvmesh.verts};
        int i;
        for (i = 0; i < uvmesh.verts.length; i += 3) {
            uvmesh.verts[i] += Math.random() * .0625 - .01325;
            uvmesh.verts[i + 1] += Math.random() * .0625 - .01325;
            uvmesh.verts[i + 2] += Math.random() * .0625 - .01325;
        }
        for (i = 0; i < uvmesh.uvs.length; i += 2) {
            uvmesh.uvs[i] += Math.random() * .03125 - .015625;
            uvmesh.uvs[i + 1] += Math.random() * .03125 - .015625;
        }


        ModelUtil.spherefixpatch(uvmesh); // fix wrap around, remove gap

        //((Model)tree21.mod).changemesh(uvmesh);
        tree21.mod.changemesh(uvmesh);


        int cnt = (int) Math.floor(frame * Main3D.frametime);
        cnt = cnt%4;
        if (cnt != oldcnt) {
            ((Model)tree21.mod).changetexture(state11texlist[cnt]);
            oldcnt = cnt;
        }

        //proc11once = false;
        //}

        /*state11ang += 1.1 * Math.sqrt(2);
        if (state11ang > 2*Math.PI)
            state11ang -= 2*Math.PI;
        Log.e(TAG,"state11ang " + state11ang);*/
        ++frame;

        roottree.proc();
        ViewPort.mainvp.doflycam(ir); //  // modify the trs of the vp
    }

    public void draw() {
        //dolights(); // get some lights to eye space
        ViewPort.mainvp.beginscene();
        roottree.draw();
    }

    public void exit() {
        Lights.removeLight(lighttre);
        SimpleUI.clearbuts("viewport");
        ViewPort.mainvp = new ViewPort(); // reset main viewport
        roottree.log();
        GLUtil.logrc();
        Log.i(TAG,"after backgroundtree glfree");
        roottree.glFree();
        freecachedtextures();
        GLUtil.logrc();
        roottree = null;
        Log.i(TAG,"exiting webgl state11");
        closeToSphere = !closeToSphere;
    }

}
