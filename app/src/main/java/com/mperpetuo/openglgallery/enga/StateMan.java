package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by rickkoenig on 2/23/16.
 */
public class StateMan {
    static String TAG = "StateMan";
    static State curState;
    static boolean doChangeState;
    static String oldStateName = "null";
    static String newStateName;
    static Class<?> c;

    // runs on opengl thread, can call changeState from any thread
    public static void init() {
        curState = null;
    }

    public synchronized static void proc() {
        if (doChangeState) {
            exitState();
            c = null;
            try {
                c = Class.forName("com.mperpetuo.openglgallery.engatest." + newStateName);
            } catch (ClassNotFoundException e) {
                //Utils.alert("can't find class '" + newStateName + "'");
                //e.printStackTrace();
                if (!newStateName.isEmpty()) {
                    Log.e(TAG,"Can't find state to go to !!!");
                }
            }
            if (c != null) {
                Constructor<?> cons = null;
                try {
                    cons = c.getConstructor();
                } catch (NoSuchMethodException e) {
                    //Utils.alert("can't find constructor for '" + newStateName + "'");
                    e.printStackTrace();
                }
                try {
                    Object obj = cons.newInstance();
                    if (obj instanceof State)
                        curState = (State) obj;
                    else
                        Utils.alert("'" + newStateName + "' is not a State");
                } catch (InstantiationException e) {
                    //Utils.alert("can't make instance of class '" + newStateName + "'");
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    //Utils.alert("don't have access to class '" + newStateName + "'");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    //Utils.alert("InvocationTargetException of class '" + newStateName + "'");
                    e.printStackTrace();
                }
                showAndGo("init", newStateName);
                doChangeState = false;
            }
        }
        if (curState != null) {
            curState.proc();
            curState.draw();
        }
    }

    public synchronized static void onResize( int x, int y) {
        Log.e(TAG,"state changed size, now at " + x + " " + y);
        // want to make more compatible with engw
        if (curState != null) {
            curState.onResize();
        }
    }

    public synchronized static void changeState(String ns) {
        Log.e(TAG,"changing state to '" + ns + "'");
        oldStateName = newStateName;
        newStateName = ns;
        doChangeState = true;
    }

    private static void exitState() {
        if (curState != null)
            showAndGo("exit",oldStateName);
        curState = null;
    }

    private static void showAndGo(String methodName,String stateName) {
        Log.w(TAG, "^^^^^^^^^^^ start " + stateName + "." + methodName + " ^^^^^^^^^^^");
        Method method = null;
        try {
            method = c.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Utils.alert("no such method '" + methodName + "' in class '" + stateName + "'");
        }
        try {
            method.invoke(curState);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Utils.alert("IllegalAccessException: method '" + methodName + "' in class '" + stateName + "'");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Utils.alert("InvocationTargetException: method '" + methodName + "' in class '" + stateName + "'");
        }
        Log.w(TAG, "vvvvvvvvvvvvv end " + stateName + "." + methodName + " vvvvvvvvvvvv");
    }

    public static String getLastState() {
        return newStateName;
    }
}
