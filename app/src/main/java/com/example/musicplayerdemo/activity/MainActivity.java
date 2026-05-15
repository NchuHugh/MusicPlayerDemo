package com.example.musicplayerdemo.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.adapter.MusicAdapter;
import com.example.musicplayerdemo.data.Music;
import com.example.musicplayerdemo.data.MusicRepository;
import com.example.musicplayerdemo.player.MusicPlayerService;
import com.example.musicplayerdemo.player.PlaybackState;
import com.example.musicplayerdemo.player.PlayerState;
import com.example.musicplayerdemo.utils.AlbumCoverProgressUtil;
import com.example.musicplayerdemo.utils.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvMiniTitle;
    private TextView tvMiniArtist;
    private ImageView ivMiniCover;
    private Music currentMusic;
    private View layoutMiniPlayer;
    private View btnMiniNext;
    private ImageButton btnMiniPlayPause;
    private MusicPlayerService musicPlayerService;
    private boolean serviceBound = false;
    /*Handler：Android 中用于线程间通信的工具，可以将一个任务（Runnable）或消息（Message）投递到指定线程的消息队列中执行。
    * Looper.getMainLooper() 获取主线程（UI 线程）的 Looper 对象。 当 Looper 启动后，会执行一个无限循环，直到线程终止
    将这个 Looper 传递给 Handler 的构造方法，这个 Handler 会绑定到主线程。*/
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    /*每秒定时刷新*/
    private final Runnable miniPlayerRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            updateMiniPlayerFromState();
            uiHandler.postDelayed(this, 1000);
        }
    };

    /**
     * 初始化主页面布局、系统边距、列表和底部迷你播放器事件。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View mainView = findViewById(R.id.main);
        int originalLeft = mainView.getPaddingLeft();
        int originalTop = mainView.getPaddingTop();
        int originalRight = mainView.getPaddingRight();
        int originalBottom = mainView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    originalLeft + systemBars.left,
                    originalTop + systemBars.top,
                    originalRight + systemBars.right,
                    originalBottom + systemBars.bottom
            );
            return insets;
        });

        initViews();
        initRecyclerView();
        initEvents();
    }

    /**
     * 页面可见时绑定播放器 Service，接收当前播放状态。
     */
    @Override
    protected void onStart() {
        super.onStart();
        bindPlayerService();
    }

    /**
     * 页面不可见时停止底部播放器刷新并解绑 Service。 节约资源
     */
    @Override
    protected void onStop() {
        super.onStop();
        stopMiniPlayerRefresh();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    /**
     * 绑定底部迷你播放器相关控件，供后续刷新当前歌曲信息使用。
     */
    private void initViews() {
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        ivMiniCover = findViewById(R.id.ivMiniCover);
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        btnMiniNext = findViewById(R.id.btnMiniNext);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
    }

    /**
     * 从仓库获取歌曲数据，并初始化 RecyclerView 的布局管理器和适配器。
     */
    private void initRecyclerView() {
        List<Music> musicList = MusicRepository.getMusicList();

        MusicAdapter adapter = new MusicAdapter(musicList, music -> {
            updateMiniPlayer(music);
            openPlayerDetail(music);
        });

        RecyclerView listView = findViewById(R.id.rvMusicList);
        listView.setLayoutManager(new LinearLayoutManager(this));  //默认方向是 VERTICAL 垂直
        listView.setAdapter(adapter);
    }

    /**
     * 绑定底部迷你播放器点击事件，包括打开详情页和切换下一首。
     */
    private void initEvents() {
        layoutMiniPlayer.setOnClickListener(v -> {
            Music music = getCurrentDisplayMusic();
            if (music == null) {
                Toast.makeText(this, "请先选择一首歌曲", Toast.LENGTH_SHORT).show();
                return;
            }
            openPlayerDetail(music);
        });

        btnMiniNext.setOnClickListener(v -> {
            if (!hasCurrentMusic()) {
                Toast.makeText(this, "请先选择一首歌曲", Toast.LENGTH_SHORT).show();
                return;
            }
            playNextInMiniPlayer();
        });

        btnMiniPlayPause.setOnClickListener(v -> {
            if (!serviceBound || musicPlayerService == null || !hasCurrentMusic()) {
                Toast.makeText(this, "请先选择一首歌曲", Toast.LENGTH_SHORT).show();
                return;
            }

            PlayerState state = musicPlayerService.getPlayerState();
            if (state.getPlaybackState() == PlaybackState.PLAYING) {
                musicPlayerService.pause();
            } else {
                musicPlayerService.resume();
            }
            updateMiniPlayerFromState();
        });
    }

    /**
     * 将选中的歌曲信息刷新到底部迷你播放器。
     */
    private void updateMiniPlayer(Music music) {
        currentMusic = music;

        tvMiniTitle.setText(music.getTitle());
        tvMiniArtist.setText(music.getArtist());

        if (music.getCoverResId() != 0) {
            ivMiniCover.clearColorFilter();
            ivMiniCover.setImageBitmap(AlbumCoverProgressUtil.createProgressCover(
                    this,
                    music.getCoverResId(),
                    0,
                    music.getDuration() * 1000,
                    48
            ));
        }
    }

    /**
     * 绑定播放器 Service，拿到 Service 后即可读取 PlayerState。
     */
    private void bindPlayerService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 从 Service 的 PlayerState 刷新底部迷你播放器。
     */
    private void updateMiniPlayerFromState() {
        if (!serviceBound || musicPlayerService == null) {
            return;
        }

        PlayerState state = musicPlayerService.getPlayerState();
        Music music = state.getCurrentMusic();
        if (music == null) {
            return;
        }

        currentMusic = music;
        tvMiniTitle.setText(music.getTitle());
        tvMiniArtist.setText(music.getArtist());
        if (music.getCoverResId() != 0) {
            ivMiniCover.clearColorFilter();
            ivMiniCover.setImageBitmap(AlbumCoverProgressUtil.createProgressCover(
                    this,
                    music.getCoverResId(),
                    state.getCurrentPosition(),
                    state.getDuration() * 1000,
                    48
            ));
        }

        if (state.getPlaybackState() == PlaybackState.PLAYING) {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    /**
     * 开始定时刷新底部播放器状态。
     */
    private void startMiniPlayerRefresh() {
        uiHandler.removeCallbacks(miniPlayerRefreshRunnable);
        uiHandler.post(miniPlayerRefreshRunnable);
    }

    /**
     * 停止定时刷新底部播放器状态。
     */
    private void stopMiniPlayerRefresh() {
        uiHandler.removeCallbacks(miniPlayerRefreshRunnable);
    }

    /**
     * 判断当前是否有可展示或可控制的歌曲。
     */
    private boolean hasCurrentMusic() {
        return getCurrentDisplayMusic() != null;
    }

    /**
     * 优先从 Service 状态获取当前歌曲，没有 Service 状态时再使用本地临时展示歌曲。
     */
    private Music getCurrentDisplayMusic() {
        if (serviceBound && musicPlayerService != null) {
            Music stateMusic = musicPlayerService.getPlayerState().getCurrentMusic();
            if (stateMusic != null) {
                return stateMusic;
            }
        }
        if (currentMusic != null) {
            return currentMusic;
        }
        return null;
    }

    /**
     * 打开播放详情页，只传递歌曲 id，详情页再从 MusicRepository 查询完整数据。
     */
    private void openPlayerDetail(Music music) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(Constants.EXTRA_MUSIC_ID, music.getId());
        startActivity(intent);
    }

    /**
     * 在底部迷你播放器中切换到下一首歌曲；Service 已有播放状态时会直接控制 Service。
     */
    private void playNextInMiniPlayer() {
        if (serviceBound && musicPlayerService != null && musicPlayerService.getPlayerState().getCurrentMusic() != null) {
            Music beforeMusic = musicPlayerService.getPlayerState().getCurrentMusic();
            musicPlayerService.playNext();
            updateMiniPlayerFromState();

            Music afterMusic = musicPlayerService.getPlayerState().getCurrentMusic();
            if (afterMusic != null && afterMusic.getId() == beforeMusic.getId()) {
                Toast.makeText(this, "已经是最后一首", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Music nextMusic = MusicRepository.getNextMusic(currentMusic.getId());
        if (nextMusic == null) {
            Toast.makeText(this, "已经是最后一首", Toast.LENGTH_SHORT).show();
            return;
        }
        updateMiniPlayer(nextMusic);
    }

    /**
     * 接收播放器 Service 绑定结果，并启动底部播放器状态同步。serviceConnection是一个实例对象  继承了ServiceConnection接口的匿名内部类的实例对象
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        /*
         调用时机：当 bindService() 成功绑定到目标 Service，且 Service 的 onBind() 方法返回了一个有效的 IBinder 对象之后。
         IBinder service：核心对象，正是 Service 的 onBind() 返回的那个 IBinder 实例。
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            serviceBound = true;
            updateMiniPlayerFromState();
            startMiniPlayerRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicPlayerService = null;
            stopMiniPlayerRefresh();
        }
    };
}