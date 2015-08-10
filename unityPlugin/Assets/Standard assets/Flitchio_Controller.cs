using UnityEngine;
using System.Collections;

public class Flitchio_Controller
{

	private readonly AndroidJavaObject mCurrentActivity;
	private readonly AndroidJavaObject mController;

	public Flitchio_Controller ()
	{
		Debug.Log ("Flitchio_Controller$ctor");

		AndroidJavaClass unityPlayerCls = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		mCurrentActivity = unityPlayerCls.GetStatic<AndroidJavaObject> ("currentActivity");

		AndroidJavaClass flitchioControllerCls = new AndroidJavaClass ("com.supenta.flitchio.sdk.FlitchioController");
		mController = flitchioControllerCls.CallStatic<AndroidJavaObject> ("getInstance", mCurrentActivity);
	}

	public bool onCreate ()
	{
		// TODO handle exception

		Debug.Log ("Flitchio_Controller$onCreate()");
		return mController.Call<bool> ("onCreate");
	}

	/* TODO implement listening mode? or not?
	onResume
	onResume

	public void onPause() {
		mController.Call("onPause");
	}
	*/

	public void onDestroy ()
	{
		Debug.Log ("Flitchio_Controller$onDestroy()");
		mController.Call ("onDestroy");
	}

	public int getVersionCode ()
	{
		// TODO it is normally static
		Debug.Log ("Flitchio_Controller$getVersionCode()");
		return mController.CallStatic<int> ("getVersionCode");
	}

	public int getFlitchioManagerVersionCode ()
	{
		// TODO it is normally static
		// TODO handle exception
		Debug.Log ("Flitchio_Controller$getFlitchioManagerVersionCode()");
		return mController.CallStatic<int> ("getFlitchioManagerVersionCode", mCurrentActivity);
	}

	public AndroidJavaObject obtainSnapshot ()
	{
		return mController.Call<AndroidJavaObject> ("obtainSnapshot");
	}

	public float getJoystickX (AndroidJavaObject snapshot, int joystickCode)
	{
		return snapshot.Call<float> ("getJoystickX", joystickCode);
	}

	public float getJoystickY (AndroidJavaObject snapshot, int joystickCode)
	{
		return snapshot.Call<float> ("getJoystickY", joystickCode);
	}

	public float getButtonPressure (AndroidJavaObject snapshot, int buttonCode)
	{
		return snapshot.Call<float> ("getButtonPressure", buttonCode);
	}

	public int getButtonState (AndroidJavaObject snapshot, int buttonCode)
	{
		return snapshot.Call<int> ("getButtonState", buttonCode);
	}

	public int getButtonRepeatCount (AndroidJavaObject snapshot, int buttonCode)
	{
		return snapshot.Call<int> ("getButtonRepeatCount", buttonCode);
	}
}
