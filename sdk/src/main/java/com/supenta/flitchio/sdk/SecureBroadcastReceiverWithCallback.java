package com.supenta.flitchio.sdk;

import android.content.Context;
import android.support.annotation.NonNull;

abstract class SecureBroadcastReceiverWithCallback<T> extends BroadcastReceiverWithCallback<T> {
    private final String permission;

    protected SecureBroadcastReceiverWithCallback(@NonNull String permission, String... actionsToWatch) {
        super(actionsToWatch);

        this.permission = permission;
    }

    @Override
    protected void registerReceiver(@NonNull Context context) {
        context.registerReceiver(this, intentFilter, permission, null);
    }
}
