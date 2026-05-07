package com.adobe.skyline.readiness.exceptions;

/**
 * Exception raised when content readiness results cannot be collected.
 */
public class ContentReadyException extends Exception {

    /**
     * Creates an exception with a content readiness failure message.
     *
     * @param message description of the content readiness failure
     */
    public ContentReadyException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a content readiness failure message and root
     * cause.
     *
     * @param message description of the content readiness failure
     * @param e underlying exception that caused the content readiness failure
     */
    public ContentReadyException(String message, Exception e) {
        super(message, e);
    }
}
