package com.mkflow;

public class CliValidationException extends Exception {
    public CliValidationException() {
    }

    public CliValidationException(String message) {
        super(message);
    }

    public CliValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CliValidationException(Throwable cause) {
        super(cause);
    }

    public CliValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
