package org.emoncms.myapps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UpgradeManager
{
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static void doUpgrade(Activity mActivity)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
        //String previousVersionName = sp.getString("version_name", "");
        int previousVersionCode = sp.getInt("version_code", 0);

        String currentVersionName = BuildConfig.VERSION_NAME;
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if (previousVersionCode == 0)
            previousVersionCode = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false) ? 1 : 0;

        if (previousVersionCode > 0 &&
                previousVersionCode < currentVersionCode)
        {
            if (previousVersionCode < 123) // Version 1.1.10
                upgrade_0_to_123(mActivity, sp);

            if (previousVersionCode < 127) // Version 1.1.10
                upgrade_123_to_124(mActivity, sp);
        }

        sp.edit().putInt("version_code", currentVersionCode).apply();
        sp.edit().putString("version_name", currentVersionName).apply();
    }

    public static void upgrade_0_to_123(Activity mActivity, SharedPreferences sp) {
        String emoncmsURL = sp.getString("emoncms_url", "emoncms.org");
        Boolean useSSL = sp.getBoolean("emoncms_usessl", false);

        if (emoncmsURL.toLowerCase().equals("emoncms.org") && !useSSL)
        {
            sp.edit().putBoolean("emoncms_usessl", true).commit();
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

    public static void upgrade_123_to_124(Activity mActivity, SharedPreferences sp) {


        String accountId = EmonApplication.get().addAccount();
        EmonApplication.get().setCurrentAccount(accountId);

        String emoncmsURL = sp.getString("emoncms_url", "emoncms.org");
        String emoncmsAPI = sp.getString("emoncms_apikey", ".org");
        Boolean useSSL = sp.getBoolean("emoncms_usessl", false);
        String powerFeed = sp.getString("myelectric_power_feed","-1");
        String scale = sp.getString("myelectric_escale","-1");
        String unitCost = sp.getString("myelectric_unit_cost","0");
        String costSymbol = sp.getString("myelectric_cost_symbol","0");

        sp.edit().remove("emoncms_url")
                .remove("emoncms_apikey")
                .remove("emoncms_usessl")
                .remove("myelectric_power_feed")
                .remove("myelectric_escale")
                .remove("myelectric_unit_cost")
                .remove("myelectric_cost_symbol")
                .commit();


        SharedPreferences.Editor accountPrefs = EmonApplication.get().getSharedPreferences(accountId).edit();
        accountPrefs.putString("emoncms_url", emoncmsURL);
        accountPrefs.putString("emoncms_apikey", emoncmsAPI);
        accountPrefs.putBoolean("emoncms_usessl", useSSL);

        accountPrefs.putString("myelectric_power_feed", powerFeed);
        accountPrefs.putString("myelectric_escale", scale);
        accountPrefs.putString("myelectric_unit_cost", unitCost);
        accountPrefs.putString("myelectric_cost_symbol", costSymbol);
        accountPrefs.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Multiple Accounts Supported");
        builder.setMessage(
                "This application has been upgraded to support multiple accounts. " +
                        "Your existing settings have been migrated to the first account. ");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();

    }
}
