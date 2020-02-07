package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import static android.opengl.GLES20.*;

/**
 * Created by rickkoenig on 2/23/16.
 */

// 1 material for whole model
public class Model extends ModelBase {

    Shader shader;

    String[] texturenames = new String[Main3D.maxTextures];
    // gl
    Texture[] reftextures = new Texture[Main3D.maxTextures];
    int glverts;
    int gluvs;
    int gluvs2;
    int glnorms;
    int glcverts;
    int glfaces;

////// Model 3d class, one material per model
// reference counted

    protected Model(String aname) {
        super(aname);
    }

    public static Model createmodel(String aname) {
        Model amod = (Model)refcountmodellist.get(aname);
        if (amod != null) {
            ++amod.refcount;
        } else {
            amod = new Model(aname);
        }
        return amod;
    }

    // set model shader
    public void setshader(String shadername) {
        shader = GLUtil.shaderList.get(shadername);
    };

    // set model texture 0
    public void settexture(String texturenamea) {
        texturenames[0] = texturenamea;
    };

    // set model texture 1
    void settexture2(String texturename2a) {
        texturenames[1] = texturename2a;
    };

    // set model texture N
    void settextureN(String texturename, int n) {
        texturenames[n] = texturename;
    };

// set model texture Array 0 - N-1
    void settextureNArray(String[] textureNameArray) {
        for (int i=0;i<textureNameArray.length;++i)
            texturenames[i] = textureNameArray[i];
    };

    @Override
    public void commit() {
        super.commit();
        if (shader == null)
            Utils.alert("missing shader on model '" + name + "'");
        if (verts == null)
            Utils.alert("missing verts on model '" + name + "'");
        if (glverts != 0)
            Utils.alert("can only commit once on model '" + name + "'");

        // build tri vertex buffer
        glverts = makeAndWriteToFloatBuffer(verts);

        // build tri norms
        if (shader.actattribs.containsKey("normalAttribute")) {
            if (norms != null) {
                glnorms = makeAndWriteToFloatBuffer(norms);
            } else {
                Utils.alert("missing norms on model '" + name + "'  shader '" + shader.name + "'");
            }
        }

        // build tri cverts
        if (shader.actattribs.containsKey("colorAttribute")) {
            if (cverts != null) {
                glcverts = makeAndWriteToFloatBuffer(cverts);
            } else {
                Utils.alert("missing cverts on model '" + name + "'  shader '" + shader.name + "'");
            }
        }

        // build tri uvs
        if (shader.actattribs.containsKey("textureCoordAttribute")) {
            if (uvs != null) {
                gluvs = makeAndWriteToFloatBuffer(uvs);
            } else {
                Utils.alert("missing uvs on model '" + name + "'  shader '" + shader.name + "'");
            }
        }

        // build tri uvs2
        if (shader.actattribs.containsKey(("textureCoordAttribute2"))) {
            if (uvs2 != null) {
                gluvs2 = makeAndWriteToFloatBuffer(uvs2);
            } else {
                Utils.alert("missing uvs2 on model '" + name + "'  shader '" + shader.name + "'");
            }
        }

        // build tri faces
        if (faces != null) {
            // (glfaces != 0) {
                glfaces = makeAndWriteToShortBuffer(faces);
            //
        } else if (nverts%3 != 0) {
            Utils.alert("no faces and verts not a multiple of 3 on model '" + name + "'");
        }

        // build textureN
        //this.hasalpha = false;
        for(int i = 0;i < Main3D.maxTextures;++i) {
            if (shader.actsamplers.containsKey("uSampler" + i)) {
                if (texturenames[i] != null) {
                    reftextures[i] = Texture.createTexture(texturenames[i]);
                    if (i == 0) {
                        if (reftextures[i] != null && reftextures[i].hasalpha)
                            flags |= FLAG_HASALPHA;
                    }
                } else {
                    Utils.alert("missing texture" + i + " '" + texturenames[i] + "' on model '" + name + "'  shader '" + shader.name + "'");
                }
            }
        }
    }
/*
        // build texture
        //hasalpha = false;
        if (shader.actsamplers.containsKey("uSampler0")) {
            if (texturename != null) {
                reftexture = Texture.createTexture(texturename);
                if (reftexture != null && reftexture.hasalpha)
                    flags |= FLAG_HASALPHA;
            } else {
                Utils.alert("missing texture0 on model '" + name + "'  shader '" + shader.name + "'");
            }
        }

        // build texture2
        if (shader.actsamplers.containsKey("uSampler1")) {
            if (texturename2 != null) {
                reftexture2 = Texture.createTexture(texturename2);
                if (reftexture2 != null && reftexture2.hasalpha)
                    flags |= FLAG_HASALPHA;
            } else {
                Utils.alert("missing texture2 on model '" + name + "'  shader '" + shader.name + "'");
            }
        }
        // maybe null out cpu side vertices and faces

    }*/
/*
    public void changeverts(float[] newverts) {
        if (newverts != null) {
            setverts(newverts);
            deleteBuffer(glverts);
            glverts = makeAndWriteToFloatBuffer(newverts);
        }
    }

    protected void changenorms(float[] newnorms) {
        if (newnorms != null) {
            setnorms(newnorms);
            if (shader.actattribs.containsKey("normalAttribute")) {
                deleteBuffer(glnorms);
                glnorms = makeAndWriteToFloatBuffer(newnorms);
            }
        }
    }
    
    protected void changeuvs(float[] newuvs) {
        if (newuvs != null) {
            setuvs(newuvs);
            if (shader.actattribs.containsKey("textureCoordAttribute")) {
                deleteBuffer(gluvs);
                gluvs = makeAndWriteToFloatBuffer(newuvs);
            }
        }
    }

    protected void changecverts(float[] newcverts) {
        if (newcverts != null) {
            setcverts(newcverts);
            if (shader.actattribs.containsKey("colorAttribute")) {
                deleteBuffer(glcverts);
                glcverts = makeAndWriteToFloatBuffer(newcverts);
            }
        }
    } */
/*
    // change and commit the faces of this model
    protected void changefaces(short[] newfaces) {
        // build tri faces
        if (newfaces != null) {
            setfaces(newfaces);
            // (glfaces != 0) {
            glfaces = makeAndWriteToShortBuffer(faces);
            //
        } else if (nverts%3 != 0) {
            Utils.alert("no faces and verts not a multiple of 3 on model '" + name + "'");
        }
    }
*/
    // keep old faces
    /*
    public void changemesh(Mesh newmesh) {
        changeverts(newmesh.verts);
        changenorms(newmesh.norms);
        changeuvs(newmesh.uvs);
        changecverts(newmesh.cverts);
    } */

