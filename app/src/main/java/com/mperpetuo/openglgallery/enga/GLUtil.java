package com.mperpetuo.openglgallery.enga;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static android.opengl.GLES20.*;
import static com.mperpetuo.openglgallery.enga.Main3D.maxTextures;

/**
 * Created by rickkoenig on 2/29/16.
 */
public class GLUtil {
    static final String TAG = "GLUtil";

    public static final HashMap<Integer,String> glTypeNames = new HashMap<>();

    public static HashMap<String,Shader> shaderList = new HashMap<>();
    static int[] arr1 = new int[1];
    static int[] arr2 = new int[1];
    static int[] arr3 = new int[1];
    static final int byteSize = 100;
    static byte[] bytes = new byte[byteSize];

    public static HashMap<String,float[]> globalmat = new HashMap<>();

    static float[] mvMatrix = new float[16];
    static float[] pMatrix = new float[16];
    static float[] v2wMatrix = new float[16];
    static final float[] LpMatrix = new float[16];

    public static Tree curtree;

    static int nactiveattribs = 0;

    static boolean glIgnoreErrs = false;

    // show reference counted resource lists
    public static void logrc() {
        Log.i(TAG,"GL textures = " + Texture.ngltextures);
        Log.i(TAG,"GL buffers = " + ModelBase.nglbuffers);
        ModelBase.modelrc();
        Texture.texturerc();
    }

    public static String getGlTypeName(int tp) {
        String r = glTypeNames.get(tp);
        if (r == null)
            r = "Unknown";
        return r;
    }

    // setup opengl environment and load all the shaders
    public static void init() {
        Matrix.setIdentityM(mvMatrix,0);
        Matrix.setIdentityM(pMatrix,0);
        Matrix.setIdentityM(v2wMatrix,0);
        curtree = null;
        nactiveattribs = 0;
        globalmat.clear();
        globalmat.put("alphacutoff", new float[]{.05f});
        globalmat.put("specpow", new float[]{500f});
        globalmat.put("LpMatrix",LpMatrix);

        glTypeNames.clear();
        glTypeNames.put(GL_FLOAT, "GL_FLOAT");
        glTypeNames.put(GL_FLOAT_VEC2, "GL_FLOAT_VEC2");
        glTypeNames.put(GL_FLOAT_VEC3, "GL_FLOAT_VEC3");
        glTypeNames.put(GL_FLOAT_VEC4, "GL_FLOAT_VEC4");
        glTypeNames.put(GL_FLOAT_MAT4, "GL_MAT4");
        glTypeNames.put(GL_SAMPLER_2D, "GL_SAMPLER_2D");
        glTypeNames.put(GL_SAMPLER_CUBE, "GL_SAMPLER_CUBE");

        initShaders();

        // Set the background frame color
        glClearColor(0.0f, 0.0f, .5f, 1.0f);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // face culling
        glFrontFace(GL_CW);
        glCullFace(GL_BACK);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST); // almost forgot this one!!

