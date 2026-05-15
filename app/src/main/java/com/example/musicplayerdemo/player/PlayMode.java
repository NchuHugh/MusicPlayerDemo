package com.example.musicplayerdemo.player;

import com.example.musicplayerdemo.R;

public enum PlayMode {
    SEQUENCE,
    LOOP_LIST,
    LOOP_ONE,
    SHUFFLE;

    public PlayMode next() {
        PlayMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public int getLabelResId() {
        switch (this) {
            case LOOP_LIST:
                return R.string.play_mode_loop_list;
            case LOOP_ONE:
                return R.string.play_mode_loop_one;
            case SHUFFLE:
                return R.string.play_mode_shuffle;
            case SEQUENCE:
            default:
                return R.string.play_mode_sequence;
        }
    }

    public int getIconResId() {
        switch (this) {
            case LOOP_LIST:
                return R.drawable.ic_play_mode_loop_list;
            case LOOP_ONE:
                return R.drawable.ic_play_mode_loop_one;
            case SHUFFLE:
                return R.drawable.ic_play_mode_shuffle;
            case SEQUENCE:
            default:
                return R.drawable.ic_play_mode_sequence;
        }
    }
}
