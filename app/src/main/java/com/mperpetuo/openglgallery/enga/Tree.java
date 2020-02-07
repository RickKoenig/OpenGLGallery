package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

import static android.opengl.GLES20.glUniform2fv;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by rickkoenig on 2/23/16.
 */
public class Tree {
    private static final String TAG = "Tree";

    // flags
    public static final int FLAG_ALWAYSFACING = 0x20;
    public static final int FLAG_DONTDRAW = 0x40;
    public static final int FLAG_DONTDRAWC = 0x80;
    public static final int FLAG_DONTCASTSHADOW = 0x100;
    public static final int FLAG_AMBLIGHT = 0x200;
    public static final int FLAG_DIRLIGHT = 0x400;
    public static final int FLAG_LIGHT = FLAG_AMBLIGHT|FLAG_DIRLIGHT;
    public int flags;

    // animation
    // private final int frm;
    //private static float Main3D.frametime = 1.0f/60.0f; // TODO: set from avg fps
    //public static float animframestep = 30;
    //public static float framestep = 1;
    //public static boolean framesteptoggleenable = true;
    //public static boolean insamp = true;

    public String name;

    // model and material properties and overrides
    public ModelBase mod; // the model
    // tree material override of model material
    public HashMap<String,float[]> mat = new HashMap<>();
    // tree texture override of model texture
    Texture treereftexture;
    String treetexturename;

    // geometry
    public static Stack<float[]> matrixStack = new Stack<>();

    public float[] trans;
    public float[] rot;
    public float[] qrot;
    public float[] scale;

    public float[] transvel;
    public float[] rotvel; // no qrotvel yet
    public float[] scalevel;


    public float[] mvm = new float[16]; // used during drawing
    public float[] o2pmat4;// = new float[16]; // object to parent matrix override fur custom transformations

    // hierarchy
    public ArrayList<Tree> children = new ArrayList<>();
    public Tree parent;

    //public static boolean buildShadowMap;

    // scratch variables
    public static float[] negtrans = new float[3];
    public static float[] invscale = new float[3];
    public static float[] invquat = new float[4];
    public static float[] qrotmat = new float[16];

    public ArrayList<float[]> possamp;
    public ArrayList<float[]> qrotsamp;
    //public static final float[] tempMatrix0 = new float[16];
    //public static float[] tempMatrix1 = new float[16];

    // animation
    static float animframestep = 30;
    static float frmstep = 1;
    static boolean insamp = true; // turn on animation
    float frm;


    public Tree(String n) {
        name = n;
        //flags = 0;
        //mvm = new float[16];
        //frm = 0;

        String ext = Utils.getExt(n);
        if (ext.equalsIgnoreCase("bwo")) {
            setmodel(Bwo.loadBwoModel(n));
        /* var s = spliturl(name);
        var lext = s.ext.toLowerCase();
        if (lext == "bwo") {
            this.mod = loadbwomodel(name); */
        } else if (ext.equalsIgnoreCase("bws")) {
            Bws.loadbws(this);
        }
    }

    // clone tree, deep
    public Tree(Tree base) {
        flags = base.flags;
        userproc = base.userproc;
        name = base.name; // Strings are immutable
        if (base.trans != null) {
            //trans = new float[3];
            //System.arraycopy(base.trans,0,trans,0,3);
            trans = base.trans.clone();
        }
        if (base.rot != null) {
            //rot = new float[3];
            //System.arraycopy(base.rot,0,rot,0,3);
            rot = base.rot.clone();
        }
        if (base.qrot != null) {
            //qrot = new float[4];
            //System.arraycopy(base.qrot,0,qrot,0,4);
            qrot = base.qrot.clone();
        }
        if (base.scale != null) {
            //scale = new float[3];
            //System.arraycopy(base.scale,0,scale,0,3);
            scale = base.scale.clone();
        }
        if (base.transvel != null) {
            //transvel = new float[3];
            //System.arraycopy(base.transvel,0,transvel,0,3);
            transvel = base.transvel.clone();
        }
        if (base.rotvel != null) {
            //rotvel = new float[3];
            //System.arraycopy(base.rotvel,0,rotvel,0,3);
            rotvel = base.rotvel.clone();
        }
        if (base.scalevel != null) {
            //scalevel = new float[3];
            //System.arraycopy(base.scalevel,0,scalevel,0,3);
            scalevel = base.scalevel.clone();
        }

        frm = base.frm;

        if (base.o2pmat4 != null) {
            o2pmat4 = base.o2pmat4.clone();
        }
        if (base.mod != null) {
            mod = base.mod.newdup();
            //mod = base.mod;
            //++mod.refcount;
        }
        // this one is a hashmap of string to float arrays
        if (base.mat != null) {
            // this is the only hashmap in tree, each string entry is an array of floats 1,2,3,4,16
            mat = new HashMap<String,float[]>();
            for (String key : base.mat.keySet()) {
                float[] val = base.mat.get(key);
                if (val == null) {
                    Log.e(TAG,"val == null for key " + key);
                } else {
                    float[] clone = val.clone();
                    mat.put(key, clone);
                }
            }
        }
        treetexturename = base.treetexturename;
        if (base.treereftexture != null) {
            treereftexture = base.treereftexture;
            ++treereftexture.refcount;
        }
// hiearchy
        children = new ArrayList<>();
        int i,n = base.children.size();
        for (i=0;i<n;++i) {
            Tree dupchild = new Tree(base.children.get(i));
            dupchild.parent = this;
            children.add(dupchild);
        }

//        name = "clone_" + base.name;
        //return new Tree("clone");
/*
        flags; x
        name; x
        mod; x
        mat; x
        treetexturename; x
        treereftexture; x
        trans; x
        rot; x
        qrot; x
        scale; x
        transvel; x
        rotvel; x
        scalevel; x
        mvm; x built later
        o2pmat4; x
        children; x
        parent; x
        userproc; x
*/
    }

