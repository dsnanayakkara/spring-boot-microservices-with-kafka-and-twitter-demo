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
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
                ClientConfiguration.builder()
                        .connectedTo(elasticConfigData.getConnectionUrl())
                        .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                        .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()));

        // Build configuration based on auth and SSL settings
        boolean hasAuth = elasticsearchUsername != null && !elasticsearchUsername.isEmpty();

        if (hasAuth && useSsl) {
            // Both auth and SSL
            return builder.withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .usingSsl()
                    .build();
        } else if (hasAuth) {
            // Auth only
            return builder.withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                    .build();
        } else if (useSsl) {
            // SSL only
            return builder.usingSsl()
                    .build();
        } else {
            // No auth, no SSL
            return builder.build();
        }
    }
}

