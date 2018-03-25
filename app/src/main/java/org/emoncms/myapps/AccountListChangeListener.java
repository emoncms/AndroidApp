package org.emoncms.myapps;

/**
 * Interface for things which want to know about changes to accounts
 */
public interface AccountListChangeListener {

    void onAddAccount(String id, String name);

    void onDeleteAccount(String id);

    void onUpdateAccount(String id, String name);
}
