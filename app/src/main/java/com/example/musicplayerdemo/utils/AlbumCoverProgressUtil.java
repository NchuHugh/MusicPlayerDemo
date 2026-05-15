package com.example.musicplayerdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public final class AlbumCoverProgressUtil {
    private static final int PROGRESS_COLOR = Color.parseColor("#1DB954");
    private static final int TRACK_COLOR = Color.parseColor("#335F6368");

    private AlbumCoverProgressUtil() {
    }

    /**
     * 生成圆形封面，并在外圈绘制绿色播放进度。
     */
    public static Bitmap createProgressCover(
            Context context,
            int coverResId,
            int currentPositionMs,
            int durationMs,
            int sizeDp
    ) {
        int sizePx = dpToPx(context, sizeDp);
        int strokePx = Math.max(2, dpToPx(context, 3));
        int coverInset = strokePx + dpToPx(context, 2);

        Bitmap result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        drawCircularCover(context, canvas, coverResId, sizePx, coverInset);
        drawProgressRing(canvas, sizePx, strokePx, currentPositionMs, durationMs);

        return result;
    }

    private static void drawCircularCover(
            Context context,
            Canvas canvas,
            int coverResId,
            int sizePx,
            int coverInset
    ) {
        Drawable cover = ContextCompat.getDrawable(context, coverResId);
        if (cover == null) {
            return;
        }

        RectF coverRect = new RectF(
                coverInset,
                coverInset,
                sizePx - coverInset,
                sizePx - coverInset
        );

        int saveCount = canvas.save();
        Path clipPath = new Path();
        clipPath.addOval(coverRect, Path.Direction.CW);
        canvas.clipPath(clipPath);

        cover.setBounds(
                Math.round(coverRect.left),
                Math.round(coverRect.top),
                Math.round(coverRect.right),
                Math.round(coverRect.bottom)
        );
        cover.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    private static void drawProgressRing(
            Canvas canvas,
            int sizePx,
            int strokePx,
            int currentPositionMs,
            int durationMs
    ) {
        float radiusInset = strokePx / 2f;
        RectF ringRect = new RectF(radiusInset, radiusInset, sizePx - radiusInset, sizePx - radiusInset);

        Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(TRACK_COLOR);
        canvas.drawOval(ringRect, trackPaint);

        if (durationMs <= 0) {
            return;
        }

        float progress = Math.max(0f, Math.min(1f, currentPositionMs / (float) durationMs));
        Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokePx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(PROGRESS_COLOR);
        canvas.drawArc(ringRect, -90f, progress * 360f, false, progressPaint);
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
