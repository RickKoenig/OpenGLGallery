package com.mperpetuo.openglgallery.engatest.physics3d;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.engatest.HelperObj;
import com.mperpetuo.openglgallery.input.*;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mperpetuo.openglgallery.enga.NuMath.EPSILON;
import static com.mperpetuo.openglgallery.enga.VECNuMath.*;
import static com.mperpetuo.openglgallery.enga.VECQuat.*;
import static com.mperpetuo.openglgallery.engatest.physics3d.CollUtil.MAXBOX2BOX;
import static com.mperpetuo.openglgallery.engatest.physics3d.CollUtil.line2btricull;
import static com.mperpetuo.openglgallery.engatest.physics3d.ObjCollisions.*;
import static com.mperpetuo.openglgallery.engatest.physics3d.Physics3d.PHY_OBJ.OBJ_BOX;
import static com.mperpetuo.openglgallery.engatest.physics3d.PrimColl.*;


public class Physics3d extends State {
    final static String TAG = "Physics3d";

    static int curscene;

    static float[] viewPos;// = new float[3];
    static float[] viewRot;// = new float[3];

    int framenum;

    final int NCORNERS = 8;
    final int FIRSTMESHOBJ = 1;
    private SimpleUI.UIPrintArea scenearea;

    private HelperObj ho;

    static boolean showVector = false;

