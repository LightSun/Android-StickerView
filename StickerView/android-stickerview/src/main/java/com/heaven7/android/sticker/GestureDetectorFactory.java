package com.heaven7.android.sticker;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/*public*/ final class GestureDetectorFactory {

    public static Delegate getDelegate(Context context, GestureDetector.OnGestureListener l){
        Class<?> clazz;
        try {
            clazz = Class.forName("androidx.core.view.GestureDetectorCompat");
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName("android.support.v4.view.GestureDetectorCompat");
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            Constructor<?> cons = clazz.getConstructor(Context.class, GestureDetector.OnGestureListener.class);
            return new Impl(clazz, cons.newInstance(context, l));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public interface Delegate{
        boolean onTouchEvent(MotionEvent e);
    }

    private static class Impl implements Delegate{
        static Method sMethod;

        final Object receiver;

        public Impl(Class<?> clazz, Object receiver) {
            this.receiver = receiver;
            if(sMethod == null){
                try {
                    sMethod = clazz.getMethod("onTouchEvent", MotionEvent.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            try {
                return (boolean) sMethod.invoke(receiver, e);
            } catch (Exception e1) {
                return false;
            }
        }
    }
}
