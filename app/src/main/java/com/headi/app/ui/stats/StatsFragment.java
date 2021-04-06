package com.headi.app.ui.stats;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.headi.app.Constants;
import com.headi.app.R;
import com.headi.app.db.DiaryStats;
import com.headi.app.db.HeadiDBContract;
import com.headi.app.db.HeadiDBSQLiteHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.headi.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private View view;
    private EditText fromDate;
    private EditText toDate;
    private String fromDateFilter;
    private String toDateFilter;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private DiaryStats diaryStats;
    private PieChart piePainDurationRatio;
    private BarChart barCountStrengthRatio;
    private LineChart lineDurationOverTime;
    private boolean isFilterSet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_stats, container, false);

        setHasOptionsMenu(true);
        setupCharts();
        readFromDB(null, null);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_stats_filter) {
            openFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCharts() {
        // Pie Chart: Pain - Duration ratio
        piePainDurationRatio = view.findViewById(R.id.stats_pain_duration_ratio);
        piePainDurationRatio.getDescription().setEnabled(false);
        piePainDurationRatio.setUsePercentValues(true);
        piePainDurationRatio.getLegend().setEnabled(false);
        piePainDurationRatio.setRotationEnabled(false);
        piePainDurationRatio.setHighlightPerTapEnabled(false);
        piePainDurationRatio.setExtraOffsets(30.f, 0.f, 30.f, 0.f);
        piePainDurationRatio.setHoleRadius(0f);
        piePainDurationRatio.setTransparentCircleRadius(0f);
        piePainDurationRatio.animateXY(1000, 1000);

        // Bar Chart: Count - Strength ratio
        barCountStrengthRatio = view.findViewById(R.id.stats_count_strength_ratio);
        barCountStrengthRatio.getDescription().setEnabled(false);
        barCountStrengthRatio.setScaleEnabled(false);
        barCountStrengthRatio.setDrawBarShadow(false);
        barCountStrengthRatio.setDrawGridBackground(false);
        barCountStrengthRatio.getLegend().setEnabled(false);
        barCountStrengthRatio.setHighlightPerTapEnabled(false);
        barCountStrengthRatio.animateXY(1000, 1000);
        barCountStrengthRatio.setTouchEnabled(false);

        XAxis xAxis = barCountStrengthRatio.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        xAxis.setLabelCount(10);

        YAxis yAxis = barCountStrengthRatio.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        yAxis.setGranularity(1.0f);
        yAxis.setGranularityEnabled(true); // Required to enable granularity
        barCountStrengthRatio.getAxisRight().setDrawLabels(false);

        // Line Chart: Pain minutes over time
        lineDurationOverTime = view.findViewById(R.id.stats_duration_over_time);
        lineDurationOverTime.getDescription().setEnabled(false);
        lineDurationOverTime.setScaleEnabled(false);
        lineDurationOverTime.setDragEnabled(false);
        lineDurationOverTime.setTouchEnabled(false);
        lineDurationOverTime.setPinchZoom(false);
        lineDurationOverTime.animateXY(1000, 1000);
        lineDurationOverTime.getLegend().setEnabled(false);
        lineDurationOverTime.setHighlightPerTapEnabled(false);
        lineDurationOverTime.getXAxis().setValueFormatter(new DiaryStats.LineChartXAxisValueFormatter());

        xAxis = lineDurationOverTime.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));

        yAxis = lineDurationOverTime.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        yAxis.setAxisMinimum(0f);
        lineDurationOverTime.getAxisRight().setDrawLabels(false);
    }

    private void openFilterDialog() {
        Context context = requireActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.stats_set_date_title));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_stats_filter_dialog, null);
        builder.setView(saveView);

        registerDatePickerListeners(saveView);

        // add apply button
        builder.setPositiveButton(context.getString(R.string.button_ok), (dialog, which) -> {
            String timeSelection = "";
            ArrayList<String> timeArgs = new ArrayList<>();
            String selection = "";
            String[] selectionArgs = new String[0];

            // time filter is set
            if (fromDateFilter != null && toDateFilter != null) {
                timeSelection = HeadiDBContract.Diary.COLUMN_START_DATE + " >= ? AND " + HeadiDBContract.Diary.COLUMN_END_DATE + " <= ?";
                timeArgs.add(fromDateFilter);
                timeArgs.add(toDateFilter);
            }

            if (!timeSelection.isEmpty()) {
                selection =  timeSelection;
                selectionArgs = timeArgs.toArray(new String[0]);
            }

            if (!selection.isEmpty()) {
                isFilterSet = true;
                readFromDB(selection, selectionArgs);
            }
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // add delete filter button
        builder.setNeutralButton(context.getString(R.string.remove_filter_button), (dialog, which) -> {
            isFilterSet = false;
            readFromDB(null, null);
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerDatePickerListeners(View view) {
        Context context = requireActivity();
        SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
        fromDate = view.findViewById(R.id.filter_from_date);
        toDate = view.findViewById(R.id.filter_to_date);

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(context, (view12, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 0,0,0);
            fromDate.setText(date_formatter.format(newDate.getTime()));
            fromDateFilter = Long.toString(newDate.getTimeInMillis());
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(context, (view1, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
            toDate.setText(date_formatter.format(newDate.getTime()));
            toDateFilter = Long.toString(newDate.getTimeInMillis());
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDate.setOnClickListener(v -> fromDatePickerDialog.show());
        toDate.setOnClickListener(v -> toDatePickerDialog.show());
    }

    private void readFromDB(String selection, String[] selectionArgs) {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        diaryStats = helper.readDiaryStatsFromDB(context, selection, selectionArgs);

        populateCharts();
        setStatsFromAndToDate();
    }

    private void setStatsFromAndToDate() {
        Context context = requireActivity();
        TextView fromAndTo = view.findViewById(R.id.stats_date_from_to);
        String from = diaryStats.getStatsFromDate();
        String to = diaryStats.getStatsToDate(isFilterSet);
        fromAndTo.setText(context.getString(R.string.from_to, from, to));
        fromAndTo.setTextColor(UiHelper.getPrimaryTextColor(getActivity()));
        if (diaryStats.getStatsFromDate().equals("0")) {
            fromAndTo.setText(context.getString(R.string.no_data_available));
            fromAndTo.setTextColor(context.getColor(R.color.button_stop));
        }
    }

    private void populateCharts() {
        piePainDurationRatio.setData(diaryStats.getPainAndDurationRatio(piePainDurationRatio));
        piePainDurationRatio.invalidate();

        barCountStrengthRatio.setData(diaryStats.getCountAndStrengthRatio());
        barCountStrengthRatio.setFitBars(true);
        barCountStrengthRatio.invalidate();

        lineDurationOverTime.setData(diaryStats.getDurationOverTime(false, isFilterSet));
        lineDurationOverTime.invalidate();

        // set trend icon
        ImageView timer_stats_trend_icon = view.findViewById(R.id.timer_stats_trend_icon);
        ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[2]));
        if (diaryStats.trendSlope > 0) {
            timer_stats_trend_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_trend_up, null));
            ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[0]));
        }
        else if (diaryStats.trendSlope < 0) {
            timer_stats_trend_icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_trend_down, null));
            ImageViewCompat.setImageTintList(timer_stats_trend_icon, ColorStateList.valueOf(Constants.MATERIAL_COLORS_500[5]));
        }
    }
}