    public void changemesh(Mesh newmesh) {
        glFreeNoRef();
        setmesh(newmesh);
        commit();
    }

    // change model texture
    public void changetextureN(String newname, int i) {
        texturenames[i] = newname;
        flags &= ~FLAG_HASALPHA;
        //if (shader.uSampler != 0 && reftexture != null) {
        if (shader.actsamplers.containsKey("uSampler" + i) && reftextures[i] != null) {
            reftextures[i].glFree();
            reftextures[i] = Texture.createTexture(texturenames[i]);
            if (reftextures[i] != null && reftextures[i].hasalpha) {
                flags |= FLAG_HASALPHA;
            }
        }
    }
    /*
    // change model texture
    public void changetexture(String newname) {
        texturename = newname;
        flags &= ~FLAG_HASALPHA;
        //if (shader.uSampler != 0 && reftexture != null) {
        if (shader.actsamplers.containsKey("uSampler0") && reftexture != null) {
            reftexture.glFree();
            reftexture = Texture.createTexture(texturename);
            if (reftexture != null && reftexture.hasalpha) {
                flags |= FLAG_HASALPHA;
            }
        }
    }
*/

    // change model texture
    public void changetexture(String newname) {
        changetextureN(newname,0);
    }

    @Override
    public void draw() {
        GLUtil.checkGlError("draw0");
        if (verts == null)
            return;
        Tree curtree = GLUtil.curtree;
        if ((flags & FLAG_NOZBUFFER) != 0)
            glDisable(GL_DEPTH_TEST);                               // turn off zbuffer
        if ((flags & FLAG_DOUBLESIDED) != 0)
            glDisable(GL_CULL_FACE);

        int i = 0;
        if (curtree != null && curtree.treereftexture != null) {
            if (curtree.treereftexture != null) {
                glActiveTexture(GL_TEXTURE0);
                if (curtree.treereftexture.iscubemap)
                    glBindTexture(GL_TEXTURE_CUBE_MAP, curtree.treereftexture.gltexture);
                else
                    glBindTexture(GL_TEXTURE_2D, curtree.treereftexture.gltexture);
                i = 1;
            } else {
                i = 0;
            }
        }
        boolean hasTexture0 = reftextures[0] != null;
        for (;i<Main3D.maxTextures;++i) {
            if (reftextures[i] != null) {
                glActiveTexture(GL_TEXTURE0 + i);
                if (reftextures[i].iscubemap)
                    glBindTexture(GL_TEXTURE_CUBE_MAP, reftextures[i].gltexture);
                else
                    glBindTexture(GL_TEXTURE_2D, reftextures[i].gltexture);
            //} else {
            //    glBindTexture(GL_TEXTURE_2D, 0);
            //    //hasTexture = false;
            }
        }

        Shader shaderProgram;
        boolean doShadowmapBuild = ShadowMap.inshadowmapbuild;
        //doShadowmapBuild = false;
        if (doShadowmapBuild) {
            if (hasTexture0)
                shaderProgram = GLUtil.shaderList.get("shadowmapbuild");
            else
                shaderProgram = GLUtil.shaderList.get("shadowmapbuildnotex");
        } else {
            shaderProgram = shader;
        }
        GLUtil.checkGlError("set shader before");

        glUseProgram(shaderProgram.glshader);
        GLUtil.checkGlError("set shader after");
        GLUtil.setMatrixModelViewUniforms(shaderProgram);
        GLUtil.setAttributes(shaderProgram);

        setUserModelUniforms(shaderProgram,mat); // model
        if (curtree != null)
            setUserModelUniforms(shaderProgram,curtree.mat); // tree
        setUserModelUniforms(shaderProgram,GLUtil.globalmat); // global


        if (glverts != 0 && shaderProgram.actattribs.containsKey("vertexPositionAttribute")) {
            glBindBuffer(GL_ARRAY_BUFFER, glverts);
            glVertexAttribPointer(shaderProgram.actattribs.get("vertexPositionAttribute"), 3, GL_FLOAT, false, 0, 0);
        }
        if (glnorms != 0 && shaderProgram.actattribs.containsKey("normalAttribute")) {
            glBindBuffer(GL_ARRAY_BUFFER, glnorms);
            glVertexAttribPointer(shaderProgram.actattribs.get("normalAttribute"),3,GL_FLOAT, false, 0, 0);
        }

        if (gluvs != 0 && shaderProgram.actattribs.containsKey("textureCoordAttribute")) {
            glBindBuffer(GL_ARRAY_BUFFER, gluvs);
            glVertexAttribPointer(shaderProgram.actattribs.get("textureCoordAttribute"),2,GL_FLOAT, false, 0, 0);
        }

        if (gluvs2 !=0 && shaderProgram.actattribs.containsKey("textureCoordAttribute2")) {
            glBindBuffer(GL_ARRAY_BUFFER, gluvs2);
            glVertexAttribPointer(shaderProgram.actattribs.get("textureCoordAttribute2"),2,GL_FLOAT, false, 0, 0);
        }

        if (glcverts != 0 && shaderProgram.actattribs.containsKey("colorAttribute")) {
            glBindBuffer(GL_ARRAY_BUFFER, glcverts);
            glVertexAttribPointer(shaderProgram.actattribs.get("colorAttribute"),4,GL_FLOAT, false, 0, 0);
        }

        GLUtil.checkGlError("draw1");
/*
        if (reftexture2 != null) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, reftexture2.gltexture);
        }
*/
        if ((flags & FLAG_HASALPHA) == 0) { // turn it off
            if (ShadowMap.inshadowmapbuild)
                glCullFace(GL_FRONT); // back face shadow generate non alpha models
            glDisable(GL_BLEND);
        }

        //GLUtil.setSamplerUniforms(shaderProgram);
        GLUtil.checkGlError("drawb");

        //if (reftexture == null)
        //    Log.e(TAG,"found a null pointer exception here");
//        if (reftexture != null && !reftexture.iscubemap) {
            if (glfaces != 0) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, glfaces); // number of faces*3
                //glDrawElements(GL_TRIANGLES, faces.length, GL_UNSIGNED_SHORT, 0);
                glDrawElements(GL_TRIANGLES, nface*3, GL_UNSIGNED_SHORT, 0);
            } else {
                //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0); // number of faces*3
                glDrawArrays(GL_TRIANGLES, 0, verts.length / 3); // *3 ? // number of 3D verts
            }
        GLUtil.checkGlError("check model draw, name = " + name);
