package com.microservices.demo.elastic.query.client.service.impl;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import com.microservices.demo.elastic.query.client.repository.SocialEventElasticsearchQueryRepository;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialEventElasticQueryClient implements ElasticQueryClient {

    private static final Logger LOG = LoggerFactory.getLogger(SocialEventElasticQueryClient.class);

    private final SocialEventElasticsearchQueryRepository repository;

    public SocialEventElasticQueryClient(SocialEventElasticsearchQueryRepository elasticsearchQueryRepository) {
        this.repository = elasticsearchQueryRepository;
    }

    @Override
    public SocialEventIndexModel getIndexModelById(String id) {
        LOG.info("Querying Elasticsearch by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    @Override
    public Page<SocialEventIndexModel> getAllIndexModels(Pageable pageable) {
        LOG.info("Querying all documents with pagination. Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Override
    public Page<SocialEventIndexModel> getIndexModelByText(String text, Pageable pageable) {
        LOG.info("Querying Elasticsearch by text: '{}' with pagination. Page: {}, Size: {}",
                text, pageable.getPageNumber(), pageable.getPageSize());
        return repository.findByText(text, pageable);
    }

    @Override
    public List<SocialEventIndexModel> getIndexModelByUserId(Long userId) {
        LOG.info("Querying Elasticsearch by userId: {}", userId);
        return repository.findByUserId(userId);
    }
}
