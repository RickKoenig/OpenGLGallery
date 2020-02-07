package com.mperpetuo.openglgallery.enga;

import java.util.HashMap;

/**
 * Created by rickkoenig on 2/29/16.
 */
public class Shader {
    int glshader = -1;
    //int uSampler = -1;
    String name = null;
    HashMap<String,Integer> actunifs = new HashMap<>();
    HashMap<String,Integer> actattribs = new HashMap<>();
    HashMap<String,Integer> actsamplers = new HashMap<>();
}
