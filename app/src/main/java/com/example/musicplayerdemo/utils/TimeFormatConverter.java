package com.example.musicplayerdemo.utils;

import java.util.Locale;

public class TimeFormatConverter {
    /**
     * 将秒数格式化为 mm:ss，用于展示歌曲总时长和当前播放时间。
     */
    public static String second2Min(int seconds) {
        if (seconds <= 0) {
            return "00:00";
        }
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }
}
