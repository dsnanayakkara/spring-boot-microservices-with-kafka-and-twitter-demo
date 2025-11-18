package com.microservices.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-streams-config")
public class KafkaStreamsConfigData {
    private String applicationId;
    private String inputTopicName;
    private String outputTopicName;
    private String wordCountTopicName;
    private String stateStoreLocation;
    private Integer numStreamThreads;
}
