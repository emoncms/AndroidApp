package org.emoncms.myapps.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class HoursMinutesXAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<String> labels;

    public HoursMinutesXAxisValueFormatter(ArrayList<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value >= labels.size()) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(labels.get((int) value)));
        return (df.format(cal.getTime()));
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
