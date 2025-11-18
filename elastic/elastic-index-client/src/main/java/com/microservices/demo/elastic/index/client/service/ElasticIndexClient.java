package com.microservices.demo.elastic.index.client.service;

import com.microservices.demo.elastic.model.index.SocialEventIndexModel;

import java.util.List;

public interface ElasticIndexClient<T> {
    List<String> save(List<T> documents);
}
