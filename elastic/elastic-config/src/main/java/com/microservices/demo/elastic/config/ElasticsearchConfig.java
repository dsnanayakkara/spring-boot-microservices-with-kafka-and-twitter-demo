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

        // Add authentication if credentials are provided
        if (elasticsearchUsername != null && !elasticsearchUsername.isEmpty()) {
            builder = builder.withBasicAuth(elasticsearchUsername, elasticsearchPassword);
        }

        // Enable SSL if configured
        if (useSsl) {
            builder = builder.usingSsl();
        }

        return builder.build();
    }
}