//        }

        if ((flags & FLAG_HASALPHA) == 0) { // turn it back on
            if (ShadowMap.inshadowmapbuild)
                glCullFace(GL_BACK); // back face shadow generate non alpha models
            glEnable(GL_BLEND);
        }
        if ((flags & FLAG_NOZBUFFER) != 0)
            glEnable(GL_DEPTH_TEST);                               // turn zbuffer back on
        if ((flags & FLAG_DOUBLESIDED) != 0)
            glEnable(GL_CULL_FACE);
        GLUtil.checkGlError("drawe");
    }

    private void glFreeNoRef() {
        if (glverts != 0) {
            deleteBuffer(glverts);
            glverts = 0;
        } else {
            Log.e(TAG,"model has no glverts to free!!");
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
        /*
        if (reftexture != null) {
            reftexture.glFree();
            reftexture = null;
        }
        if (reftexture2 != null) {
            reftexture2.glFree();
            reftexture2 = null;
        }*/
        for (int i=0;i<Main3D.maxTextures;++i) {
            if (reftextures[i] != null) {
                reftextures[i].glFree();
                reftextures[i] = null;
            }
        }
    }
    // free all opengl resources from this model
    @Override
    public void glFree() {
        if (!releaseModel()) // dec ref counter
            return; // don't free gl resources yet
        glFreeNoRef();
    }

    @Override
    public void buildLog() {
        super.buildLog();
        if (shader != null)
            modellog += " shadername '" + shader.name + "'";
        /*
        if (texturename != null)
            modellog += " texname '" + texturename + "'";
        if (texturename2 != null)
            modellog += " texname2 '" + texturename2 + "'"; */
        for (int i=0;i<Main3D.maxTextures; ++i) {
            if (texturenames[i] != null)
                modellog += " texname[" + i + "] '" + this.texturenames[i] + "'";
        }
    }

}
