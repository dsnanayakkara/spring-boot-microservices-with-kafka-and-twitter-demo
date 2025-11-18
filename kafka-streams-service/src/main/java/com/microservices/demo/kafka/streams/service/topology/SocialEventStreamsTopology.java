package com.microservices.demo.kafka.streams.service.topology;

import com.microservices.demo.config.KafkaStreamsConfigData;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class SocialEventStreamsTopology {

    private static final Logger LOG = LoggerFactory.getLogger(SocialEventStreamsTopology.class);

    private final KafkaStreamsConfigData kafkaStreamsConfigData;

    @Value("${kafka-config.schema-registry-url}")
    private String schemaRegistryUrl;

    public SocialEventStreamsTopology(KafkaStreamsConfigData streamsConfigData) {
        this.kafkaStreamsConfigData = streamsConfigData;
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        LOG.info("Building Kafka Streams topology...");

        // Configure Avro Serde for SocialEventAvroModel
        final SpecificAvroSerde<SocialEventAvroModel> eventSerde = new SpecificAvroSerde<>();
        eventSerde.configure(
                Map.of("schema.registry.url", schemaRegistryUrl, "specific.avro.reader", true),
                false
        );

        // Create KStream from input topic
        KStream<Long, SocialEventAvroModel> socialEventsStream = streamsBuilder
                .stream(
                        kafkaStreamsConfigData.getInputTopicName(),
                        Consumed.with(Serdes.Long(), eventSerde)
                );

        // Log all incoming events
        socialEventsStream
                .peek((key, value) -> LOG.debug("Processing event: key={}, id={}, user={}, text={}",
                        key, value.getId(), value.getUserId(),
                        value.getText() != null ? value.getText().toString().substring(0, Math.min(30, value.getText().length())) : "null"));

        // Filter: Only events with text content
        KStream<Long, SocialEventAvroModel> eventsWithText = socialEventsStream
                .filter((key, value) -> value.getText() != null && !value.getText().toString().isEmpty(),
                        Named.as("filter-events-with-text"));

        // Transform: Extract words and create word stream
        KStream<String, String> wordsStream = eventsWithText
                .flatMapValues(
                        value -> Arrays.asList(value.getText().toString().toLowerCase().split("\\W+")),
                        Named.as("extract-words")
                )
                .selectKey((key, word) -> word, Named.as("rekey-by-word"));

        // Filter out common stop words and short words
        KStream<String, String> filteredWords = wordsStream
                .filter((word, value) -> word.length() > 3 && !isStopWord(word),
                        Named.as("filter-stop-words"));

        // Write filtered events to output topic
        eventsWithText.to(
                kafkaStreamsConfigData.getOutputTopicName(),
                Produced.with(Serdes.Long(), eventSerde)
        );

        LOG.info("Kafka Streams topology built successfully!");
    }

    private boolean isStopWord(String word) {
        String[] stopWords = {"this", "that", "with", "from", "have", "been", "were", "will", "would", "could", "should"};
        return Arrays.asList(stopWords).contains(word.toLowerCase());
    }
}
