using UnityEngine;
using System.Collections;

public class Flitchio_ControllerManager : MonoBehaviour
{

	public Flitchio_Controller oneFlitchioController;

	void Awake ()
	{
		Debug.Log ("Flitchio_ControllerManager$Awake()");
		if (oneFlitchioController == null) {
			DontDestroyOnLoad (transform.gameObject);

			oneFlitchioController = new Flitchio_Controller ();
		}

		oneFlitchioController.onCreate ();
	}

	void OnDestroy ()
	{
		Debug.Log ("Flitchio_ControllerManager$OnDestroy()");
		if (oneFlitchioController != null) {
			oneFlitchioController.onDestroy ();
		}
	}


}
