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
    // 1) Need to remember to pass the FutureTask to the executor
    // 2) Seems to miss some potential parallelism between loading NASDAQ, NYSE and Fed just because Fed loading was called later.
    // 3) The dependencies between each task is not as clean since it seems that everything is submitted to the executor in statement execution order.
    @Override
    public void doAnalysisParallel() throws ExecutionException, InterruptedException {

        final FutureTask<StockDataCollection> nyseData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNyseData();
            }
        });
        executor.submit(nyseData);

        final FutureTask<StockDataCollection> nasdaqData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadNasdaqData();
            }
        });
        executor.submit(nasdaqData);

        final FutureTask<StockDataCollection> mergedMarketData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return mergeMarketData(Arrays.asList(nyseData.get(), nasdaqData.get()));
            }
        });
        executor.submit(mergedMarketData);

        final FutureTask<StockDataCollection> normalizedMarketData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return normalizeData(mergedMarketData.get());
            }
        });
        executor.submit(normalizedMarketData);

        final FutureTask<StockDataCollection> fedHistoricalData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadFedHistoricalData();
            }
        });
        executor.submit(fedHistoricalData);

        final FutureTask<StockDataCollection> normalizedHistoricalData = new FutureTask<StockDataCollection>(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return normalizeData(fedHistoricalData.get());
            }
        });
        executor.submit(normalizedHistoricalData);

        final FutureTask<StockAnalysisCollection> analyzedStockData = new FutureTask<StockAnalysisCollection>(new Callable<StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection call() throws Exception {
                return analyzeData(normalizedMarketData.get());
            }
        });
        executor.submit(analyzedStockData);

        FutureTask<MarketModel> modeledMarketData = new FutureTask<MarketModel>(new Callable<MarketModel>() {
            @Override
            public MarketModel call() throws Exception {
                return runModel(analyzedStockData.get());
            }
        });
        executor.submit(modeledMarketData);

        final FutureTask<StockAnalysisCollection> analyzedHistoricalData = new FutureTask<StockAnalysisCollection>(new Callable<StockAnalysisCollection>() {
            @Override
            public StockAnalysisCollection call() throws Exception {
                return analyzeData(normalizedHistoricalData.get());
            }
        });
        executor.submit(analyzedHistoricalData);

        FutureTask<MarketModel> modeledHistoricalData = new FutureTask<MarketModel>(new Callable<MarketModel>() {
            @Override
            public MarketModel call() throws Exception {
                return runModel(analyzedHistoricalData.get());
            }
        });
        executor.submit(modeledHistoricalData);

        compareModels(Arrays.asList(modeledMarketData.get(), modeledHistoricalData.get()));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        IAnalysisEngine engine = new JavaFuturesAnalysisEngine();

        System.out.println("==SEQUENTIAL==");
        Stopwatch watch = new Stopwatch().start();
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
