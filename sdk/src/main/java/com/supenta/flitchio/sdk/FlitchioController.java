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
 * Object providing the main communication channel to the Flitchio Manager app. This is the most
 * important component of the Flitchio SDK, and the only object you have to manipulate in order to
 * use Flitchio with your app.
 * <p/>
 * The controller binds to the Flitchio Manager app when you call
 * {@link #onCreate(FlitchioStatusListener)}. The Flitchio Manager app needs to be installed on the
 * user's phone, otherwise a {@link FlitchioManagerDependencyException} will be thrown. Likewise,
 * if the Flitchio Manager app installed doesn't support this version of the SDK, a
 * {@link FlitchioManagerDependencyException} will be thrown. To free the controller properly after
 * use, you must call {@link #onDestroy()}.
 * <p/>
 * After {@link #onCreate(FlitchioStatusListener)} returns, the binding is not effective yet.
 * You need to wait for the initial callback
 * {@link FlitchioStatusListener#onFlitchioStatusChanged(int)} before you can start using Flitchio.
 * It happens asynchronously, and usually right after the Activity has been initialised. To catch
 * this callback, you need to register a {@link FlitchioStatusListener} (see "listening mode"
 * below).
 * <p/>
 * As soon as this controller is bound to the Flitchio Manager app, you can check if Flitchio is
 * attached to the phone by calling {@link #isConnected()} and poll data from it by requesting a
 * {@link FlitchioSnapshot} of its state with {@link #obtainSnapshot()}. You typically call
 * {@link #obtainSnapshot()} if you want to use your controller in <em>polling mode</em>, i.e. if
 * your app is designed to have a rendering loop updating the display at high frequency. That's
 * typically the case for games that use a {@link SurfaceView} or a {@link GLSurfaceView}.
 * <p/>
 * If you don't want to actively poll data from Flitchio, but rather receive events every time
 * something has changed on the device, you can use your controller in <em>listening mode</em>.
 * There are two types of listeners: {@link FlitchioStatusListener} for updates about the
 * connection status of Flitchio, and {@link FlitchioEventListener} for updates about buttons and
 * joystick states. You can register any of the listeners or both with
 * {@link #onResume(FlitchioEventListener)}. If you register at least one
 * listener, you should then unregister with {@link #onPause()}.
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
     * Receiver used for listening to connection/disconnection events of Flitchio.
     */
    private final FlitchioStatusReceiver statusReceiver;
    private final Handler mainThreadHandler;

    /**
     * Received from service once the handshake has been done. Used for every further communication.
     */
    private int authToken = INVALID_AUTH_TOKEN;

    /**
     * Interface to FlitchioService.
     */
    private IFlitchioService flitchioService = null;

    /**
     * The event listener to be called on receiving data.
     * Known limitation: there can be only one event listener per controller, and only one
     * controller per context.
     */
    private FlitchioEventListener eventListener = null;

    /**
     * The status listener to be called on receiving connected and disconnected events.
     * Known limitation: there can be only one status listener per controller, and only one
     * controller per context.
     */
    private FlitchioStatusListener statusListener = null;

    /**
     * The thread to which the listener callbacks will be delivered (used by default), or the
     * handler associated to the thread decided by the 3rd-party dev. The same thread is used for
     * both status and event listeners.
     */
    private ListenerThread eventListenerThread = null;
    private Handler eventListenerThreadHandler = null;

    /**
     * The {@link ComponentName} for this context, used to identify this client in Flitchio Service.
     */
    private ComponentName clientId = null;

    /**
     * Current status of Flitchio. Its value is one of the constants of
     * {@link FlitchioStatusListener}.
     */
    private int currentStatus;
    private FailureReason failureReason = null;
    /**
     * Listener object for the binding to the service, that detects when the binding is done and
     * when an unexpected disconnection occurred. The methods here are ALWAYS CALLED ON UI THREAD.
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

                        reportFailure(FailureReason.SERVICE_REFUSED_CONNECTION);
                        onDestroy();
                        return;
                    }

                    // We fire "bound" event
                    reportStatus(FlitchioStatusListener.STATUS_BOUND);

                    // Right after we fire the real connection status (connected/disconnected)
                    if (flitchioService.isConnected(authToken)) {
                        reportStatus(FlitchioStatusListener.STATUS_CONNECTED);
                    } else {
                        reportStatus(FlitchioStatusListener.STATUS_DISCONNECTED);
                    }
                } catch (RemoteException e) {
                    FlitchioLog.e("Unexpected error: could not identify this controller");

                    reportFailure(FailureReason.SERVICE_UNREACHABLE); // Possibly another type of reason
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

            reportFailure(FailureReason.SERVICE_SHUTDOWN_CONNECTION);
            onDestroy();
        }
    };

    @MainThread
    private FlitchioController(@NonNull Context context) {
        this.context = context;
        this.clientId = new ComponentName(context, context.getClass());

        this.currentStatus = FlitchioStatusListener.STATUS_UNBOUND;

        this.mainThreadHandler = new Handler();
        this.statusReceiver = new FlitchioStatusReceiver();
    }

    /**
     * Get an instance of {@link FlitchioController} for this {@link Context}.
     *
     * @param context The context used to bind. It should be the Activity / Service, not the
     *                Application's context.
     * @return An instance of {@link FlitchioController}.
     * @since 0.5.0
     */
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
     * method of your {@link Activity} / {@link Service}. At the moment this method returns,
     * the binding is <strong>not yet effective</strong>.
     * To be notified as soon as the binding is done, pass a {@link FlitchioStatusListener} and
     * wait for the callback with {@link FlitchioStatusListener#STATUS_BOUND}.
     *
     * @since 0.7.0
     */
    public void onCreate(@Nullable FlitchioStatusListener statusListener) {
        if (currentStatus != FlitchioStatusListener.STATUS_BINDING_FAILED
                && currentStatus != FlitchioStatusListener.STATUS_UNBOUND) {
            FlitchioLog.i("Called onCreate() but the status is already " + currentStatus + ": nothing will be done");
            return;
        }

        this.statusListener = statusListener;

        boolean willBind = false;
        boolean isFlitchioInstallOk = false;

        try {
            checkFlitchioInstall(context);
            isFlitchioInstallOk = true;

            willBind = context.bindService(
                    new Intent().setClassName(
                            FLITCHIO_MANAGER_PACKAGE,
                            FLITCHIO_SERVICE_CLASS),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } catch (FlitchioManagerDependencyException e) {
            FlitchioLog.e("Binding won't happen: " + e);
        }

        if (isFlitchioInstallOk && willBind) {
            reportStatus(FlitchioStatusListener.STATUS_BINDING);
        } else if (!isFlitchioInstallOk) {
            reportFailure(FailureReason.MANAGER_OUTDATED);
        } else if (!willBind) {
            reportFailure(FailureReason.SERVICE_UNREACHABLE);
        }
    }

    /**
     * Register a {@link FlitchioEventListener} to receive callbacks.
     * If you use this {@link FlitchioController} in an {@link Activity}, this should be
     * called in your Activity's onResume() (hence the name). If you use this
     * {@link FlitchioController} in a {@link Service}, this can be called right after
     * {@link #onCreate(FlitchioStatusListener)}. You only need to call this if you declare at least one of the two
     * listeners.
     *
     * @param eventListener The event listener.
     * @param handler       The handler associated to the thread on which the callbacks will
     *                      happen.
     * @since 0.6.0
     */
    @MainThread
    public void onResume(FlitchioEventListener eventListener, Handler handler) {
        /*
         * SET UP THE STATUS LISTENER
         */
        if (statusListener != null) {
            statusReceiver.start(context, new FlitchioStatusListener() {
                @Override
                public void onFlitchioStatusChanged(int status) {
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
     * {@code handler} defaults to a {@link Handler} for an arbitrary thread (different from the
     * main thread).
     *
     * @see FlitchioController#onResume(FlitchioEventListener, Handler)
     * @since 0.6.0
     */
    @MainThread
    public void onResume(FlitchioEventListener eventListener) {
        onResume(eventListener, null);
    }

    @MainThread
    public void onResume() {
        onResume(null);
    }

    // TODO think what happens if Flitchio disconnects while I'm on pause (no status receiver)

    /**
     * Unregister the {@link FlitchioStatusListener} and/or {@link FlitchioEventListener} that have
     * been previously declared. If you use this {@link FlitchioController} in an {@link Activity},
     * this should be called in your Activity's onPause() (hence the name). If you use this
     * {@link FlitchioController} in a {@link Service}, this can be called as late as in your
     * Service's onDestroy(). You only need to call this if you have provided at least one of the
     * two listeners with {@link #onResume(FlitchioEventListener)}.
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
     * Terminate the {@link FlitchioController}. You won't be able to get data or callbacks from
     * Flitchio after this call. This should be called in the onDestroy() method of the
     * {@link Activity} / {@link Service}.
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

        if (currentStatus != FlitchioStatusListener.STATUS_BINDING_FAILED) {
            reportStatus(FlitchioStatusListener.STATUS_UNBOUND);
        }
    }

    /**
     * Reset the status and event listener variables (thread, handler and the listeners themselves)
     * properly.
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
     * <strong>Attention:</strong> before the binding is effective, this will always return an
     * empty snapshot. You can listen to the moment the binding gets effective by implementing
     * {@link FlitchioStatusListener#onFlitchioStatusChanged(int)}.
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
     * Check if Flitchio is connected. <strong>Attention:</strong> before the binding is effective,
     * this will always return false. You can listen to the moment the binding gets effective by
     * implementing {@link FlitchioStatusListener#onFlitchioStatusChanged(int)}.
     *
     * @return True if Flitchio is connected and data can be read from it.
     * @since 0.5.0
     */
    private boolean isConnected() {
        synchronized (lockService) {
            try {
                return flitchioService.isConnected(authToken);
                // TODO make this interact with currentStatus, probably remove isConnected
            } catch (RemoteException e) {
                FlitchioLog.e("Unexpected error while trying to check the connection status");
                return false;
            } catch (NullPointerException e) {
                FlitchioLog.w("Binding to Flitchio Manager not yet effective. " +
                        "Returned status will be disconnected.");
                return false;
            }
        }
    }

    public int getStatus() {
        return currentStatus;
    }

    @MainThread
    private void reportStatus(int newStatus) {
        this.currentStatus = newStatus;

        if (currentStatus != FlitchioStatusListener.STATUS_BINDING_FAILED) {
            failureReason = null;
        }

        fireCurrentStatus();
    }

    private void reportFailure(FailureReason failureReason) {
        this.failureReason = failureReason;
        reportStatus(FlitchioStatusListener.STATUS_BINDING_FAILED);
    }

    /**
     * @return The reason of the failed binding or null if the latest binding has not failed.
     */
    @Nullable
    public FailureReason getFailureReason() {
        return failureReason;
    }

    @MainThread
    private void fireCurrentStatus() {
        if (statusListener == null) {
            FlitchioLog.v("Not firing current status: there's no listener");
            return;
        }

        if (currentStatus == FlitchioStatusListener.STATUS_UNKNOWN) {
            FlitchioLog.v("Not firing current status: it is STATUS_UNKNOWN");
            return;
        }

        mainThreadHandler.post(new StatusRunnable(currentStatus));
    }

    private static class ActivityLifecycle {
        private static final int UNDEFINED = -1;
        private static final int ON_CREATE = 0;
        private static final int ON_RESUME = 1;
        private static final int ON_PAUSE = 2;
        private static final int ON_DESTROY = 3;
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
        private final int status;

        public StatusRunnable(int status) {
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
