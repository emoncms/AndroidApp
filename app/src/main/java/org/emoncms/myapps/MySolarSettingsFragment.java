package org.emoncms.myapps;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySolarSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = "MSSETTINGSFRAGMENT";

    private String emoncmsProtocol;
    private String emoncmsURL;
    private String emoncmsAPIKEY;
    MultiSelectListPreference powerFeedPreference;
    //ListPreference kWhFeedPreference;
    Handler mHandler = new Handler();
    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.ms_preferences);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        loadValues();
        powerFeedPreference = (MultiSelectListPreference) this.findPreference("mysolar_power_feeds");
        //kWhFeedPreference = (ListPreference) this.findPreference("mysolar_kwh_feed");
        updateFeedList();
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState) {
        super.onActivityCreated(savesInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.ms_settings_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("emoncms_url") || key.equals("emoncms_apikey") || key.equals("emoncms_usessl"))
        {
            loadValues();
            updateFeedList();
        }
    }

    void loadValues() {
        emoncmsProtocol = sp.getBoolean("emoncms_usessl", false) ? "https://" : "http://";
        emoncmsURL = sp.getString("emoncms_url", "");
        emoncmsAPIKEY = sp.getString("emoncms_apikey", "");
    }

    private void updateFeedList() {
        if (!emoncmsURL.equals("") && !emoncmsAPIKEY.equals(""))
            mHandler.post(mRunnable);
    }

    private Runnable mRunnable = new Runnable()
    {

        @Override
        public void run()
        {
            String url = String.format("%s%s/feed/list.json?apikey=%s", emoncmsProtocol, emoncmsURL, emoncmsAPIKEY);

            JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>()
                    {
                        @Override
                        public void onResponse(JSONArray response)
                        {
                            CharSequence entries[] = new String[response.length()];
                            CharSequence entryValues[] = new String[response.length()];
                            for (int i = 0; i < response.length(); i++)
                            {
                                JSONObject row;
                                try
                                {
                                    row = response.getJSONObject(i);

                                    String id = row.getString("id");
                                    String name = row.getString("name");
                                    entries[i] = name;
                                    entryValues[i] = id;
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            if (entries.length > 0 && entryValues.length > 0)
                            {
                                powerFeedPreference.setEntries(entries);
                                powerFeedPreference.setEntryValues(entryValues);
                                powerFeedPreference.setEnabled(true);
                                //kWhFeedPreference.setEntries(entries);
                                //kWhFeedPreference.setEntryValues(entryValues);
                                //kWhFeedPreference.setEnabled(true);
                            }
                        }
                    }, new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            powerFeedPreference.setEnabled(false);
                            //kWhFeedPreference.setEnabled(false);

                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.feed_download_error_message)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
            jsArrayRequest.setTag(TAG);
            HTTPClient.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
        }
    };
}