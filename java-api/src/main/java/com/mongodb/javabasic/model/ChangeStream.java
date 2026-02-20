package com.mongodb.javabasic.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.client.model.changestream.FullDocumentBeforeChange;

import lombok.Data;

@Data
public class ChangeStream<T> {
    Logger logger = LoggerFactory.getLogger(getClass());

    public enum Mode {
        BOARDCAST,
        AUTO_RECOVER,
        AUTO_SCALE
    }

    public enum ResumeStrategy {
        EVERY,
        BATCH,
        TIME,
        NONE
    }

    private static final Long DEFAULT_SAVE_TOKEN_INTERVAL = 60 * 1000l;

    private String id;
    private Mode mode;
    private Integer batchSize;
    private Long maxAwaitTime;
    private ResumeStrategy resumeStrategy = ResumeStrategy.NONE;
    private FullDocumentBeforeChange fullDocumentBeforeChange;
    private FullDocument fullDocument;
    private List<Bson> pipeline = new ArrayList<>();
    private Class<T> documentClass;

    private ChangeStreamIterable<T> _changeStream;
    private MongoChangeStreamCursor<ChangeStreamDocument<T>> cursor;
    private boolean running = false;
    private Long saveTokenInterval = DEFAULT_SAVE_TOKEN_INTERVAL;
    private String resumeToken;
    private Consumer<ChangeStreamDocument<T>> consumer;

