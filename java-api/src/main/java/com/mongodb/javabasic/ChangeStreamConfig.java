package com.mongodb.javabasic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStreamProcess;
import com.mongodb.javabasic.model.ChangeStreamProcessConfig;
import com.mongodb.javabasic.service.AggregationService;

import jakarta.annotation.PostConstruct;

@Configuration
public class ChangeStreamConfig {

        @Value("${settings.changeStream.batchSize}")
        private int batchSize;

        @Value("${settings.changeStream.maxAwaitTime}")
        private int maxAwaitTime;

        @Value("${settings.changeStream.watchColls}")
        private String[] watchColls;

        private Logger logger = LoggerFactory.getLogger(getClass());

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private AggregationService aggregationService;

        @PostConstruct
        public void startChangeStream() throws Exception {
                logger.info("Start watching:" + watchColls);
                ChangeStream<Document> changeStream = new ChangeStream<>();
                changeStream.run((ChangeStreamProcessConfig<Document> config) -> {
                        List<Bson> pipeline = (List.of(Aggregates.match(
                                        Filters.in("ns.coll", watchColls))));
                        return new ChangeStreamProcess<Document>(config,
                                        (e) -> {
                                                try {
                                                        // logger.info("Body:" + e.getFullDocument());
                                                        Document doc = e.getFullDocument();
                                                        if (doc != null) {
                                                                switch (e.getNamespace().getCollectionName()) {
                                                                        case "ori_country_info":
                                                                                aggregationService.getPipelineResults(
                                                                                                "ori_country_info",
                                                                                                "1_country_to_port.json",
                                                                                                Document.class);
                                                                                // Can be remove if exclude country are
                                                                                // mnanaged by other features
                                                                                aggregationService.getPipelineResults(
                                                                                                "ori_country_info",
                                                                                                "2_country_to_exclude.json",
                                                                                                Document.class);
                                                                                break;

                                                                        case "ori_port":
                                                                                aggregationService.getPipelineResults(
                                                                                                "ori_port",
                                                                                                "3_ori_ports_to_flatten_ports.ftl",
                                                                                                Document.class,
                                                                                                Map.of("portCodes", List.of(doc
                                                                                                                .getString("FullPortList"))));
                                                                                break;
                                                                        case "tsp_port_info":
                                                                                // port_code +
                                                                                // airports.iata_airport_code
                                                                                List<String> portCodes = new ArrayList<>();
                                                                                portCodes.addAll(doc.getList("airports", Document.class).stream().map(a->a.getString("iata_airport_code")).toList());
                                                                                portCodes.add(doc
                                                                                .getString("port_code"));
                                                                                aggregationService.getPipelineResults(
                                                                                                "ori_port",
                                                                                                "3_ori_ports_to_flatten_ports.ftl",
                                                                                                Document.class,
                                                                                                Map.of("portCodes", portCodes));
                                                                                break;

                                                                        case "tsp_flatten_ports":
                                                                        case "tsp_exclude_country":
                                                                        case "tsp_uo_direct":
                                                                        case "tsp_uo_redirect":
                                                                                /*aggregationService.getPipelineResults(
                                                                                                "tsp_flatten_ports",
                                                                                                "4_flatten_ports_to_routes.json",
                                                                                                Document.class);*/
                                                                                break;

                                                                }

                                                        }
                                                } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                }
                                        }) {
                                @Override
                                public ChangeStreamIterable<Document> initChangeStream(List<Bson> p) {
                                        if (pipeline != null && pipeline.size() > 0)
                                                p.addAll(pipeline);
                                        ChangeStreamIterable<Document> cs = mongoTemplate.getDb()
                                                        .watch(p, Document.class)
                                                        .batchSize(batchSize)
                                                        .maxAwaitTime(maxAwaitTime, TimeUnit.MILLISECONDS)
                                                        .fullDocument(FullDocument.UPDATE_LOOKUP);

                                        return cs;
                                }

                        };
                }, true);

        }

}
