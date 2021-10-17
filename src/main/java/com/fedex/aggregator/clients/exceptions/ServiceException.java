package com.fedex.aggregator.clients.exceptions;

public class ServiceException extends RuntimeException{
    private final int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    public ServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
