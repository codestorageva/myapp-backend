package com.vaistra.exception;

public class LoggedOutException extends RuntimeException {
    public LoggedOutException() {
    }
    public LoggedOutException(String msg) {
        super(msg);
    }

}
