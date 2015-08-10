using UnityEngine;
using System;
using System.Collections.Generic;

public class Flitchio_Input : MonoBehaviour
{

	const int BUTTON_TOP = 0;
	const int BUTTON_BOTTOM = 1;
	const int JOYSTICK_TOP = 0;
	const int JOYSTICK_BOTTOM = 1;
	const int STATE_PRESSING = 0;
	const int STATE_PRESSED = 1;
	const int STATE_RELEASING = 2;
	const int STATE_RELEASED = 3;
	const float ANALOG_DEADZONE = 0.19f;
	static Dictionary<string, float> mAxes = new Dictionary<string, float> ();
	static float mButtonTopPressure = 0.0f;
	static float mButtonTopState = STATE_RELEASED;
	private string 	axisHorizontal = "Horizontal",
		axisVertical = "Vertical",
		axisLookHorizontal = "LookHorizontal",
		axisLookVertical = "LookVertical";
	private Flitchio_Controller flitchioController;

	// Use this for initialization
	void Start ()
	{
		flitchioController = GameObject.Find ("FlitchioControllerManager").GetComponent<Flitchio_ControllerManager> ().oneFlitchioController;
		Debug.Log ("Flitchio_Input$Start()");
	}

	// Update is called once per frame
	void Update ()
	{
		AndroidJavaObject snapshot = flitchioController.obtainSnapshot ();

		mAxes [axisHorizontal] = flitchioController.getJoystickX (snapshot, JOYSTICK_BOTTOM);
		mAxes [axisVertical] = flitchioController.getJoystickY (snapshot, JOYSTICK_BOTTOM);

		mAxes [axisLookHorizontal] = flitchioController.getJoystickX (snapshot, JOYSTICK_TOP);
		mAxes [axisLookVertical] = flitchioController.getJoystickY (snapshot, JOYSTICK_TOP);

		// TODO also deal with BUTTON_BOTTOM
		mButtonTopState = flitchioController.getButtonState (snapshot, BUTTON_TOP);
		mButtonTopPressure = flitchioController.getButtonPressure (snapshot, BUTTON_TOP);
	}


	// ----------------------------------------
	// METHODS OF UnityEngine$Input "OVERRIDEN"
	// ----------------------------------------
	public static Vector3 acceleration {
		get{ return UnityEngine.Input.acceleration;}
	}

	public static int accelerationEventCount {
		get{ return UnityEngine.Input.accelerationEventCount;}
	}

	public static bool anyKey {
		get{ return UnityEngine.Input.anyKey;}
	}

	public static bool anyKeyDown {
		get{ return UnityEngine.Input.anyKeyDown;}
	}

	public static Compass compass {
		get{ return UnityEngine.Input.compass;}
	}

	public static string compositionString {
		get{ return UnityEngine.Input.compositionString;}
	}

	public static Vector2 compositionCursorPos {
		get{ return UnityEngine.Input.compositionCursorPos;}
	}

	public static DeviceOrientation deviceOrientation {
		get{ return UnityEngine.Input.deviceOrientation;}
	}

	public static Gyroscope gyro {
		get{ return UnityEngine.Input.gyro;}
	}

	public static IMECompositionMode imeCompositionMode {
		get{ return UnityEngine.Input.imeCompositionMode;}
		set{ UnityEngine.Input.imeCompositionMode = value;}
	}

	public static bool imeIsSelected {
		get{ return UnityEngine.Input.imeIsSelected;}
	}

	public static string inputString {
		get{ return UnityEngine.Input.inputString;}
	}

	public static Vector3 mousePosition {
		get{ return UnityEngine.Input.mousePosition;}
	}

	public static bool multiTouchEnabled {
		get{ return UnityEngine.Input.multiTouchEnabled;}
		set{ UnityEngine.Input.multiTouchEnabled = value;}
	}

	public static int touchCount {
		get{ return UnityEngine.Input.touchCount;}
	}

