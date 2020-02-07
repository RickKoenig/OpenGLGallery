package com.mperpetuo.openglgallery.enga;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by rickkoenig on 3/4/16.
 */
public class FunctorTest {
    private static final String TAG = "FunctorTest";


    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        T[] array = list.toArray((T[]) new Object[list.size()]);
        Arrays.sort(array, comparator);
        int i = 0;
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(array[i++]);
        }
    }

    static class ShowerFunc {
        void show(String s) {
            Log.e(TAG,"showing something '" + s + "'");
        }
    }

    public static void shower(List<String> list,ShowerFunc sf) {
        for (String s:list) {
            sf.show(s);
        }
    }

    public static void test() {
        Log.e(TAG, "test functors in java");


        List<String> list = Arrays.asList("10", "1", "20", "11", "21", "12");

        Comparator<String> numStringComparator = new Comparator<String>() {
            public int compare(String str1, String str2) {
                return Integer.valueOf(str1).compareTo(Integer.valueOf(str2));
            }
        };

        Collections.sort(list, numStringComparator);

        Log.e(TAG, "sorted");
        for (String s: list) {
            Log.e(TAG,"string : '" + s + "'");
        }

        ShowerFunc shf = new ShowerFunc();

        ShowerFunc shf2 = new ShowerFunc() {
            @Override
            void show(String s) {
                Log.e(TAG,"showing something else '" + s + "'");
            }
        };
        shower(list,shf2);

        Log.e(TAG,"done functor tests");
    }
}
