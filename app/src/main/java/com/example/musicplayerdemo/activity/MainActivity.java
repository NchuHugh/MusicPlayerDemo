package com.example.musicplayerdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.musicplayerdemo.utils.Constants;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvMiniTitle;
    private TextView tvMiniArtist;
    private ImageView ivMiniCover;
    private Music currentMusic;
    private View layoutMiniPlayer;
    private View btnMiniNext;
    private List<Music> musicList;

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
        initMiniPlayerEvents();
    }

    private void initViews() {
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        ivMiniCover = findViewById(R.id.ivMiniCover);
        layoutMiniPlayer = findViewById(R.id.layoutMiniPlayer);
        btnMiniNext = findViewById(R.id.btnMiniNext);
    }

    private void initRecyclerView() {
        musicList = MusicRepository.getMusicList();

        MusicAdapter adapter = new MusicAdapter(musicList, music -> {
            updateMiniPlayer(music);
            openPlayerDetail(music);
        });

        RecyclerView listView = findViewById(R.id.rvMusicList);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
    }

    private void initMiniPlayerEvents() {
        layoutMiniPlayer.setOnClickListener(v -> {
            if (currentMusic != null) {
                openPlayerDetail(currentMusic);
            } else {
                Toast.makeText(this, "请先选择一首歌曲", Toast.LENGTH_SHORT).show();
            }
        });

        btnMiniNext.setOnClickListener(v -> {
            if (currentMusic == null) {
                Toast.makeText(this, "请先选择一首歌曲", Toast.LENGTH_SHORT).show();
                return;
            }
            playNextInMiniPlayer();
        });
    }

    // 更新底部播放器
    private void updateMiniPlayer(Music music) {
        currentMusic = music;

        tvMiniTitle.setText(music.getTitle());
        tvMiniArtist.setText(music.getArtist());

        if (music.getCoverResId() != 0) {
            ivMiniCover.setImageResource(music.getCoverResId());
        }
    }

    private void openPlayerDetail(Music music) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(Constants.EXTRA_TITLE, music.getTitle());
        intent.putExtra(Constants.EXTRA_ARTIST, music.getArtist());
        intent.putExtra(Constants.EXTRA_DURATION, music.getDuration());
        intent.putExtra(Constants.EXTRA_COVER_RES_ID, music.getCoverResId());
        intent.putExtra(Constants.EXTRA_AUDIO_RES_ID, music.getAudioResId());
        startActivity(intent);
    }

    private void playNextInMiniPlayer() {
        int currentIndex = musicList.indexOf(currentMusic);
        int nextIndex = currentIndex + 1;

        if (nextIndex >= musicList.size()) {
            Toast.makeText(this, "已经是最后一首", Toast.LENGTH_SHORT).show();
            return;
        }

        updateMiniPlayer(musicList.get(nextIndex));
    }
}