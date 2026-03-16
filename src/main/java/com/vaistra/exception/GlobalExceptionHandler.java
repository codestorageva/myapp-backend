package com.vaistra.exception;

import com.vaistra.dto.response.MessageResponse;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.lang.NullPointerException;

@RestControllerAdvice
public class GlobalExceptionHandler {


//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public MessageResponse handleGlobalRegularException(MethodArgumentNotValidException ex) {
//        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Invalid Data.");
//    }
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ExceptionHandler(MethodArgumentNotValidException.class)
     public MessageResponse handleGlobalRegularException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("Invalid Data.");
     return new MessageResponse(false, HttpStatus.BAD_REQUEST, errorMessage);
}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public MessageResponse handleMessageNotReadableException(HttpMessageNotReadableException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Request body cannot be empty.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public MessageResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Invalid Argument!");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public MessageResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Invalid Argument!");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ResourceNotFoundException.class)
    public MessageResponse handleResourceNotFound(ResourceNotFoundException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.ALREADY_REPORTED)
    @ExceptionHandler(ResourceAlreadyExistException.class)
    public MessageResponse handleResourceAlreadyExistException(ResourceAlreadyExistException ex) {
        return new MessageResponse(false, HttpStatus.ALREADY_REPORTED, ex.getMessage());
    }

    @ExceptionHandler(DuplicateEntryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleDuplicateEntryException(DuplicateEntryException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InactiveStatusException.class)
    public MessageResponse handleIsActiveException(InactiveStatusException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserUnauthorizedException.class)
    public Object handleUserUnauthorizedException(UserUnauthorizedException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConfirmationTokenExpiredException.class)
    public MessageResponse handleConfirmationTokenExpiredException(ConfirmationTokenExpiredException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidArgumentException.class)
    public MessageResponse handleInvalidArgumentException(InvalidArgumentException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(java.lang.NullPointerException.class)
    public MessageResponse handleNullPointerException(NullPointerException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IOException.class)
    public MessageResponse handleIoException(IOException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // JWT Exception Handling

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(TokenUnauthorizedException.class)
    public MessageResponse handleInvalidTokenException(TokenUnauthorizedException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public MessageResponse handleBadCredentialsException(BadCredentialsException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public MessageResponse handleAccessDeniedException(AccessDeniedException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SignatureException.class)
    public MessageResponse handleSignatureException(SignatureException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ExpiredJwtException.class)
    public MessageResponse handleExpiredJwtException(ExpiredJwtException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DecodingException.class)
    public MessageResponse handleDecodingException(DecodingException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public MessageResponse handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Maximum upload size is 50MB.");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(LoggedOutException.class)
    public MessageResponse handleLoggedOutException(LoggedOutException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AlreadyDeletedException.class)
    public MessageResponse handleAlreadyDeletedException(AlreadyDeletedException ex) {
        // Create a custom error message or response as needed
        String errorMessage = "Error: " + ex.getMessage();
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidVidFormatException.class)
    public MessageResponse invalidFormatException(InvalidVidFormatException ex) {
        // Create a custom error message or response as needed
        String errorMessage = "Error: " + ex.getMessage();
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}
