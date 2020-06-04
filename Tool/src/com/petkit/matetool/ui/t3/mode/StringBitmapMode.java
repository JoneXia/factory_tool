package com.petkit.matetool.ui.t3.mode;

public class StringBitmapMode {

    private String name;
    private String text;
    private int width, height, size;

    public StringBitmapMode() {
    }

    public StringBitmapMode(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getConfigString() {
        return String.format("%s,%d,%d,%d", name, size, width, height);
    }
}
