package com.vaistra.exception;

public class TokenUnauthorizedException extends RuntimeException{

    public TokenUnauthorizedException(String msg)
    {
        super(msg);
    }
}
