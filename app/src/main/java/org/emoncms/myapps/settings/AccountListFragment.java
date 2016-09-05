package org.emoncms.myapps.settings;


import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.emoncms.myapps.MainActivity;
import org.emoncms.myapps.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment shows a list of the API keys which have been added.
 * Clicking on account goes to prefs for that account.
 *
 */
public class AccountListFragment extends ListFragment {

    private List<Account> accounts;
    private AccountAdaptor accountAdaptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accounts = new ArrayList<>();
        accounts.add(new Account("1","Account 1", "url","apikey"));
        accounts.add(new Account("2","Account 2", "url2","apikey2"));
        accountAdaptor = new AccountAdaptor(getActivity(),accounts);
        setListAdapter(accountAdaptor);


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class AccountAdaptor extends ArrayAdapter<Account> {
        AccountAdaptor(Context context, List<Account> objects) {
            super(context, R.layout.account_item, objects);
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.account_item, parent, false);
            }

            TextView url=(TextView)convertView.findViewById(R.id.url);
            url.setText(getItem(position).url);

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
        String url;
        String apiKey;

        public Account(String id, String name, String url, String apiKey) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.apiKey = apiKey;
        }
    }

}