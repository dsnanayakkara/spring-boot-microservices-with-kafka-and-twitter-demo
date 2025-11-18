package com.microservices.demo.elastic.query.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class ElasticQueryServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticQueryServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ElasticQueryServiceApplication.class, args);
        LOG.info("Elastic Query Service started successfully!");
    }
}
