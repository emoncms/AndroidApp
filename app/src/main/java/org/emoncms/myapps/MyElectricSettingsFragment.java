package org.emoncms.myapps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for setting the feed settings for the account
 */
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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.me_preferences);
        String prefsFileName = EmonApplication.getAccountSettingsFile(EmonApplication.get().getCurrentAccount());
        getPreferenceManager().setSharedPreferencesName(prefsFileName);
        sp = getActivity().getSharedPreferences(EmonApplication.getAccountSettingsFile(EmonApplication.get().getCurrentAccount()), Context.MODE_PRIVATE);

        loadValues();
        powerFeedPreference = (ListPreference) this.findPreference("myelectric_power_feed");
        kWhFeedPreference = (ListPreference) this.findPreference("myelectric_kwh_feed");
        updateFeedList();
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState) {
        super.onActivityCreated(savesInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.me_settings_title);
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

                            List<String> powerEntryList = new ArrayList<>();
                            List<String> powerEntryValueList = new ArrayList<>();

                            powerEntryList.add("AUTO");
                            powerEntryValueList.add("-1");

                            List<String> kwhFeedEntryList = new ArrayList<>();
                            List<String> kwhFeedEntryValueList = new ArrayList<>();

                            kwhFeedEntryList.add("AUTO");
                            kwhFeedEntryValueList.add("-1");

                            for (int i = 0; i < response.length(); i++)
                            {
                                JSONObject row;
                                try
                                {
                                    row = response.getJSONObject(i);

                                    String id = row.getString("id");
                                    String name = row.getString("name");
                                    int engineType = row.getInt("engine");


                                    if (engineType == 2 ||
                                        engineType == 5 ||
                                        engineType == 6)
                                    {
                                        powerEntryList.add(name);
                                        powerEntryValueList.add(id);
                                        kwhFeedEntryList.add(name);
                                        kwhFeedEntryValueList.add(id);
                                    }
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            CharSequence powerEntries[] = powerEntryList.toArray(new CharSequence[powerEntryList.size()]);
                            CharSequence powerEntryValues[] = powerEntryValueList.toArray(new CharSequence[powerEntryValueList.size()]);
                            CharSequence kwhFeedEntries[] = kwhFeedEntryList.toArray(new CharSequence[kwhFeedEntryList.size()]);
                            CharSequence kwhFeedEntryValues[] = kwhFeedEntryValueList.toArray(new CharSequence[kwhFeedEntryValueList.size()]);

                            if (powerEntries.length > 1 && powerEntryValues.length > 1)
                            {
                                powerFeedPreference.setEntries(powerEntries);
                                powerFeedPreference.setEntryValues(powerEntryValues);
                                powerFeedPreference.setEnabled(true);
                            }
                            if (kwhFeedEntries.length > 1 && kwhFeedEntryValues.length > 1)
                            {
                                kWhFeedPreference.setEntries(kwhFeedEntries);
                                kWhFeedPreference.setEntryValues(kwhFeedEntryValues);
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