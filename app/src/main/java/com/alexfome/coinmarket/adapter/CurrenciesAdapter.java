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
import com.alexfome.coinmarket.Graph;
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

    private Context context;
    private List<Currency> currencies = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private ArrayList<String> selectedCurrenciesIDs = new ArrayList<>();

    private boolean sortByUSD;

    private int extraInfoBarHeight = 40;
    private int graphBarHeight = 100;

    public CurrenciesAdapter (Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        extraInfoBarHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, extraInfoBarHeight, context.getResources().getDisplayMetrics())); // dp to px
        graphBarHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, graphBarHeight, context.getResources().getDisplayMetrics()));
    }

    public void refreshData (List<Currency> currencies) {
        this.currencies = currencies;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return currencies.size();
    }

    @Override
    public Object getItem(int i) {
        return currencies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Currency currency = currencies.get(i);
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.currency, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.currency_name);
            viewHolder.symbol = view.findViewById(R.id.currency_symbol);
            viewHolder.delta = view.findViewById(R.id.currency_delta);
            viewHolder.extraBar = view.findViewById(R.id.extraBar);
            viewHolder.column_1 = (TextView) viewHolder.extraBar.getChildAt(0);
            viewHolder.column_2 = (TextView) viewHolder.extraBar.getChildAt(1);
            viewHolder.graph = view.findViewById(R.id.graph);

            view.setTag(viewHolder);
            FontManager.setFont(context, view, FontManager.BOLDFONT);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int[] colors = context.getResources().getIntArray(R.array.growth_colors);
        int color = colors[i];
        color = ContextCompat.getColor(context, R.color.light_dark);

        LinearLayout.LayoutParams extraInfoBarParams;
        LinearLayout.LayoutParams graphLayoutParams;
        if (selectedCurrenciesIDs.contains(currency.getId())) {
            extraInfoBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, extraInfoBarHeight);
            graphLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, graphBarHeight);
            viewHolder.graph.removeAllViews();
            viewHolder.graph.addView(new Graph(context, currency.getMockData()));
        } else {
            extraInfoBarParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            graphLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            //viewHolder.graph.removeAllViews();
        }
        viewHolder.extraBar.setLayoutParams(extraInfoBarParams);
        viewHolder.graph.setLayoutParams(graphLayoutParams);

        String name = currency.getName();
        if (!(currency.getName() == null || currency.getName().equals(""))) {
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
            name = "no name";
            viewHolder.name.setText(name);
        }

        if (!(currency.getSymbol() == null || currency.getSymbol().equals(""))) {
            viewHolder.symbol.setText(currency.getSymbol());
        } else {
            viewHolder.symbol.setText("no symbol");
        }

        String delta = "";
        if (!sortByUSD) {
            double change_24h = currency.getPercentChange24h();
            if (change_24h < 0) {
                delta = "- ";
            } else if (change_24h > 0) {
                delta = "+ ";
            }
            delta = delta + Math.abs(change_24h) + "%";
            viewHolder.delta.setText(delta);
        } else {
            double change_24h = currency.getPercentChange24h();
            if (change_24h < 0) {
                delta = "- ";
            } else if (change_24h > 0) {
                delta = "+ ";
            }

            double deltaUSD = currency.getDeltaUSD();

            String num = new DecimalFormat("##.##").format(Math.abs(deltaUSD));
            delta = delta + num + "$";
            viewHolder.delta.setText(delta);
        }
        viewHolder.delta.setTextColor(color);

        viewHolder.column_1.setText(
                "PRICE: " + (currency.getPriceUSD() != 0 ? (int) currency.getMarketCapUSD() + " $" : "no data")
        );

        String availableSupply = currency.getAvailableSupply() != null ? currency.getAvailableSupply() + " $" : "no data";
        if (availableSupply.charAt(availableSupply.length() - 4) == '.') {
            availableSupply = availableSupply.substring(0, availableSupply.length() - 4) + " $";
        }
        viewHolder.column_2.setText(
                "CIRCULATING SUPPLY: "
                        + availableSupply
        );

        return view;
    }

    public void toggleView (View view, int position) {

        Currency currency = currencies.get(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (!selectedCurrenciesIDs.contains(currency.getId())) {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(extraInfoBarHeight).setDuration(300).start();
            ViewPropertyObjectAnimator.animate(viewHolder.graph).height(graphBarHeight).setDuration(300).start();
            selectedCurrenciesIDs.add(currency.getId());

            Graph graph = new Graph(context, currency.getMockData());
            viewHolder.graph.addView(graph);
        } else {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(0).setDuration(300).start();
            ViewPropertyObjectAnimator.animate(viewHolder.graph).height(0).setDuration(300).start();
            selectedCurrenciesIDs.remove(currency.getId());

            //viewHolder.graph.removeAllViews();
        }

    }

    public void switchToUSDSort () {
        sortByUSD = true;
    }

    public void switchToPercentageSort () {
        sortByUSD = false;
    }

    private class ViewHolder {
        TextView name;
        TextView symbol;
        TextView delta;
        RelativeLayout extraBar;
        LinearLayout graph;
        TextView column_1;
        TextView column_2;
    }

}
