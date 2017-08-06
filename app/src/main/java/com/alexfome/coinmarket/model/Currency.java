package com.alexfome.coinmarket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by grege on 01.08.2017.
 */

public class Currency {

    private String id;
    private String name;
    private String symbol;
    private double rank;
    @SerializedName("price_usd")
    private double priceUSD;
    @SerializedName("price_btc")
    private double priceBTC;
    @SerializedName("market_cap_usd")
    private double marketCapUSD;
    @SerializedName("available_supply")
    private String availableSupply;
    @SerializedName("total_supply")
    private double totalSupply;
    @SerializedName("percent_change_1h")
    private double percentChange1h;
    @SerializedName("percent_change_24h")
    private double percentChange24h;
    @SerializedName("percent_change_7d")
    private double percentChange7d;
    @SerializedName("last_updated")
    private String lastUpdated;
    @SerializedName("volume_24h_usd")
    private double volume24hUSD;
    @Expose (serialize = false, deserialize = false)
    private double deltaUSD;
    @Expose (serialize = false, deserialize = false)
    private ArrayList<Double> mockData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public double getPriceUSD() {
        return priceUSD;
    }

    public void setPriceUSD(double priceUSD) {
        this.priceUSD = priceUSD;
    }

    public double getPriceBTC() {
        return priceBTC;
    }

    public void setPriceBTC(double priceBTC) {
        this.priceBTC = priceBTC;
    }

    public double getMarketCapUSD() {
        return marketCapUSD;
    }

    public void setMarketCapUSD(double marketCapUSD) {
        this.marketCapUSD = marketCapUSD;
    }

    public String getAvailableSupply() {
        return availableSupply;
    }

    public void setAvailableSupply(String availableSupply) {
        this.availableSupply = availableSupply;
    }

    public double getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(double totalSupply) {
        this.totalSupply = totalSupply;
    }

    public double getPercentChange1h() {
        return percentChange1h;
    }

    public void setPercentChange1h(double percentChange1h) {
        this.percentChange1h = percentChange1h;
    }

    public double getPercentChange24h() {
        return percentChange24h;
    }

    public void setPercentChange24h(double percentChange24h) {
        this.percentChange24h = percentChange24h;
    }

    public double getPercentChange7d() {
        return percentChange7d;
    }

    public void setPercentChange7d(double percentChange7d) {
        this.percentChange7d = percentChange7d;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getVolume24hUSD() {
        return volume24hUSD;
    }

    public void setVolume24hUSD(double volume24hUSD) {
        this.volume24hUSD = volume24hUSD;
    }

    public double getDeltaUSD() {
        return deltaUSD;
    }

    public void setDeltaUSD(double deltaUSD) {
        this.deltaUSD = deltaUSD;
    }

    public void calculateDeltaUSD () {
        double factor = 1 + (getPercentChange24h() / 100);
        double prevValue = getPriceUSD() / factor;
        deltaUSD = (getPriceUSD() - prevValue);
    }

    public void generateMockData () {
        // API does not provide price statistics, so we generate data randomly to draw a graph

        Random random = new Random();
        mockData = new ArrayList<>();
        int steps = 24 * 4;
        for (int i = 0; i < steps - 1; i++) {
            double value = random.nextFloat() * priceUSD;
            mockData.add(value);
        }
        mockData.add(priceUSD);

    }

    public ArrayList<Double> getMockData() {
        return mockData;
    }
}
