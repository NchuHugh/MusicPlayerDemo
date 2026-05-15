package com.example.musicplayerdemo.provider;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * 本地曲库 ContentProvider 对外契约；调用方只依赖本类 URI/列名，不直接访问 MediaStore。
 */
public final class LocalMusicContract {

    public static final String AUTHORITY = "com.example.musicplayerdemo.provider.localmusic";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tracks");

    public static final class TrackColumns {
        public static final String _ID = MediaStore.Audio.Media._ID;
        public static final String TITLE = MediaStore.Audio.Media.TITLE;
        public static final String ARTIST = MediaStore.Audio.Media.ARTIST;
        public static final String DURATION = MediaStore.Audio.Media.DURATION;
        public static final String DISPLAY_NAME = MediaStore.Audio.Media.DISPLAY_NAME;

        private TrackColumns() {
        }
    }

    private LocalMusicContract() {
    }
}
