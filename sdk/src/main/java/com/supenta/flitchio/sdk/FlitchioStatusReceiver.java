package com.supenta.flitchio.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for listening changes to the status of Flitchio
 */
public class FlitchioStatusReceiver extends BroadcastReceiver {

    private static final String ACTION_FLITCHIO_CONNECTED =
            FlitchioController.FLITCHIO_MANAGER_PACKAGE + ".communication.ACTION_FLITCHIO_CONNECTED";

    private static final String ACTION_FLITCHIO_DISCONNECTED =
            FlitchioController.FLITCHIO_MANAGER_PACKAGE + ".communication.ACTION_FLITCHIO_DISCONNECTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        FlitchioLog.v("onReceive: " + intent);

        final String action = intent.getAction();

        switch (action) {
            case ACTION_FLITCHIO_CONNECTED:
                FlitchioLog.i("Flitchio has connected");


                break;
            case ACTION_FLITCHIO_DISCONNECTED:
                FlitchioLog.i("Flitchio has disconnected");


                break;
            default:
                FlitchioLog.wtf("Invalid intent: " + intent);
        }
    }
}
