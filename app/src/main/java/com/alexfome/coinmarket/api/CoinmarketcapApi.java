package com.alexfome.coinmarket.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by grege on 01.08.2017.
 */

public interface CoinmarketcapApi {
    @GET("ticker/")
    Call<List<com.alexfome.coinmarket.model.Currency>> getCurrencies();
}
