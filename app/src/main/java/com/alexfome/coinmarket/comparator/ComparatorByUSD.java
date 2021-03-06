package com.alexfome.coinmarket.comparator;

import com.alexfome.coinmarket.model.Currency;

import java.util.Comparator;

/**
 * Created by grege on 05.08.2017.
 */

public class ComparatorByUSD implements Comparator<Currency> {
    @Override
    public int compare(Currency currency_1, Currency currency_2) {
        if (currency_1.deltaUSD > currency_2.deltaUSD) {
            return -1;
        } else if (currency_1.deltaUSD < currency_2.deltaUSD) {
            return 1;
        } else {
            return 0;
        }
    }
}
