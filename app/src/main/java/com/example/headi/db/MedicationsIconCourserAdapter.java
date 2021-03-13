package com.example.headi.db;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.ImageViewCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import com.example.headi.Constants;
import com.example.headi.R;

public class MedicationsIconCourserAdapter extends CursorAdapter {

    public MedicationsIconCourserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_pains_mediactions_icon_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView pains_name = view.findViewById(R.id.pains_name);
        ImageView pains_image = view.findViewById(R.id.pains_image);

        // Extract properties from cursor
        String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Medication.COLUMN_MEDICATION));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Medication._ID));
        int color = Constants.getColorById(id);

        // Populate fields with extracted properties
        pains_name.setText(pain);
        pains_image.setImageResource(R.drawable.ic_menu_medication);
        ImageViewCompat.setImageTintList(pains_image, ColorStateList.valueOf(color));
    }
}
