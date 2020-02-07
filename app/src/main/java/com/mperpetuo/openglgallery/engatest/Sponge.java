package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.MainActivity;
import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Mesh;
import com.mperpetuo.openglgallery.enga.MeshA;
import com.mperpetuo.openglgallery.enga.Model2;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;

/**
 * Created by cyberrickers on 9/15/2016.
 */
public class Sponge extends State {
    final String TAG = "Sponge";

    SimpleUI.UIPrintArea levelarea;
    //SimpleUI.UIButton upBut;
    //SimpleUI.UIButton downBut;
    Tree roottree; // top level tree
    Tree atree; // a child tree with a model
    int curlevel;
    int maxlevel; // 4 for modern, 3 for galaxy SII

    int[] pow3 = {1, 3, 9, 27, 81, 243, 729};
    int[] trin;

    MeshA smesh;
    int curmeshidx;

    Mesh smeshfaceposx;
    Mesh smeshfacenegx;
    Mesh smeshfaceposy;
    Mesh smeshfacenegy;
    Mesh smeshfaceposz;
    Mesh smeshfacenegz;
    Mesh[] meshes6;

    float[] smeshfaceposx_verts = {
            0, 1, 1,
            0, 1, 0,
            0, 0, 1,
            0, 0, 0
    };
    float[] smeshfaceposx_uvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
    short[] smeshfaceposx_faces = {
            0, 1, 2,
            3, 2, 1
    };

    float[] smeshfacenegx_verts = {
            0,1,0,
            0,1,1,
            0,0,0,
            0,0,1
    };
    float[] smeshfacenegx_uvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
    short[] smeshfacenegx_faces = {
            0,1,2,
            3,2,1
    };

    float[] smeshfaceposy_verts = {
            0, 0, 0,
            1, 0, 0,
            0, 0, 1,
            1, 0, 1
    };
    float[] smeshfaceposy_uvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
    short[] smeshfaceposy_faces = {
            0,1,2,
            3,2,1
    };

    float[] smeshfacenegy_verts = {
            0,0,1,
            1,0,1,
            0,0,0,
            1,0,0
    };

    float[] smeshfacenegy_uvs = {
            0,0,
            1,0,
            0,1,
            1,1
    };
    short[] smeshfacenegy_faces = {
            0,1,2,
            3,2,1
    };

    float[] smeshfaceposz_verts = {
            0, 1, 0,
            1, 1, 0,
            0, 0, 0,
            1, 0, 0
    };
    float[] smeshfaceposz_uvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
    short[] smeshfaceposz_faces = {
            0,1,2,
            3,2,1
    };

    float[] smeshfacenegz_verts = {
            1, 1, 0,
            0, 1, 0,
            1, 0, 0,
            0, 0, 0
    };
    float[] smeshfacenegz_uvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };
    short[] smeshfacenegz_faces = {
            0,1,2,
            3,2,1
    };


    int[][] off60 = {
            {0,0,0},
            {0,0,1},
            {0,0,0},
            {1,0,0},
            {0,0,0},
            {0,1,0}
        };
    int [][] off62 = {
            {0,0,-1},
            {0,0,1},
            {-1,0,0},
            {1,0,0},
            {0,-1,0},
            {0,1,0}
    };

    float[][] colss6 = {
            {1, .125f, .125f, 1},
            {.125f, 1, .125f, 1},
            {.125f, .125f, 1, 1},
            {1, 1, .125f, 1},
            {1, .125f, 1, 1},
            {.125f, 1, 1, 1}
    };


    // return an value of binary has ones, base3 to base2 like
    int tobase3(int n,int ndig) {
        int ret = 0;
        int i;
        int p = 1;
        for (i=0;i<ndig;++i) {
            int m = n%3;
            //n = Math.floor(n/3);
            n = n/3;
            if (m == 1)
                ret += p;
            p *= 2;
        }
        return ret;
    }
    void getones(int lev) {
        int m = pow3[lev];
        trin = new int[m];
        int i;
        for (i=0;i<m;++i) {
            int r = tobase3(i,lev);
            trin[i] = r;
        }
    }

    boolean issolid(int[] pos) {
        int[] br = new int[3];
        int i;
        for (i=0;i<3;++i) {
            int t = pos[i];
            int b;
            if (t<0 || t>=trin.length)
                return false;
            b = trin[t];
            br[i] = b;
        }
        if ((br[0] & br[1]) != 0)
            return false;
        if ((br[0] & br[2]) != 0)
            return false;
        if ((br[1] & br[2]) != 0)
            return false;
        return true;
    }

    void clearsmesh() {
        smesh = new MeshA();
        smesh.verts = new ArrayList<>();
        smesh.uvs = new ArrayList<>();
        smesh.faces = new ArrayList<>();
        curmeshidx = 0;
    }

    void addsmesh(int[] off,Mesh msh) {
        int i,j;
        // 4 verts
        for (i=0;i<4;++i) {
            for (j=0;j<3;++j) {
                smesh.verts.add(msh.verts[3*i+j]+off[j]);
            }
        }
        // 4 uvs
        for (i=0;i<4;++i) {
            for (j=0;j<2;++j) {
                smesh.uvs.add(msh.uvs[2*i+j]);
            }
        }
        // 2 faces
        for (i=0;i<2;++i) {
            for (j=0;j<3;++j) {
                smesh.faces.add((short)(msh.faces[3*i+j]+curmeshidx));
            }
        }
        curmeshidx += 4;
    }

    MeshA makesponge(int level,int f) {
        int i,j,k;//,f;
        int m = pow3[level];
        getones(level);
        clearsmesh();
        for (k=0;k<=m;++k) {
            for (j=0;j<=m;++j) {
                for (i=0;i<=m;++i) {
//				for (f=0;f<6;++f) {
                    int[] off0 = new int[] {i+off60[f][0], j+off60[f][1], k+off60[f][2]};
                    int[] off1 = new int[] {i,j,k};
                    int[] off2 = new int[] {i+off62[f][0], j+off62[f][1], k+off62[f][2]};

                    // pz
                    //off1 = [i,j,k];
                    //off2 = [i,j,k-1];
                    if (issolid(off1) && !issolid(off2))
                        addsmesh(off0,meshes6[f]);
//				}
                }
            }
        }
        return smesh;
    }
