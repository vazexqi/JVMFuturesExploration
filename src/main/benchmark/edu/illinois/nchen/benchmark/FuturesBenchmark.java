package edu.illinois.nchen.benchmark;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import edu.illinois.nchen.akkaFutures.AkkaFuturesAnalysisEngine;
import edu.illinois.nchen.base.IAnalysisEngine;
import edu.illinois.nchen.gparsDataflow.GParsDataflowAnalysisEngine;
import edu.illinois.nchen.guavaListenableFutures.GuavaListenableFuturesAnalysisEngine;
import edu.illinois.nchen.javaFutures.JavaFuturesAnalysisEngine;
import edu.illinois.nchen.twitterFutures.TwitterFuturesAnalysisEngine;

import java.util.concurrent.ExecutionException;

public class FuturesBenchmark extends SimpleBenchmark {
    public void timeJavaFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            IAnalysisEngine engine = new JavaFuturesAnalysisEngine();
            engine.doAnalysisParallel();
        }
    }

    public void timeGuavaListenableFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            IAnalysisEngine engine = new GuavaListenableFuturesAnalysisEngine();
            engine.doAnalysisParallel();
        }
    }

    public void timeGParsDataflow(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            IAnalysisEngine engine = new GParsDataflowAnalysisEngine();
            engine.doAnalysisParallel();
        }
    }

    public void timeAkkaFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            IAnalysisEngine engine = new AkkaFuturesAnalysisEngine();
            engine.doAnalysisParallel();
        }
    }

    public void timeTwitterFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            IAnalysisEngine engine = new TwitterFuturesAnalysisEngine();
            engine.doAnalysisParallel();
        }
    }

    public static void main(String[] args) throws Exception {
        Runner.main(FuturesBenchmark.class, args);
    }

}
