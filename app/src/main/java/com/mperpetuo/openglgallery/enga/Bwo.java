package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

// load a bwo model file
public class Bwo {
    private static final String TAG =  "Bwo";

    //static String defaultbwoshader = "tex";
    static String defaultbwoshader = "tex";
    static String defaultbwoshadernonormals = "diffusep";

    static class Mat {
        String dtex;
        //String atex;
        UnChunker.Vec4 color = new UnChunker.Vec4(1,1,1,1); // opaque white rgba
        int vo;
        int vs;
        int fo;
        int fs;
    }

    static class OIO { // Object Info Optimized
        ArrayList<Mat> mats = new ArrayList<>();
        public UnChunker.Idx3M[] faces;
        public UnChunker.Vec3[] verts;
        public UnChunker.Vec3[] norms;
        public UnChunker.Vec2[] uvs;
        public UnChunker.Vec2[] uvs2;
    }

    // process a material chunk from .bwo file, return one material/group
    static Mat get_matgroup(UnChunker uc) {
        Mat om = new Mat();
        UnChunker.ChunkHeaderInfo chi;
        while((chi = uc.getchunkheader()) != null) {
            UnChunker.ChunkTypeEnum cte = UnChunker.tvalue[chi.ct];
            UnChunker.ChunkNameEnum cne = UnChunker.nvalue[chi.cn];
            if (cte == UnChunker.ChunkTypeEnum.KID_ENDCHUNK) {
                break;
            } else if (chi.numele > 0 && cte == UnChunker.ChunkTypeEnum.KID_I8) {
                switch(cne) {
                    case UID_NAME:
                        String name=uc.readI8v();
                        //name = "maptestnck.tga";
                        break;
                    case UID_DTEX:
                        //if (om.dtex) {
                        //	uc.skipdata();
                        //} else {
                        om.dtex=uc.readI8v();
                        //}
                        break;
                    default:
                        uc.skipdata();
                        break;
                }
            } else if (chi.numele == 0 && cte == UnChunker.ChunkTypeEnum.KID_I32) {
                switch(cne) {
                    case UID_FO:
                        om.fo = uc.readI32();
                        break;
                    case UID_FS:
                        om.fs = uc.readI32();
                        break;
                    case UID_VO:
                        om.vo = uc.readI32();
                        break;
                    case UID_VS:
                        om.vs = uc.readI32();
                        break;
                    default:
                        uc.skipdata();
                        break;
                }
            } else if (chi.numele == 0 && cte == UnChunker.ChunkTypeEnum.KID_VEC3) {
                switch(cne) { // is this used ??
                    case UID_DIFFUSE:
                        UnChunker.Vec3 color = uc.readVC3();
                        om.color.x = color.x;
                        om.color.y = color.y;
                        om.color.z = color.z;
                        om.color.w = 1;
                        break;
                    default:
                        uc.skipdata();
                        break;
                }
            } else {
                uc.skipdata();
            }
        }
        return om;
    }

    // convert oio to mesh
    private static Mesh makeAMesh(OIO oio) {
        Mesh mesh = new Mesh();
        int i;
        // verts
        if (oio.verts != null) {
            mesh.verts = new float[3*oio.verts.length];
            for (i=0;i<oio.verts.length;++i) {
                mesh.verts[i*3    ] = oio.verts[i].x;
                mesh.verts[i*3 + 1] = oio.verts[i].y;
                mesh.verts[i*3 + 2] = oio.verts[i].z;
            }
        }
        // cverts ?
        // uvs
        if (oio.uvs != null) {
            mesh.uvs = new float[2*oio.uvs.length];
            for (i=0;i<oio.uvs.length;++i) {
                mesh.uvs[i*2    ] = oio.uvs[i].x;
                mesh.uvs[i*2 + 1] = oio.uvs[i].y;
            }
        }
        // uvs2
        if (oio.uvs2 != null) {
            mesh.uvs2 = new float[2*oio.uvs2.length];
            for (i=0;i<oio.uvs2.length;++i) {
                mesh.uvs2[i*2    ] = oio.uvs2[i].x;
                mesh.uvs2[i*2 + 1] = oio.uvs2[i].y;
            }
        }
        // normals
        if (oio.norms != null) {
            mesh.norms = new float[3*oio.norms.length];
            for (i=0;i<oio.norms.length;++i) {
                mesh.norms[i*3    ] = oio.norms[i].x;
                mesh.norms[i*3 + 1] = oio.norms[i].y;
                mesh.norms[i*3 + 2] = oio.norms[i].z;
            }
        }
        // faces
        if (oio.faces != null) {
            mesh.faces = new short[3*oio.faces.length];
            for (i=0;i<oio.faces.length;++i) {
                mesh.faces[i*3 + 0] = (short)(oio.faces[i].idx[0]);
                mesh.faces[i*3 + 1] = (short)(oio.faces[i].idx[1]);
                mesh.faces[i*3 + 2] = (short)(oio.faces[i].idx[2]);
            }
        }
        return mesh;
    }

