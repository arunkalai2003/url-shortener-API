package com.example.shortener.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String m) {
        super(m);
    }
}
