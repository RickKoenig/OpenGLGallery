package com.mperpetuo.openglgallery.input;

import com.mperpetuo.openglgallery.MainActivity;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.old_input.InputResult;

/**
 * Created by cyberrickers on 3/18/2016.
 */
public class Input {
    public static InputState getResult() {
        MainActivity ma = (MainActivity) Utils.getContext();
        InputState ir = ma.mGLView.ofi.getResult();
        return ir;
    }
}
