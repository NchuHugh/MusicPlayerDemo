package com.example.musicplayerdemo.player;

import com.example.musicplayerdemo.data.Music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState {
    private Music currentMusic;

    private PlaybackState playbackState;

    private int currentPosition;

    private int duration;

    private PlayMode playMode;
}
