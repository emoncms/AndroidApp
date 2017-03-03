package org.emoncms.myapps.chart;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.emoncms.myapps.HTTPClient;
import org.emoncms.myapps.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

/**
 * Runnable which loads data for the PowerChart
 */
public class PowerChartDataLoader implements Runnable {

    private static final String FEED_URL = "%s/feed/data.json?apikey=%s&id=%d&start=%d&end=%d&interval=%d&skipmissing=1&limitinterval=1";
    private Context context;
    private PowerChart powerChart;
    private MyElectricDataManager myElectricDataManager;
    //private int wattFeedId;

    public PowerChartDataLoader(PowerChart powerChart, Context context, MyElectricDataManager myElectricDataManager) {
        this.powerChart = powerChart;
        this.context = context;
        this.myElectricDataManager = myElectricDataManager;
       // this.wattFeedId = wattFeedId;
    }

    @Override
    public void run() {

        int wattFeedId = myElectricDataManager.getSettings().getPowerFeedId();

        final long lastEntry;

        if (powerChart.requiresReset()) {
            powerChart.clearData();
        }

        if (powerChart.getLabels().size() > 0) {
            lastEntry = Long.parseLong(powerChart.getLabels().get(powerChart.getLabels().size() - 1));
        } else {
            lastEntry = 0;
        }

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR, powerChart.getChartLength());
        long startTime = cal.getTimeInMillis();

        int npoints = 1500;
        final int interval = Math.round(((endTime - startTime) / 1000) / npoints);

        if (lastEntry > startTime)
            startTime = lastEntry;

        String url = String.format(context.getResources().getConfiguration().locale, FEED_URL, myElectricDataManager.getEmonCmsUrl(), myElectricDataManager.getEmoncmsApikey(), wattFeedId, startTime, endTime, interval);
        Log.i("EMONCMS:URL", "mGetPowerHistoryRunner:" + url);
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {
                    JSONArray row;
                    try {
                        row = response.getJSONArray(i);
                        long time = Long.parseLong(row.getString(0));

                        if (lastEntry == 0) {
                            powerChart.addData(row.getString(0), row.getDouble(1));
                        } else if (time >= (lastEntry + (interval * 1000))) {
                            powerChart.removeFirstPoint();
                            powerChart.addData(row.getString(0), row.getDouble(1));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                powerChart.refreshChart();
                myElectricDataManager.clearMessage();
                myElectricDataManager.loadPowerNow(10000);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                myElectricDataManager.showMessage(R.string.connection_error + error.getMessage());
                myElectricDataManager.loadPowerHistory(5000);
            }
        });

        jsArrayRequest.setTag(myElectricDataManager.getPageTag());

        if (endTime > lastEntry + (interval * 1000)) {
            HTTPClient.getInstance(context).addToRequestQueue(jsArrayRequest);
        } else {
            myElectricDataManager.loadPowerNow(10000);
        }
    }

}
