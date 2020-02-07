package com.mperpetuo.openglgallery.enga;

import java.util.ArrayList;

/**
 * Created by rickkoenig on 3/1/16.
 */
public class Mesh {
    public float[] verts;
    public float[] uvs;
    public float[] norms;
    public float[] cverts;
    public float[] uvs2;
    public short[] faces;

    public Mesh() {
    }

    public Mesh(MeshA msha) {
        verts = convertFloat(msha.verts);
        uvs = convertFloat(msha.uvs);
        norms = convertFloat(msha.norms);
        cverts = convertFloat(msha.cverts);
        uvs2 = convertFloat(msha.uvs2);
        faces = convertShort(msha.faces);
    }

    float[] convertFloat(ArrayList<Float> fa) {
        if (fa == null)
            return null;
        float[] ret = new float[fa.size()];
        int i;
        for (i=0;i<ret.length;++i)
            ret[i] = fa.get(i);
        return ret;
    }

    short[] convertShort(ArrayList<Short> sa) {
        if (sa == null)
            return null;
        short[] ret = new short[sa.size()];
        int i;
        for (i=0;i<ret.length;++i)
            ret[i] = sa.get(i);
        return ret;
    }

}

