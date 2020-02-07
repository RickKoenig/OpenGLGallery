package com.mperpetuo.openglgallery.enga;

import android.opengl.GLES20.*;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import static android.opengl.GLES20.*;

/**
 * Created by rickkoenig on 2/23/16.
 */
public class ViewPort {
    static private final String TAG = "ViewPort";
    public static ViewPort mainvp;// = new ViewPort();

    // TODO: maybe get width and height in here and no rely on Main3D viewWidth and viewHeight

    public FrameBufferTexture target;
    public static FrameBufferTexture defaulttarget;

    //public boolean lastTouch;

    public int clearflags;
    public float[] clearcolor;

    public float[] trans;
    public float[] rot;
    public float xo,yo; // scroll/skew viewports

    public boolean incamattach;
    public Tree camattach;
    public boolean inlookat;
    public Tree lookat;
    public boolean isortho;
    public float ortho_size;

    public float near;
    public float far;
    public float zoom;

    static private SimpleUI.UIPrintArea showSpeed;

    static Flycamstate flycamstate = new Flycamstate();

    public static float mwi[] = new float[16];
    public static float mw[] = new float[16];
    public static float ia[] = new float[16];
    //public static float v2wMatrix[] = new float[16];

    public static float trns[] = new float[3];
    public static float trns2[] = new float[3];
    public static float up[] = {0,1,0};

    static final int[] mi = {0,1,2,4,5,6,8,9,10,12,13,14}; // flip over just the 4 by 3 submatrix part, it seems to work

    // stub for key click and different mouse buttons
    int key = 0;
    int mbut = 0;

    // for better touch UI, "drag' touch
    //float virtX; // = Main3D.viewWidth/2.0f;
    //float virtY; // = Main3D.viewHeight/2.0f;

    // stub for keystate
    //static boolean[] keystate = {true,false,false,false}; // move around
    static public boolean[] keystate = new boolean[4]; // stay still
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public boolean isshadowmap;

    public float getFlyCamSpeed() {
        return flycamstate.flycamspeed;
    }

    //float asp; // maybe include also the startx starty width height

    static class Flycamstate {
        boolean inflycam = true; // no keyboard so turn on by default
        //boolean flycamrevy = false;
        float flycamspeed = 1.0f/64.0f;
        float maxflycamspeed = 32.0f;
        float minflycamspeed = 1.0f/2048.0f;
    }

    static private String prettySpeed() {
        float t = flycamstate.flycamspeed;
        String s;
        if (t > -1 && t < 0)
            s = "-1/" + String.format("%1.0f",-1/t);
        else if (t > 0 && t < 1)
            s = "1/" + String.format("%1.0f",1/t);
        else
            s = String.format("%1.0f",t);
        return s;
    }

    static public void setupViewportUI(float v) {
        if (v == 0) {
            v = 1.0f / 256.0f;
            changeKeyState(ViewPort.UP,false);
        } else {
            changeKeyState(ViewPort.UP,true);
        }
        flycamstate.flycamspeed = v;
        SimpleUI.setbutsname("viewport");
        showSpeed = SimpleUI.makeaprintarea("showSpeed");
        if (keystate[UP]) {
            showSpeed.draw("VS " + prettySpeed());
        } else {
            showSpeed.draw("VS stop");
        }

        SimpleUI.makeabut("faster",new Runnable() {
            @Override
            public void run() {
                keystate[UP] = true;
                boolean neg = flycamstate.flycamspeed < 0;
                float oldspeed = Math.abs(flycamstate.flycamspeed);
                float newspeed = 2*oldspeed;
                if (newspeed > flycamstate.maxflycamspeed)
                    return;
                if (neg)
                    flycamstate.flycamspeed = -newspeed;
                else
                    flycamstate.flycamspeed = newspeed;
                showSpeed.draw("VS " + prettySpeed());
            }
        });

        SimpleUI.makeabut("slower",new Runnable() {
            @Override
            public void run() {
                keystate[UP] = true;
                boolean neg = flycamstate.flycamspeed < 0;
                float oldspeed = Math.abs(flycamstate.flycamspeed);
                float newspeed = .5f*oldspeed;
                if (newspeed < flycamstate.minflycamspeed)
                    return;
                if (neg)
                    flycamstate.flycamspeed = -newspeed;
                else
                    flycamstate.flycamspeed = newspeed;
                showSpeed.draw("VS " + prettySpeed());
            }
        });

        SimpleUI.makeabut("stop",new Runnable() {
            @Override
            public void run() {
                keystate[UP] = !keystate[UP];
                if (keystate[UP])
                    showSpeed.draw("VS " + prettySpeed());
                else
                    showSpeed.draw("VS stop");
            }
        });

        SimpleUI.makeabut("reverse",new Runnable() {
            @Override
            public void run() {
                keystate[UP] = true;
                flycamstate.flycamspeed = -flycamstate.flycamspeed;
                showSpeed.draw("VS " + prettySpeed());
            }
        });
    }

