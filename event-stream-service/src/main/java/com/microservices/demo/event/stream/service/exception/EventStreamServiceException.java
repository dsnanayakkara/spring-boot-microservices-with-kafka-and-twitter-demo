package com.microservices.demo.event.stream.service.exception;

public class EventStreamServiceException extends RuntimeException {

    public EventStreamServiceException() {
        super();
    }

    public EventStreamServiceException(String message) {
        super(message);
    }

    public EventStreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
