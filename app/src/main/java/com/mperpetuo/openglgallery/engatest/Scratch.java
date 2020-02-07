package com.mperpetuo.openglgallery.engatest;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.mperpetuo.openglgallery.enga.GLUtil;
import com.mperpetuo.openglgallery.enga.Mesh;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.Model2;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.Quat;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// jobtest
class LinkList {
    final String TAG = "Scratch Link List";
    int val;
    LinkList next;

    void print(){
        Log.i(TAG, "print link list");
        LinkList ll = this;
        while(ll != null) {
            Log.i(TAG,"val = " + ll.val);
            ll = ll.next;
        }
    }

    LinkList reverse() {
        if (this==null || this.next==null)
        return this;
        LinkList first = this;
        LinkList rest = this.next;
        LinkList rev = rest.reverse();
        rest.next =  first;
        first.next = null;
        return rev;
    }
}

class CyclicGraph {
    final String TAG = "Scratch Cyclic Graph";
    int val;
    ArrayList<CyclicGraph> nodes = new ArrayList<>();
    public CyclicGraph(int val) {
        this.val = val;
    }

    void push(CyclicGraph cg) {
        nodes.add(cg);
    }

    void printCyclicGraphRec(HashSet<CyclicGraph> visited) {
        boolean ret = visited.contains(this);
        if (!ret) {
            visited.add(this);
            Log.i(TAG,"\tval = " + this.val);
            for (CyclicGraph vi : this.nodes) {
                Log.i(TAG,"\t\tchild val = " + vi.val);
            }
            for (CyclicGraph vi : this.nodes) {
                vi.printCyclicGraphRec(visited);
            }
        }

    }
}

public class Scratch extends State {
    final String TAG = "Scratch";

    HandlerThread handlerThread;

    Tree roottree; // top level tree
    Tree atree; // a child tree with a model
    Tree btree;
    Tree ctree;
    float[] dir = new float[3];

    private static float[] multimeshVerts = new float[] {
        -1,  1, 0,
         1,  1, 0,
        -1, -1, 0,
         1, -1, 0,
        -1, -1, 0,
         1, -1, 0,
        -1, -3, 0,
         1, -3, 0
    };

    private static float[] multimeshNorms = new float[] {
        0, 0, -1,
        0, 0, -1,
        0, 0, -1,
        0, 0, -1,
        0, 0, -1,
        0, 0, -1,
        0, 0, -1,
        0, 0, -1
    };

    private static float[] multeMeshUvs = new float[] {
        0, 0,
        1, 0,
        0, 1,
        1, 1,
        .375f, .375f,
        .625f, .375f,
        .375f, .625f,
        .625f, .625f
    };

    private static short[] multiMeshFaces = new short[] {
        0, 1, 2,
        3, 2, 1,
        4, 5, 6,
        7, 6, 5
    };

    void testLinkList() {
        LinkList llA1 = new LinkList();
        LinkList llA2 = new LinkList();
        LinkList llA3 = new LinkList();
        LinkList llA4 = new LinkList();
        llA1.val = 2;
        llA1.next = llA2;
        llA2.val = 3;
        llA2.next = llA3;
        llA3.val = 5;
        llA3.next = llA4;
        llA4.val = 7;
        //llA4.next = null;
        llA1.print();
        Log.i(TAG,"now reverse link list");
        LinkList llA1R = llA1.reverse();
        llA1R.print();
        Log.i(TAG, "Done testLinkList");

    }

    void printCyclicGraph(CyclicGraph cg) {

        Log.i(TAG,"START print cyclic graph");
        if (cg == null) {
            Log.i(TAG,"\tnull");
            Log.i(TAG,"END print cyclic graph");
            return;
        }
        HashSet<CyclicGraph> visited = new HashSet<>();
        cg.printCyclicGraphRec(visited);
        Log.i(TAG,"END print cyclic graph");

    }

    void copyCyclicGraphRec1(CyclicGraph cg,HashMap<CyclicGraph,CyclicGraph> oldNewMap) {
        if (oldNewMap.containsKey(cg))
            return;
        CyclicGraph ncg = new CyclicGraph(cg.val * 11);
        oldNewMap.put(cg,ncg);
        for (CyclicGraph vi : cg.nodes) {
            copyCyclicGraphRec1(vi,oldNewMap);
        }
    }

    void copyCyclicGraphPass2(HashMap<CyclicGraph,CyclicGraph> oldNewMap) {
        for (CyclicGraph f : oldNewMap.keySet()) {
            CyclicGraph s = oldNewMap.get(f);
            for (CyclicGraph j : f.nodes) {
                CyclicGraph nj = oldNewMap.get(j);
                s.nodes.add(nj);
            }
        }
    }