    public Tree findtree(String findname) {
        if (name.equals(findname))
            return this;
        int i;
        int n = children.size();
        for (i=0;i<n;++i) {
            Tree fnd = children.get(i).findtree(findname);
            if (fnd != null)
                return fnd;
        }
        return null;
    }

    public void linkchild(Tree child) {
        if (child.parent != null)
            Utils.alert("child '" + child.name + "' already has a parent, '" + child.parent.name + "'");
        children.add(child);
        child.parent = this;
    }

    // does this work ??
    public void unlinkchild() {
        if (parent == null)
            Utils.alert("child " + name + "' has no parent to unlink");
        int idx = parent.children.indexOf(this);
        if (idx < 0)
            Utils.alert("child " + name + "' parent has already disowned you!");
        else
            //parent.children.splice(idx,1);
            parent.children.remove(this);
        parent = null;
    };

    //public ArrayList<float[]> possamp;
    //public ArrayList<float[]> qrotsamp;

    // user defined code for the tree.proc, runs 'proc' if not null
    public abstract static class UserProc {
        public abstract void proc(Tree t);
    }
    public UserProc userproc;



    void matPush(float[] m) {
        float[] mp = new float[16];
        System.arraycopy(m,0,mp,0,16);
        matrixStack.push(mp);
    }

    float[] matPop() {
        if (matrixStack.empty())
            Utils.alert("matPop: matrix stack empty!");
        float[] ret = new float[16];
        System.arraycopy(matrixStack.pop(), 0, ret, 0, 16);
        return ret;
    }

    void buildtransqrotscale(float[] out) {
        buildtransqrotscale(out, trans, qrot, scale);
    }

    static void buildtransqrotscale(float[] out, float[] transA, float[] qrotA, float[] scaleA) {
        //Matrix.setIdentityM(out, 0);
        //mat4.identity(out);
/*        if (trans != null)
        mat4.translate(out,out,sqt.trans);
        if (qrot != null)
            mat4.matFromQuat(qrotmat,sqt.qrot);
        mat4.mul(out,out,qrotmat);
        if (scale != null)
            mat4.scale(out,out,sqt.scale); */
        if (transA != null)
            NuMath.translate(out,transA);
        if (qrotA != null) {
            Quat.matFromQuat(qrotmat, qrotA);
            NuMath.mul(out,out,qrotmat);
        }
        if (scaleA != null)
            NuMath.scale(out,scaleA);
    }

    void buildtransrotscale(float[] out) {
        buildtransrotscale(out, trans, rot, scale);
    }

    // append TRS to matrix on the right
    // out = out*trans*rot*scale, used for outvec = out*invec
    public static void buildtransrotscale(float[] out, float[] transA, float[] rotA, float[] scaleA) {
        /*boolean manMatrix = false; // test the matrix if true
        if (manMatrix) {
            Matrix.setIdentityM(out,0);
            out[14] = 2.5f; // move back
            out[1] = .25f; // skew x to y
            return;
        }*/
        if (transA != null)
            NuMath.translate(out,transA);
        if (rotA != null)
            NuMath.rotateEuler(out,rotA);
        if (scaleA != null)
            NuMath.scale(out,scaleA);
    }

