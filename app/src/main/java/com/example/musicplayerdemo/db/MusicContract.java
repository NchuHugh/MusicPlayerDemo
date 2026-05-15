package com.example.musicplayerdemo.db;

public final class MusicContract {

    public static final String DB_NAME = "music_player.db";
    public static final int DB_VERSION = 1;

    public static final class LocalTrackTable {
        public static final String TABLE_NAME = "local_track";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_MEDIA_STORE_ID = "media_store_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_AUDIO_URI = "audio_uri";
        public static final String COLUMN_COVER_RES_ID = "cover_res_id";
        public static final String COLUMN_AUDIO_RES_ID = "audio_res_id";

        private LocalTrackTable() {
        }
    }

    private MusicContract() {
    }
}
