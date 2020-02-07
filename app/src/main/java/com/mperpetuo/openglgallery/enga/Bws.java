package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.ArrayList;

// load animated scene and the bwo models that go with it
public class Bws {
    private static final String TAG = "Bws";
    static int maxchild = 8000;

    //String specificname = "fortpointL3"; // just load this bwo
    static String specificname;

    enum LightMap {
        NO,
        YES,
        JUSTLIGHTMAP,
    }
    public static LightMap dolightmap = LightMap.YES; // 0 no, 1 yes, 2 show just lightmap

    public static String globallmname; // global light map name, used in Fort Point

    // convert sparse array to full array
    static void fillblankssamp(ArrayList<float[]> smp) {
        int i;
        int n = smp.size();
        float[] v = null;
        for (i=0;i<n;++i) {
            if (smp.get(i) != null) {
                v = smp.get(i);
            } else {
                smp.set(i,v);
            }
        }
        // add one more for index out of bounds exception
        smp.add(null);
    }

    public static void loadbws(Tree rt) {
        String bwsname = rt.name;
        //bwsname = "chunk/" + bwsname;
        // UnChunker.unchunktest(bwsname); // dump contents of bws, optional
        UnChunker uc = new UnChunker(bwsname);
        // build alternate if .bws doesn't exist
        if (uc.fr == null) {
            //ModelBase ret = null;
            Utils.pushandsetdir("common");
            Tree nobws = ModelUtil.buildprism("NOBWS",new float[] {.1f,.1f,.1f},"maptestnck.png","tex");
            Utils.popdir();
            rt.linkchild(nobws);
            return;
        }
        Log.d(TAG,"loading valid BWS " + rt.name);
        int i;
        int cc = 0;
        Tree cld;
        //logger("in load bws with tree name '" + bwsname + "'\n");
        UnChunker.ChunkHeaderInfo chi;

        class Node {
            String name;
            int id;
            int pid;
            int kind; // needs an enum
            String lmname;
            Tree t;
            float[] o2pmat4;
            float[] trans;
            float[] qrot;
            float[] scale;
            public ArrayList<float[]> possamp;
            public ArrayList<float[]> qrotsamp;
        }
        Node node = new Node();
        ArrayList<Node> nodes = new ArrayList<>();

        UnChunker.Vec3 res3;
        UnChunker.Vec4 res4;
        UnChunker.PosLin posLin;
        UnChunker.RotLin rotLin;
        int time;

        while((chi = uc.getchunkheader()) != null) {
            UnChunker.ChunkTypeEnum cte = UnChunker.tvalue[chi.ct];
            if (cc >= maxchild)
                break;
            UnChunker.ChunkNameEnum cne = UnChunker.nvalue[chi.cn];
            if (cte==UnChunker.ChunkTypeEnum.KID_CHUNK) {	// don't skip subchunk data
                if (cne==UnChunker.ChunkNameEnum.UID_OBJECT) {
                    //logger("ignoring data size of chunk " + uc.getchunkname_strs(cne) + " " + uc.getchunktype_strs(cte) + ", entering chunk\n");
                } else {
                    //logger("skipping chunks other than UID_OBJECT " + uc.getchunkname_strs(cne) + " " + uc.getchunktype_strs(cte) + ", skipping chunk\n");
                    uc.skipdata();
                }
                continue;
            }
            switch(cne) {
                case UID_NAME:
                    node = new Node();
                    //logger("processing bwo" + chi.numele + " " + uc.getchunkname_strs(cne) + " " + uc.getchunktype_strs(cte) + "\n");
                    node.name = uc.readI8v() + ".bwo";
                    break;
                case UID_USERPROP:
                    String auserprop = uc.readI8v();
                    //logger("   userprop = '" + auserprop + "'\n");
                    String[] props = auserprop.split(" ");
                    int n = props.length;
                    for (i=0;i<n;++i) {
                        if (props[i].equals("lightmap") && i<n-1 && dolightmap!=LightMap.NO) {
                            String lmname = props[i+1];
                            Log.e(TAG,"   lightmap is '" + lmname + "'\n");
                            //var relimg = geturlfrompathnameext("",lmname,"png");
                            String relimg = lmname + ".png";
                            //var img = preloadedimages[relimg];
                            //if (/*!img.err || */ dolightmap == LightMap.YES)
                                node.lmname = relimg; // don't want maptest as a lightmap, but use it when favorlightmap
                        }
                    }
                    break;
                case UID_ID:
                    node.id = uc.readI32();
                    break;
                case UID_PID:
                    node.pid = uc.readI32();
                    break;
                case UID_KIND:
                    node.kind = uc.readI32();
                    break;

                case UID_MATRIX:
                    //node.matrix = [];
                    //for (i=0;i<4;++i)
                    //	node.matrix.push(uc.readVC3());
                    //var m43 = uc.;
                    //node.kind = uc.readI32();
                    //uc.skipdata();
                    node.o2pmat4 = uc.readmat4();//mat4.create();
                    break;

                case UID_POS:
                    res3 = uc.readVC3();
                    node.trans = new float[] {res3.x,res3.y,res3.z};
                    break;
                case UID_ROT_QUAT:
                    res4 = uc.readVC4();
                    node.qrot = new float[] {res4.x,res4.y,res4.z,res4.w};
                    break;
                case UID_SCALE:
                    res3 = uc.readVC3();
                    node.scale = new float[] {res3.x,res3.y,res3.z};
                    break;

                case UID_POS_SAMP:
                    if (node.possamp == null)
                        node.possamp = new ArrayList<>();
                    posLin = uc.readPOS_LIN();
//			node.possamp.push([res.x,res.y,res.z]);
                    time = (int)posLin.time;
                    Utils.ensureSize(node.possamp,time + 1);
                    node.possamp.set(time,new float[] {posLin.x,posLin.y,posLin.z});
                    break;
                case UID_ROT_SAMP:
                    if (node.qrotsamp == null)
                        node.qrotsamp = new ArrayList<>();
                    rotLin = uc.readROT_LIN();
//			node.qrotsamp.push([res.x,res.y,res.z,res.w]);
                    time = (int)rotLin.time;
                    Utils.ensureSize(node.qrotsamp,time + 1);
                    node.qrotsamp.set(time,new float[] {rotLin.x,rotLin.y,rotLin.z,rotLin.w});
                    break;
                default:
                    uc.skipdata();
                    break;
            }
            if (cte == UnChunker.ChunkTypeEnum.KID_ENDCHUNK) {
                //logger("bws ENDCHUNK\n");
                if ((node.kind == 1 || node.kind == 2) && (specificname==null || node.name.equalsIgnoreCase(specificname + ".bwo"))) { // geom
                    if (dolightmap !=  LightMap.NO)
                        globallmname = node.lmname; // broadcast this to bwo reader
                    cld = new Tree(node.name);
                } else if (node.kind == 4) { // amblight
                    cld = new Tree("amblight");
                    cld.name = node.name;
                    cld.flags = Tree.FLAG_AMBLIGHT;
                    Lights.addlight(cld);
                } else if (node.kind == 7) { // dirlight
                    cld = new Tree("dirlight");
                    cld.name = node.name;
                    cld.flags = Tree.FLAG_DIRLIGHT;
                    Lights.addlight(cld);
                } else { // not a geom, don't attempt to load any .bwo's
                    cld = new Tree("");
                    cld.name = node.name;
                }
                if (node.name.startsWith("chk"))
                    cld.flags |= Tree.FLAG_DONTDRAW;
                globallmname = null;
                if (node.possamp!=null || node.qrotsamp!=null) { // only use the trans,rot,scale if we have samples too, else use the o2pmat4 instead
                    cld.trans = node.trans;
                    cld.qrot = node.qrot;
                    cld.scale = node.scale;
                    if (node.possamp!=null)
                        fillblankssamp(node.possamp);
                    if (node.qrotsamp!=null)
                        fillblankssamp(node.qrotsamp);
                }
                cld.possamp = node.possamp;
                cld.qrotsamp = node.qrotsamp;
                cld.o2pmat4 = node.o2pmat4;
                if (node.pid >= 0)
                    nodes.get(node.pid).t.linkchild(cld);
                else
                    rt.linkchild(cld);
                node.t = cld;
                nodes.add(node);
                ++cc;
                //uc.skipdata();
            }
        }
        //logger("done loadbws!\n");

    }

}