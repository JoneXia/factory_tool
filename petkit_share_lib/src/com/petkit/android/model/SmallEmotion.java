package com.petkit.android.model;/**
 * Created by Administrator on 2015-09-03.
 */

import android.content.Context;

/**
 * User:
 * Date: 2015-09-03
 * Time: 11:20
 */
public class SmallEmotion extends Emotion {

    private String mSource;
    private int mResourceId;

    public static SmallEmotion fromAssert(int  resourceId, String assertPath) {
        SmallEmotion emotionEntity = new SmallEmotion();
        emotionEntity.mResourceId = resourceId;
        emotionEntity.mSource = assertPath;
        return emotionEntity;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String mSource) {
        this.mSource = mSource;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setResourceId(int mResourceId) {
        this.mResourceId = mResourceId;
    }

    public String getCode(Context context) {
        return context.getResources().getString(mResourceId);
    }
}
