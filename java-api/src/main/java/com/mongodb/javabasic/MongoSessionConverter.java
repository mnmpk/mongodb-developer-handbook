package com.mongodb.javabasic;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.mongo.AbstractMongoSessionConverter;
import org.springframework.session.data.mongo.MongoSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.javabasic.model.SessionsEntity;

public class MongoSessionConverter extends AbstractMongoSessionConverter {
    private static final String PRINCIPAL_FIELD_NAME = "principal";

    @Autowired
    private CodecRegistry pojoCodecRegistry;
    @Autowired
    MongoConverter mongoConverter;

    @Override
    protected Query getQueryForIndex(String indexName, Object indexValue) {
        if (FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Query.query(Criteria.where(PRINCIPAL_FIELD_NAME).is(indexValue));
        } else {
            return Query.query(Criteria.where(indexName).is(indexValue));
        }
    }

    @Override
    protected DBObject convert(MongoSession session) {
        SessionsEntity sessionEntity = new SessionsEntity();
        sessionEntity.setId(session.getId());
        sessionEntity.setCreationTime(session.getCreationTime());
        sessionEntity.setLastAccessedTime(session.getLastAccessedTime());
        sessionEntity.setExpireAt(
                Date.from(session.getLastAccessedTime().plusSeconds(session.getMaxInactiveInterval().getSeconds())));
        sessionEntity.setExpireAfterSeconds(session.getMaxInactiveInterval().getSeconds());
        sessionEntity.setData(new LinkedHashMap<String, Object>());
        for (String attr : session.getAttributeNames()) {
            sessionEntity.getData().put(attr, session.getAttribute(attr));
        }
        BasicDBObject dbObj=new BasicDBObject();
        mongoConverter.write(sessionEntity, dbObj);
        return dbObj;
        /*BsonDocumentWriter bsonWriter = new BsonDocumentWriter(new BsonDocument());
        pojoCodecRegistry.get(SessionsEntity.class).encode(bsonWriter, sessionEntity,
                EncoderContext.builder().build());
        return new BasicDBObject(bsonWriter.getDocument());*/

    }

    @Override
    protected MongoSession convert(Document sessionWrapper) {
        SessionsEntity s = mongoConverter.read(SessionsEntity.class, sessionWrapper);
        /*SessionsEntity s = pojoCodecRegistry.get(SessionsEntity.class).decode(
                sessionWrapper.toBsonDocument().asBsonReader(),
                DecoderContext.builder().build());*/
        MongoSession session = new MongoSession(s.getId(), s.getCreationTime().toEpochMilli());
        session.setLastAccessedTime(s.getLastAccessedTime());
        session.setMaxInactiveInterval(Duration.of(s.getExpireAfterSeconds(), ChronoUnit.SECONDS));
        for (String attr : s.getData().keySet()) {
            session.setAttribute(attr, s.getData().get(attr));
        }
        return session;

    }

}
