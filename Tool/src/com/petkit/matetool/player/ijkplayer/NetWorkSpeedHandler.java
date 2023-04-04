package com.petkit.matetool.player.ijkplayer;

import android.content.Context;
import android.net.TrafficStats;

import com.petkit.android.utils.PetkitLog;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class NetWorkSpeedHandler {

    private long mLastTime = 0L;
    private long mLastRxBytes = 0L;
    private Context context;
    private long frequency;
    private Disposable disposable;

    public NetWorkSpeedHandler(Context context, long frequency) {
        this.context = context;
        this.frequency = frequency;
    }

    public void bindHandler(final OnNetWorkSpeedListener onNetWorkSpeedListener) {
        mLastTime = System.currentTimeMillis();
        mLastRxBytes = getTotalRxBytes(context);
        PetkitLog.d("netWorkSpeed", "bindHandler:");

        //开始计时
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
        Observable.interval(0, frequency, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        PetkitLog.d("netWorkSpeed", "getNetWorkSpeed:" + getNetWorkSpeed());
                        onNetWorkSpeedListener.netWorkSpeed(getNetWorkSpeed());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    public void unbindHandler() { //暂停计时
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    private long getTotalRxBytes(Context context) { //获取流量总量，转为KB
        if (TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED) {
            return 0;
        } else {
            return TrafficStats.getTotalRxBytes() / 1024;
        }
    }

    private String getNetWorkSpeed() {
        long nowTime = System.currentTimeMillis();
        long nowTotalRxBytes = getTotalRxBytes(context);
        float speed1 = ((nowTotalRxBytes - mLastRxBytes) * 1f) / ((nowTime - mLastTime) * 1f / 1000f) * 1024;
        mLastRxBytes = nowTotalRxBytes;
        return VideoUtils.parseByteSize(speed1);
    }


    public interface OnNetWorkSpeedListener {
        /**
         * 返回实时网速
         *
         * @param speed 实时网速
         */
        void netWorkSpeed(String speed);
    }
}
