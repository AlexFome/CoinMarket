package com.alexfome.coinmarket.model;

import com.alexfome.coinmarket.api.CoinmarketcapApi;
import com.alexfome.coinmarket.comparator.ComparatorByPercentage;
import com.alexfome.coinmarket.comparator.ComparatorByUSD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by grege on 05.08.2017.
 */

public class Currencies {

    private List<Currency> currencies = new ArrayList<>();
    private ComparatorByPercentage comparatorByPercentage;
    private ComparatorByUSD comparatorByUSD;
    private List<Currency> topByPercentage = new ArrayList<>();
    private List<Currency> topByUSDdeltaValue = new ArrayList<>();
    private int TOP_LIST_SIZE = 10;
    CoinmarketcapApi coinmarketcapApi;

    public Currencies () {
        initComparators();
        initCoinMarketAPIclient();
    }

    private void initComparators () {
        comparatorByPercentage = new ComparatorByPercentage();
        comparatorByUSD = new ComparatorByUSD();
    }

    public void setList (List<Currency> currencies) {
        this.currencies = currencies;

        for (int i = 0; i < currencies.size(); i++) {
            Currency currency = currencies.get(i);
            currency.calculateDeltaUSD();
            currency.generateMockData(); // API does not provide price statistics, so we generate data randomly to draw a graph
        }

        topByPercentage = initTopList(TOP_LIST_SIZE, comparatorByPercentage);
        topByUSDdeltaValue = initTopList(TOP_LIST_SIZE, comparatorByUSD);

    }

    private List<Currency> initTopList (int size, Comparator<Currency> comparator) {
        Collections.sort(currencies, comparator);
        List<Currency> list = new ArrayList<>();
        list.addAll(currencies.subList(0, size));
        return list;
    }

    public List<Currency> getTopByPercentage() {
        return topByPercentage;
    }

    public List<Currency> getTopByUSDdeltaValue() {
        return topByUSDdeltaValue;
    }

    private void initCoinMarketAPIclient () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.coinmarketcap.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        coinmarketcapApi = retrofit.create(CoinmarketcapApi.class);
    }

    public void refreshData (Callback<List<Currency>> callback) {
        coinmarketcapApi.getCurrencies().enqueue(callback);
    }

}
