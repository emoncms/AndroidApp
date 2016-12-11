package org.emoncms.myapps.chart;

import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class HoursMinutesXAxisValueFormatter implements XAxisValueFormatter
{
    @Override
    public String getXValue(String original, int index, ViewPortHandler viewPortHandler)
    {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(original));
        return (df.format(cal.getTime()));
    }
}
