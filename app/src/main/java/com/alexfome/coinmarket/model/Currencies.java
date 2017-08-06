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

public class Currencies {

    private final int TOP_LIST_SIZE = 10;

    private List<Currency> mCurrencies = new ArrayList<>();
    private ComparatorByPercentage mComparatorByPercentage;
    private ComparatorByUSD mComparatorByUSD;
    private CoinmarketcapApi mCoinmarketcapApi;

    public Currencies() {
        initComparators();
        initCoinMarketAPIclient();
    }

    private void initComparators () {
        mComparatorByPercentage = new ComparatorByPercentage();
        mComparatorByUSD = new ComparatorByUSD();
    }

    public void setList (List<Currency> currencies, IDataRefreshCallback callback) {
        mCurrencies = currencies;
        for (int i = 0; i < currencies.size(); i++) {
            mCurrencies.get(i).calculateDeltaUSD();
        }
        callback.onSuccess();
    }

    public List<Currency> getTopByPercentage() {
        Collections.sort(mCurrencies, mComparatorByPercentage);
        List<Currency> list = new ArrayList<>();
        list.addAll(mCurrencies.subList(0, TOP_LIST_SIZE));
        return list;
    }

    public List<Currency> getTopByUSDdeltaValue() {
        Collections.sort(mCurrencies, mComparatorByUSD);
        List<Currency> list = new ArrayList<>();
        list.addAll(mCurrencies.subList(0, TOP_LIST_SIZE));
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
        mCoinmarketcapApi.getCurrencies().enqueue(new Callback<List<Currency>>() {
            @Override
            public void onResponse(Call<List<Currency>> call, Response<List<Currency>> response) {
                setList(response.body(), callback);
            }

            @Override
            public void onFailure(Call<List<Currency>> call, Throwable t) {
                callback.onFail();
            }
        });
    }

}
