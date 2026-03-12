package com.vaistra.exception;

public class ExpiredJwtException extends RuntimeException{
    public ExpiredJwtException(String msg){
        super(msg);
    }
}
