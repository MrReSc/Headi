package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.headi.R;

import java.util.Date;

public class DiaryCourserAdapter extends CursorAdapter {

    public DiaryCourserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_stats_item, parent, false);
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

        // Extract properties from cursor
        SimpleDateFormat date_formatter = new SimpleDateFormat("E dd. MMM yyyy");
        String date = getFormattedTime(date_formatter, cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String pain_start = getFormattedTime(formatter, cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        String pain_end = getFormattedTime(formatter, cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));

        Long durationAsLong = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION)));
        String pain_duration = DateUtils.formatElapsedTime(durationAsLong / 1000);

        String pain_name = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));

        byte[] region_blob = cursor.getBlob(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_REGION));
        Bitmap region = BitmapFactory.decodeByteArray(region_blob,0,region_blob.length);

        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));

        // Populate fields with extracted properties
        diary_date.setText(date);
        diary_pain_start.setText(pain_start);
        diary_pain_end.setText(pain_end);
        diary_pain_name.setText(pain_name);
        diary_pain_duration.setText(pain_duration);
        diary_region.setImageBitmap(region);
        diary_description.setText(description);
    }

    private String getFormattedTime(SimpleDateFormat formatter, String date) {
        Long dateAsLong = Long.parseLong(date);
        return formatter.format(new Date(dateAsLong));
    }

}
