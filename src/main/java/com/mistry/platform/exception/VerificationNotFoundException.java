package com.mistry.platform.exception;

public class VerificationNotFoundException extends RuntimeException {
    public VerificationNotFoundException(String message) {
        super(message);
    }
}