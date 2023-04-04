package com.petkit.matetool.player.ijkplayer;

public class VideoConstant {
    public static final String VIDEO_TAG = "VIDEO_TAG";

    /**
     * 播放器状态
     */
    public class PlayState {
        /**
         * 播放错误
         */
        public static final int STATE_ERROR = -1;
        /**
         * 播放器闲置转态
         */
        public static final int STATE_IDLE = 0;
        /**
         * 播放器初始化完成
         */
        public static final int STATE_INITLIZED = 1;
        /**
         * 播放准备中
         */
        public static final int STATE_PREPARING = 2;
        /**
         * 播放准备就绪
         */
        public static final int STATE_PREPARED = 3;
        /**
         * 正在播放
         */
        public static final int STATE_PLAYING = 4;
        /**
         * 暂停播放
         */
        public static final int STATE_PAUSED = 5;
        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
         */
        public static final int STATE_BUFFERING_PLAYING = 6;
        /**
         * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
         */
        public static final int STATE_BUFFERING_PAUSED = 7;
        /**
         * 播放完成
         */
        public static final int STATE_COMPLETED = 8;
    }


    /**
     * 播放模式
     */
    public class PlayMode {
        /**
         * 普通模式
         */
        public static final int MODE_NORMAL = 10;
        /**
         * 全屏模式
         */
        public static final int MODE_FULL_SCREEN = 11;
        /**
         * 窗口模式
         */
        public static final int MODE_TINY_WINDOW = 12;
    }


    /**
     * 切换模式
     */
   public class SwitchMode {
        /**
         * 普通->全屏->普通模式
         */
        public static final int SWITCH_FULL_OR_NORMAL = 50;
        /**
         * 普通->窗口模式
         */
        public static final int SWITCH_WINDOW_OR_NORMAL = 51;

    }


    /**
     * 调节模式
     */
    public  class PlayAdjust {
        /**
         * 音量调节
         */
        public static final int ADJUST_VOLUME = 20;
        /**
         * 亮度调节
         */
        public static final int ADJUST_LIGHT = 21;
        /**
         * 快退快进
         */
        public static final int ADJUST_VIDEO = 22;
        /**
         * 不调节
         */
        public static final int ADJUST_UNKNOWN = 23;
    }



    /**
     * 播放形式
     */
  public   class PlayForm {
        /**
         * 顺序播放
         */
        public static final int PLAY_FORM_TURN = 40;
        /**
         * 单视频循环
         */
        public static final int PLAY_ONE_LOOP = 41;
        /**
         * 单视频播放
         */
        public static final int PLAY_ONE_END = 42;
    }
}
