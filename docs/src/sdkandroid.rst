
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

Get an instance of :javaref:`FlitchioController` with :javaref:`getInstance() <FlitchioController#getInstance(Context)>`.
The context you pass can be either an Activity or a Service: :javaref:`FlitchioController` can be used by both.
Whichever context you use, you **must** tie the controller to your context lifecycle by calling these 4 methods at appropriate moments:

* :javaref:`onCreate() <FlitchioController#onCreate(FlitchioStatusListener)>` to initialise the controller and bind to the Flitchio Manager app.
* :javaref:`onResume() <FlitchioController#onResume()>` to start monitoring status changes.
* :javaref:`onPause() <FlitchioController#onPause()>` to pause the monitoring of status changes.
* :javaref:`onDestroy() <FlitchioController#onDestroy()>` to terminate the controller properly.

If you don't call all of these 4 methods, your app may behave incorrectly.

Here is the typical initial setup for an Activity::

    public class MainActivity extends Activity {

        private FlitchioController flitchioController;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            flitchioController = FlitchioController.getInstance(this);
            flitchioController.onCreate(statusListener /* see below */);
        }
        
        protected void onResume() {
            super.onResume();
            
            flitchioController.onResume();
        }
        
        protected void onPause() {
            flitchioController.onPause();
            
            super.onPause();
        }

        protected void onDestroy() {
            flitchioController.onDestroy();
            
            super.onDestroy();
        }
    }

And here is the typical initial setup for a Service::

    public class BackgroundService extends Service {

        private FlitchioController flitchioController;
        private FlitchioStatusListener statusListener;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            flitchioController = FlitchioController.getInstance(this);
            flitchioController.onCreate(statusListener /* see below */);
            flitchioController.onResume();
        }

        public void onDestroy() {
            flitchioController.onPause();
            flitchioController.onDestroy();
            
            super.onDestroy();
        }
    }

The typical implementation of the ``statusListener`` is explained in :ref:`follow-status-changes`.

.. _follow-status-changes:

Follow status changes
^^^^^^^^^^^^^^^^^^^^^

Because the controller initialisation is asynchronous, you need to register a :javaref:`FlitchioStatusListener` to follow its status changes.
Your status listener will get a status callback at every important moment in the lifetime of your controller, such as:

* :javaref:`Status#BINDING` when you asked for a binding but it hasn't completed yet. This is usually the first status callback.
* :javaref:`Status#BOUND` when your binding has completed.
* :javaref:`Status#CONNECTED` when your binding is complete and Flitchio is connected to the phone. This is the moment you can start :ref:`getting data from Flitchio<get-data>`.
* :javaref:`Status#DISCONNECTED` when your binding is complete but Flitchio is disconnected from the phone. You can for example display a message to your user asking them to connect Flitchio to the phone.
* :javaref:`Status#BINDING_FAILED` when your controller couldn't bind. This can happen for several reasons, read :ref:`failure-reasons` for details.

Check the :javaref:`Status` API reference for an overview for all the possible statuses.

Here's a typical implementation of the status listener::

    flitchioController.onCreate(new FlitchioStatusListener() {
        @Override
        public void onFlitchioStatusChanged(Status status) {
            if (status.code == Status.BOUND) {
                // Yay, binding is effective!

            } else if (status.code == Status.CONNECTED) {
                startGettingDataFromFlitchio();

            } else if (status.code == Status.DISCONNECTED) {
                askUserToConnectFlitchio();

            } else if (status.code == Status.BINDING_FAILED) {
                int failureReason = status.failureReason;

                // See section "Troubleshoot the reasons of a failed binding"

            } else {
                Log.v("Not reacting on status: "+status);
            }
        }
    });


Note: in this example, the status listener is implemented as an anonymous class, but you can of course let your Activity/Service implement :javaref:`FlitchioStatusListener` or create another class instead.

If you don't care whether or not your controller gets bound and do not wish to follow the connection status of Flitchio, you can pass null as argument of :javaref:`FlitchioController#onCreate(FlitchioStatusListener)`.

In addition to the status callbacks, you can also check your controller's status at any time by calling :javaref:`FlitchioController#getStatus()`.


.. _get-data:

Get data from Flitchio
^^^^^^^^^^^^^^^^^^^^^^

As soon as your controller is in :javaref:`Status#CONNECTED`, you are able to get data from Flitchio.
There are two ways of accessing this data:

* Either your app implements an **event listener** to receive button and direction events (newly pressed or released, joystick moving, pressure variations).
* Or your app actively **polls** and repeatedly requests the newest state of Flitchio (pressure applied on the buttons/directions and coordinates of the joysticks at a given moment).

Whether you listen or poll is entirely your design decision.
Listening mode is simpler to implement at first sight and more suitable for standard Android apps.
Indeed, listening for events is a concept widely used in the basic Android framework itself.
On the other hand, polling mode is often useful for apps designed to have a rendering loop updating the display at high frequency.
That's typically the case of Android games that use a SurfaceView or a GLSurfaceView to draw.

.. _listening-mode:

Listening Mode
""""""""""""""

