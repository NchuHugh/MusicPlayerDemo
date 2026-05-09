package com.example.musicplayerdemo.data;

import com.example.musicplayerdemo.R;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private static final List<Music> musicList = new ArrayList<>();

    static {
        musicList.add(new Music(1, "晴天", "周杰伦", 269, R.drawable.ic_launcher_foreground, R.raw.qingtian, false, 0));
        musicList.add(new Music(2, "那天下雨了", "周杰伦", 223, R.drawable.ic_launcher_foreground, R.raw.natianxiayule, false, 0));
        musicList.add(new Music(3, "雨天", "孙燕姿", 241, R.drawable.ic_launcher_foreground, R.raw.yutian, false, 0));
        musicList.add(new Music(4, "阴天", "莫文蔚", 242, R.drawable.ic_launcher_foreground, R.raw.yintian, false, 0));
        musicList.add(new Music(5, "最佳损友", "陈奕迅", 223, R.drawable.ic_launcher_foreground, R.raw.zuijiasunyou, false, 0));
        musicList.add(new Music(6, "沙龙", "陈奕迅", 275, R.drawable.ic_launcher_foreground, R.raw.shalong, false, 0));

    }

    private MusicRepository() {
    }

    /**
     * 返回歌曲列表副本，避免外部直接修改仓库内部的静态数据。
     */
    public static List<Music> getMusicList() {
        return new ArrayList<>(musicList);
    }

    /**
     * 根据歌曲 id 查询歌曲；没有匹配数据时返回 null。
     */
    public static Music getMusicById(int id) {
        for (Music music : musicList) {
            if (music.getId() == id) {
                return music;
            }
        }
        return null;
    }

    /**
     * 根据当前歌曲 id 获取下一首歌曲；如果已到末尾则返回 null。
     */
    public static Music getNextMusic(int currentMusicId) {
        int index = getIndexById(currentMusicId);
        if (index == -1 || index >= musicList.size() - 1) {
            return null;
        }
        return musicList.get(index + 1);
    }

    /**
     * 根据当前歌曲 id 获取上一首歌曲；如果已到开头则返回 null。
     */
    public static Music getPreviousMusic(int currentMusicId) {
        int index = getIndexById(currentMusicId);
        if (index <= 0) {
            return null;
        }
        return musicList.get(index - 1);
    }

    /**
     * 获取歌曲在内部列表中的位置；没有匹配数据时返回 -1。
     */
    private static int getIndexById(int id) {
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

}
