package org.emoncms.myapps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpgradeManager
{
    public static void doUpgrade(Activity mActivity)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
        //String previousVersionName = sp.getString("version_name", "");
        int previousVersionCode = sp.getInt("version_code", 0);

        String currentVersionName = BuildConfig.VERSION_NAME;
        int currentVersionCode = BuildConfig.VERSION_CODE;

        //previousVersionCode = 120; // for testing
        if (previousVersionCode < currentVersionCode)
        {
            if (previousVersionCode <= 120) // Version 1.1.7
            {
                String emoncmsURL = sp.getString("emoncms_url", "emoncms.org");
                Boolean useSSL = sp.getBoolean("emoncms_usessl", false);

                if (emoncmsURL.toLowerCase().equals("emoncms.org") && !useSSL)
                {
                    sp.edit().putBoolean("emoncms_usessl", true).apply();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("You're now more secure");
                    builder.setMessage(
                            "emoncms.org has been upgraded to use SSL. " +
                            "This means your communication with the website is now encrypted. " +
                            "Your settings have been automatically updated to use this new feature.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.create().show();
                }
            }

            sp.edit().putInt("version_code", currentVersionCode).apply();
            sp.edit().putString("version_name", currentVersionName).apply();
        }
    }
}
