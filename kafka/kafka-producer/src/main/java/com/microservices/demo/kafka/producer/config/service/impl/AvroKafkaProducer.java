package com.microservices.demo.kafka.producer.config.service.impl;

import com.microservices.demo.kafka.producer.config.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * Generic Kafka producer for Avro-serialized messages.
 * Supports any Avro model that extends SpecificRecordBase.
 *
 * @param <K> Key type
 * @param <V> Value type (Avro model)
 */
@Service
public class AvroKafkaProducer<K extends Serializable, V extends SpecificRecordBase>
        implements KafkaProducer<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AvroKafkaProducer.class);

    private final KafkaTemplate<K, V> kafkaTemplate;

    public AvroKafkaProducer(KafkaTemplate<K, V> template) {
        this.kafkaTemplate = template;
    }

    @Override
    public void send(String topicName, K key, V message) {
        LOG.info("Sending event='{}' to topic='{}'", message, topicName);
        CompletableFuture<SendResult<K, V>> kafkaResultFuture =
                kafkaTemplate.send(topicName, key, message);
        addCallback(topicName, message, kafkaResultFuture);
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            LOG.info("Closing Kafka producer!");
            kafkaTemplate.destroy();
        }
    }

    private void addCallback(String topicName, V message,
                             CompletableFuture<SendResult<K, V>> kafkaResultFuture) {
        kafkaResultFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                LOG.error("Error while sending event {} to topic {}", message.toString(), topicName, throwable);
            } else {
                RecordMetadata metadata = result.getRecordMetadata();
                LOG.debug("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            }
        });
    }
}
