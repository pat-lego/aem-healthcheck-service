package com.adobe.skyline.readiness.exceptions;

/**
 * Exception raised when the core system readiness check cannot be completed.
 */
public class SystemReadyException extends Exception {

    /**
     * Creates an exception with a readiness failure message.
     *
     * @param message description of the readiness failure
     */
    public SystemReadyException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a readiness failure message and root cause.
     *
     * @param message description of the readiness failure
     * @param e underlying exception that caused the readiness failure
     */
    public SystemReadyException(String message, Exception e) {
        super(message, e);
    }
}
