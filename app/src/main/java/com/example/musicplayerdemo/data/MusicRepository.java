package com.example.musicplayerdemo.data;

import com.example.musicplayerdemo.R;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private MusicRepository() {
    }

    public static List<Music> getMusicList() {
        List<Music> musicList = new ArrayList<>();

        musicList.add(new Music(1, "晴天", "周杰伦", 269, R.drawable.ic_launcher_foreground, 0, false, 0));
        musicList.add(new Music(2, "那天下雨了", "周杰伦", 223, R.drawable.ic_launcher_foreground, 0, false, 0));
        musicList.add(new Music(3, "雨天", "孙燕姿", 241, R.drawable.ic_launcher_foreground, 0, false, 0));
        musicList.add(new Music(4, "阴天", "莫文蔚", 242, R.drawable.ic_launcher_foreground, 0, false, 0));
        musicList.add(new Music(5, "最佳损友", "陈奕迅", 223, R.drawable.ic_launcher_foreground, 0, false, 0));
        musicList.add(new Music(6, "沙龙", "陈奕迅", 275, R.drawable.ic_launcher_foreground, 0, false, 0));

        return musicList;
    }


}
