package com.example.musicplayerdemo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.db.DBHelper;
import com.example.musicplayerdemo.db.MusicContract;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地曲库 SQLite 读写门面；扫描层、UI 不直接操作 SQL。
 */
public final class MusicLocalStore {

    private MusicLocalStore() {
    }

    public static boolean hasLocalLibrary(Context context) {
        DBHelper helper = new DBHelper(context.getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + MusicContract.LocalTrackTable.TABLE_NAME,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    public static List<Music> loadAll(Context context) {
        DBHelper helper = new DBHelper(context.getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Music> list = new ArrayList<>();

        String orderBy = MusicContract.LocalTrackTable.COLUMN_TITLE + " ASC";
        try (Cursor cursor = db.query(
                MusicContract.LocalTrackTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                orderBy
        )) {
            while (cursor.moveToNext()) {
                list.add(rowToMusic(cursor));
            }
        }
        return list;
    }

    /**
     * 全量替换本地曲库（扫描成功后调用），返回带数据库 _id 的列表。
     */
    public static List<Music> replaceAll(Context context, List<Music> tracks) {
        DBHelper helper = new DBHelper(context.getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(MusicContract.LocalTrackTable.TABLE_NAME, null, null);
            if (tracks != null) {
                for (Music track : tracks) {
                    db.insert(MusicContract.LocalTrackTable.TABLE_NAME, null, toContentValues(track));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return loadAll(context);
    }

    private static ContentValues toContentValues(Music music) {
        ContentValues values = new ContentValues();
        values.put(MusicContract.LocalTrackTable.COLUMN_MEDIA_STORE_ID, music.getMediaStoreId());
        values.put(MusicContract.LocalTrackTable.COLUMN_TITLE, music.getTitle());
        values.put(MusicContract.LocalTrackTable.COLUMN_ARTIST, music.getArtist());
        values.put(MusicContract.LocalTrackTable.COLUMN_DURATION, music.getDuration());
        values.put(
                MusicContract.LocalTrackTable.COLUMN_AUDIO_URI,
                music.getAudioUri() != null ? music.getAudioUri().toString() : ""
        );
        values.put(MusicContract.LocalTrackTable.COLUMN_COVER_RES_ID, music.getCoverResId());
        values.put(MusicContract.LocalTrackTable.COLUMN_AUDIO_RES_ID, music.getAudioResId());
        return values;
    }

    private static Music rowToMusic(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_ID));
        long mediaStoreId = cursor.getLong(
                cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_MEDIA_STORE_ID)
        );
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_TITLE));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_ARTIST));
        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_DURATION));
        String uriString = cursor.getString(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_AUDIO_URI));
        int coverResId = cursor.getInt(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_COVER_RES_ID));
        int audioResId = cursor.getInt(cursor.getColumnIndexOrThrow(MusicContract.LocalTrackTable.COLUMN_AUDIO_RES_ID));

        Uri audioUri = uriString == null || uriString.isEmpty() ? null : Uri.parse(uriString);
        if (coverResId == 0) {
            coverResId = R.drawable.cover;
        }
        return new Music(id, mediaStoreId, title, artist, duration, coverResId, audioResId, audioUri);
    }
}
