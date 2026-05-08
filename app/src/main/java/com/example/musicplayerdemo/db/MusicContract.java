package com.example.musicplayerdemo.db;

public final class MusicContract {
    private MusicContract() {}

    public static final String DB_NAME = "music_player.db";
    public static final int DB_VERSION = 1;

    public static final class MusicTable {
        public static final String TABLE_NAME = "music";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_COVER_RES_ID = "cover_res_id";
        public static final String COLUMN_AUDIO_RES_ID = "audio_res_id";
        public static final String COLUMN_IS_FAVORITE = "is_favorite";
        public static final String COLUMN_LAST_PLAY_TIME = "last_play_time";
    }
}