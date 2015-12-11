package org.emoncms.myapps;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SettingsFragment extends PreferenceFragment
{
    static final String TAG = "SETTINGSFRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.main_preferences);
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState) {
        super.onActivityCreated(savesInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.settings_title);
    }

}