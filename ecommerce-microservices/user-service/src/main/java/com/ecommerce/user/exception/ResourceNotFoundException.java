package com.ecommerce.user.exception;

/** Thrown when a requested user does not exist -> mapped to 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
