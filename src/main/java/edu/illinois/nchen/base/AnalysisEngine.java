package edu.illinois.nchen.base;

import edu.illinois.nchen.base.businessModels.*;
import edu.illinois.nchen.utilities.Work;

import java.util.ArrayList;
import java.util.List;

public abstract class AnalysisEngine implements IAnalysisEngine {

    private float speed;

    public AnalysisEngine() {
        this(0.25f);
    }

    public AnalysisEngine(float speed) {
        this.speed = speed;
    }

    static List<StockData> makeNyseSecurityInfo() {
        return generateSecurities("NYSE", 100);

    }

    static List<StockData> makeNasdaqSecurityInfo() {
        return generateSecurities("NASDAQ", 100);
    }

    static List<StockData> makeFedSecurityInfo() {
        return generateSecurities("", 100);
    }

    private static List<StockData> generateSecurities(String stock, int quantity) {
        List<StockData> stocks = new ArrayList<StockData>();
        for (int i = 0; i < quantity; i++) {
            stocks.add(new StockData(stock + " Stock " + i, new double[]{0.0, 1.0, 2.0}));
        }
        return stocks;
    }

    protected StockDataCollection loadNyseData() {
        try {
            Work.performIOOperation(3);
            System.out.println("Done loading NYSE...");
            return new StockDataCollection(makeNyseSecurityInfo());
        } catch (InterruptedException e) {
            return null;
        }
    }


    protected StockDataCollection loadNasdaqData() {
        try {
            Work.performIOOperation(2 * speed);
            System.out.println("Done loading NASDAQ...");
            return new StockDataCollection(makeNyseSecurityInfo());
        } catch (InterruptedException e) {
            return null;
        }
    }

    protected StockDataCollection loadFedHistoricalData() {
        try {
            Work.performIOOperation(3 * speed);
            System.out.println("Done loading Fed...");
            return new StockDataCollection(makeNyseSecurityInfo());
        } catch (InterruptedException e) {
            return null;
        }
    }

    protected StockDataCollection mergeMarketData(List<StockDataCollection> allMarketData) {
        Work.performCPUOperation(2 * speed);
        List<StockData> accumulator = new ArrayList<StockData>();
        for (StockDataCollection collection : allMarketData) {
            accumulator.addAll(collection);
        }
        System.out.println("Done merging market data...");
        return new StockDataCollection(accumulator);
    }

    protected StockDataCollection normalizeData(StockDataCollection marketData) {
        Work.performCPUOperation(2 * speed);
        System.out.println("Done normalizing data...");
        return new StockDataCollection(marketData);
    }

    protected StockAnalysisCollection analyzeData(StockDataCollection data) {
        Work.performCPUOperation(2 * speed);
        System.out.println("Done analyzing data...");
        return MarketAnalyer.run(data);
    }

    protected MarketModel runModel(StockAnalysisCollection data) {
        Work.performCPUOperation(2 * speed);
        System.out.println("Done running model...");
        return MarketModeler.run(data);
    }

    protected MarketRecommendation compareModels(List<MarketModel> models) {
        Work.performCPUOperation(2 * speed );
        System.out.println("Done comparing models...");
        return ModelComparer.run(models.toArray(new MarketModel[0]));
    }
}
