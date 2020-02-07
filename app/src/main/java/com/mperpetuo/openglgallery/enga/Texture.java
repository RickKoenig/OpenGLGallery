package com.mperpetuo.openglgallery.enga;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.*;

public class Texture {
    // STATICS
    static final String TAG = "Texture";
    static Map<String,Texture> refcounttexturelist;
    public static final int FLAG_CLAMPU = 0x100;
    public static final int FLAG_CLAMPV = 0x200;
    public static final int FLAG_CLAMPUV = FLAG_CLAMPU | FLAG_CLAMPV;
    public static final int FLAG_NOFLOAT = 0x400; // not used, always no float frame buffer textures

    static int ngltextures;

    private static int globaltexflags = 0;//FLAG_CLAMPUV; // default is now wrap

    static int texnamearr[] = new int[1]; // just for glGenTextures etc.

    String name;
    int refcount;
    int gltexture;
    int width;
    int height;
    boolean hasalpha;
    boolean iscubemap;

    static class CubeEnum {
        String key;
        int x;
        int y;
        CubeEnum(String keya,int xa,int ya) {
            key = keya;
            x = xa;
            y = ya;
        }
    }

    static CubeEnum[] cubeenums = {
            new CubeEnum("posz",1,1),
            new CubeEnum("negz",3,1),
            new CubeEnum("posx",2,1),
            new CubeEnum("negx",0,1),
            new CubeEnum("posy",1,0),
            new CubeEnum("negy",1,2),
    };

    static int[]  glcmfaceenums = { // make match cubeenums
            GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
            GL_TEXTURE_CUBE_MAP_POSITIVE_X,
            GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
            GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
            GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
    };

    static void init() {
        ngltextures = 0;
        refcounttexturelist = new HashMap<>();
    }

    static int makeATexture() {
        glGenTextures(1,texnamearr,0);
        ++ngltextures;
        return texnamearr[0];
    }

    static void deleteATexture(int tn) {
        if (tn <= 0)
            return;
        texnamearr[0] = tn;
        glDeleteTextures(1, texnamearr, 0);
        decngltextures();
    }

