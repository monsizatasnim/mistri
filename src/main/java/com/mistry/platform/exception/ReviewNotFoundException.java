package com.mistry.platform.exception;

public class ReviewNotFoundException extends AccountNotFoundException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}