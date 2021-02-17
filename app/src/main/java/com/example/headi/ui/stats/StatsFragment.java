package com.example.headi.ui.stats;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StatsFragment extends Fragment {

    private StatsViewModel statsViewModel;
    private View view;
    private ListView DiaryItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        Context context = getActivity();
        statsViewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        view = inflater.inflate(R.layout.fragment_stats, container, false);
        DiaryItems = (ListView) view.findViewById(R.id.diary_list);

        registerListeners(context);
        readFromDB();
        
        return view;
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        DiaryItems.setAdapter(helper.readDiaryFromDB(context));
    }

    private void registerListeners(Context context) {
        // Find ListView to populate
        DiaryItems.setOnItemLongClickListener((adapterView, view, position, id) -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.action_delete))
                    .setMessage(context.getString(R.string.delete_diary) + "Position = " + position + " | id = " + id)
                    .setPositiveButton(context.getString(R.string.delete_button), (dialogInterface, i) -> deleteFromDB(id))
                    .setNegativeButton(context.getString(R.string.cancel_button), (dialogInterface, i) -> { })
                    .show();
            return true;
        });

    }

    private void deleteFromDB(long id) {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();

        String selection = HeadiDBContract.Diary._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};

        database.delete(HeadiDBContract.Diary.TABLE_NAME, selection, selectionArgs);
        readFromDB();
    }
}