If you want to listen for incoming events, all you need to do is implement a :javaref:`FlitchioEventListener` and pass it in :javaref:`onResume(FlitchioEventListener) <FlitchioController#onResume(FlitchioEventListener)>` instead of using the simple version with no parameter.

Here is an example of a typical controller lifecycle in listening mode for button and joystick events::

    public class MainActivity extends Activity {

        private FlitchioController flitchioController;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            flitchioController = FlitchioController.getInstance(this);

            flitchioController.onCreate(statusListener);
        }

        @Override
        protected void onResume() {
            super.onResume();

            flitchioController.onResume(eventListener /* see below */);
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


Once the controller is bound to Flitchio Manager, Flitchio is connected, and the :javaref:`FlitchioEventListener` is registered, you will receive:

* a :javaref:`ButtonEvent` whenever the user presses or releases a button/direction of Flitchio, or varies the pressure;
* a :javaref:`JoystickEvent` whenever the user moves a joystick of Flitchio.

Here's a typical implementation of the event listener::

    flitchioController.onResume(new FlitchioEventListener() {
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
    });

Note: in this example, the event listener is implemented as an anonymous class, but you can of course let your Activity/Service implement :javaref:`FlitchioEventListener` or create another class instead.

These event callbacks, by default, are executed on an arbitrary thread different from the main thread.
To define on which thread you want to receive the event callbacks, please check :ref:`define-thread-callbacks`.

.. _polling-mode:

Polling Mode
""""""""""""

When in polling mode, your app actively asks for the current state of Flitchio.
This state is represented by a :javaref:`FlitchioSnapshot` object that contains information about all the pressed buttons and all the joystick positions at a given moment.
In each iteration of your game/rendering loop, call :javaref:`obtainSnapshot() <FlitchioController#obtainSnapshot()>` to get the latest state of Flitchio.

For the sake of the example, let's assume that your display is continuously updated in a method called ``update()``.
You would then be able to query the current state like this::

    void update() {

        if (flitchioController != null && flitchioController.getStatus().code == Status#CONNECTED) {
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


**Important note:** after calling :javaref:`onCreate() <FlitchioController#onCreate(FlitchioStatusListener)>`, you can't immediately start polling, because your controller is still initialising.
Polling is possible as soon as you get a status callback with :javaref:`Status#CONNECTED`.

Troubleshooting & Best practices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Debug logs
""""""""""

The :javaref:`FlitchioController` outputs debug information if something goes wrong, for example when the connection with the Flitchio Manager app breaks.
Those logs are identified by the tag ``Flitchio``.

If you remove logging in your app with Proguard, the :javaref:`FlitchioController` will not log either.

.. _failure-reasons:

Troubleshoot the reasons of a failed binding
""""""""""""""""""""""""""""""""""""""""""""

If you get a status callback with :javaref:`Status#BINDING_FAILED`, it means that either a binding attempt hasn't succeeded, or the existing binding has ended unexpectedly.
You can troubleshoot the reason of this failure by checking the :javaref:`Status#failureReason` field of this status.
Here are the most common reasons:

* :javaref:`Status.FailingStatus#REASON_MANAGER_UNUSABLE`: the user doesn't have the Flitchio Manager app installed on their phone, or they have an outdated version. In both cases, they need to download the latest version from the Play Store. :javaref:`FlitchioController#getPlayStoreIntentForFlitchioManager()` can help you for that. Alternatively, you can check the version of the SDK with :javaref:`FlitchioController#getVersionCode()` and the version of Flitchio Manager on the user's phone with :javaref:`FlitchioController#getFlitchioManagerVersionCode(Context)`.
* :javaref:`Status.FailingStatus#REASON_SERVICE_UNREACHABLE`: the Flitchio Manager app cannot be reached for an unexpected reason.
* :javaref:`Status.FailingStatus#REASON_SERVICE_REFUSED_CONNECTION`: this controller has not been accepted by Flitchio Manager.
* :javaref:`Status.FailingStatus#REASON_SERVICE_SHUTDOWN_CONNECTION`: the binding with Flitchio Manager ended unexpectedly. Try to restart the app.

.. _define-thread-callbacks:

Receive event callbacks on a particular thread
""""""""""""""""""""""""""""""""""""""""""""""

By default, all the callback methods of :javaref:`FlitchioEventListener` are executed on an arbitrary non-UI thread, different from the main thread.
This can be problematic if you try to do UI operations in those callbacks, such as updating Views: your app will crash.
You can change the default behaviour by passing to :javaref:`FlitchioController#onResume(FlitchioEventListener, Handler)` a reference to a Handler object associated to the thread you want to receive the callbacks in.

In particular, if you want to receive these callbacks on the UI thread, you would do::

    @Override
    protected void onResume() {
        super.onResume();

        flitchioController.onResume(eventListener, new Handler());
    }

    @Override
    protected void onPause() {
        flitchioController.onPause();

        super.onPause();
    }

Note that the status callbacks of :javaref:`FlitchioStatusListener` will always happen on the main thread.

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
            flitchioController.onResume();

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
