package com.example.musicplayerdemo.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@lombok
public class Music {
    int id;   //歌曲id
    String title;  //歌曲名称
    String artist;  //歌手
    int duration;   //时长 单位：秒

    int coverResId;  //封面id

    int audioResId;  //资源id

    boolean isFavorite;  //收藏

}
