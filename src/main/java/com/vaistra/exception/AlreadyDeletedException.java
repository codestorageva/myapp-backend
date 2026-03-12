package com.vaistra.exception;

public class AlreadyDeletedException extends RuntimeException
{
    public AlreadyDeletedException()
    {}
    public AlreadyDeletedException(String msg) {
        super(msg);
    }
}
