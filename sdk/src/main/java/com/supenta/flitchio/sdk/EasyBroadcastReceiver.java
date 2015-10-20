package com.supenta.flitchio.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

abstract class EasyBroadcastReceiver extends BroadcastReceiver {
    private final IntentFilter intentFilter;
    private boolean registered;

    protected EasyBroadcastReceiver(String... actionsToWatch) {
        registered = false;

        intentFilter = new IntentFilter();
        for (String action : actionsToWatch) {
            intentFilter.addAction(action);
        }
    }

    public void start(Context context) {
        if (registered) {
            FlitchioLog.i("BroadcastReceiver already registered: not registering again");
            return;
        }

        context.registerReceiver(this, intentFilter);
        registered = true;
    }

    public void stop(Context context) {
        if (!registered) {
            FlitchioLog.i("BroadcastReceiver was not registered: no need to stop it");
            return;
        }

        try {
            context.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            FlitchioLog.i("BroadcastReceiver was not registered: no need to stop it");
        }

        registered = false;
    }

    public boolean isRegistered() {
        return registered;
    }
}
