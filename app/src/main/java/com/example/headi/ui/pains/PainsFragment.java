package com.example.headi.ui.pains;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class PainsFragment extends Fragment {

    private View view;
    private ListView PainsItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        view = inflater.inflate(R.layout.fragment_pains_medications, container, false);
        PainsItems = (ListView) view.findViewById(R.id.pains_list);

        registerListeners(context);
        readFromDB();
        return view;
    }

    private void saveToDB() {
        Context context = requireActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText mEdit = (EditText) view.findViewById(R.id.pains_add_new_pain);
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, mEdit.getText().toString());
        database.insert(HeadiDBContract.Pains.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_item_added), Toast.LENGTH_SHORT).show();
        mEdit.setText("");
        readFromDB();
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsItems.setAdapter(helper.readPainsFromDB(context));
    }

    private void deleteFromDB(long id) {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();

        String selection = HeadiDBContract.Pains._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};

        database.delete(HeadiDBContract.Pains.TABLE_NAME, selection, selectionArgs);
        readFromDB();
    }

    private void registerListeners(Context context) {
        // Add Button listener
        final Button button = view.findViewById(R.id.pains_add_button);
        button.setOnClickListener(v -> saveToDB());

        // Find ListView to populate
        PainsItems.setOnItemLongClickListener((adapterView, view, position, id) -> {

            new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.action_delete))
                    .setMessage(context.getString(R.string.delete_pains))
                    .setPositiveButton(context.getString(R.string.delete_button),
                            (dialogInterface, i) -> deleteFromDB(id))
                    .setNegativeButton(context.getString(R.string.cancel_button), (dialogInterface, i) -> {
                    })
                    .show();
            return true;
        });

    }
}