package com.mperpetuo.openglgallery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

// fire off callbacks for same item selected, more sensitive
public class MySpinner extends Spinner {

    OnItemSelectedListener listener;

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (position == getSelectedItemPosition()) {
            if (listener != null) // do we have a listener yet?
                listener.onItemSelected(null, null, position, 0);
        }
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listenera) {
        listener = listenera;
    }

}