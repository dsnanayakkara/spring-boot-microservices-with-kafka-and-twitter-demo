package com.microservices.demo.elasticsearch.service.transformer;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;
import com.microservices.demo.kafka.avro.model.SocialEventAvroModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvroToElasticModelTransformer {

    public List<SocialEventIndexModel> getElasticModels(List<SocialEventAvroModel> avroModels) {
        return avroModels.stream()
                .map(this::transform)
                .collect(Collectors.toList());
    }

    private SocialEventIndexModel transform(SocialEventAvroModel avroModel) {
        return SocialEventIndexModel.builder()
                .id(String.valueOf(avroModel.getId()))
                .userId(avroModel.getUserId())
                .text(avroModel.getText() != null ? avroModel.getText().toString() : null)
                .createdAt(avroModel.getCreatedAt() != null ?
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(avroModel.getCreatedAt()),
                                ZoneId.systemDefault()
                        ) : null)
                .build();
    }
}
