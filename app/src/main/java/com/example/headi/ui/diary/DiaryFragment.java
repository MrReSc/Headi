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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.DiaryCourserTreeAdapter;
import com.example.headi.db.DiaryEditViewHelper;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.example.headi.db.PainsCourserCheckboxAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private ExpandableListView DiaryItems;
    private EditText fromDate;
    private EditText toDate;
    private String fromDateFilter;
    private String toDateFilter;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        DiaryItems = (ExpandableListView) view.findViewById(R.id.diary_list);
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
        if (item.getItemId() == R.id.action_diary_filter_list) {
            openFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFilterDialog() {
        Context context = requireActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_diary_filter));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_diary_filter_dialog, null);
        builder.setView(saveView);

        // populate Pains in ListView
        ListView PainsItems = (ListView) saveView.findViewById(R.id.filter_pains_list);

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsCourserCheckboxAdapter painsCbAdapter = helper.readPainsWithCheckboxFromDB(context);
        PainsItems.setAdapter(painsCbAdapter);

        registerDatePickerListeners(saveView);

        // add apply button
        builder.setPositiveButton(context.getString(R.string.apply_button), (dialog, which) -> {
            String timeSelection = "";
            StringBuilder painSelection = new StringBuilder();
            ArrayList<String> timeArgs = new ArrayList<>();
            ArrayList<String> painArgs = new ArrayList<>();
            String selection = "";
            String[] selectionArgs = new String[0];


            // time filter is set
            if (fromDateFilter != null && toDateFilter != null) {
                timeSelection = HeadiDBContract.Diary.COLUMN_START_DATE + " >= ? AND " + HeadiDBContract.Diary.COLUMN_END_DATE + " <= ?";
                timeArgs.add(fromDateFilter);
                timeArgs.add(toDateFilter);
            }

            // pains filter is set
            if (!painsCbAdapter.getSelectedString().isEmpty()) {
                painSelection = new StringBuilder(HeadiDBContract.Diary.COLUMN_PAIN + " = ?");
                painArgs = painsCbAdapter.getSelectedString();
                if (painArgs.size() > 1) {
                    for (String arg : painArgs){
                        painSelection.append(" OR ").append(HeadiDBContract.Diary.COLUMN_PAIN).append(" = ?");
                    }
                }
            }

            if (!timeSelection.isEmpty() && (painSelection.length() > 0)) {
                selection =  timeSelection + " AND (" + painSelection + ")";
                timeArgs.addAll(painArgs);
                selectionArgs = timeArgs.toArray(new String[0]);
            }
            else if (!timeSelection.isEmpty()) {
                selection =  timeSelection;
                selectionArgs = timeArgs.toArray(new String[0]);
            }
            else if (painSelection.length() > 0) {
                selection = painSelection.toString();
                selectionArgs = painArgs.toArray(new String[0]);
            }

            if (!selection.isEmpty()) {
                readFromDB(selection, selectionArgs);
            }

        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // add delete filter button
        builder.setNeutralButton(context.getString(R.string.remove_filter_button), (dialog, which) -> readFromDB(null, null));

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
        DiaryCourserTreeAdapter adapter = helper.readDiaryGroupFromDB(context, selection, selectionArgs);
        DiaryItems.setAdapter(adapter);
    }

    private void registerListeners(Context context) {
        // Find ListView to populate
        DiaryItems.setOnItemLongClickListener((adapterView, view, position, id) -> {

            long groupId = DiaryItems.getExpandableListAdapter().getGroupId(position);

            // Create an alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.title_diary_item));
            builder.setMessage(context.getString(R.string.action_for_diary_item));

            builder.setNeutralButton(context.getString(R.string.edit_restore), (dialog, which) -> openItemUpdateDialog(groupId));
            builder.setPositiveButton(context.getString(R.string.delete_button), (dialog, which) -> deleteFromDB(groupId));

            // add cancel button
            builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        });

    }

    private void openItemUpdateDialog(long groupId) {

        Context context = getActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_edit_diary_item));

        // set the edit layout
        final View editView = getLayoutInflater().inflate(R.layout.fragment_diary_edit_dialog, null);
        builder.setView(editView);

        builder.setPositiveButton(context.getString(R.string.save_button), (dialog, which) -> updateItemInDB(groupId, editView));
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        populateEditView(groupId, editView);
        dialog.show();
    }

    private void updateItemInDB(long groupId, View editView) {


    }

    private void populateEditView(long groupId, View editView) {
        DiaryEditViewHelper adapter = new DiaryEditViewHelper(getActivity(), editView, groupId);
        adapter.populateView();
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