    static void setSamplerProperties() {
        if ((globaltexflags & FLAG_CLAMPU) != 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        else
            ;//glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        if ((globaltexflags & FLAG_CLAMPV) != 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        else
            ;//glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    static void setCubeMapSamplerProperties() {
        // cubemap texture filtering
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtil.checkGlError("cubTexture2");
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLUtil.checkGlError("cubTexture3");
        //glTtexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, gl.NEAREST);
        //glTtexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, gl.NEAREST);
        //if (globaltexflags & textureflagenums.CLAMPU)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLUtil.checkGlError("cubTexture4");
        //if (globaltexflags & textureflagenums.CLAMPV)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        GLUtil.checkGlError("cubTexture5");

    }

    static int bitmap2gltexture(Bitmap bm,int cubface) {
        GLUtil.checkGlError("bmtoglt 1");
        int texture;
        int bind;
        if (cubface >= 0) {
            texture = -1;
            bind = glcmfaceenums[cubface];
        } else {
            bind = GL_TEXTURE_2D;
            texture = makeATexture();
            glBindTexture(bind, texture);
        }
        GLUtil.checkGlError("bmtoglt 2");
        // read whole texture in, automatic
        if (bm == null)
            Log.e(TAG,"texImage2d null bitmap");
        GLUtils.texImage2D(bind, 0, bm, 0);
        GLUtil.checkGlError("bmtoglt 3");
        if (cubface < 0) {
            setSamplerProperties();
        }
        GLUtil.checkGlError("bmtoglt 4");
        return texture;
    }

    private void cubTexture(String aname) {
        Utils.pushandsetdir("skybox");
        gltexture = Texture.makeATexture();
        GLUtil.checkGlError("cubTexture0");
        iscubemap = true;
        glBindTexture(GL_TEXTURE_CUBE_MAP,gltexture);
        GLUtil.checkGlError("cubTexture1");
        String sname = aname.substring(4); // remove 'CUB_'
        Bitmap baseBitmap = Utils.getBitmapFromAsset(sname);
        if (baseBitmap != null) { // cross
            //Log.e(TAG,"cross bitmap found, called " + sname + " size = " + abitmap.getWidth() + " " + abitmap.getHeight());
            width = baseBitmap.getWidth()/4;
            height = baseBitmap.getHeight()/3;
            int i;
            for (i=0;i<cubeenums.length;++i) {
                CubeEnum ce = cubeenums[i];
                int aw = baseBitmap.getWidth();
                int ah = baseBitmap.getHeight();
                int sx = aw/4;
                int sy = ah/3;
                int xi = ce.x;
                int yi = ce.y;
                int xoff = xi*aw/4;
                int yoff = yi*ah/3;
                Bitmap subBitmap = Bitmap.createBitmap(baseBitmap,xoff,yoff,sx,sy);
                bitmap2gltexture(subBitmap,i);
            }
        } else { // folder of 6 files
            int i;
            for (i=0;i<cubeenums.length;++i) {
                String bmName = sname + "/" + cubeenums[i].key + ".jpg";
                Bitmap abitmap = Utils.getBitmapFromAsset(bmName);
                width = abitmap.getWidth();
                height = abitmap.getHeight();
                GLUtil.checkGlError("cubTexture2 before");
                bitmap2gltexture(abitmap, i);
                GLUtil.checkGlError("cubTexture2 after");
            }
        }
        setCubeMapSamplerProperties();
        //glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        hasalpha = false; //abitmap.hasalpha;
        GLUtil.checkGlError("cubTexture7");
        Utils.popdir();
    }

    // default
    protected Texture() {
    }

    // rgba
    protected Texture(String rname, int widtha, int heighta, byte[] data) {
        refcount = 1;
        name = rname;
        refcounttexturelist.put(rname,this);
        int bind = GL_TEXTURE_2D;
        int texture = makeATexture();
        gltexture = texture;
        glBindTexture(bind, texture);
        int np = widtha*heighta;
        if (np*4 != data.length)
            throw new RuntimeException("texture data size mismatch");
        ByteBuffer bb = ByteBuffer.allocateDirect(np * 4);    // (# of coordinate values * 4 bytes per RGBA)
        bb.order(ByteOrder.nativeOrder());
        bb.put(data, 0, np * 4);
        bb.position(0);
        glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,widtha,heighta,0, GL_RGBA,GL_UNSIGNED_BYTE,bb);
        setSamplerProperties();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        width = widtha;
        height = heighta;
        for (int i=0;i<data.length;i+=4) {
            if (data[i+3] != -1) {
                hasalpha = true;
                break;
            }
        }
    }

    // reference count name, asset name
    protected Texture(String rname,String aname) {
        GLUtil.checkGlError("start Texture constructor " + name);
        refcount = 1;
        name = rname;
        refcounttexturelist.put(rname,this);
        if (name.indexOf("CUB_") == 0) {
            cubTexture(aname);
            return;
        }
        Bitmap abitmap = Utils.getBitmapFromAsset(aname);
        // if failed with .jpg try .png
        if (abitmap == null) {
            String ext = Utils.getExt(aname);
            if (ext.equals("dds") || ext.equals("DDS") || ext.equals("JPG") || ext.equals("jpg") || ext.equals("tga"))  // extension was .dds or .tga or .jpg try .png instead
                aname = aname.substring(0, aname.length() - 3) + "png";
            abitmap = Utils.getBitmapFromAsset(aname);
        }
        /*if (aname.equals("ColWall.png")) { // what is wrong with this .png ??? checkpoints, make an alt bitmap thats invisible
            abitmap = Bitmap.createBitmap(2,2, Bitmap.Config.ARGB_8888);
        } else if (aname.equals("checkered.png")) { // what is wrong with this .png ??? start finish line, some colored bitmap
            int[] colors1 = new int[]{
                    Color.argb(255, 255, 0, 0),
                    Color.argb(255, 0, 255, 0),
                    Color.argb(255, 0, 255, 0),
                    Color.argb(255, 0, 0, 255),
                    Color.argb(255, 255, 255, 255),
            };
            abitmap = Bitmap.createBitmap(colors1, 2, 2, Bitmap.Config.ARGB_8888);
        }*/
        GLUtil.checkGlError("mid 1 Texture constructor " + name);
        if (abitmap == null) {
// maybe cross formation, strip out last slash and what's ahead of it
            int ls = aname.lastIndexOf("/"); // last slash
            if (ls > 0) {
                String sname = aname.substring(0,ls);
                //Log.e(TAG,"stripped string for skybox cross = " + sname);
                abitmap = Utils.getBitmapFromAsset(sname);
                if (abitmap != null) {
                    //Log.e(TAG,"cross bitmap found, called " + sname + " size = " + abitmap.getWidth() + " " + abitmap.getHeight());
                    String face = aname.substring(ls + 1);
                    //Log.e(TAG,"face = " + face);
                    int dot = face.lastIndexOf(".");
                    String key = "";
                    if (dot > 0)
                        key = face.substring(0,dot);
                    //Log.e(TAG,"key = " + key);
                    int i;
                    for (i=0;i<cubeenums.length;++i) {
                        CubeEnum ce = cubeenums[i];
                        if (ce.key.equals(key)) {
                            int aw = abitmap.getWidth();
                            int ah = abitmap.getHeight();
                            int sx = aw/4;
                            int sy = ah/3;
                            int xi = ce.x;
                            int yi = ce.y;
                            int xoff = xi*aw/4;
                            int yoff = yi*ah/3;
                            abitmap = Bitmap.createBitmap(abitmap,xoff,yoff,sx,sy);
                            break;
                        }
                    }
                    if (i == cubeenums.length)
                        abitmap = null;
                }
            }
        }
        GLUtil.checkGlError("mid 2 Texture constructor " + name);
        if (abitmap == null) {
            //Utils.alert("can't find bitmap resource " + aname);
            Utils.pushandsetdir("common");
            abitmap = Utils.getBitmapFromAsset("maptestnck.png"); // default texture if not found
            Utils.popdir();
            Log.e(TAG,"Texture '" + rname + "' not found!!!");
        }
        width = abitmap.getWidth();
        height = abitmap.getHeight();
        gltexture = bitmap2gltexture(abitmap,-1);
        hasalpha = abitmap.hasAlpha();
        GLUtil.checkGlError("end Texture constructor " + name);
    }

