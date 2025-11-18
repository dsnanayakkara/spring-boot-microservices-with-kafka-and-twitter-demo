package com.microservices.demo.elasticsearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class ElasticsearchIndexingServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchIndexingServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchIndexingServiceApplication.class, args);
        LOG.info("Elasticsearch Indexing Service started successfully!");
    }
}
