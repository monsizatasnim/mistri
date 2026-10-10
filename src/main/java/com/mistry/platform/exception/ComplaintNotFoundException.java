package com.mistry.platform.exception;

public class ComplaintNotFoundException extends AccountNotFoundException {
    public ComplaintNotFoundException(String message) {
        super(message);
    }
}
