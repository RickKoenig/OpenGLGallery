package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.old_input.InputResult;

public class Shapes extends State {
    final String TAG = "Shapes";

    Tree roottree; // top level tree
    Tree paperAirplane;

    // use this for now before lighting is brought back online
    //float[] elightdir;

    final float gap = 1.25f; // radius?
    final float back = -3.5f;

    float scrollX,scrollY;
    int frame;

    @Override
    public void init() {
        Log.i(TAG, "entering basic");
        frame = 0;
        // main scene
        roottree = new Tree("backgroundtree");
        roottree.trans = new float[3];

        // build some child shapes
        //Texture.globaltexflags &= ~Texture.FLAG_CLAMPUV;
        //Texture.setWrapMode();

        Tree atree;
        //// 1st row, basic shapes
        // different tiling
        float us = ModelUtil.spherepatchu;
        float vs = ModelUtil.spherepatchv;
        ModelUtil.spherepatchu = 1;
        ModelUtil.spherepatchv = 1;


        // plane
        atree =  ModelUtil.buildplanexy("aplane",4.0f/9.0f, 1, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.mod.flags |= Model.FLAG_DOUBLESIDED;
        atree.trans = new float[] {-5*gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // prism
        atree =  ModelUtil.buildprism("aprism",new float[] {4.0f/9.0f,1.0f,1.0f/9.0f}, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.trans = new float[] {-3*gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // sphere
        atree =  ModelUtil.buildsphere("asphere", 1, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.trans = new float[] {-gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // torus
        atree =  ModelUtil.buildtorusxz("atorus",.75f,.25f, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.trans = new float[] {gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // cylinder
        atree =  ModelUtil.buildcylinderxz("acylinder",1,1, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.trans = new float[] {3*gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // cone
        atree =  ModelUtil.buildconexz("acone",1,1, "maptestnck.png", "diffusespecp"); // name, size, texture, generic texture shader
        atree.trans = new float[] {5*gap,2*gap,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        //// 2nd row, some more advanced stuff, 2 textures, model2 (multi material), lighting etc.

        // day sphere
        //Texture.globaltexflags &= ~Texture.FLAG_CLAMPUV;
        //Texture.setWrapMode();
        atree =  ModelUtil.buildsphere("daySphere", 1, "light.jpg", "tex"); // name, size, texture, generic texture shader
        atree.trans = new float[] {-5*gap,0,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // night sphere
        //Texture.globaltexflags &= ~Texture.FLAG_CLAMPUV;
        //Texture.setWrapMode();
        atree =  ModelUtil.buildsphere("nightSphere", 1, "dark.jpg", "tex"); // name, size, texture, generic texture shader
        atree.trans = new float[] {-3*gap,0,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // day/night sphere
        atree =  ModelUtil.buildsphere2t("dayNightSphere", 1, "light.jpg","dark.jpg", "daynight"); // name, size, texture1, texture2, a 2 texture shader
        atree.trans = new float[] {-gap,0,0};
        atree.rotvel = new float[] {0,NuMath.TWOPI/8.0f,0};
        //elightdir = new float[] {0,0,-1};
        //fontAtree.mod.mat.put("elightdir",elightdir);
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree

        // paper airplane
        paperAirplane =  ModelUtil.buildpaperairplane("apaperplane", "cvert"); // name, size, texture, generic texture shader
        //paperAirplane = ModelUtil.buildprism("apaperairplane",new float[] {1,1,1},"maptestnck.png","tex");
        paperAirplane.trans = new float[] {gap,0,0};
        paperAirplane.scale = new float[] {2,2,2};
        paperAirplane.rotvel = new float[] {NuMath.TWOPI/8.0f,0,0};
        roottree.linkchild(paperAirplane); // link to and pass ownership to backgroundtree



        // put tiling back to defaults
        ModelUtil.spherepatchu = us;
        ModelUtil.spherepatchv = vs;

        // add a dir light
        Tree lt = new Tree("dirlight");
        //lt.rot = [0,0,0];
        lt.rotvel = new float[]{1,0,0};
        lt.flags |= Tree.FLAG_DIRLIGHT;
        Lights.addlight(lt);
        roottree.linkchild(lt);


        //Texture.globaltexflags |= Texture.FLAG_CLAMPUV;


        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        ViewPort.mainvp.trans = new float[] {0,0,back};
    }

    @Override
    public void proc() {
        // get input, some
        InputState ir = Input.getResult();
        // proc
        if (ir.touch > 0) {
            scrollX += ir.dfx;
            scrollY += ir.dfy;
            scrollX = Utils.range(-1,scrollX,1);
            scrollY = Utils.range(-.5f,scrollY,.5f);
            //Log.e(TAG,"scroll = " + scrollX + " " + scrollY);
            roottree.trans[0] = 8 * scrollX;
            roottree.trans[1] = 8 * scrollY;
        }
        /*
        float rot = frame*Main3D.frametime*NuMath.TWOPI/8.0f*3.0f; // faster light rotation than object rotation
        boolean doY = false;
        if(doY) {
            elightdir[0] = (float) Math.sin(rot);
            elightdir[1] = 0;
            elightdir[2] = (float) Math.cos(rot);
        } else {
            elightdir[0] = 0;
            elightdir[1] = (float) Math.sin(rot);
            elightdir[2] = (float) Math.cos(rot);
        }*/
        roottree.proc(); // do animation and user proc if any
        ++frame;
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
        // reset main ViewPort to default
        ViewPort.mainvp = new ViewPort();
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
