
.. _flitchio-ime:

Enable Flitchio compatibility without Flitchio SDK
---------------------------------------------------------

If you don't want to - or can't - develop with the Flitchio SDK, there's an alternate mode that uses IME.
In this mode, Flitchio acts like a USB/Bluetooth gamepad and inputs directly into the game, without the need of coding specific Flitchio support.
So if your game is designed to support existing gamepads, there's a chance that it will work with Flitchio too.

However, you should know that this mode is limited compared to Flitchio SDK.
The IME doesn't provide any pressure information for the buttons, and the joysticks are reduced to four directions (``UP``, ``DOWN``, ``LEFT`` and ``RIGHT``) instead of a full analogue input.
So you would easily gain in implementation time, but the user won't be able to enjoy the full Flitchio experience.

To enable Flitchio IME support for your game, the latter just needs to register a listener for the standard `Android KeyEvents <http://developer.android.com/reference/android/view/KeyEvent.html>`_.
That should be the case already if you have implemented support for other USB/Bluetooth gamepads.
KeyEvents are associated with a keycode that defines what identifies the key that is source of the event.
Your game reacts depending on this code and performs the appropriate action.

All you need to do is assign each control of Flitchio to a keycode.
You can map both shoulder buttons and every four directions of both joysticks to the keycodes that your game is expecting.
You can even assign two different Flitchio buttons to the same keycode, for example you could decide that both joysticks do the exact same action.
The resulting association of Flitchio controls to Android keycodes is called a mapping.

An example of mapping is as follows:

.. _fig-flitchio-ime:

.. figure:: img/flitchio_ime.png
    :alt: Example of mapping for Flitchio Compatibility Mode
    :align: center

    Example of mapping for Flitchio Compatibility Mode

We're working on a system that will enable any developer to register new mappings for their games, and share them directly with the Flitchio community.
Until then, send us an email at devs@flitch.io, and we'll help you implement the mapping to add an entry to Flitchio Game Centre.

Keep in mind that the users of Flitchio expect to have pressure information, and expect to use the joysticks fully.
Your app will greatly profit from the integration of the Flitchio SDK in addition to existing gamepad support.
