package com.example.musicplayerdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.utils.Constants;
import com.example.musicplayerdemo.utils.TimeFormatConverter;

public class PlayerActivity extends AppCompatActivity {
    private TextView tvSongTitle;
    private TextView tvArtist;
    private TextView tvTotalTime;
    private ImageView ivCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        ivCover = findViewById(R.id.ivCover);

        Intent intent = getIntent();
        String title = intent.getStringExtra(Constants.EXTRA_TITLE);
        String artist = intent.getStringExtra(Constants.EXTRA_ARTIST);
        int duration = intent.getIntExtra(Constants.EXTRA_DURATION, 0);
        int coverResId = intent.getIntExtra(Constants.EXTRA_COVER_RES_ID, 0);

        tvSongTitle.setText(title);
        tvArtist.setText(artist);
        tvTotalTime.setText(TimeFormatConverter.second2Min(duration));
        if (coverResId != 0) {
            ivCover.setImageResource(coverResId);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }
}
