package com.example.musicplayerdemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayerdemo.R;
import com.example.musicplayerdemo.data.Music;
import com.example.musicplayerdemo.utils.TimeFormatConverter;

import java.util.List;

//列表条目模板渲染
// 提供 MusicViewHolder 类型的列表项。
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<Music> musicList;
    private final OnMusicClickListener listener;

    /**
     * 创建歌曲列表适配器，接收列表数据和 item 点击回调。
     */
    public MusicAdapter(List<Music> musicList, OnMusicClickListener listener) {
        this.musicList = musicList;
        //接收外部的歌曲列表
        this.listener = listener;
    }


    /**
     * 将 item_music.xml 转换成 ViewHolder，供 RecyclerView 复用。
     */
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    /**
     * 将指定位置的 Music 数据绑定到列表 item 上，并注册点击事件。
     */
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(music.getArtist());
        holder.tvDuration.setText(TimeFormatConverter.second2Min(music.getDuration()));
        holder.ivCover.setImageResource(music.getCoverResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMusicClick(music);
            }
        });
    }

    /**
     * 缓存单个歌曲 item 中的控件引用，减少列表滚动时重复查找控件的成本。
     */
    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;

        /**
         * 从 itemView 中绑定封面、标题、歌手和时长控件。
         */
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }

    /**
     * 返回列表数据数量，RecyclerView 根据该值决定需要展示多少个 item。
     */
    @Override
    public int getItemCount() {
        return musicList == null ? 0 : musicList.size();
    }

    /**
     * 歌曲 item 点击回调，由 Activity 决定点击后执行跳转或播放等操作。
     */
    public interface OnMusicClickListener {
        void onMusicClick(Music music);
    }

}
