package org.emoncms.myapps;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.emoncms.myapps.db.EmonDatabaseHelper;
import org.emoncms.myapps.myelectric.MyElectricSettings;

/**
 * Activity just handles feed settings an instance of my electric.
 * Needs to know which instance it is dealing with!
 */
public class MyElectricSettingsActivity extends BaseActivity {

    private MyElectricSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("settings")) {
            settings = getIntent().getExtras().getParcelable("settings");
        } else {
            String defaultName = getResources().getString(R.string.settings_title_page_name_default);
            settings = new MyElectricSettings(0,defaultName,-1,-1,"1", "0","£","");
        }

        setContentView(R.layout.activity_ms_settings);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.page_settings, menu);
        return true;
    }

    private void setUpFragments() {
        String fragmentTag = getResources().getString(R.string.tag_settings_fragment);

        Fragment frag = getFragmentManager().findFragmentByTag(fragmentTag + settings.getId());
        if (frag == null) {
            frag = new MyElectricSettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("settings",settings);
            frag.setArguments(bundle);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.prefs, frag, fragmentTag)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_page:
                deletePage();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return( super.onOptionsItemSelected(item));
    }

    private void deletePage() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(R.string.settings_title_delete_page);
        builder.setMessage(R.string.settings_confirm_delete_page);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EmonApplication.get().removePage(settings);
                settings.setDeleted();
                onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();


    }



}