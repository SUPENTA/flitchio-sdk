package com.supenta.flitchio.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * Receiver of Flitchio status update events.
 * <p/>
 * Trick: the callback from this broadcast receiver is actually same as what we give to the
 * end listener so we use the same interface {@link FlitchioStatusListener}.
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
     * The possible values are the constants of {@link FlitchioStatusListener}.
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
