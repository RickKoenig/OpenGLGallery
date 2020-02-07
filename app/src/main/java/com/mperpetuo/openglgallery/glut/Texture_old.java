package com.mperpetuo.openglgallery.glut;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;

/**
 * Created by rickkoenig on 1/25/16.
 */
public class Texture_old {

    // simple texture class that has width,height and texture id, constructed from a bitmap
    int width,height;
    int id;
    Texture_old(Bitmap bm) {
        width = bm.getWidth();
        height = bm.getHeight();
        int[] arr = new int[1];
        glGenTextures(1, arr, 0);
        id = arr[0];
        glBindTexture(GL_TEXTURE_2D,id);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bm, 0);
    }

    public int getTextureID() {
        return id;
    }

    public PointF getSize() {
        return new PointF(width,height);
    }
}
