package com.mperpetuo.openglgallery.enga;

import android.util.Log;

// load a bwo model file
public class BwoOld {
    private static final String TAG =  "BwoOld";

    private static float[] multimeshVerts = {
            -1,  1, 0,
            1,  1, 0,
            -1, -1, 0,
            1, -1, 0,
            -1, -1, 0,
            1, -1, 0,
            -1, -3, 0,
            1, -3, 0
    };

    private static float[] multimeshNorms = {
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1
    };

    private static float[] multeMeshUvs = {
            0, 0,
            1, 0,
            0, 1,
            1, 1,
            .375f, .375f,
            .625f, .375f,
            .375f, .625f,
            .625f, .625f
    };

    private static short[] multiMeshFaces = {
            0, 1, 2,
            3, 2, 1,
            4, 5, 6,
            7, 6, 5
    };


    static Model2 loadBwoModel(String fname) { // or model2 ?
        fname = "chunk/" + fname;
        Log.e(TAG,"in loadBwoModel with '" + fname + "'");

        Model2 mod = Model2.createmodel(fname);
        if (mod.refcount > 1)
            return mod;

        // test bwo file
        UnChunker.unchunktest(fname);

        // for now build and display a placeholder model
        Mesh multimesh = new Mesh();
        multimesh.verts = multimeshVerts;
        multimesh.uvs = multeMeshUvs;
        multimesh.faces = multiMeshFaces;
        mod.setmesh(multimesh);
        mod.addmat("tex","Bark.png",2,4);
        mod.addmat("texc","maptestnck.png",2,4);
        mod.mats.get(1).put("color",new float[] {0,1,0,1});
        mod.flags |= Model.FLAG_DOUBLESIDED;
        mod.commit();

        return mod;
    }
}
