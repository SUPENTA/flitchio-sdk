package com.supenta.flitchio.sdk;

import android.util.Log;

/**
 * Used as a wrapper around android.util.Log.
 */
class FlitchioLog {
    private static final String TAG = "Flitchio";
    static String fileName;
    static String className;
    static String methodName;
    static int lineNumber;

    private FlitchioLog() {
    }

    @SuppressWarnings("SameReturnValue")
    private static String getTag() {
        // StackTraceElement[] sElements = new Throwable().getStackTrace();

        // className = sElements[2].getClassName() returns
        //
        // com.supenta.flitchio.sdk.FlitchioController
        // or (inner class):
        // com.supenta.flitchio.sdk.FlitchioController$IncomingHandler
        // or (inner, anonymous class):
        // com.supenta.flitchio.sdk.FlitchioController$1
        //
        // when using proguard (on the sdk release jar):
        //
        // com.supenta.flitchio.sdk.FlitchioController (public class)
        // or (inner class):
        // com.supenta.flitchio.sdk.FlitchioController$a (obfuscated)
        // or (inner, anonymous class):
        // com.supenta.flitchio.sdk.FlitchioController$1 (same as before, but useless)

        // fileName = sElements[2].getFileName();
        // className = (fileName != null ? fileName.replaceFirst("\\.java", "") : null);

        // methodName = sElements[2].getMethodName();
        // Don't add method name because
        // 1) Private methods get obfuscated in exported JAR
        // 2) Logs from anonymous inner classes are hard to understand
        // buffer.append("$");
        // buffer.append(methodName);

        // lineNumber = sElements[2].getLineNumber();

        return TAG;
    }

    public static void e(Object message) {
        Log.e(getTag(), message.toString());
    }

    public static void i(Object message) {
        Log.i(getTag(), message.toString());
    }

    public static void d(Object message) {
        Log.d(getTag(), message.toString());
    }

    public static void v(Object message) {
        Log.v(getTag(), message.toString());
    }

    public static void w(Object message) {
        Log.w(getTag(), message.toString());
    }

    public static void wtf(Object message) {
        Log.wtf(getTag(), message.toString());
    }
}
