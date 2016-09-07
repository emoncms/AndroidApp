package org.emoncms.myapps;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend Application so it can hold the list of active accounts
 */
public class EmonApplication extends Application {

    private static EmonApplication instance;

    private List<String> accounts;

    public static EmonApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        String accountSettings = settings.getString("accounts","");
        String[] accountIdList = accountSettings.split(",");

        accounts = new ArrayList<>();
        for (String account : accountIdList) {
            accounts.add(account);
        }
        instance = this;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void addAccount(String accountId) {
        accounts.add(accountId);
        writeAccountList();
    }

    public void removeAccount(String accountId) {
        accounts.remove(accountId);
        writeAccountList();
    }

    private void writeAccountList() {
        String accountSettings = android.text.TextUtils.join(",", accounts);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString("accounts", accountSettings).apply();
    }
}
