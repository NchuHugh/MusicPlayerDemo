package com.example.musicplayerdemo.data;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Music {
    private int id;
    /**
     * MediaStore 音频 _id；内置示例曲为 0。
     */
    private long mediaStoreId;
    private String title;
    private String artist;
    private int duration;

    private int coverResId;

    private int audioResId;

    /**
     * 本地扫描得到的音频；非空时使用该 Uri 播放，{@link #audioResId} 可为 0。
     */
    private Uri audioUri;

}
