package com.example.headi.ui.pains;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.example.headi.db.PainsCurserAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class PainsFragment extends Fragment {

    private PainsViewModel painsViewModel;
    private View view;
    private ListView PainsItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        painsViewModel = new ViewModelProvider(this).get(PainsViewModel.class);
        view = inflater.inflate(R.layout.fragment_pains, container, false);

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        registerListeners(context);
        readFromDB();
        return view;
    }

    private void saveToDB() {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText mEdit = (EditText)view.findViewById(R.id.pains_new_pain);
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, mEdit.getText().toString());
        database.insert(HeadiDBContract.Pains.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_pains_added), Toast.LENGTH_LONG).show();
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
        // Save Button listener
        final Button button = view.findViewById(R.id.pains_save_button);
        button.setOnClickListener(v -> saveToDB());

        // Find ListView to populate
        PainsItems = (ListView) view.findViewById(R.id.pains_list);
        PainsItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                new MaterialAlertDialogBuilder(context)
                        .setTitle(context.getString(R.string.action_delete))
                        .setMessage(context.getString(R.string.delete_pains) + "Position = " + position + " | id = " + id )
                        .setPositiveButton(context.getString(R.string.delete_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteFromDB(id);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            }
        });

    }
}