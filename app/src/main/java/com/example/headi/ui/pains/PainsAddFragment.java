package com.example.headi.ui.pains;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PainsAddFragment extends Fragment {

    private PainsAddViewModel mViewModel;
    private View view;

    public static PainsAddFragment newInstance() {
        return new PainsAddFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_pains_add, container, false);

        // Save Button listener
        final Button button = view.findViewById(R.id.pains_save_button);
        button.setOnClickListener(v -> saveToDB());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PainsAddViewModel.class);
        // TODO: Use the ViewModel
    }

    private void saveToDB() {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText mEdit = (EditText)view.findViewById(R.id.pains_new_pain);
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, mEdit.getText().toString());
        database.insert(HeadiDBContract.Pains.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_pains_added), Toast.LENGTH_LONG).show();

        // Navigate Back
        Navigation.findNavController(view).navigateUp();

    }
}