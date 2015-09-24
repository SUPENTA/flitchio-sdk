
.. javaimport::
    com.supenta.flitchio.sdk.*

Flitchio SDK for Android
------------------------

Add the SDK to your project
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Make sure that the `Android SDK <http://developer.android.com/sdk/index.html>`_ in correctly installed on your computer before reading on.

The Flitchio SDK is very lightweight and simple.
To integrate it to your project, just add the dependency to your build.gradle:

.. parsed-literal::

    dependencies {
        compile 'com.supenta.flitchio:sdk:|version|'
    }

Refresh Gradle, and you're done.
You can now import and use the `classes from the Flitchio SDK <http://dev.flitch.io/javadoc/>`_.

Bind to Flitchio
^^^^^^^^^^^^^^^^

As depicted in the picture :ref:`fig-software-architecture`, your app does not interact directly with Flitchio but rather gets its data from the Flitchio Manager app which acts like a bridge.
Whenever your app wants to gather input data from Flitchio, it needs to bind to the Flitchio Manager app using an instance of :javaref:`FlitchioController`.
After binding, you may either listen to new events (:ref:`listening mode <listening-mode>`) or poll for new data (:ref:`polling mode <polling-mode>`).
Whichever mode you choose, the initial setup is the same.

Get an instance of :javaref:`FlitchioController` and call :javaref:`onCreate() <FlitchioController#onCreate()>` to initialise the controller and bind to the Flitchio Manager app.
The context you pass to :javaref:`getInstance() <FlitchioController#getInstance(Context)>` can be either an Activity or a Service: :javaref:`FlitchioController` can be used by both.
When you don't need your :javaref:`FlitchioController` any more, call :javaref:`onDestroy() <FlitchioController#onDestroy()>` to terminate it properly.

Generally, you should initialise your controller in the ``onCreate()`` method of your Activity/Service.
Likewise, you should terminate it in the ``onDestroy()`` method of your Activity/Service.

Here is an example of a typical initialise/terminate cycle::

    public class MainActivity extends Activity {

        private FlitchioController flitchioController;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            flitchioController = FlitchioController.getInstance(this);

            try {
                flitchioController.onCreate();
            } catch (final FlitchioManagerDependencyException e) {
                // Flitchio Manager not found (see Troubleshooting section)
            }
        }

        @Override
        public void onDestroy() {
            flitchioController.onDestroy();

            super.onDestroy();
        }
    }


Get data from Flitchio
^^^^^^^^^^^^^^^^^^^^^^

The code displayed above enables your :javaref:`FlitchioController` instance to access or receive data from Flitchio Manager.
There are two ways of accessing this data:

* Either your app implements a **status listener** to receive events whenever the connection state of Flitchio changes and/or an **event listener** to receive button and direction events (newly pressed or released, joystick moving, pressure variations).
* Or your app actively **polls** and repeatedly requests the newest state of Flitchio (pressure applied on the buttons/directions and coordinates of the joysticks at a given moment).

Whether you listen or poll is entirely your design decision.
Listening mode is simpler to implement at first sight and more suitable for standard Android apps.
Indeed, listening for events is a concept widely used in the basic Android framework itself.
On the other hand, polling mode is often useful for apps designed to have a rendering loop updating the display at high frequency.
That's typically the case of Android games that use a SurfaceView or a GLSurfaceView to draw.

Please note that listening and polling mode can work together without any problem.
You could for example poll data in a rendering loop while listening for connection status changes.

.. _listening-mode:

Listening Mode
""""""""""""""

When you want to listen for incoming events, all you need to do is implement either one or both of the listener interfaces.

The two listeners are:

* :javaref:`FlitchioStatusListener` which is used for listening to the connection state of Flitchio (whether it connected or disconnected).
* :javaref:`FlitchioEventListener` which is used for listening to button and joystick events from Flitchio (top button was pressed, bottom joystick has pressure of 0.3 etc.)

