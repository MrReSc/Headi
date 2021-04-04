package com.headi.app.ui.diary;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.headi.app.R;
import com.headi.app.db.DiaryCourserTreeAdapter;
import com.headi.app.db.DiaryEditViewHelper;
import com.headi.app.db.HeadiDBContract;
import com.headi.app.db.HeadiDBSQLiteHelper;
import com.headi.app.db.PainsCourserCheckboxAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private ExpandableListView DiaryItems;
    private EditText fromDate;
    private EditText toDate;
    private String fromDateFilter;
    private String toDateFilter;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private View view;
    private static final int FILE_CREATE_PDF = 5;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_diary, container, false);
        DiaryItems = view.findViewById(R.id.diary_list);
        setHasOptionsMenu(true);

        registerForContextMenu(DiaryItems);
        readFromDB(null, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diary, menu);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_context_edit_delete, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_diary_filter_list) {
            openFilterDialog();
            return true;
        }

        if (item.getItemId() == R.id.action_diary_export_pdf) {
            exportDiaryAsPdf();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportDiaryAsPdf() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String outFileName = sdf.format(new Date()) + "_headi_diary.pdf";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, outFileName);

        startActivityForResult(intent, FILE_CREATE_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == FILE_CREATE_PDF && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(getActivity());
                helper.exportDiaryToPdf(getActivity(), uri, null, null);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int position = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        long groupId = DiaryItems.getExpandableListAdapter().getGroupId(position);

        if (item.getItemId() == R.id.action_item_edit) {
            openItemUpdateDialog(groupId);
            return true;
        }

        if (item.getItemId() == R.id.action_item_delete) {
            deleteFromDB(groupId);
            return true;
        }
        return super.onContextItemSelected(item);
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
        ListView PainsItems = saveView.findViewById(R.id.filter_pains_list);

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsCourserCheckboxAdapter painsCbAdapter = helper.readPainsWithCheckboxFromDB(context);
        PainsItems.setAdapter(painsCbAdapter);

        registerDatePickerListeners(saveView);

        // add apply button
        builder.setPositiveButton(context.getString(R.string.button_ok), (dialog, which) -> {
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
        DiaryCourserTreeAdapter adapter = helper.readDiaryGroupFromDB(context, selection, selectionArgs);
        DiaryItems.setAdapter(adapter);
    }

    private void openItemUpdateDialog(long groupId) {

        Context context = getActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_edit_item));

        // set the edit layout
        final View editView = getLayoutInflater().inflate(R.layout.fragment_diary_edit_dialog, null);
        builder.setView(editView);

        // create new edit helper
        DiaryEditViewHelper helper = new DiaryEditViewHelper(getActivity(), editView, groupId);

        builder.setPositiveButton(context.getString(R.string.save_button), (dialog, which) -> {
            helper.updateDataBase();
            readFromDB(null, null);
        });
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        helper.populateView();
        dialog.show();
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