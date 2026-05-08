package com.example.musicplayerdemo.utils;

import java.util.Locale;

public class TimeFormatConverter {
    // 秒数转 mm:ss
    public static String second2Min(int seconds) {
        if (seconds <= 0) {
            return "00:00";
        }
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }
}
