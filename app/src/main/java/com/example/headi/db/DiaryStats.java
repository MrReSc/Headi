package com.example.headi.db;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.util.TypedValue;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class DiaryStats {

    private Cursor cursor;
    private Context context;

    public DiaryStats(Context context, Cursor cursor) {
        this.cursor = cursor;
        this.context = context;
    }

    private int getPrimaryTextColor() {
        // Get the primary text color of the theme
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorPrimary});
        int primaryColor = arr.getColor(0, -1);
        return primaryColor;
    }


    public PieData getPainAndDurationPieData(PieChart chart) {

        HashMap<String, Long> result = new HashMap<>();
        ArrayList<PieEntry> entries = new ArrayList<>();

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

        PieDataSet dataSet = new PieDataSet(entries, "Quarterly Revenues 2015");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(2f);

        dataSet.setValueTextColor(getPrimaryTextColor());
        dataSet.setValueTextSize(12f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setValueLinePart1OffsetPercentage(50.f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.6f);
        dataSet.setValueLineColor(getPrimaryTextColor());
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(getPrimaryTextColor());

        return data;
    }
}
