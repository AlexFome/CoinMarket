package com.alexfome.coinmarket.model;

import com.alexfome.coinmarket.IDataRefreshCallback;
import com.alexfome.coinmarket.api.CoinmarketcapApi;
import com.alexfome.coinmarket.comparator.ComparatorByPercentage;
import com.alexfome.coinmarket.comparator.ComparatorByUSD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by grege on 05.08.2017.
 */

public class TickerLists {

    private final int TOP_LIST_SIZE = 10;

    private List<Ticker> mTickers = new ArrayList<>();
    private ComparatorByPercentage mComparatorByPercentage;
    private ComparatorByUSD mComparatorByUSD;
    private CoinmarketcapApi mCoinmarketcapApi;

    public TickerLists() {
        initComparators();
        initCoinMarketAPIclient();
    }

    private void initComparators () {
        mComparatorByPercentage = new ComparatorByPercentage();
        mComparatorByUSD = new ComparatorByUSD();
    }

    public void setList (List<Ticker> tickers, IDataRefreshCallback callback) {
        this.mTickers = tickers;
        for (int i = 0; i < tickers.size(); i++) {
            this.mTickers.get(i).calculateDeltaUSD();
        }
        callback.onSuccess();
    }

    public List<Ticker> getTopByPercentage() {
        Collections.sort(mTickers, mComparatorByPercentage);
        List<Ticker> list = new ArrayList<>();
        list.addAll(mTickers.subList(0, TOP_LIST_SIZE));
        return list;
    }

    public List<Ticker> getTopByUSDdeltaValue() {
        Collections.sort(mTickers, mComparatorByUSD);
        List<Ticker> list = new ArrayList<>();
        list.addAll(mTickers.subList(0, TOP_LIST_SIZE));
        return list;
    }

    private void initCoinMarketAPIclient () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.coinmarketcap.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mCoinmarketcapApi = retrofit.create(CoinmarketcapApi.class);
    }

    public void refreshData (final IDataRefreshCallback callback) {
        mCoinmarketcapApi.getCurrencies().enqueue(new Callback<List<Ticker>>() {
            @Override
            public void onResponse(Call<List<Ticker>> call, Response<List<Ticker>> response) {
                setList(response.body(), callback);
            }

            @Override
            public void onFailure(Call<List<Ticker>> call, Throwable t) {
                callback.onFail();
            }
        });
    }

}
