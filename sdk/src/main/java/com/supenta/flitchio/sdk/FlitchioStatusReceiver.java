package com.supenta.flitchio.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * Receiver of Flitchio status update events. Out of all the status in
 * {@link FlitchioStatusListener}, this broadcast receiver is only responsible for watching
 * {@link FlitchioStatusListener#STATUS_CONNECTED} and
 * {@link FlitchioStatusListener#STATUS_DISCONNECTED}.
 */
class FlitchioStatusReceiver extends BroadcastReceiverWithCallback<FlitchioStatusListener> {
    /**
     * Broadcast notifying that Flitchio has connected or disconnected.
     * Always contains {@link #EXTRA_STATUS}.
     * KEEP IT SYNCED WITH THE VALUE IN FLITCHIO MANAGER.
     */
    static final String ACTION_FLITCHIO_STATUS_CHANGED =
            FlitchioController.FLITCHIO_MANAGER_PACKAGE + ".ACTION_FLITCHIO_STATUS_CHANGED";

    /**
     * Current status of Flitchio passed with a {@link #ACTION_FLITCHIO_STATUS_CHANGED} broadcast.
     * The two possible values are {@link FlitchioStatusListener#STATUS_CONNECTED} and
     * {@link FlitchioStatusListener#STATUS_DISCONNECTED}.
     * KEEP IT SYNCED WITH THE VALUE IN FLITCHIO MANAGER.
     */
    static final String EXTRA_STATUS =
            FlitchioController.FLITCHIO_MANAGER_PACKAGE + ".EXTRA_STATUS";

    FlitchioStatusReceiver() {
        super(ACTION_FLITCHIO_STATUS_CHANGED);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FlitchioLog.v("onReceive: " + intent);

        final int status = intent.getIntExtra(EXTRA_STATUS, FlitchioStatusListener.STATUS_UNKNOWN);
        getCallback().onFlitchioStatusChanged(status);
    }
}
