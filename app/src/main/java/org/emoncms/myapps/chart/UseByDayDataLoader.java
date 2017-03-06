package org.emoncms.myapps.chart;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.emoncms.myapps.HTTPClient;
import org.emoncms.myapps.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Retrieves data for the DailyUsage chart and updates the chart
 */
public class UseByDayDataLoader implements Runnable {

    private static final int INTERVAL = 86400;
    private static final String FEED_URL = "%s/feed/data.json?apikey=%s&id=%d&start=%d&end=%d&interval=86400&skipmissing=1&limitinterval=1";


    private MyElectricDataManager myElectricDataManager;
    private Context context;
    private long timeZoneOffset;
    private DailyBarChart dailyUsageBarChart;
    private int daysToDisplay;

    public UseByDayDataLoader(Context context, MyElectricDataManager myElectricDataManager, DailyBarChart dailyUsageBarChart) {
        this.myElectricDataManager = myElectricDataManager;
        this.context = context;
        this.dailyUsageBarChart = dailyUsageBarChart;
        timeZoneOffset = (long) Math.floor((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET)) * 0.001);

    }

    public void setDaysToDisplay(int days) {
        this.daysToDisplay = days;
    }

    @Override
    public void run() {
        int kWhFeedId = myElectricDataManager.getSettings().getUseFeedId();

        long end = (long) Math.floor(((Calendar.getInstance().getTimeInMillis() * 0.001) + timeZoneOffset) / INTERVAL) * INTERVAL;

        end -= timeZoneOffset;
        long start = end - (INTERVAL * daysToDisplay);

        final long chart2EndTime = end * 1000;
        final long chart2StartTime = start * 1000;

        String url = String.format(FEED_URL, myElectricDataManager.getEmonCmsUrl(), myElectricDataManager.getEmoncmsApikey(), kWhFeedId, chart2StartTime, chart2EndTime);
        Log.i("EMONCMS:URL", "mDaysofWeekRunner:" + url);

        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (url, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        List<Long> dates = new ArrayList<>();
                        List<Double> power = new ArrayList<>();
                        String dayOfWeekInitials[] = context.getResources().getStringArray(R.array.day_of_week_initials);
                        Calendar calendar = Calendar.getInstance();

                        for (int i = 0; i < response.length(); i++) {
                            JSONArray row;

                            try {
                                row = response.getJSONArray(i);
                                Long date = row.getLong(0);
                                if (date <= chart2EndTime) {
                                    dates.add(date);
                                    power.add(row.getDouble(1) *  myElectricDataManager.getSettings().getPowerScaleAsFloat());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        dailyUsageBarChart.clearData();

                        int[] chart2_colors = new int[power.size()];

                        for (int i = 0; i < power.size() - 1; i++) {
                            calendar.setTimeInMillis(dates.get(i));

                            dailyUsageBarChart.addData(dayOfWeekInitials[calendar.get(Calendar.DAY_OF_WEEK) - 1],power.get(i + 1) - power.get(i));

                            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                                chart2_colors[i] = ContextCompat.getColor(context, R.color.chartBlueDark);
                            else
                                chart2_colors[i] = ContextCompat.getColor(context, R.color.chartBlue);
                        }

                        if (power.size() > 0) {
                            double yesterdaysPowerUsage = power.get(power.size() - 1);
                            double powerToday = (myElectricDataManager.getTotalUsagekWh()) - yesterdaysPowerUsage;
                            myElectricDataManager.setUseToYesterday((float)yesterdaysPowerUsage);


                            calendar.setTimeInMillis(dates.get(dates.size() - 1));

                            dailyUsageBarChart.addData(dayOfWeekInitials[calendar.get(Calendar.DAY_OF_WEEK) - 1],powerToday);


                            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                chart2_colors[chart2_colors.length - 1] = ContextCompat.getColor(context, R.color.chartBlueDark);
                            } else {
                                chart2_colors[chart2_colors.length - 1] = ContextCompat.getColor(context, R.color.chartBlue);
                            }
                        }

                        dailyUsageBarChart.setBarColours(chart2_colors);
                        dailyUsageBarChart.refreshChart();

                       myElectricDataManager.clearMessage();
                       myElectricDataManager.loadPowerHistory(0);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        myElectricDataManager.showMessage(R.string.connection_error + error.getMessage());
                        myElectricDataManager.loadUseHistory(5000);

                    }
                });

        jsArrayRequest.setTag(myElectricDataManager.getPageTag());
        HTTPClient.getInstance(context).addToRequestQueue(jsArrayRequest);
    }
}
