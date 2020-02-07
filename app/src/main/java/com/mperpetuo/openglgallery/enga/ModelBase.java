package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.*;

/**
 * Created by cyberrickers on 6/5/2016.
 */
public abstract class ModelBase {
    static final String TAG = "ModelBase";
    public static final int FLAG_NOZBUFFER = 1;
    public static final int FLAG_HASALPHA = 2;
    public static final int FLAG_ISSKYBOX = 0x10; // puts object at location eye space 0,0,0
    public static final int FLAG_DOUBLESIDED = 0x20;

    public static String modellog;

    static Map<String,ModelBase> refcountmodellist = new HashMap<>();


    static int nglbuffers;

    static int totalverts;
    static int totalfaces;

    static int buffernamearr[] = new int[1];

    public String name;
    public int refcount;

    public int nverts;
    public int nface;
    public float[] verts;
    float[] norms;
    float[] uvs;
    float[] uvs2;
    float[] cverts;
    public short[] faces;

    public float[] boxmin;
    public float[] boxmax;

    public int flags;
    // uniform materials, all user defined for entire model
    public HashMap<String,float[]> mat = new HashMap<>();

    protected ModelBase(String aname) {
        refcount = 1;
        name = aname;
        refcountmodellist.put(aname,this);
    }

    public static void initModels() {
        nglbuffers = 0;
        refcountmodellist = new HashMap<>();
        totalverts = 0;
        totalfaces = 0;
    }

    public static void modelrc() {
        Log.i(TAG,"ModelList =====");
        totalverts = 0;
        totalfaces = 0;
        int totalmodels = 0;
        int largest = 0;
        String largestname = "---";
        for (ModelBase modelref : refcountmodellist.values()) {
            modelref.log();
            ++totalmodels;
            int nv = modelref.nverts;
            if (nv > largest) {
                largest = nv;
                largestname = modelref.name;
            }
        }
        Log.i(TAG,"totalmodels " + totalmodels + " totalverts " + totalverts + " totalfaces " + totalfaces + " largest '" + largestname + "'");
    }

    private void log() {
        modellog = "";
        buildLog();
        printLog();
    }

    private void printLog() {
        Log.i(TAG,modellog);
    }

    int createBuffer() {
        glGenBuffers(1, buffernamearr, 0);
        ++nglbuffers;
        return buffernamearr[0];
    }

    int makeAndWriteToFloatBuffer(float[] data) {
        return makeAndWriteToFloatBuffer(data,0,data.length);
    }

    int makeAndWriteToFloatBuffer(float[] data,int offset,int len) {
        int glbuff = createBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(len * 4); // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        try {
            fb.put(data, offset, len);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Utils.alert("makeAndWriteToFloatBuffer failed index out of bounds");
        }
        fb.position(0);
        glBindBuffer(GL_ARRAY_BUFFER,glbuff);
        glBufferData(GL_ARRAY_BUFFER, fb.capacity() * 4, fb, GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return glbuff;
    }

    int makeAndWriteToShortBuffer(short[] data) {
        return makeAndWriteToShortBuffer(data,0,data.length);
    }

