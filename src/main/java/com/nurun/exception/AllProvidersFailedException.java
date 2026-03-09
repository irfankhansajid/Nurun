package com.nurun.exception;

public class AllProvidersFailedException extends RuntimeException {
    public AllProvidersFailedException(String message) {
        super(message);
    }
}
