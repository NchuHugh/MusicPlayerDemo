package com.example.musicplayerdemo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
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
import com.example.musicplayerdemo.player.PlayMode;
import com.example.musicplayerdemo.player.PlaybackState;
import com.example.musicplayerdemo.player.PlayerState;
import com.example.musicplayerdemo.receiver.PlaybackActionReceiver;
import com.example.musicplayerdemo.utils.AlbumCoverProgressUtil;
import com.example.musicplayerdemo.utils.Constants;

import java.util.ArrayList;
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
    private final List<Integer> shuffleHistory = new ArrayList<>();
    private int shuffleHistoryIndex = -1;
    private boolean shuffleNavigatingBack = false;
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
     * 处理播放控制 Action（可来自 {@link com.example.musicplayerdemo.receiver.PlaybackActionReceiver} 转发的通知栏操作）。
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

        if (getPlayModeOrDefault() == PlayMode.SHUFFLE && !shuffleNavigatingBack) {
            appendShuffleHistory(musicId);
        }
        shuffleNavigatingBack = false;

        releaseMediaPlayer();

        playerState.setCurrentMusic(music);
        playerState.setDuration(music.getDuration());
        playerState.setCurrentPosition(0);
        playerState.setPlaybackState(PlaybackState.PREPARING);

        try {
            if (music.getAudioUri() != null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                );
                mediaPlayer.setDataSource(this, music.getAudioUri());
                mediaPlayer.prepare();
            } else {
                mediaPlayer = MediaPlayer.create(this, music.getAudioResId());
            }
        } catch (Exception e) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

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
     * 循环切换播放模式：顺序 → 列表循环 → 单曲循环 → 随机。
     */
    public PlayMode cyclePlayMode() {
        PlayMode nextMode = getPlayModeOrDefault().next();
        playerState.setPlayMode(nextMode);
        if (nextMode == PlayMode.SHUFFLE) {
            resetShuffleHistory();
            Music current = playerState.getCurrentMusic();
            if (current != null) {
                shuffleHistory.add(current.getId());
                shuffleHistoryIndex = 0;
            }
        } else {
            clearShuffleHistory();
        }
        return nextMode;
    }

    /**
     * 切换并播放下一首歌曲（遵循当前播放模式）。
     */
    public void playNext() {
        Music currentMusic = playerState.getCurrentMusic();
        if (currentMusic == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        switch (getPlayModeOrDefault()) {
            case LOOP_LIST:
                playNextOrFirst();
                break;
            case SHUFFLE:
                playRandomNext();
                break;
            case LOOP_ONE:
            case SEQUENCE:
            default:
                Music nextMusic = MusicRepository.getNextMusic(currentMusic.getId());
                if (nextMusic != null) {
                    play(nextMusic.getId());
                }
                break;
        }
    }

    /**
     * 切换并播放上一首歌曲（遵循当前播放模式）。
     */
    public void playPrevious() {
        Music currentMusic = playerState.getCurrentMusic();
        if (currentMusic == null) {
            playerState.setPlaybackState(PlaybackState.ERROR);
            return;
        }

        switch (getPlayModeOrDefault()) {
            case LOOP_LIST:
                playPreviousOrLast();
                break;
            case SHUFFLE:
                playShufflePrevious();
                break;
            case LOOP_ONE:
            case SEQUENCE:
            default:
                Music previousMusic = MusicRepository.getPreviousMusic(currentMusic.getId());
                if (previousMusic != null) {
                    play(previousMusic.getId());
                }
                break;
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

        switch (getPlayModeOrDefault()) {
            case LOOP_ONE:
                play(playerState.getCurrentMusic().getId());
                break;
            case LOOP_LIST:
                playNextOrFirst();
                break;
            case SHUFFLE:
                playRandomNext();
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
     * 随机播放一首与当前不同的歌曲（列表仅一首时重复播放）。
     */
    private void playRandomNext() {
        List<Music> musicList = MusicRepository.getMusicList();
        if (musicList.isEmpty()) {
            playerState.setPlaybackState(PlaybackState.COMPLETED);
            return;
        }

        if (musicList.size() == 1) {
            play(musicList.get(0).getId());
            return;
        }

        Music current = playerState.getCurrentMusic();
        Music target;
        do {
            target = musicList.get(random.nextInt(musicList.size()));
        } while (current != null && target.getId() == current.getId());

        play(target.getId());
    }

    /**
     * 随机模式下回到上一首播放记录。
     */
    private void playShufflePrevious() {
        if (shuffleHistoryIndex > 0) {
            shuffleHistoryIndex--;
            shuffleNavigatingBack = true;
            play(shuffleHistory.get(shuffleHistoryIndex));
            return;
        }

        Music current = playerState.getCurrentMusic();
        if (current == null) {
            return;
        }
        Music previous = MusicRepository.getPreviousMusic(current.getId());
        if (previous != null) {
            shuffleNavigatingBack = true;
            play(previous.getId());
        }
    }

    /**
     * 列表循环模式下播放上一首，若在开头则跳到最后一首。
     */
    private void playPreviousOrLast() {
        Music currentMusic = playerState.getCurrentMusic();
        Music previousMusic = currentMusic == null
                ? null
                : MusicRepository.getPreviousMusic(currentMusic.getId());
        if (previousMusic != null) {
            play(previousMusic.getId());
            return;
        }

        List<Music> musicList = MusicRepository.getMusicList();
        if (musicList.isEmpty()) {
            return;
        }
        play(musicList.get(musicList.size() - 1).getId());
    }

    private PlayMode getPlayModeOrDefault() {
        PlayMode playMode = playerState.getPlayMode();
        if (playMode == null) {
            playMode = PlayMode.SEQUENCE;
            playerState.setPlayMode(playMode);
        }
        return playMode;
    }

    private void appendShuffleHistory(int musicId) {
        if (shuffleHistoryIndex >= 0 && shuffleHistoryIndex < shuffleHistory.size() - 1) {
            shuffleHistory.subList(shuffleHistoryIndex + 1, shuffleHistory.size()).clear();
        }
        if (shuffleHistory.isEmpty() || shuffleHistory.get(shuffleHistory.size() - 1) != musicId) {
            shuffleHistory.add(musicId);
        }
        shuffleHistoryIndex = shuffleHistory.size() - 1;
    }

    private void resetShuffleHistory() {
        shuffleHistory.clear();
        shuffleHistoryIndex = -1;
        shuffleNavigatingBack = false;
    }

    private void clearShuffleHistory() {
        resetShuffleHistory();
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

        RemoteViews collapsedView = buildNotificationView(music, title, artist, pendingIntent);
        RemoteViews expandedView = buildNotificationView(music, title, artist, pendingIntent);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_transparent)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(pendingIntent)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setOnlyAlertOnce(false)
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
                buildPlaybackBroadcastPendingIntent(Constants.ACTION_PREVIOUS, 1)
        );
        views.setOnClickPendingIntent(
                R.id.btnNotificationPlayPause,
                buildPlaybackBroadcastPendingIntent(Constants.ACTION_PLAY_PAUSE, 2)
        );
        views.setOnClickPendingIntent(
                R.id.btnNotificationNext,
                buildPlaybackBroadcastPendingIntent(Constants.ACTION_NEXT, 3)
        );
        return views;
    }

    /**
     * 通知栏按钮：先发显式广播到 {@link PlaybackActionReceiver}，再由 Receiver 启动 Service。
     */
    private PendingIntent buildPlaybackBroadcastPendingIntent(String action, int requestCode) {
        Intent intent = new Intent(this, PlaybackActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * 更新前台通知。前台服务应通过 {@link #startForeground(int, Notification)} 刷新自定义 RemoteViews，
     * 仅用 {@link NotificationManager#notify(int, Notification)} 在部分系统上折叠通知不会立即重绘。
     */
    private void updateNotification() {
        startForeground(NOTIFICATION_ID, buildNotification());
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
