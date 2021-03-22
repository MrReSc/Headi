package com.headi.app.db;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.headi.app.R;

import java.util.Locale;

@SuppressWarnings("SpellCheckingInspection")
public class DiaryEditViewHelper {

    private final Context context;
    private final View view;
    private final long groupId;
    private final SimpleDateFormat df = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private long fromDateEdited = 0;
    private long toDateEdited = 0;
    private long fromTimeEdited = 0;
    private long toTimeEdited = 0;
    private Cursor cursor;
    private final String FROM_TIME = "from_time";
    private final String TO_TIME = "to_time";
    private Spinner pain_spinner;
    private TextView diary_edit_description;
    private TextView diary_edit_medication_amount;
    private Spinner medication_spinner;
    private long fromDateAndTimeUnedited;
    private long toDateAndTimeUnedited;
    private Calendar fromCalUnedited;
    private Calendar toCalUnedited;
    private SeekBar diary_edit_strength;
    private int med_selection;

    public DiaryEditViewHelper(Context context, View view, long groupId) {
        this.context = context;
        this.view = view;
        this.groupId = groupId;
    }

    public void populateView() {
        // Get cursor for groupId
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        cursor = helper.getDiaryDataForGroupId(context, Long.toString(groupId));
        cursor.moveToFirst();

        // Pain spinner
        pain_spinner = view.findViewById(R.id.diary_edit_pain);
        PainsCourserIconAdapter pains_adapter = helper.readPainsWithIconFromDB(context);
        String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));
        int selection = getPainIndex(pains_adapter, pain);
        if (selection > -1) {
            pain_spinner.setAdapter(pains_adapter);
            pain_spinner.setSelection(selection);
        }
        else {
            String[] arraySpinner = new String[] {context.getString(R.string.pain_na)};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pain_spinner.setAdapter(adapter);
            pain_spinner.setEnabled(false);
            pain_spinner.setClickable(false);
        }

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
        diary_edit_description = view.findViewById(R.id.diary_edit_description);
        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));
        diary_edit_description.setText(description);

        // Medication spinner
        medication_spinner = view.findViewById(R.id.diary_edit_medication);
        MedicationsCourserAdapter medications_adapter = helper.readMedicationsWithoutIconFromDB(context);
        String medication = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION));
        medication_spinner.setAdapter(medications_adapter);
        med_selection = getMedicationIndex(medications_adapter, medication);
        medication_spinner.setSelection(med_selection);

        //Medication amount
        diary_edit_medication_amount = view.findViewById(R.id.diary_edit_medication_amount);
        String medication_amount = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));
        diary_edit_medication_amount.setText(med_selection > -1 ? medication_amount : "0");

        // increase and decrease medication amount buttons
        ImageView button_increase = view.findViewById(R.id.button_increase);
        ImageView button_decrease = view.findViewById(R.id.button_decrease);
        button_increase.setOnClickListener(v -> increaseMedicationAmount());
        button_decrease.setOnClickListener(v -> decreaseMedicationAmount());
        if (medication_spinner.getAdapter().getCount() == 0) {
            button_increase.setEnabled(false);
            button_decrease.setEnabled(false);
        }

        // Strength
        diary_edit_strength = view.findViewById(R.id.diary_edit_strength);
        TextView diary_edit_strength_text = view.findViewById(R.id.diary_edit_strength_text);
        int strength = cursor.getInt(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_STRENGTH));
        diary_edit_strength.setProgress(strength);
        diary_edit_strength_text.setText(context.getString(R.string.strength_of_10, Integer.toString(strength)));

        diary_edit_strength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                diary_edit_strength_text.setText(context.getString(R.string.strength_of_10, Integer.toString(diary_edit_strength.getProgress())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) + 1;
        diary_edit_medication_amount.setText(String.format(Locale.getDefault(), "%d", newVal));
    }

    private void decreaseMedicationAmount() {
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) - 1;
        diary_edit_medication_amount.setText(newVal < 0 ? "0" : Integer.toString(newVal));
    }

    private void registerDateAndTimePickerListeners(TextView fromDate, TextView toDate, TextView fromTime, TextView toTime) {

        fromCalUnedited = Calendar.getInstance();
        fromDateAndTimeUnedited = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE));
        fromCalUnedited.setTimeInMillis(fromDateAndTimeUnedited);

        toCalUnedited = Calendar.getInstance();
        toDateAndTimeUnedited = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE));
        toCalUnedited.setTimeInMillis(toDateAndTimeUnedited);

        // Date picker
        DatePickerDialog fromDatePickerDialog = new DatePickerDialog(context, (view12, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 0,0,0);
            fromDate.setText(df.format(newDate.getTime()));
            fromDateEdited = newDate.getTimeInMillis();
        }, fromCalUnedited.get(Calendar.YEAR), fromCalUnedited.get(Calendar.MONTH), fromCalUnedited.get(Calendar.DAY_OF_MONTH));

        DatePickerDialog toDatePickerDialog = new DatePickerDialog(context, (view1, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
            toDate.setText(df.format(newDate.getTime()));
            toDateEdited = newDate.getTimeInMillis();
        }, toCalUnedited.get(Calendar.YEAR), toCalUnedited.get(Calendar.MONTH), toCalUnedited.get(Calendar.DAY_OF_MONTH));

        fromDate.setOnClickListener(v -> {
            // TODO erneutes setzten des MaxDate funktioniert nicht
            fromDatePickerDialog.getDatePicker().setMaxDate(toDateEdited != 0 ? toDateEdited : toDateAndTimeUnedited);
            fromDatePickerDialog.show();
        });

        toDate.setOnClickListener(v -> {
            // TODO erneutes setzten des MinDate funktioniert nicht
            toDatePickerDialog.getDatePicker().setMinDate(fromDateEdited != 0 ? fromDateEdited : fromDateAndTimeUnedited);
            Calendar cal = Calendar.getInstance();
            toDatePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
            toDatePickerDialog.show();
        });

        // Time picker
        TimePickerDialog fromTimePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(0, 0, 0, hourOfDay, minute,0);
            long newTime = ((hourOfDay * 3600) + (minute * 60)) * 1000;
            // Check for valid time
            checkTimeForValidity(fromDatePickerDialog, newTime, fromTime, newDate, FROM_TIME);
        }, fromCalUnedited.get(Calendar.HOUR_OF_DAY), fromCalUnedited.get(Calendar.MINUTE), true);

        fromTime.setOnClickListener(v -> fromTimePickerDialog.show());

        TimePickerDialog toTimePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(0, 0, 0, hourOfDay, minute,0);
            long newTime = ((hourOfDay * 3600) + (minute * 60)) * 1000;
            // Check for valid time
            checkTimeForValidity(toDatePickerDialog, newTime, toTime, newDate, TO_TIME);
        }, toCalUnedited.get(Calendar.HOUR_OF_DAY), toCalUnedited.get(Calendar.MINUTE), true);

        toTime.setOnClickListener(v -> toTimePickerDialog.show());
    }

    private void checkTimeForValidity(DatePickerDialog datePicker, long newTime, TextView timeTextView, Calendar newDate, String whichTime) {

        int day = datePicker.getDatePicker().getDayOfMonth();
        int month = datePicker.getDatePicker().getMonth();
        int year =  datePicker.getDatePicker().getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0);

        long newTimeStamp = calendar.getTimeInMillis() + newTime;
        long startDate = getCalcEditedDateAndTime(fromDateEdited, fromTimeEdited, fromDateAndTimeUnedited, fromCalUnedited);
        long endDate = getCalcEditedDateAndTime(toDateEdited, toTimeEdited, toDateAndTimeUnedited, toCalUnedited);

        if (newTimeStamp < endDate && whichTime.equals(FROM_TIME)) {
            timeTextView.setText(tf.format(newDate.getTime()));
            fromTimeEdited = newTime;
        }
        else if (newTimeStamp > startDate && whichTime.equals(TO_TIME)) {
            timeTextView.setText(tf.format(newDate.getTime()));
            toTimeEdited = newTime;
        }
        else {
            Toast.makeText(context, context.getString(R.string.new_time_not_valid), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateDataBase() {
        // Save to DB
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        String selection = HeadiDBContract.Diary._ID + " = ?";
        String[] selectionArgs = {Long.toString(groupId)};

        long startDate = getCalcEditedDateAndTime(fromDateEdited, fromTimeEdited, fromDateAndTimeUnedited, fromCalUnedited);
        long endDate = getCalcEditedDateAndTime(toDateEdited, toTimeEdited, toDateAndTimeUnedited, toCalUnedited);

        String diaryMedication = "";
        if (medication_spinner.getAdapter().getCount() > 0) {
            diaryMedication = ((Cursor) medication_spinner.getSelectedItem()).getString(1);
        }

        int diaryMedicationAmount = Integer.parseInt(diary_edit_medication_amount.getText().toString());

        if (diaryMedicationAmount == 0 && med_selection > -1) {
            diaryMedication = "";
        }

        if (diaryMedicationAmount > 0 || med_selection > -1) {
            values.put(HeadiDBContract.Diary.COLUMN_MEDICATION, diaryMedication);
            values.put(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT, diaryMedicationAmount);
        }

        values.put(HeadiDBContract.Diary.COLUMN_START_DATE, startDate);
        values.put(HeadiDBContract.Diary.COLUMN_END_DATE, endDate);
        values.put(HeadiDBContract.Diary.COLUMN_DURATION, endDate - startDate);
        values.put(HeadiDBContract.Diary.COLUMN_DESCRIPTION, diary_edit_description.getText().toString());
        values.put(HeadiDBContract.Diary.COLUMN_STRENGTH, diary_edit_strength.getProgress());

        try {
            String pain = ((Cursor) pain_spinner.getSelectedItem()).getString(1);
            values.put(HeadiDBContract.Diary.COLUMN_PAIN, pain);
        } catch (Exception e) {
            e.printStackTrace();
        }

        database.update(HeadiDBContract.Diary.TABLE_NAME, values, selection, selectionArgs);

        Toast.makeText(context, context.getString(R.string.item_saved), Toast.LENGTH_SHORT).show();

    }

    private long getCalcEditedDateAndTime(long dateEdited, long timeEdited, long dateAndTimeUnedited, Calendar calUnedited) {
        if (dateEdited != 0 || timeEdited != 0) {
            if (dateEdited != 0 && timeEdited != 0) {
                return dateEdited + timeEdited;
            }

            if (dateEdited != 0) {
                long hour = calUnedited.get(Calendar.HOUR_OF_DAY) * 3600 * 1000;
                long minute = calUnedited.get(Calendar.MINUTE) * 60 * 1000;
                return dateEdited + (hour + minute);
            }

            int year = calUnedited.get(Calendar.YEAR);
            int month = calUnedited.get(Calendar.MONTH);
            int day = calUnedited.get(Calendar.DAY_OF_MONTH);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0);

            return timeEdited + calendar.getTimeInMillis();
        }
        return dateAndTimeUnedited;
    }
}
