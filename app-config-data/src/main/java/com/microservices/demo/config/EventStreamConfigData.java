package com.microservices.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "event-stream-service")
public class EventStreamConfigData {
    private List<String> eventKeywords;
    private String welcomeMessage;
    private Boolean enableMockEvents;
    private Long mockSleepMs;
    private Integer mockMinMessageLength;
    private Integer mockMaxMessageLength;
    private String topicName;
}
