package com.supenta.flitchio.sdk;

import com.supenta.flitchio.sdk.FlitchioSnapshot;
import com.supenta.flitchio.sdk.IFlitchioClient;

/** @hide */
interface IFlitchioService {
	FlitchioSnapshot getSnapshot(in int authToken);
	boolean isConnected(in int authToken);

	/* !!! Remove the "oneway"s if these methods are supposed to return exceptions */

	oneway void registerClient(in int authToken, IFlitchioClient client);
	oneway void unregisterClient(in int authToken, IFlitchioClient client);

	int receiveClientInfo(in ComponentName componentName);
	oneway void removeClientInfo(in int authToken);
}