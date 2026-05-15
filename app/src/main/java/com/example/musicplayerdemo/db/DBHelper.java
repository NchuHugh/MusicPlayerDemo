package com.example.musicplayerdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, MusicContract.DB_NAME, null, MusicContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + MusicContract.LocalTrackTable.TABLE_NAME + " ("
                        + MusicContract.LocalTrackTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MusicContract.LocalTrackTable.COLUMN_MEDIA_STORE_ID + " INTEGER NOT NULL UNIQUE, "
                        + MusicContract.LocalTrackTable.COLUMN_TITLE + " TEXT NOT NULL, "
                        + MusicContract.LocalTrackTable.COLUMN_ARTIST + " TEXT, "
                        + MusicContract.LocalTrackTable.COLUMN_DURATION + " INTEGER NOT NULL, "
                        + MusicContract.LocalTrackTable.COLUMN_AUDIO_URI + " TEXT NOT NULL, "
                        + MusicContract.LocalTrackTable.COLUMN_COVER_RES_ID + " INTEGER DEFAULT 0, "
                        + MusicContract.LocalTrackTable.COLUMN_AUDIO_RES_ID + " INTEGER DEFAULT 0"
                        + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.LocalTrackTable.TABLE_NAME);
        onCreate(db);
    }
}
