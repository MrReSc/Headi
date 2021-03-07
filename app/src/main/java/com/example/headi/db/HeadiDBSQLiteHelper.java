package com.example.headi.db;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.headi.MainActivity;
import com.example.headi.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class HeadiDBSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "headi_database";

    public HeadiDBSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(HeadiDBContract.Pains.CREATE_TABLE);
        sqLiteDatabase.execSQL(HeadiDBContract.Diary.CREATE_TABLE);
        sqLiteDatabase.execSQL(HeadiDBContract.Medication.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeadiDBContract.Pains.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeadiDBContract.Diary.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeadiDBContract.Medication.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public PainsCourserIconAdapter readPainsWithIconFromDB(Context context) {
        // Setup cursor adapter using cursor from last step
        return new PainsCourserIconAdapter(context, readPainsFromDB(context), 0);
    }

    public PainsCourserCheckboxAdapter readPainsWithCheckboxFromDB(Context context) {
        // Setup cursor adapter using cursor from last step
        return new PainsCourserCheckboxAdapter(context, readPainsFromDB(context), 0);
    }

    private Cursor readPainsFromDB (Context context) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Pains._ID,
                HeadiDBContract.Pains.COLUMN_PAIN,
        };

        String orderBy = HeadiDBContract.Pains.COLUMN_PAIN + " ASC";

        Cursor cursor = database.query(
                HeadiDBContract.Pains.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );

        // Setup cursor adapter using cursor from last step
        return cursor;
    }

    public MedicationsIconCourserAdapter readMedicationsWithIconFromDB(Context context) {
        // Setup cursor adapter using cursor from last step
        return new MedicationsIconCourserAdapter(context, readMedicationsFromDB(context), 0);
    }

    public MedicationsCourserAdapter readMedicationsWithoutIconFromDB(Context context) {
        // Setup cursor adapter using cursor from last step
        return new MedicationsCourserAdapter(context, readMedicationsFromDB(context), 0);
    }

    private Cursor readMedicationsFromDB (Context context) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Medication._ID,
                HeadiDBContract.Medication.COLUMN_MEDICATION,
        };

        String orderBy = HeadiDBContract.Medication.COLUMN_MEDICATION + " ASC";

        Cursor cursor = database.query(
                HeadiDBContract.Medication.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );

        return cursor;
    }

    public DiaryCourserTreeAdapter readDiaryGroupFromDB(Context context, String selection, String[] selectionArgs) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Diary._ID,
                HeadiDBContract.Diary.COLUMN_DURATION,
                HeadiDBContract.Diary.COLUMN_END_DATE,
                HeadiDBContract.Diary.COLUMN_PAIN,
                HeadiDBContract.Diary.COLUMN_START_DATE,
        };

        String orderBy = HeadiDBContract.Diary.COLUMN_START_DATE + " DESC";

        Cursor cursor = database.query(
                HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );

        // Setup cursor adapter using cursor from last step
        return new DiaryCourserTreeAdapter(cursor, context);
    }

    public Cursor getDiaryChildrenCursor(Context context, String id) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Diary._ID,
                HeadiDBContract.Diary.COLUMN_REGION,
                HeadiDBContract.Diary.COLUMN_STRENGTH,
                HeadiDBContract.Diary.COLUMN_MEDICATION,
                HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT,
                HeadiDBContract.Diary.COLUMN_DESCRIPTION,
        };

        String selection = HeadiDBContract.Diary._ID + " = ?";
        String[] selectionArgs = {id};

        Cursor cursor = database.query(
                HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                null                              // sort
        );

        return cursor;
    }

    public DiaryStats readDiaryStatsFromDB(Context context, String selection, String[] selectionArgs) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Diary._ID,
                HeadiDBContract.Diary.COLUMN_DURATION,
                HeadiDBContract.Diary.COLUMN_END_DATE,
                HeadiDBContract.Diary.COLUMN_PAIN,
                HeadiDBContract.Diary.COLUMN_STRENGTH,
                HeadiDBContract.Diary.COLUMN_MEDICATION,
                HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT,
                HeadiDBContract.Diary.COLUMN_START_DATE,
        };

        String orderBy = HeadiDBContract.Diary.COLUMN_START_DATE + " DESC";

        Cursor cursor = database.query(
                HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );

        return new DiaryStats(context, cursor);
    }

    public Long getPainIdFromName(Context context, String name) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Pains._ID,
                HeadiDBContract.Pains.COLUMN_PAIN,
        };

        String selection = HeadiDBContract.Pains.COLUMN_PAIN + " = ?";
        String[] selectionArgs = {name};

        Cursor cursor = database.query(
                HeadiDBContract.Pains.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                null                              // sort
        );

        cursor.moveToFirst();
        return cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Pains._ID));
    }

    public void performBackup(Context context) {
        // TODO new Rework to new Storage API
        MainActivity activity = (MainActivity) context;
        verifyStoragePermissions(activity);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String outFileName = sdf.format(new Date()) + ".db";

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name));
        String outFile = Environment.getExternalStorageDirectory() + File.separator + context.getString(R.string.app_name) + File.separator + outFileName;

        boolean success = true;
        if (!folder.exists())
            success = folder.mkdirs();

        if (success) {
            backup(activity, outFileName, outFile);
        } else
            Toast.makeText(activity, context.getString(R.string.create_folder_error), Toast.LENGTH_SHORT).show();
    }

    private void backup(MainActivity activity, String outFileName, String outFile) {

        //database path
        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFile);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            fis.close();

            Toast.makeText(activity, activity.getString(R.string.backup_complete) + "\n" + outFileName, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(activity, activity.getString(R.string.backup_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void performRestore(Context context, Uri inFileUri) {
        // TODO new Rework to new Storage API
        MainActivity activity = (MainActivity) context;
        verifyStoragePermissions(activity);

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_restore));
        builder.setMessage(context.getString(R.string.ask_for_restore_database));

        builder.setPositiveButton(context.getString(R.string.button_restore), (dialog, which) -> {
            restore(context, inFileUri);
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> {
            dialog.dismiss();
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void restore(Context context, Uri inFileUri) {
        // TODO new Rework to new Storage API
        final String outFileName = context.getDatabasePath(DATABASE_NAME).toString();

        final String docId = DocumentsContract.getDocumentId(inFileUri);
        final String[] split = docId.split(":");
        final String type = split[0];
        String inFileName = "";
        if ("primary".equalsIgnoreCase(type)) {
            inFileName = Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            // Transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Close the streams /storage/self/primary/Headi/20210306_060423.db
            output.flush();
            output.close();
            fis.close();

            Toast.makeText(context, context.getString(R.string.restore_complete), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.restore_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_CODE_PERMISSIONS
            );
        }
    }

    // Storage Permissions variables
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_PERMISSIONS = 2;

}
