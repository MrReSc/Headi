package com.headi.app.db;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;

import com.headi.app.Constants;
import com.headi.app.R;
import com.headi.app.ui.UiHelper;

import java.util.Locale;

public class DiaryCourserTreeAdapter extends CursorTreeAdapter {

    private final Context context;


    public DiaryCourserTreeAdapter(Cursor cursor, Context context) {
        super(cursor, context);
        this.context = context;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        String groupId = groupCursor.getString(groupCursor.getColumnIndexOrThrow(HeadiDBContract.Diary._ID));
        return helper.getDiaryChildrenCursor(context, groupId);
    }

    @Override
    public long getGroupId(int groupPosition) {
        getCursor().moveToPosition(groupPosition);
        return getCursor().getLong(0);
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_diary_list_group, parent, false);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        // Find fields to populate in inflated template
        TextView diary_date = view.findViewById(R.id.diary_date);
        TextView diary_pain_start = view.findViewById(R.id.diary_pain_start);
        TextView diary_pain_end = view.findViewById(R.id.diary_pain_end);
        TextView diary_pain_name = view.findViewById(R.id.diary_pain_name);
        TextView diary_pain_duration = view.findViewById(R.id.diary_pain_duration);
        ImageView pains_image = view.findViewById(R.id.pains_image);

        // Extract properties from cursor
        SimpleDateFormat df = new SimpleDateFormat("E dd. MMM yyyy", Locale.getDefault());
        String date = df.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));

        SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String pain_start = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)));
        String pain_end = tf.format(cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)));

        long s = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION)) / 1000;
        String pain_duration = String.format(Locale.getDefault(), "%02dH %02dM", s / 3600, (s % 3600) / 60);

        String pain_name = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN));

        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        Long id = helper.getPainIdFromName(context, pain_name);
        int color;

        if (id > -1) {
            color = Constants.getColorById(id);
        }
        else {
            color = UiHelper.getPrimaryTextColor(context);
        }

        // Populate fields with extracted properties
        diary_date.setText(date);
        diary_pain_start.setText(pain_start);
        diary_pain_end.setText(pain_end);
        diary_pain_name.setText(pain_name);
        //diary_pain_name.setTextColor(color);
        ImageViewCompat.setImageTintList(pains_image, ColorStateList.valueOf(color));
        diary_pain_duration.setText(pain_duration);
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_diary_list_child, parent, false);    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        // Find fields to populate in inflated template
        ImageView diary_region = view.findViewById(R.id.diary_region);
        TextView diary_description = view.findViewById(R.id.diary_description);
        TextView diary_medication = view.findViewById(R.id.diary_medication);
        ProgressBar diary_strength = view.findViewById(R.id.diary_strength);
        TextView diary_strength_text = view.findViewById(R.id.diary_strength_text);

        byte[] region_blob = cursor.getBlob(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_REGION));
        Bitmap region = BitmapFactory.decodeByteArray(region_blob, 0, region_blob.length);

        String description = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DESCRIPTION));
        String medication = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION));
        String medication_amount = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));
        String strength = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_STRENGTH));

        // Populate fields with extracted properties
        diary_region.setImageBitmap(region);
        diary_strength.setProgress(Integer.parseInt(strength));
        diary_strength_text.setText(context.getString(R.string.strength_of_10, strength));

        if (description.isEmpty()) {
            diary_description.setText(context.getString(R.string.none));
        }
        else {
            diary_description.setText(description);
        }

        if (medication.isEmpty() || medication_amount.equals("0") || medication_amount.equals("null")) {
            diary_medication.setText(context.getString(R.string.none));
        }
        else {
            diary_medication.setText(context.getString(R.string.pieces, medication_amount, medication));
        }
    }
}
