package org.emoncms.myapps.chart;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;

import org.emoncms.myapps.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the PowerChart display
 */
public class PowerChart {

    private final Context context;
    private LineChart powerChart;
    private ArrayList<String> chartLabels;
    private List<Double> chartValues;

    private int powerChartLength = -6;
    private boolean requiresReset = false;

    private LineData powerData;

    public PowerChart(LineChart powerChart,  Context context) {
        this.powerChart = powerChart;
        this.context = context;

        chartLabels = new ArrayList<>();
        chartValues = new ArrayList<>();
        
        setFormatting();
        powerData = createData();
        
    }



    public ArrayList<String> getLabels() {
        return chartLabels;
    }

    public List<Double> getValues() {
        return chartValues;
    }

    public void clearData() {
        chartLabels.clear();
        chartValues.clear();
    }

    public void setChartLength(int length) {
        powerData = createData();
        powerChartLength = length;
        requiresReset = true;
    }

    public int getChartLength() {
        return powerChartLength;
    }

    public boolean requiresReset() {
        return requiresReset;
    }

    public void restoreData(ArrayList<String> savedChartLabels, double[] savedChartValues) {
        if (validNonNullSavedData(savedChartLabels, savedChartValues)) {

            chartLabels = savedChartLabels;
            for (int i = 0; i < chartLabels.size(); i++) {
                chartValues.add(savedChartValues[i]);
            }
        }

        refreshChart();
    }

    /**
     * Updates the chart to use the current contents of Labels and Data
     */
    public void refreshChart() {


        LineDataSet dataSet = (LineDataSet) powerData.getDataSetByLabel("watts", true);
        dataSet.clear();
        //powerData.clearValues();
        //powerData.addDataSet(createDataSet());




        for (int i = 0; i < chartLabels.size(); i++) {

            powerData.addEntry(new Entry(i, chartValues.get(i).floatValue()), 0);
        }

        if (requiresReset) {
            powerChart.fitScreen();
        }
        requiresReset = false;

        XAxis xAxis = powerChart.getXAxis();
        xAxis.setValueFormatter(new HoursMinutesXAxisValueFormatter(chartLabels));


        notifyDataChanged();
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

    /**
     * Removes a point at the beginning of the dataset
     */
    public void removeFirstPoint() {
        chartLabels.remove(0);
        chartValues.remove(0);
    }

    private void notifyDataChanged() {
        powerData.notifyDataChanged();
        powerChart.notifyDataSetChanged();
        powerChart.invalidate();

    }



    private boolean validNonNullSavedData(List<String> savedChartLabels, double[] savedChartValues) {
        return (savedChartLabels != null && savedChartValues != null
                && savedChartLabels.size() > 0
                && savedChartLabels.size() == savedChartValues.length);
    }


    
    private LineData createData() {
        LineDataSet powerDataset = createDataSet();

        LineData ld = new LineData();
        ld.addDataSet(powerDataset);
        powerChart.setData(ld);
        return  ld;
    }

    private LineDataSet createDataSet() {
        LineDataSet powerDataset = new LineDataSet(null, "watts");
        powerDataset.setColor(ContextCompat.getColor(context, R.color.chartBlue));
        powerDataset.setValueTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        powerDataset.setDrawCircles(false);
        powerDataset.setDrawFilled(true);
        powerDataset.setFillColor(ContextCompat.getColor(context, R.color.chartBlue));
        powerDataset.setDrawValues(false);
        powerDataset.setValueTextSize(R.integer.chartValueTextSize);
        powerDataset.setHighlightEnabled(false);
        return powerDataset;
    }


    private void setFormatting() {
        powerChart.setDrawGridBackground(false);
        powerChart.getLegend().setEnabled(false);
        powerChart.getAxisRight().setEnabled(false);
        powerChart.getDescription().setEnabled(false);
        powerChart.setNoDataText("");
        powerChart.setHardwareAccelerationEnabled(true);

        YAxis yAxis = powerChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setDrawTopYLabelEntry(false);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        yAxis.setTextSize(context.getResources().getInteger(R.integer.chartDateTextSize));
        yAxis.setValueFormatter(new IntegerYAxisValueFormatter());

        XAxis xAxis = powerChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        xAxis.setValueFormatter(new HoursMinutesXAxisValueFormatter(chartLabels));

        xAxis.setTextSize(context.getResources().getInteger(R.integer.chartDateTextSize));
    }


}
