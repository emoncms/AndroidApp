package org.emoncms.myapps;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import org.emoncms.myapps.chart.DailyBarChart;
import org.emoncms.myapps.chart.FeedDataLoader;
import org.emoncms.myapps.chart.MyElectricDataManager;
import org.emoncms.myapps.chart.PowerChart;
import org.emoncms.myapps.chart.PowerChartDataLoader;
import org.emoncms.myapps.chart.PowerNowDataLoader;
import org.emoncms.myapps.chart.UseByDayDataLoader;
import org.emoncms.myapps.myelectric.MyElectricSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Handles UI components for MyElectric
 */
public class MyElectricMainFragment extends Fragment implements MyElectricDataManager {

    static final int dailyChartUpdateInterval = 60000;

    private String emonCmsUrl;
    private String emonCmsApiKey;

    private MyElectricSettings myElectricSettings;

    private PowerChart powerChart;
    private DailyBarChart dailyUsageBarChart;
    private int daysToDisplay;

    private TextView txtPower;
    private TextView txtUseToday;

    private SwitchCompat costSwitch;
    private Handler mHandler = new Handler();

    long timezone = 0;

    double yesterdaysPowerUsage;
    float totalPowerUsage;
    long nextDailyChartUpdate = 0;

    double powerNow = 0;
    double powerToday = 0;

    private boolean blnShowCost = false;

    private View rootView;
    private Snackbar snackbar;

    private FeedDataLoader mGetFeedsRunner;
    private PowerChartDataLoader mGetPowerHistoryRunner;
    private Runnable mGetPowerRunner;
    private UseByDayDataLoader mGetUsageByDayRunner;

    private boolean isMessage = false;
    private boolean isVisibleInPager = false;

    public static MyElectricMainFragment newInstance(MyElectricSettings settings) {
        MyElectricMainFragment yf = new MyElectricMainFragment();
        Log.d("emon-me", "Making new instance " + settings);

        Bundle args = new Bundle();
        args.putParcelable("settings", settings);
        yf.setArguments(args);
        return yf;
    }


    private void updateTextFields() {
        if (getActivity() != null) {
            if (blnShowCost) {
                txtPower.setText(String.format(getActivity().getResources().getConfiguration().locale, "%s%.2f/h", myElectricSettings.getCostSymbol(), (powerNow * 0.001) * myElectricSettings.getUnitCost()));
                txtUseToday.setText(String.format(getActivity().getResources().getConfiguration().locale, "%s%.2f", myElectricSettings.getCostSymbol(), powerToday * myElectricSettings.getUnitCost()));
            } else {
                txtPower.setText(String.format(getActivity().getResources().getConfiguration().locale, "%.0fW", powerNow));
                txtUseToday.setText(String.format(getActivity().getResources().getConfiguration().locale, "%.1fkWh", powerToday));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey("settings")) {
            myElectricSettings = getArguments().getParcelable("settings");

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.me_fragment, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        if (view == null)
            throw new NullPointerException("getView returned null");

            view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (v.getWidth() != 0) {
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    setDaysToDisplay(v.getWidth(), displayMetrics.density);
                }
            }
        });

        setHasOptionsMenu(true);


