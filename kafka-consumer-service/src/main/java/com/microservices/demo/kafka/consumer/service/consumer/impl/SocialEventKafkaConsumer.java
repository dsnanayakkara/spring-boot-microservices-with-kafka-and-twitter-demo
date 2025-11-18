package com.microservices.demo.kafka.consumer.service.consumer.impl;

import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import com.microservices.demo.kafka.consumer.config.service.KafkaConsumer;
import com.microservices.demo.kafka.consumer.service.metrics.ConsumerMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialEventKafkaConsumer implements KafkaConsumer<Long, SocialEventAvroModel> {

    private static final Logger LOG = LoggerFactory.getLogger(SocialEventKafkaConsumer.class);

    private final ConsumerMetrics consumerMetrics;

    private long messageCount = 0;
    private long lastLogTime = System.currentTimeMillis();
    private static final long LOG_INTERVAL_MS = 30000; // Log statistics every 30 seconds

    public SocialEventKafkaConsumer(ConsumerMetrics metrics) {
        this.consumerMetrics = metrics;
    }

    @Override
    @KafkaListener(id = "socialEventListener", topics = "${kafka-config.topic-name}",
                   groupId = "${kafka-consumer-config.consumer-group-id}")
    public void receive(@Payload List<SocialEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<Long> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        LOG.debug("Received {} social events with keys {}, partitions {} and offsets {}",
                messages.size(), keys.toString(), partitions.toString(), offsets.toString());

        // Update metrics
        consumerMetrics.incrementConsumed(messages.size());

        // Process each message
        for (int i = 0; i < messages.size(); i++) {
            SocialEventAvroModel event = messages.get(i);
            Long key = keys.get(i);
            Integer partition = partitions.get(i);
            Long offset = offsets.get(i);

            try {
                Timer.Sample sample = Timer.start();
                processEvent(event, key, partition, offset);
                sample.stop(consumerMetrics.getProcessingTimer());
                consumerMetrics.incrementProcessed();
                messageCount++;
            } catch (Exception e) {
                LOG.error("Error processing event with ID: {}", event.getId(), e);
                consumerMetrics.incrementFailed();
            }
        }

        // Log statistics periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= LOG_INTERVAL_MS) {
            double rate = (messageCount * 60000.0) / (currentTime - lastLogTime + LOG_INTERVAL_MS);
            LOG.info("📊 Consumed {} events so far | Rate: {}/min | Batch size: {}",
                    messageCount, String.format("%.2f", rate), messages.size());
            lastLogTime = currentTime;
        }
    }

    private void processEvent(SocialEventAvroModel event, Long key, Integer partition, Long offset) {
        LOG.info("Processing event - ID: {}, User: {}, Text: {}, Created: {} [partition={}, offset={}]",
                event.getId(),
                event.getUserId(),
                event.getText() != null ? event.getText().toString().substring(0, Math.min(50, event.getText().length())) + "..." : "null",
                event.getCreatedAt(),
                partition,
                offset);

        // TODO: Add business logic here (e.g., save to database, send to another service, etc.)
        // For now, just logging the event
    }
}
