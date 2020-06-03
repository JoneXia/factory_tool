package com.petkit.matetool.ui.t3.mode;

import java.util.ArrayList;

public class LocaleStringBitmapMode {

    private String locale;

    private ArrayList<StringBitmapMode> strings;

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public ArrayList<StringBitmapMode> getStrings() {
        return strings;
    }

    public void setStrings(ArrayList<StringBitmapMode> strings) {
        this.strings = strings;
    }

    public String getLocale() {
        return locale;
    }
}
