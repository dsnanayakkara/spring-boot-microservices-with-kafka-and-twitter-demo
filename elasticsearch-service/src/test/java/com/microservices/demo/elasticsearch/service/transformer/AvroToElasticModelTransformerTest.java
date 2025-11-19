package com.microservices.demo.elasticsearch.service.transformer;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AvroToElasticModelTransformer
 * Tests critical data transformation logic from Kafka Avro to Elasticsearch model
 */
@DisplayName("Avro to Elasticsearch Model Transformer Tests")
class AvroToElasticModelTransformerTest {

    private AvroToElasticModelTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new AvroToElasticModelTransformer();
    }

    @Test
    @DisplayName("Should transform single Avro model to Elastic model")
    void shouldTransformSingleModel() {
        // Given
        Long eventId = 12345L;
        Long userId = 67890L;
        String eventText = "Test event message";
        Long createdAt = System.currentTimeMillis();

        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(eventId)
                .setUserId(userId)
                .setText(eventText)
                .setCreatedAt(createdAt)
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should transform one model");

        SocialEventIndexModel elasticModel = result.get(0);
        assertEquals(String.valueOf(eventId), elasticModel.getId(), "ID should match");
        assertEquals(userId, elasticModel.getUserId(), "User ID should match");
        assertEquals(eventText, elasticModel.getText(), "Text should match");
        assertNotNull(elasticModel.getCreatedAt(), "Created at should not be null");
    }

    @Test
    @DisplayName("Should transform multiple Avro models")
    void shouldTransformMultipleModels() {
        // Given
        SocialEventAvroModel avroModel1 = SocialEventAvroModel.newBuilder()
                .setId(1L)
                .setUserId(100L)
                .setText("Event 1")
                .setCreatedAt(System.currentTimeMillis())
                .build();

        SocialEventAvroModel avroModel2 = SocialEventAvroModel.newBuilder()
                .setId(2L)
                .setUserId(200L)
                .setText("Event 2")
                .setCreatedAt(System.currentTimeMillis())
                .build();

        SocialEventAvroModel avroModel3 = SocialEventAvroModel.newBuilder()
                .setId(3L)
                .setUserId(300L)
                .setText("Event 3")
                .setCreatedAt(System.currentTimeMillis())
                .build();

        List<SocialEventAvroModel> avroModels = Arrays.asList(avroModel1, avroModel2, avroModel3);

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(avroModels);

        // Then
        assertEquals(3, result.size(), "Should transform three models");
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
        assertEquals("3", result.get(2).getId());
    }

    @Test
    @DisplayName("Should handle null text field")
    void shouldHandleNullText() {
        // Given
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(123L)
                .setUserId(456L)
                .setText(null)
                .setCreatedAt(System.currentTimeMillis())
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getText(), "Text should be null when input is null");
    }

    @Test
    @DisplayName("Should handle null createdAt field")
    void shouldHandleNullCreatedAt() {
        // Given
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(123L)
                .setUserId(456L)
                .setText("Test")
                .setCreatedAt(null)
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getCreatedAt(), "CreatedAt should be null when input is null");
    }

    @Test
    @DisplayName("Should handle empty list")
    void shouldHandleEmptyList() {
        // Given
        List<SocialEventAvroModel> emptyList = Arrays.asList();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(emptyList);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
    }

    @Test
    @DisplayName("Should correctly convert timestamp to LocalDateTime")
    void shouldConvertTimestampCorrectly() {
        // Given
        Long timestamp = 1700000000000L; // Fixed timestamp for testing
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(123L)
                .setUserId(456L)
                .setText("Test")
                .setCreatedAt(timestamp)
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        LocalDateTime expectedDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );

        assertEquals(expectedDateTime, result.get(0).getCreatedAt(),
                "Timestamp should be correctly converted to LocalDateTime");
    }

    @Test
    @DisplayName("Should preserve ID as string")
    void shouldPreserveIdAsString() {
        // Given
        Long eventId = 999888777L;
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(eventId)
                .setUserId(123L)
                .setText("Test")
                .setCreatedAt(System.currentTimeMillis())
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertEquals("999888777", result.get(0).getId(), "ID should be converted to string");
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void shouldHandleSpecialCharactersInText() {
        // Given
        String specialText = "Test with special chars: @#$%^&*()_+-={}[]|:;<>?,./~`";
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(123L)
                .setUserId(456L)
                .setText(specialText)
                .setCreatedAt(System.currentTimeMillis())
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertEquals(specialText, result.get(0).getText(), "Special characters should be preserved");
    }

    @Test
    @DisplayName("Should handle Unicode characters in text")
    void shouldHandleUnicodeCharacters() {
        // Given
        String unicodeText = "Unicode test: こんにちは 你好 مرحبا 🚀 😀";
        SocialEventAvroModel avroModel = SocialEventAvroModel.newBuilder()
                .setId(123L)
                .setUserId(456L)
                .setText(unicodeText)
                .setCreatedAt(System.currentTimeMillis())
                .build();

        // When
        List<SocialEventIndexModel> result = transformer.getElasticModels(Arrays.asList(avroModel));

        // Then
        assertEquals(unicodeText, result.get(0).getText(), "Unicode characters should be preserved");
    }
}