    public ChangeStream(String id, Mode mode, Integer batchSize, Long maxAwaitTime,
            ResumeStrategy resumeStrategy, long saveTokenInterval, FullDocumentBeforeChange fullDocumentBeforeChange,
            FullDocument fullDocument, List<Bson> pipeline, Class<T> clazz) {
        this.id = id;
        this.mode = mode;
        this.batchSize = batchSize;
        this.maxAwaitTime = maxAwaitTime;
        this.resumeStrategy = resumeStrategy;
        this.saveTokenInterval = saveTokenInterval;
        this.fullDocumentBeforeChange = fullDocumentBeforeChange;
        this.fullDocument = fullDocument;
        this.pipeline.clear();
        this.pipeline.addAll(pipeline);
        this.documentClass = clazz;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public void watch(MongoCollection<?> coll, ChangeStreamRegistry<T> reg,
            Consumer<BsonString> checkPoint) {
        this._changeStream = coll.watch(getScaledPipeline(reg), this.documentClass);
        this.watch(reg.getBody(), checkPoint);
    }

    public void watch(MongoDatabase db, ChangeStreamRegistry<T> reg, Consumer<BsonString> checkPoint) {
        this._changeStream = db.watch(getScaledPipeline(reg), this.documentClass);
        this.watch(reg.getBody(), checkPoint);
    }

    public void watch(Consumer<ChangeStreamDocument<T>> consumer, Consumer<BsonString> checkPoint) {
        logger.info("Intializing change stream " + this.getId());
        this.consumer = consumer;
        if (this.batchSize != null) {
            this._changeStream = this._changeStream.batchSize(this.batchSize);
        }
        if (this.maxAwaitTime != null) {
            this._changeStream = this._changeStream.maxAwaitTime(this.maxAwaitTime, TimeUnit.MILLISECONDS);
        }
        if (resumeToken != null) {
            this._changeStream = this._changeStream.resumeAfter(new Document("_data", resumeToken).toBsonDocument());
        }
        if (fullDocument != null) {
            this._changeStream = this._changeStream.fullDocument(fullDocument);
        }
        if (fullDocumentBeforeChange != null) {
            this._changeStream = this._changeStream.fullDocumentBeforeChange(fullDocumentBeforeChange);
        }
        this.cursor = this._changeStream.cursor();
        this.setRunning(true);
        ScheduledExecutorService scheduler = null;
        if (ResumeStrategy.TIME == this.getResumeStrategy()) {
            scheduler = this.timer(this, checkPoint);
        }

        try {
            while (this.isRunning()) {
                ChangeStreamDocument<T> e = this.getCursor().tryNext();
                if (e != null) {
                    this.getConsumer().accept(e);
                    if ((ResumeStrategy.BATCH == this.getResumeStrategy() && this.getCursor().available() == 0)
                            || ResumeStrategy.EVERY == this.getResumeStrategy()) {
                        checkPoint.accept(e.getResumeToken().getString("_data"));
                    }
                }
                // TODO: check if sleep needed to avoid high CPU
            }
        } finally {
            logger.info("Change stream " + this.getId() + " stopped");
            if (scheduler != null)
                scheduler.shutdown();
        }
    }

    private List<Bson> getScaledPipeline(ChangeStreamRegistry<T> reg) {
        List<Bson> list = new ArrayList<>(this.pipeline);
        if (Mode.AUTO_SCALE == reg.getChangeStream().getMode() && reg.getInstanceSize() > 0
                && reg.getInstanceIndex() >= 0) {
            list.add(new Document("$match",
                    new Document("$expr",
                            new Document("$eq", Arrays.asList(new Document("$abs",
                                    new Document("$mod", Arrays.asList(
                                            new Document("$toHashedIndexKey", "$documentKey._id"),
                                            reg.getInstanceSize()))),
                                    reg.getInstanceIndex()))))
                    .toBsonDocument());
        }
        return list;
    }

    private ScheduledExecutorService timer(ChangeStream<T> cs, Consumer<BsonString> checkPoint) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    if (cs.isRunning()) {
                        if (cs.getCursor().getResumeToken() != null)
                            checkPoint.accept(cs.getCursor().getResumeToken().getString("_data"));
                    } else {
                        scheduler.shutdown();
                        logger.info("timer stopped");
                    }
                } catch (MongoWriteException e) {
                    if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                        logger.info("Repeated resume token");
                    } else {
                        logger.error("Unable to save resume token", e);
                    }
                } catch (Exception e) {
                    scheduler.shutdown();
                    logger.error("Unexpected error:", e);
                    cs.setRunning(false);
                    throw e;
                }
            }
        };
        logger.info("start timer:" + cs.getSaveTokenInterval());
        scheduler.scheduleAtFixedRate(task, 0, cs.getSaveTokenInterval(), TimeUnit.MILLISECONDS);
        return scheduler;
    }

    public static ChangeStream<Document> of(String id) {
        return of(id, Mode.BOARDCAST);
    }

    public static ChangeStream<Document> of(String id, Mode mode) {
        return of(id, mode, null);
    }

    public static ChangeStream<Document> of(String id, Mode mode,
            List<Bson> pipeline) {
        return new ChangeStream<Document>(id, mode, null, null, ResumeStrategy.NONE, DEFAULT_SAVE_TOKEN_INTERVAL, null,
                null,
                pipeline,
                Document.class);
    }

    public ChangeStream<T> batchSize(Integer batchSize) {
        return new ChangeStream<T>(this.id, this.mode, batchSize, this.maxAwaitTime,
                this.resumeStrategy, this.saveTokenInterval, this.fullDocumentBeforeChange, this.fullDocument,
                this.pipeline, this.documentClass);
    }

    public ChangeStream<T> maxAwaitTime(Long maxAwaitTime) {
        return new ChangeStream<T>(this.id, this.mode, this.batchSize, maxAwaitTime,
                this.resumeStrategy, this.saveTokenInterval, this.fullDocumentBeforeChange, this.fullDocument,
                this.pipeline, this.documentClass);
    }

    public ChangeStream<T> resumeStrategy(ResumeStrategy resumeStrategy) {
        return new ChangeStream<T>(this.id, this.mode, this.batchSize, maxAwaitTime,
                resumeStrategy, this.saveTokenInterval, this.fullDocumentBeforeChange, this.fullDocument, this.pipeline,
                this.documentClass);
    }

    public ChangeStream<T> resumeStrategy(ResumeStrategy resumeStrategy, long saveTokenInterval) {
        return new ChangeStream<T>(this.id, this.mode, this.batchSize, maxAwaitTime,
                resumeStrategy, saveTokenInterval, this.fullDocumentBeforeChange, this.fullDocument, this.pipeline,
                this.documentClass);
    }

    public ChangeStream<T> resumeAfter(String resumeToken) {
        this.resumeToken = resumeToken;
        return this;
    }

    public ChangeStream<T> fullDocumentBeforeChange(FullDocumentBeforeChange fullDocumentBeforeChange) {
        return new ChangeStream<T>(this.id, this.mode, this.batchSize, maxAwaitTime,
                this.resumeStrategy, this.saveTokenInterval, fullDocumentBeforeChange, this.fullDocument, this.pipeline,
                this.documentClass);
    }

    public ChangeStream<T> fullDocument(FullDocument fullDocument) {
        return new ChangeStream<T>(this.id, this.mode, this.batchSize, maxAwaitTime,
                this.resumeStrategy, this.saveTokenInterval, this.fullDocumentBeforeChange, fullDocument, this.pipeline,
                this.documentClass);
    }

    public <NewT> ChangeStream<NewT> withClass(Class<NewT> clazz) {
        return new ChangeStream<NewT>(this.id, this.mode, this.batchSize, this.maxAwaitTime,
                this.resumeStrategy, this.saveTokenInterval, this.fullDocumentBeforeChange, this.fullDocument,
                this.pipeline, clazz);
    }

}
