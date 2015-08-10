To use this Unity plugin in an existing Unity project, you need to:

- Copy the AAR archive of the Flitchio SDK in the folder Assets\Plugins\Android.
- Copy the whole Assets folder to the Assets folder of your game (SDK and scripts).
- Add a GameObject named FlitchioControllerManager and link the Flitchio_ControllerManager script to it.
- Attach the Flitchio_Input script to the GameObject you want to control.

Further tuning depend on your game.
Basically you need to replace the default input system by the Flitchio input system.
A sample game is available: it is a mod of the Angry Bots Unity demo for Flitchio.
