package com.mperpetuo.openglgallery.enga;

//import javax.microedition.khronos.opengles.GL10;
//import static javax.microedition.khronos.opengles.GL10.*;

import android.opengl.GLES30;
import android.util.Log;

import static android.opengl.GLES20.*;

public class FrameBufferTexture extends Texture {
    int renderbuffer = -1; // zbuffer part of frame buffer object
    int framebuffer = -1; // the high level frame buffer object
    // gltexture, in base class the color buffer part of frame buffer object

    // intel emulator, webgl like GL_RGBA, qualcomm likes GLES30.GL_RGBA32F for float textures attatched to a framebuffer
    static boolean useAlt = false; // set to true when reverting to alternate for example qualcomm device

    private int makeFrameBuffers(int wid, int hit) {
        gltexture = makeATexture();
        glBindTexture(GL_TEXTURE_2D, gltexture);
        // frame buffer texture filtering
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        //glTtexParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR_MIPMAP_NEAREST); // need power of 2 textures


        GLUtil.ignoreGLError(true);
        boolean ext1 = Main3D.hasFloatTextures;
        boolean ext2 = Main3D.hasFloatLinearTextures;
        //ext1 = false; // can't get float textures to work on emulator
        if (!ext1)
            Log.e(TAG,"no float textures");
        if (!ext2)
            Log.e(TAG,"no float textures with linear filtering");
        //ext1 = false;
        if (ext1) {
            // which one to use ??
            // intel, webgl happy
            //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, wid, hit, 0, GL_RGBA, GL_FLOAT, null);
            // qualcomm happy
            //glTexImage2D(GL_TEXTURE_2D, 0, GLES30.GL_RGBA32F, wid, hit, 0, GL_RGBA, GL_FLOAT, null);
            int format = useAlt ? GLES30.GL_RGBA32F : GL_RGBA; // test intel then qualcomm
            //int format = useAlt ? GL_RGBA : GLES30.GL_RGBA32F; // test qualcomm then intel
            glTexImage2D(GL_TEXTURE_2D, 0, format, wid, hit, 0, GL_RGBA, GL_FLOAT, null);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, wid, hit, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        }
        GLUtil.checkGlError("glTexImage2D for frame buffer");
        glBindTexture(GL_TEXTURE_2D,0);
        GLUtil.checkGlError("texture unbind for frame buffer");

        //glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,wid,hit,0,GL_RGBA,GL_UNSIGNED_BYTE,null);
        GLUtil.ignoreGLError(false);

        //gl.generateMipmap(gl.TEXTURE_2D);
        // create zbuffer
        renderbuffer = createARenderbuffer();
        glBindRenderbuffer(GL_RENDERBUFFER,renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH_COMPONENT16,wid,hit); // zbuffer
        glBindRenderbuffer(GL_RENDERBUFFER,0);
        GLUtil.checkGlError("zbuffer unbind for frame buffer");

        // create frame buffer
        framebuffer = createAFramebuffer();
        glBindFramebuffer(GL_FRAMEBUFFER,framebuffer);
        // attach texture and zbuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,gltexture,0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER,renderbuffer);

        int fpstat = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (fpstat == GL_FRAMEBUFFER_COMPLETE)
            Log.e(TAG,"FrameBuffer Complete!");
        else
            Log.e(TAG,"FrameBuffer INComplete!!! " + fpstat);
        // unbind
        //glBindTexture(GL_TEXTURE_2D,0);
        //glBindRenderbuffer(GL_RENDERBUFFER,0);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        return fpstat;
    }

    protected FrameBufferTexture(String aname,int wid,int hit) {
        refcount = 1;
        name = aname;
        refcounttexturelist.put(aname,this);
        // find a good format
        while(true) {
            int status = makeFrameBuffers(wid,hit);
            if (status == GL_FRAMEBUFFER_COMPLETE) {
                Log.e(TAG,"framebuffer: found a complete framebuffer");
                break;
            }
            freeBuffers();
            if (useAlt == true) {
                Log.e(TAG,"framebuffer: giving up on complete framebuffer");
                throw new RuntimeException("framebuffer: giving up on building a complete framebuffer");
            }
            Log.e(TAG,"framebuffer: switching to alt");
            useAlt = true;
        }
        name = aname;
        width = wid;
        height = hit;
        hasalpha = false;
        iscubemap = false;
        GLUtil.checkGlError("end FrameBufferTexture constructor");
    }

    public static FrameBufferTexture createTexture(String aname, int width, int height) {
        Texture aftex = refcounttexturelist.get(aname);
        if (aftex != null) {
            ++aftex.refcount;
        } else {
            aftex = new FrameBufferTexture(aname, width, height);
        }
        return (FrameBufferTexture) aftex;
    }

    int createAFramebuffer() {
        glGenFramebuffers(1,texnamearr,0);
        ++ngltextures;
        return texnamearr[0];
    }

    int createARenderbuffer() {
        glGenRenderbuffers(1,texnamearr,0);
        ++ngltextures;
        return texnamearr[0];
    }

    void deleteARenderbuffer(int tn) {
        if (tn <= 0)
            return;
        texnamearr[0] = tn;
        glDeleteRenderbuffers(1, texnamearr, 0);
        decngltextures();
    }

    void deleteAFramebuffer(int tn) {
        if (tn <= 0)
            return;
        texnamearr[0] = tn;
        glDeleteFramebuffers(1, texnamearr, 0);
        decngltextures();
    }

    @Override
    public void glFree() {
        --refcount;
        if (refcount > 0) {
            return;
        }
        if (refcount < 0) {
            Utils.alert("FrameBufferTexture refcount < 0 in '" + name + "'");
        }
        refcounttexturelist.remove(name);
        freeBuffers();
    }

    private void freeBuffers() {
        deleteATexture(gltexture);
        gltexture = -1;
        deleteARenderbuffer(renderbuffer);
        renderbuffer = -1;
        deleteAFramebuffer(framebuffer);
        framebuffer = -1;
    }

    public static void useframebuffer(int fbtn) {
        if (fbtn > 0)
            glBindFramebuffer(GL_FRAMEBUFFER,fbtn);
        else
            glBindFramebuffer(GL_FRAMEBUFFER,0);
    }

    public void resize(int w,int h) {
        if (w == width && h == height) {
            Log.i(TAG,"framebuffer textures same size = " + w + " " + h);
            return;
        }
        Log.i(TAG,"framebuffer textures changing from " + width + " " + height + " to "  + w + " " + h);
        freeBuffers();
        makeFrameBuffers(w,h);
        width = w;
        height = h;
    }

}
