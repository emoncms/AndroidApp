package org.emoncms.myapps;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MySolarMainFragement extends Fragment
{
    static final String TAG = "MySolarMainFragement";
    static String emoncmsURL;
    static String emoncmsAPIKEY;
    static String emoncmsProtocol;
    static String powerCostSymbol;
    static float powerCost = 0;
    static float powerScale;
    float dpWidth = 0;

    TextView txtPower;
    TextView txtUseToday;
    TextView txtDebug;
//    LineChart chart1;
//    BarChart chart2;
    boolean blnDebugOnShow = false;
    Handler mHandler = new Handler();
//    Float toYesterdayPowerUsagekWh = 0F;
    Float totalPowerUsage = 0F;
//    int powerGraphLength = -6;
//    boolean resetPowerGraph = false;
//    Button chart1_3h;
//    Button chart1_6h;
//    Button chart1_D;
//    Button chart1_W;
//    Button chart1_M;
//    SwitchCompat costSwitch;

    int wattFeedId = 0;
    int kWhFeelId = 0;
    long timezone = 0;

//    int dailyChartUpdateInterval = 60000;
//    long nextDailyChartUpdate = 0;

    float powerNow = 0;
    //float powerTodaykWh = 0;

    boolean blnShowCost = false;

    private Runnable mGetFeedsRunner = new Runnable()
    {
        @Override
        public void run()
        {
            String url = String.format("%s%s/feed/list.json?apikey=%s", emoncmsProtocol, emoncmsURL, emoncmsAPIKEY);
            Log.i("EMONCMS:URL", "mGetFeedsRunner:" + url);
            JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>()
                    {

                        @Override
                        public void onResponse(JSONArray response)
                        {
                            for (int i = 0; i < response.length(); i++)
                            {
                                JSONObject row;
                                try
                                {
                                    row = response.getJSONObject(i);
                                    int id = row.getInt("id");
                                    if (id == wattFeedId)
                                    {
                                        powerNow = Float.parseFloat(row.getString("value"));
                                        updateTextFields();
                                    }
                                    else if (id == kWhFeelId)
                                    {
                                        totalPowerUsage = ((Double) row.getDouble("value")).floatValue() * powerScale;
                                    }

                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            if (blnDebugOnShow)
                            {
                                blnDebugOnShow = false;
                                txtDebug.setVisibility(View.GONE);
                            }
/*
                            if (Calendar.getInstance().getTimeInMillis() > nextDailyChartUpdate)
                            {
                                nextDailyChartUpdate = Calendar.getInstance().getTimeInMillis() + dailyChartUpdateInterval;
                                mHandler.post(mDaysofWeekRunner);
                            }
                            else
                            {
                                mHandler.post(mGetPowerHistoryRunner);
                            }

*/
                            mHandler.postDelayed(mGetFeedsRunner, 10000);
                        }
                    }, new Response.ErrorListener()
                    {

                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            blnDebugOnShow = true;
                            txtDebug.setVisibility(View.VISIBLE);
                            mHandler.postDelayed(mGetFeedsRunner, 5000);
                        }
                    });

            jsArrayRequest.setTag(TAG);
            HTTPClient.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
        }
    };

    private void updateTextFields()
    {
        if (blnShowCost)
        {
            txtPower.setText(String.format("%s%.2f/h", powerCostSymbol, (powerNow*0.001)*powerCost));
            //txtUseToday.setText(String.format("%s%.2f", powerCostSymbol, powerTodaykWh*powerCost));
        }
        else
        {
            txtPower.setText(String.format("%.0fW", powerNow));
            //txtUseToday.setText(String.format("%.1fkWh", powerTodaykWh));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        emoncmsURL = SP.getString("emoncms_url", "emoncms.org");
        emoncmsAPIKEY = SP.getString("emoncms_apikey", null);
        emoncmsProtocol = SP.getBoolean("emoncms_usessl", false) ? "https://" : "http://";

        wattFeedId = Integer.valueOf(SP.getString("myelectric_power_feed", "-1"));
        /*
        kWhFeelId = Integer.valueOf(SP.getString("myelectric_kwh_feed", "-1"));
        powerScale = Integer.valueOf(SP.getString("myelectric_escale", "0")) == 0 ? 1.0F : 0.001F;
        powerCost = Float.parseFloat(SP.getString("myelectric_unit_cost", "0"));
        powerCostSymbol = SP.getString("myelectric_cost_symbol", "£");
        try
        {
            if (powerCostSymbol.equals("0"))
                powerCostSymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        }
        catch (IllegalArgumentException  e)
        {
            powerCostSymbol = "£";
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.ms_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState)
    {
        super.onActivityCreated(savesInstanceState);

        View view = getView();
        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.ms_title);


        txtPower = (TextView) view.findViewById(R.id.txtMSPower);
        txtDebug = (TextView) view.findViewById(R.id.txtDebug);
        /*
        txtUseToday = (TextView) view.findViewById(R.id.txtUseToday);
        chart1_3h = (Button) view.findViewById(R.id.btnChart1_3H);
        chart1_6h = (Button) view.findViewById(R.id.btnChart1_6H);
        chart1_D = (Button) view.findViewById(R.id.btnChart1_D);
        chart1_W = (Button) view.findViewById(R.id.btnChart1_W);
        chart1_M = (Button) view.findViewById(R.id.btnChart1_M);

        chart1_3h.setOnClickListener(buttonListener);
        chart1_6h.setOnClickListener(buttonListener);
        chart1_D.setOnClickListener(buttonListener);
        chart1_W.setOnClickListener(buttonListener);
        chart1_M.setOnClickListener(buttonListener);

        chart1 = (LineChart) view.findViewById(R.id.chart1);
        chart1.setDrawGridBackground(false);
        chart1.getLegend().setEnabled(false);
        chart1.getAxisRight().setEnabled(false);
        chart1.setDescription("");
        chart1.setNoDataText("");
        chart1.setHardwareAccelerationEnabled(true);
        chart1.setData(new LineData());

        YAxis yAxis = chart1.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setDrawTopYLabelEntry(false);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
        yAxis.setTextSize(getResources().getInteger(R.integer.chartDateTextSize));
        yAxis.setValueFormatter(new IntegerYAxisValueFormatter());

        XAxis xAxis = chart1.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
        xAxis.setValueFormatter(new HoursMinutesXAxisValueFormatter());
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setTextSize(getResources().getInteger(R.integer.chartDateTextSize));

        chart2 = (BarChart) view.findViewById(R.id.chart2);
        chart2.setDrawGridBackground(false);
        chart2.getLegend().setEnabled(false);
        chart2.getAxisLeft().setEnabled(false);
        chart2.getAxisRight().setEnabled(false);
        chart2.setHardwareAccelerationEnabled(true);
        chart2.setDrawValueAboveBar(false);
        chart2.setDescription("");
        chart2.setNoDataText("");
        chart2.setTouchEnabled(false);

        xAxis = chart2.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
        xAxis.setTextSize(getResources().getInteger(R.integer.chartValueTextSize));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            chart1.setHardwareAccelerationEnabled(false);
            chart2.setHardwareAccelerationEnabled(false);
        }
        */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ms_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        //costSwitch = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.cost_switch));
        //costSwitch.setOnCheckedChangeListener(checkedChangedListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //((MainActivity) getActivity()).showFragment(MainActivity.MyAppViews.MySolarSettingsView);
            return true;
        }
        else if (id == R.id.full_screen) {
            boolean fullScreen = ((MainActivity) getActivity()).setFullScreen();
            if (fullScreen)
                item.setIcon(R.drawable.ic_fullscreen_exit_white_24dp);
            else
                item.setIcon(R.drawable.ic_fullscreen_white_24dp);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        timezone = (long) Math.floor((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET))*0.001);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (wattFeedId < 0 || kWhFeelId < 0)
        {
            txtDebug.setText(getResources().getString(R.string.me_not_configured_text));
            txtDebug.setVisibility(View.VISIBLE);
        }
        else
            mHandler.post(mGetFeedsRunner);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        HTTPClient.getInstance(getActivity()).cancellAll(TAG);
        mHandler.removeCallbacksAndMessages(null);
    }
}
