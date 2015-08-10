package com.supenta.flitchio.sdk;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Thread that declares a Handler. This class is quite generic, actually Android has HandlerThread
 * that is a class Handler-ready, but it doesn't directly provide a Handler.
 */
class ListenerThread extends HandlerThread {

    private Handler handler;

    /**
     * Start the thread and directly create a handler for it.
     */
    public ListenerThread() {
        super("ListenerThread");

        start();
        handler = new Handler(getLooper());
    }

    /**
     * Return the handler internally created for this thread.
     *
     * @return The handler internally created for this thread.
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * Quit the handler thread's looper.
     *
     * @return True if the looper looper has been asked to quit or false if the
     * thread had not yet started running
     */
    @Override
    public boolean quit() {
        handler = null;
        return super.quit();
    }
}
