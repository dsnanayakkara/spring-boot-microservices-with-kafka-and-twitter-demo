package com.microservices.demo.elastic.config;

import com.microservices.demo.config.ElasticConfigData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.microservices.demo.elastic.index.client.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticConfigData elasticConfigData;

    @Value("${elasticsearch.username:}")
    private String elasticsearchUsername;

    @Value("${elasticsearch.password:}")
    private String elasticsearchPassword;

    @Value("${elasticsearch.use-ssl:false}")
    private boolean useSsl;

    public ElasticsearchConfig(ElasticConfigData configData) {
        this.elasticConfigData = configData;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        // Check if authentication is configured
        boolean hasAuth = elasticsearchUsername != null && !elasticsearchUsername.isEmpty();

        // Build configuration in one chain without intermediate variables
        // IMPORTANT: SSL must be configured BEFORE authentication in the builder chain

        if (hasAuth && useSsl) {
            // Both SSL and auth (SSL first, then auth)
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .usingSsl()
                    .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .build();
        } else if (hasAuth) {
            // Auth only
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .build();
        } else if (useSsl) {
            // SSL only
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .usingSsl()
                    .build();
        } else {
            // No auth, no SSL
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .build();
        }
    }
}

