package com.mongodb.javabasic.config;

import java.util.LinkedHashMap;

import org.bson.Document;
import org.mongodb.spring.session.JacksonMongoSessionConverter;
import org.mongodb.spring.session.MongoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;

public class MongoSessionConverter extends JacksonMongoSessionConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    MongoConverter mongoConverter;

    @Override
    protected Document convert(MongoSession session) {
        Document doc = super.convert(session);
        // Presist custom data here
        doc.put("customData", session.getAttribute("data"));
        doc.get("attrs", Document.class).remove("data"); // Remove the default "data" attribute to avoid confusion
        return doc;
    }

    @Override
    protected MongoSession convert(Document sessionWrapper) {
        MongoSession session = super.convert(sessionWrapper);
        Document customValue = sessionWrapper.get("customData", Document.class);
        logger.debug("Extracted custom data value: {}", customValue);
        if (customValue != null) {
            session.setAttribute("data", mongoConverter.read(LinkedHashMap.class, customValue));
        }
        return session;
    }

}
