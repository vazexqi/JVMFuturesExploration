package edu.illinois.nchen.guavaListenableFutures;

import akka.jsr166y.ForkJoinPool;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.*;
import edu.illinois.nchen.base.IAnalysisEngine;
import edu.illinois.nchen.base.SequentialAnalysisEngine;
import edu.illinois.nchen.base.businessModels.MarketModel;
import edu.illinois.nchen.base.businessModels.MarketRecommendation;
import edu.illinois.nchen.base.businessModels.StockAnalysisCollection;
import edu.illinois.nchen.base.businessModels.StockDataCollection;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class GuavaListenableFuturesAnalysisEngine extends SequentialAnalysisEngine {

    private ListeningExecutorService executor;

    public GuavaListenableFuturesAnalysisEngine() {
        this.executor = MoreExecutors.listeningDecorator(new ForkJoinPool());
    }

    // NOTES - Current problems/limitations/inconveniences with the current way:
    // 1) Need to remember to create a ListeningExecutorService
    // 2) Slightly longer method signatures for Function but the input/output types are much clearer
    // 3) Seems to better mimic the original C# example (although lacking in syntax)
    @Override
    public void doAnalysisParallel() throws ExecutionException, InterruptedException {

        final ListenableFuture<StockDataCollection> nyseData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNyseData();
            }
        });

        final ListenableFuture<StockDataCollection> nasdaqData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNasdaqData();
            }
        });

        final ListenableFuture<StockDataCollection> mergedMarketData = Futures.transform(Futures.successfulAsList(nyseData, nasdaqData), new Function<List<StockDataCollection>, StockDataCollection>() {
            @Override
            public StockDataCollection apply(final List<StockDataCollection> input) {
                return mergeMarketData(input);
            }
        });

        ListenableFuture<StockDataCollection> normalizedMarketData = Futures.transform(mergedMarketData, new Function<StockDataCollection, StockDataCollection>() {
            @Override
            public StockDataCollection apply(final StockDataCollection input) {
                return normalizeData(input);
            }
        });

        final ListenableFuture<StockDataCollection> fedHistoricalData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadFedHistoricalData();
            }
        });

        final ListenableFuture<StockDataCollection> normalizedHistoricalData = Futures.transform(fedHistoricalData, new Function<StockDataCollection, StockDataCollection>() {
            @Override
            public StockDataCollection apply(final StockDataCollection input) {
                return normalizeData(input);
            }
        });

        final ListenableFuture<StockAnalysisCollection> analyzedStockData = Futures.transform(normalizedMarketData, new Function<StockDataCollection, StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection apply(final StockDataCollection input) {
                return analyzeData(input);
            }
        });

        ListenableFuture<MarketModel> modeledMarketData = Futures.transform(analyzedStockData, new Function<StockAnalysisCollection, MarketModel>() {
            @Override
            public MarketModel apply(final StockAnalysisCollection input) {
                return runModel(input);
            }
        });

        final ListenableFuture<StockAnalysisCollection> analyzedHistoricalData = Futures.transform(normalizedHistoricalData, new Function<StockDataCollection, StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection apply(final StockDataCollection input) {
                return analyzeData(input);
            }
        });

        ListenableFuture<MarketModel> modeledHistoricalData = Futures.transform(analyzedHistoricalData, new Function<StockAnalysisCollection, MarketModel>() {
            @Override
            public MarketModel apply(final StockAnalysisCollection input) {
                return runModel(input);
            }
        });

        ListenableFuture<MarketRecommendation> results = Futures.transform(Futures.successfulAsList(modeledMarketData, modeledHistoricalData), new Function<List<MarketModel>, MarketRecommendation>() {
            @Override
            public MarketRecommendation apply(final List<MarketModel> input) {
                return compareModels(input);
            }
        });

        results.get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        IAnalysisEngine engine = new GuavaListenableFuturesAnalysisEngine();

        Stopwatch watch;

        System.out.println("==SEQUENTIAL==");
        watch = new Stopwatch().start();
        engine.doAnalysisSequential();
        watch.stop();
        System.out.println(watch.elapsedMillis() + "ms taken.");

        System.out.println("==PARALLEL==");
        watch = new Stopwatch().start();
        engine.doAnalysisParallel();
        watch.stop();
        System.out.println(watch.elapsedMillis() + "ms taken.");
    }
}
