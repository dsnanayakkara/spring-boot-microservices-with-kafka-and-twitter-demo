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
        // IMPORTANT ORDER: connectedTo() -> usingSsl() -> withBasicAuth() -> timeouts -> build()
        // SSL and auth must be configured BEFORE timeout configurations

        if (hasAuth && useSsl) {
            // Both SSL and auth (SSL first, then auth, then timeouts)
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .usingSsl()
                    .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .build();
        } else if (hasAuth) {
            // Auth only (auth before timeouts)
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                    .build();
        } else if (useSsl) {
            // SSL only (SSL before timeouts)
            return ClientConfiguration.builder()
                    .connectedTo(elasticConfigData.getConnectionUrl())
                    .usingSsl()
                    .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                    .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
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

