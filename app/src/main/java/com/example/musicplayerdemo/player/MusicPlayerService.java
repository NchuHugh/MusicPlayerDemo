package com.example.musicplayerdemo.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.activity.PlayerActivity;
import com.example.musicplayerdemo.data.Music;
import com.example.musicplayerdemo.data.MusicRepository;
import com.example.musicplayerdemo.utils.AlbumCoverProgressUtil;
import com.example.musicplayerdemo.utils.Constants;

import java.util.List;
import java.util.Random;

import lombok.Getter;

public class MusicPlayerService extends Service {
    private static final String CHANNEL_ID = "music_player_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaPlayer mediaPlayer;
    /**
     * -- GETTER --
     * 返回当前播放状态，供 Activity 刷新页面 UI。
     */
    @Getter
    private final PlayerState playerState = new PlayerState();
    private final IBinder binder = new MusicBinder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isPlaying = false;

    /**
     * 定时同步 MediaPlayer 当前播放进度到 PlayerState。
     */
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                playerState.setCurrentPosition(mediaPlayer.getCurrentPosition());
                updateNotification();
                handler.postDelayed(this, 1000);
            }
        }
    };

    /**
     * 初始化 Service 内部播放状态。
     */
    @Override
    public void onCreate() {
        super.onCreate();
        playerState.setPlaybackState(PlaybackState.IDLE);
        playerState.setCurrentPosition(0);
        playerState.setDuration(0);
        playerState.setPlayMode(PlayMode.SEQUENCE);
        createNotificationChannel();
    }

    /**
     * 处理通知栏按钮发送过来的播放控制 Action。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        switch (intent.getAction()) {
            case Constants.ACTION_PREVIOUS:
                playPrevious();
                updateNotification();
                break;
            case Constants.ACTION_PLAY_PAUSE:
                togglePlayPause();
                break;
            case Constants.ACTION_NEXT:
                playNext();
                updateNotification();
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    /**
     * 播放指定歌曲。
     */
    public void play(int musicId) {
        Music music = MusicRepository.getMusicById(musicId);
        if (music == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        releaseMediaPlayer();

        playerState.setCurrentMusic(music);
        playerState.setDuration(music.getDuration());
        playerState.setCurrentPosition(0);
        playerState.setPlaybackState(PlaybackState.PREPARING);

        mediaPlayer = MediaPlayer.create(this, music.getAudioResId());
        if (mediaPlayer == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        mediaPlayer.setOnCompletionListener(mp -> handleCompletion());

        mediaPlayer.start();
        isPlaying = true;
        playerState.setPlaybackState(PlaybackState.PLAYING);
        startProgressUpdate();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    /**
     * 暂停当前播放。
     */
    public void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            playerState.setPlaybackState(PlaybackState.PAUSED);
            stopProgressUpdate();
            updateNotification();
        }

    }

    /**
     * 从暂停位置继续播放当前歌曲。
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            playerState.setPlaybackState(PlaybackState.PLAYING);
            startProgressUpdate();
            updateNotification();
        }
    }

    /**
     * 根据当前播放状态在播放和暂停之间切换。
     */
    private void togglePlayPause() {
        if (playerState.getPlaybackState() == PlaybackState.PLAYING) {
            pause();
        } else {
            resume();
        }
    }

    /**
     * 切换并播放下一首歌曲。
     */
    public void playNext() {
        Music currentMusic = playerState.getCurrentMusic();
        if (currentMusic == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        Music nextMusic = MusicRepository.getNextMusic(currentMusic.getId());
        if (nextMusic == null) {
            return;
        }

        play(nextMusic.getId());
    }

    /**
     * 切换并播放上一首歌曲。
     */
    public void playPrevious() {
        Music currentMusic = playerState.getCurrentMusic();
        if (currentMusic == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        Music previousMusic = MusicRepository.getPreviousMusic(currentMusic.getId());
        if (previousMusic != null) {
            play(previousMusic.getId());
        }
    }

    /**
     * 跳转到指定播放进度，position 单位为毫秒。
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
        playerState.setCurrentPosition(position);
    }

    /**
     * 启动播放进度同步任务。
     */
    private void startProgressUpdate() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    /**
     * 停止播放进度同步任务。
     */
    private void stopProgressUpdate() {
        handler.removeCallbacks(progressRunnable);
    }

    /**
     * 释放 MediaPlayer 资源，切歌、停止播放或 Service 销毁时调用。
     */
    private void releaseMediaPlayer() {
        stopProgressUpdate();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
    }

    /**
     * 处理歌曲播放完成后的下一步行为。
     */
    private void handleCompletion() {
        stopProgressUpdate();
        isPlaying = false;
        playerState.setCurrentPosition(0);
        updateNotification();

        PlayMode playMode = playerState.getPlayMode();
        if (playMode == null) {
            playMode = PlayMode.SEQUENCE;
            playerState.setPlayMode(playMode);
        }

        switch (playMode) {
            case LOOP_ONE:
                play(playerState.getCurrentMusic().getId());
                break;
            case LOOP_LIST:
                playNextOrFirst();
                break;
            case SHUFFLE:
                playRandom();
                break;
            case SEQUENCE:
            default:
                playNextOnCompletion();
                break;
        }
    }

    /**
     * 顺序播放完成时尝试播放下一首；没有下一首则进入完成状态。
     */
    private void playNextOnCompletion() {
        Music currentMusic = playerState.getCurrentMusic();
        Music nextMusic = currentMusic == null ? null : MusicRepository.getNextMusic(currentMusic.getId());
        if (nextMusic == null) {
            playerState.setPlaybackState(PlaybackState.COMPLETED);
            return;
        }

        play(nextMusic.getId());
    }

    /**
     * 列表循环模式下播放下一首，如果到末尾则回到第一首。
     */
    private void playNextOrFirst() {
        Music currentMusic = playerState.getCurrentMusic();
        Music nextMusic = currentMusic == null ? null : MusicRepository.getNextMusic(currentMusic.getId());
        if (nextMusic != null) {
            play(nextMusic.getId());
            return;
        }

        List<Music> musicList = MusicRepository.getMusicList();
        if (musicList.isEmpty()) {
            playerState.setPlaybackState(PlaybackState.COMPLETED);
            return;
        }

        play(musicList.get(0).getId());
    }

    /**
     * 随机播放列表中的一首歌曲。
     */
    private void playRandom() {
        List<Music> musicList = MusicRepository.getMusicList();
        if (musicList.isEmpty()) {
            playerState.setPlaybackState(PlaybackState.COMPLETED);
            return;
        }

        Music music = musicList.get(random.nextInt(musicList.size()));
        play(music.getId());
    }

    /**
     * 创建通知渠道；Android 8.0 及以上显示通知前必须先创建 Channel。
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("音乐播放前台服务通知");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 根据当前 PlayerState 构建前台服务通知。
     */
    private Notification buildNotification() {
        Music music = playerState.getCurrentMusic();
        String title = music == null ? "音乐播放器" : music.getTitle();
        String artist = music == null ? "暂无播放歌曲" : music.getArtist();

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Constants.EXTRA_FROM_NOTIFICATION, true);
        if (music != null) {
            intent.putExtra(Constants.EXTRA_MUSIC_ID, music.getId());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        RemoteViews notificationView = buildNotificationView(music, title, artist, pendingIntent);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_transparent)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationView)
                .setCustomBigContentView(notificationView)
                .setOnlyAlertOnce(true)
                .setOngoing(playerState.getPlaybackState() == PlaybackState.PLAYING)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    /**
     * 构建类似底部播放器的自定义通知布局。
     */
    private RemoteViews buildNotificationView(Music music, String title, String artist, PendingIntent contentIntent) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification_player);
        views.setTextViewText(R.id.tvNotificationTitle, title);
        views.setTextViewText(R.id.tvNotificationArtist, artist);

        if (music != null && music.getCoverResId() != 0) {
            views.setImageViewBitmap(
                    R.id.ivNotificationCover,
                    AlbumCoverProgressUtil.createProgressCover(
                            this,
                            music.getCoverResId(),
                            playerState.getCurrentPosition(),
                            playerState.getDuration() * 1000,
                            48
                    )
            );
        } else {
            views.setImageViewResource(R.id.ivNotificationCover, android.R.drawable.ic_media_play);
        }

        int playPauseIcon = playerState.getPlaybackState() == PlaybackState.PLAYING
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play;
        views.setImageViewResource(R.id.btnNotificationPlayPause, playPauseIcon);

        views.setOnClickPendingIntent(R.id.layoutNotificationPlayer, contentIntent);
        views.setOnClickPendingIntent(
                R.id.btnNotificationPrevious,
                buildServiceActionPendingIntent(Constants.ACTION_PREVIOUS, 1)
        );
        views.setOnClickPendingIntent(
                R.id.btnNotificationPlayPause,
                buildServiceActionPendingIntent(Constants.ACTION_PLAY_PAUSE, 2)
        );
        views.setOnClickPendingIntent(
                R.id.btnNotificationNext,
                buildServiceActionPendingIntent(Constants.ACTION_NEXT, 3)
        );
        return views;
    }

    /**
     * 创建发送给 MusicPlayerService 的通知栏按钮 PendingIntent。
     */
    private PendingIntent buildServiceActionPendingIntent(String action, int requestCode) {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * 更新已经显示的播放通知。
     */
    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification());
        }
    }


    /**
     * Service 销毁时释放播放器资源。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        stopForeground(STOP_FOREGROUND_REMOVE);
    }


    /**
     * Activity 调用 bindService 后，系统会通过该方法返回 Binder。
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * 暴露 Service 实例给绑定方，Activity 可通过它调用播放控制方法。
     */
    public class MusicBinder extends Binder {
        /**
         * 返回当前 MusicPlayerService 实例。
         */
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }
}
