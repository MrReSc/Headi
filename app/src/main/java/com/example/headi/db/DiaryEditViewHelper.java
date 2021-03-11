package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.headi.R;

import java.util.Locale;

public class DiaryEditViewHelper {

    private Context context;
    private View view;
    private long groupId;

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

        // Date formatter
        SimpleDateFormat df = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());

        // Time formatter
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // From time and date
        TextView diary_edit_from_date = (TextView) view.findViewById(R.id.diary_edit_from_date);
        TextView diary_edit_from_time = (TextView) view.findViewById(R.id.diary_edit_from_time);
        String from_date = df.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        String from_time = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        diary_edit_from_date.setText(from_date);
        diary_edit_from_time.setText(from_time);

        // To time and date
        TextView diary_edit_to_date = (TextView) view.findViewById(R.id.diary_edit_to_date);
        TextView diary_edit_to_time = (TextView) view.findViewById(R.id.diary_edit_to_time);
        String to_date = df.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));
        String to_time = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));
        diary_edit_to_date.setText(to_date);
        diary_edit_to_time.setText(to_time);

        // Description
        TextView diary_edit_description = (TextView) view.findViewById(R.id.diary_edit_description);
        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));
        diary_edit_description.setText(description);

        //Medication amount
        TextView diary_edit_medication_amount = (TextView) view.findViewById(R.id.diary_edit_medication_amount);
        String medication_amount = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));
        diary_edit_medication_amount.setText(medication_amount);

        // increase and decrease medication amount buttons
        ImageView button_increase = view.findViewById(R.id.button_increase);
        ImageView button_decrease = view.findViewById(R.id.button_decrease);
        button_increase.setOnClickListener(v -> increaseMedicationAmount(view));
        button_decrease.setOnClickListener(v -> decreaseMedicationAmount(view));

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

    private void increaseMedicationAmount(View view) {
        TextView diary_edit_medication_amount = (TextView) view.findViewById(R.id.diary_edit_medication_amount);
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) + 1;
        diary_edit_medication_amount.setText(String.format(Locale.getDefault(), "%d", newVal));
    }

    private void decreaseMedicationAmount(View view) {
        TextView diary_edit_medication_amount = (TextView) view.findViewById(R.id.diary_edit_medication_amount);
        int newVal = Integer.parseInt(diary_edit_medication_amount.getText().toString()) - 1;
        diary_edit_medication_amount.setText(newVal < 0 ? "0" : Integer.toString(newVal));
    }

}
