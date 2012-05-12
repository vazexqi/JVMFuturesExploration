package edu.illinois.nchen.javaFutures;

import edu.illinois.nchen.base.IAnalysisEngine;
import edu.illinois.nchen.base.SequentialAnalysisEngine;

public class JavaFuturesAnalysisEngine extends SequentialAnalysisEngine {
    public static void main(String[] args) {
        IAnalysisEngine engine = new JavaFuturesAnalysisEngine();
        engine.doAnalysisSequential();
    }
}
