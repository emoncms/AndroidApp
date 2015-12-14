package org.emoncms.myapps;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyElectricMainFragment extends Fragment
{
    static final String TAG = "emoncms";
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
    LineChart chart1;
    BarChart chart2;
    boolean blnDebugOnShow = false;
    Handler mHandler = new Handler();
    Float yesterdaysPowerUsage = 0F;
    Float totalPowerUsage = 0F;
    int powerGraphLength = -6;
    boolean resetPowerGraph = false;
    Button chart1_3h;
    Button chart1_6h;
    Button chart1_D;
    Button chart1_W;
    Button chart1_M;
    SwitchCompat costSwitch;

    int wattFeedId = 0;
    int kWhFeelId = 0;

    int dailyChartUpdateInterval = 60000;
    long nextDailyChartUpdate = 0;

    float powerNow = 0;
    float powerToday = 0;

    boolean blnShowCost = false;

    private Runnable mGetFeedsRunner = new Runnable()
    {
        @Override
        public void run()
        {
            String url = String.format("%s%s/feed/list.json?apikey=%s", emoncmsProtocol, emoncmsURL, emoncmsAPIKEY);
            Log.i("EMONCMS:URL", "mGetFeedsRunner:"+url);
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

                            if (Calendar.getInstance().getTimeInMillis() > nextDailyChartUpdate)
                            {
                                nextDailyChartUpdate = Calendar.getInstance().getTimeInMillis() + dailyChartUpdateInterval;
                                mHandler.post(mDaysofWeekRunner);
                            }
                            else
                            {
                                mHandler.post(mGetPowerHistoryRunner);
                            }
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


    private Runnable mDaysofWeekRunner = new Runnable()
    {
        @Override
        public void run()
        {
            int daysToDisplay =  Math.round(dpWidth / 50)-1;
            int interval = 86400;
            Date now = new Date();
            int timezone = (((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET)) / 60000)/-60)*3600;
            long time_now = (long) (Math.floor(now.getTime() * 0.001));
            long end = (long) (Math.floor((time_now+timezone)/interval)*interval)-timezone;
            long start = end - interval * daysToDisplay;

            final long chart2EndTime = end * 1000;
            final long chart2StartTime = start * 1000;

            String url = String.format("%s%s/feed/data.json?id=%d&start=%d&end=%d&interval=86400&skipmissing=1&limitinterval=1&apikey=%s", emoncmsProtocol, emoncmsURL, kWhFeelId, chart2StartTime, chart2EndTime, emoncmsAPIKEY);
            Log.i("EMONCMS:URL", "mDaysofWeekRunner:"+url);
            JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                    (url, new Response.Listener<JSONArray>()
                    {

                        @Override
                        public void onResponse(JSONArray response)
                        {
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            ArrayList<String> labels = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("E");

                            List<Long> dates = new ArrayList<>();
                            List<Float> power = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++)
                            {
                                JSONArray row;

                                try
                                {
                                    row = response.getJSONArray(i);
                                    Long date = row.getLong(0);
                                    if (date <= chart2EndTime)
                                    {
                                        dates.add(date);
                                        power.add(((Double) row.getDouble(1)).floatValue() * powerScale);
                                    }
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            for (int i = 0; i < power.size() - 1; i++)
                            {
                                labels.add(sdf.format(new Date(dates.get(i))).substring(0, 1));
                                float graph_value = power.get(i + 1) - power.get(i);
                                if (graph_value < 0) graph_value = 0;
                                entries.add(new BarEntry(graph_value, i));
                            }

                            if (power.size() > 0)
                            {
                                yesterdaysPowerUsage = power.get(power.size() - 1);
                                labels.add(sdf.format(new Date(dates.get(dates.size() - 1))).substring(0, 1));
                                entries.add(new BarEntry(0, entries.size()));
                            }

                            try
                            {
                                BarDataSet dataset = new BarDataSet(entries, "kWh");
                                dataset.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                                dataset.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                                dataset.setValueTextSize(getResources().getInteger(R.integer.chartValueTextSize));
                                dataset.setValueFormatter(new Chart2ValueFormatter());
                                BarData barData = new BarData(labels, dataset);
                                chart2.setData(barData);

                                if (yesterdaysPowerUsage > 0)
                                {
                                    powerToday = totalPowerUsage - yesterdaysPowerUsage;
                                    updateTextFields();
                                    Entry e = dataset.getEntryForXIndex(dataset.getEntryCount() - 1);
                                    e.setVal(powerToday);
                                }

                                chart2.notifyDataSetChanged();
                                chart2.invalidate();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            if (blnDebugOnShow)
                            {
                                blnDebugOnShow = false;
                                txtDebug.setVisibility(View.GONE);
                            }

                            mHandler.post(mGetPowerHistoryRunner);
                        }
                    }, new Response.ErrorListener()
                    {

                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            blnDebugOnShow = true;
                            txtDebug.setVisibility(View.VISIBLE);
                            mHandler.postDelayed(mDaysofWeekRunner, 5000);
                        }
                    });

            jsArrayRequest.setTag(TAG);
            HTTPClient.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
        }
    };

    private Runnable mGetPowerHistoryRunner = new Runnable()
    {
        @Override
        public void run()
        {
            Calendar cal = Calendar.getInstance();
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, powerGraphLength);

            long startTime = cal.getTimeInMillis();
            int npoints = 1500;
            final int graph_interval = Math.round(((endTime - startTime) / npoints) / 1000);

            String url = String.format("%s%s/feed/data.json?id=%d&start=%d&end=%d&interval=%d&skipmissing=1&limitinterval=1&apikey=%s", emoncmsProtocol, emoncmsURL, wattFeedId, startTime, endTime, graph_interval, emoncmsAPIKEY);
            Log.i("EMONCMS:URL", "mGetPowerHistoryRunner:"+url);
            JsonArrayRequest jsArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    LineData data = chart1.getData();
                    LineDataSet set = data.getDataSetByIndex(0);

                    long lastEntry = 0;

                    if (set == null)
                    {
                        set = new LineDataSet(null, "watts");
                        set.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                        set.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                        set.setValueTextSize(ContextCompat.getColor(getActivity(), R.integer.chartValueTextSize));
                        set.setDrawCircles(false);
                        set.setDrawFilled(true);
                        set.setFillColor(ContextCompat.getColor(getActivity(), R.color.chartBlue));
                        set.setDrawValues(false);
                        set.setHighlightEnabled(false);
                        data.addDataSet(set);
                    }

                    if (resetPowerGraph)
                    {
                        data.getXVals().clear();
                        set.clear();
                    }

                    if (data.getXValCount() > 0)
                    {
                        lastEntry = Long.parseLong(data.getXVals().get(data.getXValCount()-1));
                    }

                    for (int i = 0; i < response.length(); i++)
                    {
                        JSONArray row;
                        try
                        {
                            row = response.getJSONArray(i);
                            long time = Long.parseLong(row.getString(0));

                            if (lastEntry == 0)
                            {
                                data.addXValue(row.getString(0));
                                data.addEntry(new Entry(Float.parseFloat(row.getString(1)), set.getEntryCount()), 0);
                            }
                            else if (time > (lastEntry+(graph_interval*1000)))
                            {
                                Entry e = set.getEntryForXIndex(0);
                                Boolean removeEntry = data.removeEntry(e, 0);

                                if (removeEntry)
                                {
                                    data.removeXValue(0);
                                    for (Entry entry : set.getYVals()) {
                                        entry.setXIndex(entry.getXIndex() - 1);
                                    }
                                }

                                data.addXValue(row.getString(0));
                                data.addEntry(new Entry(Float.parseFloat(row.getString(1)), set.getEntryCount()), 0);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    chart1.notifyDataSetChanged();
                    chart1.invalidate();
                    resetPowerGraph = false;

                    if (blnDebugOnShow)
                    {
                        blnDebugOnShow = false;
                        txtDebug.setVisibility(View.GONE);
                    }

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
            txtUseToday.setText(String.format("%s%.2f", powerCostSymbol, powerToday*powerCost));
        }
        else
        {
            txtPower.setText(String.format("%.0fW", powerNow));
            txtUseToday.setText(String.format("%.1fkWh", powerToday));
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
        kWhFeelId = Integer.valueOf(SP.getString("myelectric_kwh_feed", "-1"));
        powerScale = Integer.valueOf(SP.getString("myelectric_escale", "0")) == 0 ? 1.0F : 0.001F;
        powerCost = Float.parseFloat(SP.getString("myelectric_unit_cost", "0"));
        powerCostSymbol = SP.getString("myelectric_cost_symbol", "£");
        if (powerCostSymbol.equals("0")) powerCostSymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.me_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savesInstanceState)
    {
        super.onActivityCreated(savesInstanceState);

        View view = getView();
        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.me_title);

        txtPower = (TextView) view.findViewById(R.id.txtPower);
        txtUseToday = (TextView) view.findViewById(R.id.txtUseToday);
        txtDebug = (TextView) view.findViewById(R.id.txtDebug);
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
        yAxis.setValueFormatter(new Chart1YAxisValueFormatter());

        XAxis xAxis = chart1.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
        xAxis.setValueFormatter(new Chart1XAxisValueFormatter());
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.me_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        costSwitch = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.cost_switch));
        costSwitch.setOnCheckedChangeListener(checkedChangedListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MyElectricSettingsFragment(), getResources().getString(R.string.tag_me_settings_fragment))
                    .commit();
            return true;
        }
        else if (id == R.id.full_screen) {
            ((MainActivity) getActivity()).setFullScreen();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();

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

    private CompoundButton.OnCheckedChangeListener checkedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            blnShowCost = isChecked;
            updateTextFields();
        }
    };

    private OnClickListener buttonListener = new OnClickListener() {
        public void onClick(View v) {

            switch (v.getId())
            {
                case R.id.btnChart1_3H:
                    powerGraphLength = -3;
                    resetPowerGraph = true;
                    break;
                case R.id.btnChart1_6H:
                    powerGraphLength = -6;
                    resetPowerGraph = true;
                    break;
                case R.id.btnChart1_D:
                    powerGraphLength = -24;
                    resetPowerGraph = true;
                    break;
                case R.id.btnChart1_W:
                    powerGraphLength = -168; // 7 * 24
                    resetPowerGraph = true;
                    break;
                case R.id.btnChart1_M: // 4 Weeks
                    powerGraphLength = -720; // 30 * 24
                    resetPowerGraph = true;
                    break;
            }

            HTTPClient.getInstance(getActivity()).cancellAll(TAG);
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(mGetPowerHistoryRunner);
        }
    };

    public class Chart1XAxisValueFormatter implements XAxisValueFormatter
    {
        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler)
        {
            DateFormat df = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(original));
            return (df.format(cal.getTime()));
        }
    }

    public class Chart1YAxisValueFormatter implements YAxisValueFormatter
    {
        private DecimalFormat mFormat;

        public Chart1YAxisValueFormatter () {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return mFormat.format(value);
        }
    }

    public class Chart2ValueFormatter implements ValueFormatter
    {

        private DecimalFormat mFormat;

        public Chart2ValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }
}