    static void buildtransqrotscaleinv(float[] out,float[] trans,float[] rotq,float[] scale) {
        if (scale != null) {
            NuMath.inv(invscale,scale);
            NuMath.scale(out,invscale);
        }
        if (rotq != null) {
            Quat.conjugate(invquat,rotq);
            Quat.matFromQuat(qrotmat,invquat);
            NuMath.mul(out,out,qrotmat);
        }
        if (trans != null) {
            NuMath.negate(negtrans,trans);
            NuMath.translate(out,negtrans);
        }
    }

    static void buildtransrotscaleinv(float[] out,float[] trans,float[] rot,float[] scale) {
        if (scale != null) {
            NuMath.inv(invscale,scale);
            NuMath.scale(out,invscale);
        }
        if (rot != null) {
            NuMath.rotateEulerInv(out,rot);
        }
        if (trans != null) {
            NuMath.negate(negtrans,trans);
            //negtrans[0] = -trans[0];
            //negtrans[1] = -trans[1];
            //negtrans[2] = -trans[2];
            NuMath.translate(out,negtrans);
        }
    }

    public void setmodel(ModelBase m) {
        mod = m;
    }

    public void settexture(String s) {
        if (treereftexture != null)
            treereftexture.glFree();
        treereftexture = Texture.createTexture(s);
    }

    // look for freaky javascript interpreter bug that causes an array[3] 0 element to go from 0 to NaN, quite rare, hasn't happened in a while
    // now ported from javascript to java
    // checks rot[3] for nans or infinities
    public void crn(String name,float[] r) {
        if (r == null)
            return;
        int i;
        for (i=0;i<3;++i)
            if (Float.isInfinite(r[i]) || Float.isNaN(r[i])) {
                throw new RuntimeException("not a number " + name + " " + i + " frametime " + Main3D.frametime);
                //r[i] = 0;
            }
    }

    private static int Tree2level = 0;
    public void proc() {
        //if (name.equals("Trex_body2.bwo")) {
        //    Log.e(TAG,"animating special object, frame = " + frm);
        //}
        /*if (name.equals("Trex_body2.bwo")) {
            if (trans != null) {
                if (rot == null) {
                    rot = new float[3];
                }
                trans[0] += .1f * frm;
                frm++;
            }
            //flags |= Tree.FLAG_DONTDRAWC;
        }*/

        /*
        if (Tree2level == 0) {
            if (treeglobals.framesteptoggleenable && input.key == "f".charCodeAt(0)) {
                treeinfo.insamp = !treeinfo.insamp;
                input.key = 0; // so other viewports don't get a key..
            }
        } */
        if (Float.isInfinite(Main3D.frametime) || Float.isNaN(Main3D.frametime)) {
            throw new RuntimeException("frametime is not a number");
        }
        
        // rot,trans,scale velocities
        if (rotvel != null) {
            if (rot == null)
                rot = new float[3];
            crn("proc1",rot);
            crn("prco1vel",rotvel);
            rot[0] = NuMath.normalangrad(rot[0] + rotvel[0] * Main3D.frametime);
            rot[1] = NuMath.normalangrad(rot[1] + rotvel[1] * Main3D.frametime);
            rot[2] = NuMath.normalangrad(rot[2] + rotvel[2] * Main3D.frametime);
            crn("proc2",rot);
        }

        if (transvel != null) {
            if (trans == null)
                trans = new float[3];
            trans[0] += transvel[0]*Main3D.frametime;
            trans[1] += transvel[1]*Main3D.frametime;
            trans[2] += transvel[2]*Main3D.frametime;
        }

        if (scalevel != null) {
            if (scale == null)
                scalevel = new float[] {1.0f,1.0f,1.0f};
            scale[0] *= Math.pow(scalevel[0],Main3D.frametime);
            scale[1] *= Math.pow(scalevel[1],Main3D.frametime);
            scale[2] *= Math.pow(scalevel[2],Main3D.frametime);
        } 

        // sampled animation, not key frames, but interpolates between samples
        if (insamp) {
            // advance to next frame
            int nframes = 0;
            if (possamp != null) // if both exist, assume they are the same length
                nframes = possamp.size()-2;
            else if (qrotsamp != null)
                nframes = qrotsamp.size()-2;
            if (nframes > 0) { // 0 doesn't count too
                frm += frmstep*animframestep*Main3D.frametime;
                if (frm >= nframes) {
                    frm -= nframes;
                } else if (frm < 0) {
                    frm += nframes;
                }
            }
            // get trs from sample
            int ifrm = (int)Math.floor(frm);
            float twn = frm - ifrm;
            int ifrm2 = ifrm + 1;
            if (possamp != null) {
                float[] pos0 = null;
                float[] pos1 = null;
                if (ifrm < possamp.size())
                    pos0 = possamp.get(ifrm);
                if (ifrm2 < possamp.size())
                    pos1 = possamp.get(ifrm2);
                if (pos0 != null) {
                    if (pos1 != null) {
                        float[] twnpos = new float[3];
                        NuMath.interp(twnpos,pos0,pos1,twn);
                        trans = twnpos;
                    } else {
                        trans = pos0;//vec3.clone(possamp[ifrm]);
                    }
                }
            }
            if (qrotsamp != null) {
                float[] qrot0 = null;
                float[] qrot1 = null;
                if (ifrm < qrotsamp.size())
                    qrot0 = qrotsamp.get(ifrm);
                if (ifrm2 < qrotsamp.size())
                    qrot1 = qrotsamp.get(ifrm2);
                if (qrot0 != null) {
                    if (qrot1 != null) {
                        float[] twnqrot = new float[4];//quat.create();
                        float[] ff = qrot0;
                        float[] ff2 = qrot1.clone();
                        if (Quat.dot(ff,ff2) < 0)
                            //if (ff[3] * ff2[3] < 0)
                            Quat.negate(ff2,ff2); // choose the short path on the great circle
                        Quat.interp(twnqrot,ff,ff2,twn);
                        qrot = twnqrot;
                    } else {
                        qrot = qrot0;//quat.clone(qrotsamp[ifrm]);
                    }
                }
            }
        }


        if (userproc != null)
            userproc.proc(this);

// do children last
        // proc through a copy in case the children delete themselves
        //if (children != null) {
            ArrayList<Tree> childcopy = new ArrayList<>(children);
            for (Tree child : childcopy) {
                ++Tree2level;
                child.proc();
                --Tree2level;
            }
        //}
        crn("proc3",rot); // check for float errors
    }

