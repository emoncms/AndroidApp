package org.emoncms.myapps.chart;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.emoncms.myapps.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A Bar Chart
 */
public class DailyBarChart {

    private BarChart barChart;
    private final Context context;

    private ArrayList<String> chartLabels;
    private List<Double> chartValues;
    private int[] chartBarColours;

    private BarData barData;

    //FIXME need to load these
    private boolean showCost;
    private float powerCost = 0;

    public DailyBarChart(BarChart barChart, Context context) {
        this.barChart = barChart;
        this.context = context;

        chartLabels = new ArrayList<>();
        chartValues = new ArrayList<>();

        setFormatting();

        barData = createDataSet();
    }

    public ArrayList<String> getLabels() {
        return chartLabels;
    }

    public List<Double> getValues() {
        return chartValues;
    }

    public int[] getBarColours() {
        return chartBarColours;
    }

    public void setBarColours(int[] colours) {
        chartBarColours = colours;
    }

    public void clearData() {
        chartLabels.clear();
        chartValues.clear();
    }

    public void restoreData(ArrayList<String> savedChartLabels, double[] savedChartValues, int[] colours, int daysToDisplay) {
        if (validNonNullSavedData(savedChartLabels, savedChartValues)) {

            chartLabels = savedChartLabels;
            for (int i = 0; i < chartLabels.size(); i++) {
                chartValues.add(savedChartValues[i]);
            }
        }

        int start = 0;
        if (daysToDisplay < chartLabels.size()) {
            start = chartLabels.size() - daysToDisplay;
        }

        setBarColours(colours);

        refreshChart(start);
    }

    private boolean validNonNullSavedData(List<String> savedChartLabels, double[] savedChartValues) {
        return (savedChartLabels != null && savedChartValues != null
                && savedChartLabels.size() > 0
                && savedChartLabels.size() == savedChartValues.length);
    }

    /**
     * Adds a point at the end of the data set
     * @param label
     * @param data
     */
    public void addData(String label, double data) {
        chartLabels.add(label);
        chartValues.add(data);
    }

    public void refreshChart() {
        refreshChart(0);
    }

    public void refreshChart(int start) {
        barData.getXVals().clear();
        BarDataSet dataSet = (BarDataSet) barData.getDataSetByLabel("kWh", true);
        dataSet.clear();
        dataSet.setColors(chartBarColours);

        for (int i = start; i < chartLabels.size(); i++) {
            barData.addEntry(new BarEntry(chartValues.get(i).floatValue(), i), 0);
            barData.addXValue(chartLabels.get(i));
        }


        notifyDataChanged();

    }

    private void notifyDataChanged() {
        barData.notifyDataChanged();
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }



    private void setFormatting() {
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setHardwareAccelerationEnabled(true);
        barChart.setDescription("");
        barChart.setNoDataText("");
        barChart.setTouchEnabled(false);
        barChart.setExtraBottomOffset(2);

        XAxis xAxis2 = barChart.getXAxis();
        xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis2.setTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        xAxis2.setTextSize(context.getResources().getInteger(R.integer.chartValueTextSize));
        xAxis2.setDrawGridLines(false);
        xAxis2.setDrawAxisLine(false);
    }

    private BarData createDataSet() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        BarData chart2_bardata = new BarData();
        BarDataSet chart2_dataset = new BarDataSet(entries, "kWh");
        chart2_dataset.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        chart2_dataset.setValueTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        chart2_dataset.setValueTextSize(context.getResources().getInteger(R.integer.chartValueTextSize));
        chart2_dataset.setValueFormatter(new Chart2ValueFormatter());
        chart2_bardata.addDataSet(chart2_dataset);
        barChart.setData(chart2_bardata);
        return chart2_bardata;
    }

    public class Chart2ValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public Chart2ValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
            mFormat.setNegativePrefix("");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            if (showCost)
                value = value * powerCost;

            return mFormat.format(value);
        }
    }


}
