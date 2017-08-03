package com.alexfome.coinmarket.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by grege on 01.08.2017.
 */

public class Currency {

    public String id;
    public String name;
    public String symbol;
    public float rank;
    public float price_usd;
    public float price_btc;
    public float market_cap_usd;
    public String available_supply;
    public float total_supply;
    public float percent_change_1h;
    public float percent_change_24h;
    public float percent_change_7d;
    public String last_updated;
    @SerializedName("24h_volume_usd")
    public float volume_24h_usd;

    public boolean selected;

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

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }

    public float getPrice_usd() {
        return price_usd;
    }

    public void setPrice_usd(float price_usd) {
        this.price_usd = price_usd;
    }

    public float getPrice_btc() {
        return price_btc;
    }

    public void setPrice_btc(float price_btc) {
        this.price_btc = price_btc;
    }

    public float getMarket_cap_usd() {
        return market_cap_usd;
    }

    public void setMarket_cap_usd(float market_cap_usd) {
        this.market_cap_usd = market_cap_usd;
    }

    public String getAvailable_supply() {
        return available_supply;
    }

    public void setAvailable_supply(String available_supply) {
        this.available_supply = available_supply;
    }

    public float getTotal_supply() {
        return total_supply;
    }

    public void setTotal_supply(float total_supply) {
        this.total_supply = total_supply;
    }

    public float getPercent_change_1h() {
        return percent_change_1h;
    }

    public void setPercent_change_1h(float percent_change_1h) {
        this.percent_change_1h = percent_change_1h;
    }

    public float getPercent_change_24h() {
        return percent_change_24h;
    }

    public void setPercent_change_24h(float percent_change_24h) {
        this.percent_change_24h = percent_change_24h;
    }

    public float getPercent_change_7d() {
        return percent_change_7d;
    }

    public void setPercent_change_7d(float percent_change_7d) {
        this.percent_change_7d = percent_change_7d;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }

    public float calculateDeltaUSD () {
        float factor = 1 + (getPercent_change_24h() / 100);
        float prevValue = getPrice_usd() / factor;
        float deltaUSD = (getPrice_usd() - prevValue);

        return deltaUSD;
    }

    public float getVolume_24h_usd() {
        return volume_24h_usd;
    }

    public void setVolume_24h_usd(float volume_24h_usd) {
        this.volume_24h_usd = volume_24h_usd;
    }
}
