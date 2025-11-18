package com.microservices.demo.twitter.to.kafka.service.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Simple POJO representing a social media message for simulation purposes.
 * Replaces Twitter4J Status objects.
 */
@Data
@Builder
public class SocialMediaMessage {
    private Long id;
    private Long userId;
    private String text;
    private LocalDateTime createdAt;
}
