package com.ecommerce.user.exception;

/** Thrown when username/email already exists -> mapped to 409 Conflict. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
