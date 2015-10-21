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
import android.support.annotation.BinderThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Object providing the main communication channel to the Flitchio Manager app.
 * This is the most important component of the Flitchio SDK, and the only object you have to
 * manipulate in order to use Flitchio with your app.
 * <p/>
 * <strong>The controller initialises and binds to the Flitchio Manager app when you call
 * {@link #onCreate(FlitchioStatusListener)}.
 * To free the controller properly after use, you must call {@link #onDestroy()}.
 * You also need to call {@link #onResume()} (or one of its other versions) and {@link #onPause()}
 * at the appropriate moments to ensure a clean lifecycle.</strong>
 * <p/>
 * After {@link #onCreate(FlitchioStatusListener)} returns, the binding is not effective yet.
 * You will get notified by a {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)}
 * callback.
 * When the status callback is {@link Status#BOUND}, you can start
 * using Flitchio.
 * The {@link FlitchioStatusListener} will also notify you about other status changes (like
 * "Flitchio just disconnected").
 * See {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)} for details.
 * <p/>
 * As soon as this controller is in {@link Status#CONNECTED}, you can poll
 * data by calling {@link #obtainSnapshot()}.
 * You typically call {@link #obtainSnapshot()} if you want to use your controller in
 * <em>polling mode</em>, i.e. if your app is designed to have a rendering loop updating the
 * display at high frequency.
 * That's typically the case for games that use a {@link SurfaceView} or a {@link GLSurfaceView}.
 * <p/>
 * If you don't want to actively poll data from Flitchio, but rather receive events every time
 * a button has been pressed/released or a joystick has moved, you can use your controller in
 * <em>listening mode</em>.
 * Simply use the variant of {@link #onResume(FlitchioEventListener)} that lets you register
 * a {@link FlitchioEventListener}.
 *
 * @since 0.5.0
 */
@SuppressWarnings("unused")
public class FlitchioController {

    static final String FLITCHIO_MANAGER_PACKAGE = "com.supenta.flitchio.manager";
    private static final String FLITCHIO_SERVICE_CLASS =
            FLITCHIO_MANAGER_PACKAGE + ".communication.FlitchioService";
    private static final int INVALID_AUTH_TOKEN = -1;

    /**
     * Map of per-{@link Context} existing {@link FlitchioController}s.
     * Using {@link WeakReference} to not leak memory when contexts have to be destroyed by the
     * system.
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

    private final Context context;

    /**
     * Receiver used for listening to connection/disconnection events of Flitchio.
     */
    private final FlitchioStatusReceiver statusReceiver = new FlitchioStatusReceiver();
    private final Handler mainThreadHandler = new Handler();

    /**
     * Received from service once the handshake has been done.
     * Used for every further communication.
     */
    private int authToken = INVALID_AUTH_TOKEN;

    private IFlitchioService flitchioService = null;

    /**
     * The event listener to be called on receiving data.
     * Known limitation: there can be only one event listener per controller, and only one
     * controller per context.
     */
    private FlitchioEventListener eventListener = null;

    /**
     * The status listener to be called on every lifecycle change of this controller (binding,
     * unbinding, binding error, connection of Flitchio, disconnection of Flitchio).
     */
    private FlitchioStatusListener statusListener = null;

    /**
     * The thread to which the event callbacks will be delivered (used by default), or the
     * handler associated to the thread decided by the 3rd-party dev.
     */
    private ListenerThread eventListenerThread = null;
    private Handler eventListenerThreadHandler = null;

    /**
     * The {@link ComponentName} for this context, used to identify this client in Flitchio Service.
     */
    private ComponentName clientId = null;

    /**
     * Status of this controller.
     */
    private Status currentStatus = Status.UNBOUND;

    /**
     * Listener object for the binding to the service, that detects when the binding is done and
     * when an unexpected disconnection occurred.
     * The methods here are ALWAYS CALLED ON UI THREAD.
     * Meaning that a binding will be effective ONLY after onCreate(), onStart(), onResume().
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        @MainThread
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (lockService) {
                flitchioService = IFlitchioService.Stub.asInterface(service);

                try {
                    authToken = flitchioService.receiveClientInfo(clientId);

                    if (authToken == INVALID_AUTH_TOKEN) {
                        FlitchioLog.e("Unexpected error: could not authenticate to service");

                        reportStatus(Status.FailureReason.SERVICE_REFUSED_CONNECTION);
                        onDestroy();
                        return;
                    }

                    // We fire "bound" event
                    reportStatus(Status.BOUND);

                    // Right after we fire the real connection status (connected/disconnected)
                    if (flitchioService.isConnected(authToken)) {
                        reportStatus(Status.CONNECTED);
                    } else {
                        reportStatus(Status.DISCONNECTED);
                    }
                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error: could not identify this controller");

                    reportStatus(Status.FailureReason.SERVICE_UNREACHABLE); // Possibly another type of reason
                    onDestroy();
                    return;
                }
            }

            // We register the client in case he asked for it while binding was not ready
            if (eventListener != null) {
                registerClient();
            }
        }

        @Override
        @MainThread
        public void onServiceDisconnected(ComponentName className) {
            FlitchioLog.e(
                    "Unexpected error: this controller has been unbound from Flitchio Manager");

            reportStatus(Status.FailureReason.SERVICE_SHUTDOWN_CONNECTION);
            onDestroy();
        }
    };

    @MainThread
    private FlitchioController(@NonNull Context context) {
        this.context = context;
        this.clientId = new ComponentName(context, context.getClass());
    }

    /**
     * Get an instance of {@link FlitchioController} for this {@link Context}.
     *
     * @param context The context used to bind.
     *                It should be the Activity / Service, not the Application's context.
     * @return An instance of {@link FlitchioController}.
     * @since 0.5.0
     */
    @MainThread
    public static synchronized FlitchioController getInstance(@NonNull Context context) {
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
     * @return True if the Manager installed on the phone can be used by this version of the SDK.
     */
    private static boolean isFlitchioManagerUsable(Context context) {
        return BuildConfig.DEBUG // In debug, we consider the Manager always accessible
                || getFlitchioManagerVersionCode(context) >= getVersionCode();
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
     * Get the version code of FlitchioManager installed on the system.
     * This corresponds to the "android:versionCode" attribute in the Manifest of Flitchio Manager.
     *
     * @return The version code of Flitchio Manager, or -1 if it not installed.
     * @since 0.5.0
     */
    public static int getFlitchioManagerVersionCode(@NonNull Context context) {
        try {
            PackageInfo flitchioManagerInfo = context.getPackageManager()
                    .getPackageInfo(FLITCHIO_MANAGER_PACKAGE, 0);

            return flitchioManagerInfo.versionCode;

        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * Initialise this controller.
     * <p/>
     * <strong>You must call this method appropriately in the lifecycle of your Activity or
     * Service.</strong>
     * It should be called in the onCreate() method of your {@link Activity} / {@link Service}.
     * <p/>
     * This method attempts to initialise the controller and bind to Flitchio.
     * At the moment it returns, the binding is <strong>not yet effective</strong>.
     * To be notified with the status changes (binding effective or failed, Flitchio
     * connected or disconnected), declare a {@link FlitchioStatusListener} and pass it.
     * See {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)} for the status changes you
     * can watch.
     * <p/>
     * In the rare case that you don't wish to receive status callbacks, you can pass null.
     *
     * @param statusListener The listener for status changes.
     * @since 0.7.0
     */
    public void onCreate(@Nullable FlitchioStatusListener statusListener) {
        this.statusListener = statusListener;

        if (currentStatus != Status.BINDING_FAILED
                && currentStatus != Status.UNBOUND) {

            // BINDING_FAILED and UNBOUND and the only two "initial" values possible

            FlitchioLog.i("Called onCreate() but the status is already " + currentStatus + ": nothing will be done");
            return;
        }

        if (!isFlitchioManagerUsable(context)) {
            reportStatus(Status.FailureReason.MANAGER_UNUSABLE);
            return;
        }

        boolean willBind = context.bindService(
                new Intent().setClassName(
                        FLITCHIO_MANAGER_PACKAGE,
                        FLITCHIO_SERVICE_CLASS),
                serviceConnection,
                Context.BIND_AUTO_CREATE);

        if (!willBind) {
            reportStatus(Status.FailureReason.SERVICE_UNREACHABLE);
        } else {
            reportStatus(Status.BINDING);
        }
    }

    /**
     * Resume this controller.
     * <p/>
     * <strong>You must call this method (or one of its other versions) appropriately in the
     * lifecycle of your Activity or Service.</strong>
     * If you use this controller in an {@link Activity}, this method should be called in your
     * Activity's onResume().
     * If you use this controller in a {@link Service}, this method can be called right after
     * {@link #onCreate(FlitchioStatusListener)}.
     * <p/>
     * After calling this method, you are ensured to receive at least one
     * {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)} callback with the current
     * status.
     * <p/>
     * This is a variant of {@link #onResume(FlitchioEventListener)} that allows you to
     * define the thread on which you wish to receive the event callbacks.
     *
     * @param eventListener The event listener.
     * @param handler       The handler associated to the thread on which the callbacks will
     *                      happen.
     * @see FlitchioController#onResume()
     * @see FlitchioController#onResume(FlitchioEventListener)
     * @since 0.7.0
     */
    @MainThread
    public void onResume(FlitchioEventListener eventListener, Handler handler) {
        /*
         * SET UP THE STATUS LISTENER
         */
        if (statusListener != null) {
            /*
             * DETERMINE THE CURRENT STATUS (as we may have missed updates during pause)
             */
            Status statusAfterCheck;
            try {
                if (flitchioService.isConnected(authToken)) {
                    statusAfterCheck = Status.CONNECTED;
                } else {
                    statusAfterCheck = Status.DISCONNECTED;
                }
            } catch (Exception e) { // NullPointerException | RemoteException
                // Status is either UNBOUND, BINDING or BINDING_FAILED: we just fire it again
                statusAfterCheck = this.currentStatus;
            }
            FlitchioLog.v("Resumed the controller: current status is " + statusAfterCheck);

            /*
             * REPORT the determined current status
             */
            reportStatus(statusAfterCheck);

            /*
             * START LISTENING to status updates again
             */
            statusReceiver.start(context, new FlitchioStatusListener() {
                @Override
                public void onFlitchioStatusChanged(Status status) {
                    reportStatus(status);
                }
            });
        }

        /*
         * SET UP THE EVENT LISTENER
         */
        synchronized (lockListener) {
            // Ensure clean state = termination of the (previous) listener' thread
            resetEventListener();
            this.eventListener = eventListener;

            if (this.eventListener != null) {
                if (handler != null) {
                    eventListenerThreadHandler = handler;
                } else {
                    // We create an arbitrary thread to handle listener callbacks
                    eventListenerThread = new ListenerThread();
                    eventListenerThreadHandler = eventListenerThread.getHandler();
                }
            }
        }

        /*
         * REGISTER THE CLIENT ON THE SERVICE
         * This will fail on the first call of onResume() as the binding will not be ready then.
         * It will be called in onServiceConnected() when the binding is ready.
         */
        if (this.eventListener != null) {
            registerClient();
        }
    }

    /**
     * Resume this controller.
     * <p/>
     * <strong>You must call this method (or one of its other versions) appropriately in the
     * lifecycle of your Activity or Service.</strong>
     * If you use this controller in an {@link Activity}, this method should be called in your
     * Activity's onResume().
     * If you use this controller in a {@link Service}, this method can be called right after
     * {@link #onCreate(FlitchioStatusListener)}.
     * <p/>
     * After calling this method, you are ensured to receive at least one
     * {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)} callback with the current
     * status.
     * <p/>
     * This is a variant of {@link #onResume()} that allows you to listen for button and joystick
     * events.
     * You will get these event callback an arbitrary thread (different from the main thread).
     * If you want to receive event callbacks on the main thread, use
     * {@link #onResume(FlitchioEventListener, Handler)} instead, with a handler associated with
     * the main thread.
     *
     * @param eventListener The event listener.
     * @see FlitchioController#onResume()
     * @see FlitchioController#onResume(FlitchioEventListener, Handler)
     * @since 0.7.0
     */
    @MainThread
    public void onResume(FlitchioEventListener eventListener) {
        onResume(eventListener, null);
    }

    /**
     * Resume this controller.
     * <p/>
     * <strong>You must call this method (or one of its other versions) appropriately in the
     * lifecycle of your Activity or Service.</strong>
     * If you use this controller in an {@link Activity}, this method should be called in your
     * Activity's onResume().
     * If you use this controller in a {@link Service}, this method can be called right after
     * {@link #onCreate(FlitchioStatusListener)}.
     * <p/>
     * After calling this method, you are ensured to receive at least one
     * {@link FlitchioStatusListener#onFlitchioStatusChanged(Status)} callback with the current
     * status.
     * <p/>
     * This variant only enables you to follow status changes of Flitchio.
     * If you also wish to listen to button and joystick events, use
     * {@link #onResume(FlitchioEventListener)} instead.
     *
     * @see #onResume(FlitchioEventListener)
     * @see #onResume(FlitchioEventListener, Handler)
     * @since 0.7.0
     */
    @MainThread
    public void onResume() {
        onResume(null);
    }

    /**
     * Pause this controller.
     * <p/>
     * <strong>You must call this method appropriately in the lifecycle of your Activity or
     * Service.</strong>
     * If you use this controller in an {@link Activity}, this method should be called in your
     * Activity's onPause().
     * If you use this controller in a {@link Service}, this method can be called right before
     * {@link #onDestroy()}.
     *
     * @since 0.5.0
     */
    @MainThread
    public void onPause() {
        if (statusListener != null) {
            statusReceiver.stop(context);
        }

        unregisterClient();
        resetEventListener();
    }

    /**
     * Terminate this controller.
     * <p/>
     * <strong>You must call this method appropriately in the lifecycle of your Activity or
     * Service.</strong>
     * It should be called in the onDestroy() method of your {@link Activity} / {@link Service}.
     *
     * @since 0.5.0
     */
    @MainThread
    public void onDestroy() {
        /*
         * ENSURE termination of the thread and proper unregistration from the service.
         * onPause() should have been called before but this is for safety.
         */
        onPause();

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
                        "this controller termination");
            } catch (NullPointerException e) {
                FlitchioLog.w("Binding to Flitchio Manager not yet effective");
            }

            /*
             * UNBIND
             */
            try {
                context.unbindService(serviceConnection);
            } catch (IllegalArgumentException e) {
                FlitchioLog.w("It seems that you tried to call onDestroy without" +
                        " having a binding to Flitchio Manager");
            }
            flitchioService = null;
        }

        statusListener = null;

        if (currentStatus != Status.BINDING_FAILED) {
            reportStatus(Status.UNBOUND);
        }
    }

    /**
     * Reset the event listener variables (thread, handler and the listener itself) properly.
     */
    @MainThread
    private void resetEventListener() {
        synchronized (lockListener) {
            if (eventListenerThread != null) {
                eventListenerThread.quit();
                eventListenerThread = null;
            }
            eventListenerThreadHandler = null;

            eventListener = null;
        }
    }

    /**
     * Register this FlitchioController to the Service. This is a RPC.
     */
    @MainThread
    private void registerClient() {
        synchronized (lockService) {
            if (flitchioService != null) {
                try {
                    flitchioService.registerClient(authToken, clientStub);
                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error while trying to register");
                } catch (NullPointerException e) {
                    FlitchioLog.w("Binding to Flitchio Manager not yet effective. " +
                            "Client will be registered later.");
                }
            }
        }
    }

    /**
     * Unregister this FlitchioController from the Service. This is a RPC.
     */
    @MainThread
    private void unregisterClient() {
        synchronized (lockService) {
            if (flitchioService != null) {
                try {
                    flitchioService.unregisterClient(authToken, clientStub);
                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error while trying to unregister");
                } catch (NullPointerException e) {
                    FlitchioLog.w("Binding to Flitchio Manager not yet effective");
                }
            }
        }
    }

    /**
     * Retrieve the latest state of Flitchio as a {@link FlitchioSnapshot}.
     * <strong>Note:</strong> before the status is {@link Status#CONNECTED},
     * the snapshot returned by this method is always empty.
     *
     * @return The snapshot representing the latest state of Flitchio. It is never null: when
     * the status isn't {@link Status#DISCONNECTED}, you get an empty snapshot
     * instead.
     * @since 0.5.0
     */
    @NonNull
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
                FlitchioLog.e("Unexpected error while trying to obtain a snapshot");
                return new FlitchioSnapshot();

            } catch (NullPointerException e) {
                FlitchioLog.w("Binding to Flitchio Manager not yet effective. " +
                        "Returned snapshot will be empty.");
                return new FlitchioSnapshot();
            }
        }
    }

    /**
     * Retrieve the current status of this controller. To get notified of status changes, pass a
     * {@link FlitchioStatusListener} in {@link #onCreate(FlitchioStatusListener)}.
     *
     * @return The status of this controller.
     * @since 0.7.0
     */
    public Status getStatus() {
        return currentStatus;
    }

    private void reportStatus(Status newStatus) {
        reportStatus(newStatus, Status.FailureReason.NONE);
    }

    private void reportStatus(Status.FailureReason failureReason) {
        reportStatus(Status.BINDING_FAILED, failureReason);
    }

    /**
     * Set the current status and fire the change event to the listener.
     *
     * @param newStatus The new status.
     */
    @MainThread
    private void reportStatus(@NonNull Status newStatus, @NonNull Status.FailureReason failureReason) {
        if (newStatus == Status.UNKNOWN) {
            FlitchioLog.wtf("Unexpected: new status is unknown");
        }

        currentStatus = newStatus;
        currentStatus.setFailureReason(failureReason);

        fireCurrentStatus();
    }

    @MainThread
    private void fireCurrentStatus() {
        if (statusListener == null) {
            FlitchioLog.v("Not firing current status: there's no listener");
            return;
        }

        mainThreadHandler.post(new StatusRunnable(currentStatus));
    }

    /**
     * Stub that receives the IPC callbacks from the Service and presents them to the client
     * ({@link FlitchioEventListener}) the right way and in the right thread.
     */
    private class IFlitchioClientStub extends IFlitchioClient.Stub {

        @Override
        @BinderThread
        public void onButtonEvent(ButtonEvent event) throws RemoteException {
            synchronized (lockListener) {
                if (eventListenerThreadHandler != null) {
                    eventListenerThreadHandler.post(new ButtonEventRunnable(event));

                }
            }
        }

        @Override
        @BinderThread
        public void onJoystickEvent(JoystickEvent event) throws RemoteException {
            // TODO link the chain of events in order to receive ButtonEvents for Dpad only if the
            // corresponding joystick has been ignored

            synchronized (lockListener) {
                if (eventListenerThreadHandler != null) {
                    eventListenerThreadHandler.post(new JoystickEventRunnable(event));
                }
            }
        }
    }

    /**
     * Runnable callback for status changed (connected/disconnected) events.
     */
    @MainThread
    private class StatusRunnable implements Runnable {
        private final Status status;

        public StatusRunnable(Status status) {
            this.status = status;
        }

        @Override
        public void run() {
            if (statusListener != null) {
                statusListener.onFlitchioStatusChanged(status);
            }
        }
    }

    /**
     * Runnable callback for button events. It will be run on the listener thread.
     */
    private class ButtonEventRunnable implements Runnable {
        private final ButtonEvent event;

        public ButtonEventRunnable(ButtonEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            synchronized (lockListener) {
                if (eventListener != null) {
                    eventListener.onFlitchioButtonEvent(event.getSource(), event);
                }
            }
        }
    }

    /**
     * Runnable callback for joystick events. It will be run on the listener thread.
     */
    private class JoystickEventRunnable implements Runnable {
        private final JoystickEvent event;

        public JoystickEventRunnable(JoystickEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            synchronized (lockListener) {
                if (eventListener != null) {
                    eventListener.onFlitchioJoystickEvent(event.getSource(), event);
                }
            }
        }
    }
}
