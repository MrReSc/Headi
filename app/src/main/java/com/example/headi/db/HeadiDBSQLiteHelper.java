package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.headi.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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

        // Setup cursor adapter using cursor from last step
        return database.query(
                HeadiDBContract.Pains.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );
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

        return database.query(
                HeadiDBContract.Medication.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );
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

        return database.query(
                HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                null                              // sort
        );
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
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(HeadiDBContract.Pains._ID));
        cursor.close();
        return id;
    }

    public void performCsvExport(Context context, Uri path, String name) {

        try {

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

            String orderBy = HeadiDBContract.Diary.COLUMN_START_DATE + " ASC";

            Cursor cursor = database.query(
                    HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                    projection,                               // The columns to return
                    null,                            // The columns for the WHERE clause
                    null,                         // The values for the WHERE clause
                    null,                             // don't group the rows
                    null,                              // don't filter by row groups
                    orderBy                                   // sort
            );

            OutputStream output = context.getContentResolver().openOutputStream(path);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

            String header = HeadiDBContract.Diary._ID + "," +
                    HeadiDBContract.Diary.COLUMN_PAIN + "," +
                    HeadiDBContract.Diary.COLUMN_START_DATE + "," +
                    HeadiDBContract.Diary.COLUMN_END_DATE + "," +
                    HeadiDBContract.Diary.COLUMN_DURATION + "," +
                    HeadiDBContract.Diary.COLUMN_STRENGTH + "," +
                    HeadiDBContract.Diary.COLUMN_MEDICATION + "," +
                    HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT;

            writer.write(header);
            writer.newLine();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary._ID)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_PAIN)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_START_DATE)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_END_DATE)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_DURATION)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_STRENGTH)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION)) + "," +
                        cursor.getString(cursor.getColumnIndexOrThrow(HeadiDBContract.Diary.COLUMN_MEDICATION_AMOUNT));

                writer.write(data);
                writer.newLine();
            }

            writer.close();
            Toast.makeText(context, context.getString(R.string.export_complete) + "\n" + name, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.export_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void performBackup(Context context, Uri path, String name) {
        final String inFileName = context.getDatabasePath(DATABASE_NAME).toString();

        try {

            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            OutputStream output = context.getContentResolver().openOutputStream(path);

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

            Toast.makeText(context, context.getString(R.string.backup_complete) + "\n" + name, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.backup_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void performRestore(Context context, Uri inFileUri) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_restore));
        builder.setMessage(context.getString(R.string.ask_for_restore_database));

        builder.setPositiveButton(context.getString(R.string.button_restore), (dialog, which) -> restore(context, inFileUri));

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void restore(Context context, Uri path) {

        final String outFileName = context.getDatabasePath(DATABASE_NAME).toString();

        try {

            InputStream fis = context.getContentResolver().openInputStream(path);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

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

            Toast.makeText(context, context.getString(R.string.restore_complete), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.restore_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
