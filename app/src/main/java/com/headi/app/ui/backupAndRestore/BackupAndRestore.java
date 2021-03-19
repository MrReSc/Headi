package com.headi.app.ui.backupAndRestore;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.headi.app.R;
import com.headi.app.db.HeadiDBSQLiteHelper;

import java.util.Date;
import java.util.Locale;


public class BackupAndRestore extends Fragment {

    private View view;
    private HeadiDBSQLiteHelper helper;
    private static final int FILE_SELECT_DB_CODE = 0;
    private static final int FILE_CREATE_DB_CODE = 1;
    private static final int FILE_CREATE_CSV_CODE = 2;

    private String outFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_backup_restore, container, false);
        helper = new HeadiDBSQLiteHelper(getActivity());
        registerListeners();
        return view;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void registerListeners() {

        view.findViewById(R.id.button_backup).setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            outFileName = sdf.format(new Date()) + "_headi_backup.db";

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/x-sqlite3");
            intent.putExtra(Intent.EXTRA_TITLE, outFileName);

            startActivityForResult(intent, FILE_CREATE_DB_CODE);
        });

        view.findViewById(R.id.button_restore).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent = Intent.createChooser(intent, getActivity().getString(R.string.choose_backup));

            startActivityForResult(intent, FILE_SELECT_DB_CODE);
        });

        view.findViewById(R.id.button_export).setOnClickListener(v -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            outFileName = sdf.format(new Date()) + "_headi_diary.csv";

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, outFileName);

            startActivityForResult(intent, FILE_CREATE_CSV_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == FILE_CREATE_DB_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                helper.performBackup(getActivity(), uri, outFileName);
            }
        }

        if (requestCode == FILE_SELECT_DB_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                helper.performRestore(getActivity(), uri);
            }
        }

        if (requestCode == FILE_CREATE_CSV_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                helper.performCsvExport(getActivity(), uri, outFileName);
            }
        }
    }
}