package org.emoncms.myapps.chart;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.emoncms.myapps.HTTPClient;
import org.emoncms.myapps.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FeedLoader gets feedids for default feednames if a specific id not specified in preferences.
 * Then it calls loadPower
 */
public class FeedDataLoader implements Runnable {

    private static final String watt_default_feed_name = "use";
    private static final String kwh_default_feed_name = "use_kwh";
    private static final String TAG = "MyElectricMainFragment";

    private static final String FEED_URL = "%s/feed/list.json?apikey=%s";
    private Context context;
    private MyElectricDataManager myElectricDataManager;

    public FeedDataLoader(Context context, MyElectricDataManager myElectricDataManager) {
        this.context = context;
        this.myElectricDataManager = myElectricDataManager;
    }

    @Override
    public void run() {
        String url = String.format(context.getResources().getConfiguration().locale, FEED_URL, myElectricDataManager.getEmonCmsUrl(), myElectricDataManager.getEmoncmsApikey());
        Log.i("EMONCMS:URL", "mGetFeedsRunner:" + url);

        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        int wattFeedId = -1;
                        int kWhFeelId = -1;

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject row;

                            try {
                                row = response.getJSONObject(i);

                                if (watt_default_feed_name.equals(row.getString("name"))) {
                                    wattFeedId = row.getInt("id");
                                }

                                if (kwh_default_feed_name.equals(row.getString("name"))) {
                                    kWhFeelId = row.getInt("id");
                                }

                                if (wattFeedId >= 0 && kWhFeelId >= 0) {
                                    myElectricDataManager.setFeedIds(wattFeedId, kWhFeelId);
                                    myElectricDataManager.loadPowerNow(0);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        myElectricDataManager.showMessage(R.string.me_not_configured_text);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        myElectricDataManager.showMessage(R.string.feed_download_error_message);
                        myElectricDataManager.loadFeeds(5000);
                    }
                });
        jsArrayRequest.setTag(TAG);
        HTTPClient.getInstance(context).addToRequestQueue(jsArrayRequest);
    }
}
