package com.microservices.demo.elastic.query.client.repository;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialEventElasticsearchQueryRepository extends ElasticsearchRepository<SocialEventIndexModel, String> {

    Page<SocialEventIndexModel> findByText(String text, Pageable pageable);

    List<SocialEventIndexModel> findByUserId(Long userId);
}
