package com.headi.app.db;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import com.headi.app.Constants;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.headi.app.ui.UiHelper;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;

public class DiaryStats {

    private final Cursor cursor;
    private final Context context;
    private final SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
    private static final long DAY_MILLS = 86400000L;
    public double trendSlope = 0;

    public DiaryStats(Context context, Cursor cursor) {
        this.cursor = cursor;
        this.context = context;
    }

    public PieData getPainAndDurationRatio(PieChart chart) {
        HashMap<String, Long> result = new HashMap<>();
        ArrayList<PieEntry> entries = new ArrayList<>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));
            Long duration = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION)) / 60000;

            if (duration > 0) {
                if (result.containsKey(pain)) {
                    result.put(pain, result.get(pain) + duration);
                }
                else {
                    result.put(pain, duration);
                }
            }
        }

        for (String key : result.keySet()) {
            entries.add(new PieEntry((float) result.get(key), key));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Pain - Duration Ratio");
        dataSet.setColors(Constants.MATERIAL_COLORS_500);
        dataSet.setSliceSpace(2f);

        dataSet.setValueTextColor(UiHelper.getPrimaryTextColor(context));
        dataSet.setValueTextSize(12f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setValueLinePart1OffsetPercentage(50.f);
        dataSet.setValueLinePart1Length(1.1f);
        dataSet.setValueLinePart2Length(0.6f);
        dataSet.setValueLineColor(UiHelper.getPrimaryTextColor(context));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(UiHelper.getPrimaryTextColor(context));

        chart.setEntryLabelColor(UiHelper.getPrimaryTextColor(context));

        return data;
    }

    public BarData getCountAndStrengthRatio() {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        ArrayList<BarEntry> entries = new ArrayList<>();

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            Integer strength = cursor.getInt(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_STRENGTH));

            if (result.containsKey(strength)) {
                result.put(strength, result.get(strength) + 1);
            }
            else {
                result.put(strength, 1);
            }
        }

        // fill list with non existing pain strength
        for (int i = 1; i < 11; i++) {
            if (!result.containsKey(i)) {
                result.put(i, 0);
            }
        }

        for (Integer key : result.keySet()) {
            entries.add(new BarEntry((float) key, result.get(key), key.toString()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Count - Strength Ratio");
        dataSet.setColors(Constants.MATERIAL_COLORS_500);
        dataSet.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        return new BarData(dataSets);
    }

    public boolean getDurationOverTimeDataAvailable() {
        long startDate = 0;
        long endDate = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            startDate = getDateFromTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
            cursor.moveToFirst();
            endDate = getDateFromTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        }

        return endDate > startDate + DAY_MILLS;
    }

    public LineData getDurationOverTime() {
        TreeMap<Long, Long> result = new TreeMap<>();
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entriesTrendLine = new ArrayList<>();
        long startDate = 0;
        long endDate = 0;

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            startDate = getDateFromTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
            cursor.moveToFirst();
            endDate = getDateFromTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        }

        // generate list for every day between start and end date
        for (long i = startDate; i <= endDate; i += DAY_MILLS ) {
            result.put(i, 0L);
        }

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            Long date = getDateFromTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
            Long duration = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION)) / 60000;

            if (result.containsKey(date)) {
                result.put(date, result.get(date) + duration);
            }
            else {
                result.put(date, duration);
            }
        }

        for (Long key : result.keySet()) {
            entries.add(new BarEntry((float) key, result.get(key)));
        }

        // calc trend line
        SimpleRegression simpleRegression = new SimpleRegression(true);
        double[][] regressionData = new double[result.size()][2];
        int counter = 0;
        for (Long key : result.keySet()) {
            regressionData[counter][0] = key;
            regressionData[counter][1] = result.get(key);
            counter ++;
        }

        simpleRegression.addData(regressionData);

        for (Long key : result.keySet()) {
            float y = (float) simpleRegression.predict(key);
            entriesTrendLine.add(new BarEntry((float) key, y));
        }

        trendSlope = simpleRegression.getSlope();
        int color = Constants.MATERIAL_COLORS_500[6];
        if (trendSlope > 0) {
            color = Constants.MATERIAL_COLORS_500[0];
        }
        else if (trendSlope < 0) {
            color = Constants.MATERIAL_COLORS_500[5];
        }

        // Setup data sets
        LineDataSet dataSet = new LineDataSet(entries, "Duration over time");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1.8f);
        dataSet.setColor(Constants.MATERIAL_COLORS_500[1]);
        dataSet.setFillColor(Constants.MATERIAL_COLORS_500[1]);
        dataSet.setFillAlpha(100);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        LineDataSet dataSetTrend = new LineDataSet(entriesTrendLine, "Trend");
        dataSetTrend.setMode(LineDataSet.Mode.LINEAR);
        dataSetTrend.setDrawValues(false);
        dataSetTrend.setDrawFilled(false);
        dataSetTrend.setDrawCircles(false);
        dataSetTrend.setLineWidth(1.1f);
        dataSetTrend.setColor(color);
        dataSetTrend.setDrawHorizontalHighlightIndicator(false);

        // create a data object with the data sets
        LineData data = new LineData(dataSetTrend, dataSet);
        data.setDrawValues(false);

        // set data
        return data;
    }

    private Long getDateFromTimestamp (Long time) {
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date(time));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public String getStatsFromDate() {
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            return date_formatter.format(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE))));
        }
        return "0";
    }

    public String getStatsToDate() {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return date_formatter.format(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE))));
        }
        return "0";
    }

    public static class LineChartXAxisValueFormatter extends IndexAxisValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            SimpleDateFormat df = new SimpleDateFormat("dd. MMM", Locale.getDefault());
            return df.format((long) value);
        }
    }
}

