package org.emoncms.myapps.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.emoncms.myapps.BaseActivity;
import org.emoncms.myapps.R;


/**
 * Activity to handle settings
 */
public class AccountSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        setUpToolbar();
        setUpFragments();
    }

    private void setUpToolbar() {
        Toolbar  toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.account_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpFragments() {

        String accountsSettingsTag = "account_settings_fragment";
        Fragment fragAccounts = getFragmentManager().findFragmentByTag(accountsSettingsTag);
        if (fragAccounts == null) {
            fragAccounts = new AccountSettingsFragment();
        }

        //pass this on as contains accountId
        fragAccounts.setArguments(getIntent().getExtras());

        getFragmentManager().beginTransaction()
                .replace(R.id.prefs, fragAccounts, accountsSettingsTag)
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return( super.onOptionsItemSelected(item));
    }


}