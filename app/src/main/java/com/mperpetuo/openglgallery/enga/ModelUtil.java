package com.mperpetuo.openglgallery.enga;

/**
 * Created by rickkoenig on 2/23/16.
 */
public class ModelUtil {
    //private static String TAG = "ModelUtil";

    // everything else is built with surfaces
    static class SurfVertex {
        float[] v;
        float[] n;
    }

    static abstract class Surf {

        abstract SurfVertex surf(float p, float q);
        //    SurfVertex vert = new SurfVertex();
        //    return vert;
        //}
    }

    // surface patch functions

    static class Cylinderxz_mid_surf extends Surf {
        float mRad,mHit;
        Cylinderxz_mid_surf(float rad,float hit) {
            mRad = rad;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float sa,ca;
            p = NuMath.TWOPI*p;
            q = mHit*(1.0f-q);
            sa = (float)Math.sin(p);
            ca = (float)Math.cos(p);
            n[0] = ca;
            n[1] = 0;
            n[2] = sa;
            v[0] = mRad*n[0];
            v[1] = q;
            v[2] = mRad*n[2];
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    static class Cylinderxz_top_surf extends Surf {
        float mRad,mHit;
        Cylinderxz_top_surf(float rad,float hit) {
            mRad = rad;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float sa,ca;
            p = NuMath.TWOPI*p;
            q = mRad*q;
            sa = (float)Math.sin(p);
            ca = (float)Math.cos(p);
            n[0] = 0;
            n[1] = 1;
            n[2] = 0;
            v[0] = q*ca;
            v[1] = mHit;
            v[2] = q*sa;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    // cylinder bottom
    static class Cylinderxz_bot_surf extends Surf {
        float mRad,mHit;
        Cylinderxz_bot_surf(float rad,float hit) {
            mRad = rad;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float sa,ca;
            p = NuMath.TWOPI*p;
            q = mRad*(1-q);
            sa = (float)Math.sin(p);
            ca = (float)Math.cos(p);
            n[0] = 0;
            n[1] = -1;
            n[2] = 0;
            v[0] = q*ca;
            v[1] = 0;
            v[2] = q*sa;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    static class Conexz_mid_surf extends Surf {
        float mRad,mHit;
        Conexz_mid_surf(float rad,float hit) {
            mRad = rad;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float[] ns = {mHit,mRad,0.0f};
            NuMath.normalize(ns);
            float sa,ca;
            p = NuMath.TWOPI*p;
            float h = mHit*(1-q);
            sa = (float)Math.sin(p);
            ca = (float)Math.cos(p);
            float[] nc = {ca,0,sa};
            float radh = mRad*q;
            v[0] = radh*nc[0];
            v[1] = h;
            v[2] = radh*nc[2];
            n[0] = ns[0] * nc[0];
            n[1] = ns[1];
            n[2] = ns[0] * nc[2];
            //n[0] = 0;
            //n[1] = 0;
            //n[2] = -1;
            float m2 = n[0]*n[0] + n[1]*n[1] + n[2]*n[2];
            if (m2 < .95f || m2 > 1.05f)
                Utils.alert("bad m2\n");
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    // cone bottom
    static class Conexz_bot_surf extends Surf {
        float mRad,mHit;
        Conexz_bot_surf(float rad,float hit) {
            mRad = rad;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float sa,ca;
            p = NuMath.TWOPI*p;
            q = mRad*(1-q);
            sa = (float)Math.sin(p);
            ca = (float)Math.cos(p);
            n[0] = 0;
            n[1] = -1;
            n[2] = 0;
            v[0] = q*ca;
            v[1] = 0;
            v[2] = q*sa;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    static class SphereSurf extends Surf {
        float mRad;

        SphereSurf(float rad) {
            mRad = rad;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            float sp, cp, sq, cq;
            p = NuMath.TWOPI * p;
            q = NuMath.PIOVER2 - NuMath.PI * q;
            sp = (float)Math.sin(p);
            cp = (float)Math.cos(p);
            sq = (float)Math.sin(q);
            cq = (float)Math.cos(q);
            n[0] = sp * cq;
            n[1] = sq;
            n[2] = -cp * cq;
            v[0] = mRad * n[0];
            v[1] = mRad * n[1];
            v[2] = mRad * n[2];
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    static class TorusxzSurf extends Surf  {
        float mRad0,mRad1;
        TorusxzSurf(float rad0,float rad1) {
            mRad0 = rad0;
            mRad1 = rad1;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float[] v = new float[3];
            float[] n = new float[3];
            p *= 2*Math.PI;
            q *= 2*Math.PI;
            float sp = (float)Math.sin(p);
            float cp = (float)Math.cos(p);
            float sq = (float)Math.sin(q);
            float cq = (float)Math.cos(q);
            float radv = mRad0 - mRad1*cq;
            v[0] = radv*sp;
            v[1] = mRad1*sq;
            v[2] = -radv*cp;
            n[0] = -cq*sp;
            n[1] = sq;
            n[2] = cq*cp;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    public static Mesh buildpatch(int np,int nq,float tileu,float tilev,Surf funcsurf) {
        Mesh mesh = new Mesh();
/// build the verts, uvs and norms for the model
        int nv = (np+1)*(nq+1);
        int nf = np*nq*2;
        float[] verts = new float[3*nv]; //struct pointf3* verts=new pointf3[nv];
        float[] norms = new float[3*nv]; //struct pointf3* verts=new pointf3[nv];
        float[] uvs = new float[2*nv]; //struct uv* uvs=new uv[nv];
        short[] faces = new short[3*nf];
        int i,j,k=0;
        float trati = tileu/np;
        float tratj = tilev/nq;
        boolean haveNorms = false;
        for (i=0;i<=np;++i) {
            float s = (float)i/np;
            for (j=0;j<=nq;++j) {
                float t = (float)j/nq;
                SurfVertex coord = funcsurf.surf(s, t);
                int k3 = 3*k;
                verts[k3] = coord.v[0];
                verts[k3+1] = coord.v[1];
                verts[k3+2] = coord.v[2];
                if (coord.n != null) {
                    haveNorms = true;
                    norms[k3] = coord.n[0];
                    norms[k3+1] = coord.n[1];
                    norms[k3+2] = coord.n[2];
                }
                int k2 = 2*k;
                uvs[k2] = i*trati;
                uvs[k2+1] = j*tratj;
                ++k;
            }
        }
        k = 0;
        for (i=0;i<np;++i) {
            for (j=0;j<nq;++j) {
                int k6 = 6*k;
                int f0=j+(nq+1)*i;
                int f1=f0+(nq+1);
                int f2=f0+1;
                int f3=f1+1;
                faces[k6] = (short)f0;
                faces[k6+1] = (short)f1;
                faces[k6+2] = (short)f2;
                faces[k6+3] = (short)f3;
                faces[k6+4] = (short)f2;
                faces[k6+5] = (short)f1;
                ++k;
            }
        }
        mesh.verts = verts;
        if (haveNorms)
            mesh.norms = norms;
        mesh.uvs = uvs;
        mesh.faces = faces;
        return mesh;
    }

    // paper airplane mesh
    // pointing straight up
    private static float[] paperairplanemeshVerts = {
            0, .5f, 0,
            0, -.5f, 0,
            -1.0f / 3.0f, -.5f, 0,

            0, .5f, 0,
            -1.0f / 3.0f, -.5f, 0,
            0, -.5f, 0,

            0, .5f, 0,
            1.0f / 3.0f, -.5f, 0,
            0, -.5f, 0,

            0, .5f, 0,
            0, -.5f, 0,
            1.0f / 3.0f, -.5f, 0,

            0, .5f, 0,
            0, -.5f, 0,
            0, -.5f, 1.0f / 6.0f,

            0, .5f, 0,
            0, -.5f, 1.0f / 6.0f,
            0, -.5f, 0
    };
    private static float[] paperairplanemeshCVerts = {
            1, 0, 0, 1,
            1, 0, 0, 1,
            1, 0, 0, 1,

            0, 1, 0, 1,
            0, 1, 0, 1,
            0, 1, 0, 1,

            1, 1, 0, 1,
            1, 1, 0, 1,
            1, 1, 0, 1,

            0, 1, 1, 1,
            0, 1, 1, 1,
            0, 1, 1, 1,

            1, 0, 1, 1,
            1, 0, 1, 1,
            1, 0, 1, 1,

            0, 0, 1, 1,
            0, 0, 1, 1,
            0, 0, 1, 1
    };


// switched the front and back because we're outside the box (for skybox textures)
    public static final float[] prismMeshBaseVerts = {
        // front (POSZ) switched
        -1, 1, -1,
        1, 1, -1,
        -1, -1, -1,
        1, -1, -1,
        // back (NEGZ) switched
        1, 1, 1,
        -1, 1, 1,
        1, -1, 1,
        -1, -1, 1,
        // right (POSX)
        1, 1, -1,
        1, 1, 1,
        1, -1, -1,
        1, -1, 1,
        // left (NEGX)
        -1, 1, 1,
        -1, 1, -1,
        -1, -1, 1,
        -1, -1, -1,
        // top (POSY)
        -1, 1, 1,
        1, 1, 1,
        -1, 1, -1,
        1, 1, -1,
        // bottom (NEGY)
        -1, -1, -1,
        1, -1, -1,
        -1, -1, 1,
        1, -1, 1
    };

    public static final float[] prismMeshBaseUvs = {
        // front
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        // back
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        // right
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        // left
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        // top
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        // bottom
        0, 0,
        1, 0,
        0, 1,
        1, 1
    };

    public static final float[] prismMeshBaseNorms = {
            // front
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            // back
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            // right
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            // left
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            // top
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            // bottom
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0
    };

    public static final short[] prismMeshBaseFaces =  {
        // front
        0, 1, 2,
        3, 2, 1,
        // back
        4, 5, 6,
        7, 6, 5,
        // right
        8, 9,10,
        11,10, 9,
        // left
        12,13,14,
        15,14,13,
        // top
        16,17,18,
        19,18,17,
        // bottom
        20,21,22,
        23,22,21
    };

    // inside the box
    final static float ERR2 = 1.0f/2000.0f;

    static float[] skyboxmeshVerts = {
        // back (POSZ)
        -1, 1, 1,
        1, 1, 1,
        -1, -1, 1,
        1, -1, 1,
        // front (NEGZ)
        1, 1, -1,
        -1, 1, -1,
        1, -1, -1,
        -1, -1, -1,
        // right (POSX)
        1, 1, 1,
        1, 1, -1,
        1, -1, 1,
        1, -1, -1,
        // left (NEGX)
        -1, 1, -1,
        -1, 1, 1,
        -1, -1, -1,
        -1, -1, 1,
        // top (POSY)
        -1, 1, -1,
        1, 1, -1,
        -1, 1, 1,
        1, 1, 1,
        // bot (NEGY)
        -1, -1, 1,
        1, -1, 1,
        -1, -1, -1,
        1, -1, -1
    };

    static float[] skyboxmeshUVs = {
        // front
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2,
        // back
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2,
        // right
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2,
        // left
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2,
        // top
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2,
        // bot
        ERR2, ERR2,
        1 - ERR2, ERR2,
        ERR2, 1 - ERR2,
        1 - ERR2, 1 - ERR2
    };

    static short[] skyboxmeshFaces = {
        // front
        0, 1, 2,
        3, 2, 1,
        // back
        4, 5, 6,
        7, 6, 5,
        // right
        8, 9, 10,
        11, 10, 9,
        // left
        12, 13, 14,
        15, 14, 13,
        // top
        16, 17, 18,
        19, 18, 17,
        // bot
        20, 21, 22,
        23, 22, 21
    };

    public static Model buildprismmodel(String name,float[] size,String texname,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            int i,j,k=0;
            Mesh prismmesh = new Mesh();
            if (size == null) {
                prismmesh.verts = prismMeshBaseVerts;
            } else {
                prismmesh.verts = new float[3*24];
                for (j = 0; j < 24; ++j) {
                    for (i = 0; i < 3; ++i, ++k) {
                        prismmesh.verts[k] = size[i] * prismMeshBaseVerts[k];
                    }
                }
            }
            prismmesh.uvs = prismMeshBaseUvs;
            prismmesh.norms = prismMeshBaseNorms;
            prismmesh.faces = prismMeshBaseFaces;
            mod.setmesh(prismmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildprism(String name, float[] size,String texname,String shadername) {
        Model mod = buildprismmodel(name,size,texname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    public static Model2 buildprismmodel6(String name,float[] size,String basetexname,String shadername) {
        boolean no = false;
        if (no)
            return null;
        Model2 mod = Model2.createmodel(name);
        if (mod.refcount == 1) {
            int i,j,k = 0;
            Mesh prismmesh = new Mesh();
            if (size == null) {
                prismmesh.verts = prismMeshBaseVerts;
            } else {
                prismmesh.verts = new float[3*24];
                for (j = 0; j < 24; ++j) {
                    for (i = 0; i < 3; ++i,++k) {
                        prismmesh.verts[k] = size[i] * prismMeshBaseVerts[k];
                    }
                }
            }
            prismmesh.uvs = prismMeshBaseUvs;
            prismmesh.faces = skyboxmeshFaces;
            mod.setmesh(prismmesh);
            for (k=0;k<Texture.cubeenums.length;++k) {
                String key = Texture.cubeenums[k].key;
                //Utils.has
                if (true)
                    mod.addmat(shadername,basetexname + "/" + key + ".jpg",2,4);
                else
                    mod.addmat(shadername,basetexname + "/" + key,2,4);
                //mod.addmat(shadername,key + "_" + basetexname,2,4);
                //mod.addmat("tex","maptestnck.png",2,4);
            }
            /*
            var keys = Object.keys(cubeenums);
            for (var k=0;k<keys.length;++k) {
                var key = keys[k];
                mod.addmat(shadername,key+"_"+basetexname,2,4);
                //mod.addmat("tex","maptestnck.png",2,4);
            }*/
            mod.commit();
        }
        Texture.setWrapMode();
        return mod;
    }

    public static Tree buildprism6(String name,float[] size,String basetexname,String shadername) {
        Model2 mod = buildprismmodel6(name,size,basetexname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

/*
    public static Tree buildprism6(String name, float[] size, String texture, String shader) {
        return new Tree(name);
    }
*/
/*
    function buildskyboxmodel(name,size,basetexname,shadername) {
	globaltexflags = textureflagenums.CLAMPU | textureflagenums.CLAMPV;
    var mod = Model2.createmodel(name);
    mod.flags = modelflagenums.NOZBUFFER | modelflagenums.ISSKYBOX;
	if (mod.refcount == 1) {
		var i,j,k;
	    skyboxmesh.verts = [];
		for (j=0;j<24;++j) {
			for (i=0;i<3;++i) {
				skyboxmesh.verts.push(size[i]*skyboxmesh.baseverts[3*j+i]);
			}
		}
	    mod.setmesh(skyboxmesh);
		var keys = Object.keys(cubeenums);
		for (var k=0;k<keys.length;++k) {
			var key = keys[k];
			mod.addmat(shadername,key+"_"+basetexname,2,4);
			//mod.addmat("tex","maptestnck.png",2,4);
		}
	    mod.commit();
	}
	globaltexflags = 0;
    return mod;
}

/*
function buildskyboxmodel2(name,size,basetexname,shadername) {
    var mod = Model2.createmodel(name);
    mod.flags = modelflagenums.NOZBUFFER | modelflagenums.ISSKYBOX;
	if (mod.refcount == 1) {
		var i,j,k;
	    skyboxmesh.verts = [];
		for (j=0;j<24;++j) {
			for (i=0;i<3;++i) {
				skyboxmesh.verts.push(size[i]*skyboxmesh.baseverts[3*j+i]);
			}
		}
	    mod.setmesh(skyboxmesh);
		mod.addmat(shadername,basetexname,12,24);
	    mod.commit();
	}
    return mod;
}

function buildskybox2(name,size,basetexname,shadername) {
	alert("skyboxmodel2");
	var mod = buildskyboxmodel2(name,size,basetexname,shadername);
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}

*/

/* function buildskybox(name,size,basetexname,shadername) {
    var res = basetexname.indexOf("CUB_");
    if (res == 0)
        return buildskybox2(name,size,basetexname,shadername);
    var mod = buildskyboxmodel(name,size,basetexname,shadername);
    var ret = new Tree2(name);
    ret.setmodel(mod);
    return ret;
}
    */

    /*public static Tree buildskybox(String name, float[] size, String texture, String shader) {
        return new Tree(name);
    }*/


    public static Model2 buildskyboxmodel(String name,float[] size,String basetexname,String shadername) {
        Texture.setClampMode();
        Model2 mod = Model2.createmodel(name);
        mod.flags |= Model.FLAG_NOZBUFFER | Model.FLAG_ISSKYBOX;
        if (mod.refcount == 1) {
            int i,j,k=0;
            Mesh skyboxmesh = new Mesh();
            if (size == null) {
                skyboxmesh.verts = skyboxmeshVerts;
            } else {
                skyboxmesh.verts = new float[3*24];
                for (j = 0; j < 24; ++j) {
                    for (i = 0; i < 3; ++i, ++k) {
                        skyboxmesh.verts[k] = size[i] * skyboxmeshVerts[k];
                    }
                }
            }
            skyboxmesh.uvs = skyboxmeshUVs;
            skyboxmesh.faces = skyboxmeshFaces;
            mod.setmesh(skyboxmesh);
            for (k=0;k<Texture.cubeenums.length;++k) {
                String key = Texture.cubeenums[k].key;
                //Utils.has
                mod.addmat(shadername,basetexname + "/" + key + ".jpg",2,4);
                //mod.addmat(shadername,key + "_" + basetexname,2,4);
                //mod.addmat("tex","maptestnck.png",2,4);
            }
            Utils.pushandsetdir("skybox");
            mod.commit();
            Utils.popdir();
        }
        Texture.setWrapMode();
        return mod;
    }

    public static Tree buildskybox(String name,float[] size,String basetexname,String shadername) {
        //var res = basetexname.indexOf("CUB_");
        //if (res == 0)
        //    return buildskybox2(name,size,basetexname,shadername);
        Model2 mod = buildskyboxmodel(name,size,basetexname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    // sphere
// fix 'wrap gap' in sphere mesh, after random tweeking of verts
// 2d array runs down in y
    public static void spherefixpatch(Mesh mesh) {
        int p2left = 0;
        int p3left = 0;
        int p2right = spherepatchi*(spherepatchj+1);
        int p3right = p2right;
        p2right *= 2;
        p3right *= 3;
        // fix prime meridian
        int i,j;
        for (j=0;j<=spherepatchj;++j) {
            mesh.verts[p3right  ] = mesh.verts[p3left  ];
            mesh.verts[p3right+1] = mesh.verts[p3left+1];
            mesh.verts[p3right+2] = mesh.verts[p3left+2];
            if (mesh.norms != null) {
                mesh.norms[p3right  ] = mesh.norms[p3left  ];
                mesh.norms[p3right+1] = mesh.norms[p3left+1];
                mesh.norms[p3right+2] = mesh.norms[p3left+2];
            }
            mesh.uvs[p2right  ] = mesh.uvs[p2left  ] + spherepatchu;
            mesh.uvs[p2right+1] = mesh.uvs[p2left+1];
            p2left += 2;
            p3left += 3;
            p2right += 2;
            p3right += 3;
        }
        // fix top verts
        int step3 = 3*(spherepatchj+1);
        int p3top = step3;
        for (i=1;i<=spherepatchi;++i) {
            mesh.verts[p3top  ] = mesh.verts[0];
            mesh.verts[p3top+1] = mesh.verts[1];
            mesh.verts[p3top+2] = mesh.verts[2];
            p3top += step3;
        }
        // fix bot verts
        int p3bot0 = 3*spherepatchj;
        int p3bot = p3bot0 + step3;
        for (i=1;i<=spherepatchi;++i) {
            mesh.verts[p3bot  ] = mesh.verts[p3bot0  ];
            mesh.verts[p3bot+1] = mesh.verts[p3bot0+1];
            mesh.verts[p3bot+2] = mesh.verts[p3bot0+2];
            p3bot += step3;
        }
    }

    public static int spherepatchi = 40;
    public static int spherepatchj = 40;
    public static float spherepatchu = 3;
    public static float spherepatchv = 3;

    public static Mesh buildspheremesh(float rad) {
        return buildpatch(spherepatchi,spherepatchj,spherepatchu,spherepatchv,new SphereSurf(rad));
    }

    public static Model buildspheremodel(String name,float rad,String texname,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh spheremesh = buildspheremesh(rad);
            mod.setmesh(spheremesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildsphere(String name,float rad,String texname,String shadername) {
        Model mod = buildspheremodel(name, rad, texname, shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    public static Model buildspheremodel2t(String name,float rad,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh spheremesh = buildspheremesh(rad);
            mod.setmesh(spheremesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildsphere2t(String name,float rad,String texname1,String texname2,String shadername) {
        Model mod = buildspheremodel2t(name,rad,texname1,texname2,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

/*
function buildspheremesh3(rad3) {
	return buildpatch(spherepatchi,spherepatchj,spherepatchu,spherepatchv,spheref_surf3(rad3));
}

function buildspheremodel3(name,rad3,texname,shadername) {
	var mod = Model.createmodel(name);
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		//var spheremesh = buildpatch(8,8,3,3,spheref_surf(rad));
		var spheremesh3 = buildspheremesh3(rad3);
	    mod.setmesh(spheremesh3);
	    mod.settexture(texname);
	    mod.commit();
	}
    return mod;
}

function buildsphere3(name,rad3,texname,shadername) {
	var mod = buildspheremodel3(name,rad3,texname,shadername);
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}
*/

    public static int cylpatchi = 40;
    public static int cylpatchj = 5;
    public static float cylpatchu = 3;
    public static float cylpatchv = 3;

    // cylinder middle
    public static Mesh buildcylinderxzmeshmid(float rad,float hit) {
        return buildpatch(cylpatchi,cylpatchj,cylpatchu,cylpatchv,new Cylinderxz_mid_surf(rad,hit));
    }

    public static Model buildcylinderxzmidmodel(String name,float rad,float hit,String texname,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_mid");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshmid(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    // cylinder top
    public static Mesh buildcylinderxzmeshtop(float rad,float hit) {
        return buildpatch(cylpatchi,cylpatchj,cylpatchu,cylpatchv,new Cylinderxz_top_surf(rad,hit));
    }

    public static Model buildcylinderxztopmodel(String name,float rad,float hit,String texname,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_top");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshtop(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    // cylinder bottom
    public static Mesh buildcylinderxzmeshbot(float rad,float hit) {
        return buildpatch(cylpatchi,cylpatchj,cylpatchu,cylpatchv,new Cylinderxz_bot_surf(rad,hit));
    }


    public static Model buildcylinderxzbotmodel(String name,float rad,float hit,String texname,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_bot");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshbot(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    // cylinder middle

    public static Model buildcylinderxzmidmodel2t(String name,float rad,float hit,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_mid");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshmid(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    // cylinder top
    public static Model buildcylinderxztopmodel2t(String name,float rad,float hit,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_top");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshtop(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    // cylinder bottom
    public static Model buildcylinderxzbotmodel2t(String name,float rad,float hit,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel("cyl_" + name + "_bot");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh cylmesh = buildcylinderxzmeshbot(rad,hit);
            mod.setmesh(cylmesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    // cylinder tree
    public static Tree buildcylinderxz(String name,float rad,float hit,String texname,String shadername) {
        // the whole cylinder
        Tree grp = new Tree(name);
        // mid section
        Tree mid = new Tree(name);
        Model modmid = buildcylinderxzmidmodel(name,rad,hit,texname,shadername);
        mid.setmodel(modmid);
        grp.linkchild(mid);
        // top section
        Tree top = new Tree(name);
        Model modtop = buildcylinderxztopmodel(name,rad,hit,texname,shadername);
        top.setmodel(modtop);
        grp.linkchild(top);
        // bot section
        Tree bot = new Tree(name);
        Model modbot = buildcylinderxzbotmodel(name,rad,hit,texname,shadername);
        bot.setmodel(modbot);
        grp.linkchild(bot);
        // everything
        return grp;
    }

    // cylinder tree
    public static Tree buildcylinderxz2t(String name,float rad,float hit,String texname,String texname2t,String shadername) {
        // the whole cylinder
        Tree grp = new Tree(name);
        // mid section
        Tree mid = new Tree(name);
        Model modmid = buildcylinderxzmidmodel2t(name,rad,hit,texname,texname2t,shadername);
        mid.setmodel(modmid);
        grp.linkchild(mid);
        // top section
        Tree top = new Tree(name);
        Model modtop = buildcylinderxztopmodel2t(name,rad,hit,texname,texname2t,shadername);
        top.setmodel(modtop);
        grp.linkchild(top);
        // bot section
        Tree bot = new Tree(name);
        Model modbot = buildcylinderxzbotmodel2t(name,rad,hit,texname,texname2t,shadername);
        bot.setmodel(modbot);
        grp.linkchild(bot);
        // everything
        return grp;
    }


    public static int conepatchi = 40;
    public static int conepatchj = 5;
    public static float conepatchu = 3;
    public static float conepatchv = 3;

    // cone middle
    public static Mesh buildconexzmeshmid(float rad,float hit) {
        return buildpatch(conepatchi,conepatchj,conepatchu,conepatchv,new Conexz_mid_surf(rad,hit));
    }

    public static Model buildconexzmidmodel(String name,float rad,float hit,String texname,String shadername) {
        Model mod = Model.createmodel("cone_" + name + "_mid");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh conemesh = buildconexzmeshmid(rad,hit);
            mod.setmesh(conemesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }


    public static Model buildconexzmidmodel2t(String name,float rad,float hit,String texname,String texname2,String shadername) {
	Model mod = Model.createmodel(name + "_mid");
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		//var spheremesh = buildpatch(8,8,3,3,spheref_surf(rad));
		Mesh conemesh = buildconexzmeshmid(rad,hit);
	    mod.setmesh(conemesh);
	    mod.settexture(texname);
	    mod.settexture2(texname2);
	    mod.commit();
	}
    return mod;
}


    // cone bottom
    public static Mesh buildconexzmeshbot(float rad,float hit) {
        return buildpatch(conepatchi,conepatchj,conepatchu,conepatchv,new Conexz_bot_surf(rad,hit));
    }

    public static Model buildconexzbotmodel(String name,float rad,float hit,String texname,String shadername) {
        Model mod = Model.createmodel("cone_" + name + "_bot");
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh conemesh = buildconexzmeshbot(rad,hit);
            mod.setmesh(conemesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }


    public static Model buildconexzbotmodel2t(String name,float rad,float hit,String texname,String texname2,String shadername) {
	Model mod = Model.createmodel(name + "_bot");
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		//var spheremesh = buildpatch(8,8,3,3,spheref_surf(rad));
		Mesh conemesh = buildconexzmeshbot(rad,hit);
	    mod.setmesh(conemesh);
	    mod.settexture(texname);
	    mod.settexture2(texname2);
	    mod.commit();
	}
    return mod;
}


    // cone tree
    public static Tree buildconexz(String name,float rad,float hit,String texname,String shadername) {
        // the whole cone
        Tree grp = new Tree(name);
        // mid section
        Tree mid = new Tree(name);
        Model modmid = buildconexzmidmodel(name,rad,hit,texname,shadername);
        mid.setmodel(modmid);
        grp.linkchild(mid);
        // bot section
        Tree bot = new Tree(name);
        Model modbot = buildconexzbotmodel(name,rad,hit,texname,shadername);
        bot.setmodel(modbot);
        grp.linkchild(bot);
        // everything
        return grp;
    }


// cone tree
    public static Tree buildconexz2t(String name,float rad,float hit,String texname,String texname2,String shadername) {
	// the whole cone
	Tree grp = new Tree(name);
	// mid section
	Tree mid = new Tree(name);
	Model modmid = buildconexzmidmodel2t(name,rad,hit,texname,texname2,shadername);
	mid.setmodel(modmid);
	grp.linkchild(mid);
	// bot section
	Tree bot = new Tree(name);
	Model modbot = buildconexzbotmodel2t(name,rad,hit,texname,texname2,shadername);
	bot.setmodel(modbot);
	grp.linkchild(bot);
	// everything
	return grp;
}


    // planexy,planexz
    public static int planepatchi = 2;
    public static int planepatchj = 2;
    public static float planepatchu = 1;
    public static float planepatchv = 1;

    // planexz
    static class PlanexzSurf extends Surf {
        float mWid;
        float mHit;

        PlanexzSurf(float wid,float hit) {
            mWid = wid;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float startx = -mWid;
            float stepx = 2.0f*mWid;
            float startz = mHit;
            float stepz = -2.0f*mHit;
            float[] v = new float[3];
            float[] n = new float[3];
            n[0] = 0;
            n[1] = 1;
            n[2] = 0;
            v[0] = startx + p*stepx;
            v[1] = 0;
            v[2] = startz + q*stepz;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    public static Mesh buildplanexzmesh(float wid,float hit) {
        return buildpatch(planepatchi,planepatchj,planepatchu,planepatchv,new PlanexzSurf(wid,hit));
    }

    public static Model buildplanexzmodel(String name,float wid,float hit,String texname,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh planexzmesh = buildplanexzmesh(wid,hit);
            mod.setmesh(planexzmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildplanexz(String name, float wid, float hit, String texname, String shadername) {
        Model mod = buildplanexzmodel(name,wid,hit,texname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    public static Model buildplanexzmodel2t(String name,float wid,float hit,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh planexzmesh = buildplanexzmesh(wid,hit);
            mod.setmesh(planexzmesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildplanexz2t(String name, float wid, float hit, String texname1,String texname2,String shadername) {
        Model mod = buildplanexzmodel2t(name,wid,hit,texname1,texname2,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

/*
function buildplanexzmesh(wid,hit) {
	return buildpatch(planepatchi,planepatchj,planepatchu,planepatchv,planexz_surf(wid,hit));
}

function buildplanexzmodel(name,wid,hit,texname,shadername) {
	var mod = Model.createmodel(name);
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		//var spheremesh = buildpatch(8,8,3,3,spheref_surf(rad));
		var planexzmesh = buildplanexzmesh(wid,hit);
	    mod.setmesh(planexzmesh);
	    mod.settexture(texname);
	    mod.commit();
	}
    return mod;
}

function buildplanexz(name,wid,hit,texname,shadername) {
	var mod = buildplanexzmodel(name,wid,hit,texname,shadername);
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}

function buildplanexzmodel2t(name,wid,hit,texname1,texname2,shadername) {
	var mod = Model.createmodel(name);
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		var planexzmesh = buildplanexzmesh(wid,hit);
	    mod.setmesh(planexzmesh);
	    mod.settexture(texname1);
	    mod.settexture2(texname2);
	    mod.commit();
	}
    return mod;
}

function buildplanexz2t(name,wid,hit,texname1,texname2,shadername) {
	var mod = buildplanexzmodel2t(name,wid,hit,texname1,texname2,shadername);
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}
     */

    // planexy
    static class PlanexySurf extends Surf {
        float mWid;
        float mHit;

        PlanexySurf(float wid,float hit) {
            mWid = wid;
            mHit = hit;
        }

        @Override
        SurfVertex surf(float p, float q) {
            float startx = -mWid;
            float stepx = 2.0f*mWid;
            float starty = mHit;
            float stepy = -2.0f*mHit;
            float[] v = new float[3];
            float[] n = new float[3];
            n[0] = 0;
            n[1] = 0;
            n[2] = -1;
            v[0] = startx + p*stepx;
            v[1] = starty + q*stepy;
            v[2] = 0;
            SurfVertex vert = new SurfVertex();
            vert.v = v;
            vert.n = n;
            return vert;
        }
    }

    public static Mesh buildplanexymesh(float wid,float hit) {
        return buildpatch(planepatchi,planepatchj,planepatchu,planepatchv,new PlanexySurf(wid,hit));
    }

    public static Model buildplanexymodel(String name,float wid,float hit,String texname,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh planexymesh = buildplanexymesh(wid,hit);
            mod.setmesh(planexymesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildplanexy(String name, float wid, float hit, String texname, String shadername) {
        Model mod = buildplanexymodel(name,wid,hit,texname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    public static Model buildplanexymodel2t(String name,float wid,float hit,String texname1,String texname2,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh planexymesh = buildplanexymesh(wid,hit);
            mod.setmesh(planexymesh);
            mod.settexture(texname1);
            mod.settexture2(texname2);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildplanexy2t(String name, float wid, float hit, String texname1,String texname2, String shadername) {
        Model mod = buildplanexymodel2t(name,wid,hit,texname1,texname2,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    public static Model buildplanexymodelNt(String name,float wid,float hit,String[] texnames,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh planexymesh = buildplanexymesh(wid,hit);
            mod.setmesh(planexymesh);
            mod.settextureNArray(texnames);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildplanexyNt(String name, float wid, float hit, String[] texnames, String shadername) {
        Model mod = buildplanexymodelNt(name,wid,hit,texnames,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    /*
function buildplanexymodel2t(name,wid,hit,texname1,texname2,shadername) {
	var mod = Model.createmodel(name);
	if (mod.refcount == 1) {
	    mod.setshader(shadername);
		var planexymesh = buildplanexymesh(wid,hit);
	    mod.setmesh(planexymesh);
	    mod.settexture(texname1);
	    mod.settexture2(texname2);
	    mod.commit();
	}
    return mod;
}

function buildplanexy2t(name,wid,hit,texname1,texname2,shadername) {
	var mod = buildplanexymodel2t(name,wid,hit,texname1,texname2,shadername);
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}
 */

    // torusxz
    public static int toruspatchi = 40;
    public static int toruspatchj = 40;
    public static float toruspatchu = 3;
    public static float toruspatchv = 3;

    public static Mesh buildtorusxzmesh(float rad0,float rad1) {
        return buildpatch(toruspatchi,toruspatchj,toruspatchu,toruspatchv,new TorusxzSurf(rad0,rad1));
    }

    public static Model buildtorusxzmodel(String name,float rad0,float rad1,String texname,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh torusxzmesh = buildtorusxzmesh(rad0,rad1);
            mod.setmesh(torusxzmesh);
            mod.settexture(texname);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildtorusxz(String name,float rad0,float rad1,String texname,String shadername) {
        Model mod = buildtorusxzmodel(name,rad0,rad1,texname,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

/*
    public static Tree buildpaperairplane(String name,String shadername) {

//	var mod = buildtorusxzmodel(name,1,.5,texname,shadername);
        Mesh paperairplanemesh = new Mesh();

        paperairplanemesh.verts = paperairplanemeshVerts;
        paperairplanemesh.cverts = paperairplanemeshCVerts;

        //
        //paperairplanemesh.verts = prismMeshBaseVerts;
        //paperairplanemesh.faces = prismMeshBaseFaces;
        //
        //paperairplanemesh.verts = new float[] {-1,1,0,1,1,0,-1,-1,0};
        //paperairplanemesh.verts = new float[] {-1,0,1,1,0,1,-1,0,-1};
        //paperairplanemesh.verts = paperairplanemeshVerts;
        //paperairplanemesh.faces = new short[] {0,1,2};
        Model mod = Model.createmodel(name);
        mod.setmesh(paperairplanemesh);
        //mod.settexture(texname);
        mod.setshader(shadername);
        mod.commit();
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }
*/
    public static Model buildpaperairplanemodel(String name,String shadername) {
        Model mod = Model.createmodel(name);
        if (mod.refcount == 1) {
            mod.setshader(shadername);
            Mesh paperairplanemesh = new Mesh();
            paperairplanemesh.verts = paperairplanemeshVerts;
            paperairplanemesh.cverts = paperairplanemeshCVerts;
            mod.setmesh(paperairplanemesh);
            mod.commit();
        }
        return mod;
    }

    public static Tree buildpaperairplane(String name,String shadername) {
        Model mod = buildpaperairplanemodel(name,shadername);
        Tree ret = new Tree(name);
        ret.setmodel(mod);
        return ret;
    }

    /*
function buildpaperairplane(name,texname,shadername) {
//	var mod = buildtorusxzmodel(name,1,.5,texname,shadername);
	var mod = Model.createmodel(name);
	mod.setmesh(paperairplanemesh);
	mod.settexture(texname);
	mod.setshader(shadername);
	mod.commit();
	var ret = new Tree2(name);
	ret.setmodel(mod);
	return ret;
}
     */

}
