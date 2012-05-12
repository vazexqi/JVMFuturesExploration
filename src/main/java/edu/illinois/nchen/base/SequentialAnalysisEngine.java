package edu.illinois.nchen.base;

import edu.illinois.nchen.base.businessModels.MarketModel;
import edu.illinois.nchen.base.businessModels.StockAnalysisCollection;
import edu.illinois.nchen.base.businessModels.StockDataCollection;

import java.util.Arrays;

public class SequentialAnalysisEngine extends AnalysisEngine {
    @Override
    public void doAnalysisSequential() {
        StockDataCollection nyseData = loadNyseData();
        StockDataCollection nasdaqData = loadNasdaqData();
        StockDataCollection mergedMarketData = mergeMarketData(Arrays.asList(nyseData, nasdaqData));
        StockDataCollection normalizedMarketData = normalizeData(mergedMarketData);
        StockDataCollection fedHistoricalData = loadFedHistoricalData();
        StockDataCollection normalizedHistoricalData = normalizeData(fedHistoricalData);
        StockAnalysisCollection analyzedStockData = analyzeData(normalizedMarketData);
        MarketModel modeledMarketData = runModel(analyzedStockData);
        StockAnalysisCollection analyzedHistoricalData = analyzeData(normalizedHistoricalData);
        MarketModel modeledHistoricalData = runModel(analyzedHistoricalData);
        compareModels(Arrays.asList(modeledMarketData, modeledHistoricalData));
    }

    @Override
    public void doAnalysisParallel() {
        System.out.println("This version cannot handle parallel analysis");
    }
}
