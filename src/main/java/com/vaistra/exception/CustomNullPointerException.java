package com.vaistra.exception;

public class CustomNullPointerException extends RuntimeException{

    public CustomNullPointerException(){}
    public CustomNullPointerException(String msg)
    {
        super(msg);
    }
}
