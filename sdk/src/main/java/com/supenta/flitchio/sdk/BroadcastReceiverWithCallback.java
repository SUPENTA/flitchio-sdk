package com.supenta.flitchio.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

/**
 * Utility class for an easier definition of {@link BroadcastReceiver}s.
 *
 * @param <T> An interface for callbacks.
 */
abstract class BroadcastReceiverWithCallback<T> extends BroadcastReceiver {
    protected final IntentFilter intentFilter;
    private T callback;

    protected BroadcastReceiverWithCallback(String... actionsToWatch) {
        intentFilter = new IntentFilter();
        for (String action : actionsToWatch) {
            intentFilter.addAction(action);
        }
    }

    public void start(@NonNull Context context, @NonNull T callback) {
        this.callback = callback;

        registerReceiver(context);
    }

    protected void registerReceiver(@NonNull Context context) {
        context.registerReceiver(this, intentFilter);
    }

    public void stop(@NonNull Context context) {
        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            FlitchioLog.i("BroadcastReceiver was not registered: no need to stop it");
        }

        callback = null;
    }

    @NonNull
    protected T getCallback() {
        return callback;
    }
}
