package com.mperpetuo.openglgallery.engatest;

import android.util.Log;
import com.mperpetuo.openglgallery.enga.*;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import static com.mperpetuo.openglgallery.enga.Utils.incWrap;

public class Basic4Textures extends State {
    final String TAG = "Basic4Textures";
    Tree roottree; // top level tree
    static int shaderIndex = 0;
    static final String[] shaderNames = {"interleave4", "tex4", "blend4uv"};
    final int maxShaderIndex = shaderNames.length;
    Tree planexy;

    @Override
    public void init() {
        Log.i(TAG, "entering webgl basic4textures 3D\n");
        // main scene
        roottree = new Tree("basic4textures root tree");
        // build a planexy (a square)
        String[] textureList = {"maptestnck.png","panel.jpg","Bark.png","take0016.jpg"};
        //Tree plane = ModelUtil.buildplanexy2t("aplane", 1, 1, "light.jpg","dark.jpg",shaderName);
        planexy = ModelUtil.buildplanexyNt("aplane",1,1,textureList,shaderNames[shaderIndex]);
        //atree = ModelUtil.buildplanexy("aplane", 1, 1, "take0016.jpg", "tex"); // name, size, texture, generic texture shader
        //var plane = buildplanexyNt("aplane",1,1,textureList,shaderName); // TODO: implement
        planexy.mod.flags |= Model.FLAG_DOUBLESIDED; // draw backface since scale has a -1
        planexy.trans = new float[] {0,0,1};
        roottree.linkchild(planexy); // link to and pass ownership to backgroundtree
        ViewPort.mainvp = new ViewPort(); // use default viewport
        ViewPort.setupViewportUI(0);
        onResize();
    }

    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        // proc
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
    public void onResize() {
        if (Main3D.viewAsp >= 1.0f) {
            planexy.scale = new float[] {Main3D.viewAsp, 1, 1};
        } else {
            planexy.scale = new float[] {1, 1.0f/Main3D.viewAsp, 1};
        }
        if (shaderIndex == 0) {
            // set 'resolution' uniform in shader
            planexy.mat.put("resolution",new float[] {Main3D.viewWidth,Main3D.viewHeight});
        }
    }

    @Override
    public void exit() {
        // reset main ViewPort to default
        SimpleUI.clearbuts("viewport");
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG, "before roottree glFree");
        roottree.log(); // show roottree allocated resources
        GLUtil.logrc(); // show all allocated resources
        // cleanup
        roottree.glFree();
        // show usage after cleanup
        Log.i(TAG, "after roottree glFree");
        roottree = null;
        GLUtil.logrc(); // show all allocated resources, should be clean
        shaderIndex = incWrap(shaderIndex, maxShaderIndex);
    }
}
