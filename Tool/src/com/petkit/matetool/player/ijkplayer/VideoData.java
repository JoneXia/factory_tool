package com.petkit.matetool.player.ijkplayer;

public class VideoData {
    private String name;
    private String url;
    private long size;
    private long progress;
    private float frameSpeed;

    public VideoData(String name, String url, long size, long progress) {
        this.name = name;
        this.url = url;
        this.size = size;
        this.progress = progress;
    }

    public VideoData(String name, String url, long size, long progress, float frameSpeed) {
        this.name = name;
        this.url = url;
        this.size = size;
        this.progress = progress;
        this.frameSpeed = frameSpeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public float getFrameSpeed() {
        return frameSpeed;
    }

    public void setFrameSpeed(float frameSpeed) {
        this.frameSpeed = frameSpeed;
    }
}
