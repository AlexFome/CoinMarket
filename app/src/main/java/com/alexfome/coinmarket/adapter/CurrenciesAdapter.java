package com.alexfome.coinmarket.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexfome.coinmarket.FontManager;
import com.alexfome.coinmarket.R;
import com.alexfome.coinmarket.model.Currency;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by grege on 01.08.2017.
 */

public class CurrenciesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Currency> mCurrencies = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mSelectedCurrenciesIDs = new ArrayList<>();

    private boolean mSortByUSD;

    private int mExtraInfoBarHeight;

    public CurrenciesAdapter (Context context) {
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mExtraInfoBarHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mContext.getResources().getDimension(R.dimen.extra_info_bar_height), mContext.getResources().getDisplayMetrics())); // dp to px
    }

    public void refreshData (List<Currency> currencies, boolean sortByUSD) {
        mCurrencies = currencies;
        mSortByUSD = sortByUSD;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCurrencies.size();
    }

    @Override
    public Object getItem(int i) {
        return mCurrencies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Currency currency = mCurrencies.get(i);
        ViewHolder viewHolder;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.currency, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.currency_name);
            viewHolder.symbol = view.findViewById(R.id.currency_symbol);
            viewHolder.delta = view.findViewById(R.id.currency_delta);
            viewHolder.extraBar = view.findViewById(R.id.extraBar);
            viewHolder.column_1 = (TextView) viewHolder.extraBar.getChildAt(0);
            viewHolder.column_2 = (TextView) viewHolder.extraBar.getChildAt(1);
            view.setTag(viewHolder);
            FontManager.setFont(mContext, view, FontManager.BOLDFONT);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        LinearLayout.LayoutParams extraInfoBarParams;
        if (mSelectedCurrenciesIDs.contains(currency.id)) {
            extraInfoBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mExtraInfoBarHeight);
        } else {
            extraInfoBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        }
        viewHolder.extraBar.setLayoutParams(extraInfoBarParams);
        String name = currency.name;
        if (!(currency.name == null || currency.name.equals(""))) {
            int breakpoint = 13;
            if (name.length() > breakpoint) {
                name = name.substring(0, breakpoint);
                if (name.charAt(name.length() - 1) == ' ') {
                    name = name.substring(0, breakpoint - 1);
                }
                viewHolder.name.setText(name + "...");
            } else {
                viewHolder.name.setText(name);
            }
        } else {
            name = mContext.getResources().getString(R.string.no_name);
            viewHolder.name.setText(name);
        }
        if (!(currency.symbol == null || currency.symbol.equals(""))) {
            viewHolder.symbol.setText(currency.symbol);
        } else {
            viewHolder.symbol.setText(mContext.getResources().getString(R.string.no_symbol));
        }

        String delta = "";
        double change_24h = currency.percentChange24h;
        if (change_24h < 0) {
            delta = "- ";
        } else if (change_24h > 0) {
            delta = "+ ";
        }
        if (!mSortByUSD) {
            delta = delta + Math.abs(change_24h) + "%";
        } else {
            double deltaUSD = currency.deltaUSD;
            String num = new DecimalFormat("##.##").format(Math.abs(deltaUSD));
            delta = delta + num + "$";
        }
        viewHolder.delta.setText(delta);
        viewHolder.delta.setTextColor(ContextCompat.getColor(mContext, R.color.light_dark));
        String price = mContext.getResources().getString(R.string.price) + " ";
        if (currency.priceUSD != 0) {
            price = price + (mSortByUSD ? (int) currency.priceUSD : new DecimalFormat("##.####").format(Math.abs(currency.priceUSD))) + " $";
        } else {
            price = price + mContext.getResources().getString(R.string.no_data);
        }
        viewHolder.column_1.setText(price);
        String availableSupply = currency.availableSupply != 0.0 ? (int) currency.availableSupply + " $" : mContext.getResources().getString(R.string.no_data);
        viewHolder.column_2.setText(
                mContext.getResources().getString(R.string.circulating_supply)
                        + " "
                        + availableSupply
        );

        return view;
    }

    public void toggleView (View view, int position) {
        Currency currency = mCurrencies.get(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (!mSelectedCurrenciesIDs.contains(currency.id)) {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(mExtraInfoBarHeight).setDuration(300).start();
            mSelectedCurrenciesIDs.add(currency.id);
        } else {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(0).setDuration(300).start();
            mSelectedCurrenciesIDs.remove(currency.id);
        }
    }

    private class ViewHolder {
        TextView name;
        TextView symbol;
        TextView delta;
        RelativeLayout extraBar;
        TextView column_1;
        TextView column_2;
    }

}
