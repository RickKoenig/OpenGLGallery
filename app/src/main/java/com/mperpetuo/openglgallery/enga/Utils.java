package com.mperpetuo.openglgallery.enga;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mperpetuo.openglgallery.MainActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by rickkoenig on 2/23/16.
 */

public class Utils {
    static final String TAG = "Utils";
    static String indent = "";
    static String indentString = "   ";
    static int indentAmount = indentString.length();

    static Bitmap curBitmap;
    static String curBitmapString = "";

    static String defaultdir = "common";
    static String curdir = defaultdir;

    static Stack<String> dirStack = new Stack<>();
    static final int maxDirStackSize = 4;

    public static void resetDirStack() {
        dirStack.clear();
        curdir = defaultdir;
    }

    public static void pushandsetdir(String newdir) {
        if (dirStack.size() >= maxDirStackSize)
            alert("pushandsetdir: '" + newdir + "' dirStack is too large " + dirStack.size() + " !!");
        dirStack.push(curdir);
        curdir = newdir;
        Log.i(TAG,"pushandsetdir: '" + newdir + "' stack size " + dirStack.size());
    }

    public static void popdir() {
        if (dirStack.empty())
            alert("popdir dirStack is empty !!");
        curdir = dirStack.pop();
        Log.i(TAG,"popdir: '" + curdir + "' stack size " + dirStack.size());
    }

    static MainActivity cont;

    public static void setContext(Context c) {
        cont = (MainActivity)c;
    }

    public static MainActivity getContext() {
        return cont;
    }

    static void addIndent(int spc) {
        if (spc > 0) {
            indent += indentString;
        } else if (spc < 0) {
            indent = indent.substring(indentAmount);
        }
    }

    // list contents of assets folder
    public static void showAssets(String path) {
        try {
            AssetManager assetManager = cont.getAssets();
            String[] aList = assetManager.list(path);
            if (indent.isEmpty() /* && aList.length > 0 */)
                //if (true)
                Log.e(TAG, "------ assets file list of '" + path + "'");
            if (aList.length > 0) {
                addIndent(indentAmount);
                for (String s : aList) {
                    Log.e(TAG, indent + s);
                    if (path.isEmpty())
                        showAssets(s);
                    else
                        showAssets(path + "/" + s);
                }
                addIndent(-indentAmount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String[] getdirFromAsset() {
        AssetManager assetManager = cont.getAssets();
        String[] aList = null;
        try {
            aList = assetManager.list(curdir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aList;
    }

    static ByteBuffer getBinaryFromAsset(String strName) {
        final int bufferSize = 0x20000; // ~130K.
        //final int bufferSize = 0x400; // ~1K.
        AssetManager assetManager = cont.getAssets();
        InputStream inStream;
        ByteArrayOutputStream outStream;
        try {
            inStream = assetManager.open(curdir + "/" + strName);
            byte[] buffer = new byte[bufferSize];
            outStream = new ByteArrayOutputStream(bufferSize);
            int read;
            while (true) {
                read = inStream.read(buffer);
                if (read == -1)
                    break;
                outStream.write(buffer, 0, read);
            }
            ByteBuffer byteData = ByteBuffer.wrap(outStream.toByteArray());
            byteData.order(ByteOrder.LITTLE_ENDIAN);
            inStream.close();
            outStream.close();
            return byteData;
        } catch (IOException e) {
            //e.printStackTrace();
            Log.e(TAG,"can't open binary from asset for " + strName);
        }
        return null;
    }

    // cache last bitmap accessed
    public static Bitmap getBitmapFromAsset(String strName) {
        strName = curdir + "/" + strName;
        //if (strName.equals("textures/pter.jpg")) {
        //    Log.e(TAG,"PTER.JPG loaded");
        //}
        if (curBitmapString.equals(strName))
            return curBitmap;
        AssetManager assetManager = cont.getAssets();
        Bitmap bitmap = null;
        try {
            InputStream inStream = null;
            inStream = assetManager.open(strName);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(inStream,null,opt);
            inStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        if (bitmap == null)
            return null;
        boolean scaleDown = true;
        int maxWidth = 1024;
        int maxHeight = 1024;
        if (scaleDown && (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight)) {
            bitmap = Bitmap.createScaledBitmap(bitmap,maxWidth,maxHeight,false);
        }
        curBitmap = bitmap;
        curBitmapString = strName;
        return bitmap;
    }

    public static String getStringFromAsset(String filename, boolean useNewline)
    {
        filename = curdir + "/" + filename;
        try {
            Context ctx = Utils.getContext();
            InputStream is = ctx.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line + (useNewline ? "\n" : ""));
            }
            is.close();
            return builder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void alert(String mess) {
        Log.e(TAG, "ALERT: " + mess);
        throw new RuntimeException(mess);
    }

    public static float range(float lo, float val, float hi) {
        if (val < lo)
            return lo;
        if (val > hi)
            return hi;
        return val;
    }

    // good for sliders
    public static float mapRange(float in, float inMin, float inMax, float outMin, float outMax) {
        return (in - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    // inc but wrap
    public static int incWrap(int val,int num) {
        ++val;
        if (val >= num)
            val -= num;
        return val;
    }

    // return the extension of a url, everything after the last '.'
    public static String getExt(String url) {
        int pos = url.lastIndexOf('.');
        if (pos >= 0) {
            return url.substring(pos + 1);
        } else {
            return "";
        }
    }

    // increase size of arraylist
    public static void ensureSize(ArrayList<?> list, int size) {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(null);
        }
    }

/*
    function spliturl(url) {
        var s = {};
        s.path = "";
        s.name = "";
        s.ext = "";
        var pidx = url.lastIndexOf("/");
        if (pidx>=0) {
            s.path = url.substr(0,pidx);
            url = url.substr(pidx+1);
        }
        var eidx = url.lastIndexOf(".");
        if (eidx >= 0) {
            s.name = url.substr(0,eidx);
            s.ext = url.substr(eidx+1);
        } else {
            s.name = url;
        }
        return s;
    }
*/

    /*    static void addPath(String subdir) {

        }

        static void upPath() {

        }
    */

}
