package com.example.musicplayerdemo.data;

import android.content.Context;

import com.example.musicplayerdemo.R;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private static final List<Music> musicList = new ArrayList<>();
    private static boolean usingPersistedLocalLibrary = false;

    private MusicRepository() {
    }

    /**
     * 启动时加载曲库：若 SQLite 中已有扫描结果则读库，否则使用内置示例曲。
     */
    public static void initLibrary(Context context) {
        musicList.clear();
        if (MusicLocalStore.hasLocalLibrary(context)) {
            musicList.addAll(MusicLocalStore.loadAll(context));
            usingPersistedLocalLibrary = true;
        } else {
            loadBuiltinTracks();
            usingPersistedLocalLibrary = false;
        }
    }

    /**
     * 扫描成功后写入 SQLite 并刷新内存列表。
     */
    public static List<Music> saveScannedLibrary(Context context, List<Music> scanned) {
        List<Music> persisted = MusicLocalStore.replaceAll(context, scanned);
        musicList.clear();
        musicList.addAll(persisted);
        usingPersistedLocalLibrary = !musicList.isEmpty();
        return persisted;
    }

    public static boolean isUsingPersistedLocalLibrary() {
        return usingPersistedLocalLibrary;
    }

    /**
     * 返回歌曲列表副本，避免外部直接修改仓库内部的静态数据。
     */
    public static List<Music> getMusicList() {
        return new ArrayList<>(musicList);
    }

    /**
     * 用扫描结果整体替换当前内存曲库（不写库，仅供特殊场景）。
     */
    public static void replaceMusicList(List<Music> newList) {
        musicList.clear();
        if (newList != null) {
            musicList.addAll(newList);
        }
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

    private static void loadBuiltinTracks() {
        musicList.add(new Music(1, 0L, "晴天", "周杰伦", 269, R.drawable.qingtian, R.raw.qingtian, null));
        musicList.add(new Music(2, 0L, "那天下雨了", "周杰伦", 223, R.drawable.natianxiayule, R.raw.natianxiayule, null));
        musicList.add(new Music(3, 0L, "雨天", "孙燕姿", 241, R.drawable.yutian, R.raw.yutian, null));
        musicList.add(new Music(4, 0L, "阴天", "莫文蔚", 242, R.drawable.yintian, R.raw.yintian, null));
        musicList.add(new Music(5, 0L, "最佳损友", "陈奕迅", 223, R.drawable.zuijiasunyou, R.raw.zuijiasunyou, null));
        musicList.add(new Music(6, 0L, "沙龙", "陈奕迅", 275, R.drawable.shalong, R.raw.shalong, null));
    }

    private static int getIndexById(int id) {
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

}
