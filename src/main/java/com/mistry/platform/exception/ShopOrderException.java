package com.mistry.platform.exception;

import org.springframework.http.HttpStatus;

public class ShopOrderException extends RuntimeException {

    private final HttpStatus status;

    public ShopOrderException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}