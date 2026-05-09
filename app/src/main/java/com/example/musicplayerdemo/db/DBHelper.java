package com.example.musicplayerdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    /**
     * 创建数据库帮助类实例，指定数据库名称和版本。
     */
    DBHelper(Context context) {
        super(context, MusicContract.DB_NAME, null, MusicContract.DB_VERSION);
    }


    /**
     * 首次创建数据库时建表，保存歌曲元数据、收藏状态和最近播放时间。
     */
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

    /**
     * 数据库版本升级时重建歌曲表；学习阶段先采用简单重建策略。
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //简单重建
        db.execSQL("DROP TABLE IF EXISTS " + MusicContract.MusicTable.TABLE_NAME);
        onCreate(db);
    }
}
