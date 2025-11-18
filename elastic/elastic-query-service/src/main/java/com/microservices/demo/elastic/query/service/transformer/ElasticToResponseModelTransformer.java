package com.microservices.demo.elastic.query.service.transformer;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import com.microservices.demo.elastic.query.service.model.SocialEventQueryResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticToResponseModelTransformer {

    public SocialEventQueryResponseModel getResponseModel(SocialEventIndexModel indexModel) {
        return SocialEventQueryResponseModel.builder()
                .id(indexModel.getId())
                .userId(indexModel.getUserId())
                .text(indexModel.getText())
                .createdAt(indexModel.getCreatedAt())
                .build();
    }

    public List<SocialEventQueryResponseModel> getResponseModels(List<SocialEventIndexModel> indexModels) {
        return indexModels.stream()
                .map(this::getResponseModel)
                .collect(Collectors.toList());
    }
}
