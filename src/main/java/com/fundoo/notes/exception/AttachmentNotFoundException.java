package com.fundoo.notes.exception;

public class AttachmentNotFoundException
        extends RuntimeException {

    public AttachmentNotFoundException(String message) {
        super(message);
    }
}