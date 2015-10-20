package com.supenta.flitchio.sdk;

import android.content.Context;
import android.support.annotation.NonNull;

abstract class BroadcastReceiverWithCallback<T> extends EasyBroadcastReceiver {
    private T callback;

    BroadcastReceiverWithCallback(String... actionsToWatch) {
        super(actionsToWatch);
    }

    public void start(Context context, @NonNull T callback) {
        super.start(context);

        this.callback = callback;
    }

    /**
     * Warning: stop() also removes the callback given in {@link #start(Context, Object)}.
     *
     * @param context
     */
    @Override
    public void stop(Context context) {
        callback = null;

        super.stop(context);
    }

    @NonNull
    protected T retrieveCallback() {
        return callback;
    }
}
