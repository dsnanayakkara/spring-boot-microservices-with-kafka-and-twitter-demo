package com.microservices.demo.kafka.streams.service.topology;

import com.microservices.demo.config.KafkaStreamsConfigData;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@Component
public class WordCountAggregationTopology {

    private static final Logger LOG = LoggerFactory.getLogger(WordCountAggregationTopology.class);

    private final KafkaStreamsConfigData kafkaStreamsConfigData;

    @Value("${kafka-config.schema-registry-url}")
    private String schemaRegistryUrl;

    public WordCountAggregationTopology(KafkaStreamsConfigData streamsConfigData) {
        this.kafkaStreamsConfigData = streamsConfigData;
    }

    @Autowired
    public void buildWordCountTopology(StreamsBuilder streamsBuilder) {
        LOG.info("Building word count aggregation topology...");

        // Configure Avro Serde
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

        // Word count with tumbling time windows (5 minute windows)
        KTable<Windowed<String>, Long> wordCounts = socialEventsStream
                .filter((key, value) -> value.getText() != null && !value.getText().toString().isEmpty())
                .flatMapValues(value -> Arrays.asList(value.getText().toString().toLowerCase().split("\\W+")))
                .filter((key, word) -> word.length() > 3) // Filter out short words
                .groupBy((key, word) -> word, Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-counts-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        // Convert windowed key to string for output
        wordCounts
                .toStream()
                .map((windowedKey, count) -> {
                    String key = String.format("%s@%d-%d",
                            windowedKey.key(),
                            windowedKey.window().start(),
                            windowedKey.window().end());
                    String value = String.format("{\"word\":\"%s\",\"count\":%d,\"windowStart\":%d,\"windowEnd\":%d}",
                            windowedKey.key(), count,
                            windowedKey.window().start(),
                            windowedKey.window().end());
                    return KeyValue.pair(key, value);
                })
                .peek((key, value) -> LOG.debug("Word count: {}", value))
                .to(kafkaStreamsConfigData.getWordCountTopicName(),
                        Produced.with(Serdes.String(), Serdes.String()));

        // User event count aggregation (session-based grouping)
        KTable<Long, Long> userEventCounts = socialEventsStream
                .filter((key, value) -> value.getUserId() != null)
                .groupByKey(Grouped.with(Serdes.Long(), eventSerde))
                .count(Materialized.<Long, Long, KeyValueStore<Bytes, byte[]>>as("user-event-counts-store")
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(Serdes.Long()));

        // Log user event counts
        userEventCounts
                .toStream()
                .peek((userId, count) -> LOG.info("User {} has {} events in total", userId, count));

        LOG.info("Word count aggregation topology built successfully!");
    }
}
