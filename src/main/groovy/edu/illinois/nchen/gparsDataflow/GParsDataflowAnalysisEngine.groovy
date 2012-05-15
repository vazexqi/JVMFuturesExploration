package edu.illinois.nchen.gparsDataflow

import com.google.common.base.Stopwatch
import edu.illinois.nchen.base.IAnalysisEngine
import edu.illinois.nchen.base.SequentialAnalysisEngine
import groovyx.gpars.dataflow.Promise
import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.group.PGroup
import groovyx.gpars.scheduler.FJPool

import java.util.concurrent.ExecutionException

public class GParsDataflowAnalysisEngine extends SequentialAnalysisEngine {

    private PGroup pGroup

    public GParsDataflowAnalysisEngine() {
        this.pGroup = new DefaultPGroup(new FJPool())
    }

    // NOTES - Current problems/limitations/inconveniences with the current way:
    // 1) Must remember to invoke task on a pGroup
    // 2) Hard to check for type safety from just reading the lines
    @Override
    void doAnalysisParallel() {
        Promise nyseData = pGroup.task {loadNyseData()}
        Promise nasdaqData = pGroup.task {loadNasdaqData()}

        Promise mergedMarketData = pGroup.whenAllBound([nyseData, nasdaqData]) { nyse, nasdaq ->
            mergeMarketData([nyse, nasdaq])
        }

        Promise modeledMarketData = mergedMarketData.then {it -> normalizeData(it)} then {it -> analyzeData(it)} then {it -> runModel(it)}

        Promise modeledHistoricalData = pGroup.task {return loadFedHistoricalData()} then {it -> normalizeData(it)} then {it -> analyzeData(it)} then {it -> runModel(it)}

        Promise results = pGroup.whenAllBound([modeledMarketData, modeledHistoricalData]) {marketData, historicalData ->
            compareModels([marketData, historicalData])
        }
        results.get()
    }

    // This is the more verbose way to express the dependencies using Groovy/GPars but it might be more understandable.
    // The code above in doAnalysisParallel() does the same thing as the code below, but more succinctly.
    void doAnalysisParallel_Verbose() {
        Promise nyseData = pGroup.task {loadNyseData()}
        Promise nasdaqData = pGroup.task {loadNasdaqData()}

        Promise mergedMarketData = pGroup.whenAllBound([nyseData, nasdaqData]) { nyse, nasdaq ->
            mergeMarketData([nyse, nasdaq])
        }

        Promise normalizedMarketData = mergedMarketData.then {it -> normalizeData(it)}

        Promise fedHistoricalData = pGroup.task {return loadFedHistoricalData()}

        Promise normalizedHistoricalData = fedHistoricalData.then {it -> normalizeData(it)}

        Promise analyzedStockData = normalizedMarketData.then {it -> analyzeData(it)}

        Promise modeledMarketData = analyzedStockData.then {it -> runModel(it)}

        Promise analyzedHistoricalData = normalizedHistoricalData.then {it -> analyzeData(it)}

        Promise modeledHistoricalData = analyzedHistoricalData.then {it -> runModel(it)}

        Promise results = pGroup.whenAllBound([modeledMarketData, modeledHistoricalData]) {marketData, historicalData ->
            compareModels([marketData, historicalData])
        }
        results.get()
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        IAnalysisEngine engine = new GParsDataflowAnalysisEngine()

        Stopwatch watch

        System.out.println("==SEQUENTIAL==")
        watch = new Stopwatch().start()
        engine.doAnalysisSequential()
        watch.stop()
        System.out.println(watch.elapsedMillis() + "ms taken.")

        System.out.println("==PARALLEL==")
        watch = new Stopwatch().start()
        engine.doAnalysisParallel()
        watch.stop()
        System.out.println(watch.elapsedMillis() + "ms taken.")
    }

}