        timezone = (long) Math.floor((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET)) * 0.001);

        TextView txtPageName = (TextView) view.findViewById(R.id.pageName);
        txtPageName.setText(myElectricSettings.getName().toUpperCase());

        txtPower = (TextView) view.findViewById(R.id.txtPower);
        txtUseToday = (TextView) view.findViewById(R.id.txtUseToday);
        Button power3hButton = (Button) view.findViewById(R.id.btnChart1_3H);
        Button power6hButton = (Button) view.findViewById(R.id.btnChart1_6H);
        Button power1dButton = (Button) view.findViewById(R.id.btnChart1_D);
        Button power1wButton = (Button) view.findViewById(R.id.btnChart1_W);
        Button power1mButton = (Button) view.findViewById(R.id.btnChart1_M);

        power3hButton.setOnClickListener(buttonListener);
        power6hButton.setOnClickListener(buttonListener);
        power1dButton.setOnClickListener(buttonListener);
        power1wButton.setOnClickListener(buttonListener);
        power1mButton.setOnClickListener(buttonListener);

        powerChart = new PowerChart((LineChart) view.findViewById(R.id.chart1), getActivity());
        dailyUsageBarChart = new DailyBarChart((BarChart) view.findViewById(R.id.chart2), getActivity());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        setDaysToDisplay(displayMetrics.widthPixels, displayMetrics.density);

        setUpCharts(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (myElectricSettings != null) {
            Log.d("me", "visibility hint " + myElectricSettings.getName() + " - " + isVisibleToUser);
        }
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && snackbar != null) {
            snackbar.dismiss();
        } else if (isVisibleToUser && isMessage) {
            getSnackbar().show();
        }
        isVisibleInPager = isVisibleToUser;
    }

    private void loadConfig() {

        SharedPreferences sp = EmonApplication.get().getSharedPreferences(EmonApplication.get().getCurrentAccount());
        emonCmsUrl = sp.getBoolean(getString(R.string.setting_usessl), false) ? "https://" : "http://";
        emonCmsUrl += sp.getString(getString(R.string.setting_url), "emoncms.org");
        emonCmsApiKey = sp.getString(getString(R.string.setting_apikey), null);

    }

    private void setDaysToDisplay(int width, float density) {
        daysToDisplay = Math.round((width / density) / 52) - 1;
        if (mGetUsageByDayRunner != null) {
            mGetUsageByDayRunner.setDaysToDisplay(daysToDisplay);
        }
    }

    private void setUpCharts(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            Log.d("emon-me","Loading saved instance state " + savedInstanceState.getDoubleArray("chart1_values").length);
            blnShowCost = savedInstanceState.getBoolean("show_cost", false);
            powerChart.setChartLength(savedInstanceState.getInt("power_graph_length", -6));
            powerNow = savedInstanceState.getDouble("power_now", 0);
            powerToday = savedInstanceState.getDouble("power_today", 0);
            //wattFeedId = savedInstanceState.getInt("watt_feed_id", -1);
            //kWhFeelId = savedInstanceState.getInt("kwh_feed_id", -1);
            int[] chart2_colors = savedInstanceState.getIntArray("chart2_colors");

            updateTextFields();

            //put stored data back in the charts

            int lastPowerFeedId = savedInstanceState.getInt("power_feed_id");
            if (lastPowerFeedId > 0 && lastPowerFeedId == myElectricSettings.getPowerFeedId()) {
                ArrayList<String> chartLabels = savedInstanceState.getStringArrayList("chart1_labels");
                double saved_chart1_values[] = savedInstanceState.getDoubleArray("chart1_values");
                powerChart.restoreData(chartLabels, saved_chart1_values);
            }

            int lastUseFeedId = savedInstanceState.getInt("use_feed_id");
            if (lastUseFeedId > 0 && lastUseFeedId == myElectricSettings.getUseFeedId()) {
                double saved_chart2_values[] = savedInstanceState.getDoubleArray("chart2_values");
                ArrayList<String> chart2Labels = savedInstanceState.getStringArrayList("chart2_labels");
                dailyUsageBarChart.restoreData(chart2Labels, saved_chart2_values, chart2_colors, daysToDisplay);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("LIFECYCLE", "onSaveInstanceState");
        outState.putBoolean("show_cost", blnShowCost);
        outState.putInt("power_graph_length", powerChart.getChartLength());
        outState.putDouble("power_now", powerNow);
        outState.putDouble("power_today", powerToday);

        outState.putInt("power_feed_id", myElectricSettings.getPowerFeedId());
        outState.putInt("use_feed_id", myElectricSettings.getUseFeedId());


        outState.putParcelable("settings", myElectricSettings);
        outState.putIntArray("chart2_colors", dailyUsageBarChart.getBarColours());

        double[] values = new double[powerChart.getValues().size()];

        for (int i = 0; i < powerChart.getValues().size(); i++)
            values[i] = powerChart.getValues().get(i);

        outState.putStringArrayList("chart1_labels", powerChart.getLabels());
        outState.putDoubleArray("chart1_values", values);

        values = new double[dailyUsageBarChart.getLabels().size()];

        for (int i = 0; i < dailyUsageBarChart.getLabels().size(); i++)
            values[i] = dailyUsageBarChart.getValues().get(i);

        outState.putStringArrayList("chart2_labels", dailyUsageBarChart.getLabels());
        outState.putDoubleArray("chart2_values", values);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.me_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        /*costSwitch = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.cost_switch));
        costSwitch.setOnCheckedChangeListener(checkedChangedListener);
        costSwitch.setChecked(blnShowCost);*/
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onResume() {
        Log.d("emon-me","resume");
        super.onResume();
        clearMessage();
        loadConfig();

        mGetPowerHistoryRunner = new PowerChartDataLoader(powerChart, this.getActivity(), this, myElectricSettings.getPowerFeedId());
        mGetFeedsRunner = new FeedDataLoader(getActivity(), this);
        mGetPowerRunner = new PowerNowDataLoader(getActivity(), this, myElectricSettings.getPowerFeedId(), myElectricSettings.getUseFeedId());
        mGetUsageByDayRunner = new UseByDayDataLoader(getActivity(), this, dailyUsageBarChart, myElectricSettings.getUseFeedId());

        if (emonCmsApiKey == null || emonCmsApiKey.equals("") || emonCmsUrl == null || emonCmsUrl.equals("")) {
            showMessage(R.string.server_not_configured);
        } else if (myElectricSettings.getPowerFeedId() == -1 || myElectricSettings.getUseFeedId() == -1) {
            mHandler.post(mGetFeedsRunner);
        } else if (myElectricSettings.getPowerFeedId() >= 0 && myElectricSettings.getUseFeedId() >= 0) {
            clearMessage();
            mHandler.post(mGetPowerRunner);
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            clearMessage();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        super.onPause();

        clearMessage();
        HTTPClient.getInstance(getActivity()).cancellAll(getPageTag());
        mHandler.removeCallbacksAndMessages(null);
    }

    private CompoundButton.OnCheckedChangeListener checkedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            blnShowCost = isChecked;
            dailyUsageBarChart.refreshChart();
            updateTextFields();
        }
    };

    private OnClickListener buttonListener = new OnClickListener() {
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btnChart1_3H:
                    powerChart.setChartLength(-3);
                    break;
                case R.id.btnChart1_6H:
                    powerChart.setChartLength(-6);
                    break;
                case R.id.btnChart1_D:
                    powerChart.setChartLength(-24);
                    break;
                case R.id.btnChart1_W:
                    powerChart.setChartLength(-168); // 7 * 24
                    break;
                case R.id.btnChart1_M: // 4 Weeks
                    powerChart.setChartLength(-720); // 30 * 24
                    break;
            }
            HTTPClient.getInstance(getActivity()).cancellAll(getPageTag());
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(mGetPowerHistoryRunner);
        }
    };

    @Override
    public void loadPowerNow(int delay) {
        mHandler.postDelayed(mGetPowerRunner, delay);
    }

    @Override
    public void loadPowerHistory(int delay) {
        mHandler.postDelayed(mGetPowerHistoryRunner, delay);
    }

    @Override
    public boolean loadUseHistory(int delay) {
        if (Calendar.getInstance().getTimeInMillis() > nextDailyChartUpdate) {
            nextDailyChartUpdate = Calendar.getInstance().getTimeInMillis() + dailyChartUpdateInterval;
            mHandler.postDelayed(mGetUsageByDayRunner, delay);
            return true;
        }
        return false;

    }

    @Override
    public void loadFeeds(int delay) {
        mHandler.postDelayed(mGetFeedsRunner, delay);
    }

    private Snackbar getSnackbar() {
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView.findViewById(R.id.mefrag),  R.string.connection_error, Snackbar.LENGTH_INDEFINITE);
            View snackbar_view = snackbar.getView();
            snackbar_view.setBackgroundColor(Color.GRAY);
            TextView tv = (TextView) snackbar_view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTypeface(null, Typeface.BOLD);
        }
        return snackbar;
    }


    @Override
    public void showMessage(String message) {
        isMessage = true;
        if (myElectricSettings != null) {
            Log.d("me", "showing message " + myElectricSettings.getName() + " - " + message);
        }
        getSnackbar().setText(message);
        if (isVisibleInPager) {
            getSnackbar().show();
        }
    }

    @Override
    public void showMessage(int message) {
        isMessage = true;
        if (myElectricSettings != null) {
            Log.d("me", "showing message " + myElectricSettings.getName() + " - " + message);
        }

        getSnackbar().setText(message);
        if (isVisibleInPager) {
            getSnackbar().show();
        }
    }

    @Override
    public void clearMessage() {
        isMessage = false;
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public String getEmonCmsUrl() {
        return emonCmsUrl;
    }

    @Override
    public String getEmoncmsApikey() {
        return emonCmsApiKey;
    }

    @Override
    public void setFeedIds(int flowId, int useId) {
        myElectricSettings.setPowerFeedId(flowId);
        myElectricSettings.setUseFeedId(useId);
    }

    @Override
    public void setCurrentValues(float powerNow, float totalPowerUsage) {
        this.powerNow = powerNow;
        this.totalPowerUsage = totalPowerUsage;
        if (yesterdaysPowerUsage > 0) {
            this.powerToday = totalPowerUsage - yesterdaysPowerUsage;
        }
        updateTextFields();
    }

    @Override
    public float getTotalUsage() {
        return totalPowerUsage;
    }

    @Override
    public void setUseToYesterday(float useToYesterday) {
        this.yesterdaysPowerUsage = useToYesterday;
        this.powerToday = totalPowerUsage - yesterdaysPowerUsage;
        updateTextFields();

    }

    @Override
    public String getPageTag() {
        return EmonApplication.get().getCurrentAccount() + myElectricSettings.getName();
    }


}