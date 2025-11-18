package com.microservices.demo.elastic.index.client.service.impl;

import com.microservices.demo.elastic.index.client.repository.SocialEventElasticsearchRepository;
import com.microservices.demo.elastic.index.client.service.ElasticIndexClient;
import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "true", matchIfMissing = true)
public class SocialEventElasticIndexClient implements ElasticIndexClient<SocialEventIndexModel> {

    private static final Logger LOG = LoggerFactory.getLogger(SocialEventElasticIndexClient.class);

    private final SocialEventElasticsearchRepository repository;

    public SocialEventElasticIndexClient(SocialEventElasticsearchRepository elasticsearchRepository) {
        this.repository = elasticsearchRepository;
    }

    @Override
    public List<String> save(List<SocialEventIndexModel> documents) {
        List<SocialEventIndexModel> savedDocuments = (List<SocialEventIndexModel>) repository.saveAll(documents);
        List<String> documentIds = savedDocuments.stream()
                .map(SocialEventIndexModel::getId)
                .collect(Collectors.toList());
        LOG.info("Successfully indexed {} documents to Elasticsearch", documentIds.size());
        return documentIds;
    }
}
