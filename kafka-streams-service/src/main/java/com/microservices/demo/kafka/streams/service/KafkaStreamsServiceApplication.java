package com.microservices.demo.kafka.streams.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
@ComponentScan(basePackages = "com.microservices.demo")
public class KafkaStreamsServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsServiceApplication.class, args);
        LOG.info("Kafka Streams Service started successfully!");
    }
}
