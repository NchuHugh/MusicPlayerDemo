package com.example.musicplayerdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.example.musicplayerdemo.service.MusicPlayerService;
import com.example.musicplayerdemo.utils.Constants;

/**
 * 接收通知栏播放控制广播，再转发给 {@link MusicPlayerService}。
 * 便于与 Widget、媒体键等统一走广播入口。
 */
public class PlaybackActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (!Constants.ACTION_PREVIOUS.equals(action)
                && !Constants.ACTION_PLAY_PAUSE.equals(action)
                && !Constants.ACTION_NEXT.equals(action)) {
            return;
        }

        Intent serviceIntent = new Intent(context, MusicPlayerService.class);
        serviceIntent.setAction(action);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