    public ViewPort() {
        flycamstate = new Flycamstate();
        clearflags = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT;
        clearcolor =  new float[] {0.0f,.75f,1.0f,1.0f}; // RGBA
        //near = .002f; // webgl version uses this as the default
        near = .02f; // android esp. qualcom devel devices
        far = 10000.0f;
        zoom = 1.0f;
        ortho_size = 4.0f;
        trans = new float[3];
        rot = new float[3];
        //virtX = .5f;
        //virtY = .5f;
        //isortho = true;
        keystate[UP] = false;
    }

    static private void changeKeyState(int idx,boolean value) {
        keystate[idx] = value;
    }

    public void changeFlyCamSpeed(float newSpeed) {
        flycamstate.flycamspeed = newSpeed;
    }

    public void resetKeyState() {
        keystate = new boolean[4];
    }

    public void doflycam(InputState ir) {
        //if (Main3D.viewChanged) {
            //ir.dx = 0;
            //ir.dy = 0;
            // rotation of device has just happened, lets reset the flycam
            //virtX = .5f;
            //virtY = .5f;
            //keystate[UP] = false;
            //if (showSpeed != null) {
            //    showSpeed.draw("VS stop");
            //}
        //}
        // everything turned off except for look around
        float leftright=0,foreback=0,updown=0;
        float mxc,mxr,myc,myr,rcx,rsx,rcy,rsy;
        if (key == (int)'c') {
            flycamstate.inflycam = !flycamstate.inflycam;
            key = 0; // so other viewports don't get a key..
        }
        if (key == (int)'l') {
            inlookat = !inlookat;
            key = 0; // so other viewports don't get a key..
        }
        if (key == (int)'a') {
            incamattach = !incamattach;
            key = 0; // so other viewports don't get a key..
        }
        /*if (key == (int)'y') {
            flycamstate.flycamrevy = !flycamstate.flycamrevy;
            key = 0; // so other viewports don't get a key..
        }*/
        if (rot == null)
            rot = new float[3];
        if ((ir.x>=0 && ir.x<Main3D.viewWidth && ir.y>=0 && ir.y<Main3D.viewHeight && ir.touch > 0)/* || Main3D.viewChanged */) {
            if (key == (int)'r') {
                trans[0] = trans[1] = trans[2] = 0;
                rot[0] = rot[1] = rot[2] = 0;
                key = 0;
            }

            /*
            virtX += ir.dx/Main3D.viewWidth;
            if (virtX < 0)
                ++virtX;
            else if (virtX >= 1)
                --virtX;
            rot[1] = NuMath.normalangrad(  (virtX - .5f)*2*(2*NuMath.PI)  );

            virtY += ir.dy/Main3D.viewHeight;
            virtY = Utils.range(0,virtY,1);
            rot[0] = NuMath.normalangrad(  (virtY - .5f)*.5f*(2*NuMath.PI)  );
*/

            rot[1] += ir.dx*2*(2*NuMath.PI)/Main3D.viewWidth;
            rot[1] = NuMath.normalangrad(rot[1]);

            rot[0] += ir.dy*.5f*(2*NuMath.PI)/Main3D.viewHeight;
            rot[0] = NuMath.normalangrad(rot[0]);
            rot[0] = Utils.range(-.5f*NuMath.PI,rot[0],.5f*NuMath.PI);



            if (flycamstate.inflycam) {
                if (key == (int)'+' || key == (int)'=')
                    flycamstate.flycamspeed *= 2.0;
                if (key == (int)'-')
                    flycamstate.flycamspeed *= .5;
                if (keystate[RIGHT])
                    leftright += flycamstate.flycamspeed;
                if (keystate[LEFT])
                    leftright -= flycamstate.flycamspeed;
                if (keystate[UP])
                    foreback += flycamstate.flycamspeed;
                if (keystate[DOWN]) {
                    foreback -= flycamstate.flycamspeed;
                }
                if ((mbut&2) != 0)
                    updown += flycamstate.flycamspeed;
                if ((mbut&1) != 0)
                    updown -= flycamstate.flycamspeed;

                //mxc = .5f*Main3D.viewWidth;
                //myc = .5f*Main3D.viewHeight;
                //mxr = 1.0f*2*2*NuMath.PI/Main3D.viewWidth;
                //myr = .5f*2*NuMath.PI/Main3D.viewHeight;
                //if (flycamstate.flycamrevy)
                //    myr = -myr;
                //rot[1] = NuMath.normalangrad((virtX*Main3D.viewWidth - mxc)*mxr);
                //rot[0] = NuMath.normalangrad((virtY*Main3D.viewHeight - myc)*myr);




                rcx = (float)Math.cos(rot[0]);
                rsx = (float)Math.sin(rot[0]);
                rcy = (float)Math.cos(rot[1]);
                rsy = (float)Math.sin(rot[1]);
                trans[0] += leftright*rcy;
                trans[2] -= leftright*rsy;
                trans[0] += foreback*rcx*rsy;
                trans[1] += -foreback*rsx;
                trans[2] += foreback*rcx*rcy;
                trans[0] += updown*rsx*rsy;
                trans[1] += updown*rcx;
                trans[2] += updown*rsx*rcy;
           }
        }
    }

