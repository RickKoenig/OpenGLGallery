package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import static android.opengl.GLES20.*;
import static com.mperpetuo.openglgallery.enga.Main3D.maxTextures;

/**
 * Created by cyberrickers on 6/5/2016.
 */
public class Model2 extends ModelBase {

    class Group {
        int nvert;
        int vertidx;
        int nface;
        int faceidx;

        Shader shader;

        boolean hasalpha;
        String[] texturenames = new String[maxTextures];
        // gl
        Texture[] reftextures = new Texture[maxTextures];
        int glverts;
        int gluvs;
        int gluvs2;
        int glnorms;
        int glcverts;
        int glfaces;

        // free all gl resources for this group
        void glFree() {

            if (glverts != 0) {
                deleteBuffer(glverts);
                glverts = 0;
            } else {
                Log.e(TAG,"model2 has no glverts to free in group " + shader.name + "!!");
            }
            if (glnorms != 0) {
                deleteBuffer(glnorms);
                glnorms = 0;
            }

            if (glcverts != 0) {
                deleteBuffer(glcverts);
                glcverts = 0;
            }

            if (gluvs != 0) {
                deleteBuffer(gluvs);
                gluvs = 0;
            }

            if (gluvs2 != 0) {
                deleteBuffer(gluvs2);
                gluvs2 = 0;
            }

            if (glfaces != 0) {
                deleteBuffer(glfaces);
                glfaces = 0;
            }

            for (int i = 0; i< maxTextures; ++i) {
                Texture t = reftextures[i];
                if (t != null) {
                    t.glFree();
                    t = null;
                }
            }
        }
    }

    // maybe combine these two
    public ArrayList<HashMap<String, float[]>> mats = new ArrayList<>();
    public ArrayList<Group> grps = new ArrayList<>();
    private static short[] faceShift = {};

    protected Model2(String aname) {
        super(aname);
    }

    public static Model2 createmodel(String aname) {
        Model2 amod = (Model2) refcountmodellist.get(aname);
        if (amod != null) {
            ++amod.refcount;
        } else {
            amod = new Model2(aname);
        }
        return amod;
    }

    public Group addmat(String matname, String texname, int nface, int nvert) {
        HashMap<String, float[]> mat = new HashMap<>();
        Group grp = new Group();
        //grp.name = matname;
        grp.shader = GLUtil.shaderList.get(matname);
        grp.texturenames[0] = texname;
        grp.nvert = nvert;
        grp.nface = nface;
        int len = grps.size();
        if (len == 0) {
            grp.vertidx = 0;
            grp.faceidx = 0;
        } else {
            --len;
            Group prev = grps.get(len);
            grp.vertidx = prev.vertidx + prev.nvert;
            grp.faceidx = prev.faceidx + prev.nface;
        }
        grps.add(grp);
        mats.add(mat); // empty user defined uniforms
        return grp;

    }

    public void addmat2t(String matname,String texname,String texname2,int nface,int nvert) {
        Group grp = addmat(matname,texname,nface,nvert);
        grp.texturenames[1] = texname2;
    };

    public void addmatNtArray(String matname,String[] texNameArray,int nface,int nvert) {
        Group grp = addmat(matname,texNameArray[0],nface,nvert);
        for (int i = 1;i < maxTextures; ++i)
            grp.texturenames[1] = texNameArray[i];
    };

