package com.mperpetuo.openglgallery.engatest;

import com.mperpetuo.openglgallery.enga.Colors;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.Quat;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.VEC;
import com.mperpetuo.openglgallery.enga.VECNuMath;

import java.util.ArrayList;

public class HelperObj {
    private ArrayList<Tree> helplist = new ArrayList<>();
    private Tree preload = new Tree("helper_preload"); // prevent reload of models
    private static final float thinscale = .015f;

    public HelperObj() {
        addvector(preload,new VEC(),new VEC(1,1,1), Colors.F32WHITE);
    }


    // y pyramid base almost +-2 , 0, +-2 top is almost 0,1,0, for vector
    Model buildavectory() {
        Model m = Model.createmodel("helper_vector");
        if (m.refcount == 1) {
            //pushandsetdir("gfxtest");
            //vector<pointf3> v(prismverts,prismverts+24);
            //vector<uv> uvs(prismuvs,prismuvs+24);
            float[] v = ModelUtil.prismMeshBaseVerts.clone();
            float[] uvs = ModelUtil.prismMeshBaseUvs.clone();

            int i;
            final float thinscaler = 1.0f/thinscale;

            for (i=0;i<24;++i) {
                int vi = i*3;
                int ui = i*2;
                v[vi+1]=.5f*ModelUtil.prismMeshBaseVerts[vi+1]+.5f;
                v[vi] -= v[vi]*v[vi+1]*.8f;
                v[vi+2] -= v[vi+2]*v[vi+1]*.8f;
                uvs[ui]*=1;
                uvs[ui+1]*=.5*thinscaler;
            }
            m.setverts(v); // taper to (0,1,0) at y = 1
            m.setuvs(uvs);
            m.setfaces(ModelUtil.prismMeshBaseFaces);
            m.settexture("maptestnck.tga");
            m.setshader("texc");
            m.commit();
            //popdir();
        }
        return m;
    };
    public void addvector(Tree rt, VEC p0, VEC p1, float[] color) {

//	pointf3 nrm;
        VEC nrm = new VEC(p1.x-p0.x,p1.y-p0.y,p1.z-p0.z);
//	nrm.x=p1.x-p0.x;
//	nrm.y=p1.y-p0.y;
//	nrm.z=p1.z-p0.z;
        float f = VECNuMath.normalize(nrm);
        if (f == 0)
            return;
        Model modpris = buildavectory();
        Tree lin = new Tree("helper_vector");
        //lin->buildo2p=O2P_FROMTRANSQUATSCALE;
        lin.setmodel(modpris);
        lin.trans = new float[] {p0.x,p0.y,p0.z};
        //var up = vec3.fromValues(0,1,0);
        //var q = vec4.fromValues(0,0,0,1);
        //normal2quat(&up,&nrm,&q);

        //float[] q = dir2quat(nrm);

        float[] q = new float[4];
        float[] nrma = {nrm.x, nrm.y, nrm.z};
        Quat.dir2quat(q, nrma);

        lin.qrot = q;

        lin.scale = new float[] {thinscale * f, f, thinscale * f};
        lin.mat.put("color",color);
        rt.linkchild(lin);
        //if (!linger)
        helplist.add(lin);
    }


    public void reset() {
        int i;
        for (i=0;i<helplist.size();++i) {
            Tree tre = helplist.get(i);
            tre.glFree();
            //tre.unlinkchild();
        }
        //vector<tree2*>::iterator i;
        //for (i=helplist.begin();i!=helplist.end();++i)
        //	delete *i;
        this.helplist.clear(); // clear
    }

    public void glfree() {
        preload.glFree();
    }

}
/*
    function helperobj() {
        this.helplist = [];
        this.preload = new Tree2("helper_preload"); // prevent reload of models
        //addbox(preload,pointf3x(),pointf3x(1,1,1),F32WHITE);
        //addsphere(preload,pointf3x(),1,F32WHITE);
        //addline(preload,pointf3x(),pointf3x(1,1,1),F32WHITE);
        this.addvector(this.preload,vec3.create(),vec3.fromValues(1,1,1),F32WHITE);
        //helplist.clear(); // 'keep' 'preload'
    }

helperobj.thinscale = .015;


// y pyramid base almost +-2 , 0, +-2 top is almost 0,1,0, for vector
        helperobj.buildavectory = function() {
        var m = Model.createmodel("helper_vector");
        if (m.refcount == 1) {
        //pushandsetdir("gfxtest");
        //vector<pointf3> v(prismverts,prismverts+24);
        //vector<uv> uvs(prismuvs,prismuvs+24);
        var v = prismmesh.baseverts.slice(0);
        var uvs = prismmesh.uvs.slice(0);

        var i;
        var thinscaler = 1.0/helperobj.thinscale;

        for (i=0;i<24;++i) {
        var vi = i*3;
        var ui = i*2;
        v[vi+1]=.5*prismmesh.baseverts[vi+1]+.5;
        v[vi] -= v[vi]*v[vi+1]*.8;
        v[vi+2] -= v[vi+2]*v[vi+1]*.8;
        uvs[ui]*=1;
        uvs[ui+1]*=.5*thinscaler;
        }
        m.setverts(v); // taper to (0,1,0) at y = 1
        m.setuvs(uvs);
        m.setfaces(prismmesh.faces);
        m.settexture("maptestnck.tga");
        m.setshader("texc");
        m.commit();
        }
        return m;
        };


        helperobj.prototype.addvector = function(rt,p0,p1,color) {
//	pointf3 nrm;
        var nrm = vec3.fromValues(p1.x-p0.x,p1.y-p0.y,p1.z-p0.z);
//	nrm.x=p1.x-p0.x;
//	nrm.y=p1.y-p0.y;
//	nrm.z=p1.z-p0.z;
        var f=vec3.normalize3d(nrm,nrm);
        if (f==0)
        return;
        var modpris = helperobj.buildavectory();
        var lin = new Tree2("helper_vector");
        //lin->buildo2p=O2P_FROMTRANSQUATSCALE;
        lin.setmodel(modpris);
        lin.trans=vec3.fromValues(p0.x,p0.y,p0.z);
        //var up = vec3.fromValues(0,1,0);
        //var q = vec4.fromValues(0,0,0,1);
        //normal2quat(&up,&nrm,&q);
        var q = dir2quat(nrm);
        lin.qrot=q;

        lin.scale=vec3.fromValues(helperobj.thinscale*f,f,helperobj.thinscale*f);
        lin.mat.color=vec4.clone(color);
        rt.linkchild(lin);
        //if (!linger)
        this.helplist.push(lin);
        };

        helperobj.prototype.reset = function() {
        var i;
        for (i=0;i<this.helplist.length;++i) {
        var tre = this.helplist[i];
        tre.glfree();
        //tre.unlinkchild();
        }
        //vector<tree2*>::iterator i;
        //for (i=helplist.begin();i!=helplist.end();++i)
        //	delete *i;
        this.helplist = []; // clear
        };

        helperobj.prototype.glfree = function() {
        this.preload.glfree();
        };
*/
