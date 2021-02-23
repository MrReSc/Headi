package com.example.headi.ui.diary;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.DiaryCourserAdapter;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class DiaryFragment extends Fragment {

    private View view;
    private ListView DiaryItems;
    private EditText fromDate;
    private EditText toDate;
    private String fromDateFilter;
    private String toDateFilter;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        view = inflater.inflate(R.layout.fragment_diary, container, false);
        DiaryItems = (ListView) view.findViewById(R.id.diary_list);
        setHasOptionsMenu(true);

        registerListeners(context);
        readFromDB(null, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diary, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_diary_filter_list:  {
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
        builder.setTitle(context.getString(R.string.title_diary_filter));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_diary_filter_dialog, null);
        builder.setView(saveView);

        registerDatePickerListeners(saveView);

        // add apply button
        builder.setPositiveButton(context.getString(R.string.apply_button), (dialog, which) -> {
            // apply date filter
            if (!fromDateFilter.isEmpty() && !toDateFilter.isEmpty()) {
                String selection = HeadiDBContract.Diary.COLUMN_START_DATE + " >= ? AND " +
                                   HeadiDBContract.Diary.COLUMN_END_DATE + " <= ?";

                String[] selectionArgs = {fromDateFilter, toDateFilter};
                readFromDB(selection, selectionArgs);
            }

        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // add delete filter button
        builder.setNeutralButton(context.getString(R.string.delete_filter_button), (dialog, which) -> {
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
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        DiaryCourserAdapter adapter = helper.readDiaryFromDB(context, selection, selectionArgs);
        DiaryItems.setAdapter(adapter);
    }

    private void registerListeners(Context context) {
        // Find ListView to populate
        DiaryItems.setOnItemLongClickListener((adapterView, view, position, id) -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.action_delete))
                    .setMessage(context.getString(R.string.delete_diary))
                    .setPositiveButton(context.getString(R.string.delete_button),
                            (dialogInterface, i) -> deleteFromDB(id))
                    .setNegativeButton(context.getString(R.string.cancel_button), (dialogInterface, i) -> {
                    })
                    .show();
            return true;
        });

    }

    private void deleteFromDB(long id) {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();

        String selection = HeadiDBContract.Diary._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};

        database.delete(HeadiDBContract.Diary.TABLE_NAME, selection, selectionArgs);
        readFromDB(null, null);
    }
}