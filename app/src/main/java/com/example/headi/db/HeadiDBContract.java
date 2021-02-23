package com.example.headi.db;

import android.provider.BaseColumns;

public final class HeadiDBContract {

    private HeadiDBContract() {
    }

    public static class Pains implements BaseColumns {
        public static final String TABLE_NAME = "pains";
        public static final String COLUMN_PAIN = "pain";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PAIN + " TEXT" + ")";
    }

    public static class Medication implements BaseColumns {
        public static final String TABLE_NAME = "medication";
        public static final String COLUMN_MEDICATION = "medication";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MEDICATION + " TEXT" + ")";
    }

    public static class Diary implements BaseColumns {
        public static final String TABLE_NAME = "diary";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_PAIN = "pain";
        public static final String COLUMN_STRENGTH = "strength";
        public static final String COLUMN_REGION = "region";
        public static final String COLUMN_MEDICATION = "medication";
        public static final String COLUMN_MEDICATION_AMOUNT = "medication_amount";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_START_DATE + " INTEGER, " +
                COLUMN_END_DATE + " INTEGER, " +
                COLUMN_DURATION + " INTEGER, " +
                COLUMN_STRENGTH + " INTEGER, " +
                COLUMN_REGION + " BLOB, " +
                COLUMN_MEDICATION + " TEXT, " +
                COLUMN_MEDICATION_AMOUNT + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PAIN + " TEXT " + ")";
    }
}
