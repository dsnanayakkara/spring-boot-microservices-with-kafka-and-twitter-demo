package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.config.service.KafkaProducer;
import com.microservices.demo.twitter.to.kafka.service.model.SocialMediaMessage;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced mock stream runner that generates realistic social media messages
 * for demonstrating high-throughput Kafka microservice integration.
 * This replaces the Twitter API integration with a configurable data simulator.
 */
@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "true", matchIfMissing = true)
public class EnhancedMockStreamRunner implements StreamRunner {

    private static final Logger LOG = LoggerFactory.getLogger(EnhancedMockStreamRunner.class);

    private final TwitterToKafkaServiceConfigData configData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final Random random = new Random();
    private final AtomicLong messageCounter = new AtomicLong(0);
    private ScheduledExecutorService executorService;

    // Realistic message templates for different categories
    private static final List<String> TECH_MESSAGES = Arrays.asList(
            "Just deployed a new microservice using {keyword}! The performance improvements are incredible. #DevOps #CloudNative",
            "Learning {keyword} has been a game changer for our architecture. Highly recommend checking it out! #TechTips",
            "Anyone else excited about the latest {keyword} features? This is going to revolutionize how we build systems!",
            "Working on a new project with {keyword}. The ecosystem is amazing and the community support is outstanding!",
            "Pro tip: When using {keyword}, make sure to follow best practices for scalability and resilience.",
            "Just finished a deep dive into {keyword}. Here are my top 5 takeaways from the experience...",
            "Our team migrated to {keyword} last month. The results have exceeded all expectations! #DevLife",
            "Debugging a tricky issue with {keyword}. Sometimes the simplest solutions are the best ones.",
            "Conference talk on {keyword} was mind-blowing! Can't wait to implement these patterns in production.",
            "Comparison: {keyword} vs traditional approaches. The benefits are clear for modern distributed systems."
    );

    private static final List<String> TUTORIAL_MESSAGES = Arrays.asList(
            "New tutorial: Getting started with {keyword} in 10 minutes. Perfect for beginners! Link in bio.",
            "Step-by-step guide to mastering {keyword}. Covering everything from basics to advanced patterns.",
            "Common mistakes to avoid when working with {keyword}. Learn from my experience!",
            "Best practices for {keyword} in production environments. Thread 🧵",
            "How we scaled our {keyword} implementation to handle millions of requests per day.",
            "Understanding {keyword}: A comprehensive guide for developers of all skill levels.",
            "Top 10 resources for learning {keyword} in 2024. Bookmark this for later!",
            "Real-world {keyword} use cases that demonstrate its power and flexibility.",
            "Architecture deep dive: How we use {keyword} to build resilient microservices.",
            "From zero to hero with {keyword}. My learning journey and key insights."
    );

    private static final List<String> QUESTION_MESSAGES = Arrays.asList(
            "Has anyone experienced performance issues with {keyword}? Looking for optimization tips.",
            "What's the best way to implement error handling in {keyword}? Need some advice.",
            "Choosing between options for {keyword} integration. What has worked well for you?",
            "How do you handle testing with {keyword}? Share your testing strategies!",
            "What are the most important metrics to monitor when using {keyword}?",
            "Recommended resources for advanced {keyword} patterns? Moving beyond the basics.",
            "How do you ensure data consistency when working with {keyword}?",
            "What's your experience with {keyword} in high-traffic production environments?",
            "Looking for feedback on our {keyword} architecture. Open to suggestions!",
            "How do you handle versioning and backward compatibility with {keyword}?"
    );

    private static final List<String> ANNOUNCEMENT_MESSAGES = Arrays.asList(
            "🚀 Just released version 2.0 of our {keyword} library! Check out the new features.",
            "Excited to announce our new open-source project built with {keyword}! ⭐ Star us on GitHub!",
            "Join us for a webinar on {keyword} best practices next week. Registration is now open!",
            "Big news! Our {keyword} integration is now available in production. Try it out!",
            "We're hiring! Looking for talented developers with {keyword} experience. DM for details.",
            "Conference announcement: Speaking about {keyword} at the upcoming tech summit!",
            "New blog post: How we use {keyword} to power our platform. Read more on our tech blog.",
            "Open source contribution: We've added support for {keyword} in our framework!",
            "Launching a {keyword} study group. Join developers from around the world!",
            "Case study published: How {keyword} helped us achieve 99.99% uptime."
    );

    private static final List<List<String>> MESSAGE_CATEGORIES = Arrays.asList(
            TECH_MESSAGES,
            TUTORIAL_MESSAGES,
            QUESTION_MESSAGES,
            ANNOUNCEMENT_MESSAGES
    );

    public EnhancedMockStreamRunner(TwitterToKafkaServiceConfigData configData,
                                    KafkaProducer<Long, TwitterAvroModel> kafkaProducer) {
        this.configData = configData;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void start() {
        String[] keywords = configData.getTwitterKeywords().toArray(new String[0]);
        long sleepTimeMs = configData.getMockSleepMs();

        LOG.info("Starting enhanced mock data stream for keywords: {}", Arrays.toString(keywords));
        LOG.info("Generating messages every {} ms", sleepTimeMs);

        executorService = Executors.newScheduledThreadPool(2);

        // Schedule message generation
        executorService.scheduleAtFixedRate(
                () -> generateAndSendMessage(keywords),
                0,
                sleepTimeMs,
                TimeUnit.MILLISECONDS
        );

        // Schedule stats logging every 30 seconds
        executorService.scheduleAtFixedRate(
                this::logStats,
                30,
                30,
                TimeUnit.SECONDS
        );
    }

    private void generateAndSendMessage(String[] keywords) {
        try {
            SocialMediaMessage message = generateRealisticMessage(keywords);
            TwitterAvroModel avroModel = convertToAvroModel(message);
            kafkaProducer.send(configData.getTopicName(), message.getUserId(), avroModel);
            messageCounter.incrementAndGet();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Generated message {}: {}", message.getId(),
                         message.getText().substring(0, Math.min(50, message.getText().length())) + "...");
            }
        } catch (Exception e) {
            LOG.error("Error generating or sending message", e);
        }
    }

    private SocialMediaMessage generateRealisticMessage(String[] keywords) {
        // Select random category
        List<String> category = MESSAGE_CATEGORIES.get(random.nextInt(MESSAGE_CATEGORIES.size()));
        String template = category.get(random.nextInt(category.size()));

        // Select random keyword
        String keyword = keywords[random.nextInt(keywords.length)];

        // Generate message
        String messageText = template.replace("{keyword}", keyword);

        return SocialMediaMessage.builder()
                .id(ThreadLocalRandom.current().nextLong(1000000, 9999999999L))
                .userId(ThreadLocalRandom.current().nextLong(1000, 999999))
                .text(messageText)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private TwitterAvroModel convertToAvroModel(SocialMediaMessage message) {
        return TwitterAvroModel
                .newBuilder()
                .setId(message.getId())
                .setUserId(message.getUserId())
                .setText(message.getText())
                .setCreatedAt(message.getCreatedAt()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli())
                .build();
    }

    private void logStats() {
        long count = messageCounter.get();
        LOG.info("📊 Messages generated so far: {} | Average rate: {} msgs/min",
                count,
                String.format("%.2f", (count * 60000.0) / (System.currentTimeMillis() - getStartTime())));
    }

    private long startTime = System.currentTimeMillis();

    private long getStartTime() {
        return startTime;
    }

    @PreDestroy
    public void shutdown() {
        LOG.info("Shutting down enhanced mock stream runner...");
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("Enhanced mock stream runner stopped. Total messages generated: {}", messageCounter.get());
    }
}
