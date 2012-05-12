package edu.illinois.nchen.base.businessModels;

import java.util.ArrayList;

public class MarketAnalyer {

    public static StockAnalysisCollection run(StockDataCollection data) {
        return new StockAnalysisCollection(new ArrayList<StockAnalysis>());
    }
}
