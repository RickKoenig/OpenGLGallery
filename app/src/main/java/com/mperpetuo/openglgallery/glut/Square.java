package com.mperpetuo.openglgallery.glut;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by cyberrickers on 12/26/2015.
 */
class Square {
    private FloatBuffer vertexBuffer;
    private ShortBuffer faceBuffer;
    //private final int mProgram;
    private int mPositionHandle;
    private int mUVHandle;
    private int mSampler;
    private int mPosMatrixHandle;
    private int mTextureHandle;

    boolean useVBO = true; // use vertex buffer gpu objects, otherwise pass vertices to gpu every draw
    private int aVBO; // load vertices onto gpu handle

    boolean useFBO = true; // use face buffer gpu objects, otherwise pass faces to gpu every draw
    private int aFBO; // load faces onto gpu handle

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 5;
    static final int COORDS_PER_POS = 3;
    static final int COORDS_PER_UV = 2;
    static float squareCoords[] = {
            -1,-1,0,  0,1,   // bottom left
             1,-1,0,  1,1,   // bottom right
            -1, 1,0,  0,0,   // top left
             1, 1,0,  1,0 }; // top right
    private final short faces[] = { 0, 1, 2, 3, 2, 1 }; // order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per number
    private float aspect;

    public Square(Context cont) {
        // build a texture
        int[] texarr = new int[1];
        glGenTextures(1, texarr, 0);
        mTextureHandle = texarr[0];
        glBindTexture(GL_TEXTURE_2D, mTextureHandle);
        //Bitmap bm = BitmapFactory.decodeResource(cont.getResources(), R.drawable.caution);
        Bitmap bm = Utils.getBitmapFromAsset("common/caution.png");
        aspect = (float)bm.getWidth()/bm.getHeight();
        //Bitmap bm = BitmapFactory.decodeResource(cont.getResources(), R.drawable.maptestnck);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bm, 0);
        GLUtil.checkGlError("texImage2D");
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4); // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the face list
        ByteBuffer dlb = ByteBuffer.allocateDirect(faces.length * 2); // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder());
        faceBuffer = dlb.asShortBuffer();
        faceBuffer.put(faces);
        faceBuffer.position(0);
/*
        // prepare shaders and OpenGL program
        int vertexShader = GLUtils.loadShader(GL_VERTEX_SHADER,Globals.tex_vs); //vertexShaderCode
        int fragmentShader = GLUtils.loadShader(GL_FRAGMENT_SHADER,Globals.tex_ps); //fragmentShaderCode
        mProgram = glCreateProgram();             // create empty OpenGL Program
        glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        glLinkProgram(mProgram);                  // create OpenGL program executables
        GLUtils.checkShader(mProgram);              // get some info about shader */

        if (useVBO) { // transfer vertices to gpu
            final int buffers[] = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            aVBO = buffers[0];

            // Bind to the buffer. Future commands will affect this buffer specifically.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aVBO);

            // Transfer data from client memory to the buffer.
            // We can release the client memory after this call.
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4,
                    vertexBuffer, GLES20.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            vertexBuffer = null;
        }
        if (useFBO) { // transfer faces to gpu
            // build tri faces
            final int buffers[] = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            aFBO = buffers[0];

            // Bind to the buffer. Future commands will affect this buffer specifically.
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, aFBO);

            // Transfer data from client memory to the buffer.
            // We can release the client memory after this call.
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, faceBuffer.capacity() * 2,
                    faceBuffer, GLES20.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            faceBuffer = null;
        }
        Log.w("square constructor", "Done!");
    }

    //public void draw(float[] mvMatrix,float[] pMatrix) {
    public void draw(float[] posMatrix,int mProgram,int altTextureHandle) {
        // Add program to OpenGL environment
        glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = glGetAttribLocation(mProgram, "vPosition");
        GLUtil.checkGlError("glGetAttribLocation");

        // Enable a handle to the square vertices
        glEnableVertexAttribArray(mPositionHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // get handle to vertex shader's vPosition member
        mUVHandle = glGetAttribLocation(mProgram, "vUV");
        GLUtil.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        glEnableVertexAttribArray(mUVHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // Prepare the triangle coordinate data
        if (useVBO) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, aVBO);
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_POS,
                    GLES20.GL_FLOAT, false,
                    vertexStride, 0);

            GLES20.glVertexAttribPointer(mUVHandle, COORDS_PER_UV,
                    GLES20.GL_FLOAT, false,
                    vertexStride, COORDS_PER_POS*4);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        } else {
            vertexBuffer.position(0);
            glVertexAttribPointer(mPositionHandle, COORDS_PER_POS,
                    GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            GLUtil.checkGlError("glVertexAttribPointer");
            vertexBuffer.position(3);
            glVertexAttribPointer(mUVHandle, COORDS_PER_UV,
                    GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            GLUtil.checkGlError("glVertexAttribPointer");
        }

        // get texture sampler uniform
        mSampler = glGetUniformLocation(mProgram,"tex_sampler");
        GLUtil.checkGlError("glGetUniformLocation");
        glUniform1i(mSampler, 0);
        GLUtil.checkGlError("glUniform1i");
        if (altTextureHandle > 0)
            glBindTexture(GL_TEXTURE_2D, altTextureHandle);
        else
            glBindTexture(GL_TEXTURE_2D, mTextureHandle);
        GLUtil.checkGlError("glBindTexture");

        // get handle to shape's transformation matrix
        mPosMatrixHandle = glGetUniformLocation(mProgram, "posMatrix");
        GLUtil.checkGlError("glGetUniformLocation posMatrix");

        // Apply the projection and view transformation
        glUniformMatrix4fv(mPosMatrixHandle, 1, false, posMatrix, 0);
        GLUtil.checkGlError("glUniformMatrix4fv");

        // Draw the square
        if (useFBO) { // draw faces already loaded onto gpu
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,aFBO);
            glDrawElements(GL_TRIANGLES, faces.length, GL_UNSIGNED_SHORT, 0);
        } else { // draw faces transferred to gpu
            glDrawElements(GL_TRIANGLES, faces.length, GL_UNSIGNED_SHORT, faceBuffer);
        }
        GLUtil.checkGlError("glDrawElements");

        // Disable vertex array
        glDisableVertexAttribArray(mPositionHandle);
        GLUtil.checkGlError("glDisableVertexAttribArray");
        glDisableVertexAttribArray(mUVHandle);
        GLUtil.checkGlError("glDisableVertexAttribArray");

        // unbind any buffers
        if (useVBO)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        if (useFBO)
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    float getAspect() {
        return aspect;
    }
}
