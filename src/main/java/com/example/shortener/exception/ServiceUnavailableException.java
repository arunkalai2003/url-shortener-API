package com.example.shortener.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String m, Throwable t) {
        super(m, t);
    }
}
