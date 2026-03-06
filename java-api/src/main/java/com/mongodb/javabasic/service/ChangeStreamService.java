package com.mongodb.javabasic.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.client.model.changestream.UpdateDescription;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.javabasic.model.Aggregation;
import com.mongodb.javabasic.model.ChangeStream;
import com.mongodb.javabasic.model.ChangeStream.Mode;
import com.mongodb.javabasic.model.ChangeStream.ResumeStrategy;
import com.mongodb.javabasic.model.ChangeStreamRegistry;
import com.mongodb.javabasic.model.PipelineTemplate;
import com.mongodb.javabasic.repositories.PipelineRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class ChangeStreamService<T> {
	Logger logger = LoggerFactory.getLogger(getClass());
	private static final String INDEX_NAME = "ttl";
	private static final String RESUME_TOKEN_COLL = "_resumeTokens";
	private static final String CSID_FIELD = "_id.cs";
	private static final String HOST_FIELD = "_id.h";
	private static final String DATE_FIELD = "at";
	private static final String TOKEN_FIELD = "t";
	private static final String CHANGE_STREAMS_FIELD = "cs";

	@Value("${settings.changeStream.tokenMaxLifeTime}")
	private long tokenMaxLifeTime;
	@Value("${settings.instances.collection}")
	private String instancesCollectionName;
	@Value("${settings.instances.maxTimeout}")
	private long maxTimeout;

	@Autowired
	private MongoClient mongoClient;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private Executor taskExecutor;

	@Autowired
	private Map<String, ChangeStreamRegistry<T>> changeStreams;

	@Autowired
	private ApplicationEventService<Document> applicationEventService;
	@Autowired
	private AggregationService aggregationService;
	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private String podName;
	@Autowired
	private Set<String> instances;

	@PostConstruct
	private void init() {
		mongoTemplate.getCollection(RESUME_TOKEN_COLL).createIndex(Indexes.descending(DATE_FIELD),
				new IndexOptions().expireAfter(tokenMaxLifeTime, TimeUnit.MILLISECONDS).name(INDEX_NAME));

		logger.info("subscribe on node change");
		applicationEventService.subscribe(ae -> {
			try {
				if (OperationType.INSERT == OperationType.valueOf(ae.getName())
						|| OperationType.DELETE == OperationType.valueOf(ae.getName())) {
					logger.info("change streams running in this node " + changeStreams.keySet());
				}
				for (String csId : changeStreams.keySet()) {
					ChangeStreamRegistry<T> reg = changeStreams.get(csId);
					ChangeStream<T> cs = reg.getChangeStream();
					switch (OperationType.valueOf(ae.getName())) {
						case INSERT:
							if (Mode.AUTO_SCALE == cs.getMode()) {
								this._stop(reg);
								run(reg, ae.getDocument() != null ? ((Document) ae.getDocument()).getDate("at") : null);
							} else {
								reg.getInstances().add(ae.getKey());
							}
							break;
						case UPDATE:
							if (Mode.AUTO_RECOVER == cs.getMode()) {
								UpdateDescription ud = ae.getUpdateDescription();
								if (ud != null) {
									if (ud.getRemovedFields().contains(CHANGE_STREAMS_FIELD)) {
										logger.info(
												"stopping change stream " + csId);
										this._stop(reg);
									} else {
										BsonDocument updatedFields = ud.getUpdatedFields();
										if (updatedFields != null && updatedFields.containsKey(CHANGE_STREAMS_FIELD)) {
											if (updatedFields.getArray(CHANGE_STREAMS_FIELD)
													.contains(new BsonString(csId))) {
												if (!reg.getChangeStream().isRunning()) {
													// restart change stream
													this.run(reg);
												}
											} else {
												logger.info(
														"stopping change stream " + csId);
												this._stop(reg);
											}
										}
									}
									/*
									 * if (ud.getRemovedFields().contains(CHANGE_STREAMS_FIELD)
									 * || (updatedFields != null && updatedFields.containsKey(CHANGE_STREAMS_FIELD)
									 * && !updatedFields.getArray(CHANGE_STREAMS_FIELD)
									 * .contains(new BsonString(csId)))) {
									 * logger.info(
									 * "stopping change stream " + csId + ", remove from registry.");
									 * this._stop(reg);
									 * // changeStreams.remove(csId);
									 * }
									 */
								}
							} else if (Mode.AUTO_SCALE == cs.getMode()) {
								if (!cs.isRunning()) {
									logger.info(cs.getId() + " is not yet running in " + podName + " start it now.");
									scale(reg);
								}
							}
							break;
						case DELETE:
							if (Mode.AUTO_RECOVER == cs.getMode()) {
								if (podName.equals(ae.getKey())) {
									if (changeStreams.containsKey(cs.getId())
											&& cs.isRunning()) {
										logger.info(
												"Change stream running in this node should stop.");
										this._stop(reg);
									}
								} else {
									if (changeStreams.containsKey(cs.getId())
											&& changeStreams.get(cs.getId()).getChangeStream().isRunning()) {
										logger.info(
												ae.getKey() + " is dead, I'm still running change stream:"
														+ cs.getId());
									} else {
										logger.info(
												ae.getKey() + " is dead, " + podName
														+ " try to take over change stream:"
														+ cs.getId());
										this._stop(reg);
										run(reg);
									}
								}
							} else if (Mode.AUTO_SCALE == cs.getMode()) {
								this._stop(reg);
								run(reg, ((Document) ae.getDocument()).getDate("at"));
							} else {
								reg.getInstances().remove(ae.getKey());
							}
							break;
						default:
							break;
					}
				}
			} catch (RuntimeException e) {
				logger.error("Unexpected error while processing node changes:", e);
			}
		});
	}

	@PreDestroy
	private void destroy() {
		applicationEventService.clear();
		for (String csId : changeStreams.keySet()) {
			changeStreams.get(csId).getChangeStream().setRunning(false);
			changeStreams.remove(csId);
		}
	}

	public void run(ChangeStreamRegistry<T> reg) {
		this.run(reg, null);
	}

	@Retryable(includes = { MongoException.class }, maxRetries = 10, delay = 5000, multiplier = 2)
	public void run(ChangeStreamRegistry<T> reg, Date earliest) {
		logger.info("Start running change stream");
		changeStreams.put(reg.getChangeStream().getId(), reg);
		ChangeStream<T> cs = reg.getChangeStream();

		logger.info("ResumeStrategy:" + cs.getResumeStrategy());
		if (ResumeStrategy.NONE != cs.getResumeStrategy()) {
			PipelineTemplate p = this.pipelineRepository.findByName("csScaleToken");
			if (p == null || p.getContent() == null)
				throw new RuntimeException("Pipeline not found");
			Aggregation<Document> agg = Aggregation.of(RESUME_TOKEN_COLL, p.getContent());
			Map<String, Object> variables = new HashMap<>();
			variables.put("earliest", earliest);
			List<Document> l = aggregationService.execute(agg, variables);
			if (l.size() > 0) {
				cs.resumeAfter(l.get(0).getString(TOKEN_FIELD));
			}
		}

		logger.info("Mode:" + cs.getMode());
		if (Mode.AUTO_RECOVER == cs.getMode()) {
			final ClientSession clientSession = mongoClient.startSession();
			try {
				clientSession.withTransaction(new TransactionBody<Void>() {
					@Override
					public Void execute() {
						Document doc = mongoTemplate.getCollection(instancesCollectionName)
								.find(clientSession, Filters.eq(CHANGE_STREAMS_FIELD, cs.getId())).first();
						if (doc == null) {
							UpdateResult ur = mongoTemplate.getCollection(instancesCollectionName).updateOne(
									clientSession,
									Filters.eq("_id", podName),
									Updates.combine(Updates.set("_id", podName),
											Updates.set(DATE_FIELD, Instant.now().plus(maxTimeout, ChronoUnit.MILLIS)),
											Updates.addToSet(CHANGE_STREAMS_FIELD, cs.getId())),
									new UpdateOptions().upsert(true));
							logger.info("Update result:" + ur);
							if (ur.getUpsertedId() != null || ur.getModifiedCount() > 0) {
								logger.info(podName + " wins");
								start(reg);
							}
						} else {
							reg.getInstances().add(doc.getString("_id"));
							if (podName.equals(doc.getString("_id"))) {
								if (changeStreams.containsKey(cs.getId())
										&& cs.isRunning()) {
									logger.info("Change stream already running");
								} else {
									start(reg);
								}
							} else {
								logger.info("Change stream already running in other node:" + doc);
							}
						}
						return null;
					}

				}, TransactionOptions.builder()
						.writeConcern(WriteConcern.MAJORITY).readConcern(ReadConcern.MAJORITY)
						.readPreference(ReadPreference.primary())
						.build());
			} catch (RuntimeException e) {
				logger.error("Unexpected error while registering change stream " + cs.getId() + ":", e);
				throw e;
			} finally {
				clientSession.close();
			}
		} else if (Mode.AUTO_SCALE == cs.getMode()) {
			scale(reg);
		} else {
			start(reg);
		}
	}

	private void start(ChangeStreamRegistry<T> reg) {
		CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(() -> {
			try {
				if (reg.getCollectionName() != null) {
					reg.getChangeStream().watch(mongoTemplate.getCollection(reg.getCollectionName()), reg,
							resumeToken -> {
								saveCheckpoint(reg.getChangeStream().getId(), resumeToken);
							});
				} else {
					reg.getChangeStream().watch(mongoTemplate.getDb(), reg, resumeToken -> {
						saveCheckpoint(reg.getChangeStream().getId(), resumeToken);
					});
				}
			} catch (RuntimeException e) {
				logger.error("Stopping change stream '" + reg.getChangeStream().getId() + "'' due to unexpected error:",
						e);
				this._stop(reg);
				// Recover for unexpected exception
				this.run(reg);
			}
			return null;
		}, taskExecutor);
		reg.setCompletableFuture(completableFuture);
		reg.getInstances().add(podName);
		logger.info(reg.getInstances() + " are running change stream " + reg.getChangeStream().getId());
		logger.info("Change stream " + reg.getChangeStream().getId() + " started");
	}

	public void stop(ChangeStreamRegistry<T> reg) {
		logger.info("Stop running change stream");
		this._stop(reg);
		mongoTemplate.getCollection(instancesCollectionName).findOneAndUpdate(
				Filters.eq(CHANGE_STREAMS_FIELD, reg.getChangeStream().getId()),
				Updates.pull(CHANGE_STREAMS_FIELD, podName));
	}

	public void _stop(ChangeStreamRegistry<T> reg) {
		reg.getChangeStream().setRunning(false);
		reg.getInstances().remove(podName);
		if (reg.getCompletableFuture() != null)
			reg.getCompletableFuture().join();
	}

	private void scale(ChangeStreamRegistry<T> reg) {
		ChangeStream<T> cs = reg.getChangeStream();
		int index = new ArrayList<String>(this.instances).indexOf(podName);
		reg.setInstanceSize(instances.size());
		reg.setInstanceIndex(index);
		reg.getInstances().clear();
		reg.getInstances().addAll(instances);
		if (instances.size() > 0 && index >= 0) {
			if (cs.isRunning()) {
				this._stop(reg);
			}
			logger.info("change stream " + (index + 1) + " of " + this.instances.size()
					+ " start running in "
					+ podName);
			start(reg);
		} else {
			logger.info("Node " + podName + " is not ready to run change stream:" + cs.getId());
		}
	}

	@Retryable(includes = { MongoException.class }, maxRetries = 10, delay = 1000, multiplier = 2)
	private void saveCheckpoint(String csId, BsonString token) {
		logger.info("save checkpoint");
		mongoTemplate.getCollection(RESUME_TOKEN_COLL).updateOne(
				Filters.and(Filters.eq(CSID_FIELD, csId), Filters.eq(HOST_FIELD, podName)),
				Updates.combine(Updates.set(CSID_FIELD, csId),
						Updates.combine(Updates.set(HOST_FIELD, podName),
								Updates.set(DATE_FIELD, new Date()),
								Updates.set(TOKEN_FIELD, token))),
				new UpdateOptions().upsert(true));
	}

}
