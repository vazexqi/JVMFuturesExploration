package edu.illinois.nchen.guavaListenableFutures;

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
import java.util.concurrent.ForkJoinPool;

public class GuavaListenableFuturesAnalysisEngine extends SequentialAnalysisEngine {

    private ListeningExecutorService executor;

    public GuavaListenableFuturesAnalysisEngine() {
        this.executor = MoreExecutors.listeningDecorator(new ForkJoinPool());
    }

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

        ListenableFuture<List<StockDataCollection>> marketData = Futures.successfulAsList(nyseData, nasdaqData);

        final ListenableFuture<StockDataCollection> mergedMarketData = Futures.transform(marketData, new AsyncFunction<List<StockDataCollection>, StockDataCollection>() {
            @Override
            public ListenableFuture<StockDataCollection> apply(final List<StockDataCollection> input) throws Exception {
                return executor.submit(new Callable<StockDataCollection>() {
                    @Override
                    public StockDataCollection call() throws Exception {
                        return mergeMarketData(input);
                    }
                });
            }
        });

        ListenableFuture<StockDataCollection> normalizedMarketData = Futures.transform(mergedMarketData, new AsyncFunction<StockDataCollection, StockDataCollection>() {
            @Override
            public ListenableFuture<StockDataCollection> apply(final StockDataCollection input) throws Exception {
                return executor.submit(new Callable<StockDataCollection>() {
                    @Override
                    public StockDataCollection call() throws Exception {
                        return normalizeData(input);
                    }
                });
            }
        });

        final ListenableFuture<StockDataCollection> fedHistoricalData = executor.submit(new Callable<StockDataCollection>() {
            @Override
            public StockDataCollection call() throws Exception {
                return loadFedHistoricalData();
            }
        });

        final ListenableFuture<StockDataCollection> normalizedHistoricalData = Futures.transform(fedHistoricalData, new AsyncFunction<StockDataCollection, StockDataCollection>() {
            @Override
            public ListenableFuture<StockDataCollection> apply(final StockDataCollection input) throws Exception {
                return executor.submit(new Callable<StockDataCollection>() {
                    @Override
                    public StockDataCollection call() throws Exception {
                        return normalizeData(input);
                    }
                });
            }
        });

        final ListenableFuture<StockAnalysisCollection> analyzedStockData = Futures.transform(normalizedMarketData, new AsyncFunction<StockDataCollection, StockAnalysisCollection>() {
            @Override
            public ListenableFuture<StockAnalysisCollection> apply(final StockDataCollection input) throws Exception {
                return executor.submit(new Callable<StockAnalysisCollection>() {
                    @Override
                    public StockAnalysisCollection call() throws Exception {
                        return analyzeData(input);
                    }
                });
            }
        });

        ListenableFuture<MarketModel> modeledMarketData = Futures.transform(analyzedStockData, new AsyncFunction<StockAnalysisCollection, MarketModel>() {
            @Override
            public ListenableFuture<MarketModel> apply(final StockAnalysisCollection input) throws Exception {
                return executor.submit(new Callable<MarketModel>() {
                    @Override
                    public MarketModel call() throws Exception {
                        return runModel(input);
                    }
                });
            }
        });

        final ListenableFuture<StockAnalysisCollection> analyzedHistoricalData = Futures.transform(normalizedHistoricalData, new AsyncFunction<StockDataCollection, StockAnalysisCollection>() {
            @Override
            public ListenableFuture<StockAnalysisCollection> apply(final StockDataCollection input) throws Exception {
                return executor.submit(new Callable<StockAnalysisCollection>() {
                    @Override
                    public StockAnalysisCollection call() throws Exception {
                        return analyzeData(input);
                    }
                });
            }
        });

        ListenableFuture<MarketModel> modeledHistoricalData = Futures.transform(analyzedHistoricalData, new AsyncFunction<StockAnalysisCollection, MarketModel>() {
            @Override
            public ListenableFuture<MarketModel> apply(final StockAnalysisCollection input) throws Exception {
                return executor.submit(new Callable<MarketModel>() {
                    @Override
                    public MarketModel call() throws Exception {
                        return runModel(input);
                    }
                });
            }
        });

        ListenableFuture<List<MarketModel>> modeledData = Futures.successfulAsList(modeledMarketData, modeledHistoricalData);

        ListenableFuture<MarketRecommendation> results = Futures.transform(modeledData, new AsyncFunction<List<MarketModel>, MarketRecommendation>() {
            @Override
            public ListenableFuture<MarketRecommendation> apply(final List<MarketModel> input) throws Exception {
                return executor.submit(new Callable<MarketRecommendation>() {
                    @Override
                    public MarketRecommendation call() throws Exception {
                        return compareModels(input);
                    }
                });
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
