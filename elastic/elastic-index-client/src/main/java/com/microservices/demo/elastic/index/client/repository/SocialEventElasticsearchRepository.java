package com.microservices.demo.elastic.index.client.repository;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialEventElasticsearchRepository extends ElasticsearchRepository<SocialEventIndexModel, String> {
}
