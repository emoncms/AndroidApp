package org.emoncms.myapps;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.emoncms.myapps.db.EmonDatabaseHelper;
import org.emoncms.myapps.myelectric.MyElectricSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Extend Application so it can hold the list of accounts, and handle adding/removing
 */
public class EmonApplication extends Application {

    private static final String ACCOUNT_PREFS_FILE = "emoncms_account_";
    private static final String PREF_ACCOUNTS = "accounts";
    private static final String PREF_CURRENT_ACCOUNT = "app_current_account";
    private static EmonApplication instance;

    private List<AccountListChangeListener> accountChangeListeners;
    private List<PageChangeListener> pageChangeListeners;


    private Map<String,String> accounts;
    private String currentAccount;

    private List<MyElectricSettings> pages;

    int currentPageIndex;

    public static EmonApplication get() {
        return instance;
    }

    public static String getAccountSettingsFile(String account) {
        return ACCOUNT_PREFS_FILE + account;
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

        loadPages();

        instance = this;

        accountChangeListeners = new ArrayList<>();
        pageChangeListeners = new ArrayList<>();
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
        accountChangeListeners.add(listener);
    }

    public void addPageChangeListener(PageChangeListener listener) {
        pageChangeListeners.add(listener);
    }

    public void removePageChangeListener(PageChangeListener listener) {
        pageChangeListeners.remove(listener);
    }

    public String addAccount() {

        String accountId = UUID.randomUUID().toString();
        String accountName = "emoncms" + (EmonApplication.get().getAccounts().size() + 1);

        SharedPreferences sharedPrefs = getSharedPreferences(accountId);
        sharedPrefs.edit().putString("emoncms_name", accountName).apply();

        accounts.put(accountId,accountName);

        for (AccountListChangeListener listener : accountChangeListeners) {
            listener.onAddAccount(accountId,accountName);
        }

        writeAccountList();

        if (accounts.size() == 1) {
            setCurrentAccount(accountId);
        }
        return accountId;
    }

    public void addFirstPage() {
        MyElectricSettings defaultPage = new MyElectricSettings(0,"My Electric",0,0,0,"Pounds","£");
        int id = EmonDatabaseHelper.getInstance(this).addPage(currentAccount,defaultPage);
        defaultPage.setId(id);
        if (currentAccount.equals(currentAccount)) {
            this.addPage(defaultPage);
        }
    }

    public void updateAccount(String accountId, String accountName) {
        accounts.put(accountId,accountName);
        for (AccountListChangeListener listener : accountChangeListeners) {
            listener.onUpdateAccount(accountId,accountName);
        }
    }

    public void removeAccount(String accountId) {

        for (MyElectricSettings page : pages) {
            EmonDatabaseHelper.getInstance(this).deletePage(page.getId());
        }

        accounts.remove(accountId);
        for (AccountListChangeListener listener : accountChangeListeners) {
            listener.onDeleteAccount(accountId);
        }
        writeAccountList();

        if (currentAccount.equals(accountId) && !this.accounts.isEmpty()) {
            setCurrentAccount(accounts.keySet().iterator().next());
        }
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

        loadPages();
    }

    public List<MyElectricSettings> getPages() {
        return pages;
    }

    private void loadPages() {
        pages = new ArrayList<>();
        pages = EmonDatabaseHelper.getInstance(this).getPages(getCurrentAccount());

    }


    public void addPage(MyElectricSettings page) {
        pages.add(page);
        for (PageChangeListener pageChangeListener: pageChangeListeners) {
            Log.d("app","alling page change listener");
            pageChangeListener.onAddPage(page);
        }
    }

    public void removePage(MyElectricSettings page) {

        EmonDatabaseHelper.getInstance(this).deletePage(page.getId());

        for (Iterator<MyElectricSettings> iterator = pages.iterator(); iterator.hasNext(); ) {
            MyElectricSettings item = iterator.next();
            if (item.getId()  == page.getId()) {
                iterator.remove();
            }
        }

        for (PageChangeListener pageChangeListener: pageChangeListeners) {
            pageChangeListener.onDeletePage(page);
        }
    }

    public void updatePage(MyElectricSettings page) {

        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i).getId() == page.getId()) {
                pages.set(i,page);
            }
        }

        for (PageChangeListener pageChangeListener: pageChangeListeners) {
            Log.d("emon-app","Calling page change listener " + pageChangeListener);
            pageChangeListener.onUpdatePage(page);
        }
    }


}
