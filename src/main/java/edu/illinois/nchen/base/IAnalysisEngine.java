package edu.illinois.nchen.base;

import java.util.concurrent.ExecutionException;

public interface IAnalysisEngine {
    void doAnalysisSequential();

    void doAnalysisParallel() throws ExecutionException, InterruptedException;
}
