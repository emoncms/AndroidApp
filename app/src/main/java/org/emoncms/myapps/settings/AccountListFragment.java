package org.emoncms.myapps.settings;


import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.emoncms.myapps.EmonApplication;
import org.emoncms.myapps.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment shows a list of the Accounts which have been added.
 * Clicking on account goes to prefs for that account.
 *
 */
public class AccountListFragment extends ListFragment {

    private List<Account> accounts;
    private AccountAdaptor accountAdaptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadCurrentAccounts();


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (EmonApplication.get().getAccounts().isEmpty()) {
            Snackbar snackbar = Snackbar.make(view, R.string.settings_add_one_account, Snackbar.LENGTH_INDEFINITE);
            View snackbar_view = snackbar.getView();
            snackbar_view.setBackgroundColor(Color.GRAY);
            snackbar.show();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadCurrentAccounts() {
        accounts = new ArrayList<>();

        for (String accountId : EmonApplication.get().getAccounts().keySet()) {
            accounts.add(new Account(accountId,EmonApplication.get().getAccounts().get(accountId)));
        }

        accountAdaptor = new AccountAdaptor(getActivity(),accounts);
        setListAdapter(accountAdaptor);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentAccounts();
    }

    private class AccountAdaptor extends ArrayAdapter<Account> {
        AccountAdaptor(Context context, List<Account> objects) {
            super(context, R.layout.menu_item_account, objects);
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item_account, parent, false);
            }

            TextView url=(TextView)convertView.findViewById(R.id.name);
            url.setText(getItem(position).name);

            return(convertView);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {

        super.onListItemClick(l, v, pos, id);

        Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
        intent.putExtra("account", accounts.get(pos).id);
        startActivity(intent);


    }


    class Account {
        String id;
        String name;


        public Account(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

}