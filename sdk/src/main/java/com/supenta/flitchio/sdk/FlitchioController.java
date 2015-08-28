package com.supenta.flitchio.sdk;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Object providing the main communication channel to the Flitchio Manager app. This is the most
 * important component of the Flitchio SDK, and the only object you have to manipulate in order to
 * use Flitchio with your app.
 * <p>
 * The controller binds to the Flitchio Manager app when you call {@link #onCreate()}. You need the
 * Flitchio Manager app to be able to use Flitchio, otherwise a
 * {@link FlitchioManagerDependencyException} will be thrown. When {@link #onCreate()} returns true,
 * the binding is going to be effective soon in the future, but you can't use {@link #isConnected()}
 * or {@link #obtainSnapshot()} yet. You can listen to the moment the binding gets effective by
 * implementing {@link FlitchioListener#onFlitchioStatusChanged(boolean)}. To free the controller
 * properly, you must call {@link #onDestroy()}.
 * <p>
 * After initialisation and as soon as this controller is bound to the Flitchio Manager app, you can
 * check if Flitchio is attached to the phone by calling {@link #isConnected()} and poll data from
 * it by requesting a {@link FlitchioSnapshot} of its state with {@link #obtainSnapshot()}. You
 * typically call {@link #obtainSnapshot()} if you want to use your controller in
 * <em>polling mode</em>, i.e. if your app is designed to have a rendering loop updating the display
 * at high frequency. That's typically the case for games that use a {@link SurfaceView} or a
 * {@link GLSurfaceView}.
 * <p>
 * If you don't want to actively poll data from Flitchio, but rather receive events every time
 * something has changed on the device, you can use your controller in <em>listening mode</em>. To
 * do so, simply register a {@link FlitchioListener} with {@link #onResume(FlitchioListener)} and
 * unregister it with {@link #onPause()}. You will then receive {@link ButtonEvent}s for button
 * presses/releases and {@link JoystickEvent}s for joystick position updates, as well as updates
 * about the connection status of Flitchio.
 *
 * @since 0.5.0
 */
public class FlitchioController {

    private static final String FLITCHIO_MANAGER_PACKAGE = "com.supenta.flitchio.manager";
    private static final String FLITCHIO_SERVICE_CLASS =
            FLITCHIO_MANAGER_PACKAGE + ".communication.FlitchioService";
    private static final int INVALID_AUTH_TOKEN = -1;
    /**
     * Map of per-{@link Context} existing {@link FlitchioController}s. Using {@link WeakReference}
     * to not leak memory when contexts have to be destroyed by the system.
     */
    private static final Map<WeakReference<Context>, FlitchioController> controllers = new HashMap<>();
    /**
     * Interface to this client passed to FlitchioService to identify this client and to allow
     * callbacks.
     */
    private final IFlitchioClient clientStub = new IFlitchioClientStub();
    /**
     * Locks used to synchronise the 3 threads: the main one where FlitchioController is created
     * (most probably UI thread), the anonymous callback thread on which the service does his
     * callbacks, and the listener thread defined by the user where the callbacks will happen.
     */
    private final Object lockListener = new Object();
    private final Object lockService = new Object();
    /**
     * The context used to bind.
     */
    private final Context context;
    /**
     * Received from service once the handshake has been done. Used for every further communication.
     */
    private int authToken = INVALID_AUTH_TOKEN;
    /**
     * Variable used to follow the flow of the Activity. Its value is updated correctly if the
     * 3rd-party dev does the appropriate callbacks.
     */
    private ActivityLifecycle activityLifecycleMoment = ActivityLifecycle.UNDEFINED;
    /**
     * Interface to FlitchioService.
     */
    private IFlitchioService flitchioService = null;
    /**
     * The listener to be called on receiving data and disconnected events. Known limitation: there
     * can be only one listener per controller, and only one controller per context.
     */
    private FlitchioListener listener = null;
    /**
     * The thread to which the listener callbacks will be delivered (used by default), or the
     * handler associated to the thread decided by the 3rd-party dev.
     */
    private ListenerThread listenerThread = null;
    private Handler listenerHandler = null;
    /**
     * The {@link ComponentName} for this context, used to identify this client in Flitchio Service.
     */
    private ComponentName clientId = null;
    /**
     * Listener object for the binding to the service, that detects when the binding is done and
     * when an unexpected disconnection occurred. The methods here are ALWAYS CALLED ON UI THREAD.
     * Meaning that a binding will be effective ONLY after onCreate(), onStart(), onResume().
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            /* Executed on the same thread as onResume */
            synchronized (lockService) {
                flitchioService = IFlitchioService.Stub.asInterface(service);
                try {
                    authToken = flitchioService.receiveClientInfo(clientId);
                    if (authToken == INVALID_AUTH_TOKEN) {
                        FlitchioLog.e("Unexpected error: could not authenticate to service.");
                    }

                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error: could not identify this controller.");
                }
            }

            // We connect the client in case he asked for it while binding was not ready. The client
            // should not call onResume() if its listener is null, but in case he did, we don't
            // register the client for a null Listener.
            if (activityLifecycleMoment == ActivityLifecycle.ON_RESUME && listener != null) {
                registerClient();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            FlitchioLog.e(
                    "Unexpected error: this controller has been unbound from Flitchio Manager.");

            synchronized (lockService) {
                flitchioService = null;
            }

            resetListener();

            synchronized (lockListener) {
                if (listenerHandler != null) {
                    listenerHandler.post(new StatusRunnable(false /* isConnected */));
                }
            }
        }
    };

    /**
     * @param context A valid context, ensured to be non-null.
     */
    private FlitchioController(Context context) {
        this.context = context;
        clientId = new ComponentName(context, context.getClass());
    }

    /**
     * Get an instance of {@link FlitchioController} for this {@link Context}.
     *
     * @param context The context used to bind. It should be the Activity / Service, not the
     *                Application's context.
     * @return An instance of {@link FlitchioController}.
     * @since 0.5.0
     */
    public static synchronized FlitchioController getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }

        // Check if there's already a controller for this context
        for (Map.Entry<WeakReference<Context>, FlitchioController> entry : controllers.entrySet()) {
            if (entry.getKey().get() == context) {
                // Means that:
                // 1. The context is still valid (context in map != null, i.e. not destroyed by the
                //    system)
                // 2. A FlitchioController already has been declared for this context
                return entry.getValue();
            }
        }

        // Otherwise create a new Controller, remember it and return it
        FlitchioController controller = new FlitchioController(context);
        controllers.put(new WeakReference<>(context), controller);

        return controller;
    }

    /**
     * Check the installation of Flitchio Manager by comparing the version codes.
     *
     * @throws FlitchioManagerDependencyException
     */
    private static void checkFlitchioInstall(Context context)
            throws FlitchioManagerDependencyException {
        if (!BuildConfig.DEBUG && getFlitchioManagerVersionCode(context) < getVersionCode()) {
            throw new FlitchioManagerDependencyException();
        }
    }

    /**
     * Get an Intent to download the FlitchioManager app on the Play Store.
     *
     * @return An intent to open the PlayStore on FlitchioManager page.
     * @since 0.5.0
     */
    public static Intent getPlayStoreIntentForFlitchioManager() {
        // This intent will work only if the Play Store is installed, which we assume here.
        // Otherwise a possible alternative is:
        // new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="
        // + appPackageName));

        return new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + FLITCHIO_MANAGER_PACKAGE))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * Get the SDK version code used in the JAR. This returns the "android:versionCode" value
     * defined in the Manifest.
     *
     * @return The version code of the SDK.
     * @since 0.5.0
     */
    @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * Get the version code of FlitchioManager installed on the system. This corresponds to the
     * "android:versionCode" attribute in the Manifest of Flitchio Manager.
     *
     * @return The version code of Flitchio Manager.
     * @throws FlitchioManagerDependencyException If FlitchioManager is not installed.
     * @since 0.5.0
     */
    @SuppressWarnings("WeakerAccess")
    public static int getFlitchioManagerVersionCode(Context context)
            throws FlitchioManagerDependencyException {
        try {
            PackageInfo flitchioManagerInfo = context.getPackageManager()
                    .getPackageInfo(FLITCHIO_MANAGER_PACKAGE, 0);

            return flitchioManagerInfo.versionCode;

        } catch (NameNotFoundException e) {
            throw new FlitchioManagerDependencyException();
        }
    }

    /**
     * Initialise the {@link FlitchioController}. This method verifies the presence of the Flitchio
     * Manager app and binds to it. It must be the first method to be called, in the onCreate()
     * method of your {@link Activity} / {@link Service}. At the moment this method returns true,
     * the binding is <strong>not yet effective</strong>.
     * To be notified as soon as the binding is done, you should declare a {@link FlitchioListener}
     * and implement {@link FlitchioListener#onFlitchioStatusChanged(boolean)}.
     *
     * @return True if the {@link FlitchioController} is going to get bound.
     * @throws FlitchioManagerDependencyException If Flitchio Manager was not found or is too old
     *                                            and needs to be upgraded.
     * @since 0.5.0
     */
    public boolean onCreate() throws FlitchioManagerDependencyException {
        if (flitchioService != null) {
            // We already have binding, onCreate() has been called before
            return true;
        }

        checkFlitchioInstall(context);

        Intent intentToService = new Intent().setClassName(
                FLITCHIO_MANAGER_PACKAGE,
                FLITCHIO_SERVICE_CLASS);

        boolean willBind = context.bindService(
                intentToService,
                serviceConnection,
                Context.BIND_AUTO_CREATE);

        activityLifecycleMoment = ActivityLifecycle.ON_CREATE;

        return willBind;
    }

    /**
     * Register a {@link FlitchioListener} to receive callbacks. If you use this
     * {@link FlitchioController} in an {@link Activity}, this should be called in your Activity's
     * onResume() (hence the name). If you use this {@link FlitchioController} in a {@link Service},
     * this can be called right after {@link #onCreate()}. You only need to call this if you declare
     * a {@link FlitchioListener}.
     *
     * @param listener The listener.
     * @param handler  The handler associated to the thread on which the callbacks will happen.
     * @since 0.5.0
     */
    public void onResume(FlitchioListener listener, Handler handler) {
        synchronized (lockListener) {
            /*
             * ENSURE CLEAN STATE = termination of the (previous) listener thread
             */
            resetListener();

            /*
             * SET LISTENER
             */
            if (listener != null) {
                this.listener = listener;

                if (handler != null) {
                    listenerHandler = handler;
                } else {
                    // We create an arbitrary thread to handle listener callbacks
                    listenerThread = new ListenerThread();
                    listenerHandler = listenerThread.getHandler();
                }
            } else {
                FlitchioLog.i(
                        "No need to call onResume()/onPause() if you don't declare a listener.");
            }
        }

        /*
         * REGISTER CLIENT.
         * This will fail on the first call of onResume() as the binding will not be ready then.
         * It will be called in onServiceConnected() when the binding is ready.
         */
        registerClient();

        activityLifecycleMoment = ActivityLifecycle.ON_RESUME;
    }

    /**
     * Register a {@link FlitchioListener} to receive callbacks. If you use this
     * {@link FlitchioController} in an {@link Activity}, this should be called in your Activity's
     * onResume() (hence the name). If you use this {@link FlitchioController} in a {@link Service},
     * this can be called right after {@link #onCreate()}.
     * You only need to call this if you declare a {@link FlitchioListener}.
     *
     * @param listener The listener.
     * @since 0.5.0
     */
    public void onResume(FlitchioListener listener) {
        onResume(listener, null);
    }

    /**
     * Unregister the {@link FlitchioListener} that has been previously declared. If you use this
     * {@link FlitchioController} in an {@link Activity}, this should be called in your Activity's
     * onPause() (hence the name). If you use this {@link FlitchioController} in a {@link Service},
     * this can be called as late as in your Service's onDestroy().
     * You only need to call this if you have declared Listener with
     * {@link #onResume(FlitchioListener)}.
     *
     * @since 0.5.0
     */
    public void onPause() {
        unregisterClient();
        resetListener();

        activityLifecycleMoment = ActivityLifecycle.ON_PAUSE;
    }

    /**
     * Terminate the {@link FlitchioController}. You won't be able to get data or callbacks from
     * Flitchio after this call. This should be called in the onDestroy() method of the
     * {@link Activity} / {@link Service}.
     *
     * @since 0.5.0
     */
    public void onDestroy() {
        /*
         * ENSURE termination of the thread and proper unregistration from the service,
         * this is for safety.
         */
        if (activityLifecycleMoment != ActivityLifecycle.ON_PAUSE) {
            onPause();
        }

        synchronized (lockService) {
            /*
             * NOTIFY service that this Controller is exiting. Important note: the Activity's
             * onDestroy() is not always called
             * (http://developer.android.com/reference/android/app/Activity.html#onDestroy()),
             * so the service will not always be notified of an unbinding.
             */
            try {
                flitchioService.removeClientInfo(authToken);

            } catch (RemoteException e) {
                FlitchioLog.e("Unexpected error: could not notify Flitchio Manager about " +
                        "this controller termination.");

            } catch (NullPointerException | IllegalArgumentException e) {
                FlitchioLog.w("Warning: binding to Flitchio Manager not yet effective.");
            }

            /*
             * UNBIND
             */
            context.unbindService(serviceConnection);
            flitchioService = null;
        }

        activityLifecycleMoment = ActivityLifecycle.ON_DESTROY;
    }

    /**
     * Reset the listener variables (thread, handler and listener itself) properly.
     */
    private void resetListener() {
        synchronized (lockListener) {
            if (listenerThread != null) {
                listenerThread.quit();
                listenerThread = null;
            }
            listenerHandler = null;

            listener = null;
        }
    }

    /**
     * Register this FlitchioController to the Service. This is a RPC. This is only called
     * if context != null.
     */
    private void registerClient() {
        synchronized (lockService) {
            if (flitchioService != null) {
                try {
                    flitchioService.registerClient(authToken, clientStub);

                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error while trying to register.");

                } catch (NullPointerException e) {
                    FlitchioLog.w("Warning: binding to Flitchio Manager not yet effective. " +
                            "Client will be registered later.");
                }
            }
        }
    }

    /**
     * Unregister this FlitchioController from the Service. This is a RPC. This is only called
     * if context != null.
     */
    private void unregisterClient() {
        synchronized (lockService) {
            if (flitchioService != null) {
                try {
                    flitchioService.unregisterClient(authToken, clientStub);
                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error while trying to unregister.");
                } catch (NullPointerException e) {
                    FlitchioLog.w("Warning: binding to Flitchio Manager not yet effective.");
                }
            }
        }
    }

    /**
     * Retrieve the latest state of Flitchio as a {@link FlitchioSnapshot}.
     * <strong>Attention:</strong> before the binding is effective, this will always return an empty
     * snapshot. You can listen to the moment the binding gets effective by implementing
     * {@link FlitchioListener#onFlitchioStatusChanged(boolean)}.
     *
     * @return The snapshot representing the latest state of Flitchio. It is never null: when
     * Flitchio is disconnected, you get an empty snapshot instead.
     * @since 0.5.0
     */
    public FlitchioSnapshot obtainSnapshot() {
        synchronized (lockService) {
            try {
                FlitchioSnapshot snapshot = flitchioService.getSnapshot(authToken);
                if (snapshot == null) {
                    return new FlitchioSnapshot();
                } else {
                    return snapshot;
                }

            } catch (RemoteException e) {
                FlitchioLog.e("Unexpected error while trying to obtain a snapshot.");
                return new FlitchioSnapshot();

            } catch (NullPointerException e) {
                FlitchioLog.w("Warning: binding to Flitchio Manager not yet effective. " +
                        "Returned snapshot will be empty.");
                return new FlitchioSnapshot();
            }
        }
    }

    /**
     * Check if Flitchio is connected. <strong>Attention:</strong> before the binding is effective,
     * this will always return false. You can listen to the moment the binding gets effective by
     * implementing {@link FlitchioListener#onFlitchioStatusChanged(boolean)}.
     *
     * @return True if Flitchio is connected and data can be read from it.
     * @since 0.5.0
     */
    public boolean isConnected() {
        synchronized (lockService) {
            try {
                return flitchioService.isConnected(authToken);

            } catch (RemoteException e) {
                FlitchioLog.e("Unexpected error while trying to check the connection status.");
                return false;

            } catch (NullPointerException e) {
                FlitchioLog.w("Warning: binding to Flitchio Manager not yet effective. " +
                        "Returned status will be disconnected.");
                return false;
            }
        }
    }

    private enum ActivityLifecycle {
        UNDEFINED, ON_CREATE, ON_RESUME, ON_PAUSE, ON_DESTROY
    }

    /**
     * Stub that receives the IPC callbacks from the Service and presents them to the client
     * ({@link FlitchioListener}) the right way and in the right thread.
     *
     * @author david.f
     */
    private class IFlitchioClientStub extends IFlitchioClient.Stub {

        @Override
        public void onStatusChanged(boolean isConnected) throws RemoteException {
            synchronized (lockListener) {
                if (listenerHandler != null) {
                    listenerHandler.post(new StatusRunnable(isConnected));
                }
            }
        }

        @Override
        public void onButtonEvent(ButtonEvent event) throws RemoteException {
            synchronized (lockListener) {
                if (listenerHandler != null) {
                    listenerHandler.post(new ButtonEventRunnable(event));

                }
            }
        }

        @Override
        public void onJoystickEvent(JoystickEvent event) throws RemoteException {
            // TODO link the chain of events in order to receive ButtonEvents for Dpad only if the
            // corresponding joystick has been ignored

            synchronized (lockListener) {
                if (listenerHandler != null) {
                    listenerHandler.post(new JoystickEventRunnable(event));
                }
            }
        }
    }

    /**
     * Runnable callback for status changed (connected/disconnected) events. It will be run on the
     * listener's thread.
     */
    private class StatusRunnable implements Runnable {
        private final boolean isConnected;

        public StatusRunnable(boolean isConnected) {
            this.isConnected = isConnected;
        }

        @Override
        public void run() {
            synchronized (lockListener) {
                if (listener != null) {
                    listener.onFlitchioStatusChanged(isConnected);
                }
            }
        }
    }

    /**
     * Runnable callback for button events. It will be run on the listener's thread.
     */
    private class ButtonEventRunnable implements Runnable {
        private final ButtonEvent event;

        public ButtonEventRunnable(ButtonEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            synchronized (lockListener) {
                if (listener != null) {
                    listener.onFlitchioButtonEvent(event.getSource(), event);
                }
            }
        }
    }

    /**
     * Runnable callback for joystick events. It will be run on the listener's thread.
     */
    private class JoystickEventRunnable implements Runnable {
        private final JoystickEvent event;

        public JoystickEventRunnable(JoystickEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            synchronized (lockListener) {
                if (listener != null) {
                    listener.onFlitchioJoystickEvent(event.getSource(), event);
                }
            }
        }
    }
}
