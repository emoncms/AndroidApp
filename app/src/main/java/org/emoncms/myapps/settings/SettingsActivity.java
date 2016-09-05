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
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpToolbar();
        setUpFragments();
    }

    private void setUpToolbar() {
        Toolbar  toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpFragments() {
        String fragmentTag = getResources().getString(R.string.tag_settings_fragment);
        String accountsTag = getResources().getString(R.string.tag_accounts_fragment);
        Fragment fragAccounts = getFragmentManager().findFragmentByTag(accountsTag);
        if (fragAccounts == null) {
            fragAccounts = new AccountListFragment();
        }

        Fragment frag = getFragmentManager().findFragmentByTag(fragmentTag);
        if (frag == null) {
            frag = new AppSettingsFragment();
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.accounts, fragAccounts, accountsTag)
                .commit();
        getFragmentManager().beginTransaction()
                .replace(R.id.prefs, frag, fragmentTag)
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