After implementing either of them you need to:

* Call :javaref:`onResume() <FlitchioController#onResume(FlitchioStatusListener, FlitchioEventListener, Handler)>` to register your listener(s) and start receiving events. You can pass `null` for the listener that you do not wish to register.
* Call :javaref:`onPause() <FlitchioController#onPause()>` to unregister your listener(s) and stop the stream of events.

If you use your :javaref:`FlitchioController` in an Activity, you should register your listeners and unregister them respectively in the ``onResume()`` and ``onPause()`` methods of your Activity.
If you don't unregister, your Activity will receive data from Flitchio Manager even when it's in the background.
This may lead to inconsistent behaviour and **should be avoided at all times**.

If you use your :javaref:`FlitchioController` in a Service, you can register your listeners right after calling :javaref:`onCreate() <FlitchioController#onCreate()>` and unregister them right before calling :javaref:`onDestroy() <FlitchioController#onDestroy()>`.

Here is an example of a typical controller lifecycle which listens only for status (connection and disconnection) events::

    public class MainActivity extends Activity {

        private FlitchioController flitchioController;
        private FlitchioStatusListener flitchioStatusListenerImpl;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            flitchioController = FlitchioController.getInstance(this);
            flitchioStatusListenerImpl = new FlitchioStatusListenerImpl(); // see below

            try {
                flitchioController.onCreate();
            } catch (final FlitchioManagerDependencyException e) {
                // Flitchio Manager not found (see Troubleshooting section)
            }
        }

        @Override
        protected void onResume() {
            super.onResume();

            flitchioController.onResume(flitchioStatusListenerImpl, null); // event listener is not needed
        }

        @Override
        protected void onPause() {
            flitchioController.onPause();

            super.onPause();
        }

        @Override
        protected void onDestroy() {
            flitchioController.onDestroy();

            super.onDestroy();
        }
    }


Here is an example of a typical controller lifecycle in listening mode for both status and button/joystick events::

    public class MainActivity extends Activity {

        private FlitchioController flitchioController;
        private FlitchioStatusListener flitchioStatusListenerImpl;
        private FlitchioEventListener flitchioEventListenerImpl;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            flitchioController = FlitchioController.getInstance(this);
            flitchioStatusListenerImpl = new FlitchioStatusListenerImpl(); // see below
            flitchioEventListenerImpl = new FlitchioEventListenerImpl(); // see below

            try {
                mFlitchioController.onCreate();
            } catch (final FlitchioManagerDependencyException e) {
                // Flitchio Manager not found (see Troubleshooting section)
            }
        }

        @Override
        protected void onResume() {
            super.onResume();

            flitchioController.onResume(flitchioStatusListenerImpl, flitchioEventListenerImpl);
        }

        @Override
        protected void onPause() {
            flitchioController.onPause();

            super.onPause();
        }

        @Override
        protected void onDestroy() {
            flitchioController.onDestroy();

            super.onDestroy();
        }
    }


Once the controller is bound to Flitchio Manager and the :javaref:`FlitchioStatusListener` is registered, you will receive the connection status of Flitchio whenever it changes.

Here is an example of what you can do with the received status event::

    public class FlitchioStatusListenerImpl implements FlitchioStatusListener {
        @Override
        public void onFlitchioStatusChanged(boolean isConnected) {
            if (isConnected) {

            } else {

            }
        }
    }


Once the controller is bound to Flitchio Manager and the :javaref:`FlitchioEventListener` is registered, you will receive:

* a :javaref:`ButtonEvent` whenever the user presses or releases a button/direction of Flitchio, or varies the pressure;
* a :javaref:`JoystickEvent` whenever the user moves a joystick of Flitchio.

