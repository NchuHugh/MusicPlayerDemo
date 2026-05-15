package com.example.musicplayerdemo.data;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.provider.LocalMusicContract;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 {@link LocalMusicContract#CONTENT_URI} 查询结果映射为 {@link Music}，与 Provider/MediaStore 实现解耦。
 */
public final class LocalMusicCursorMapper {

    private LocalMusicCursorMapper() {
    }

    public static List<Music> fromCursor(Cursor cursor) {
        List<Music> list = new ArrayList<>();
        if (cursor == null) {
            return list;
        }

        int idxId = cursor.getColumnIndex(LocalMusicContract.TrackColumns._ID);
        int idxTitle = cursor.getColumnIndex(LocalMusicContract.TrackColumns.TITLE);
        int idxArtist = cursor.getColumnIndex(LocalMusicContract.TrackColumns.ARTIST);
        int idxDuration = cursor.getColumnIndex(LocalMusicContract.TrackColumns.DURATION);
        int idxDisplayName = cursor.getColumnIndex(LocalMusicContract.TrackColumns.DISPLAY_NAME);

        while (cursor.moveToNext()) {
            if (idxId < 0) {
                continue;
            }
            long mediaStoreId = cursor.getLong(idxId);
            Uri trackUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaStoreId
            );

            String title = safeString(cursor, idxTitle);
            if (title.isEmpty()) {
                title = safeString(cursor, idxDisplayName);
            }
            if (title.isEmpty()) {
                title = "未知标题";
            }

            String artist = safeString(cursor, idxArtist);
            if (artist.isEmpty() || "<unknown>".equalsIgnoreCase(artist)) {
                artist = "未知歌手";
            }

            long durationMs = idxDuration >= 0 ? cursor.getLong(idxDuration) : 0L;
            int durationSec = (int) Math.max(1L, durationMs / 1000L);

            list.add(new Music(0, mediaStoreId, title, artist, durationSec, R.drawable.cover, 0, trackUri));
        }
        return list;
    }

    private static String safeString(Cursor cursor, int columnIndex) {
        if (columnIndex < 0) {
            return "";
        }
        String value = cursor.getString(columnIndex);
        return value == null ? "" : value.trim();
    }
}
