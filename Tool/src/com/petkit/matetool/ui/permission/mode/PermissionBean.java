package com.petkit.matetool.ui.permission.mode;

import java.io.Serializable;

/**
 * Created by petkit on 16/5/11.
 */
public class PermissionBean implements Serializable {

    private String content;
    private int name;
    private int icon;

    public PermissionBean(String content, int name, int icon) {
        this.content = content;
        this.name = name;
        this.icon = icon;
    }

    public String getContent() {
        return content;
    }

    public int getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }
}