Here is an example of what you can do with the received events::

    public class FlitchioEventListenerImpl implements FlitchioEventListener {
        @Override
        public void onFlitchioButtonEvent(InputElement.Button source, ButtonEvent event) {
            if (event.getAction() == ButtonEvent.ACTION_DOWN) {
                // The source button has just been pressed
            }

            float pressure = event.getPressure();
            if (pressure > 0.5f) {
                // The source button is being pressed strongly
            }

            if (source == InputElement.BUTTON_TOP) {
                // The top shoulder button was pressed, released, or its pressure has changed
            } else if (source == InputElement.DPAD_BOTTOM_LEFT) {
                // The left direction on bottom joystick was pressed or released
            }
        }

        @Override
        public void onFlitchioJoystickEvent(InputElement.Joystick source, JoystickEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (source == InputElement.JOYSTICK_TOP) {
                // The top joystick has moved
            }
        }
    }


**Important note:** these listener callbacks, by default, are executed on an arbitrary thread different from the main thread.
To define on which thread you want to receive these callbacks, please check :ref:`define-thread-callbacks`.


.. _polling-mode:

Polling Mode
""""""""""""

When in polling mode, your app actively asks for the current state of Flitchio.
This state is represented by a :javaref:`FlitchioSnapshot` object that contains information about all the pressed buttons and all the joystick positions at a given moment.
In each iteration of your game/rendering loop, call :javaref:`isConnected() <FlitchioController#isConnected()>` to check the connection status of Flitchio and call :javaref:`obtainSnapshot() <FlitchioController#obtainSnapshot()>` to get the latest state of Flitchio.

For the sake of the example, let's assume that your display is continuously updated in a method called ``update()``.
You would then be able to query the current state like this::

    void update() {

        if (flitchioController != null && flitchioController.isConnected()) {
            // Retrieve the current state of Flitchio
            FlitchioSnapshot snapshot = flitchioController.obtainSnapshot();

            if (snapshot.getJoystickX(JOYSTICK_TOP) == 0.0f &&
                snapshot.getJoystickY(JOYSTICK_TOP) == 0.0f) {
                // The top joystick is in central position
            }

            if (snapshot.getButtonPressure(BUTTON_BOTTOM) > 0.5f) {
                // The bottom shoulder button is being pressed strongly
            }
        }
    }


**Important note:** after calling :javaref:`onCreate() <FlitchioController#onCreate()>`, you can't immediately start polling, because your :javaref:`FlitchioController` is still initialising.
Polling is possible from the moment the binding gets effective, i.e. from the first :javaref:`onFlitchioStatusChanged() <FlitchioStatusListener#onFlitchioStatusChanged(boolean)>` callback.

Please read :ref:`know-when-to-poll` for further details.


