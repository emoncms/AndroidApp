package org.emoncms.myapps;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString(getString(R.string.setting_language), getString(R.string.setting_language));
        Configuration config = getResources().getConfiguration();

        if (lang.equals(getString(R.string.setting_language)))
            config.locale = Locale.getDefault();
        else
            config.locale = new Locale(lang);

        Resources res = getBaseContext().getResources();
        getResources().updateConfiguration(config, res.getDisplayMetrics());

        super.onCreate(savedInstanceState);
    }
}
