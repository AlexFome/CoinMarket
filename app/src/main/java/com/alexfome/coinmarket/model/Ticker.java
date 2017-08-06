package com.alexfome.coinmarket.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by grege on 01.08.2017.
 */

public class Ticker {

    public String id;
    public String name;
    public String symbol;
    public double rank;
    @SerializedName("price_usd")
    public double priceUSD;
    @SerializedName("price_btc")
    public double priceBTC;
    @SerializedName("market_cap_usd")
    public double marketCapUSD;
    @SerializedName("available_supply")
    public double availableSupply;
    @SerializedName("total_supply")
    public double totalSupply;
    @SerializedName("percent_change_1h")
    public double percentChange1h;
    @SerializedName("percent_change_24h")
    public double percentChange24h;
    @SerializedName("percent_change_7d")
    public double percentChange7d;
    @SerializedName("last_updated")
    public String lastUpdated;
    @SerializedName("volume_24h_usd")
    public double volume24hUSD;
    @Expose (serialize = false, deserialize = false)
    public double deltaUSD;
    @Expose (serialize = false, deserialize = false)
    public ArrayList<Double> mockData;

    public void calculateDeltaUSD () {
        double factor = 1 + (percentChange24h / 100);
        double prevValue = priceUSD / factor;
        deltaUSD = (priceUSD - prevValue);
    }
}
