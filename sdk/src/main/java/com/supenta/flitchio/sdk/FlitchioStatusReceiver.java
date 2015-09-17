package com.supenta.flitchio.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Receiver for listening changes to the status of Flitchio
 */
public class FlitchioStatusReceiver extends BroadcastReceiver {

    private static final String ACTION_FLITCHIO_CONNECTED =
            FlitchioController.FLITCHIO_SERVICE_CLASS + ".communication.ACTION_FLITCHIO_CONNECTED";

    private static final String ACTION_FLITCHIO_DISCONNECTED =
            FlitchioController.FLITCHIO_SERVICE_CLASS + ".communication.ACTION_FLITCHIO_DISCONNECTED";

    private final FlitchioController flitchioController;

    public FlitchioStatusReceiver(FlitchioController flitchioController) {
        this.flitchioController = flitchioController;
    }

    IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FLITCHIO_CONNECTED);
        intentFilter.addAction(ACTION_FLITCHIO_DISCONNECTED);
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FlitchioLog.v("onReceive: " + intent);

        final String action = intent.getAction();

        switch (action) {
            case ACTION_FLITCHIO_CONNECTED:
                FlitchioLog.i("Flitchio has connected");

                flitchioController.postStatusUpdate(true /* isConnected */);
                break;
            case ACTION_FLITCHIO_DISCONNECTED:
                FlitchioLog.i("Flitchio has disconnected");

                flitchioController.postStatusUpdate(false /* isConnected */);
                break;
            default:
                FlitchioLog.wtf("Invalid intent: " + intent);
        }
    }
}
