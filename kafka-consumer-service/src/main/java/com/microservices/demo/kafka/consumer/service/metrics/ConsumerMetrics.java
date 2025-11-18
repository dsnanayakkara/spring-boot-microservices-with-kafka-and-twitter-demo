package com.microservices.demo.kafka.consumer.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ConsumerMetrics {

    private final Counter messagesConsumedCounter;
    private final Counter messagesProcessedCounter;
    private final Counter messagesFailedCounter;
    private final Timer processingTimer;
    private final AtomicLong lastProcessedTimestamp;

    public ConsumerMetrics(MeterRegistry meterRegistry) {
        this.messagesConsumedCounter = Counter.builder("kafka.consumer.messages.consumed")
                .description("Total number of messages consumed from Kafka")
                .tag("topic", "social-events")
                .register(meterRegistry);

        this.messagesProcessedCounter = Counter.builder("kafka.consumer.messages.processed")
                .description("Total number of messages successfully processed")
                .tag("topic", "social-events")
                .register(meterRegistry);

        this.messagesFailedCounter = Counter.builder("kafka.consumer.messages.failed")
                .description("Total number of messages that failed processing")
                .tag("topic", "social-events")
                .register(meterRegistry);

        this.processingTimer = Timer.builder("kafka.consumer.processing.time")
                .description("Time taken to process messages")
                .tag("topic", "social-events")
                .register(meterRegistry);

        this.lastProcessedTimestamp = new AtomicLong(System.currentTimeMillis());

        meterRegistry.gauge("kafka.consumer.last.processed.timestamp", lastProcessedTimestamp);
    }

    public void incrementConsumed(int count) {
        messagesConsumedCounter.increment(count);
    }

    public void incrementProcessed() {
        messagesProcessedCounter.increment();
        lastProcessedTimestamp.set(System.currentTimeMillis());
    }

    public void incrementFailed() {
        messagesFailedCounter.increment();
    }

    public Timer getProcessingTimer() {
        return processingTimer;
    }

    public double getMessagesConsumed() {
        return messagesConsumedCounter.count();
    }

    public double getMessagesProcessed() {
        return messagesProcessedCounter.count();
    }

    public double getMessagesFailed() {
        return messagesFailedCounter.count();
    }
}