    CyclicGraph copyCyclicGraph(CyclicGraph cg) {
        if (cg == null)
            return null;
        HashMap<CyclicGraph,CyclicGraph> oldNewMap = new HashMap<>();
        copyCyclicGraphRec1(cg,oldNewMap);
        copyCyclicGraphPass2(oldNewMap);
        CyclicGraph ret = oldNewMap.get(cg);
        return ret;
    }

    void testCopyCyclicGraph() {
        Log.i(TAG, "testCopyCyclicGraph");
        //resetCyclicGraphPool();
        // build A graph
	/*
	== visual representation ==
		1 -> 2 -> 3 ->
		^    ^    ^   \
		\----\----\-- - 4

		== connections ==
		1 -> 2
		2 -> 3
		3 -> 4
		4 -> 1, 2, 3
	*/
        CyclicGraph a_1 = new CyclicGraph(1);
        CyclicGraph a_2 = new CyclicGraph(2);
        CyclicGraph a_3 = new CyclicGraph(3);
        CyclicGraph a_4 = new CyclicGraph(4);
        a_1.push(a_2);
        a_2.push(a_3);
        a_3.push(a_4);
        a_4.push(a_1);
        a_4.push(a_2);
        a_4.push(a_3);

        // build B graph
        CyclicGraph b_1 = new CyclicGraph(1);
        CyclicGraph b_2 = new CyclicGraph(2);
        CyclicGraph b_3 = new CyclicGraph(3);
        CyclicGraph b_4 = new CyclicGraph(4);
        CyclicGraph b_5 = new CyclicGraph(5);
        CyclicGraph b_6 = new CyclicGraph(6);
        CyclicGraph b_7 = new CyclicGraph(7);
        CyclicGraph b_8 = new CyclicGraph(8);
        b_1.push(b_2);
        b_2.push(b_6);
        b_2.push(b_3);
        b_3.push(b_2);
        b_3.push(b_4);
        b_4.push(b_3);
        b_5.push(b_6);
        b_6.push(b_8);
        b_6.push(b_7);
        b_7.push(b_8);
        b_8.push(b_5);
        b_8.push(b_7);

        CyclicGraph c_1 = new CyclicGraph(1);
        CyclicGraph c_2 = new CyclicGraph(2);
        c_1.push(c_2);
        c_2.push(c_1);

        CyclicGraph d_1 = new CyclicGraph(1);
        CyclicGraph d_2 = new CyclicGraph(2);
        d_1.push(d_2);

        CyclicGraph e_1 = new CyclicGraph(1);

        Log.i(TAG,"print cyclic graphs");

        printCyclicGraph(null);
        printCyclicGraph(a_1);

        printCyclicGraph(b_1);
        printCyclicGraph(c_1);
        printCyclicGraph(d_1);
        printCyclicGraph(e_1);

        Log.i(TAG," ");
        Log.i(TAG,"print cyclic graphs copies");
        Log.i(TAG," ");
        CyclicGraph copy_nul = copyCyclicGraph(null);
        CyclicGraph copya_1 = copyCyclicGraph(a_1);
        CyclicGraph copyb_1 = copyCyclicGraph(b_1);
        CyclicGraph copyc_1 = copyCyclicGraph(c_1);
        CyclicGraph copyd_1 = copyCyclicGraph(d_1);
        CyclicGraph copye_1 = copyCyclicGraph(e_1);
        printCyclicGraph(copy_nul);
        printCyclicGraph(copya_1);
        printCyclicGraph(copyb_1);
        printCyclicGraph(copyc_1);
        printCyclicGraph(copyd_1);
        printCyclicGraph(copye_1);
    }

    void testThreads() {
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.i("RUNNABLE","in runnable for handlerThread");
            }
        });
        //handlerThread.quit();
    }

    private class Zigzag {
        LinkedList<Iterator<Integer>> mAli = new LinkedList<>();

        Zigzag(List<List<Integer>> zll) {
            for (List<Integer> ii : zll) {
                if (ii.size() > 0) {
                    mAli.add(ii.iterator());
                }
            }
            Log.i(TAG,"loaded iterators");
        }

        boolean hasNext() {
            return mAli.size() != 0;
        }

        int next() {
            Iterator<Integer> ele = mAli.removeFirst();
            int ret = ele.next();
            if (ele.hasNext()) {
                mAli.addLast(ele);
            }
            return ret;
        }
    }

    private void testZigzag() {
        Log.i(TAG, "START testZigzag");
        List<List<Integer>> ll = new LinkedList<>();
        ll.add(new LinkedList<>(Arrays.asList(3,4,5,77,49,31,69,444)));
        ll.add(new LinkedList<>(Arrays.asList(33,44)));
        ll.add(new LinkedList<Integer>());
        ll.add(new LinkedList<>(Arrays.asList(111,222,333)));
        ll.add(new LinkedList<>(Arrays.asList(34,45,56,7778,49990,3100)));

        Zigzag zz = new Zigzag(ll);
        while(zz.hasNext()) {
            Log.i(TAG, "list next = " + zz.next());
        }

        Log.i(TAG,"Run again..");
        zz = new Zigzag(ll);
        while(zz.hasNext()) {
            Log.i(TAG, "list next = " + zz.next());
        }
        Log.i(TAG, "END testZigzag");
    }

