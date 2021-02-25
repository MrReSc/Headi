package com.example.headi.ui.stats;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.example.headi.db.PainsCourserCheckboxAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        view = inflater.inflate(R.layout.fragment_stats, container, false);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stats_filter:  {
                openFilterDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
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
        builder.setPositiveButton(context.getString(R.string.apply_button), (dialog, which) -> {
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
                readFromDB(selection, selectionArgs);
            }
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // add delete filter button
        builder.setNeutralButton(context.getString(R.string.remove_filter_button), (dialog, which) -> {
            readFromDB(null, null);
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerDatePickerListeners(View view) {
        Context context = requireActivity();
        SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
        fromDate = (EditText) view.findViewById(R.id.filter_from_date);
        toDate = (EditText) view.findViewById(R.id.filter_to_date);

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth, 0,0,0);
                fromDate.setText(date_formatter.format(newDate.getTime()));
                fromDateFilter = Long.toString(newDate.getTimeInMillis());
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
                toDate.setText(date_formatter.format(newDate.getTime()));
                toDateFilter = Long.toString(newDate.getTimeInMillis());
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDate.setOnClickListener(v -> fromDatePickerDialog.show());
        toDate.setOnClickListener(v -> toDatePickerDialog.show());
    }

    private void readFromDB(String selection, String[] selectionArgs) {


    }
}