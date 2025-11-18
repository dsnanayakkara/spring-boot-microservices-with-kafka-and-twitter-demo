package com.microservices.demo.kafka.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class KafkaConsumerServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerServiceApplication.class, args);
        LOG.info("Kafka Consumer Service started successfully!");
    }
}
