package com.petkit.matetool.player.ijkplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;

import com.petkit.android.utils.PetkitLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class VideoUtils {
    /**
     * 播放进度转换为时间
     *
     * @param progress 进度(整型)
     */
    public static String progress2Time(long progress) {
        if (progress < 0) {
            return "00:00";
        } else {
            SimpleDateFormat simpleDate = new SimpleDateFormat("HH:mm:ss");
            simpleDate.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            String result = simpleDate.format(new Date(progress));
//            if (result.startsWith("00:")) {
//                return result.replace("00:", "");
//            } else {
            return result;
//            }

        }
    }

    /**
     * 将滑动的距离转为进度显示
     *
     * @param distance   滑动的距离
     * @param duration   总进度
     * @param proportion 一次屏幕的滑动所占总进度的比例
     * @param all        总高度/总宽度
     */
    public static Double dt2progress(Float distance, Long duration, Integer all, Double proportion) {
        return (distance * duration * proportion / all);
    }


    public static Integer getPhoneDisplayWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        (activity).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;// 宽度
    }


    public static Integer getPhoneDisplayHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        (activity).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;// 高度
    }

    /**
     * 获取视频流的第一帧图片
     *
     * @param url 在线视频的播放地址/本地视频的uri
     * @return Bitmap
     */
    public static Bitmap getNetVideoBitmap(String url) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(url, new HashMap<String, String>());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 竖屏下使用：两边等比例放大，结果宽高一边等于宽或高，一边小于宽或高
     * @param phoneWidth
     * @param phoneHeight
     * @param playerWidth
     * @param playerHeight
     * @return
     */
    //按比例改变视频大小适配屏幕宽高,
    public static LinearLayout.LayoutParams changeVideoSize(int phoneWidth, int phoneHeight, int playerWidth, int playerHeight) {
        PetkitLog.d("changeVideoSize", "changeVideoSize:phoneWidth"+phoneWidth+", phoneHeight:"+phoneHeight+", playerWidth:"+playerWidth+", playerHeight:"+playerHeight);
        int defaultWidth = phoneWidth < 0 ? 1920 : phoneWidth;

        //根据视频尺寸去计算->视频可以在TextureView中放大的最大倍数。
        double max = Math.max(playerHeight * 1.0 / phoneHeight, playerWidth * 1.0 / defaultWidth);


        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        double videoWidth = Math.ceil(playerWidth * 1.0 / max);
        double videoHeight = Math.ceil(playerHeight * 1.0 / max);
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        return new LinearLayout.LayoutParams((int) videoWidth, (int) videoHeight);
    }

    /**
     * 全屏下使用：两边等比例放大，结果宽高一边等于宽或高，一边大于宽或高
     * @param phoneWidth
     * @param phoneHeight
     * @param playerWidth
     * @param playerHeight
     * @return
     */

    public static LinearLayout.LayoutParams changeVideoSizeMax(int phoneWidth, int phoneHeight, int playerWidth, int playerHeight) {
        PetkitLog.d("changeVideoSize", "changeVideoSize:phoneWidth"+phoneWidth+", phoneHeight:"+phoneHeight+", playerWidth:"+playerWidth+", playerHeight:"+playerHeight);
        int defaultWidth = phoneWidth < 0 ? 1920 : phoneWidth;

        //根据视频尺寸去计算->视频可以在TextureView中放大的最大倍数。
        double min = Math.min(playerHeight * 1.0 / phoneHeight, playerWidth * 1.0 / defaultWidth);


        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        double videoWidth = Math.ceil(playerWidth * 1.0 / min);
        double videoHeight = Math.ceil(playerHeight * 1.0 / min);
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        return new LinearLayout.LayoutParams((int) videoWidth, (int) videoHeight);
    }

    /**
     * 获取手机是否开启自动旋转屏幕
     *
     * @param context 上下文
     * @return 是否已开启自动旋转屏幕功能
     */
    public static boolean getIsOpenRotate(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * long转文件大小
     *
     * @param size
     * @return 返回B、K、M、G
     */
    public static String parseByteSize(Float size) {
        if (size >= 0 && size < 1024f) {
            return String.format("%.1fB", size);
        } else if (size >= 1024 && size < 1024f * 1024f) {
            return String.format("%.1fK", size / 1024f);
        } else if (size >= 1024f * 1024f && size < 1024f * 1024f * 1024f) {
            return String.format("%.1fM", size / 1024f / 1024f);

        } else if (size >= 1024f * 1024f * 1024f) {
            return String.format("%.1fG", size / 1024f / 1024f / 1024f);
        } else {
            return "0B";
        }

    }
}
