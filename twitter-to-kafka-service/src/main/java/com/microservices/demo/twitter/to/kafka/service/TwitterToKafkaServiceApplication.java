package com.microservices.demo.twitter.to.kafka.service;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.function.Consumer;
@ComponentScan(basePackages = "com.microservices.demo")
@SpringBootApplication
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    public static final Logger LOG = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);
    private final  TwitterToKafkaServiceConfigData configData;
    private final StreamRunner streamRunner;

    @Autowired
    public TwitterToKafkaServiceApplication(TwitterToKafkaServiceConfigData configData, StreamRunner streamRunner) {
        this.configData = configData;
        this.streamRunner = streamRunner;
    }

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("App starts...");
        Consumer<String> logFunction = LOG::info;
        configData.getTwitterKeyWords().forEach(logFunction);
        LOG.info(configData.getWelcomeMessage());
        streamRunner.start();
    }
}
