package com.vaistra.exception;

public class ConfirmationTokenExpiredException extends RuntimeException{

    public ConfirmationTokenExpiredException(String msg)
    {
        super(msg);
    }
}