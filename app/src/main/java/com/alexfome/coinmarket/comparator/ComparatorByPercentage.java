package com.alexfome.coinmarket.comparator;

import com.alexfome.coinmarket.model.Currency;

import java.util.Comparator;

/**
 * Created by grege on 05.08.2017.
 */

public class ComparatorByPercentage implements Comparator<Currency> {
    @Override
    public int compare(Currency currency_1, Currency currency_2) {
        if (currency_1.getPercentChange24h() > currency_2.getPercentChange24h()) {
            return -1;
        } else if (currency_1.getPercentChange24h() < currency_2.getPercentChange24h()) {
            return 1;
        } else {
            return 0;
        }
    }
}
