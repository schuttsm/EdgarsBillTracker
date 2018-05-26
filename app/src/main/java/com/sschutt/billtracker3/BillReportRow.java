package com.sschutt.billtracker3;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Created by Stephen on 5/20/2018.
 */

public class BillReportRow {
    public String amount;
    public String currency;
    public String category;
    public Date date;
    // public int timezone;
    public String id;

    public String getFormattedDate() {
        if (DateUtils.isToday(date.getTime())) {
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
            return "Today " + dt.format(date);
        }
        else {
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm EEE, MMM d");
            return dt.format(date);
        }
    }
}
