package com.microservices.demo.elastic.query.client.service;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ElasticQueryClient {

    SocialEventIndexModel getIndexModelById(String id);

    Page<SocialEventIndexModel> getAllIndexModels(Pageable pageable);

    Page<SocialEventIndexModel> getIndexModelByText(String text, Pageable pageable);

    List<SocialEventIndexModel> getIndexModelByUserId(Long userId);
}
