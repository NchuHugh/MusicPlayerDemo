package com.example.musicplayerdemo.data;

import android.content.Context;
import android.database.Cursor;

import com.example.musicplayerdemo.provider.LocalMusicContract;

import java.util.List;

/**
 * 本地扫描门面：UI 层仅依赖此类，通过 ContentResolver 访问 {@link LocalMusicContract#CONTENT_URI}。
 */
public final class MusicLocalScanGateway {

    private MusicLocalScanGateway() {
    }

    public static List<Music> scanAllTracks(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    LocalMusicContract.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            return LocalMusicCursorMapper.fromCursor(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
