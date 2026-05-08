package com.example.musicplayerdemo.entity;

@lombok
public class PlayerState {
  Music currentMusic;

  int currentIndex; //当前歌曲索引

    enum playbackState {playing,pause};

    int currentPosition;
    

}