/*
    Mesh makesponge(int level,int f) {
        return null;
    }

        private void updatelevel() {
            backgroundtree.glFree();
            backgroundtree = new Tree("backgroundtree");

        }
*/

    void updatelevel() {
        int i, f;

        levelarea.draw("Level : " + curlevel);
        //printareadraw(levelarea,"Level : " + curlevel);
        if (roottree == null)
            Log.e(TAG,"Wow, backgroundtree == null");
        roottree.glFree();
        roottree = new Tree("backgroundtree");
        i = curlevel;
        //for (i=cur;i<=3;++i) {
        for (int g = 0; g < 6; ++g) {
            // a modelpart
            Model2 amod = Model2.createmodel("spongemod m" + i + "s" + g);
            if (amod.refcount == 1) {
                //amod.setmesh(smeshtemplate);
                MeshA msha = makesponge(i, g);//,[0,-i*1.5,0]);
                Mesh msh = new Mesh(msha);
                amod.setmesh(msh);
                //amod.settexture("maptestnck.png");
                //amod.setshader("tex");
                int fs = 6000;
                for (f = 0; f < amod.faces.length; f += fs) {
                    int f2 = f + fs;
                    if (f2 >= amod.faces.length)
                        f2 = amod.faces.length;
                    int fp = f2 - f;
                    fp /= 3;
                    //amod.addmat("tex",null,fp,2*fp);
                    //amod.addmat("texc","BridgeCon1.png",fp,2*fp);
                    amod.addmat("texc", "maptestnck.png", fp, 2 * fp);
                }
                amod.mat.put("color", colss6[g]);
                //amod.mat.color = [.75,.75,.75,1];
                amod.commit();
                //amod.settexture();
                Tree atree = new Tree("spongepart" + i);
                atree.setmodel(amod);
                //atree.trans = [0,(4-i)*1.5,0];
                atree.trans = new float[]{-.5f, -.5f, 0};
                float scl = 1.0f / pow3[i];
                atree.scale = new float[]{scl, scl, scl};
                //pendpce0.rotvel = [.1,.5,0];
                //pendpce0.flags |= treeflagenums.ALWAYSFACING;
                roottree.linkchild(atree);
            }
        }
    }

    void lesslevel() {
        if (curlevel > 0)
            --curlevel;
        updatelevel();
    }

    Runnable runLessLevel = new Runnable() {
        @Override
        public void run() {
            lesslevel();
        }
    };

    void  morelevel() {
        if (curlevel < maxlevel)
            ++curlevel;
        updatelevel();
    }

    Runnable runMoreLevel = new Runnable() {
        @Override
        public void run() {
            morelevel();
        }
    };

    @Override
    public void init() {
        Log.i(TAG, "entering sponge");
        SimpleUI.setbutsname("menger");
        maxlevel = MainActivity.oldDevice ? 3 : 4;
        // less,more printarea for sponge
        levelarea = SimpleUI.makeaprintarea("level: ");
        SimpleUI.makeabut("higher level",runMoreLevel/*morelevel*/);
        SimpleUI.makeabut("lower level",runLessLevel/*lesslevel*/);
/*
        setbutsname('menger');
        // less,more printarea for sponge
        levelarea = makeaprintarea('level: ');
        makeabut("lower level",lesslevel);
        makeabut("higher level",morelevel);
*/
        // test UI
        /*
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"running on UI thread");
                TextView tv = (TextView) Utils.getContext().findViewById(R.id.textView1);
                tv.setText("setting text !!!");
            }
        });*/
        //levelarea.draw("Test levelarea draw");

        // real UI
        // ui
        /*
        setbutsname('menger');
        // less,more,reset for pendu1
        levelarea = makeaprintarea('level: ');
        makeabut("lower level",lesslevel);
        makeabut("higher level",morelevel);
        */

        smeshfaceposx = new Mesh();
        smeshfaceposx.verts = smeshfaceposx_verts;
        smeshfaceposx.uvs = smeshfaceposx_uvs;
        smeshfaceposx.faces = smeshfaceposx_faces;

        smeshfacenegx = new Mesh();
        smeshfacenegx.verts = smeshfacenegx_verts;
        smeshfacenegx.uvs = smeshfacenegx_uvs;
        smeshfacenegx.faces = smeshfacenegx_faces;

        smeshfaceposy = new Mesh();
        smeshfaceposy.verts = smeshfaceposy_verts;
        smeshfaceposy.uvs = smeshfaceposy_uvs;
        smeshfaceposy.faces = smeshfaceposy_faces;

        smeshfacenegy = new Mesh();
        smeshfacenegy.verts = smeshfacenegy_verts;
        smeshfacenegy.uvs = smeshfacenegy_uvs;
        smeshfacenegy.faces = smeshfacenegy_faces;

        smeshfaceposz = new Mesh();
        smeshfaceposz.verts = smeshfaceposz_verts;
        smeshfaceposz.uvs = smeshfaceposz_uvs;
        smeshfaceposz.faces = smeshfaceposz_faces;

        smeshfacenegz = new Mesh();
        smeshfacenegz.verts = smeshfacenegz_verts;
        smeshfacenegz.uvs = smeshfacenegz_uvs;
        smeshfacenegz.faces = smeshfacenegz_faces;

        meshes6 = new Mesh[] {
                smeshfaceposz,
                smeshfacenegz,
                smeshfaceposx,
                smeshfacenegx,
                smeshfaceposy,
                smeshfacenegy
        };

        // main scene
        roottree = new Tree("root");
        // build a prism
        atree =  ModelUtil.buildplanexy("aplane", 1, 1, "maptestnck.png", "tex"); // name, size, texture, generic texture shader
        roottree.linkchild(atree); // link to and pass ownership to backgroundtree
        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane

        curlevel = 2;
        updatelevel();
        //levelarea.draw("Test levelarea draw");
        //levelarea.draw("short");


        // set the lights
        //lights.wlightdir = vec3.fromValues(0,0,1);

        // set the camera
        //mainvp.trans = [0,0,-15]; // flycam
        ViewPort.mainvp.trans = new float[] {0,0,-1}; // flycam
        ViewPort.mainvp.rot = new float[3];  // flycam
        ViewPort.mainvp.near = .001f;
        ViewPort.mainvp.far = 10.0f;
        //ViewPort.mainvp.xo = .5f; // test skew/scroll in the viewport
        //ViewPort.mainvp.yo = .5f;
        //ViewPort.mainvp.changeKeyState(ViewPort.UP,true);
        ViewPort.setupViewportUI(1.0f/1024.0f);

/*
        float t = 1.0f/2048.0f;
        while(t <= 32) {
            String s;
            //t = -t;
            if (t > -1 && t < 0)
                s = "-1/" + String.format("%1.0f",-1/t);
            else if (t > 0 && t < 1)
                s = "1/" + String.format("%1.0f",1/t);
            else
                s = String.format("%1.0f",t);
            Log.e(TAG,"float test print = " + s);
            //t = -t;
            t *= 2;
        } */
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        // proc
        roottree.proc(); // do animation and user proc if any
        ViewPort.mainvp.doflycam(ir);
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
        SimpleUI.clearbuts("viewport");
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

        SimpleUI.clearbuts("menger");
        Log.e(TAG,"done clearing menger");
        //SimpleUI.removeUIPrintArea(levelarea);
        //SimpleUI.removeUIButton(upBut);
        //SimpleUI.removeUIButton(downBut);
    }

}
