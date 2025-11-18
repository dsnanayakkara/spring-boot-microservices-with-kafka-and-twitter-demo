package com.microservices.demo.elasticsearch.service.consumer.impl;

import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import com.microservices.demo.elasticsearch.service.transformer.AvroToElasticModelTransformer;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import com.microservices.demo.kafka.consumer.config.service.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialEventKafkaToElasticConsumer implements KafkaConsumer<Long, SocialEventAvroModel> {

    private static final Logger LOG = LoggerFactory.getLogger(SocialEventKafkaToElasticConsumer.class);

    private final AvroToElasticModelTransformer transformer;
    private final ElasticIndexClient<SocialEventIndexModel> elasticIndexClient;

    private long eventsIndexed = 0;
    private long lastLogTime = System.currentTimeMillis();
    private static final long LOG_INTERVAL_MS = 30000; // Log statistics every 30 seconds

    public SocialEventKafkaToElasticConsumer(AvroToElasticModelTransformer avroToElasticModelTransformer,
                                              ElasticIndexClient<SocialEventIndexModel> indexClient) {
        this.transformer = avroToElasticModelTransformer;
        this.elasticIndexClient = indexClient;
    }

    @Override
    @KafkaListener(id = "elasticConsumerListener", topics = "${kafka-config.topic-name}",
                   groupId = "${kafka-consumer-config.consumer-group-id}")
    public void receive(@Payload List<SocialEventAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<Long> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        LOG.debug("Received {} events for indexing to Elasticsearch. Keys: {}, Partitions: {}, Offsets: {}",
                messages.size(), keys, partitions, offsets);

        // Transform Avro models to Elasticsearch models
        List<SocialEventIndexModel> elasticModels = transformer.getElasticModels(messages);

        // Index to Elasticsearch
        List<String> documentIds = elasticIndexClient.save(elasticModels);

        eventsIndexed += documentIds.size();

        LOG.info("Successfully indexed {} documents. Total indexed: {}", documentIds.size(), eventsIndexed);

        // Log statistics periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= LOG_INTERVAL_MS) {
            double rate = (eventsIndexed * 60000.0) / (currentTime - lastLogTime + LOG_INTERVAL_MS);
            LOG.info("📊 Indexed {} events to Elasticsearch | Rate: {}/min | Batch size: {}",
                    eventsIndexed, String.format("%.2f", rate), messages.size());
            lastLogTime = currentTime;
        }
    }
}
