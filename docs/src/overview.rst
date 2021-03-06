
Getting started
---------------

`Flitchio <https://flitch.io/>`_ is a new kind of controller that is attached to the back of an Android smartphone.
It uses NFC to communicate with the host device and may be used for a variety of applications especially in gaming.
The Flitchio SDK provides you with everything you need to build apps enhanced by `Flitchio <https://flitch.io/>`_.

This page is intended for app developers only.
It guides you through the installation and the integration of the Flitchio SDK into your project.

Note that if your game already supports USB/Bluetooth game controllers, and you want to add basic support for Flitchio, you might not need to implement anything.
See :ref:`flitchio-ime` for further information.

If you're the happy owner of a Flitchio, but you're still looking for an innovative use case to develop, :ref:`here <use-cases>` are some ideas.

Overview of the device
^^^^^^^^^^^^^^^^^^^^^^

.. _fig-flitchio-device:

.. figure:: img/flitchio_device.png
    :alt: Flitchio seen from the back of the phone
    :align: center

    Flitchio seen from the back of the phone

Flitchio comes as a smartphone case that adds interactive controls at the back of the phone.

It has two **shoulder buttons** which are clickable and sensitive to pressure variations.
Through the Flitchio SDK, you can receive press and release events of the buttons and measure the pressure applied by the user.

* The button called ``BUTTON_TOP`` is the one naturally at the top when you hold your phone in portrait. When you rotate your phone to landscape to handle Flitchio, ``BUTTON_TOP`` is under your *right* hand.
* The button called ``BUTTON_BOTTOM`` is the one naturally at the bottom when you hold your phone in portrait. When you rotate your phone to landscape to handle Flitchio, ``BUTTON_BOTTOM`` is under your *left* hand.

Flitchio also has two analogue **joysticks**.
Through the Flitchio SDK, you can receive events when each of the joystick moves, and you can poll the current position of each joystick at all times.

* The joystick called ``JOYSTICK_TOP`` is the one naturally at the top when you hold your phone in portrait. When you rotate your phone to landscape to handle Flitchio, ``JOYSTICK_TOP`` is under your *right* hand.
* The button called ``JOYSTICK_BOTTOM`` is the one naturally at the bottom when you hold your phone in portrait. When you rotate your phone to landscape to handle Flitchio, ``JOYSTICK_BOTTOM`` is under your *left* hand.

For each joystick are also defined virtual buttons for the four directions (up, down, left and right).
These virtual buttons are called **D-Pad buttons** (for "directional pad") and they act just like shoulder buttons in the sense that you can receive press and release events for each them.
However, joysticks don't support pressure-sensitivity so the pressure of the D-Pad buttons will always be 0.5 when pressed.
You have eight D-Pad buttons in total:

* ``DPAD_BOTTOM_UP`` for the up direction of ``JOYSTICK_BOTTOM``,
* ``DPAD_BOTTOM_DOWN`` for the down direction of ``JOYSTICK_BOTTOM``,
* ``DPAD_BOTTOM_LEFT`` for the left direction of ``JOYSTICK_BOTTOM``,
* ``DPAD_BOTTOM_RIGHT`` for the right direction of ``JOYSTICK_BOTTOM``,
* ``DPAD_TOP_UP`` for the up direction of ``JOYSTICK_TOP``,
* ``DPAD_TOP_DOWN`` for the down direction of ``JOYSTICK_TOP``,
* ``DPAD_TOP_LEFT`` for the left direction of ``JOYSTICK_TOP``,
* ``DPAD_TOP_RIGHT`` for the right direction of ``JOYSTICK_TOP``.

The Flitchio Manager app automatically handles the orientation of the phone so that the user experience is always consistent: e.g. whether your app works in portrait or landscape, the left direction is always the natural left direction. You don't have to worry about it.


Communicate with Flitchio
^^^^^^^^^^^^^^^^^^^^^^^^^

The Flitchio Manager Android app is required in order to connect to Flitchio.
Through the Flitchio SDK, your app will bind to Flitchio Manager to access the device.

If you possess Flitchio, you probably already have downloaded the Flitchio Manager app.
If you haven't, `go to the Play Store and install it <https://play.google.com/store/apps/details?id=com.supenta.flitchio.manager>`_.

Before building your app, you'll need to understand the general architecture of Flitchio apps.
The diagram below pictures the interactions between your app, the Flitchio Manager app and Flitchio itself:

.. _fig-software-architecture:

.. figure:: img/software_architecture.png
    :alt: Overall software architecture
    :align: center

    Overall software architecture

Flitchio communicates through NFC with the mobile device.
The `Flitchio Manager <https://play.google.com/store/apps/details?id=com.supenta.flitchio.manager>`_ app is absolutely required on the user's phone.
It receives data from the device and dispatches it to your client app.
