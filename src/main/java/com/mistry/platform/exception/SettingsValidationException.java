package com.mistry.platform.exception;

import java.util.List;

public class SettingsValidationException extends RuntimeException {

    private final List<String> errors;

    public SettingsValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}