	public static Touch[] touches {
		get{ return UnityEngine.Input.touches;}
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static AccelerationEvent GetAccelerationEvent (int index)
	{
		return UnityEngine.Input.GetAccelerationEvent (index);
	}

	// Retrieves a Controllers axis. ------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static float GetAxis (string axisName)
	{
		float axisValue;
		if (mAxes.TryGetValue (axisName, out axisValue)) {
			if (Math.Abs (axisValue) > ANALOG_DEADZONE) {
				return axisValue *1.5f;
			}
		}
		return UnityEngine.Input.GetAxis (axisName);
	}

	// Retrieves a Controllers axis. ------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static float GetAxisRaw (string axisName)
	{
		float axisValue;
		if (mAxes.TryGetValue (axisName, out axisValue)) {
			if (Math.Abs (axisValue) > ANALOG_DEADZONE) {
				return axisValue*1.5f;
			}
		}
		return UnityEngine.Input.GetAxisRaw (axisName);
	}

	// Retrieves a Controllers button State -----------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetButton (string buttonName)
	{
		/*int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (buttonName, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.PRESSING:
				case ButtonState.PRESSED:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetButton (buttonName);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetButtonDown (string buttonName)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (buttonName, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.PRESSING:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetButtonDown (buttonName);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetButtonUp (string buttonName)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (buttonName, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.RELEASING:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetButtonUp (buttonName);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static string[] GetJoystickNames ()
	{
		//return UnityEngine.Input.GetJoystickNames ();
		return null;
	}


	// Detect Continuous Key Presses with KeyCode -----------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKey (KeyCode key)
	{
		if (key == KeyCode.Joystick1Button11) { // that's our button
			if (mButtonTopState == STATE_PRESSED || mButtonTopState == STATE_PRESSING) {
				return true;
			}
		}

		return UnityEngine.Input.GetKey (key);
	}

	// Detect Continuous Key Presses with String ------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKey (string name)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (name, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.PRESSING:
				case ButtonState.PRESSED:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetKey (name);
	}

	// Detect Single Key Press with KeyCode -----------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKeyDown (KeyCode key)
	{
		if (key == KeyCode.Joystick1Button11) { // that's our button
			if (mButtonTopState == STATE_PRESSING) {
				return true;
			}
		}

		return UnityEngine.Input.GetKeyDown (key);
	}

	// Detect Single Key Press with String ------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKeyDown (string name)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (name, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.PRESSING:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetKeyDown (name);
	}

	// Detect Key Release with KeyCode ----------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKeyUp (KeyCode key)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this Key exist? If so...
		if (buttonKeyCodes.TryGetValue (key, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.RELEASING:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetKeyUp (key);
	}

	// Detect Key Release with String -----------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetKeyUp (string name)
	{/*
		int buttonID;
		ButtonState buttonState;

		// Does this String exist? If so...
		if (buttonStrings.TryGetValue (name, out buttonID))
		{
			// Get Corrosponding buttonID from Moga Dictionary ...
			if (mogaButtons.TryGetValue(buttonID, out buttonState))
			{
				switch (buttonState)	// If Button State is Pressed or Pressing...
				{
				case ButtonState.RELEASING:
					return true;
				}
			}
		}*/
		return UnityEngine.Input.GetKeyUp (name);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetMouseButton (int button)
	{
		return UnityEngine.Input.GetMouseButton (button);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetMouseButtonDown (int button)
	{
		return UnityEngine.Input.GetMouseButtonUp (button);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static bool GetMouseButtonUp (int button)
	{
		return UnityEngine.Input.GetMouseButtonUp (button);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static Touch GetTouch (int index)
	{
		return UnityEngine.Input.GetTouch (index);
	}

	// ------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------
	public static void ResetInputAxes ()
	{
		foreach (string axisName in mAxes.Keys) {
			mAxes [axisName] = 0.0f;
		}

		UnityEngine.Input.ResetInputAxes ();
	}

	#if GPS_ENABLED && UNITY_ANDROID
	public static LocationService location
	{
		get{return UnityEngine.Input.location;}
	}
	#elif !GPS_ENABLED && UNITY_ANDROID
	public static LocationService location
	{
		get { Debug.LogError("Define GPS_ENABLED to use this property"); return null; }
	}
	#endif
}
