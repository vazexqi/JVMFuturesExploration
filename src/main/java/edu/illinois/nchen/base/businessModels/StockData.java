package edu.illinois.nchen.base.businessModels;

public class StockData {
    private String name;
    private double[] priceHistory;

    public StockData(String name, double[] priceHistory) {
        this.name = name;
        this.priceHistory = priceHistory;
    }
}
