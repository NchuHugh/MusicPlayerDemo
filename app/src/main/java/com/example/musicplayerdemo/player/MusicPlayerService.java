package com.example.musicplayerdemo.player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.musicplayerdemo.data.Music;
import com.example.musicplayerdemo.data.MusicRepository;

import java.util.List;
import java.util.Random;

import lombok.Getter;

public class MusicPlayerService extends Service {

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
     * Service 销毁时释放播放器资源。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
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
