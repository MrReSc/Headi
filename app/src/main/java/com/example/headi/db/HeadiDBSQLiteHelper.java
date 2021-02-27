package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public DiaryCourserAdapter readDiaryFromDB(Context context, String selection, String[] selectionArgs) {
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Diary._ID,
                HeadiDBContract.Diary.COLUMN_DESCRIPTION,
                HeadiDBContract.Diary.COLUMN_DURATION,
                HeadiDBContract.Diary.COLUMN_END_DATE,
                HeadiDBContract.Diary.COLUMN_PAIN,
                HeadiDBContract.Diary.COLUMN_STRENGTH,
                HeadiDBContract.Diary.COLUMN_REGION,
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

        // Setup cursor adapter using cursor from last step
        return new DiaryCourserAdapter(context, cursor, 0);
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

}
