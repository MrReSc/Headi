package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.headi.R;

import java.util.Date;
import java.util.Locale;

public class DiaryCourserAdapter extends CursorAdapter {

    public DiaryCourserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_diary_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView diary_date = (TextView) view.findViewById(R.id.diary_date);
        TextView diary_pain_start = (TextView) view.findViewById(R.id.diary_pain_start);
        TextView diary_pain_end = (TextView) view.findViewById(R.id.diary_pain_end);
        TextView diary_pain_name = (TextView) view.findViewById(R.id.diary_pain_name);
        TextView diary_pain_duration = (TextView) view.findViewById(R.id.diary_pain_duration);
        ImageView diary_region = (ImageView) view.findViewById(R.id.diary_region);
        TextView diary_description = (TextView) view.findViewById(R.id.diary_description);
        TextView diary_medication = (TextView) view.findViewById(R.id.diary_medication);
        ProgressBar diary_strength = (ProgressBar) view.findViewById(R.id.diary_strength);
        TextView diary_strength_text = (TextView) view.findViewById(R.id.diary_strength_text);

        // Extract properties from cursor
        SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
        String date = getFormattedTime(date_formatter,
                cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String pain_start = getFormattedTime(formatter,
                cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        String pain_end = getFormattedTime(formatter,
                cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));

        Long s = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION))) / 1000;
        String pain_duration = String.format(Locale.getDefault(), "%02dH %02dM", s / 3600, (s % 3600) / 60);

        String pain_name = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));

        byte[] region_blob = cursor.getBlob(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_REGION));
        Bitmap region = BitmapFactory.decodeByteArray(region_blob, 0, region_blob.length);

        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));
        String medication = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION));
        String medication_amount = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));
        String strength = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_STRENGTH));

        // Populate fields with extracted properties
        diary_date.setText(date);
        diary_pain_start.setText(pain_start);
        diary_pain_end.setText(pain_end);
        diary_pain_name.setText(pain_name);
        diary_pain_duration.setText(pain_duration);
        diary_region.setImageBitmap(region);
        diary_description.setText(description);
        diary_medication.setText(medication_amount + " " + context.getString(R.string.pieces) + " " + medication);
        diary_strength.setProgress(Integer.parseInt(strength));
        diary_strength_text.setText(strength + " / 10");
    }

    private String getFormattedTime(SimpleDateFormat formatter, String date) {
        Long dateAsLong = Long.parseLong(date);
        return formatter.format(new Date(dateAsLong));
    }

}
