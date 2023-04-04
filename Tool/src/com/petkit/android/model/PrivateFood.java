package com.petkit.android.model;

import java.io.Serializable;

/**
 *
 * Created by YuanmengZeng on 2016/3/1.
 */
public class PrivateFood implements Serializable {

    private static final long serialVersionUID = 3126226861236859708L;

    private String createdAt;
    private String name ;
    private String userId;
    private int id ;
    private int status ;
    private int type;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public int getType() {
        return type;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setType(int type) {
        this.type = type;
    }
}
