package com.petkit.matetool.ui.P3.mode;

import com.petkit.android.utils.ByteUtil;

public class GsensorData {

    private short x;
    private short y;
    private short z;

    public GsensorData(short x, short y, short z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GsensorData(byte[] data) {
        byte[] temp = new byte[2];

        System.arraycopy(data, 0, temp, 0, 2);
        x = ByteUtil.bytes2Short(temp);

        System.arraycopy(data, 2, temp, 0, 2);
        y = ByteUtil.bytes2Short(temp);

        System.arraycopy(data, 4, temp, 0, 2);
        z = ByteUtil.bytes2Short(temp);
//        z = (short) ((ByteUtil.bytes2Short(temp) >> 2) & 0xff);
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getZ() {
        return z;
    }

    public void setZ(short z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
