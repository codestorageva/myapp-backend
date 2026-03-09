package com.vaistra.exception;

public class RequestBodyMissingException extends RuntimeException {

    public RequestBodyMissingException() {
    }

    public RequestBodyMissingException(String msg) {
        super(msg);
    }
}