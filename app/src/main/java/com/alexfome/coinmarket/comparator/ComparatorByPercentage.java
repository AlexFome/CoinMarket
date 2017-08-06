package com.alexfome.coinmarket.comparator;

import com.alexfome.coinmarket.model.Ticker;

import java.util.Comparator;

/**
 * Created by grege on 05.08.2017.
 */

public class ComparatorByPercentage implements Comparator<Ticker> {
    @Override
    public int compare(Ticker ticker_1, Ticker ticker_2) {
        if (ticker_1.percentChange24h > ticker_2.percentChange24h) {
            return -1;
        } else if (ticker_1.percentChange24h < ticker_2.percentChange24h) {
            return 1;
        } else {
            return 0;
        }
    }
}