    // refcount name and a resource
    private static Texture createTexture(String rname,String aname) {
        Texture atex = refcounttexturelist.get(rname);
        if (atex != null) {
            ++atex.refcount;
        } else {
            atex = new Texture(rname,aname);
        }
        return atex;
    }

    // preferred way of creating a texture
    public static Texture createTexture(String aname) {
        return createTexture(aname,aname);
    }

    public static Texture createTexture(String rname, int width, int height, byte[] data) {
        Texture atex = refcounttexturelist.get(rname);
        if (atex != null) {
            ++atex.refcount;
        } else {
            atex = new Texture(rname,width,height,data);
        }
        return atex;
    }

    public static void texturerc() {
        Log.i(TAG,"Texturelist =====");
        int totalpixels = 0;
        int totaltextures = 0;
        int largest = 0;
        String largestname = "---";
        for (Texture texref : refcounttexturelist.values()) {
            String texlog;
            if (texref.hasalpha)
                texlog = "  Atex '" + texref.name + "'";
            else
                texlog = "   tex '" + texref.name + "'";
            texlog += " refcount " + texref.refcount;
            int cubemult = 1;
            if (texref instanceof FrameBufferTexture)
                texlog += " FB ";
            if (texref.iscubemap) {
                texlog += " CM 6";
                cubemult = 6;
            }
            texlog += " w " + texref.width;
            texlog += " h " + texref.height;
            int prod = texref.width*texref.height*cubemult;
            texlog += " p " + prod;
            totalpixels += prod;
            texlog += " glname " + texref.gltexture;
            //logger("   tex = '" + texref.name + "' refcount = " + texref.refcount + "\n");
            Log.i(TAG,texlog);
            ++totaltextures;
            if (prod > largest) {
                largest = prod;
                largestname = texref.name;
            }
        }
        Log.i(TAG,"totaltextures " + totaltextures + " totalpixels " + totalpixels + " largest '" + largestname + "'");
    }

    static void decngltextures() {
            --ngltextures;
        if (ngltextures == 0)
            Log.w(TAG,"ngltextures now = 0");
        if (ngltextures < 0)
        Utils.alert("ngltextures < 0\n");
    }

    // remove opengl resource from this texture
    public void glFree() {
        --refcount;
        if (refcount > 0) {
            return;
        }
        if (refcount < 0) {
        Utils.alert("Texture refcount < 0 in '" + name + "'");
        }
        refcounttexturelist.remove(name);
        deleteATexture(gltexture);
        gltexture = -1;
    }

    public static void setWrapMode() {
        globaltexflags = 0;
    }

    public static void setClampMode() {
        globaltexflags = FLAG_CLAMPUV;
    }

}

