package com.alexfome.coinmarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alexfome.coinmarket.FontManager;
import com.alexfome.coinmarket.R;
import com.alexfome.coinmarket.UIManager;

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

    int extensionHeight = 1000;

    public CurrenciesAdapter (List<com.alexfome.coinmarket.model.Currency> currencies, Context context) {
        this.currencies = currencies;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
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

            view.setTag(viewHolder);
            FontManager.setFont(context, view, FontManager.BOLDFONT);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int[] colors = context.getResources().getIntArray(R.array.growth_colors);
        UIManager.setBackgroundShapeColor(view.getBackground(), colors[i]);

        RelativeLayout.LayoutParams layoutParams;
        if (viewHolder.extanded) {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, extensionHeight);
        } else {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
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

        return view;
    }

    public void extandView (View view) {

        // IN DEVELOPMENT

//        ViewHolder viewHolder = (ViewHolder) view.getTag();
//        if (!viewHolder.extanded) {
//            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(extensionHeight).setDuration(300).start();
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            //viewHolder.notesBar.setLayoutParams(layoutParams);
//            viewHolder.extanded = true;
//            extendedTasks.add(view);
//        } else {
//            ViewPropertyObjectAnimator.animate(viewHolder.extraBar).height(0).setDuration(300).start();
//            //ViewPropertyObjectAnimator.animate(viewHolder.notesBar).height(0).setDuration(300).start();
//            viewHolder.extanded = false;
//            extendedTasks.remove(view);
//        }

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
        for (int i = 0; i < extendedTasks.size(); i++) {
            extandView (extendedTasks.get(i));
        }
    }

    private class ViewHolder {
        TextView name;
        TextView delta;
        LinearLayout extraBar;
        boolean extanded;
    }
}
