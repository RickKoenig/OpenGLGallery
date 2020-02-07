package com.mperpetuo.openglgallery.enga;

/**
 * Created by rickkoenig on 2/23/16.
 */

// a base class for a state, like an activity
public abstract class State {
    abstract public void init();
    public void onResize() {} // read Main3D.wiewWidth and Main3D.viewHeight and Main3D.viewAsp
    abstract public void proc();
    abstract public void draw();
    abstract public void exit();
}
