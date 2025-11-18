package com.microservices.demo.event.stream.service;

import com.microservices.demo.event.stream.service.init.StreamInitializer;
import com.microservices.demo.event.stream.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class EventStreamServiceApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(EventStreamServiceApplication.class);

    private final StreamRunner streamRunner;

    private final StreamInitializer streamInitializer;

    public EventStreamServiceApplication(StreamRunner runner, StreamInitializer initializer) {
        this.streamRunner = runner;
        this.streamInitializer = initializer;
    }

    public static void main(String[] args) {
        SpringApplication.run(EventStreamServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Event Stream Service starting...");
        streamInitializer.init();
        streamRunner.start();
    }
}