        // How big can points be for point cloud?
        FloatBuffer sizes = FloatBuffer.allocate(2);
        glGetFloatv(GL_ALIASED_POINT_SIZE_RANGE, sizes);
        Log.i(TAG, "pointcloud pointsizes min " + sizes.get(0) + " max " + sizes.get(1));
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);

        //exitShaders();
        //initShaders();
    }

    // enable only the ones we need
    static void setAttributes(Shader shaderProgram) {
        int i, n = shaderProgram.actattribs.size();
        // assume attribs count from 0
        if (n > nactiveattribs) {
            for (i = nactiveattribs; i < n; ++i)
                glEnableVertexAttribArray(i);
        } else if (n < nactiveattribs) {
            for (i = n; i < nactiveattribs; ++i)
                glDisableVertexAttribArray(i);

        }
        nactiveattribs = n;
    }

    static void setMatrixModelViewUniforms(Shader shaderProgram) {
        boolean passthru = false;
        if (passthru) {
            Matrix.setIdentityM(mvMatrix,0);
            Matrix.setIdentityM(pMatrix,0);
            Matrix.setIdentityM(v2wMatrix,0);
        }
        //mvMatrix[13] += 1.0f;
        if (shaderProgram.actunifs.containsKey("mvMatrixUniform"))
            glUniformMatrix4fv(shaderProgram.actunifs.get("mvMatrixUniform"), 1,false, mvMatrix,0); // model view
        if (shaderProgram.actunifs.containsKey("pMatrixUniform"))
            glUniformMatrix4fv(shaderProgram.actunifs.get("pMatrixUniform"), 1,false, pMatrix,0); // perspective
        if (shaderProgram.actunifs.containsKey("v2wMatrix")) // view to world
            glUniformMatrix4fv(shaderProgram.actunifs.get("v2wMatrix"), 1,false, v2wMatrix,0); // for env map and shadowmapping
    }

    public static void setSamplerUniforms(Shader shaderProgram) {
        for (int i=0;i<maxTextures;++i) {
            String samplerName = "uSampler" + i;
            if (shaderProgram.actsamplers.containsKey(samplerName))
                glUniform1i(shaderProgram.actsamplers.get(samplerName), i);
        }
        /*
        if (shaderProgram.actsamplers.containsKey("uSampler0"))
            //glUniform1i(0,0);
            glUniform1i(shaderProgram.actsamplers.get("uSampler0"), 0);
        if (shaderProgram.actsamplers.containsKey("uSampler1"))
            glUniform1i(shaderProgram.actsamplers.get("uSampler1"), 1); */
    }

    static void initShaders() {
        Context cont = Utils.getContext();
        String subName = null;
        Utils.pushandsetdir("shaders");
        shaderList.clear();
        try {
            AssetManager assetManager = cont.getAssets();
            //String[] aList = assetManager.list("shaders");
            String[] aList = Utils.getdirFromAsset();
            Log.d(TAG,"list of shader files");
            for (String s : aList) {
                int idx = s.indexOf(".vert.glsl");
                if (idx != -1) {
                    subName = s.substring(0, idx);
                    Log.d(TAG,"======================= sub shader name = '" + subName + "'");
                    Shader sh = new Shader();
                    sh.name = subName;
                    sh.glshader = loadShader(subName);
                    shaderList.put(subName, sh);
                    // got a complete shader program, let's study it

                    // study uniforms
                    glGetProgramiv(sh.glshader,GL_ACTIVE_UNIFORMS,arr1,0);
                    int numUniforms = arr1[0];
                    int i;
                    for (i=0;i<numUniforms;++i) {
                        glGetActiveUniform(sh.glshader,i,byteSize, arr1,0,arr2,0,arr3,0,bytes,0);
                        int len = arr1[0];
                        int size = arr2[0];
                        int type = arr3[0];
                        String uniformName = new String(bytes,0,len, "UTF-8");
                        int uniformLocation = glGetUniformLocation(sh.glshader,uniformName);
                        if (type == GL_SAMPLER_2D || type == GL_SAMPLER_CUBE) {
                            sh.actsamplers.put(uniformName,uniformLocation);
                        } else {
                            sh.actunifs.put(uniformName,uniformLocation);
                        }
                        Log.d(TAG,"uniform " + i + ": " + uniformName + " len " + len + " size " + size + " type " + getGlTypeName(type) + " uniform location " + uniformLocation);
                    }

                    // study attributes
                    glGetProgramiv(sh.glshader,GL_ACTIVE_ATTRIBUTES,arr1,0);
                    int numAttribs = arr1[0];
                    for (i=0;i<numAttribs;++i) {
                        glGetActiveAttrib(sh.glshader, i, byteSize, arr1, 0, arr2, 0, arr3, 0, bytes, 0);
                        int len = arr1[0];
                        int size = arr2[0];
                        int type = arr3[0];
                        String attribName = new String(bytes,0,len, "UTF-8");
                        int attribLocation = glGetAttribLocation(sh.glshader, attribName);
                        sh.actattribs.put(attribName,attribLocation);
                        Log.d(TAG,"attribute " + i + ": " + attribName + " len " + len + " size " + size + " type " + getGlTypeName(type) + " attribute location " + attribLocation);
                    }

                    checkGlError("look at uniforms");
                    glUseProgram(sh.glshader);
                    setSamplerUniforms(sh);
                    //setMatrixPersUniforms(shaderProgram);

                } else {
                    Log.d(TAG,"skipping file '" + s + "' not a .vert.glsl");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Shaderlist, size = " + shaderList.size());
        for (String sn : shaderList.keySet()) {
            Shader sh = shaderList.get(sn);
            Log.d(TAG,"shader: " + sh.name + " glname " + sh.glshader);
        }
        Utils.popdir();
    }

    public static void exitShaders() {
        for (String sn : shaderList.keySet()) {
            Shader sh = shaderList.get(sn);
            glDeleteProgram(sh.glshader);
        }
        shaderList.clear();
    }

    // check for opengl errors
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            if (!glIgnoreErrs)
                throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public static void ignoreGLError(boolean enable) {
        if (!enable && glIgnoreErrs)
            checkGlError("turning OFF ignore GL errors");
        if (enable && !glIgnoreErrs)
            checkGlError("turning ON ignore GL errors");
        glIgnoreErrs = enable;
    }

    // load a complete shader from an asset prefix, example loadShader("tex");
    public static int loadShader(String shadername) {
        String vertexString = Utils.getStringFromAsset(shadername + ".vert.glsl", true);
        String fragmentString = Utils.getStringFromAsset(shadername + ".frag.glsl", true);
        int vertexShader = loadShaderPart(GL_VERTEX_SHADER,vertexString);
        int fragmentShader = loadShaderPart(GL_FRAGMENT_SHADER,fragmentString);
        int mProgram = glCreateProgram();             // create empty OpenGL Program
        glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        glLinkProgram(mProgram);                  // create OpenGL program executables
        glDetachShader(mProgram, vertexShader);
        glDetachShader(mProgram,fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        checkShader(mProgram);
        checkGlError("loadShader " + shadername);
        return mProgram;
    }

    // load either a vertex or a fragment shader
    private static int loadShaderPart(int type, String shaderCode) {
        // create a vertex shader type (GL_VERTEX_SHADER)
        // or a fragment shader type (GL_FRAGMENT_SHADER)
        int shader = glCreateShader(type);

        // add the source code to the shader and compile it
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        int[] compiled = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String info = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new RuntimeException("Could not compile shader " + type + ":" + info);
        }
        return shader;
    }

    // show some info about shaders
    private static void checkShader(int program) {
        ByteBuffer bb = ByteBuffer.allocateDirect(4); // 4 bytes per int
        bb.order(ByteOrder.nativeOrder());
        IntBuffer total = bb.asIntBuffer();
        glGetProgramiv(program, GL_ACTIVE_UNIFORMS, total);
        Log.d("checkShader", "num uniforms = " + total.get(0));
        glGetProgramiv(program, GL_ACTIVE_ATTRIBUTES, total);
        Log.d("checkShader","num attribs = " + total.get(0));
    }

}
