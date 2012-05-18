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

    IAnalysisEngine javaEngine;
    IAnalysisEngine guavaEngine;
    IAnalysisEngine gparsEngine;
    IAnalysisEngine akkaEngine;
    IAnalysisEngine twitterEngine;

    @Override
    protected void setUp() throws Exception {
        javaEngine = new JavaFuturesAnalysisEngine();
        guavaEngine = new GuavaListenableFuturesAnalysisEngine();
        gparsEngine = new GParsDataflowAnalysisEngine();
        akkaEngine = new AkkaFuturesAnalysisEngine();
        twitterEngine = new TwitterFuturesAnalysisEngine();
    }

    public void timeJavaFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            javaEngine.doAnalysisParallel();
        }
    }

    public void timeGuavaListenableFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            guavaEngine.doAnalysisParallel();
        }
    }

    public void timeGParsDataflow(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            gparsEngine.doAnalysisParallel();
        }
    }

    public void timeAkkaFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            akkaEngine.doAnalysisParallel();
        }
    }

    public void timeTwitterFutures(int reps) throws ExecutionException, InterruptedException {
        for (int i = 0; i < reps; i++) {
            twitterEngine.doAnalysisParallel();
        }
    }

    public static void main(String[] args) throws Exception {
        Runner.main(FuturesBenchmark.class, args);
    }

}