    int drawpri; // for sorting different types of trees (skybox,opaque,translucent)

    static Comparator<Tree> treeComparator = new Comparator<Tree>() {
        public int compare(Tree a, Tree b) {
            // draw in order 0 skybox, 1 opaque, 2 translucent/alpha last
            // but alpha has to have objects z sorted in order far to near
            // decreasing trans z, draw far away first, (left handed coord system, inc z goes away from camera towards the horizon)
            if (a.drawpri == 2 && b.drawpri == 2) {
                // sort alphas
                float bc = b.mvm[14];
                float ac = a.mvm[14];
                if (bc == ac)
                    return 0;
                if (bc > ac)
                    return 1;
                return -1;
            }
            return a.drawpri - b.drawpri; // lower drawpris have priority
        }
    };

    // drawing
    static ArrayList<Tree> Tree2drawlist = new ArrayList<>();

    public void draw() {
        //if (name.equals("Trex_body2.bwo")) {
        //    Log.e(TAG,"drawing special object, trans = " + trans[0] + " " + trans[1] + " " + trans[2]);
        //}
        if (Tree2level == 0) {
            Tree2drawlist.clear();
            //Matrix.setIdentityM(GLUtil.mvMatrix,0);
        }
        if ((flags & FLAG_DONTDRAWC)!= 0) {
            return;
        }
        matPush(GLUtil.mvMatrix);
	    // if (false)
	    // if (o2pmat4)
        if (o2pmat4!=null && qrotsamp == null && possamp == null) { // unless animating
        // use this matrix as O2P
        //if (o2pmat4!=null) {
            NuMath.mul(GLUtil.mvMatrix, GLUtil.mvMatrix, o2pmat4);
        } else if (qrot != null) {
            buildtransqrotscale(GLUtil.mvMatrix); // using tqs members (trans quat scale)
        } else {
            buildtransrotscale(GLUtil.mvMatrix); // using trs members (trans rot scale)
            if (mod!=null && (mod.flags&Model.FLAG_ISSKYBOX)!=0) { // skybox is relative to view
                GLUtil.mvMatrix[12] = 0;
                GLUtil.mvMatrix[13] = 0;
                GLUtil.mvMatrix[14] = 0;
            } else if ((flags&FLAG_ALWAYSFACING)!=0) { // rotate to view and keep scale
                float scl = Math.abs(NuMath.determinant(GLUtil.mvMatrix));
                scl = (float)Math.pow(scl,1.0f/3.0f);
                GLUtil.mvMatrix[0] = scl;
                GLUtil.mvMatrix[1] = 0;
                GLUtil.mvMatrix[2] = 0;
                GLUtil.mvMatrix[4] = 0;
                GLUtil.mvMatrix[5] = scl;
                GLUtil.mvMatrix[6] = 0;
                GLUtil.mvMatrix[8] = 0;
                GLUtil.mvMatrix[9] = 0;
                GLUtil.mvMatrix[10] = scl;
            }
        }
        //if (this == dirlight)
        //    mat4.copy(this.mvm,mvMatrix);
        //if (this == Lights.dirlight)
            System.arraycopy(GLUtil.mvMatrix,0,mvm,0,16);
        if (mod != null) {
            if ((flags&FLAG_DONTDRAW) == 0) {
                if (!ShadowMap.inshadowmapbuild  || (flags & Tree.FLAG_DONTCASTSHADOW) == 0) {
                    //curtree = this;
                    //if ((mod.flags & modelflagenums.HASALPHA))
                    Tree2drawlist.add(this);

                    //mod.draw(); // later
                    //curtree = null;
                }
            }
        }
// children
        //if (children != null) {
            for (Tree t : children) {
                ++Tree2level;
                t.draw();
                --Tree2level;
            }
        //}
        // GLUtil.mvMatrix = matPop(); // bad !
        if (Tree2level == 0) {
            int ndrawlist = Tree2drawlist.size();
            int alphacnt = 0;
            int skyboxcnt = 0;
            int opaquecnt = 0;
            // sort drawlist
            int i;
            for (i=0;i<ndrawlist;++i) { // set drawpri (0 skybox, 1 opaque, 2 alpha)
                GLUtil.curtree = Tree2drawlist.get(i);
                ModelBase ctm = GLUtil.curtree.mod; // only trees with models get added to the drawlist
                if ((ctm.flags & Model.FLAG_ISSKYBOX)!=0) {
                    ++skyboxcnt;
                    GLUtil.curtree.drawpri = 0; // highest priority
                } else if ((ctm.flags & Model.FLAG_HASALPHA)!=0) {
                    ++alphacnt;
                    GLUtil.curtree.drawpri = 2; // lowest priority, but sort by 'z'
                } else {
                    ++opaquecnt;
                    GLUtil.curtree.drawpri = 1; // medium priority, no need to sub sort these, zbuffer will take care of this
                }
                //curtree.mod.draw(); // later
            }
            Collections.sort(Tree2drawlist,treeComparator);
            // draw drawlist
            Lights.dolights();
            for (i=0;i<ndrawlist;++i) {
                GLUtil.curtree = Tree2drawlist.get(i); // model can acess the tree overrides
                System.arraycopy(GLUtil.curtree.mvm,0,GLUtil.mvMatrix,0,16); // get tree matrix to global so model will use this
                GLUtil.curtree.mod.draw(); // draw the model
            }
            GLUtil.curtree = null; // keep lower level (engine bypass) stuff happy
        }
        GLUtil.mvMatrix = matPop(); // good !
    }

