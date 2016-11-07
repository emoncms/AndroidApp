package org.emoncms.myapps;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Activity just handles feed settings an instance of my electric.
 * Needs to know which instance it is dealing with!
 */
public class MyElectricSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void setUpFragments() {
        String fragmentTag = getResources().getString(R.string.tag_settings_fragment);

        //TODO If change fragment to set account id on resume, could avoid appending account to tag
        Fragment frag = getFragmentManager().findFragmentByTag(fragmentTag + EmonApplication.get().getCurrentAccount());
        if (frag == null) {
            frag = new MyElectricSettingsFragment();
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.prefs, frag, fragmentTag)
                .commit();
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