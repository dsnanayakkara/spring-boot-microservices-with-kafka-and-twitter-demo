package com.microservices.demo.elasticsearch.service.init;

import com.microservices.demo.config.ElasticConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchIndexInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchIndexInitializer.class);

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticConfigData elasticConfigData;

    public ElasticsearchIndexInitializer(ElasticsearchOperations operations,
                                          ElasticConfigData configData) {
        this.elasticsearchOperations = operations;
        this.elasticConfigData = configData;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() {
        String indexName = elasticConfigData.getIndexName();
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);

        if (!indexOperations.exists()) {
            LOG.info("Creating Elasticsearch index: {}", indexName);

            // Create index settings
            Settings settings = Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 1)
                    .put("index.refresh_interval", "1s")
                    .put("analysis.analyzer.default.type", "standard")
                    .build();

            indexOperations.create(settings);

            LOG.info("Successfully created index: {} with settings", indexName);
        } else {
            LOG.info("Index {} already exists", indexName);
        }
    }
}
