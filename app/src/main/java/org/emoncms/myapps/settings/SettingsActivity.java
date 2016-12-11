package org.emoncms.myapps.settings;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.emoncms.myapps.BaseActivity;
import org.emoncms.myapps.EmonApplication;
import org.emoncms.myapps.R;

import java.util.UUID;


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
        setUpAddNewAccount();

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

    private void setUpAddNewAccount() {
        TextView addAccount = (TextView) findViewById(R.id.add_account);

        addAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAccount();
            }
        });

    }

    private void addNewAccount() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String newAccountId =  EmonApplication.get().addAccount();

        Log.d("Settings","Opening New account " + newAccountId);

        Intent intent = new Intent(this,AccountSettingsActivity.class);
        intent.putExtra("account", newAccountId);
        startActivity(intent);
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