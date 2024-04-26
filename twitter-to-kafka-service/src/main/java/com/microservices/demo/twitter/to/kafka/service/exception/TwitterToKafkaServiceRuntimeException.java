package com.microservices.demo.twitter.to.kafka.service.exception;

public class TwitterToKafkaServiceRuntimeException extends RuntimeException {
    public TwitterToKafkaServiceRuntimeException() {
        super();
    }

    public TwitterToKafkaServiceRuntimeException(String message) {
        super(message);
    }

    public TwitterToKafkaServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
