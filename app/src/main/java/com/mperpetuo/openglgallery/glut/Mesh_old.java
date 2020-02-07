package com.mperpetuo.openglgallery.glut;

import android.util.Log;


import com.mperpetuo.openglgallery.enga.GLUtil;

import java.nio.*;
import static android.opengl.GLES20.*;

class Mesh_old {
    // geometry of object, vertices and faces
    //private final int mProgram;
    private int aVBO; // load vertices onto gpu handle

    private int aFBO; // load faces onto gpu handle

    private int aProgram;

    private int mPositionHandle;
    private int mUVHandle;
    private int mSamplerHandle;
    private int mPosMatrixHandle;

    private int faceDataLength;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 5;
    static final int COORDS_PER_POS = 3;
    static final int COORDS_PER_UV = 2;
    /*static float squareCoords[] = {
            -1,-1,0,  0,1,   // bottom left
            1,-1,0,  1,1,   // bottom right
            -1, 1,0,  0,0,   // top left
            1, 1,0,  1,0 }; // top right */
    //private final short faces[] = { 0, 1, 2, 3, 2, 1 }; // order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per number

    // for now just pass in 5 floats per point and 3 shorts per face
    public Mesh_old(float[] vertexData, short faceData[], int prog) {

        faceDataLength = faceData.length;

        // keep the shader
        aProgram = prog;

        // load vertex buffer into gpu
        FloatBuffer vertexBuffer;
        ShortBuffer faceBuffer;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexData.length * 4); // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);

        // load face buffer into gpu
        // initialize byte buffer for the face list
        ByteBuffer dlb = ByteBuffer.allocateDirect(faceData.length * 2); // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder());
        faceBuffer = dlb.asShortBuffer();
        faceBuffer.put(faceData);
        faceBuffer.position(0);

        int buffers[] = new int[1];
        glGenBuffers(1, buffers, 0);
        aVBO = buffers[0];

        // Bind to the buffer. Future commands will affect this buffer specifically.
        glBindBuffer(GL_ARRAY_BUFFER, aVBO);

        // Transfer data from client memory to the buffer.
        // We can release the client memory after this call.
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4,
                vertexBuffer, GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        vertexBuffer = null;
        // build tri faceData
        buffers = new int[1];
        glGenBuffers(1, buffers, 0);
        aFBO = buffers[0];

        // Bind to the buffer. Future commands will affect this buffer specifically.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, aFBO);

        // Transfer data from client memory to the buffer.
        // We can release the client memory after this call.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, faceBuffer.capacity() * 2,
                faceBuffer, GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        faceBuffer = null;


        // get handle to vertex shader's vPosition member
        mPositionHandle = glGetAttribLocation(aProgram, "vPosition");
        GLUtil.checkGlError("glGetAttribLocation");

        // Enable a handle to the square vertices
        glEnableVertexAttribArray(mPositionHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // get handle to vertex shader's vPosition member
        mUVHandle = glGetAttribLocation(aProgram, "vUV");
        GLUtil.checkGlError("glGetAttribLocation");

        // Enable a handle to the triangle vertices
        glEnableVertexAttribArray(mUVHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // get texture sampler uniform
        mSamplerHandle = glGetUniformLocation(aProgram, "tex_sampler");
        GLUtil.checkGlError("glGetUniformLocation");

        // get handle to shape's transformation matrix
        mPosMatrixHandle = glGetUniformLocation(aProgram, "posMatrix");
        GLUtil.checkGlError("glGetUniformLocation posMatrix");


        Log.w("mesh constructor", "Done!");
    }

    //public void draw(float[] mvMatrix,float[] pMatrix) {
    public void draw(float[] posMatrix,int textureHandle) {
        // Add program to OpenGL environment
        glUseProgram(aProgram);

        // Enable a handle to the meshes vertices pos
        glEnableVertexAttribArray(mPositionHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // Enable a handle to the meshes vertices uv
        glEnableVertexAttribArray(mUVHandle);
        GLUtil.checkGlError("glEnableVertexAttribArray");

        // Prepare the triangle coordinate data
        glBindBuffer(GL_ARRAY_BUFFER, aVBO);
        glVertexAttribPointer(mPositionHandle, COORDS_PER_POS,
                GL_FLOAT, false,
                vertexStride, 0);

        glVertexAttribPointer(mUVHandle, COORDS_PER_UV,
                GL_FLOAT, false,
                vertexStride, COORDS_PER_POS * 4);
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        glBindTexture(GL_TEXTURE_2D, textureHandle);
        GLUtil.checkGlError("glBindTexture");

        //glUniform1i(mSamplerHandle, 0); // set this sampler to slot 0
        //GLUtils.checkGlError("glUniform1i");

        // Apply the projection and view transformation
        glUniformMatrix4fv(mPosMatrixHandle, 1, false, posMatrix, 0);
        GLUtil.checkGlError("glUniformMatrix4fv");

        // Draw the square
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, aFBO);
        glDrawElements(GL_TRIANGLES, faceDataLength, GL_UNSIGNED_SHORT, 0);
        GLUtil.checkGlError("glDrawElements");

        // Disable vertex array
        glDisableVertexAttribArray(mPositionHandle);
        GLUtil.checkGlError("glDisableVertexAttribArray");
        glDisableVertexAttribArray(mUVHandle);
        GLUtil.checkGlError("glDisableVertexAttribArray");

        // unbind any buffers
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
