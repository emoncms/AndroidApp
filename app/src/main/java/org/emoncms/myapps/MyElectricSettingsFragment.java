package org.emoncms.myapps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyElectricSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = "MESETTINGSFRAGMENT";

    private String emoncmsProtocol;
    private String emoncmsURL;
    private String emoncmsAPIKEY;
    ListPreference powerFeedPreference;
    ListPreference kWhFeedPreference;
    Handler mHandler = new Handler();
    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.me_preferences);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        loadValues();
        powerFeedPreference = (ListPreference) this.findPreference("myelectric_power_feed");
        kWhFeedPreference = (ListPreference) this.findPreference("myelectric_kwh_feed");
        updateFeedList();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.me_settings_title);
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
                                kWhFeedPreference.setEntries(entries);
                                kWhFeedPreference.setEntryValues(entryValues);
                                kWhFeedPreference.setEnabled(true);
                            }
                        }
                    }, new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            powerFeedPreference.setEnabled(false);
                            kWhFeedPreference.setEnabled(false);
                        }
                    });
            jsArrayRequest.setTag(TAG);
            HTTPClient.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
        }
    };
}