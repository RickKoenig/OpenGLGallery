package com.mperpetuo.openglgallery;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cyberrickers on 9/19/2016.
 */
public class SortTest {
    final String TAG = "SortTest";

    class PointI3 {
        int x,y,z;
        PointI3(int xa,int ya,int za) {
            x = xa;
            y = ya;
            z = za;
        }
        @Override
        public String toString() {
            return x + " " + y + " " + z;
        }
    }

    static Comparator<PointI3> treeComparatorX = new Comparator<PointI3>() {
        public int compare(PointI3 a, PointI3 b) {
            return a.x - b.x; // lower drawpris have priority
        }
    };

    static Comparator<PointI3> treeComparatorY = new Comparator<PointI3>() {
        public int compare(PointI3 a, PointI3 b) {
            return a.y - b.y; // lower drawpris have priority
        }
    };

    static Comparator<PointI3> treeComparatorZ = new Comparator<PointI3>() {
        public int compare(PointI3 a, PointI3 b) {
            return a.z - b.z; // lower drawpris have priority
        }
    };

    public void test() {
        Log.e(TAG,"start sorttest");
        PointI3[] points = new PointI3[] {
                new PointI3(9,3,4),
                new PointI3(2,2,5),
                new PointI3(6,1,1),
                new PointI3(7,3,4),
                new PointI3(1,1,4),
        };
        //ArrayList<PointI3> pointsA = Arrays.asList(points);
        ArrayList<PointI3> pointsA = new ArrayList<PointI3>(Arrays.asList(points));

        Log.e(TAG,"middle");
        for (PointI3 pi3 : pointsA) {
            Log.e(TAG,pi3.toString());
        }

        Collections.sort(pointsA,treeComparatorX);
        Log.e(TAG,"middle sorted X");
        for (PointI3 pi3 : pointsA) {
            Log.e(TAG,pi3.toString());
        }

        Collections.sort(pointsA,treeComparatorY);
        Log.e(TAG,"middle sorted Y");
        for (PointI3 pi3 : pointsA) {
            Log.e(TAG,pi3.toString());
        }

        Collections.sort(pointsA,treeComparatorZ);
        Log.e(TAG,"middle sorted Z");
        for (PointI3 pi3 : pointsA) {
            Log.e(TAG,pi3.toString());
        }

        Log.e(TAG,"end sorttest");
    }
}
