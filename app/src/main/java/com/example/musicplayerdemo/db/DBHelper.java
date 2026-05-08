package com.example.musicplayerdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.musicplayerdemo.data.Music;

public class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context){
    super(context,MusicContract.DB_NAME,null, MusicContract.DB_VERSION);
    //父类构造原型 public SQLiteOpenHelper(
        //    android.content.Context context,
        //    String name,
        //    android.database.sqlite.SQLiteDatabase.CursorFactory factory,
        //    int version
        //)
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表sql
        String sql =
                "CREATE TABLE " + MusicContract.MusicTable.TABLE_NAME +
                 " (" +
                MusicContract.MusicTable.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                MusicContract.MusicTable.COLUMN_TITLE + " TEXT NOT NULL, " +
                MusicContract.MusicTable.COLUMN_ARTIST + " TEXT, " +
                MusicContract.MusicTable.COLUMN_DURATION + " INTEGER, " +
                MusicContract.MusicTable.COLUMN_COVER_RES_ID + " INTEGER, " +
                MusicContract.MusicTable.COLUMN_AUDIO_RES_ID + " INTEGER, " +
                MusicContract.MusicTable.COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                MusicContract.MusicTable.COLUMN_LAST_PLAY_TIME + " INTEGER DEFAULT 0" +
                  ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //简单重建
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.MusicTable.TABLE_NAME);
        onCreate(db);
    }
}
