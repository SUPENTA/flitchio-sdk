package com.supenta.flitchio.sdk;

import com.supenta.flitchio.sdk.ButtonEvent;
import com.supenta.flitchio.sdk.JoystickEvent;

/** @hide */
interface IFlitchioClient {
	oneway void onButtonEvent(in ButtonEvent event);
	oneway void onJoystickEvent(in JoystickEvent event);
}