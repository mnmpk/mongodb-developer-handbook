package com.mongodb.javabasic.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.javabasic.model.TspIdConfigMapDocument;

public interface TspIdConfigMapDocumentRepository extends MongoRepository<TspIdConfigMapDocument, String> {
    List<TspIdConfigMapDocument> findByChannelAndUsed(String channel, boolean used);

    List<TspIdConfigMapDocument> findByChannel(String channel);

    TspIdConfigMapDocument findByChannelAndTspId(String channel, String tspId);

    Long countByChannelAndUsed(String channel, boolean used);
}