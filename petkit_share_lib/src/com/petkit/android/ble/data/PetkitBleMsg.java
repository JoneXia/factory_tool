package com.petkit.android.ble.data;



import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static com.petkit.android.ble.data.BaseDataUtils.TYPE_PETKIT_BLE_REQUEST;

public class PetkitBleMsg implements Serializable {


    private long ts;
    private int cmd = -1;
    private byte[] data;
    private int type;
    private int sequence;
    private int totalNumber;
    private int index;

    private byte[] rawData;
    private int dataLen;


    public PetkitBleMsg() {
    }

    public PetkitBleMsg(int cmd) {
        this.cmd = cmd;
        this.type = TYPE_PETKIT_BLE_REQUEST;
    }

    public PetkitBleMsg(int cmd, byte[] data, int type) {
        this.cmd = cmd;
        this.data = data;
        this.type = type;
    }

    public PetkitBleMsg(int cmd, byte[] data) {
        this.cmd = cmd;
        this.data = data;
        this.type = TYPE_PETKIT_BLE_REQUEST;
    }

    public PetkitBleMsg(PetkitBleMsg data) {
        this.ts = data.getTs();
        this.cmd = data.getCmd();
        this.data = data.getData();
        this.type = data.getType();
        this.sequence = data.getSequence();
        this.rawData = data.rawData;
    }

    public PetkitBleMsg(int cmd, byte[] data, int type, int sequence) {
        this.cmd = cmd;
        this.data = data;
        this.type = type;
        this.sequence = sequence;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.totalNumber = totalNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "PetkitBleMsg{" +
                "ts=" + ts +
                ", cmd=" + cmd +
                ", type=" + type +
                ", sequence=" + sequence +
                ", rawData=" + Arrays.toString(rawData) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PetkitBleMsg)) return false;
        PetkitBleMsg msg = (PetkitBleMsg) o;
        return getCmd() == msg.getCmd() &&
                getSequence() == msg.getSequence();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCmd(), getSequence());
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public byte[] getRawData() {
        return rawData;
    }




}
