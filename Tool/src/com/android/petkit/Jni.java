package com.android.petkit;


public class Jni {
    //Jni采用单例模式
    private static Jni instance=null;
    private Jni(){
    }
    public static Jni getInstance(){
        if(instance==null){
            instance =new Jni();
        }
        return instance;
    }

    //1——初始化库，注册回调函数，设置视频通话的若干基本参数
    public native void initLib(String serverip,int serverport,int videoformat);

    //2——读取来自服务器的视频数据
    public native int readVideoData(byte[] videoData);

    //3——读取来自服务器的音频数据
    public native int readAudioData(byte[] audioData);

    //4——释放库
    public native void releaseLib();

}
