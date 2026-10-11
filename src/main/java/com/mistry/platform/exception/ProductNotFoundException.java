package com.mistry.platform.exception;

public class ProductNotFoundException extends AccountNotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
