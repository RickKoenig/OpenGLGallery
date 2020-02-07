package com.mperpetuo.openglgallery.enga;

import android.opengl.Matrix;

public class Lights {

    static Tree amblight,dirlight;

    public static void initlights() {
        amblight = null;
        dirlight = null;
    }

    public static void dolights() {

        float wld4[] = {0,0,1,0};
        float eld4[] = new float[4];
        if (dirlight != null && dirlight.mvm != null) {
            Matrix.multiplyMV(eld4,0,dirlight.mvm,0,wld4,0);
        } else {
            Matrix.multiplyMV(eld4,0,GLUtil.mvMatrix,0,wld4,0);
        }
        float[] eld3 = new float[3];
        eld3[0] = eld4[0];
        eld3[1] = eld4[1];
        eld3[2] = eld4[2]; // pare it down to 3 elements
        NuMath.normalize(eld3); // normalize
        GLUtil.globalmat.put("elightdir",eld3);
    }

    public static void addlight(Tree t) {
        if ((t.flags & Tree.FLAG_LIGHT) != 0) {
            if (amblight == null && (t.flags & Tree.FLAG_AMBLIGHT) != 0) {
                amblight = t;
            }
            if (dirlight == null && (t.flags & Tree.FLAG_DIRLIGHT) != 0) {
                dirlight = t;
            }
        }
    }

    public static void removeLight(Tree t) {
        if (t == amblight)
            amblight = null;
        if (t == dirlight)
            dirlight = null;
    }
}

    /*
    var lights = {
            "wlightdir":[0.0,0.0,0.0]
};

var dirlight = null; // this light will point along z

        var amblight = null;

        function dolights() {
        function addlight(t) {

        function removelight(t) {
}
*/