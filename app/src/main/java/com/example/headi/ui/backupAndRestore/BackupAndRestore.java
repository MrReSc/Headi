package com.example.headi.ui.backupAndRestore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.HeadiDBSQLiteHelper;


public class BackupAndRestore extends Fragment {

    private View view;
    private HeadiDBSQLiteHelper helper;
    private static final int FILE_SELECT_CODE = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_backup_restore, container, false);
        helper = new HeadiDBSQLiteHelper(getActivity());
        registerListeners();
        return view;
    }

    private void registerListeners() {

        view.findViewById(R.id.button_backup).setOnClickListener(v -> helper.performBackup(getActivity()));

        view.findViewById(R.id.button_restore).setOnClickListener(v -> {
            Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFileIntent.setType("*/*");
            chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
            startActivityForResult(chooseFileIntent, FILE_SELECT_CODE);
        });

        view.findViewById(R.id.button_export).setOnClickListener(v -> {
            // TODO Auto-generated method stub
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == -1) {
                Uri fileUri = data.getData();
                helper.performRestore(getActivity(), fileUri);
            }
        }
    }
}