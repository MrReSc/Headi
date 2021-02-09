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

    public static class Diary implements BaseColumns {
        public static final String TABLE_NAME = "diary";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_PAIN_ID = "pain_id";
        public static final String COLUMN_REGION = "region";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " INTEGER, " +
                COLUMN_DURATION + " INTEGER, " +
                COLUMN_REGION + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_PAIN_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_PAIN_ID + ") REFERENCES " +
                Pains.TABLE_NAME + "(" + Pains._ID + ") " + ")";
    }
}