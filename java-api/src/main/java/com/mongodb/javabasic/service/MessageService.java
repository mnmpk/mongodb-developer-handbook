package com.mongodb.javabasic.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.client.model.changestream.UpdateDescription;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStream.Mode;
import com.mongodb.javabasic.model.ChangeStreamRegistry;
import com.mongodb.javabasic.model.Message;
import com.mongodb.javabasic.model.Message.Type;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 *
 * @param <T>
 */
@Controller
public class MessageService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final String INDEX_KEY = "cAt";
	private static final String INDEX_NAME = "ttl";
	private static final String MESSAGE_COLL = "_messages";
	@Autowired
	private ChangeStreamService<Document> changeStreamService;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private CodecRegistry pojoCodecRegistry;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Value("${settings.messaging.maxLifeTime}")
	private long messageMaxLifeTime;

	private ChangeStream<Document> cs;

	@PostConstruct
	private void init() {
		mongoTemplate.getCollection(MESSAGE_COLL).createIndex(Indexes.descending(INDEX_KEY),
				new IndexOptions().expireAfter(messageMaxLifeTime, TimeUnit.MILLISECONDS).name(INDEX_NAME));
		this.watch();
	}

	@PreDestroy
	private void clear() {
		if (cs != null)
			cs.setRunning(false);
	}

	private void watch() {
		this.cs = ChangeStream.of("message-service", Mode.BOARDCAST,
				List.of(Aggregates
						.match(Filters.in("ns.coll",
								List.of(MESSAGE_COLL, "tRatingFinal")))));
		changeStreamService.run(ChangeStreamRegistry.<Document>builder().body(e -> {
			switch (e.getNamespace().getCollectionName()) {
				case MESSAGE_COLL:
					if (OperationType.INSERT == e.getOperationType()) {
						Document fullDoc = e.getFullDocument();
						Message message = pojoCodecRegistry.get(Message.class).decode(
								fullDoc.toBsonDocument().asBsonReader(),
								DecoderContext.builder().build());
						message.setType(Type.RES);
						send(message);
					}
					break;
				default:
					logger.info("{} operation on Document {} in collection {}, send refresh command",
							e.getOperationType().getValue(),
							e.getDocumentKey(),
							e.getNamespace().getCollectionName());
					switch (e.getOperationType()) {
						case INSERT:
						case REPLACE:
							send(Message.builder().type(Message.Type.RES).target("/sync").content(e.getFullDocument())
									.build());
							break;
						case UPDATE:
							UpdateDescription updateDesc = e.getUpdateDescription();
							BsonDocument document = new BsonDocument();
							pojoCodecRegistry.get(UpdateDescription.class).encode(new BsonDocumentWriter(document),
									updateDesc, EncoderContext.builder().build());
							send(Message.builder().type(Message.Type.RES).target("/sync")
									.content(new Document(document))
									.build());
							break;
						case DELETE:
							send(Message.builder().type(Message.Type.RES).target("/sync")
									.content(new Document(e.getDocumentKey()))
									.build());
							break;
						default:
							break;
					}
					send(Message.builder().type(Message.Type.RES).target("/cmd")
							.content(
									new Document("type", "REFRESH").append("coll",
											e.getNamespace().getCollectionName()))
							.build());
					break;
			}
		}).changeStream(this.cs).build());
	}

	public Message queue(Message message) {
		logger.info("Boarcast message received, append to the queue");
		Date now = new Date();
		message.setCreatedAt(now);
		message.setType(null);
		mongoTemplate.getCollection(MESSAGE_COLL).withDocumentClass(Message.class)
				.insertOne(message);
		message.setType(Type.ACK);
		return message;
	}

	public void send(Message message) {
		this.simpMessagingTemplate.convertAndSend(message.getTarget(),
				message);
	}

}