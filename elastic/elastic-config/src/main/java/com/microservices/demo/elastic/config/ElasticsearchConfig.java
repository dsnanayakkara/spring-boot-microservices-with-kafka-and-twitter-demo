package com.microservices.demo.elastic.config;

import com.microservices.demo.config.ElasticConfigData;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.microservices.demo.elastic.index.client.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticConfigData elasticConfigData;

    public ElasticsearchConfig(ElasticConfigData configData) {
        this.elasticConfigData = configData;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticConfigData.getConnectionUrl())
                .withConnectTimeout(Duration.ofMillis(elasticConfigData.getConnectionTimeoutMs()))
                .withSocketTimeout(Duration.ofMillis(elasticConfigData.getSocketTimeoutMs()))
                .build();
    }
}
