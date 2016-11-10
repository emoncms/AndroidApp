package org.emoncms.myapps;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.emoncms.myapps.db.EmonDatabaseHelper;
import org.emoncms.myapps.myelectric.MyElectricSettings;
import org.emoncms.myapps.settings.FeedSpinnerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for setting the feed settings for the account
 */
public class MyElectricSettingsFragment extends Fragment {
    static final String TAG = "MESETTINGSFRAGMENT";

    private String emoncmsProtocol;
    private String emoncmsURL;
    private String emoncmsAPIKEY;
    Spinner powerFeedPreference;
    Spinner kWhFeedPreference;
    EditText namePreference;
    Handler mHandler = new Handler();
    SharedPreferences sp;

    private MyElectricSettings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getArguments().getParcelable("settings");
        sp = EmonApplication.get().getSharedPreferences(EmonApplication.get().getCurrentAccount());
        loadValues();
        updateFeedList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.page_settings, container, false);
        powerFeedPreference = (Spinner) result.findViewById(R.id.powerFeedSpinner);
        kWhFeedPreference = (Spinner) result.findViewById(R.id.useFeedSpinner);
        namePreference = (EditText) result.findViewById(R.id.page_name);
        namePreference.setText(settings.getName());
        return(result);
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState) {
        super.onActivityCreated(savesInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.me_settings_title);
    }


    @Override
    public void onPause() {
        savePage();
        super.onPause();
    }

    private void savePage() {

        Log.d("settings","Saving Page");

        settings.setPowerFeedId((int)powerFeedPreference.getSelectedItemId());
        settings.setUseFeedId((int)kWhFeedPreference.getSelectedItemId());
        settings.setName(namePreference.getText().toString());

        if (settings.getId() == 0) {
            //FIXME probably move database access into EmonApplication
            Log.d("settings","Inserting");
            int id = EmonDatabaseHelper.getInstance(getActivity()).addPage(EmonApplication.get().getCurrentAccount(), settings);
            settings.setId(id);
            EmonApplication.get().addPage(settings);
        } else {
            Log.d("settings","Updating");
            EmonDatabaseHelper.getInstance(getActivity()).updatePage(settings.getId(), settings);
            EmonApplication.get().updatePage(settings);
        }
    }


    void loadValues() {
        emoncmsProtocol = sp.getBoolean("emoncms_usessl", false) ? "https://" : "http://";
        emoncmsURL = sp.getString("emoncms_url", "");
        emoncmsAPIKEY = sp.getString("emoncms_apikey", "");

        Log.d("PREF URL", emoncmsURL);
    }

    private void updateFeedList() {
        if (!emoncmsURL.equals("") && !emoncmsAPIKEY.equals(""))
            mHandler.post(runnableFeedLoader);
    }

    private Runnable runnableFeedLoader = new Runnable() {

        @Override
        public void run() {
            String url = String.format("%s%s/feed/list.json?apikey=%s", emoncmsProtocol, emoncmsURL, emoncmsAPIKEY);

            JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            List<String> powerEntryList = new ArrayList<>();
                            List<Integer> powerEntryValueList = new ArrayList<>();

                            powerEntryList.add("AUTO");
                            powerEntryValueList.add(-1);

                            List<String> kwhFeedEntryList = new ArrayList<>();
                            List<Integer> kwhFeedEntryValueList = new ArrayList<>();

                            kwhFeedEntryList.add("AUTO");
                            kwhFeedEntryValueList.add(-1);

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject row;
                                try {
                                    row = response.getJSONObject(i);

                                    int id = row.getInt("id");
                                    String name = row.getString("name");
                                    int engineType = row.getInt("engine");


                                    if (engineType == 2 ||
                                            engineType == 5 ||
                                            engineType == 6) {
                                        powerEntryList.add(name);
                                        powerEntryValueList.add(id);
                                        kwhFeedEntryList.add(name);
                                        kwhFeedEntryValueList.add(id);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            FeedSpinnerAdapter powerSpinnerAdapter = new FeedSpinnerAdapter(getActivity(),R.layout.support_simple_spinner_dropdown_item,powerEntryValueList,powerEntryList);
                            powerFeedPreference.setAdapter(powerSpinnerAdapter);
                            powerFeedPreference.setEnabled(true);

                            FeedSpinnerAdapter useSpinnerAdapter = new FeedSpinnerAdapter(getActivity(),R.layout.support_simple_spinner_dropdown_item,kwhFeedEntryValueList,kwhFeedEntryList);
                            kWhFeedPreference.setAdapter(useSpinnerAdapter);

                            kWhFeedPreference.setEnabled(true);


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
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