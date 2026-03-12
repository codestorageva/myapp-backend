package com.vaistra.exception;

public class InactiveStatusException extends RuntimeException {
    public InactiveStatusException() {
    }

    public InactiveStatusException(String msg) {
        super(msg);
    }

}
