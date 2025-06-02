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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.mongo.AbstractMongoSessionConverter;
import org.springframework.session.data.mongo.MongoSession;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.javabasic.model.SessionsEntity;

public class MongoSessionConverter extends AbstractMongoSessionConverter {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String ATTRS_FIELD_NAME = "attrs.";
    private static final String SESSION_ENTITY_ATTR_NAME = "data";
    private static final String PRINCIPAL_FIELD_NAME = "principal";

    @Autowired
    MongoConverter mongoConverter;

    @Override
    protected Query getQueryForIndex(String indexName, Object indexValue) {
        if (FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Query.query(Criteria.where(PRINCIPAL_FIELD_NAME).is(indexValue));
        } else {
            return Query.query(Criteria.where(ATTRS_FIELD_NAME+indexName).is(indexValue));
        }
    }

    @Override
    protected DBObject convert(MongoSession session) {
        BasicDBObject dbObj=new BasicDBObject();
        dbObj.put(PRINCIPAL_FIELD_NAME, extractPrincipal(session));
        //dbObj.put(EXPIRE_AT_FIELD_NAME, session.getExpireAt());
        mongoConverter.write(session, dbObj);
        return dbObj;
    }

    @Override
    protected MongoSession convert(Document sessionWrapper) {
        MongoSession session = new MongoSession(sessionWrapper.getString("_id"));
        Document attrs = sessionWrapper.get("attrs", Document.class);
        if(sessionWrapper!=null && attrs!=null && attrs.containsKey(SESSION_ENTITY_ATTR_NAME)) {
            SessionsEntity s = mongoConverter.read(SessionsEntity.class, attrs.get(SESSION_ENTITY_ATTR_NAME, Document.class));
            session.setAttribute(SESSION_ENTITY_ATTR_NAME, s);
        }else{
            session.setAttribute(SESSION_ENTITY_ATTR_NAME, new SessionsEntity());
        }
        return session;

    }

}
