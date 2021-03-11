package com.example.headi.db;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.headi.R;

import java.util.Locale;

public class DiaryEditViewHelper {

    private Context context;
    private View view;
    private long groupId;
    private SimpleDateFormat df = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
    private SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private String fromDateEdited;
    private String toDateEdited;
    private String fromTimeEdited;
    private String toTimeEdited;

    public DiaryEditViewHelper(Context context, View view, long groupId) {
        this.context = context;
        this.view = view;
        this.groupId = groupId;
    }

    public void populateView() {
        // Get cursor for groupId
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        Cursor cursor = helper.getDiaryDataForGroupId(context, Long.toString(groupId));
        cursor.moveToFirst();

        // Pain spinner
        Spinner pain_spinner = view.findViewById(R.id.diary_edit_pain);
        PainsCourserIconAdapter pains_adapter = helper.readPainsWithIconFromDB(context);
        pain_spinner.setAdapter(pains_adapter);
        String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));
        pain_spinner.setSelection(getPainIndex(pains_adapter, pain));

        // From time and date
        TextView diary_edit_from_date = view.findViewById(R.id.diary_edit_from_date);
        TextView diary_edit_from_time = view.findViewById(R.id.diary_edit_from_time);
        String from_date = df.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        String from_time = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        diary_edit_from_date.setText(from_date);
        diary_edit_from_time.setText(from_time);

        // To time and date
        TextView diary_edit_to_date = view.findViewById(R.id.diary_edit_to_date);
        TextView diary_edit_to_time = view.findViewById(R.id.diary_edit_to_time);
        String to_date = df.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));
        String to_time = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));
        diary_edit_to_date.setText(to_date);
        diary_edit_to_time.setText(to_time);

        registerDateAndTimePickerListeners(diary_edit_from_date, diary_edit_to_date, diary_edit_from_time, diary_edit_to_time);

        // Description
        TextView diary_edit_description = view.findViewById(R.id.diary_edit_description);
        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));
        diary_edit_description.setText(description);

        //Medication amount
        TextView diary_edit_medication_amount = view.findViewById(R.id.diary_edit_medication_amount);
        String medication_amount = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));
        diary_edit_medication_amount.setText(medication_amount);

        // increase and decrease medication amount buttons
        ImageView button_increase = view.findViewById(R.id.button_increase);
        ImageView button_decrease = view.findViewById(R.id.button_decrease);
        button_increase.setOnClickListener(v -> increaseMedicationAmount());
        button_decrease.setOnClickListener(v -> decreaseMedicationAmount());

        // Medication spinner
        Spinner medication_spinner = view.findViewById(R.id.diary_edit_medication);
        MedicationsCourserAdapter medications_adapter = helper.readMedicationsWithoutIconFromDB(context);
        medication_spinner.setAdapter(medications_adapter);
        String medication = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION));
        medication_spinner.setSelection(getMedicationIndex(medications_adapter, medication));


    }

    private int getPainIndex(PainsCourserIconAdapter adapter, String searchString) {
        if (searchString != null && adapter.getCount() != 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Cursor cursor = (Cursor) adapter.getItem(i);
                String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Pains.COLUMN_PAIN));
                if (pain.equals(searchString)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getMedicationIndex(MedicationsCourserAdapter adapter, String searchString) {
        if (searchString != null && adapter.getCount() != 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Cursor cursor = (Cursor) adapter.getItem(i);
                String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Medication.COLUMN_MEDICATION));
                if (pain.equals(searchString)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void increaseMedicationAmount() {
        TextView diary_edit_medication_amount = view.findViewById(R.id.diary_edit_medication_amount);
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) + 1;
        diary_edit_medication_amount.setText(String.format(Locale.getDefault(), "%d", newVal));
    }

    private void decreaseMedicationAmount() {
        TextView diary_edit_medication_amount = view.findViewById(R.id.diary_edit_medication_amount);
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) - 1;
        diary_edit_medication_amount.setText(newVal < 0 ? "0" : Integer.toString(newVal));
    }

    private void registerDateAndTimePickerListeners(TextView fromDate, TextView toDate, TextView fromTime, TextView toTime) {

        Calendar newCalendar = Calendar.getInstance();

        // Date picker
        DatePickerDialog fromDatePickerDialog = new DatePickerDialog(context, (view12, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 0,0,0);
            fromDate.setText(df.format(newDate.getTime()));
            fromDateEdited = Long.toString(newDate.getTimeInMillis());
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDate.setOnClickListener(v -> fromDatePickerDialog.show());

        DatePickerDialog toDatePickerDialog = new DatePickerDialog(context, (view1, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
            toDate.setText(df.format(newDate.getTime()));
            toDateEdited = Long.toString(newDate.getTimeInMillis());
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDate.setOnClickListener(v -> toDatePickerDialog.show());

        // Time picker
        TimePickerDialog fromTimePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(0, 0, 0, hourOfDay, minute,0);
            fromTime.setText(tf.format(newDate.getTime()));
            long mills = (hourOfDay * 3600) + (minute * 60) * 1000;
            fromTimeEdited = Long.toString(mills);
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        fromTime.setOnClickListener(v -> fromTimePickerDialog.show());

        TimePickerDialog toTimePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(0, 0, 0, hourOfDay, minute,0);
            toTime.setText(tf.format(newDate.getTime()));
            long mills = (hourOfDay * 3600) + (minute * 60) * 1000;
            toTimeEdited = Long.toString(mills);
        },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

        toTime.setOnClickListener(v -> toTimePickerDialog.show());
    }
}
