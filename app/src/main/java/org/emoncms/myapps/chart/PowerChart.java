package org.emoncms.myapps.chart;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.emoncms.myapps.R;

/**
 * Class to handle the PowerChart display
 */
public class PowerChart {

    private final Context context;
    private LineChart powerChart;

    public PowerChart(LineChart powerChart,  Context context) {
        this.powerChart = powerChart;
        this.context = context;
        
        setFormatting();
        setupDataSet();

        
        
    }
    
    private void setupDataSet() {
        LineDataSet chart1_dataset = new LineDataSet(null, "watts");
        chart1_dataset.setColor(ContextCompat.getColor(context, R.color.chartBlue));
        chart1_dataset.setValueTextColor(ContextCompat.getColor(context, R.color.lightGrey));
        chart1_dataset.setDrawCircles(false);
        chart1_dataset.setDrawFilled(true);
        chart1_dataset.setFillColor(ContextCompat.getColor(context, R.color.chartBlue));
        chart1_dataset.setDrawValues(false);
        chart1_dataset.setValueTextSize(ContextCompat.getColor(context, R.integer.chartValueTextSize));
        chart1_dataset.setHighlightEnabled(false);
        LineData chart1_linedata = new LineData();
        chart1_linedata.addDataSet(chart1_dataset);
        powerChart.setData(chart1_linedata);
    }
    
    private void setFormatting() {
        powerChart.setDrawGridBackground(false);
        powerChart.getLegend().setEnabled(false);
        powerChart.getAxisRight().setEnabled(false);
        powerChart.setDescription("");
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
        xAxis.setValueFormatter(new HoursMinutesXAxisValueFormatter());
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setTextSize(context.getResources().getInteger(R.integer.chartDateTextSize));
    }
}
