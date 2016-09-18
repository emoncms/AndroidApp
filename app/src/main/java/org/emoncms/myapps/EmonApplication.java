package org.emoncms.myapps;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extend Application so it can hold the list of active accounts
 */
public class EmonApplication extends Application {

    private static final String ACCOUNT_PREFS_FILE = "emoncms_account_";
    private static final String PREF_ACCOUNTS = "accounts";
    private static final String PREF_CURRENT_ACCOUNT = "app_current_account";
    private static EmonApplication instance;

    private List<AccountListChangeListener> listeners;
    private Map<String,String> accounts;
    private String currentAccount;

    public static EmonApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        accounts = new LinkedHashMap<>();
        currentAccount = settings.getString(PREF_CURRENT_ACCOUNT,"");
        String accountSettings = settings.getString(PREF_ACCOUNTS, "");

        String[] accountIdList = accountSettings.split(",");

        for (String account : accountIdList) {
            if (!account.trim().isEmpty()) {
                String name = loadAccountName(account);
                accounts.put(account,name);
            }
        }

        instance = this;

        listeners = new ArrayList<>();
    }

    private String loadAccountName(String accountId) {
        return getSharedPreferences(accountId).getString("emoncms_name","emoncms");
    }

    public SharedPreferences getSharedPreferences(String accountId) {
        return getApplicationContext().getSharedPreferences(EmonApplication.getAccountSettingsFile(accountId), Context.MODE_PRIVATE);
    }

    public Map<String,String> getAccounts() {
        return accounts;
    }

    public void addAccountChangeListener(AccountListChangeListener listener) {
        listeners.add(listener);
    }

    public void addAccount(String accountId, String accountName) {
        accounts.put(accountId,accountName);
        for (AccountListChangeListener listener : listeners) {
            listener.onAddAccount(accountId,accountName);
        }
        writeAccountList();
    }

    public void updateAccount(String accountId, String accountName) {
        accounts.put(accountId,accountName);
        for (AccountListChangeListener listener : listeners) {
            listener.onUpdateAccount(accountId,accountName);
        }
    }

    public void removeAccount(String accountId) {
        accounts.remove(accountId);
        for (AccountListChangeListener listener : listeners) {
            listener.onDeleteAccount(accountId);
        }
        writeAccountList();
    }

    private void writeAccountList() {
        String accountSettings = android.text.TextUtils.join(",", accounts.keySet());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString(PREF_ACCOUNTS, accountSettings).apply();
    }

    public String getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(String currentAccount) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putString(PREF_CURRENT_ACCOUNT, currentAccount).apply();
        this.currentAccount = currentAccount;
    }

    public static String getAccountSettingsFile(String account) {
        return ACCOUNT_PREFS_FILE + account;
    }
}
