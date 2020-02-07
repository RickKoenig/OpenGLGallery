package com.mperpetuo.openglgallery.enga;

import android.util.Log;

public class ModelFont extends Model {
    String text;
    int cw;
    int ch;
    int maxcols;
    int maxrows;
    boolean wwrap;
    int nc;
    int ng;
    int ngcap;
    private float fudgex;
    private float fudgey;

    protected ModelFont(String aname, String fontname, String shadername, int csizex, int csizey, int wrapx, int wrapy, boolean wordwrap) {
        super(aname);
        text = "";
        setshader(shadername);
        texturenames[0] = fontname;
        settexture(texturenames[0]);
        cw = csizex;
        ch = csizey;
        maxcols = wrapx;
        maxrows = wrapy;
        wwrap = wordwrap;
        nc = 0; // number of chars printing including \n and space
        ng = 0; // number of glyphs printing, just printable chars
        ngcap = 0; // number of glyphs alloced, same as max of glyphs printed
        reftextures[0] = Texture.createTexture(texturenames[0]);
        fudgex = 0;//.025; // fudge in for fonts, helps prevent wrap around effects (stray pixels from next glyph), but loses precise pixel mapping
        fudgey = 0;//.025;
    }

    public static ModelFont createmodel(String aname,String texname,String shadername,int csizex,int csizey,int wrapx,int wrapy,boolean wordwrap) {
        ModelFont amod = (ModelFont) refcountmodellist.get(aname);
        if (amod != null) {
            ++amod.refcount;
        } else {
            amod = new ModelFont(aname,texname,shadername,csizex,csizey,wrapx,wrapy,wordwrap);
        }
        return amod;
    }

    // let print commit to opengl
    @Override
    public void commit() {
        Log.e(TAG,"commit called on ModelFont " + name);
    }

    public void setfudge(boolean dofudge) {
        if (dofudge) {
            fudgex = .025f;
            fudgey = .025f;
        } else {
            fudgex = 0;
            fudgey = 0;
        }
    }

    public void print(String atext) {
        if (text.equals(atext))
            return; // nothing to change
        text = atext;
        int i;
        if (shader == null)
            Utils.alert("missing shader on model '" + name + "'");
        //if (!averts)
        //	alert("missing verts on model '" + name + "'");
        nc = text.length();
        int j = 0;
        int x = 0;
        int y = 0;

        // build tri verts and uvs
        float[] averts = new float[nc*12]; //new Float32Array(nc*12); // 4 vec3's
        float[] auvs = new float[nc*8];	// 4 vec2's
        for (i=0;i<nc;++i) {
            int cc = text.charAt(i);
            if (cc >= 128)
                continue;
//		    if (cc == 32) { // space, invisible
//			    ++x;
//			    continue;
//	        }
            if (cc == 10) { // \n
                x = 0;
                ++y;
                continue;
            }
            if (x >= maxcols) {
                if (wwrap) {
                    x = 0;
                    ++y;
                } else {
                    continue;
                }
            }
            if (y >= maxrows)
                break;
            int r = cc>>3;
            int c = cc&7;

            float u0 = c/8.0f + fudgex/8.0f;
            float v0 = r/16.0f + fudgey/16.0f;
            float u1 = (c+1)/8.0f-fudgex/8.0f;
            float v1 = (r+1)/16.0f-fudgey/16.0f;
            averts[12*j   ] = cw*x;
            averts[12*j+ 1] = -ch*y;
            averts[12*j+ 2] = 0;
            averts[12*j+ 3] = cw*(x + 1);
            averts[12*j+ 4] = -ch*y;
            averts[12*j+ 5] = 0;
            averts[12*j+ 6] = cw*x;
            averts[12*j+ 7] = -ch*(y + 1);
            averts[12*j+ 8] = 0;
            averts[12*j+ 9] = cw*(x + 1);
            averts[12*j+10] = -ch*(y + 1);
            averts[12*j+11] = 0;
            auvs[8*j  ] = u0;
            auvs[8*j+1] = v0;
            auvs[8*j+2] = u1;
            auvs[8*j+3] = v0;
            auvs[8*j+4] = u0;
            auvs[8*j+5] = v1;
            auvs[8*j+6] = u1;
            auvs[8*j+7] = v1;
            ++x;
            ++j;
        }
        ng = j;
        if (glverts != 0)
            deleteBuffer(glverts);
        setverts(averts);
        glverts = makeAndWriteToFloatBuffer(averts);

        if (gluvs != 0)
            deleteBuffer(gluvs);
        setuvs(auvs);
        gluvs = makeAndWriteToFloatBuffer(auvs);

        // grow tri faces if necessary
        if (ngcap < ng) {
            //afaces = new Uint16Array(ng*6); // 2 face3's
            faces = new short[ng*6];
            for (i=0;i<ng;++i) { // expand
                faces[6*i  ] = (short)(i*4);
                faces[6*i+1] = (short)(i*4 + 1);
                faces[6*i+2] = (short)(i*4 + 2);
                faces[6*i+3] = (short)(i*4 + 3);
                faces[6*i+4] = (short)(i*4 + 2);
                faces[6*i+5] = (short)(i*4 + 1);
            }
            deleteBuffer(glfaces);
            glfaces = makeAndWriteToShortBuffer(faces);
            ngcap = ng;
        }
        nface = ng*2; // does this work?
        // build sampler and texture
        if (shader.actsamplers.containsKey("uSampler0") && reftextures[0] == null) {
            Utils.alert("missing texture0 on font '" + name + "'  shader '" + shader.name + "'");
        }
    
    }

    // let Model do the draw
/*
    @Override
    public void draw() {

    }
*/
    // let Model free resources
    /*
    @Override
    public void glFree() {
        if (!releaseModel()) // dec ref counter
            return; // don't free gl resources yet
    }
    */
    @Override
    public void buildLog() {
        super.buildLog();
    /*        if (shader != null)
            modellog += " shadername '" + shader.name + "'";
        if (texturename != null)
            modellog += " texname '" + texturename + "'";
        if (texturename2 != null)
            modellog += " texname2 '" + texturename2 + "'"; */
        modellog += " MF ";
        //Log.i(TAG,modellog);
    }

}
