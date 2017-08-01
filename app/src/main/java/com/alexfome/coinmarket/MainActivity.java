package com.alexfome.coinmarket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alexfome.coinmarket.adapter.CurrenciesAdapter;
import com.alexfome.coinmarket.api.CoinmarketcapApi;
import com.alexfome.coinmarket.model.Currency;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private List<com.alexfome.coinmarket.model.Currency> currencies;

    @BindView(R.id.currencies_list) protected ListView currenciesList;
    @BindView(R.id.settings_button) protected TextView settingsButton;
    @BindView(R.id.refresh_button) protected TextView refreshButton;
    @BindView(R.id.settings_bar) protected LinearLayout settingsBar;
    @BindView(R.id.sort_type_percentage) protected TextView sortByPercentageButton;
    @BindView(R.id.sort_type_usd) protected TextView sortByUSDButton;
    @BindView(R.id.auto_refresh_state) protected TextView autoRefreshState;
    @BindView(R.id.facebook_login) protected LoginButton loginButton;
    ProgressDialog progressDialog;
    private CurrenciesAdapter adapter;
    private CoinmarketcapApi coinmarketcapApi;
    private ComparatorByPercentage comparatorByPercentage;
    private ComparatorByUSD comparatorByUSD;

    private boolean settingsOpened;

    private int settingsBarHeight = 233;

    private boolean sortByUSD;
    private boolean autoRefresh;

    private String sharedPreferencesName = "sharedPreferencesName";
    private String sortByUSDKey = "sortByUSDKey";
    private String autoRefreshKey = "autoRefreshKey";

    private Handler handler;
    private Runnable refresh;

    SharedPreferences sharedPreferences;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        callbackManager = CallbackManager.Factory.create();

        FontManager.setFont(this, findViewById(R.id.parent), FontManager.BOLDFONT);
        settingsButton.setTypeface(FontManager.getTypeface(this, FontManager.ICONFONT));
        refreshButton.setTypeface(FontManager.getTypeface(this, FontManager.ICONFONT));

        settingsBarHeight = UIManager.dpToPx(this, settingsBarHeight);
        settingsBar.setTop(settingsBarHeight);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settingsBar.getLayoutParams();
        layoutParams.setMargins(0,0,0, -settingsBarHeight);
        settingsBar.setLayoutParams(layoutParams);

        comparatorByPercentage = new ComparatorByPercentage();
        comparatorByUSD = new ComparatorByUSD();

        sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
        sortByUSD = sharedPreferences.getBoolean(sortByUSDKey, false);
        autoRefresh = sharedPreferences.getBoolean(autoRefreshKey, false);

        if (sortByUSD) {
            sortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            sortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
        }
        if (autoRefresh) {
            autoRefreshState.setTextColor(ContextCompat.getColor(this, R.color.dark));
            autoRefreshState.setText ("ON");
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.coinmarketcap.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        coinmarketcapApi = retrofit.create(CoinmarketcapApi.class);

        refresh = new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        };

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        refreshData();

    }

    @OnClick(R.id.refresh_button)
    protected void onRefreshClick () {
        if (!autoRefresh) {
            refreshData();
        }
    }

    private void refreshData () {

        if (handler == null) {
            handler = new Handler();
        }
        if (!autoRefresh) {
            progressDialog.show();
        }
        coinmarketcapApi.getCurrencies().enqueue(new Callback<List<com.alexfome.coinmarket.model.Currency>>() {
            @Override
            public void onResponse(Call<List<com.alexfome.coinmarket.model.Currency>> call, Response<List<com.alexfome.coinmarket.model.Currency>> response) {
                if (progressDialog.isShowing()) progressDialog.hide();
                currencies = response.body();
                displayData();

                if (autoRefresh) {
                    handler.postDelayed(refresh, 6 * 1000);
                }
            }

            @Override
            public void onFailure(Call<List<com.alexfome.coinmarket.model.Currency>> call, Throwable t) {
                if (progressDialog.isShowing()) progressDialog.hide();
                dataLoadFailed();
            }
        });

    }

    @OnClick(R.id.settings_button)
    protected void toggleSettings () {
        if (settingsOpened) {
            closeSettings();
        } else {
            openSettings();
        }
    }

    @OnClick(R.id.auto_refresh_toggle_button)
    protected void toggleAutoRefresh () {
        if (autoRefresh) {
            handler.removeCallbacks(refresh);
            autoRefresh = false;
            autoRefreshState.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            autoRefreshState.setText("OFF");
        } else {
            autoRefreshState.setTextColor(ContextCompat.getColor(this, R.color.dark));
            autoRefreshState.setText("ON");
            autoRefresh = true;
            refreshData();
        }
        sharedPreferences.edit().putBoolean(autoRefreshKey, autoRefresh).commit();
    }

    @OnClick(R.id.sort_toggle_button)
    protected void toggleSortingType () {
        if (sortByUSD) {
            sortByUSD = false;
            sortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            sortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            adapter.switchToPercentageSort();
            displayData();
        } else {
            sortByUSD = true;
            sortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            sortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            adapter.switchToUSDSort();
            displayData();
        }
        sharedPreferences.edit().putBoolean(sortByUSDKey, sortByUSD).commit();
    }

    @OnClick(R.id.privacy_policy_button)
    protected void openPrivacyPolicy () {
        Uri uri = Uri.parse("https://docs.google.com/document/d/1mHdCPVbVPDMXBwESwRpxzBwxfxy6TbzGn02OGP7FyYM/edit?usp=sharing"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openSettings () {
        settingsButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
        settingsOpened = true;
        ViewPropertyObjectAnimator.animate(settingsBar).bottomMargin(0).setDuration(300).start();
    }

    private void closeSettings () {
        settingsButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
        settingsOpened = false;
        ViewPropertyObjectAnimator.animate(settingsBar).bottomMargin(-settingsBarHeight).setDuration(300).start();
    }

    private void displayData () {
        if (!sortByUSD) {
            Collections.sort(currencies, comparatorByPercentage);
        } else {
            Collections.sort(currencies, comparatorByUSD);
        }
        List<Currency> top10 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            top10.add(currencies.get(i));
        }

        if (adapter == null) {
            adapter = new CurrenciesAdapter(top10, this);
            currenciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.extandView(view);
                }
            });
        } else {
            adapter.refreshData(top10);
        }

        if (!sortByUSD) {
            adapter.switchToPercentageSort();
        } else {
            adapter.switchToUSDSort();
        }
        currenciesList.setAdapter(adapter);
    }

    private void dataLoadFailed () {
        Toast.makeText(this, "Please, turn on the internet", Toast.LENGTH_LONG).show();
    }

    private class ComparatorByPercentage implements Comparator<Currency> {

        @Override
        public int compare(Currency currency_1, Currency currency_2) {
            if (currency_1.getPercent_change_24h() > currency_2.getPercent_change_24h()) {
                return -1;
            } else if (currency_1.getPercent_change_24h() < currency_2.getPercent_change_24h()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class ComparatorByUSD implements Comparator<Currency> {

        @Override
        public int compare(Currency currency_1, Currency currency_2) {
            if (currency_1.calculateDeltaUSD() > currency_2.calculateDeltaUSD()) {
                return -1;
            } else if (currency_1.calculateDeltaUSD() < currency_2.calculateDeltaUSD()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (settingsOpened) {
            toggleSettings();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
