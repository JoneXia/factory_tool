package com.petkit.android.model;

import java.io.Serializable;

/**
 * Created by Jone on 16/7/8.
 */
public class ImageDetail implements Serializable {

    /**
     * h : 2560
     * url : http://sandbox.img5.petkit.com/post/2016/96d180d78fb7478c90b69d075f55b1cfzpdFp1C6
     * w : 1920
     */

    private int h;
    private String url;
    private int w;

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }
}
