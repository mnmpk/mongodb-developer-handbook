package com.mongodb.javabasic.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.TspIdConfigMapDocumentSuggested;

public interface TspIdConfigMapDocumentSuggestedRepository extends MongoRepository<TspIdConfigMapDocumentSuggested, String> {
    List<TspIdConfigMapDocumentSuggested> findByChannelAndUsedExists(String channel, boolean used);

    List<TspIdConfigMapDocumentSuggested> findByChannel(String channel);

    TspIdConfigMapDocumentSuggested findByChannelAndTspId(String channel, String tspId);

    Long countByChannelAndUsedExists(String channel, boolean used);
}