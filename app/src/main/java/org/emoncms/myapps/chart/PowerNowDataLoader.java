package org.emoncms.myapps.chart;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.emoncms.myapps.HTTPClient;
import org.emoncms.myapps.R;
import org.emoncms.myapps.Utils;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Loads current value for each feed
 */
public class PowerNowDataLoader implements Runnable {

    private static final String FEED_URL = "%s/feed/fetch.json?apikey=%s&ids=%d,%d";

    private int wattFeedId;
    private int kWhFeedId;
    private Context context;
    private MyElectricDataManager myElectricDataManager;

    private float powerScale;

    public PowerNowDataLoader(Context context, MyElectricDataManager myElectricDataManager, int wattFeedId, int kWhFeedId, float powerScale) {
        this.wattFeedId = wattFeedId;
        this.kWhFeedId = kWhFeedId;
        this.context = context;
        this.myElectricDataManager = myElectricDataManager;
        this.powerScale = powerScale;
    }

    @Override
    public void run() {
        String url = String.format(context.getResources().getConfiguration().locale, FEED_URL, myElectricDataManager.getEmonCmsUrl() , myElectricDataManager.getEmoncmsApikey(), wattFeedId, kWhFeedId);
        Log.i("EMONCMS:URL", "mGetPowerRunner:" + url);
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String watt_value = "";
                        String kwh_value = "";

                        if (response.length() == 2) {
                            try {
                                watt_value = response.getString(0);
                                kwh_value = response.getString(1);

                                float powerNow = 0;
                                float totalPowerUsage = 0;

                                if (Utils.isNumeric(watt_value)) {
                                    powerNow = Float.parseFloat(watt_value);
                                }

                                if (Utils.isNumeric(kwh_value)) {
                                    totalPowerUsage = Float.parseFloat(kwh_value) * powerScale;
                                }

                                myElectricDataManager.setCurrentValues(powerNow, totalPowerUsage);
                                myElectricDataManager.clearMessage();
                            } catch (JSONException e) {
                                myElectricDataManager.showMessage(e.getMessage());
                            }
                        } else {
                            myElectricDataManager.showMessage(R.string.invalid_number_of_responses);
                        }

                        if (!Utils.isNumeric(watt_value)) {
                            myElectricDataManager.showMessage(R.string.invalid_watt_feedid);
                        } else if (!Utils.isNumeric(kwh_value)) {
                            myElectricDataManager.showMessage(R.string.invalid_kwh_feedid);
                        } else {
                            if (!myElectricDataManager.loadUseHistory(0)) {
                                myElectricDataManager.loadPowerHistory(0);
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        myElectricDataManager.showMessage(R.string.connection_error + error.getMessage());
                        myElectricDataManager.loadPowerNow(5000);
                    }
                });

        jsArrayRequest.setTag(myElectricDataManager.getPageTag());
        HTTPClient.getInstance(context).addToRequestQueue(jsArrayRequest);
    }
}
