package com.vaistra.exception;

public class DuplicateEntryException extends RuntimeException{
    public DuplicateEntryException(){}
    public DuplicateEntryException(String msg)
    {
        super(msg);
    }
}
