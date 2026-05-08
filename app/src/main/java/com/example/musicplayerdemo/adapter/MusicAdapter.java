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

    public MusicAdapter(List<Music> musicList, OnMusicClickListener listener) {
        this.musicList = musicList;
        //接收外部的歌曲列表
        this.listener = listener;
    }


    /**
     *
     将布局文件转化成view对象
     */
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    /**
     * 将Music的对应值绑定到MusicItem上
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
     * 统一读取 item_music 中的各 View，封装后方便后续重新赋值
     */
    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }

    @Override
    public int getItemCount() {
        return musicList == null ? 0 : musicList.size();
    }

    //事件监听 函数式接口 具体实现使用lambda表达式
    public interface OnMusicClickListener {
        void onMusicClick(Music music);
    }

}