    public void beginscene() {
        if (defaulttarget != target) {
            //FrameBufferTexture.useframebuffer(target);
            if (target == null)
                FrameBufferTexture.useframebuffer(0);
            else
                FrameBufferTexture.useframebuffer(target.framebuffer);
            defaulttarget = target;
        }
        if ((clearflags & GL_COLOR_BUFFER_BIT)!=0) {
            // I'm not sure why css background-color has an effect on alpha blending ??
            glClearColor(clearcolor[0], clearcolor[1], clearcolor[2], clearcolor[3]);
            // So I'll just set the style.backgroundColor to the same as the gl.clearColor
            //glc.style.backgroundColor = "#" + tohex2(vp.clearcolor[0]) + tohex2(vp.clearcolor[1]) + tohex2(vp.clearcolor[2]);
        }
        glClear(clearflags);
        if (target != null) {
            setview((float)target.width/target.height);
            glViewport(0, 0, target.width,target.height);
        } else {
            setview(Main3D.viewAsp);
            glViewport(0, 0, Main3D.viewWidth,Main3D.viewHeight);
        }
        if (isshadowmap)
            ShadowMap.beginpass();
        else
            ShadowMap.endpass();

    }

    void setview(float asp) {
        Tree a;

        // lookat cam
        if (inlookat && lookat != null) {
        //if (false) {
            a = lookat; // get tree node that we're looking at
            //float[] mwi = new float[16]; // lookat node to world matrix
            Matrix.setIdentityM(mwi,0);

            while(a != null) {
                if (a.o2pmat4 != null && a.qrotsamp == null && a.possamp == null) { // test TODO
                    //var ia = mat4.create();
                    Matrix.invertM(ia,0,a.o2pmat4,0);
                    NuMath.mul(mwi,mwi,ia);
                } else if (a.qrot != null) {
                    Tree.buildtransqrotscaleinv(mwi,a.trans,a.qrot,a.scale); // using tqs members (trans quat scale) // test TODO
                } else {
                    Tree.buildtransrotscaleinv(mwi,a.trans,a.rot,a.scale); // using trs members (trans rot scale) // works!
                }
                a = a.parent; // walk up to world
            }

            //var mw = mat4.create();
            // and back to non invert
            Matrix.invertM(mw,0,mwi,0);
            trns[0] = mw[12];
            trns[1] = mw[13];
            trns[2] = mw[14];
            float[] trns2 = trans;

            if (incamattach && camattach != null) { // do both lookat and camattach
                // borrow mvMatrix and v2wMatrix
                //mat4.identity(mvMatrix);
                Matrix.setIdentityM(GLUtil.mvMatrix,0);
                Tree.buildtransrotscaleinv(GLUtil.mvMatrix, mainvp.trans,mainvp.rot,null); // viewport first
                a = camattach; // get tree node that camera is attached to, where in the world is the camera
                while(a != null) {
                    if (a.o2pmat4 != null && a.qrotsamp == null && a.possamp == null) { // test TODO
                        //var ia = mat4.create();
                        Matrix.invertM(ia,0,a.o2pmat4,0);
                        NuMath.mul(GLUtil.mvMatrix,GLUtil.mvMatrix,ia);
                    } else if (a.qrot != null) {
                        Tree.buildtransqrotscaleinv(GLUtil.mvMatrix,a.trans,a.qrot,a.scale); // using tqs members (trans quat scale) // test TODO
                    } else {
                        Tree.buildtransrotscaleinv(GLUtil.mvMatrix,a.trans,a.rot,a.scale); // using trs members (trans rot scale) // works!
                    }
                    a = a.parent; // walk up to world
                }
                Matrix.invertM(GLUtil.v2wMatrix,0,GLUtil.mvMatrix,0); // for env map and shadowmapping
                trns2 = new float[] {GLUtil.v2wMatrix[12],GLUtil.v2wMatrix[13],GLUtil.v2wMatrix[14]}; // camera location in world space
                // done borrow mvMatrix and v2wMatrix
            }

            NuMath.lookAtlhc(GLUtil.mvMatrix,trns2,trns,up);

        } else { // no lookat, just check for camattach

            //mat4.identity(mvMatrix);
            Matrix.setIdentityM(GLUtil.mvMatrix,0);

		    //if (true) {
            // ajust mvMatrix for attached camera
            Tree.buildtransrotscaleinv(GLUtil.mvMatrix,trans,rot,null); // viewport first TODO later
          if (incamattach && camattach != null) { // build up the camera to world matrix, then invert it
                a = camattach; // get tree node that camera is attached to
                while(a != null) {
                    if (a.o2pmat4 != null && a.qrotsamp == null && a.possamp == null) { // test TODO
                    //if (a.o2pmat4 != null) { // test TODO
                        //var ia = mat4.create ();
                        Matrix.invertM(ia,0,a.o2pmat4,0);
                        NuMath.mul(GLUtil.mvMatrix,GLUtil.mvMatrix,ia);
                    } else if (a.qrot != null) {
                        //float[] aqrot = new float[4];
                        //float[] aqrot = a.qrot.clone();
                        //aqrot[3] = 1;
                        //Tree.buildtransqrotscaleinv(GLUtil.mvMatrix,a.trans,aqrot,a.scale); // using tqs members (trans quat scale) // test TODO
                        Tree.buildtransqrotscaleinv(GLUtil.mvMatrix,a.trans,a.qrot,a.scale); // using tqs members (trans quat scale) // test TODO
                    } else {
                        Tree.buildtransrotscaleinv(GLUtil.mvMatrix,a.trans,a.rot,a.scale); // using trs members (trans rot scale) // works!
                    }
                    a = a.parent; // walk up to world
                }
                // check for neg scale and invert the scale of the matrix somehow...
                // and normalize matrix
                // leave pos scale alone...
                float d = NuMath.determinant(GLUtil.mvMatrix);
                float id;
                if (d < 0) {
                    id = -1.0f/d;
                    id = (float)-Math.pow(id,1.0/3.0);
                } else {
                    id = 1.0f/d;
                    id = (float)Math.pow(id,1.0f/3.0f);
                }
                int i;
                for (i=0;i<mi.length;++i) {
                    int j = mi[i];
                    GLUtil.mvMatrix[j] *= id;
                }
            }

        }
        //mat4.invert(v2wMatrix,mvMatrix); // for env map and shadowmapping TODO: later
        Matrix.invertM(GLUtil.v2wMatrix,0,GLUtil.mvMatrix,0);
        // set projection matrix here
        if (isortho) {
            // mat4.ortholhc(pMatrix,-vp.ortho_size*vp.asp,vp.ortho_size*vp.asp,-vp.ortho_size,vp.ortho_size,vp.near,vp.far);
            //NuMath.ortholhc(GLUtil.pMatrix,-ortho_size*asp,ortho_size*asp,-ortho_size,ortho_size,near,far);
            NuMath.ortholhc(GLUtil.pMatrix,ortho_size,asp,near,far);

        } else {
            //Matrix.frustumM(GLUtil.pMatrix,0,);
            ;// mat4.perspectivelhczf(pMatrix,vp.zoom,vp.asp,vp.near,vp.far);
            NuMath.perspectivelhczf(GLUtil.pMatrix,zoom,asp,near,far);
            // apply scroll/skew to the perspective projection matrix
            if (asp > 1.0f) { // landscape
                GLUtil.pMatrix[8] += xo*2/asp; // skew X
                GLUtil.pMatrix[9] += yo*2; // skew Y
            } else { // portrait
                GLUtil.pMatrix[8] += xo*2; // skew X
                GLUtil.pMatrix[9] += yo*2*asp; // skew Y
            }
        }

        // get light matrices over for 2nd pass
        //if (isshadowmap) {
            // copy over matrices to light map matrices
            //globalmat.lightpMatrix = pMatrix;
            //ShadowMap.lmvMatrix = mvMatrix;
            //ShadowMap.inshadowmapbuild = true;
        //}
        //dolights(); // call later in Tree2.draw

    }

}
