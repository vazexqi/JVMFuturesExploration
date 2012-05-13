package edu.illinois.nchen.javaFutures;

import com.google.common.base.Stopwatch;
import edu.illinois.nchen.base.IAnalysisEngine;
import edu.illinois.nchen.base.SequentialAnalysisEngine;
import edu.illinois.nchen.base.businessModels.MarketModel;
import edu.illinois.nchen.base.businessModels.StockAnalysisCollection;
import edu.illinois.nchen.base.businessModels.StockDataCollection;

import java.util.Arrays;
import java.util.concurrent.*;

public class JavaFuturesAnalysisEngine extends SequentialAnalysisEngine {

    private ExecutorService executor;

    public JavaFuturesAnalysisEngine() {
        this.executor = new ForkJoinPool();
    }

    // NOTES - Current problems/limitations/inconveniences with the current way:
    // 1) Need to remember to pass the Callable to the executor and assign the future to a final local variable
    // 2) Seems to miss some potential parallelism between loading NASDAQ, NYSE and Fed just because Fed loading was called later.
    // 3) The dependencies between each task is not as clean since it seems that everything is submitted to the executor in statement execution order.
    @Override
    public void doAnalysisParallel() throws ExecutionException, InterruptedException {

        final Future<StockDataCollection> nyseData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNyseData();
            }
        });

        final Future<StockDataCollection> nasdaqData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNasdaqData();
            }
        });

        final Future<StockDataCollection> mergedMarketData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return mergeMarketData(Arrays.asList(nyseData.get(), nasdaqData.get()));
            }
        });

        final Future<StockDataCollection> normalizedMarketData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return normalizeData(mergedMarketData.get());
            }
        });

        final Future<StockDataCollection> fedHistoricalData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadFedHistoricalData();
            }
        });

        final Future<StockDataCollection> normalizedHistoricalData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return normalizeData(fedHistoricalData.get());
            }
        });

        final Future<StockAnalysisCollection> analyzedStockData = executor.submit(new Callable<StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection call() throws Exception {
                return analyzeData(normalizedMarketData.get());
            }
        });

        Future<MarketModel> modeledMarketData = executor.submit(new Callable<MarketModel>() {
            @Override
            public MarketModel call() throws Exception {
                return runModel(analyzedStockData.get());
            }
        });

        final Future<StockAnalysisCollection> analyzedHistoricalData = executor.submit(new Callable<StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection call() throws Exception {
                return analyzeData(normalizedHistoricalData.get());
            }
        });

        Future<MarketModel> modeledHistoricalData = executor.submit(new Callable<MarketModel>() {
            @Override
            public MarketModel call() throws Exception {
                return runModel(analyzedHistoricalData.get());
            }
        });

        compareModels(Arrays.asList(modeledMarketData.get(), modeledHistoricalData.get()));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        IAnalysisEngine engine = new JavaFuturesAnalysisEngine();

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
