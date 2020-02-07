package com.mperpetuo.openglgallery.input;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mperpetuo.openglgallery.MainActivity;
import com.mperpetuo.openglgallery.R;
import com.mperpetuo.openglgallery.enga.Utils;

import java.util.ArrayList;

/**
 * Created by cyberrickers on 9/22/2016.
 */

// let opengl thread make some buttons and textviews
public class SimpleUI {
    static private String TAG = "SimpleUI";
    static ArrayList<UIElement> uiElements = new ArrayList<>(); // might be a hashmap but several elements with same key
    static String curGroup;
    static LinearLayout uiLayout;


    static class UIElement {
        public String uiGroupName;

        UIElement() {
            final UIElement fthis = this;
            Utils.getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (curGroup == null)
                        Log.d(TAG,"UIElement constructor, curgroup == null");
                    uiGroupName = curGroup;
                    uiElements.add(fthis);
                }
            });
        }
    }

    static public class UIPrintArea extends UIElement {
        TextView textView;

        UIPrintArea(final String ss) {
            Utils.getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView = new TextView(Utils.getContext());
                    textView.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                    int textHeight = MainActivity.oldDevice ? 35 : 110;
                    textView.setHeight(textHeight);
                    textView.setTextSize(16);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundColor(Color.argb(0x80,0xff,0xff,0xff));
                    textView.setTextColor(Color.BLACK);
                    ViewGroup.MarginLayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    int marg = 6;
                    params.setMargins(marg,marg,marg,marg);
                    textView.setLayoutParams(params);
                    Log.d(TAG,"adding printarea to view");
                    uiLayout.addView(textView);
                    draw(ss);
                }
            });
        }

        public void draw(final String s) {
            //if (FPSmeterTextView == null)
            //    return;
            Utils.getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView local = textView;
                    if (local != null)
                        local.setText(s);
                    else
                        Log.d(TAG,"UIPrintArea draw null FPSmeterTextView");
                }
            });
        }
    }

    static public class UIButton extends UIElement {
        Button button;
        Runnable mRun; // what to run when button is clicked, runs on opengl thread

        UIButton(final String ss, final Runnable run) {
            //super();
            Utils.getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRun = run;
                    button = new Button(Utils.getContext());
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG,"button " + ((Button)v).getText() + " clicked");
                            Utils.getContext().mGLView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG,"running in the opengl thread!!");
                                    if (mRun != null) {
                                        mRun.run();
                                    }
                                }
                            });
                        }
                    });
                    // define the style of the button
                    button.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                    int butHeight = MainActivity.oldDevice ? 45 : 3;
                    //butHeight = 10;
                    button.setHeight(butHeight); // 2.3.3
                    button.setGravity(Gravity.CENTER);
                    button.setBackgroundColor(Color.argb(0x80,0xff,0x40,0x40));
                    button.setTextColor(Color.BLACK);
                    ViewGroup.MarginLayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    int marg = 3;
                    params.setMargins(marg,marg,marg,marg);
                    button.setLayoutParams(params);
                    Log.d(TAG,"adding button to view");
                    uiLayout.addView(button);
                    draw(ss);
                }
            });
        }

        public void draw(final String s) {
            //if (FPSmeterTextView == null)
            //    return;
            Utils.getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (button != null)
                        button.setText(s);
                    else
                        Log.d(TAG,"UIPrintArea draw null FPSmeterTextView");
                    /*
                    Button local = button;
                    if (local != null)
                        local.setText(s);
                    else
                        Log.d(TAG,"UIPrintArea draw null FPSmeterTextView");
                        */
                }
            });
        }
    }

    public static void reset() {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uiLayout = (LinearLayout)Utils.getContext().findViewById(R.id.simpleUI);
                uiLayout.removeAllViews();
                Log.d(TAG,"reseting simpleUI");
                curGroup = null;
                uiElements.clear();
            }
        });
    }

    public static UIPrintArea makeaprintarea(String startString) {
        return new UIPrintArea(startString);
    }

    public static void makeabut(String startString, Runnable run) {
        new UIButton(startString,run);
    }

    public static void setbutsname(final String ns) {
        // run on UI thread?

        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"setting butsname to " + ns);
                curGroup = ns;
            }
        });
        //curGroup = ns; // don't run on UI thread
    }

    public static void clearbuts(final String group) {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"clearing buts for " + group);
                for (int i = 0; i < uiElements.size(); ++i) {
                    UIElement uie = uiElements.get(i);
                    if (group == null)
                        Log.d(TAG,"group == null");
                    if (uie == null)
                        Log.d(TAG,"uie == null");
                    if (uie.uiGroupName == null)
                        Log.d(TAG,"uiGroupName == null");
                    if (uie.uiGroupName.equals(group)) {
                        if (uie instanceof UIPrintArea) { // printarea, remove from layout
                            UIPrintArea uipa = (UIPrintArea) uie;
                            removeUIPrintArea(uipa);
                        }
                        if (uie instanceof UIButton) { // button, remove from layout
                            UIButton uib = (UIButton) uie;
                            removeUIButton(uib);
                        }
                        uiElements.remove(i--); // remove from the list
                    }
                }
                Log.d(TAG,"done clearing buts");
            }
        });

    }

    private static void removeUIPrintArea(final UIPrintArea printArea) {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"removing printarea from view");
                uiLayout.removeView(printArea.textView);
                printArea.textView = null;
            }
        });
    }

    private static void removeUIButton(final UIButton uiButton) {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"removing button from view");
                uiLayout.removeView(uiButton.button);
                uiButton.button = null;
            }
        });
    }

    /*
    public static void removeUIElement(final UIElement ele) {
        Utils.getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //uiLayout.removeView(ele.aview);
                //ele.aview = null;
            }
        });
    } */

}
