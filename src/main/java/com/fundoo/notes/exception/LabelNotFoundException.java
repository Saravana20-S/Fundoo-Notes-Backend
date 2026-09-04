package com.fundoo.notes.exception;

public class LabelNotFoundException extends RuntimeException {

    public LabelNotFoundException(String message) {
        super(message);
    }
}