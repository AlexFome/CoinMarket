package com.alexfome.coinmarket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alexfome.coinmarket.adapter.CurrenciesAdapter;
import com.alexfome.coinmarket.model.Currencies;
import com.alexfome.coinmarket.model.Currency;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Currencies currencies;

    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.currencies_list) protected ListView currenciesList;
    @BindView(R.id.settings_bar) protected LinearLayout settingsBar;
    @BindView(R.id.sort_type_percentage) protected TextView sortByPercentageButton;
    @BindView(R.id.sort_type_usd) protected TextView sortByUSDButton;
    @BindView(R.id.auto_refresh_state) protected Switch autoRefreshState;
    @BindView(R.id.facebook_login) protected LoginButton loginButton;
    private ProgressDialog progressDialog;
    private CurrenciesAdapter adapter;

    private boolean settingsOpened;

    private int settingsBarHeight = 233;
    private int AUTO_REFRESH_INTERVAL = 6 * 1000;

    private boolean sortByUSD;
    private boolean autoRefresh;

    private String sharedPreferencesName = "sharedPreferencesName";
    private String sortByUSDKey = "sortByUSDKey";
    private String autoRefreshKey = "autoRefreshKey";

    SharedPreferences sharedPreferences;
    CallbackManager callbackManager;

    private Handler handler;
    private Runnable refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        currencies = new Currencies();

        initListeners();
        setToolbar ();
        setAdapter ();
        initFacebookLogin();
        setFonts();
        prepareSettingsBar();
        applySavedSettings ();
        initProgressBar ();
        initAutoRefreshHandler ();

        refreshData();

    }

    protected void onRefreshClick () {
        if (!autoRefresh) {
            refreshData();
        }
    }

    private void refreshData () {

        final Context context = this;
        currencies.refreshData(new Callback<List<Currency>>() {
            @Override
            public void onResponse(Call<List<Currency>> call, Response<List<Currency>> response) {
                currencies.setList(response.body());
                displayData();

                if (autoRefresh) {
                    handler.postDelayed(refresh, AUTO_REFRESH_INTERVAL);
                }
                if (progressDialog.isShowing()) {
                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<List<Currency>> call, Throwable t) {
                dataLoadFailed();

                if (autoRefresh) {
                    handler.postDelayed(refresh, AUTO_REFRESH_INTERVAL);
                }
                if (progressDialog.isShowing()) {
                    progressDialog.hide();
                }
            }
        });

    }

    protected void toggleSettings () {
        if (settingsOpened) {
            closeSettings();
        } else {
            openSettings();
        }
    }

    private void startAutoRefresh() {
        refreshData();
    }

    private void stopAutoRefresh() {
        handler.removeCallbacks(refresh);
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
        sharedPreferences.edit().putBoolean(sortByUSDKey, sortByUSD).apply();
    }

    @OnClick(R.id.privacy_policy_button)
    protected void openPrivacyPolicy () {
        Uri uri = Uri.parse("https://docs.google.com/document/d/1mHdCPVbVPDMXBwESwRpxzBwxfxy6TbzGn02OGP7FyYM/edit?usp=sharing");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openSettings () {
        settingsOpened = true;
        ViewPropertyObjectAnimator.animate(settingsBar).bottomMargin(0).setDuration(300).start();
    }

    private void closeSettings () {
        settingsOpened = false;
        ViewPropertyObjectAnimator.animate(settingsBar).bottomMargin(-settingsBarHeight).setDuration(300).start();
    }

    private void displayData () {
        if (!sortByUSD) {
            adapter.refreshData(currencies.getTopByPercentage());
        } else {
            adapter.refreshData(currencies.getTopByUSDdeltaValue());
        }
    }

    private void dataLoadFailed () {
        Toast.makeText(this, "Please, turn on the internet", Toast.LENGTH_LONG).show();
    }

    private void initFacebookLogin () {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
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
    }

    private void setAdapter () {
        adapter = new CurrenciesAdapter(this);
        currenciesList.setAdapter(adapter);
        currenciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleView(view, position);
            }
        });
    }

    private void applySavedSettings() {
        sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);
        sortByUSD = sharedPreferences.getBoolean(sortByUSDKey, false);
        autoRefresh = sharedPreferences.getBoolean(autoRefreshKey, false);

        if (sortByUSD) {
            sortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            sortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            adapter.switchToUSDSort();
        }
        if (autoRefresh) {
            autoRefreshState.setChecked(true);
        }
    }

    private void prepareSettingsBar () {
        settingsBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, settingsBarHeight, getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settingsBar.getLayoutParams();
        layoutParams.setMargins(0,0,0, -settingsBarHeight);
        settingsBar.setLayoutParams(layoutParams);
    }

    private void initProgressBar () {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void setFonts () {
        FontManager.setFont(this, findViewById(R.id.parent), FontManager.BOLDFONT);
    }

    private void initListeners () {
        autoRefreshState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    autoRefresh = false;
                    stopAutoRefresh();
                } else {
                    autoRefresh = true;
                    startAutoRefresh();
                }
                sharedPreferences.edit().putBoolean(autoRefreshKey, autoRefresh).apply();
            }
        });
    }

    private void initAutoRefreshHandler () {
        handler = new Handler();
        refresh = new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        };
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

    private void setToolbar () {
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                toggleSettings ();
                return true;
            case R.id.action_refresh:
                onRefreshClick ();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        if (autoRefresh) {
            stopAutoRefresh();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (autoRefresh) {
            startAutoRefresh();
        }
        super.onResume();
    }
}
