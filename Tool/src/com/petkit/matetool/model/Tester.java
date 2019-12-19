package com.petkit.matetool.model;

import java.io.Serializable;

/**
 * 测试者信息，设备测试需要，用于生成SN
 *
 * Created by Jone on 17/5/9.
 */
public class Tester implements Serializable {

    private String name;
    private String code;
    private String station;
    private String session;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }


    public void setSession(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }

    /**
     * 检查合法性
     * @return bool
     */
    public boolean checkValid() {
        return name != null && code != null && code.length() == 2 && station != null && station.length() == 1;
    }
}
