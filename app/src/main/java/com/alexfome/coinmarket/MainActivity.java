package com.alexfome.coinmarket;

import android.app.ProgressDialog;
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
import com.alexfome.coinmarket.model.Ticker;
import com.alexfome.coinmarket.model.TickerLists;
import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME";
    private final String SORT_BY_USD_KEY = "SORT_BY_USD_KEY";
    private final String AUTO_REFRESH_KEY = "AUTO_REFRESH_KEY";
    private final int AUTO_REFRESH_INTERVAL = 6 * 1000;

    private TickerLists mTickerLists;

    @BindView(R.id.toolbar) protected Toolbar mToolbar;
    @BindView(R.id.currencies_list) protected ListView mCurrenciesList;
    @BindView(R.id.settings_bar) protected LinearLayout mSettingsBar;
    @BindView(R.id.sort_type_percentage) protected TextView mSortByPercentageButton;
    @BindView(R.id.sort_type_usd) protected TextView mSortByUSDButton;
    @BindView(R.id.auto_refresh_state) protected Switch mAutoRefreshState;
    @BindView(R.id.facebook_login) protected LoginButton mLoginButton;
    private ProgressDialog mProgressDialog;
    private CurrenciesAdapter mAdapter;
    private boolean mSettingsOpened;
    private int mSettingsBarHeight;
    private boolean mSortByUSD;
    private boolean mAutoRefresh;
    private SharedPreferences mSharedPreferences;
    private CallbackManager mCallbackManager;
    private Handler mHandler;
    private Runnable mRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mTickerLists = new TickerLists();
        initListeners();
        setSupportActionBar(mToolbar);
        setAdapter ();
        initAutoRefreshRunnable();
        initFacebookLogin();
        setFonts();
        prepareSettingsBar();
        applySavedSettings ();
        initProgressBar ();
        refreshData();
        if (mAutoRefresh) {
            mHandler.postDelayed(mRefresh, AUTO_REFRESH_INTERVAL);
        }
    }

    protected void onRefreshClick () {
        if (!mAutoRefresh) {
            refreshData();
        }
    }

    private void refreshData () {
        mTickerLists.refreshData(new IDataRefreshCallback() {
            @Override
            public void onSuccess() {
                displayData();
                hideProgressBar();
            }

            @Override
            public void onFail() {
                dataLoadFailed();
                hideProgressBar();
            }
        });
    }

    private void hideProgressBar () {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    protected void toggleSettings () {
        if (mSettingsOpened) {
            closeSettings();
        } else {
            openSettings();
        }
    }

    private void startAutoRefresh() {
        refreshData();
        mHandler.postDelayed(mRefresh, AUTO_REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        mHandler.removeCallbacks(mRefresh);
    }

    @OnClick(R.id.sort_toggle_button)
    protected void toggleSortingType () {
        if (mSortByUSD) {
            mSortByUSD = false;
            mSortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            mSortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            displayData();
        } else {
            mSortByUSD = true;
            mSortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
            mSortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            displayData();
        }
        mSharedPreferences.edit().putBoolean(SORT_BY_USD_KEY, mSortByUSD).apply();
    }

    @OnClick(R.id.privacy_policy_button)
    protected void openPrivacyPolicy () {
        Uri uri = Uri.parse(getResources().getString(R.string.privacy_policy_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openSettings () {
        mSettingsOpened = true;
        ViewPropertyObjectAnimator.animate(mSettingsBar).bottomMargin(0).setDuration(300).start();
    }

    private void closeSettings () {
        mSettingsOpened = false;
        ViewPropertyObjectAnimator.animate(mSettingsBar).bottomMargin(-mSettingsBarHeight).setDuration(300).start();
    }

    private void displayData () {
        List<Ticker> data = mSortByUSD ? mTickerLists.getTopByUSDdeltaValue() : mTickerLists.getTopByPercentage();
        mAdapter.refreshData(data, mSortByUSD);
    }

    private void dataLoadFailed () {
        Toast.makeText(this, getResources().getString(R.string.please_turn_on_the_internet), Toast.LENGTH_LONG).show();
    }

    private void initFacebookLogin () {
        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
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
        mAdapter = new CurrenciesAdapter(this);
        mCurrenciesList.setAdapter(mAdapter);
        mCurrenciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.toggleView(view, position);
            }
        });
    }

    private void applySavedSettings() {
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mSortByUSD = mSharedPreferences.getBoolean(SORT_BY_USD_KEY, false);
        mAutoRefresh = mSharedPreferences.getBoolean(AUTO_REFRESH_KEY, false);
        if (mSortByUSD) {
            mSortByUSDButton.setTextColor(ContextCompat.getColor(this, R.color.dark));
            mSortByPercentageButton.setTextColor(ContextCompat.getColor(this, R.color.light_dark));
        }
        mAutoRefreshState.setChecked(mAutoRefresh);
    }

    private void prepareSettingsBar () {
        mSettingsBarHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.settings_bar_height), getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSettingsBar.getLayoutParams();
        layoutParams.setMargins(0,0,0, -mSettingsBarHeight);
        mSettingsBar.setLayoutParams(layoutParams);
    }

    private void initProgressBar () {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.loading));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    private void setFonts () {
        FontManager.setFont(this, findViewById(R.id.parent), FontManager.BOLDFONT);
    }

    private void initListeners () {
        mAutoRefreshState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAutoRefresh = isChecked;
                if (!mAutoRefresh) {
                    stopAutoRefresh();
                } else {
                    startAutoRefresh();
                }
                mSharedPreferences.edit().putBoolean(AUTO_REFRESH_KEY, mAutoRefresh).apply();
            }
        });
    }

    private void initAutoRefreshRunnable () {
        mHandler = new Handler();
        mRefresh = new Runnable() {
            @Override
            public void run() {
                refreshData();
                mHandler.postDelayed(mRefresh, AUTO_REFRESH_INTERVAL);
            }
        };
    }

    @Override
    public void onBackPressed()
    {
        if (mSettingsOpened) {
            toggleSettings();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
        if (mAutoRefresh) {
            stopAutoRefresh();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mAutoRefresh && mHandler != null && mRefresh != null) {
            startAutoRefresh();
        }
        super.onResume();
    }
}
