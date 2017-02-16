package org.emoncms.myapps.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class IntegerYAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;

    public IntegerYAxisValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value);
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }


}