    public void commit() {
        super.commit();
        for (Group grp : grps) {
            GLUtil.checkGlError("C0 " + name);
            if (grp.shader == null) {
                Utils.alert("missing shader on model2 '" + name + "' grp ");
            }
            GLUtil.checkGlError("C1 " + name);

            // build tri vertex buffer
            grp.glverts = makeAndWriteToFloatBuffer(verts,3*grp.vertidx,3*grp.nvert);

            // build tri norms
            if (grp.shader.actattribs.containsKey("normalAttribute")) {
                if (norms != null) {
                    grp.glnorms = makeAndWriteToFloatBuffer(norms,3*grp.vertidx,3*grp.nvert);
                } else {
                    Utils.alert("missing norms on model2 '" + name + "'  shader '" + grp.shader.name + "'");
                }
            }
            GLUtil.checkGlError("C2 " + name);

            // build tri cverts
            if (grp.shader.actattribs.containsKey("colorAttribute")) {
                if (cverts != null) {
                    grp.glcverts = makeAndWriteToFloatBuffer(cverts,4*grp.vertidx,4*grp.nvert);
                } else {
                    Utils.alert("missing cverts on model2 '" + name + "'  shader '" + grp.shader.name + "'");
                }
            }
            GLUtil.checkGlError("C3 " + name);

            // build tri uvs
            if (grp.shader.actattribs.containsKey("textureCoordAttribute")) {
                if (uvs != null) {
                    grp.gluvs = makeAndWriteToFloatBuffer(uvs,2*grp.vertidx,2*grp.nvert);
                } else {
                    Utils.alert("missing uvs on model2 '" + name + "'  shader '" + grp.shader.name + "'");
                }
            }
            GLUtil.checkGlError("C4 " + name);

            // build tri uvs2
            if (grp.shader.actattribs.containsKey(("textureCoordAttribute2"))) {
                if (uvs2 != null) {
                    grp.gluvs2 = makeAndWriteToFloatBuffer(uvs2,2*grp.vertidx,2*grp.nvert);
                } else {
                    Utils.alert("missing uvs2 on model2 '" + name + "'  shader '" + grp.shader.name + "'");
                }
            }
            GLUtil.checkGlError("C5 " + name);

            // build tri faces
            if (faces != null) {
                // (glfaces != 0) {
                if (grp.nface*3 > faceShift.length)
                    faceShift = new short[grp.nface*3];


                // build tri faces
                /*
                if (this.faces) {
                    grp.glfaces = gl.createBuffer();
                    ++nglbuffers;
                    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER,grp.glfaces);
                    var subarr = arrfaces.subarray(3*grp.faceidx,3*(grp.faceidx+grp.nface));
                    int j;
                    for (j=0;j<3*grp.nface;++j)
                        subarr[j] = subarr[j] - grp.vertidx;

                    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,subarr,gl.STATIC_DRAW);


                */
                int j;
                for (j=0;j<3*grp.nface;++j) {
                    faceShift[j] = (short) (faces[j + grp.faceidx*3] - grp.vertidx);
                }
                grp.glfaces = makeAndWriteToShortBuffer(faceShift,0,3*grp.nface);
                //grp.glfaces = makeAndWriteToShortBuffer(faceShift,3*grp.faceidx,3*grp.nface);
                //
            } else if (nverts%3 != 0) {
                Utils.alert("no faces and verts not a multiple of 3 on model2 '" + name + "'");
            }
            GLUtil.checkGlError("C6 " + name);

            // build textureN
            grp.hasalpha = false;
            for(int j=0;j<maxTextures;++j) {
                if (grp.shader.actsamplers.containsKey("uSampler" + j)) {
                    if (grp.texturenames[j] != null) {
                        grp.reftextures[j] = Texture.createTexture(grp.texturenames[j]);
                        if (grp.reftextures[j] != null && grp.reftextures[j].hasalpha) {
                            //flags |= FLAG_HASALPHA;
                            grp.hasalpha = true;
                            if (grp == grps.get(j))
                                this.flags |= FLAG_HASALPHA;
                        }
                    } else {
                        Utils.alert("missing texture[" + j + "]+  on model '" + name + "'  shader '" + grp.shader.name + "'");
                    }
                }
                GLUtil.checkGlError("C7 " + name);
                /*
                // build texture2
                if (grp.shader.actsamplers.containsKey("uSampler1")) {
                    if (grp.texturename2 != null) {
                        grp.reftexture2 = Texture.createTexture(grp.texturename2);
                        if (grp.reftexture2 != null && grp.reftexture2.hasalpha)
                            grp.hasalpha = true;// |= FLAG_HASALPHA;
                    } else {
                        Utils.alert("missing texture1 on model '" + name + "'  shader '" + grp.shader.name + "'");
                    }
                }
*/
                GLUtil.checkGlError("C8 " + name);
                // do attributes
            }
        }
    }

