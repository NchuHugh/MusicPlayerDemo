package com.example.musicplayerdemo.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 只读 Provider：将本应用「本地音乐」查询统一为 content://，内部转发至系统 MediaStore。
 */
public class LocalMusicProvider extends ContentProvider {

    private static final int CODE_TRACKS = 1;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String[] DEFAULT_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
    };

    static {
        URI_MATCHER.addURI(LocalMusicContract.AUTHORITY, "tracks", CODE_TRACKS);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder
    ) {
        if (URI_MATCHER.match(uri) != CODE_TRACKS || getContext() == null) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        String[] proj = projection != null && projection.length > 0 ? projection : DEFAULT_PROJECTION;
        String order = sortOrder != null ? sortOrder : MediaStore.Audio.Media.TITLE + " ASC";
        String baseSelection = MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.DURATION + ">=?";
        String[] baseArgs = new String[]{String.valueOf(1000)};

        return getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                proj,
                baseSelection,
                baseArgs,
                order
        );
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (URI_MATCHER.match(uri) == CODE_TRACKS) {
            return "vnd.android.cursor.dir/vnd." + LocalMusicContract.AUTHORITY + ".track";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Read-only provider");
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs
    ) {
        throw new UnsupportedOperationException("Read-only provider");
    }
}
