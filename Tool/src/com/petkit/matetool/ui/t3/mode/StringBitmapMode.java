package com.petkit.matetool.ui.t3.mode;

public class StringBitmapMode {

    private String name;
    private String text;

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
}