    @Override
    public void draw() {
        Tree curtree = GLUtil.curtree;
        int i,n = mats.size();
        if ((flags & FLAG_NOZBUFFER) != 0)
            glDisable(GL_DEPTH_TEST);                               // turn off zbuffer
        if ((flags & FLAG_DOUBLESIDED) != 0)
            glDisable(GL_CULL_FACE);
        GLUtil.checkGlError("D1 " + name);
        //if (n > 1) // draw only the first group
        //    n = 1;
        for (i=0;i<n;++i) {
            Group grp = grps.get(i);
            HashMap<String,float[]> matg = mats.get(i);
            Shader shaderProgram;
            if (ShadowMap.inshadowmapbuild)
                if (grp.reftextures[0] != null)
                    shaderProgram = GLUtil.shaderList.get("shadowmapbuild");
                else
                    shaderProgram = GLUtil.shaderList.get("shadowmapbuildnotex");
            else
                shaderProgram = grp.shader;
            glUseProgram(shaderProgram.glshader);
            GLUtil.checkGlError("D2 " + name);
            GLUtil.setMatrixModelViewUniforms(shaderProgram);
            GLUtil.setAttributes(shaderProgram);

            setUserModelUniforms(shaderProgram,matg); // group material, very local
            setUserModelUniforms(shaderProgram,mat); // model material
            if (curtree != null)
                setUserModelUniforms(shaderProgram,curtree.mat); // tree material
            setUserModelUniforms(shaderProgram,GLUtil.globalmat); // global material

            GLUtil.checkGlError("D3 " + name);
            if (grp.glverts != 0 && shaderProgram.actattribs.containsKey("vertexPositionAttribute")) {
                glBindBuffer(GL_ARRAY_BUFFER, grp.glverts);
                glVertexAttribPointer(shaderProgram.actattribs.get("vertexPositionAttribute"), 3, GL_FLOAT, false, 0, 0);
            }

            //if (grp.glnorms) {
            if (grp.glnorms != 0&& shaderProgram.actattribs.containsKey("normalAttribute")) {
                glBindBuffer(GL_ARRAY_BUFFER, grp.glnorms);
                glVertexAttribPointer(shaderProgram.actattribs.get("normalAttribute"),3,GL_FLOAT, false, 0, 0);
            }

            //if (grp.gluvs) {
            if (grp.gluvs != 0 && shaderProgram.actattribs.containsKey("textureCoordAttribute")) {
                glBindBuffer(GL_ARRAY_BUFFER, grp.gluvs);
                glVertexAttribPointer(shaderProgram.actattribs.get("textureCoordAttribute"),2,GL_FLOAT, false, 0, 0);
            }

            //if (grp.gluvs2) {
            if (grp.gluvs2 != 0 && shaderProgram.actattribs.containsKey("textureCoordAttribute2")) {
                glBindBuffer(GL_ARRAY_BUFFER, grp.gluvs2);
                glVertexAttribPointer(shaderProgram.actattribs.get("textureCoordAttribute2"),2,GL_FLOAT, false, 0, 0);
            }

            //if (grp.glcverts) {
            if (grp.glcverts != 0 && shaderProgram.actattribs.containsKey("colorAttribute")) {
                glBindBuffer(GL_ARRAY_BUFFER, grp.glcverts);
                glVertexAttribPointer(shaderProgram.actattribs.get("colorAttribute"),4,GL_FLOAT, false, 0, 0);
            }

            for (int j =0;j< maxTextures;++j) {
                if (grp.reftextures[j] != null) {
                    glActiveTexture(GL_TEXTURE0 + j);
                    if (grp.reftextures[j].iscubemap)
                        glBindTexture(GL_TEXTURE_CUBE_MAP, grp.reftextures[j].gltexture);
                    else
                        glBindTexture(GL_TEXTURE_2D, grp.reftextures[j].gltexture);
                }
            }

            GLUtil.checkGlError("D4 " + name);
            if (!grp.hasalpha) { // turn it off
                glDisable(GL_BLEND);
                if (ShadowMap.inshadowmapbuild)
                    glCullFace(GL_FRONT); // back face shadow generate non alpha models
            }
            GLUtil.checkGlError("before check model2 draw, name = " + name);
            if (grp.glfaces != 0) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,grp.glfaces);
                glDrawElements(GL_TRIANGLES,grp.nface*3,GL_UNSIGNED_SHORT,0);
            } else {
                glDrawArrays(GL_TRIANGLES,0,grp.nvert); // *3 ?
            }
            GLUtil.checkGlError("after check model2 draw, name = " + name);
            
		
            if (!grp.hasalpha) { // turn it back on
                if (ShadowMap.inshadowmapbuild)
                    glCullFace(GL_BACK);
                //gl.frontFace(gl.CW);
                glEnable(GL_BLEND);
            }
        }
        if ((flags & FLAG_NOZBUFFER) != 0)
            glEnable(GL_DEPTH_TEST);                               // turn zbuffer back on
        if ((flags & FLAG_DOUBLESIDED) != 0)
            glEnable(GL_CULL_FACE);

    }

    @Override
    public void changemesh(Mesh newmesh) {
        glFreeNoRef();
        setmesh(newmesh);
        commit();
    }

    private void glFreeNoRef() {
        for (Group grp : grps) {
            grp.glFree();
        }
    }

    @Override
    public void glFree() {
        if (!releaseModel()) // dec ref counter
            return;
        glFreeNoRef();
    }

    @Override
    public void buildLog() {
        super.buildLog();
        modellog += " num groups " + grps.size();
        for (Group grp : grps) {
            int vo = grp.vertidx;
            int vs = grp.nvert;
            int fo = grp.faceidx;
            int fs = grp.nface;
            modellog += "\n      grp vo " + vo + " vs " + vs + " fo " + fo + " fs " + fs;
            modellog += " shader '" + grp.shader.name + "'";
            for (int j =0;j< maxTextures; ++j) {
                if (grp.texturenames[j] != null)
                    modellog += " texname[" + j + "]'" + grp.texturenames[j] + "'";
            }
        }
        //Log.i(TAG,modellog);
    }

}