Troubleshooting & Best practices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Debug logs
""""""""""

The :javaref:`FlitchioController` outputs debug information if something goes wrong, for example when the connection with the Flitchio Manager app breaks.
Those logs are identified by the tag ``Flitchio``.

If you remove logging in your app with Proguard, the :javaref:`FlitchioController` will not log either.


Deal with Flitchio Manager connection error
"""""""""""""""""""""""""""""""""""""""""""

The Flitchio Manager app is required on the user's system to enable 3\ :sup:`rd`\ -party apps (like yours) to use Flitchio.
If it's not installed, or if the version of Flitchio Manager installed is older than the version of the Flitchio SDK you are developing with, the :javaref:`onCreate() <FlitchioController#onCreate()>` method will throw an exception.
You can handle it, by disabling Flitchio functionalities for your app, or preferably by redirecting your users to the Play Store to download Flitchio Manager::

    try {
        flitchioController.onCreate();
    } catch (final FlitchioManagerDependencyException e) {
        // Start activity to update FlitchioManager
        startActivity(FlitchioController.getPlayStoreIntentForFlitchioManager());

        // Ask your user to restart your app now in order to re-init the controller
        // correctly.
    }

.. _know-when-to-poll:

Know when to poll data from Flitchio
""""""""""""""""""""""""""""""""""""

Because you need Android to initialise the service connection, you can't poll data from Flitchio right after :javaref:`onCreate() <FlitchioController#onCreate()>` returns true.
If you poll while the binding is not effective, :javaref:`isConnected() <FlitchioController#isConnected()>` will always return false and :javaref:`obtainSnapshot() <FlitchioController#obtainSnapshot()>` will always return an empty snapshot.
To be notified as soon as the binding gets effective to be able to start polling, you should register a :javaref:`FlitchioStatusListener` and implement :javaref:`onFlitchioStatusChanged() <FlitchioStatusListener#onFlitchioStatusChanged(boolean)>`.
Only from the moment that method is called you can poll data about the real Flitchio state.
The callback happens very shortly (it's a matter of milliseconds) after your Activity or Service is initialised (i.e. after the sequence ``onCreate()`` - ``onStart()`` - ``onResume()``).

See :ref:`listening-mode` to understand how to declare a :javaref:`FlitchioStatusListener`.

Here's an example by code::

    public class MainActivity extends Activity implements FlitchioStatusListener {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            flitchioController = FlitchioController.getInstance(this);

            try {
                flitchioController.onCreate();
            } catch (final FlitchioManagerDependencyException e) {
                // ...
            }

            // flitchioController.obtainSnapshot() is invalid here!
        }

        @Override
        protected void onResume() {
            flitchioController.onResume(this, null);

            // flitchioController.obtainSnapshot() is invalid here!
        }

        @Override
        public void onFlitchioStatusChanged(boolean isConnected) {
            // Binding valid from here!

            if (isConnected) {
                FlitchioSnapshot validSnapshot = flitchioController.obtainSnapshot();
            } else {
                // Binding is valid but Flitchio is disconnected
            }
        }
    }


.. _define-thread-callbacks:

Receive listener callbacks on a particular thread
"""""""""""""""""""""""""""""""""""""""""""""""""

By default, all the callback methods of :javaref:`FlitchioStatusListener` and :javaref:`FlitchioEventListener` are executed on an arbitrary non-UI thread, different from the main thread.
This can be problematic if you try to do UI operations in those callbacks, such as updating Views: your app will crash.
You can change the default behaviour by passing to :javaref:`onResume() <FlitchioController#onResume(FlitchioStatusListener, FlitchioEventListener, Handler)>` a reference to a Handler object associated to the thread you want to receive the callbacks in.

In particular, if you want to receive these callbacks on the UI thread, you would do::

    @Override
    protected void onResume() {
        super.onResume();

        flitchioController.onResume(this, this, new Handler());
    }

    @Override
    protected void onPause() {
        flitchioController.onPause();

        super.onPause();
    }


Keep your screen on while using Flitchio
""""""""""""""""""""""""""""""""""""""""

Remember that **NFC is turned off as long as the screen is off**.
This should not bother you, though.
When the screen is turned on again, Flitchio is automatically detected again and your app reconnects without any problem.

However, keep in mind that bare NFC communication does not prevent the touchscreen from turning off automatically when the user didn't touch it.
If your app solely depends on input from Flitchio, i.e. the user doesn't use the touchscreen, make sure to tell Android not to turn off the screen::

    public class MainActivity extends Activity {

        @Override
        protected void onResume() {
            flitchioController.onResume(...);

            keepScreenOn(true);
        }

        @Override
        protected void onPause() {
            keepScreenOn(false);

            flitchioController.onPause();
        }

        private void keepScreenOn(boolean keepScreenOn) {
            if (getWindow() != null) {
                if (keepScreenOn) {
                    // Tell Android to keep the screen on while this window is visible to
                    // the user.
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    // Release window flag to make screen turn off if needed
                    // (this has no effect if flag wasn't set).
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        }
    }

There are other possibilities to keep the screen on. Check out this `answer <http://stackoverflow.com/a/18487237/2923406>`_ on StackOverflow.
