package com.mongodb.javabasic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class ChangeStream<T> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<ChangeStreamProcess<T>> changeStreams = new ArrayList<>();

    private ChangeStreamProcess<T> earilest = null;
    private ChangeStreamProcess<T> latest = null;

    public void run(Function<ChangeStreamProcessConfig<T>, ChangeStreamProcess<T>> initProcess, boolean resume)
            throws Exception {
        this.run(1, initProcess, resume);
    }
    @SuppressWarnings("unchecked")
    public void run(int noOfChangeStream,
            Function<ChangeStreamProcessConfig<T>, ChangeStreamProcess<T>> initProcess, boolean resume)
            throws Exception {
        if (changeStreams.size() > 0) {
            throw new Exception("Change stream is already running");
        }
        for (int i = 0; i < noOfChangeStream; i++) {
            run((ChangeStreamProcessConfig<T>) ChangeStreamProcessConfig.builder()
                    .startAt(resume && earilest != null ? earilest.getClusterTime() : null)
                    .endAt(resume && latest != null ? latest.getClusterTime() : null)
                    .noOfChangeStream(noOfChangeStream).changeStreamIndex(i).build(), initProcess);
        }
    }

    public void run(
            Function<ChangeStreamProcessConfig<T>, ChangeStreamProcess<T>> initProcess) throws Exception {
        // TODO: get instance list
        var noOfInstances = 2;
        for (int i = 0; i < noOfInstances; i++) {
            // TODO: Remote call to instance
        }
    }

    public void run(ChangeStreamProcessConfig<T> config,
            Function<ChangeStreamProcessConfig<T>, ChangeStreamProcess<T>> initProcess) {
        changeStreams.add(initProcess.apply(config));
        logger.info((config.getChangeStreamIndex() + 1) + "/" + config.getNoOfChangeStream() + ": Run Change stream");
        new Thread(changeStreams.get(config.getChangeStreamIndex())).start();
    }

    public void stopAll() throws Exception {
        for (ChangeStreamProcess<T> process : changeStreams) {
            process.stop();
        }
        // Wait until all threads stopped completed
        while (!changeStreams.stream().allMatch(cs -> cs.isDone())) {
            Thread.sleep(100);
        }
        earilest = null;
        latest = null;
        for (ChangeStreamProcess<T> process : changeStreams) {
            if (earilest == null || earilest.getClusterTime().compareTo(process.getClusterTime()) > 0)
                earilest = process;
            if (latest == null || latest.getClusterTime().compareTo(process.getClusterTime()) < 0)
                latest = process;
        }
        logger.info("earilest:" + earilest.getClusterTime() + " latest:" + latest.getClusterTime());
        changeStreams.clear();
    }
}
