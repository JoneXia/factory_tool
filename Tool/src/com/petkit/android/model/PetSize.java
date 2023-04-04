package com.petkit.android.model;

import java.io.Serializable;

/**
 * Created by YuanmengZeng on 2016/1/21.
 */
public class PetSize implements Serializable {

    private final static Long serialVersionUID = 5644313570781431705L;

    private int id;
    private String name;
    private String detail;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
