package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.example.headi.R;

public class PainsCourserAdapter extends CursorAdapter {

    public PainsCourserAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_pains_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView pains_name = (TextView) view.findViewById(R.id.pains_name);

        // Extract properties from cursor
        String pain = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Pains.COLUMN_PAIN));

        // Populate fields with extracted properties
        pains_name.setText(pain);
    }
}
