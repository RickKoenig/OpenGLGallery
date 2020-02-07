package com.mperpetuo.openglgallery;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.Toast;

import com.mperpetuo.openglgallery.enga.StateMan;
import com.mperpetuo.openglgallery.engatest.StateList;

import java.io.File;

import static com.mperpetuo.openglgallery.enga.Utils.*;

public class MainActivity extends Activity {
	final private String TAG = "MainActivity";
    public MyGLSurfaceView mGLView;
    //Context mContext;
	public TextView FPSmeterTextView;
	Spinner mySpinner;
	//public SimpleUI simpleUI;
	SeekBar gainSeekBar;
	SeekBar convSeekBar;
	final float MIN_GAIN = 0;
	final float MAX_GAIN = .18f;
	float mGain = 0.0f;

	final float MIN_CONV = -1;
	final float MAX_CONV = 1;
	float mConv = 0.0f;
	boolean is3D;
	Button button3D;

	public static final String CAPTURE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+ File.separator+"mPerpetuo"+File.separator;
	String lastState;
	ArrayAdapter<String> sa;

	public static final boolean oldDevice = false; // false like 2.3.3 true like 5.1.1
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "o");
		Log.i(TAG, " n");
		Log.i(TAG, "  C");
		Log.i(TAG, "   r");
		Log.i(TAG, "    e");
		Log.i(TAG, "     a");
		Log.i(TAG, "      t");
		Log.i(TAG, "       e");
		setContext(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // (NEW)
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
		  WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // (NEW)
       	// get GLSurfaceView instance from layout
       	setContentView(R.layout.main);
   		mGLView = (MyGLSurfaceView)findViewById(R.id.surfaceoverlay1);
		FPSmeterTextView = (TextView)findViewById(R.id.FPSmeter);
		button3D = findViewById(R.id.button3D);
		button3D.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				is3D = !is3D;
				if (is3D) {
					gainSeekBar.setVisibility(View.VISIBLE);
					convSeekBar.setVisibility(View.VISIBLE);
					button3D.setText("3D");
				} else {
					gainSeekBar.setVisibility(View.GONE);
					convSeekBar.setVisibility(View.GONE);
					button3D.setText("2D");
				}
			}
		});
		gainSeekBar = findViewById(R.id.gain_seekbar);
		gainSeekBar.setProgress((int) mapRange(mGain, MIN_GAIN, MAX_GAIN,0, gainSeekBar.getMax()));
		gainSeekBar.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
						mGain = mapRange(i,0,gainSeekBar.getMax(),MIN_GAIN,MAX_GAIN);
						Log.e(TAG,"gain seekbar = " + mGain);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				}
		);
		convSeekBar = findViewById(R.id.conv_seekbar);
		convSeekBar.setProgress((int) mapRange(mConv, MIN_CONV, MAX_CONV,0, convSeekBar.getMax()));
		//convSeekBar.setProgress(convSeekBar.getMax()/2);
		convSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				mConv = mapRange(i,0,convSeekBar.getMax(),MIN_CONV,MAX_CONV);
				Log.e(TAG,"conv seekbar = " + mConv);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
   		// make some toast
        //mContext = this;
/*    	CharSequence text = "Hello toast!";
    	int duration = Toast.LENGTH_SHORT;
    	atoast = Toast.makeText(this, text, duration);
   		atoast.setGravity(Gravity.BOTTOM|Gravity.LEFT,0,0); */
		mySpinner = (Spinner) findViewById(R.id.stateSelect);
		String[] states = {"a","b","c"};
		sa = new ArrayAdapter<>(this,R.layout.astate);
		sa.setDropDownViewResource(R.layout.astate);
		//mySpinner.setDropDownWidth(desiredWidth);
		for (String state : StateList.states) {
			sa.add(state);
		}
		//SpinnerAdapter sa = new SpinnerA;
		mySpinner.setAdapter(sa);
		// setSelection will do a callback if listener set first, play around with the order of these two next statements
		// 2 the listener, more sensitive for MySpinner, fires off callbacks for same item selected
		mySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String startState = StateList.states[position];
				Log.e(TAG,"something selected, position = " + position + " id = " + id + " state = '" + startState + "'");
				StateMan.changeState(startState);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.e(TAG,"nothing selected");
			}
		});
		//simpleUI = new SimpleUI;
    }

    @Override
    protected void onResume() {
        super.onResume();
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		Log.i(TAG,"onResume");
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
       	mGLView.onResume();
		// 1 the new postion, which will fire off a callback to listener in MySpinner
		if (lastState == null || lastState.isEmpty())
			lastState = StateList.startState;
		mySpinner.setSelection(sa.getPosition(lastState));
		//StateMan.changeState(StateList.startState);
    }

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
		lastState = StateMan.getLastState();
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow()
					.getDecorView()
					.setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE
									| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_FULLSCREEN
									| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	public float getGain() {
		return mGain;
	}

	public float getConv() {
		return mConv;
	}

	public boolean getIs3D() {
		return is3D;
	}

	public void show3DUI() {
		if (is3D) {
			gainSeekBar.setVisibility(View.VISIBLE);
			convSeekBar.setVisibility(View.VISIBLE);
		}
		button3D.setVisibility(View.VISIBLE);
	}

	public void hide3DUI() {
		gainSeekBar.setVisibility(View.GONE);
		convSeekBar.setVisibility(View.GONE);
		button3D.setVisibility(View.GONE);
	}

}
