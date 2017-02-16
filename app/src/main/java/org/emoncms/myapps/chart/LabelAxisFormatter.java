package org.emoncms.myapps.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class LabelAxisFormatter implements IAxisValueFormatter {

    private ArrayList<String> labels;

    public LabelAxisFormatter(ArrayList<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if ((int)value >= labels.size()) {
            return "";
        }
        return labels.get((int) value);
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