    private Runnable resetScene = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"in resetScene");
            StateMan.changeState("physics3d.Physics3d");
        }
    };

    private Runnable nextScene = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"in nextScene");
            ++curscene;
            if (curscene == numscenes)
                curscene = 0;
            StateMan.changeState("physics3d.Physics3d");
        }
    };

    private Runnable prevScene = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"in prevScene");
            --curscene;
            if (curscene < 0)
                curscene = numscenes - 1;
            StateMan.changeState("physics3d.Physics3d");
        }
    };
    private int numscenes;
    private static float flyCamSpeed;

    private Runnable showVectors = new Runnable() {
        @Override
        public void run() {
            showVector = !showVector;
        }
    };

    // phyobjects

    class objstate {
        VEC pos = new VEC(),rot = new VEC();
        VEC momentum = new VEC(),angmomentum = new VEC(); // angmom cm
        VEC vel = new VEC(),rotvel = new VEC();	// always derived from above

        void copy(objstate rhs) {
            pos.copy(rhs.pos);
            rot.copy(rhs.rot);
            momentum.copy(rhs.momentum);
            angmomentum.copy(rhs.angmomentum);
            vel.copy(rhs.vel);
            rotvel.copy(rhs.rotvel);
        }

    }

    class nb {
        //int nnb;
        //int visited;
        ArrayList<Integer> nbs = new ArrayList<>();
        //int nnballoced;
    }

    class nbf {
        //int nnbf;
//	int visited;
        ArrayList<Integer> nbfs = new ArrayList<>();
        //int nnbfalloced;
    }

    class phyobject {
        // object
        int kind; // box1, cyl1, sph1, look in objects.txt, right now box1 is special
        VEC scale = new VEC();
        // gen object
        // 8 box points
        VEC[] pnts = new VEC[NCORNERS]; // local bbox 8 points
        VEC[] rpnts = new VEC[NCORNERS]; // world bbox 8 points
        // mesh points if kind>=3
        boolean haswf; // object has current world verts
        int nwpnts;
        int nwfaces;
        VEC[] wpnts; // malloced world verts
        nb[] nbs; // neighboring verts
        nbf[] nbfs; // neighboring faces
        FACE[] lfaces; // handy ptr
        VEC[] lpnts; // handy ptr
        //ArrayList<contact> contacts; // indexed by the other object
        // pos
        objstate s0 = new objstate(),st = new objstate(); // motion: s0 -> st, collisions st->st
        float transenergy;
        float potenergy;
        float rotenergy;
        // parameters
        float mass;
        float elast;
        float frict;
        // generated parameters
        VEC moivec = new VEC();	// try out principal axis
        boolean norot,notrans;
// reference to root node of object
        Tree t;

        phyobject() {
            //Log.e(TAG,"phyobject constructor");
            int i;
            for (i=0;i<NCORNERS;++i) {
                pnts[i] = new VEC();
                rpnts[i] = new VEC();
            }
            // should be initialized outside
            for (i=0;i<MAXBOX2BOX;++i) {
                resv[i] = new VEC();
            }
            for (i=0;i<MAXBOX2BOX/2;++i) {
                v[i] = new VEC();
                vc[i] = new VEC();
            }
        }
    }

    String ascene = "";
    int[] nextvert = {1,2,0};

    // trees
    Tree roottree,footballfield,cam; // top level tree

    enum PHY_OBJ {OBJ_BOX,OBJ_CYL,OBJ_SPH};
    final int MAX3DBOX = 100; // TODO get it from other module
    final int MAXPHYOBJECTS = MAX3DBOX; // 100
    phyobject[] phyobjects = new phyobject[MAXPHYOBJECTS];
    VEC totangmomentum = new VEC(),totangcmmomentum = new VEC(),totangorgmomentum = new VEC();
    VEC totmomentum = new VEC();
    float tottransenergy,totrotenergy,totpotenergy,totenergy;
    float littlegee = 10.0f; //9.8f;

    // for debprint
    int nphyobjects;
    int curphyobjectnum;
    int lastcurphyobjectnum;
    //phyobject curphyobject;
    final int MAXWORLDOBJECTS = 10;
    VEC moisqs[]={ // make this match MAXWORLDOBJECTS
            new VEC(1.0f/12.0f,1.0f/12.0f,1.0f/12.0f),
            new VEC(1.0f/16.0f,1.0f/12.0f,1.0f/16.0f),
            new VEC(1.0f/20.0f,1.0f/20.0f,1.0f/20.0f),
            new VEC(),
            new VEC(),
            new VEC(),
            new VEC(),
            new VEC(),
            new VEC(),
            new VEC(),
    };

    int numworldobjects;
    String[] objkindstr = new String[MAXWORLDOBJECTS];
    ArrayList<String> worldobjectsscript;
    Tree worldobjectsscene;

    float timestep = 1.0f/30.0f; // 1/30
    int iterations = 10; // 10
    VEC result0,result1;

    boolean midpointmethod = true;
    boolean oldgravity = true; // 1 means avoiding resting contact problems

    float timestep2 = 0;
    int bisect;
    float globalelast = 1;
    float sep;

    VEC[] resv = new VEC[MAXBOX2BOX];
    VEC[] v = new VEC[MAXBOX2BOX/2];
    VEC[] vc = new VEC[MAXBOX2BOX/2];
    float[] da = new float[MAXBOX2BOX/2];

    void fixupmodelcm(ModelBase mod,VEC moisqs) {
        Log.i(TAG,"---------- fuxupmodelcm " + mod.name + " ---------------");
        int i,j,f;
        final int SCANY = 2;
        final int SCANZ = 2;
        VEC[][] amin = new VEC[SCANZ][SCANY];
        VEC[][] amax = new VEC[SCANZ][SCANY];
        for (j=0;j<SCANZ;++j) {
            for (i = 0; i < SCANY; ++i) {
                amin[j][i] = new VEC();
                amax[j][i] = new VEC();
            }
        }
        VEC obmin,obmax;
        VEC bmin = new VEC(),bmax = new VEC();
        VEC[] verts;
        VEC v0,v1,v2;
        FACE[] faces;
        int nfaces,nverts;
        int nintsect;
        VEC cm,rm;
        float m,sm;
        obmin = new VEC(mod.boxmin);
        obmax = new VEC(mod.boxmax);
        //logger("obmin = (" + obmin.x + "," + obmin.y + "," + obmin.z + ") obmax = )
        Log.i(TAG,"obmin = " + obmin + " obmax = " + obmax);
        //Log.e(TAG,");")
        verts = VEC.makeVECArray(mod.verts);
        nverts = mod.verts.length/3;
        faces = FACE.makeFACEArray(mod.faces);
        //faces = mod->faces;
        nfaces = mod.faces.length/3;
        rm = new VEC();
        sm = 0;
        for (j=0;j<SCANZ;j++) {
            //Log.e(TAG,"scanning Z " + j + "/" + SCANZ);
            for (i = 0; i < SCANY; i++) {
                nintsect = 0;
                amin[j][i].x = obmin.x * 2 - obmax.x;
                amin[j][i].y = obmin.y + (obmax.y - obmin.y) * (1 + 2 * i) * (.5f / SCANY);
                amin[j][i].z = obmin.z + (obmax.z - obmin.z) * (1 + 2 * j) * (.5f / SCANZ);
                amax[j][i].x = obmax.x * 2 - obmin.x;
                amax[j][i].y = amin[j][i].y;
                amax[j][i].z = amin[j][i].z;
                Log.i(TAG,"calling line2btricull [" + j + "][" + i + "] amin + " + amin[j][i] + " amax " + amax[j][i]);
                for (f = 0; f < nfaces; f++) {
                    v0 = verts[faces[f].vertidx[0]];
                    v1 = verts[faces[f].vertidx[1]];
                    v2 = verts[faces[f].vertidx[2]];
                    if (line2btricull(v0, v1, v2, amin[j][i], amax[j][i], bmin)) {
                        Log.i(TAG,"coll min hit, FACEIDX " + f + ", 3 verts " + v0 + " " + v1 + " " + v2);
                        Log.i(TAG,"intsect " + bmin);
                        nintsect++;
                        break;
                    }
                }
                for (f = 0; f < nfaces; f++) {
                    v0 = verts[faces[f].vertidx[0]];
                    v1 = verts[faces[f].vertidx[1]];
                    v2 = verts[faces[f].vertidx[2]];
                    if (j == 0 && i == 1 && f == 181)
                        Log.w(TAG,"special check of line2btricull");
                    if (line2btricull(v0, v1, v2, amax[j][i], amin[j][i], bmax)) {
                        Log.i(TAG,"coll max hit, FACEIDX " + f + ", 3 verts " + v0 + " " + v1 + " " + v2);
                        Log.i(TAG,"intsect " + bmax);
                        nintsect++;
                        break;
                    }
                    if (j == 0 && i == 1 && f == 181)
                        Log.w(TAG,"end special check of line2btricull");
                }
                if (nintsect == 2) {
                    amax[j][i].copy(bmax);
                    amin[j][i].copy(bmin);
                    m = bmax.x - bmin.x;
                    rm.x += (bmax.x + bmin.x) * .5f * m;
                    rm.y += bmin.y * m;
                    rm.z += bmin.z * m;
                    sm += m;
                    Log.i(TAG,"got 2 intsects, rm = " + rm + " sm = " + sm);
                    //					addline(&tmin,&tmax,rgbwhite);
                } else {
                    amax[j][i] = new VEC();
                    amin[j][i] = new VEC();
                }
            }
        }
        cm = new VEC();
        cm.x = rm.x/sm;
        cm.y = rm.y/sm;
        cm.z = rm.z/sm;
        Log.i(TAG,"cm " + cm);
        for (j=0;j<SCANZ;j++) {
            for (i = 0; i < SCANY; i++) {
                amin[j][i].x -= cm.x;
                amin[j][i].y -= cm.y;
                amin[j][i].z -= cm.z;
                amax[j][i].x -= cm.x;
                amax[j][i].y -= cm.y;
                amax[j][i].z -= cm.z;
            }
        }
        moisqs.copy(new VEC());
        for (j=0;j<SCANZ;j++)
            for (i=0;i<SCANY;i++) {
                bmax = amax[j][i];
                bmin = amin[j][i];
                m = bmax.x - bmin.x;
                moisqs.x += (bmax.x*bmax.x+bmin.x*bmax.x+bmin.x*bmin.x)*(1.0f/3.0f)*m;
                moisqs.y += bmin.y*bmin.y*m;
                moisqs.z += bmin.z*bmin.z*m;
            }
        moisqs.x /= sm;
        moisqs.y /= sm;
        moisqs.z /= sm;
        Log.i(TAG,"moisqs " + moisqs);

        for (j=0;j<nverts;j++) {
            verts[j].x -= cm.x;
            verts[j].y -= cm.y;
            verts[j].z -= cm.z;
        }
        Mesh msh = new Mesh();
        msh.verts = VEC.makeFLOATArray(verts);
        mod.changemesh(msh);
    }

    private void initworldobjects(String worldobjsname) {
        int i;
        String fullname;
        Tree mt;
        worldobjectsscript = new Script(worldobjsname).getData();
        numworldobjects = worldobjectsscript.size();
        //usescnlights=0;
        //mystrncpy(roottree->name,"worldobjectsscene",NAMESIZE);
        if (numworldobjects >= MAXWORLDOBJECTS)
            Utils.alert("too many world objects");
        worldobjectsscene = new Tree("worldObjectScene");
        VEC nomoisq = new VEC(0,0,0);
        for (i=0;i<numworldobjects;i++) {
            objkindstr[i] = worldobjectsscript.get(i);
            //fullname = "";
            fullname = objkindstr[i] + ".BWS";
            //sprintf(fullname,"%s.BWS",worldobjectsscript->idx(i).c_str());
            //linkchildtoparent(loadscene(fullname),worldobjectsscene);
            worldobjectsscene.linkchild(new Tree(fullname));
            if (i >= FIRSTMESHOBJ) {
                //fullname = "";
                //sprintf(fullname,"%s.bwo",worldobjectsscript->idx(i).c_str());
                fullname = objkindstr[i] + ".bwo";
                //mt = worldobjectsscene->find(fullname);
                mt = worldobjectsscene.findtree(fullname);
                if (mt == null)
                    Utils.alert("can't find '" + fullname + "'");
                fixupmodelcm(mt.mod,moisqs[i]);
            }
        }
    }

    private void freeworldobjects() {
        worldobjectsscene.glFree();
        worldobjectsscene = null;
        worldobjectsscript = null;
    }

    // angvel to angmom
    private void domoi(phyobject p,VEC rot,VEC angvelin,VEC angmomout) {
        VEC quat = new VEC();
        if (p.norot)
            return;
        quatinverse(rot,quat);
        quatrot(quat,angvelin,angmomout);
        angmomout.x *= p.moivec.x;
        angmomout.y *= p.moivec.y;
        angmomout.z *= p.moivec.z;
        quatrot(rot,angmomout,angmomout);
    }

    // angmom to angvel
    private void doinvmoi(phyobject p,VEC rot,VEC angmomin,VEC angvelout) {
        VEC quat = new VEC();
        if (p.norot)
            return;
        quatinverse(rot,quat);
        quatrot(quat,angmomin,angvelout);
        angvelout.x /= p.moivec.x;
        angvelout.y /= p.moivec.y;
        angvelout.z /= p.moivec.z;
        quatrot(rot,angvelout,angvelout);
    }

    // always at st, sets p->rotenergy
    void calcangenergy(phyobject p) {
        VEC quat = new VEC();
        VEC proprotvel = new VEC();
        if (p.norot)
            return;
        quatinverse(p.st.rot,quat);
        quatrot(quat,p.st.rotvel,proprotvel);
        p.rotenergy=.5f*(
                p.moivec.x*proprotvel.x*proprotvel.x +
                        p.moivec.y*proprotvel.y*proprotvel.y +
                                p.moivec.z*proprotvel.z*proprotvel.z);
    }

    // instances //////////////////////////////////////////////////
    void initphysicsobjects(String name) {
        Log.e(TAG,"initphysicsobjects " + name);
        littlegee = 10.0f;
        int i,j,k,m,n;
        String fullname;
        Tree obj = null;
        int tp = 0;
        phyobject po = null;
        nphyobjects = -1;
        ArrayList<String> sc = new Script(name).getData();
        int nsc = sc.size();
// read script
        while(tp < nsc) {
            if (sc.get(tp).equals("object")) {
                ++nphyobjects;
                if (nphyobjects >= MAXPHYOBJECTS)
                    Utils.alert("too many objects");

                //po = phyobjects[nphyobjects]; // these are null
                po = new phyobject(); // so create new one and put in
                phyobjects[nphyobjects] = po; // the array

                //memset(po,0,sizeof(struct phyobject));
                po.mass = 1;
                po.s0.rot.w = 1;
                po.elast = 1;
                po.scale.x = 1;
                po.scale.y = 1;
                po.scale.z = 1;
                tp++;
                if (tp+1 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                fullname = sc.get(tp) + ".bwo";
                obj = new Tree(fullname);
                // obj.dissolve=.5f;
                // obj.buildo2p = O2P_FROMTRANSQUATSCALE;
                roottree.linkchild(obj);
                po.t = obj;
                for (i=0;i<numworldobjects;++i)
                    if (sc.get(tp).equals(objkindstr[i])) {
                        po.kind = i;
                        break;
                    }
                if (i == numworldobjects)
                    Utils.alert("unknown object '" + sc.get(tp) + "'");
                tp++;
            } else if (sc.get(tp).equals("pos")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+3 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.s0.pos.x = Float.parseFloat(sc.get(tp++));
                po.s0.pos.y = Float.parseFloat(sc.get(tp++));
                po.s0.pos.z = Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp).equals("littlegee")) {
                tp++;
                if (tp+1 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                littlegee = Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp) .equals("elast")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+1 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.elast = Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp).equals("norot")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.norot = true;
            } else if (sc.get(tp).equals("notrans")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.notrans = true;
            } else if (sc.get(tp).equals("mass")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+1 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.mass = Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp).equals( "rot")) {
                float len;
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+3 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.s0.rot.x = Float.parseFloat(sc.get(tp++));
                po.s0.rot.y = Float.parseFloat(sc.get(tp++));
                po.s0.rot.z = Float.parseFloat(sc.get(tp++));
                len = normalize(po.s0.rot);
                if (len != 0) {
                    po.s0.rot.w = len*NuMath.DEGREE2RAD;
                    VECQuat.rotaxis2quat(po.s0.rot,po.s0.rot);
                } else {
                    po.s0.rot = new VEC(0,0,0);
                    po.s0.rot.w = 1;
                }
            } else if (sc.get(tp).equals("rotvel")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+3>nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.s0.rotvel.x = NuMath.DEGREE2RAD*Float.parseFloat(sc.get(tp++));
                po.s0.rotvel.y = NuMath.DEGREE2RAD*Float.parseFloat(sc.get(tp++));
                po.s0.rotvel.z = NuMath.DEGREE2RAD*Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp).equals("vel")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+3 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.s0.vel.x = Float.parseFloat(sc.get(tp++));
                po.s0.vel.y = Float.parseFloat(sc.get(tp++));
                po.s0.vel.z = Float.parseFloat(sc.get(tp++));
            } else if (sc.get(tp).equals("scale")) {
                if (obj == null)
                    Utils.alert("use 'object' first");
                tp++;
                if (tp+3 > nsc)
                    Utils.alert("end o file in '" + name + "'");
                po.scale.x = Float.parseFloat(sc.get(tp++));
                po.scale.y = Float.parseFloat(sc.get(tp++));
                po.scale.z = Float.parseFloat(sc.get(tp++));
            } else
                Utils.alert("unknown obj script keyword '" + sc.get(tp) + "'");
        }
        ++nphyobjects;
        lastcurphyobjectnum = -1;
        curphyobjectnum = 0;
