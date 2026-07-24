package com.ecommerce.user.exception;

/** Thrown when login username/password do not match -> mapped to 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
