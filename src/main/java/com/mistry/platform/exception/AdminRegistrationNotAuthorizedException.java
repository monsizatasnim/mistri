package com.mistry.platform.exception;

public class AdminRegistrationNotAuthorizedException extends RuntimeException {

    public AdminRegistrationNotAuthorizedException(String message) {
        super(message);
    }
}