    int makeAndWriteToShortBuffer(short[] data,int offset,int len) {
        int glbuff = createBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(len * 2); // (# of coordinate values * 2 bytes per short)
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer fb = bb.asShortBuffer();
        fb.put(data,offset,len);
        fb.position(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,glbuff);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, fb.capacity()*2,fb,GL_STATIC_DRAW);
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        return glbuff;
    }

    void deleteBuffer(int name) {
        if (name <= 0)
            return;
        buffernamearr[0] = name;
        glDeleteBuffers(1,buffernamearr,0);
        decnglbuffers();
    }

    private void decnglbuffers() {
        --nglbuffers;
        if (nglbuffers == 0)
            Log.w(TAG, "model nglbuffers now = 0");
        if (nglbuffers < 0)
            Utils.alert("model nglbuffers < 0");
    }

    // set model verts (3 floats each)
    void setverts(float[] vertsa) {
        if (vertsa.length%3 != 0)
            Utils.alert("verts array not a multiple of 3 on model '" + name + "' length " + vertsa.length);
        verts = vertsa;
        nverts = verts.length/3;
    };

    // set model norms (3 floats each)
    void setnorms(float[] normsa) {
        if (normsa.length%3 != 0)
            Utils.alert("norms array not a multiple of 3 on model '" + name + "' length " + normsa.length);
        norms = normsa;
        if (norms.length/3 != nverts)
            Utils.alert("vert norm mismatch on model '" + name + "'");
    };

    // set model uvs (2 floats each)
    void setuvs(float[] uvsa) {
        if (uvsa.length%2 != 0)
            Utils.alert("uvs array not a multiple of 2 on model '" + name + "' length " + uvsa.length);
        uvs = uvsa;
        if (uvs.length/2 != nverts)
            Utils.alert("vert uv mismatch on model '" + name + "'");
    };

    void setuvs2(float[] uvs2a) {
        if (uvs2a.length%2 != 0)
            Utils.alert("uvs2 array not a multiple of 2 on model '" + name + "' length " + uvs2a.length);
        uvs2 = uvs2a;
        if (uvs2.length/2 != nverts)
            Utils.alert("vert uv2 mismatch on model '" + name + "'");
    };

    // set model cverts (4 floats each)
    void setcverts(float[] cvertsa) {
        if (cvertsa.length%4 != 0)
            Utils.alert("cverts array not a multiple of 4 on model '" + name + "' length " + cvertsa.length);
        cverts = cvertsa;
        if (cverts.length/4 != nverts)
            Utils.alert("vert cvert mismatch on model '" + name + "'");
    };

    // set model faces (1 short each)
    void setfaces(short[] facesa) {
        if (facesa.length%3 != 0)
            Utils.alert("faces array not a multiple of 3 on model '" + name + "' length " + facesa.length);
        faces = facesa;
        nface = faces.length/3;
    };

    // set model mesh
    public void setmesh(Mesh mesh) {
        if (mesh == null)
            return;
        if (mesh.verts != null)
            setverts(mesh.verts);
        if (mesh.norms != null)
            setnorms(mesh.norms);
        if (mesh.uvs != null)
            setuvs(mesh.uvs);
        if (mesh.uvs2 != null)
            setuvs2(mesh.uvs2);
        if (mesh.cverts != null)
            setcverts(mesh.cverts);
        if (mesh.faces != null)
            setfaces(mesh.faces);
    };

    public ModelBase newdup() {
        ++refcount;
        return this;
    }

    public void setbbox() {
        boxmin = new float[] {1e20f,1e20f,1e20f};
        boxmax = new float[] {-1e20f,-1e20f,-1e20f};
        int nv=nverts;
        for (int j=0;j<nv;++j) {
            int i = j*3;
            if (verts[i] < boxmin[0])
                boxmin[0] = verts[i];
            if (verts[i] > boxmax[0])
                boxmax[0] = verts[i];
            if (verts[i+1] < boxmin[1])
                boxmin[1] = verts[i+1];
            if (verts[i+1] > boxmax[1])
                boxmax[1] = verts[i+1];
            if (verts[i+2] < boxmin[2])
                boxmin[2] = verts[i+2];
            if (verts[i+2] > boxmax[2])
                boxmax[2] = verts[i+2];
        }
        /*
        Log.i(TAG,"bounding box for model '" + name + "' is (" +
                boxmin[0] + " " + boxmin[1] + " " + boxmin[2] + ") (" +
                boxmax[0] + " " + boxmax[1] + " " + boxmax[2] + ")");
                */
//	logger("bounding box for model '%s' is (%f,%f,%f) (%f,%f,%f), rad %f\n",
//	  name.c_str(),boxmin.x,boxmin.y,boxmin.z,boxmax.x,boxmax.y,boxmax.z,boxrad);
    }

    public void commit() {
        //Log.e(TAG,"commit base model " + name);
        setbbox();
    }

    protected void setUserModelUniforms(Shader shad,HashMap<String,float[]> mat) {
        for (String key : mat.keySet()) {
            Integer uloc = shad.actunifs.get(key);
            // boolean haskey = shader.actunifs.containsKey(key);
            if (uloc != null) {
                //int autype = shader.actunifs.get(key);
                //int size =
                float[] val = mat.get(key);
                // move Float to float in array
                //int i;
                //for (i=0;i<val.length;++i)
                //    smallFloatBuffer[i] = val[i];
                // assume correct data for shader
                switch(val.length) {
                    //int type = autype;
                    //switch(type) {
                    case 1:
                        //case GL_FLOAT:
                        glUniform1f(uloc,val[0]);
                        break;
                    case 2:
                        //case GL_FLOAT_VEC2:
                        glUniform2fv(uloc,1,val,0);
                        break;
                    case 3:
                        //case GL_FLOAT_VEC3:
                        glUniform3fv(uloc,1,val,0);
                        break;
                    case 4:
                        //case GL_FLOAT_VEC4:
                        glUniform4fv(uloc,1,val,0);
                        break;
                    case 16:
                        //case GL_FLOAT_MAT4:
                        glUniformMatrix4fv(uloc,1,false,val,0);
                        break;
                    default:
                        Utils.alert("unknown size for float uniforms " + val.length);
                }
            }
        }
    }

    // free all opengl resources from this model
    public abstract void draw();

    // returns if resources should be freed
    boolean releaseModel() {
        --refcount;
        if (refcount > 0) {
            return false; // don't free resources
        }
        if (refcount < 0) {
            Utils.alert("ModelBase refcount < 0 in '" + name + "'");
        }
        refcountmodellist.remove(name); // remove from modellist
        return true;
    }

    public abstract void glFree();

    public void buildLog() {
        String className = getClass().getSimpleName();
        modellog += "   " + className + " '" + name + "'";
        modellog += " refcount " + refcount;
        modellog += " verts " + nverts;
        if (nface > 0) {
            modellog += " faces " + nface;
            totalfaces += nface;
        }
        totalverts += nverts;
    }

    public abstract void changemesh(Mesh uvmesh);
}