    private static int tree2level = 0;

    private String tree2indent(int n) {
        String ret = "";
        int i;
        for (i=0;i<n;++i) {
            ret += "   ";
        }
        return ret;
    }

    private static int ntreenodes = 0;

    public void log() {
        if (tree2level == 0) {
            Log.i(TAG, "Tree Log ===============");
            ntreenodes = 0;
        }
        ++ntreenodes;
        String modname = "---";
        if (mod != null)
            modname = mod.name;
        String logstr = tree2indent(tree2level) + " DP " + drawpri + " " + name + " mod " + modname;
        //if (mod && mod.isafont)
        //    logger(" ModelFont");
        //logger(tree2level);
        if (treetexturename != null)
            logstr += " treetexture " + treetexturename;
        Log.i(TAG,logstr);
        //if (children != null) {
            for (Tree t : children) {
                ++tree2level;
                t.log();
                --tree2level;
            }
        //}
        if (tree2level == 0) {
            Log.i(TAG,"num nodes = " + ntreenodes);
        }
    }

    public void glFree() {
        if (mod != null) {
            mod.glFree();
            mod = null;
        }
        if (treereftexture != null) {
            treereftexture.glFree();
            treereftexture = null;
        }
        Lights.removeLight(this);

        //if (children != null) {
            for (Tree t:children) {
                t.glFree();
            }
        //}
    }
}
