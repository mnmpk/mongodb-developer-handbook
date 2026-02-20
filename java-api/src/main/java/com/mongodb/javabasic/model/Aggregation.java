package com.mongodb.javabasic.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonElement;
import org.bson.BsonNull;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.util.BsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Aggregation<T> {
    private static String PLACEHOLDER_KEY = "_ph";
    private String collectionName;
    private List<? extends Bson> pipelineTemplate;
    private Class<T> documentClass;
    private Long permission;

    public static Aggregation<Document> of(String collectionName) {
        return of(collectionName, List.of());
    }

    public static Aggregation<Document> of(String collectionName, List<? extends Bson> pipelineTemplate) {
        return new Aggregation<Document>(collectionName, pipelineTemplate, Document.class, null);
    }

    public <NewT> Aggregation<NewT> withClass(Class<NewT> clazz) {
        return new Aggregation<NewT>(this.collectionName, this.pipelineTemplate, clazz, this.permission);
    }

    public Aggregation<T> withPermissionCheck(Long permission) {
        return new Aggregation<T>(this.collectionName, this.pipelineTemplate, this.documentClass, permission);
    }

    public BsonValue merge() {
        return this.merge(Optional.empty());
    }

    public BsonValue merge(Optional<Map<String, Object>> variables) {
        if (this.pipelineTemplate == null)
            throw new RuntimeException("Empty pipeline template");
        return this.traverse(new BsonArray(this.pipelineTemplate.stream()
                .map(s -> (BsonValue) ((Bson) s).toBsonDocument()).collect(Collectors.toList())), variables.map(vs -> {
                    return vs.entrySet().stream().map(entry -> {
                        if (entry.getValue() instanceof Bson
                                && !(entry.getValue() instanceof BsonValue)) {
                            return Map.entry(entry.getKey(),
                                    (BsonValue) ((Bson) entry.getValue())
                                            .toBsonDocument());
                        }
                        return Map.entry(entry.getKey(),
                                entry.getValue() != null ? BsonUtils.simpleToBsonValue(entry.getValue()) : BsonNull.VALUE);
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }).orElse(new HashMap<>()));
    }

    private BsonValue traverse(BsonValue bson, Map<String, BsonValue> map) {
        Function<BsonValue, BsonValue> replaceValue = (value) -> {
            if (value.isDocument()) {
                BsonDocument d = value.asDocument();
                if (d.containsKey(PLACEHOLDER_KEY)) {
                    return map.get(d.getString(PLACEHOLDER_KEY).getValue());
                } else {
                    return traverse(value, map);
                }
            } else if (value.isArray()) {
                return traverse(value, map);
            } else {
                return value;
            }
        };
        if (bson.isDocument()) {
            return new BsonDocument(bson.asDocument().entrySet().stream().map(entry -> {
                return Map.entry(entry.getKey(), replaceValue.apply(entry.getValue()));
            }).map(entry -> new BsonElement(entry.getKey(), entry.getValue())).toList());
        } else if (bson.isArray()) {
            return new BsonArray(bson.asArray().stream().map(replaceValue).toList());
        } else {
            return replaceValue.apply(bson);
        }
    }
}
