package com.petkit.matetool.model;

import java.io.Serializable;
import java.util.Objects;

public class UDPScanRecord implements Serializable {

    private String type;
    private int step;

    public UDPScanRecord() {
    }

    public UDPScanRecord(String type, int step) {
        this.type = type;
        this.step = step;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UDPScanRecord)) return false;
        UDPScanRecord record = (UDPScanRecord) o;
        return getStep() == record.getStep() && Objects.equals(getType(), record.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getStep());
    }

    @Override
    public String toString() {
        return "type='" + type + '\'';
    }
}
