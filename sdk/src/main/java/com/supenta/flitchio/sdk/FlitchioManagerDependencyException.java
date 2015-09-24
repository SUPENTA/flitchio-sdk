package com.supenta.flitchio.sdk;

/**
 * Exception thrown when the Flitchio Manager app was not found on the system or is too old for this
 * SDK version.
 *
 * @since 0.5.0
 */
@SuppressWarnings("serial")
public class FlitchioManagerDependencyException extends Exception {
    public FlitchioManagerDependencyException() {
        super("The latest version of Flitchio Manager is not installed. " +
                        "Please install or upgrade the Flitchio Manager app."
        );
    }
}
