package com.alexfome.coinmarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alexfome.coinmarket.FontManager;
import com.alexfome.coinmarket.R;
import com.alexfome.coinmarket.UIManager;
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
    private List<com.alexfome.coinmarket.model.Currency> currencies;
    LayoutInflater layoutInflater;
    ArrayList<View> extendedTasks = new ArrayList<>();

    boolean sortByUSD;

    int extensionHeight = 75;

    public CurrenciesAdapter (List<com.alexfome.coinmarket.model.Currency> currencies, Context context) {
        this.currencies = currencies;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        extensionHeight = UIManager.dpToPx(context, extensionHeight);
    }

    public void refreshData (List<com.alexfome.coinmarket.model.Currency> currencies) {
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

        com.alexfome.coinmarket.model.Currency currency = currencies.get(i);
        ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.currency, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.currency_name);
            viewHolder.delta = view.findViewById(R.id.currency_delta);
            viewHolder.extraBar = view.findViewById(R.id.extraBar);
            viewHolder.column_1 = (TextView) viewHolder.extraBar.getChildAt(0);
            viewHolder.column_2 = (TextView) viewHolder.extraBar.getChildAt(1);
            viewHolder.column_3 = (TextView) viewHolder.extraBar.getChildAt(2);

            view.setTag(viewHolder);
            FontManager.setFont(context, view, FontManager.BOLDFONT);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int[] colors = context.getResources().getIntArray(R.array.growth_colors);
        UIManager.setBackgroundShapeColor(view.getBackground(), colors[i]);

        LinearLayout.LayoutParams layoutParams;
        if (currency.selected) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, extensionHeight);
        } else {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        }
        viewHolder.extraBar.setLayoutParams(layoutParams);

        String name = currency.getName();
        int breakpoint = 11;
        if (name.length() > breakpoint) {
            name = name.substring(0, breakpoint);
            if (name.charAt(name.length() - 1) == ' ') {
                name = name.substring(0, breakpoint - 1);
            }
            viewHolder.name.setText(name + "...");
        } else {
            viewHolder.name.setText(name);
        }

        String delta = "";
        if (!sortByUSD) {
            float change_24h = currency.getPercent_change_24h();
            if (change_24h < 0) {
                delta = "- ";
            } else if (change_24h > 0) {
                delta = "+ ";
            }
            delta = delta + Math.abs(change_24h) + "%";
            viewHolder.delta.setText(delta);
        } else {
            float change_24h = currency.getPercent_change_24h();
            if (change_24h < 0) {
                delta = "- ";
            } else if (change_24h > 0) {
                delta = "+ ";
            }

            float deltaUSD = currency.calculateDeltaUSD();

            String num = new DecimalFormat("##.##").format(Math.abs(deltaUSD));
            delta = delta + num + "$";
            viewHolder.delta.setText(delta);
        }

        viewHolder.column_1.setText(
                "Market cap"
                        + "\n\n"
                        + (currency.getMarket_cap_usd() != 0 ? (int) currency.getMarket_cap_usd() + " $" : "no data")
        );
        viewHolder.column_2.setText(
                "Volume (24h)"
                        + "\n\n"
                        + (currency.getVolume_24h_usd() != 0 ? (int) currency.getVolume_24h_usd() + " $" : "no data")

        );

        String availableSupply = currency.getAvailable_supply() != null ? currency.getAvailable_supply() + " $" : "no data";
        if (availableSupply.charAt(availableSupply.length() - 4) == '.') {
            availableSupply = availableSupply.substring(0, availableSupply.length() - 4) + " $";
        }
        viewHolder.column_3.setText(
                "Circulating Supply"
                        + "\n\n"
                        + availableSupply
        );

        return view;
    }

    public void toggleView (View view, int position) {

        Currency currency = currencies.get(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (!currency.selected) {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(extensionHeight).setDuration(300).start();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //viewHolder.notesBar.setLayoutParams(layoutParams);
            viewHolder.extanded = true;
            currency.selected = true;
            extendedTasks.add(view);
        } else {
            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(0).setDuration(300).start();
            //ViewPropertyObjectAnimator.animate(viewHolder.notesBar).height(0).setDuration(300).start();
            currency.selected = false;
            viewHolder.extanded = false;
            extendedTasks.remove(view);
        }

    }

    private void closeView (View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(0).setDuration(300).start();
        //ViewPropertyObjectAnimator.animate(viewHolder.notesBar).height(0).setDuration(300).start();
        viewHolder.extanded = false;
        extendedTasks.remove(view);
    }

    public void switchToUSDSort () {
        sortByUSD = true;
        notifyDataSetChanged();
    }

    public void switchToPercentageSort () {
        sortByUSD = false;
        notifyDataSetChanged();
    }

    public void closeAllOpenedTasks () {
        for (int i = 0; i < currencies.size(); i++) {
            currencies.get(i).selected = false;
        }
        for (int i = 0; i < extendedTasks.size(); i++) {
            closeView(extendedTasks.get(i));
        }
    }

    private class ViewHolder {
        TextView name;
        TextView delta;
        LinearLayout extraBar;
        TextView column_1;
        TextView column_2;
        TextView column_3;
        boolean extanded;
    }
}
