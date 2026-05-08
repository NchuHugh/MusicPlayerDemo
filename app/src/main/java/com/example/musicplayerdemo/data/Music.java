package com.example.musicplayerdemo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Music {
    private int id;   // 歌曲id
    private String title;  // 歌曲名称
    private String artist;  // 歌手
    private int duration;   // 时长，单位：秒

    private int coverResId;  // 封面id

    private int audioResId;  // 资源id

    private boolean favorite;  // 收藏
    private int lastPlayTime;

}
