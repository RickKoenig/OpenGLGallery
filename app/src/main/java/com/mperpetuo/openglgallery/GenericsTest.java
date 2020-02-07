package com.mperpetuo.openglgallery;

import android.util.Log;

/**
 * Created by cyberrickers on 1/24/2016.
 */
public class GenericsTest {
    final String TAG = "GenericsTest";
    public void test() {

        // test generics
        Number n = new Float(3.1f);
        Log.e(TAG, "test generics" + n);
        //n += 10;
        Box<Float> b = new Box<>();
        b.set(10.1f);
        float f = b.get();
        Log.e(TAG,"box get = " + f);

        // test conversions between numeric types
        float f32 = 1.1f;
        double f64 = 1.1;
        byte i8 = 34;
        short i16 = 69;
        int i32 = 3333;
        long i64 = 3343134;
        f32 = i64; // int to float, okay
        i32 = (int)f32; // float to int, needs a cast
        //i64 = f32;
        //i8 = f32;
        f32 = i8;
        f32 = i64;

        // test float to int
        Log.e(TAG,"test float to int");
        for (f32=-3.0f;f32<=3.0f;f32+=.125f) {
            //i32 = (int)f32; // round towards 0
            //i32 = (int)Math.rint(f32); // bankers rounding
            i32 = Math.round(f32); // best rounding, round nearest, .5 rounds up
            //i32 = (int)Math.floor(f32); // lower
            //i32 = (int)Math.ceil(f32); // higher
            Log.e(TAG,"float " + f32 + " int " + i32);
        }

        // test int to float precision, find out where ints can't be stored as floats
        Log.e(TAG,"test precision");
        int failCount = 0;
        for (i32=0;i32>=0;++i32) { // until it wraps to negative
            f32 = i32;
            int testInt = Math.round(f32); // 8388609
            //int testInt = (int)f32; // 16777217
            //int testInt = (int)Math.rint(f32); // 16777217
            if (testInt != i32) {
                Log.e(TAG,"int can't be stored as float, src = " + i32 + " dest = " + testInt + " float = " + f32);
                ++failCount;
                if (failCount > 10)
                    break;
            }
        }
        //Log.e(TAG,"one past max = " + i32);

    }

    /**
     * Generic version of the Box class.
     * @param <T> the type of the value being boxed
     */
    public class Box<T> {
        // T stands for "Type"
        private T t;

        public void set(T tA) {
            t = tA;
        }

        public T get() {
            return t;
        }
    }

}
