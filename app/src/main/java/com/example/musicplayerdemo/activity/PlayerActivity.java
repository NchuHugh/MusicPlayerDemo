package com.example.musicplayerdemo.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.data.Music;
import com.example.musicplayerdemo.service.MusicPlayerService;
import com.example.musicplayerdemo.player.PlayMode;
import com.example.musicplayerdemo.player.PlaybackState;
import com.example.musicplayerdemo.player.PlayerState;
import com.example.musicplayerdemo.utils.Constants;
import com.example.musicplayerdemo.utils.TimeFormatConverter;

public class PlayerActivity extends AppCompatActivity {
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    private TextView tvSongTitle;
    private TextView tvArtist;
    private TextView tvTotalTime;
    private ImageView ivCover;
    private TextView tvCurrentTime;
    private ImageButton btnPlayMode;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnPlayPause;
    private SeekBar seekBarProgress;

    private MusicPlayerService musicPlayerService;
    private boolean serviceBound = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable uiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            updateUiFromState();
            uiHandler.postDelayed(this, 1000);
            //启动后每秒刷新UI
        }
    };

    /**
     * 接收 Service 绑定结果，保存 Service 实例以便后续调用播放控制方法。
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * Service 连接成功时由系统回调，此处通过 Binder 获取 MusicPlayerService 实例。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            serviceBound = true;
            int musicId = getIntent().getIntExtra(Constants.EXTRA_MUSIC_ID, -1);
            if (!isOpenedFromNotification()) {
                playInitialMusicIfNeeded(musicId);
            }
            updateUiFromState();
            startUiRefresh();
        }

        /**
         * Service 异常断开时由系统回调，清空本地 Service 引用。
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicPlayerService = null;
        }
    };

    /**
     * 只有当前入口歌曲和 Service 正在维护的歌曲不一致时才重新播放。
     */
    private void playInitialMusicIfNeeded(int musicId) {
        if (musicId == -1 || musicPlayerService == null) {
            return;
        }

        PlayerState state = musicPlayerService.getPlayerState();
        Music currentMusic = state.getCurrentMusic();
        if (currentMusic != null && currentMusic.getId() == musicId) {
            return;
        }

        musicPlayerService.play(musicId);
    }

    /**
     * 判断当前页面是否由通知点击打开；通知只负责回到详情页，不触发重新播放。
     */
    private boolean isOpenedFromNotification() {
        return getIntent().getBooleanExtra(Constants.EXTRA_FROM_NOTIFICATION, false);
    }

    /**
     * 初始化播放详情页，绑定 UI、事件和播放服务，并加载入口歌曲。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);

        initViews();
        initEvents();
        requestNotificationPermissionIfNeeded();
        initService();
    }

    /**
     * 已存在的 PlayerActivity 被通知重新唤起时，只刷新 Intent 和 UI，不重新播放。
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        updateUiFromState();
    }

    /**
     * Android 13 及以上显示通知需要运行时申请 POST_NOTIFICATIONS 权限。
     */
    private void requestNotificationPermissionIfNeeded() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS
            );
        }
    }

    /**
     * 启动并绑定 MusicPlayerService
     */
    private void initService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 绑定播放详情页中的静态控件，包括歌曲信息、控制按钮和进度条。
     */
    private void initViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        ivCover = findViewById(R.id.ivCover);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        btnPlayMode = findViewById(R.id.btnPlayMode);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBarProgress = findViewById(R.id.seekBarProgress);
    }

    /**
     * 注册页面上的交互事件，包括返回、播放暂停、上一首下一首和拖动进度条。
     */
    private void initEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlayMode.setOnClickListener(v -> {
            if (!serviceBound || musicPlayerService == null) {
                Toast.makeText(this, "播放器服务未连接", Toast.LENGTH_SHORT).show();
                return;
            }
            PlayMode mode = musicPlayerService.cyclePlayMode();
            btnPlayMode.setImageResource(mode.getIconResId());
            String label = getString(mode.getLabelResId());
            Toast.makeText(this, getString(R.string.play_mode_switched, label), Toast.LENGTH_SHORT).show();
        });
        //播放/暂停
        btnPlayPause.setOnClickListener(v -> {
            if (!serviceBound || musicPlayerService == null) {
                Toast.makeText(this, "播放器服务未连接", Toast.LENGTH_SHORT).show();
                return;
            }

            PlayerState state = musicPlayerService.getPlayerState();
            if (state.getPlaybackState() == PlaybackState.PLAYING) {
                musicPlayerService.pause();
            } else {
                musicPlayerService.resume();
            }
            updateUiFromState();
        });
         //上一首
        btnPrevious.setOnClickListener(v -> {
            if (!serviceBound || musicPlayerService == null) {
                Toast.makeText(this, "播放器服务未连接", Toast.LENGTH_SHORT).show();
                return;
            }
            musicPlayerService.playPrevious();
            updateUiFromState();
        });
        //下一首
        btnNext.setOnClickListener(v -> {
            if (!serviceBound || musicPlayerService == null) {
                Toast.makeText(this, "播放器服务未连接", Toast.LENGTH_SHORT).show();
                return;
            }
            musicPlayerService.playNext();
            updateUiFromState();
        });
         //播放进度条操作
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              //只有用户拖动才允许修改
                if (fromUser && serviceBound && musicPlayerService != null) {
                    musicPlayerService.seekTo(progress);
                    updateUiFromState();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * 根据 Service 中的 PlayerState 刷新详情页 UI。
     */
    private void updateUiFromState() {
        if (!serviceBound || musicPlayerService == null) {
            return;
        }

        PlayerState state = musicPlayerService.getPlayerState();
        Music music = state.getCurrentMusic();
        if (music == null) {
            return;
        }

        tvSongTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());
        tvTotalTime.setText(TimeFormatConverter.second2Min(state.getDuration()));
        tvCurrentTime.setText(TimeFormatConverter.second2Min(state.getCurrentPosition() / 1000));
        seekBarProgress.setMax(state.getDuration() * 1000);
        seekBarProgress.setProgress(state.getCurrentPosition());

        if (music.getCoverResId() != 0) {
            ivCover.clearColorFilter();
            ivCover.setImageResource(music.getCoverResId());
        } else {
            ivCover.setImageResource(android.R.drawable.ic_media_play);
        }

        if (state.getPlaybackState() == PlaybackState.PLAYING) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }

        PlayMode playMode = state.getPlayMode() == null ? PlayMode.SEQUENCE : state.getPlayMode();
        btnPlayMode.setImageResource(playMode.getIconResId());
    }

    /**
     * 开始定时读取 PlayerState 并刷新详情页 UI。
     */
    private void startUiRefresh() {
        uiHandler.removeCallbacks(uiRefreshRunnable);
        uiHandler.post(uiRefreshRunnable);
    }

    /**
     * 停止详情页 UI 定时刷新，避免 Activity 销毁后继续更新控件。
     */
    private void stopUiRefresh() {
        uiHandler.removeCallbacks(uiRefreshRunnable);
    }


    /**
     * 页面销毁时解绑 Service；播放器资源由 MusicPlayerService 统一管理。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopUiRefresh();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }


}
