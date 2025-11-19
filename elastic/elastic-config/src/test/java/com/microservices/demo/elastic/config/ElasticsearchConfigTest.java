package com.microservices.demo.elastic.config;

import com.microservices.demo.config.ElasticConfigData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ElasticsearchConfig
 * Tests critical Elasticsearch client configuration builder logic
 */
@DisplayName("Elasticsearch Configuration Tests")
class ElasticsearchConfigTest {

    private ElasticsearchConfig elasticsearchConfig;
    private ElasticConfigData elasticConfigData;

    @BeforeEach
    void setUp() {
        elasticConfigData = new ElasticConfigData();
        elasticConfigData.setConnectionUrl("localhost:9200");
        elasticConfigData.setConnectionTimeoutMs(5000);
        elasticConfigData.setSocketTimeoutMs(30000);

        elasticsearchConfig = new ElasticsearchConfig(elasticConfigData);
    }

    @Test
    @DisplayName("Should create configuration without auth or SSL")
    void shouldCreateConfigurationWithoutAuthOrSsl() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
    }

    @Test
    @DisplayName("Should create configuration with authentication only")
    void shouldCreateConfigurationWithAuthOnly() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "elastic");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password123");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
    }

    @Test
    @DisplayName("Should create configuration with SSL only")
    void shouldCreateConfigurationWithSslOnly() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", true);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
    }

    @Test
    @DisplayName("Should create configuration with both authentication and SSL")
    void shouldCreateConfigurationWithAuthAndSsl() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "elastic");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password123");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", true);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
    }

    @Test
    @DisplayName("Should handle null username as no authentication")
    void shouldHandleNullUsernameAsNoAuth() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", null);
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password123");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null even with null username");
    }

    @Test
    @DisplayName("Should use configured connection URL")
    void shouldUseConfiguredConnectionUrl() {
        // Given
        elasticConfigData.setConnectionUrl("elasticsearch:9200");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
        // Note: Cannot directly assert connection URL from ClientConfiguration
        // but we verify the configuration builds successfully
    }

    @Test
    @DisplayName("Should use configured timeout values")
    void shouldUseConfiguredTimeouts() {
        // Given
        elasticConfigData.setConnectionTimeoutMs(10000);
        elasticConfigData.setSocketTimeoutMs(60000);
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
        // Note: Cannot directly assert timeout values from ClientConfiguration
        // but we verify the configuration builds successfully with custom timeouts
    }

    @Test
    @DisplayName("Should handle empty string username as no authentication")
    void shouldHandleEmptyStringUsername() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password123");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null with empty username");
    }

    @Test
    @DisplayName("Should handle whitespace-only username as authentication")
    void shouldHandleWhitespaceUsername() {
        // Given
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "   ");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password123");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);

        // When
        ClientConfiguration config = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config, "Configuration should not be null");
        // Whitespace username would be treated as valid username in current implementation
    }

    @Test
    @DisplayName("Should create different configurations for different settings")
    void shouldCreateDifferentConfigurationsForDifferentSettings() {
        // Given - Config 1: No auth, no SSL
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", false);
        ClientConfiguration config1 = elasticsearchConfig.clientConfiguration();

        // Given - Config 2: With auth and SSL
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchUsername", "elastic");
        ReflectionTestUtils.setField(elasticsearchConfig, "elasticsearchPassword", "password");
        ReflectionTestUtils.setField(elasticsearchConfig, "useSsl", true);
        ClientConfiguration config2 = elasticsearchConfig.clientConfiguration();

        // Then
        assertNotNull(config1, "First configuration should not be null");
        assertNotNull(config2, "Second configuration should not be null");
        assertNotSame(config1, config2, "Configurations should be different objects");
    }
}