@Override
    public void init() {
        Log.i(TAG, "entering scratch");
        testLinkList();
        testCopyCyclicGraph();
        testThreads();
        testZigzag();
        // main scene
        roottree = new Tree("root");
        // build a planexy
        float us = ModelUtil.planepatchu;
        float vs = ModelUtil.planepatchv;
        ModelUtil.planepatchu = 2;
        ModelUtil.planepatchv = 2;
        atree =  ModelUtil.buildplanexy("aplane", 1, 1, "maptestnck.png", "texc"); // name, size, texture, generic texture shader
        ModelUtil.planepatchu = us;
        ModelUtil.planepatchv = vs;
        atree.trans = new float[] {0,1,0};
        atree.scale = new float[] {.4f,1,1};
        atree.mat.put("color",new float[] {1,.5f,1,1});
        atree.mod.flags |= Model.FLAG_NOZBUFFER; // avoid Z fighting

        btree = new Tree("1st");
        btree.qrot = new float[] {0,0, NuMath.SQRT2O2,-NuMath.SQRT2O2};
        btree.scale = new float[] {1,1,1};

        Tree ptree = ModelUtil.buildplanexy("aprism",1,1,"maptestnck.png","tex");
        ptree.trans = new float[] {0,1,0};
        roottree.linkchild(ptree);


// test multi material 'Model2' from scratch

    /*
        Model multimodel = Model.createmodel("multimaterial");
        if (multimodel.refcount == 1) {
            Mesh multimesh = new Mesh();
            multimesh.verts = multimeshVerts;
            multimesh.uvs = multeMeshUvs;
            multimesh.faces = multiMeshFaces;
            multimodel.setmesh(multimesh);
            multimodel.setshader("tex");
            multimodel.settexture("Bark.png");
            //multimodel.settexture("maptestnck.png");
            multimodel.commit();
        }
    */
        Model2 multimodel = Model2.createmodel("multimaterial");
        if (multimodel.refcount == 1) {
            Mesh multimesh = new Mesh();
            multimesh.verts = multimeshVerts;
            multimesh.uvs = multeMeshUvs;
            multimesh.faces = multiMeshFaces;
            multimodel.setmesh(multimesh);
            multimodel.addmat("tex","Bark.png",2,4);
            multimodel.addmat("texc","maptestnck.png",2,4);
            multimodel.mats.get(1).put("color",new float[] {0,1,0,1});
            multimodel.commit();
        }
        ctree = new Tree("multimaterial");
        ctree.setmodel(multimodel);

        //ctree = buildplanexy("multimaterial",1,1,"Bark.png","tex");

        ctree.trans = new float[] {-1.6f,1,0};
        ctree.scale = new float[] {.5f,.5f,1};
        roottree.linkchild(ctree);




        btree.linkchild(atree); // link to and pass ownership to backgroundtree
        roottree.linkchild(btree);

        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        ViewPort.mainvp.trans = new float[] {0,0,-2};
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();

        // proc
        if (ir.touch > 0) {
            dir[0] = ir.fx;
            dir[1] = ir.fy;
            dir[2] = 0.0f;
                /*
                float len = NuMath.length(dir);
                if (len < NuMath.EPSILON)
                    len = 1.0f; */
            float len = Quat.dir2quat(btree.qrot, dir);
            btree.scale[1] = len; // size is 1 it's really 2
        }
/*
        dir[0] = input.fmx;
        dir[1] = input.fmy;
        dir[2] = 0;
        var len = vec3.length(dir);
        btree.qrot = dir2quat(dir);
        btree.scale[1] = len;
        backgroundtree.proc(); // do animation and user proc if any
        //ViewPort.mainvp.doflycam(ir); */
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
        ViewPort.mainvp = new ViewPort();

        // show current usage
        Log.i(TAG, "before backgroundtree glFree");
        roottree.log();
        GLUtil.logrc(); // show all allocated resources

        // cleanup
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after backgroundtree glFree");
        GLUtil.logrc(); // show all allocated resources, should be clean


        handlerThread.quit();
    }

}