    static ModelBase loadBwoModel(String bwoname) {
        String origname = bwoname;
        //bwoname = "chunk/" + bwoname;
        //UnChunker.unchunktest(bwoname); // dump contents of bwo, optional
        UnChunker uc = new UnChunker(bwoname);
        // build alternate if .bwo doesn't exist
        if (uc.fr == null) {
            boolean showFailedBwo = false;
            ModelBase ret;
            if (showFailedBwo) {
                Utils.pushandsetdir("common");
                float nobwosize = 1;
                ret = ModelUtil.buildprismmodel("NOBWO",new float[] {nobwosize,nobwosize,nobwosize},"maptestnck.png","tex");
                Utils.popdir();
            } else {
                ret = null;
            }
            return ret;
        }
        Log.d(TAG,"loading valid BWO " + origname);
        Model2 ret = Model2.createmodel(origname);
        if (ret.refcount > 1)
            return ret;
        UnChunker.ChunkHeaderInfo chi;
        OIO oio = new OIO();

        int head = 10;
        while((chi = uc.getchunkheader()) != null) {
            UnChunker.ChunkTypeEnum cte = UnChunker.tvalue[chi.ct];
            if (cte == UnChunker.ChunkTypeEnum.KID_ENDCHUNK) {	// don't skip subchunk data
                //logger("end chunk " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + ", entering chunk\n");
                break;
            }
            UnChunker.ChunkNameEnum cne = UnChunker.nvalue[chi.cn];
            switch(cne) {
                case UID_FL:
                    //logger("processing " + chi.numele + " " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + "\n");
                    oio.faces = uc.readIDX3Mv();
                    break;
                case UID_VL:
                    //logger("processing " + chi.numele + " " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + "\n");
                    oio.verts = uc.readVC3v();
                    break;
                case UID_VN:
                    //logger("processing " + chi.numele + " " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + "\n");
                    oio.norms = uc.readVC3v();
                    break;
                // cverts ?
                case UID_TV:
                    //logger("processing " + chi.numele + " " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + "\n");
                    if (oio.uvs == null) {
                        oio.uvs = uc.readVC2v();
                    } else if (oio.uvs2 == null) {
                        oio.uvs2 = uc.readVC2v();
                    } else {
                        uc.skipdata();
                    }
                    break;
                case UID_MATERIAL:
                    if (cte == UnChunker.ChunkTypeEnum.KID_CHUNK) {
                        oio.mats.add(get_matgroup(uc));
                    }
                default:
                    //logger("DATA " + uc.getchunkname_strs(chi.cn) + " " + uc.getchunktype_strs(chi.ct) + ": SKIPPING\n");
                    uc.skipdata();
                    break;
            }
        }
        // modify the mesh depending on dolightmap
        boolean candolightmap = oio.uvs2!=null && Bws.globallmname!=null;
        //candolightmap = false;
        switch(Bws.dolightmap) {
            case NO: // no lightmap
                oio.uvs2 = null;
                break;
            case YES: // mix it all together
                break;
            case JUSTLIGHTMAP: // just show the lightmap
                if (candolightmap) { // switch over to alt uvs
                    oio.uvs = oio.uvs2;
                    oio.uvs2 = null;
                } else {
                    oio.uvs2 = null;
                }
                break;
        }
        Mesh msh = makeAMesh(oio);
        ret.setmesh(msh);
        int i;
        String shaderName = oio.norms != null ? defaultbwoshadernonormals : defaultbwoshader;
        for (i=0;i<oio.mats.size();++i) {
            Mat mat = oio.mats.get(i);
            if (mat == null)
                continue;
            if (mat.dtex == null) { // no texture, use 'flat' shader and set color to white
                ret.addmat("flat",null,mat.fs,mat.vs);
                HashMap<String,float[]> m = ret.mats.get(ret.mats.size()-1);
                m.put("color",new float[] {1,1,1,1});
                continue;
            }
            // 1 or 2 textures depending on dolightmap
            switch(Bws.dolightmap) {
                case NO:
                    ret.addmat(shaderName,mat.dtex,mat.fs,mat.vs);
                    break;
                case YES:
                    if (candolightmap) {
                        ret.addmat2t("lightmap",mat.dtex,Bws.globallmname,mat.fs,mat.vs); // mix it all together
                    } else {
                        ret.addmat(shaderName,mat.dtex,mat.fs,mat.vs);
                    }
                    break;
                case JUSTLIGHTMAP:
                    if (candolightmap) {
                        ret.addmat(shaderName,Bws.globallmname,mat.fs,mat.vs); // switch over to alt texture
                    } else {
                        ret.addmat(shaderName,mat.dtex,mat.fs,mat.vs);
                    }
                    break;
            }
        }
        // check groups
        for (i=0;i<oio.mats.size();++i) {
            Mat mat = oio.mats.get(i);
            //if (mat == null)
            //    continue;
            Model2.Group glmat = ret.grps.get(i);
            if (mat.fo != glmat.faceidx || mat.fs != glmat.nface || mat.vo != glmat.vertidx || mat.vs != glmat.nvert)
                Utils.alert("group mismatch '" + bwoname + "'");
        }
        GLUtil.checkGlError("before bwo model commit " + bwoname);
        ret.commit();
        GLUtil.checkGlError("after bwo model commit " + bwoname);
        return ret;
    }

}