// prepare objects
        for (i=0;i<nphyobjects;i++) {

            FACE[] f;
            VEC[] v;
            int nf,nv,ne;
// build 8 point bbox
            po=phyobjects[i];
            VEC bmin = new VEC(po.t.mod.boxmin);
            VEC bmax = new VEC(po.t.mod.boxmax);
            for (j=0;j<NCORNERS;j++) {
                if ((j&1) != 0)
                    po.pnts[j].x = bmax.x;
                else
                    po.pnts[j].x = bmin.x;
                if ((j&2) != 0)
                    po.pnts[j].y = bmax.y;
                else
                    po.pnts[j].y = bmin.y;
                if ((j&4) != 0)
                    po.pnts[j].z = bmax.z;
                else
                    po.pnts[j].z = bmin.z;
            }
// calc moivec
            VEC s = new VEC(po.scale);
            if (po.mass != 0) {
                s.x = moisqs[po.kind].x*s.x*s.x;
                s.y = moisqs[po.kind].y*s.y*s.y;
                s.z = moisqs[po.kind].z*s.z*s.z;
                po.moivec.x = po.mass*(s.y + s.z);
                po.moivec.y = po.mass*(s.x + s.z);
                po.moivec.z = po.mass*(s.x + s.y);
                Log.i(TAG,"object '" + po.t.name + "' moivec " + po.moivec.x + " " + po.moivec.y + " " + po.moivec.z);
// calc angmom
                domoi(po,po.s0.rot,po.s0.rotvel,po.s0.angmomentum);
// calc mom
                po.s0.momentum.x = po.mass*po.s0.vel.x;
                po.s0.momentum.y = po.mass*po.s0.vel.y;
                po.s0.momentum.z = po.mass*po.s0.vel.z;
            } else {
                po.norot = po.notrans = true;
            }
            if (po.norot)
                po.moivec = new VEC();
            if (po.notrans)
                po.mass = 0;
// init collisions
            ModelBase mod = po.t.mod;
            po.nwfaces = mod.nface;
            po.nwpnts = mod.nverts;
            if (po.nwfaces > MAXF)
                Utils.alert("too many faces '" + po.nwfaces + "/" + MAXF + "'" + po.t.name);
            //po.lfaces =(FACE *)memalloc(sizeof(FACE)*po->nwfaces);
            //po.lfaces = new FACE[po.nwfaces];
            po.lfaces = FACE.makeFACEArray(mod.faces);
            //po.lpnts =(VEC *)memalloc(sizeof(VEC)*po->t->mod->verts.size());
            //po.lpnts = new VEC[po.nwpnts];
            po.lpnts = VEC.makeVECArray(mod.verts);
            // remove duplicate verts, compact mesh
            po.nwpnts = 0;
            nf = po.nwfaces;
            //f=po->t->mod->faces;
            f = po.lfaces;
            v = po.lpnts;
            for (j=0;j<nf;j++) {
                for (m=0;m<3;m++) {
                    n = f[j].vertidx[m];
                    for (k=0;k<po.nwpnts;k++)  {
                        if (v[n].equals(po.lpnts[k]))
                            break;
                    }
                    po.lfaces[j].vertidx[m] = (short)k;
                    if (k == po.nwpnts) {
                        po.lpnts[po.nwpnts] = v[n];
                        ++po.nwpnts;
                    }
                }
            }
            if (po.nwpnts > MAXV)
                Utils.alert("too many verts '" + po.nwpnts + "/" + MAXV + "'" + po.t.name);
            //po->contacts=(contact*)memzalloc(sizeof(struct contact)*nphyobjects); // not used
            //po->lpnts=(VEC *)memrealloc(po->lpnts,sizeof(VEC)*po->nwpnts);
            //po.lpnts = Arrays.copyOf(po.lpnts,po.nwpnts); // don't need
            //po->wpnts=(VEC *)memalloc(sizeof(VEC)*po->nwpnts);
            po.wpnts = new VEC[po.nwpnts];
            for (j=0;j<po.nwpnts;++j)
                po.wpnts[j] = new VEC();
// build neighbors
            f = po.lfaces;
            nf = po.nwfaces;
            nv = po.nwpnts;
            v = po.lpnts;
            po.nbs = new nb[nv];//ArrayList<>();//nb();)memzalloc(sizeof(struct nb)*nv);
            for (j=0;j<nv;++j)
                po.nbs[j] = new nb();
            for (j=0;j<nf;j++) {
                for (k=0;k<3;k++) {
                    int vs,ve;
                    vs = f[j].vertidx[k];
                    ve = f[j].vertidx[nextvert[k]];
                    nb lmb = po.nbs[vs];
                    for (m=0;m<lmb.nbs.size();++m)
                        if (lmb.nbs.get(m) == ve)
                            break;
                    if (m == lmb.nbs.size()) {
                        /*
                        if (lmb->nnb>=lmb->nnballoced) {
                            lmb->nnballoced+=10;
                            lmb->nbs=(int*)memrealloc(lmb->nbs,sizeof(int)*lmb->nnballoced);
                        }
                        lmb->nnb++;
                        lmb->nbs[m]=ve; */
                        lmb.nbs.add(ve);
                    }
                    lmb = po.nbs[ve];
                    for (m=0;m<lmb.nbs.size();++m)
                        if (lmb.nbs.get(m) == vs)
                            break;
                    if (m == lmb.nbs.size()) {
                        /*
                        if (lmb->nnb>=lmb->nnballoced) {
                            lmb->nnballoced+=10;
                            lmb->nbs=(int*)memrealloc(lmb->nbs,sizeof(int)*lmb->nnballoced);
                        }
                        ++lmb.nnb;
                        lmb->nbs[m]=vs; */
                        lmb.nbs.add(vs);
                    }
                }
            }

// build face neighbors
            f = po.lfaces;
            nf = po.nwfaces;
            nv = po.nwpnts;
            v = po.lpnts;
            po.nbfs = new nbf[nv];//(struct nbf *)memzalloc(sizeof(struct nbf)*nv);
            for (j=0;j<nv;++j)
                po.nbfs[j] = new nbf();
            for (j=0;j<nf;j++) {
                for (k=0;k<3;k++) {
                    int vs;
                    vs = f[j].vertidx[k];
                    nbf lmb = po.nbfs[vs];
                    /*
                    if (lmb->nnbf>=lmb->nnbfalloced) {
                        lmb->nnbfalloced+=10;
                        lmb->nbfs=(int*)memrealloc(lmb->nbfs,sizeof(int)*lmb->nnbfalloced);
                    }
                    lmb->nbfs[lmb->nnbf]=j;
                    lmb->nnbf++; */
                    lmb.nbfs.add(j);
                }
            }

// show faces and neighbors
            for (j=0;j<nf;j++) {
                int v0,v1,v2;
                v0=f[j].vertidx[0];
                v1=f[j].vertidx[1];
                v2=f[j].vertidx[2];
                Log.d(TAG,"face " + j + ", (" + v0 + " " + v1 + " " + v2 + ")");
            }
            ne = 0;
            StringBuilder sb = new StringBuilder();
            for (j=0;j<nv;j++) {
                ne += po.nbs[j].nbs.size();
                sb.setLength(0);
                sb.append("vert " + j + ", " + po.nbs[j].nbs.size() + " neighbors  ");
                for (k=0;k<po.nbs[j].nbs.size();++k)
                    sb.append(po.nbs[j].nbs.get(k) + "  ");
                Log.d(TAG,sb.toString());
            }
            if ((ne&1) == 0)
                ne >>= 1;

            Log.d(TAG,"f " + nf + " + v " + nv + " = 2 + e " + ne);
            for (j=0;j<nv;j++) {
                sb.setLength(0);
                sb.append("vert " + j + ", " + po.nbfs[j].nbfs.size() + "  faces  ");
                for (k=0;k<po.nbfs[j].nbfs.size();++k)
                    sb.append(po.nbfs[j].nbfs.get(k) + "  ");
                Log.d(TAG,sb.toString());
            }
        }
        init3dbboxes(nphyobjects);
    }

    /*
     // don't need, JAVA will GC
     void freephysicsobjects() {
     }
     */

    int getcolpoint(VEC[] pnts,int npnts,phyobject p,VEC loc,VEC norm) {
        int i;
        float dsum = 0,d;
        VEC p2o = new VEC();
        //VEC v[MAXBOX2BOX/2];
        //VEC vc[MAXBOX2BOX/2];
        //float da[MAXBOX2BOX/2];
        VEC crs = new VEC();
        loc.clear(); //*loc=zerov;
        for (i=0;i<npnts;i+=2) {
            d = dist3dsq(pnts[i],pnts[i+1]);
            da[i>>1] = d;
            loc.x += pnts[i].x*d;
            loc.y += pnts[i].y*d;
            loc.z += pnts[i].z*d;
            loc.x += pnts[i+1].x*d;
            loc.y += pnts[i+1].y*d;
            loc.z += pnts[i+1].z*d;
            dsum += 2*d;
        }
        if (dsum == 0) {
            Log.e(TAG, "dsum == 0");
            return 0; // why does this happen...
        }
        loc.x /= dsum;
        loc.y /= dsum;
        loc.z /= dsum;
        npnts /= 2;
        for (i=0;i<npnts;i++) {
            v[i].x = pnts[i*2+1].x - pnts[i*2].x;
            v[i].y = pnts[i*2+1].y - pnts[i*2].y;
            v[i].z = pnts[i*2+1].z - pnts[i*2].z;
            vc[i].x = pnts[i*2].x - loc.x;
            vc[i].y = pnts[i*2].y - loc.y;
            vc[i].z = pnts[i*2].z - loc.z;
        }
        norm.clear();//*norm=zerov;
        for (i=0;i<npnts;i++) {
            cross3d(vc[i],v[i],crs);
            norm.x += crs.x*da[i];
            norm.y += crs.y*da[i];
            norm.z += crs.z*da[i];
        }
        d = VECNuMath.normalize(norm,norm);
        if (d < EPSILON) {
//		Log.i(TAG"getcolpoint ret 0\n");
            return 0;
        }
        p2o.x = loc.x - p.st.pos.x;
        p2o.y = loc.y - p.st.pos.y;
        p2o.z = loc.z - p.st.pos.z;
        if (dot3d(p2o,norm) > 0) {
            norm.x = -norm.x;
            norm.y = -norm.y;
            norm.z = -norm.z;
        }
        return 1;
    }

    // generate forces st -> st
    void calcimpulseo2o(phyobject p0,phyobject p1,VEC loc,VEC norm) {
        float k;
        VEC vang0 = new VEC(),vang1 = new VEC();
        VEC vrel = new VEC(),vrel0 = new VEC(),vrel1 = new VEC();	// obj 1 rel to obj 0 (obj0 space), norm is toward obj 0 away from obj 1
        VEC rc0 = new VEC(),rc1 = new VEC();
        VEC force10 = new VEC(),torque = new VEC();
        float top,bot=0;
        VEC rcn0 = new VEC(),rcn1 = new VEC(),rcnr0 = new VEC(),rcnr1 = new VEC();
        rc0.x = loc.x - p0.st.pos.x;
        rc0.y = loc.y - p0.st.pos.y;
        rc0.z = loc.z - p0.st.pos.z;
        cross3d(p0.st.rotvel,rc0,vang0);
        vrel0.x = p0.st.vel.x + vang0.x;
        vrel0.y = p0.st.vel.y + vang0.y;
        vrel0.z = p0.st.vel.z + vang0.z;
        rc1.x = loc.x - p1.st.pos.x;
        rc1.y = loc.y - p1.st.pos.y;
        rc1.z = loc.z - p1.st.pos.z;
        cross3d(p1.st.rotvel,rc1,vang1);
        vrel1.x = p1.st.vel.x + vang1.x;
        vrel1.y = p1.st.vel.y + vang1.y;
        vrel1.z = p1.st.vel.z + vang1.z;
        vrel.x = vrel1.x - vrel0.x;
        vrel.y = vrel1.y - vrel0.y;
        vrel.z = vrel1.z - vrel0.z;
        top = 2.0f*dot3d(norm,vrel);
        if (top <= 0)
            return; // moving away
        if (!p0.notrans)
            bot += 1.0f/p0.mass;
        if (!p1.notrans)
            bot += 1.0f/p1.mass;
        if (!p0.norot) {
            cross3d(rc0,norm,rcn0);
            doinvmoi(p0,p0.st.rot,rcn0,rcnr0);
            bot += dot3d(rcn0,rcnr0);
        }
        if (!p1.norot) {
            cross3d(rc1,norm,rcn1);
            doinvmoi(p1,p1.st.rot,rcn1,rcnr1);
            bot += dot3d(rcn1,rcnr1);
        }
        if (bot < EPSILON)
            return;
        k = top/bot;
        k *= .5f + .5f*p0.elast*p1.elast*globalelast;
        force10.x = k*norm.x;
        force10.y = k*norm.y;
        force10.z = k*norm.z;
        if (!p0.notrans) {
            p0.st.momentum.x += force10.x;
            p0.st.momentum.y += force10.y;
            p0.st.momentum.z += force10.z;
            p0.st.vel.x = p0.st.momentum.x/p0.mass;
            p0.st.vel.y = p0.st.momentum.y/p0.mass;
            p0.st.vel.z = p0.st.momentum.z/p0.mass;
        }
        if (!p1.notrans) {
            p1.st.momentum.x -= force10.x;
            p1.st.momentum.y -= force10.y;
            p1.st.momentum.z -= force10.z;
            p1.st.vel.x = p1.st.momentum.x/p1.mass;
            p1.st.vel.y = p1.st.momentum.y/p1.mass;
            p1.st.vel.z = p1.st.momentum.z/p1.mass;
        }
        if (!p0.norot) {
            cross3d(rc0,force10,torque);
            p0.st.angmomentum.x += torque.x;
            p0.st.angmomentum.y += torque.y;
            p0.st.angmomentum.z += torque.z;
            doinvmoi(p0,p0.st.rot,p0.st.angmomentum,p0.st.rotvel);
        }
        if (!p1.norot) {
            cross3d(rc1,force10,torque);
            p1.st.angmomentum.x -= torque.x;
            p1.st.angmomentum.y -= torque.y;
            p1.st.angmomentum.z -= torque.z;
            doinvmoi(p1,p1.st.rot,p1.st.angmomentum,p1.st.rotvel);
        }
        boolean nvx0 = Float.isNaN(p0.st.vel.x);
        boolean nvy0 = Float.isNaN(p0.st.vel.y);
        boolean nvz0 = Float.isNaN(p0.st.vel.z);
        boolean nvx1 = Float.isNaN(p1.st.vel.x);
        boolean nvy1 = Float.isNaN(p1.st.vel.y);
        boolean nvz1 = Float.isNaN(p1.st.vel.z);
        boolean nrvx0 = Float.isNaN(p0.st.rotvel.x);
        boolean nrvy0 = Float.isNaN(p0.st.rotvel.y);
        boolean nrvz0 = Float.isNaN(p0.st.rotvel.z);
        boolean nrvw0 = Float.isNaN(p0.st.rotvel.w);
        boolean nrvx1 = Float.isNaN(p1.st.rotvel.x);
        boolean nrvy1 = Float.isNaN(p1.st.rotvel.y);
        boolean nrvz1 = Float.isNaN(p1.st.rotvel.z);
        boolean nrvw1 = Float.isNaN(p1.st.rotvel.w);
        if (nvx0 || nvy0 || nvz0 || nvx1 || nvy1 || nvz1 || nrvx0 || nrvy0 || nrvz0 || nrvw0 || nrvx1 || nrvy1 || nrvz1 || nrvw1)
            Log.e(TAG,"NaN in vel or retvel");
    }

    void calcimpulseo(phyobject p0,VEC loc,VEC norm) {
        float k;
        VEC vang0 = new VEC();
        VEC vrel = new VEC();	// obj 1 rel to obj 0 (obj0 space), norm is toward obj 0 away from obj 1
        VEC rc0 = new VEC();
        VEC force10 = new VEC(),torque = new VEC();
        float top,bot = 0;
        VEC rcn0 = new VEC(),rcnr0 = new VEC();
        rc0.x = loc.x - p0.st.pos.x;
        rc0.y = loc.y - p0.st.pos.y;
        rc0.z = loc.z - p0.st.pos.z;
        VECNuMath.cross3d(p0.st.rotvel,rc0,vang0);
        vrel.x = -(p0.st.vel.x+vang0.x);
        vrel.y = -(p0.st.vel.y+vang0.y);
        vrel.z = -(p0.st.vel.z+vang0.z);
        top = 2.0f*dot3d(norm,vrel);
        if (top <= 0)
            return; // moving away
        if (!p0.notrans)
            bot += 1.0f/p0.mass;
        if (!p0.norot) {
            VECNuMath.cross3d(rc0,norm,rcn0);
            doinvmoi(p0,p0.st.rot,rcn0,rcnr0);
            bot += dot3d(rcn0,rcnr0);
        }
        if (bot < EPSILON)
            return;
        k = top/bot;
        k *= .5f + .5f*p0.elast*globalelast;
        force10.x = k*norm.x;
        force10.y = k*norm.y;
        force10.z = k*norm.z;
        if (!p0.notrans) {
            p0.st.momentum.x += force10.x;
            p0.st.momentum.y += force10.y;
            p0.st.momentum.z += force10.z;
            p0.st.vel.x = p0.st.momentum.x/p0.mass;
            p0.st.vel.y = p0.st.momentum.y/p0.mass;
            p0.st.vel.z = p0.st.momentum.z/p0.mass;
        }
        if (!p0.norot) {
            VECNuMath.cross3d(rc0,force10,torque);
            p0.st.angmomentum.x += torque.x;
            p0.st.angmomentum.y += torque.y;
            p0.st.angmomentum.z += torque.z;
            doinvmoi(p0,p0.st.rot,p0.st.angmomentum,p0.st.rotvel);
            //Log.e(TAG,"calcimpulseo with loc (" + loc.x + " " + loc.y + " " + loc.z + "), norm (" + norm.x + " " + norm.y + " " + norm.z +")  force = (" + force10.x + " " + force10.y + " " + force10.z + ") + torque = (" + torque.x + " " + torque.y + " " + torque.z + ")");
            //logger("calcimpulseo with loc (%f %f %f), norm (%f %f %f)    ",loc->x,loc->y,loc->z,norm->x,norm->y,norm->z);
            //logger("force = (%f %f %f) torque = (%f %f %f)\n",force10.x,force10.y,force10.z,torque.x,torque.y,torque.z);
        }
    }

    // calc the force of p1 on p0
    float collideobjects(phyobject p0,phyobject p1,/*struct contact *ct,*/boolean imp) {
        //if (framenum == 14)
        //    Log.e(TAG,"framenum == 14");
        int cp;
        boolean didcoll = false;
        VEC loc = new VEC(),norm = new VEC();
        //VEC resv[MAXBOX2BOX];
        if (p0.kind != OBJ_BOX.ordinal() || p1.kind != OBJ_BOX.ordinal())
            return 1;
        cp = CollUtil.box2box(p0.rpnts,p1.rpnts,resv);
        if (cp > 0) {
            if (getcolpoint(resv,cp,p0,loc,norm) != 0) {
                didcoll = true;
                if (imp)
                    calcimpulseo2o(p0,p1,loc,norm); // generate forces
            }
        }
        if (didcoll)
            return 0;
        return 1;
    }

    VEC wallnorms[] = {
            new VEC( 0, 1, 0), // bot
            new VEC( 1, 0, 0), // left
            new VEC(-1, 0, 0), // right
            new VEC( 0, 0,-1), // back
            new VEC( 0, 0, 1), // front
            new VEC( 0,-1, 0), // top
    };
    final int NWALLS = wallnorms.length;
    VEC walllocs[] = {
            new VEC(  0,  0,  0),
            new VEC(-50,  0,  0),
            new VEC( 50,  0,  0),
            new VEC(  0,  0, 30),
            new VEC(  0,  0,-30),
            new VEC(  0,100,  0),
    };

    // calc the force of p on ground
    float collideground(phyobject p,boolean imp) {
        boolean doret = false;
        if (doret)
            return 1;
        float minsep = 1e20f,sep;
        int k,i;
        VEC loc = new VEC();//,norm;
//	VEC resv[MAXBOX2PLANE];
        float bmin,d,planed;
        VEC[] b = p.rpnts;
// collide with walls
        for (k=0;k<NWALLS;k++) {
// check bbox with plane
            bmin = dot3d(b[0],wallnorms[k]);
            for (i=1;i<NCORNERS;i++) {
                d = dot3d(b[i],wallnorms[k]);
                if (d < bmin)
                    bmin = d;
            }
            // TODO: this could be done just once..
            planed = dot3d(walllocs[k],wallnorms[k]);
            if (bmin >= planed)
                continue;

            sep = PrimColl.meshplane(p,walllocs[k],wallnorms[k],loc);
            if (sep <= 0) {
                if (imp)
                    calcimpulseo(p,loc,wallnorms[k]); // generate forces
            }
            if (sep < minsep)
                minsep = sep;
        }
        return minsep;
    }

    boolean collidephysicsobjects(boolean doimpulse) {
        /*boolean doit = false;
        if (!doit)
            return false;*/
        int i,j;
        boolean didcoll = false;
        VEC b0[],b0max,b0min;
        for (i=0;i<nphyobjects;i++) {
            phyobject p = phyobjects[i];
// get bbox
            p.haswf = false;
            for (j=0;j<NCORNERS;j++) {
                p.rpnts[j].x = p.scale.x*p.pnts[j].x;
                p.rpnts[j].y = p.scale.y*p.pnts[j].y;
                p.rpnts[j].z = p.scale.z*p.pnts[j].z;
            }
            quatrots(p.st.rot,p.rpnts,p.rpnts,NCORNERS);
            for (j=0;j<NCORNERS;j++) {
                p.rpnts[j].x += p.st.pos.x;
                p.rpnts[j].y += p.st.pos.y;
                p.rpnts[j].z += p.st.pos.z;
            }
            b0 = p.rpnts;
            //b0max = b0min = b0[0];
            b0max = new VEC(b0[0]);
            b0min = new VEC(b0[0]);
            for (j=1;j<NCORNERS;j++) {
                if (b0[j].x > b0max.x)
                    b0max.x = b0[j].x;
                if (b0[j].y > b0max.y)
                    b0max.y = b0[j].y;
                if (b0[j].z > b0max.z)
                    b0max.z = b0[j].z;
                if (b0[j].x < b0min.x)
                    b0min.x = b0[j].x;
                if (b0[j].y < b0min.y)
                    b0min.y = b0[j].y;
                if (b0[j].z < b0min.z)
                    b0min.z = b0[j].z;
            }
// set bbox
//		setVEC(&bboxs3d[i].b,-10,-10,-10);
//		setVEC(&bboxs3d[i].e,10,10,10);
            bboxs3d[i].b.copy(b0min);
            bboxs3d[i].e.copy(b0max);
        }
        collide3dboxes();
// collide with each other

        for (i=0;i<ncolpairs;i++) {
            //if (i == 12 && framenum == 14)
            //    Log.e(TAG,"i == 12 && framenum == 14");
            phyobject p0,p1;
            p0 = phyobjects[colpairs3d[i].a];
            p1 = phyobjects[colpairs3d[i].b];
            if (collideobjects(p0,p1/*p0.contacts[colpairs3d[i].b],*/,doimpulse) <= 0)
                didcoll = true;
        }
// collide on the ground
        for (i=0;i<nphyobjects;i++) {
            sep = collideground(phyobjects[i],doimpulse);
            if (sep <= 0) {
                didcoll = true;
                //Log.e(TAG,"collision with ground, sep = " + sep);
            } else {
                //Log.e(TAG,"no collision with ground");

            }
        }
        return didcoll;
    }

    // convert vels to delta distances
    void movephysicsobjects(float timestep) {
        int i;
        VEC q = new VEC();
        for (i=0;i<nphyobjects;i++) {
            phyobject po = phyobjects[i];
            po.st.momentum.copy(po.s0.momentum);
            po.st.pos.copy(po.s0.pos);
            po.st.vel.copy(po.s0.vel);
            if (po.notrans)
                continue;
            po.st.pos.x = po.s0.pos.x +
                    timestep*po.s0.momentum.x/po.mass;
            po.st.pos.y = po.s0.pos.y +
                    timestep*po.s0.momentum.y/po.mass;
            if (!oldgravity) {
                po.st.pos.y -= .5f*littlegee*timestep*timestep;
                po.st.momentum.y -= littlegee*po.mass*timestep;
            }
            po.st.pos.z = po.s0.pos.z+
                    timestep*po.s0.momentum.z/po.mass;
            po.st.vel.x = po.st.momentum.x/po.mass;
            po.st.vel.y = po.st.momentum.y/po.mass;
            po.st.vel.z = po.st.momentum.z/po.mass;
            if (oldgravity) {
                po.st.vel.y -= timestep*littlegee;
                po.st.momentum.y = po.mass*po.st.vel.y;
            }
        }
        for (i=0;i<nphyobjects;i++) {
            phyobject po = phyobjects[i];
            // copy by value not by reference
            po.st.rot.copy(po.s0.rot);
            po.st.rotvel.copy(po.s0.rotvel);
            po.st.angmomentum.copy(po.s0.angmomentum);
            if (po.norot)
                continue;
            if (midpointmethod) { // actually midpoint seems adequate
                VEC sk0 = new VEC();
                VEC w1 = new VEC(),k1 = new VEC();
                sk0.w = normalize(po.s0.rotvel,sk0);
                if (sk0.w != 0) {
                    sk0.w *= timestep*.5f;
                    rotaxis2quat(sk0,sk0);
                    quattimes(sk0,po.s0.rot,po.st.rot); // rt = r0 + 1/2 r0
                    doinvmoi(phyobjects[i],po.st.rot,po.s0.angmomentum,w1); //w(1/2)
                    k1.w = normalize(w1,k1);
                    if (k1.w != 0) {
                        k1.w *= timestep;
                        rotaxis2quat(k1,k1);
                        quattimes(k1,po.s0.rot,po.st.rot); // rt = r0 + w(1/2)
                        quatnormalize(po.st.rot,po.st.rot);
                        doinvmoi(phyobjects[i],po.st.rot,po.st.angmomentum,po.st.rotvel);
                    }
                }
            } else { // euler method
                q.w = normalize(po.s0.rotvel,q);
                q.w*=timestep;
                if (q.w != 0) {
                    rotaxis2quat(q,q);
                    quattimes(q,po.s0.rot,po.st.rot); // world rel, rt = r0 + w0
                    quatnormalize(po.st.rot,po.st.rot);
                }
                doinvmoi(phyobjects[i],po.st.rot,po.st.angmomentum,po.st.rotvel);
            }
        }
    }

    // move data from physics struct to tree struct
    void drawprepphysicsobjects() {
        int k;
        for (k=0;k<nphyobjects;k++) {
            phyobject po = phyobjects[k];
            po.t.trans = new float[3];
            VEC.copy3(po.s0.pos,po.t.trans);
            po.t.qrot = new float[4];
            VEC.copy4(po.s0.rot,po.t.qrot);
            po.t.scale = new float[3];
            VEC.copy3(po.scale,po.t.scale);

            if (showVector) {
                float scaleVec = .0055f;
                float scaleRotVel = 10.0f;
                VEC p1 = new VEC();
                p1.x = po.st.pos.x + po.st.angmomentum.x*scaleVec;
                p1.y = po.st.pos.y + po.st.angmomentum.y*scaleVec;
                p1.z = po.st.pos.z + po.st.angmomentum.z*scaleVec;
                ho.addvector(roottree, po.st.pos, p1, Colors.F32CYAN); // ang mom
                p1.x = po.st.pos.x + po.st.rotvel.x*scaleRotVel;
                p1.y = po.st.pos.y + po.st.rotvel.y*scaleRotVel;
                p1.z = po.st.pos.z + po.st.rotvel.z*scaleRotVel;
                ho.addvector(roottree, po.st.pos, p1, Colors.F32LIGHTRED); // rot vel
            }
/*            po.t.trans.copy(po.st.pos; // world rel
            po.t.scale.copy{po.scale; // world rel
            po.t.rot.copy = po.st.rot; */
            //		addnull(&po.st.pos,&po.st.rot);
        }
    }

    /*
    // get data in and out of debprint
    void	getdebprintphysicsobjects() {
// get data from debprint
        if (nphyobjects<=0)
            return;
        quatnormalize(&curphyobject.s0.rot,&curphyobject.s0.rot);
        if (!curphyobject.notrans) {
            curphyobject.s0.vel.x=curphyobject.s0.momentum.x/curphyobject.mass;
            curphyobject.s0.vel.y=curphyobject.s0.momentum.y/curphyobject.mass;
            curphyobject.s0.vel.z=curphyobject.s0.momentum.z/curphyobject.mass;
        }
        if (!curphyobject.norot)
            doinvmoi(&curphyobject,
        &curphyobject.s0.rot,&curphyobject.s0.angmomentum,&curphyobject.s0.rotvel);
        curphyobjectnum=range(0,curphyobjectnum,nphyobjects-1);
        if (lastcurphyobjectnum==curphyobjectnum)
            phyobjects[curphyobjectnum]=curphyobject;
    }

    void	setdebprintphysicsobjects()
    {
// send data to debprint;
        if (nphyobjects<=0)
            return;
        lastcurphyobjectnum=curphyobjectnum;
        curphyobject=phyobjects[curphyobjectnum];
    }
*/

    void copynew2old() {
        int i;
        for (i=0;i<nphyobjects;i++) {
            //phyobjects[i].s0 = phyobjects[i].st; // world rel, just copy object reference, WRONG
            phyobjects[i].s0.copy(phyobjects[i].st); // deep copy, RIGHT
        }
    }

    void calcenergynew() {
        int i;
        VEC t = new VEC();
        totenergy = totrotenergy = tottransenergy = totpotenergy = 0;
        totangmomentum.clear();
        totmomentum.clear();
        totangorgmomentum.clear();
        totangcmmomentum.clear();
        for (i=0;i<nphyobjects;i++) {
            phyobject po = phyobjects[i];
            if (!po.notrans) {
                po.transenergy = .5f * length2(po.st.vel) * po.mass;
                po.potenergy = po.mass * littlegee * po.st.pos.y;
            }
            if (!po.norot) {
                calcangenergy(phyobjects[i]);
                cross3d(po.st.pos,po.st.vel,t);
                t.x *= po.mass;
                t.y *= po.mass;
                t.z *= po.mass;
                totangorgmomentum.x += t.x;
                totangorgmomentum.y += t.y;
                totangorgmomentum.z += t.z;
                totangmomentum.x += po.st.angmomentum.x+t.x;
                totangmomentum.y += po.st.angmomentum.y+t.y;
                totangmomentum.z += po.st.angmomentum.z+t.z;
            }
            tottransenergy += po.transenergy;
            totpotenergy += po.potenergy;
            totrotenergy += po.rotenergy;
            totenergy += po.transenergy + po.potenergy + po.rotenergy;
            totmomentum.x += po.st.momentum.x;
            totmomentum.y += po.st.momentum.y;
            totmomentum.z += po.st.momentum.z;
            totangcmmomentum.x += po.st.angmomentum.x;
            totangcmmomentum.y += po.st.angmomentum.y;
            totangcmmomentum.z += po.st.angmomentum.z;
        }
    }

    void procphysicsobjects(float timestep,int iterations) {
        boolean runSim = true;
        if (runSim) {
            int i;
            float thresh = 0;
            float t0, t1, timeleft;
            //getdebprintphysicsobjects();
            if (iterations <= 0) { // analyse 1 timestep (timestep2)
                movephysicsobjects(timestep2); // s0 -> st
                calcenergynew(); // st
            } else {
                timestep /= iterations;
                if (bisect > 0)
                    thresh = timestep / (1 << bisect);
                for (i = 0; i < iterations; i++) {
                    if (bisect > 0) { // finer collision time checking, objects don't touch (maybe alittle)
                        timeleft = timestep;
                        while (timeleft > 0) {
                            movephysicsobjects(0);
                            if (collidephysicsobjects(false)) { // trouble, objects touch at t=0
                                if (timeleft < thresh) {
                                    movephysicsobjects(timeleft); // s0 -> st, just doit the oldway
                                    collidephysicsobjects(true); // st -> st
                                    calcenergynew(); // st
                                    copynew2old(); // st -> s0
                                    timeleft = 0;
                                } else {
                                    movephysicsobjects(thresh); // s0 -> st, just doit the oldway
                                    collidephysicsobjects(true); // st -> st
                                    calcenergynew(); // st
                                    copynew2old(); // st -> s0
                                    timeleft -= thresh;
                                }
                            } else {
                                movephysicsobjects(timeleft);
                                if (!collidephysicsobjects(false)) { // no collisions during whole timestep
                                    movephysicsobjects(timeleft); // s0 -> st
                                    calcenergynew(); // st
                                    copynew2old(); // st -> s0
                                    timeleft = 0;
                                } else { // collision happened sometime inbetween
                                    t0 = 0;
                                    t1 = timeleft;
                                    timestep = (t0 + t1) * .5f;
                                    while (t1 - t0 > thresh) {
                                        movephysicsobjects(timestep);
                                        if (collidephysicsobjects(false))
                                            t1 = timestep;
                                        else
                                            t0 = timestep;
                                        timestep = (t0 + t1) * .5f;
                                    }
                                    movephysicsobjects(timestep); // or t1
                                    collidephysicsobjects(true); // st -> st
                                    calcenergynew(); // st
                                    copynew2old(); // st -> s0
                                    timeleft -= timestep;
                                }
                            }
                        }
                    } else { // collision with some penatration, no bisection
                        movephysicsobjects(timestep); // s0 -> st
                        collidephysicsobjects(true); // st -> st
                        calcenergynew(); // st
                        copynew2old(); // st -> s0
                    }
                    //Log.e(TAG,"frame = " + framenum + ", trans = (" + po.st.pos.x + " " + po.st.pos.y + " " + po.st.pos.z + "), rot = (" + po.st.rot.x + " " + po.st.rot.y + " " +po.st.rot.z + " " +po.st.rot.w + ")");
                    ++framenum;
                }
            }
        }
        drawprepphysicsobjects();
        //setdebprintphysicsobjects();
    }

    @Override
    public void init() {
        Log.i(TAG, "entering physics3d");
        SimpleUI.setbutsname("physics3d");
        // less,more printarea for sponge
        scenearea = SimpleUI.makeaprintarea("scene: ");
        SimpleUI.makeabut("next scene",nextScene/*morelevel*/);
        SimpleUI.makeabut("reset scene",resetScene/*morelevel*/);
        SimpleUI.makeabut("prev scene",prevScene/*lesslevel*/);
        SimpleUI.makeabut("show vectors",showVectors/*lesslevel*/);

        // main scene
        roottree = new Tree("roottree");
        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        //ViewPort.mainvp.trans = new float[] {0,9,-33};
        if (viewPos != null)
            System.arraycopy(viewPos,0,ViewPort.mainvp.trans,0,3);
        else
            ViewPort.mainvp.trans = new float[] {0,27/.5f,-100/.5f};
        if (viewRot != null)
            System.arraycopy(viewRot,0,ViewPort.mainvp.rot,0,3);
        ViewPort.mainvp.zoom = 3.2f;
        ViewPort.mainvp.near = 2.5f;
        ViewPort.mainvp.far = 1000;
        if (flyCamSpeed == 0)
            flyCamSpeed = 1.0f/2.0f;
        ViewPort.mainvp.setupViewportUI(flyCamSpeed); // create some UI under 'viewport'
        Utils.pushandsetdir("physics3d");
        footballfield = new Tree("footballfield.BWS");
        roottree.linkchild(footballfield);
// worldobjects
        initworldobjects("objects.txt");
// instance of worldobjects
        // test script
        Script s = new Script("testscript.txt");
        ArrayList<String> data = s.getData();
        s = new Script("testscriptdevice.txt");
        data = s.getData();
        s = new Script("testscriptconfig.txt");
        data = s.getData();

        // instances of worldobjects
        if (ascene.isEmpty()) {
            ArrayList<String> scenes = new Script("pickscene.txt").getData();
            numscenes = scenes.size();
            //if (numscenes!=1)
            //    Utils.alert("pick just 1 scene");
            ascene = scenes.get(curscene);
        }
        initphysicsobjects(ascene);
        scenearea.draw(ascene);
        Utils.popdir();

        ho = new HelperObj();
    }

    int slow;
    @Override
    public void proc() {
        /*VEC v = null;
        v.x = 5;*/
        // get input
        InputState ir = Input.getResult();
        // proc
        if (slow == 0) {
            ho.reset();
            procphysicsobjects(timestep, iterations);
            ho.addvector(roottree,new VEC(0,2,0),new VEC(30,2,0),Colors.F32CYAN);
            slow = 2;
        }
        --slow;
        roottree.proc(); // do animation and user proc if any
        ViewPort.mainvp.doflycam(ir);
    }

    @Override
    public void draw() {
        // setup camera
        ViewPort.mainvp.beginscene();
        // draw scene from camera
        roottree.draw();
    }

    @Override
    public void exit() {
        // reset main ViewPort to default
        SimpleUI.clearbuts("viewport"); // remove UI
        viewPos = Arrays.copyOf(ViewPort.mainvp.trans,3);
        viewRot = Arrays.copyOf(ViewPort.mainvp.rot,3);
        flyCamSpeed = ViewPort.mainvp.getFlyCamSpeed();
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG, "before roottree glFree");
        roottree.log();
        GLUtil.logrc(); // show all allocated resources
        ho.glfree();
        ho = null;
        // cleanup


// free heirarchy
        phyobject po;
        int i,j;
        //freetree(aviewport.roottree);
        freeworldobjects();
        //freephysicsobjects(); // JAVA will do automatically
        for (i=0;i<MAXWORLDOBJECTS;i++)
            objkindstr[i] = "";
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after roottree glFree");
        roottree = null;
        GLUtil.logrc(); // show all allocated resources, should be clean
        SimpleUI.clearbuts("physics3d");

    }

}
