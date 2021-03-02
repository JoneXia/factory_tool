package com.petkit.android.ble.data;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.utils.PetkitLog;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseDataUtils {

    //命令类型
    //Request
    public static final int TYPE_PETKIT_BLE_REQUEST = 1;
    //Non-Response Request 该请求无需回复
    public static final int TYPE_PETKIT_BLE_NON_RESPONSE_REQUEST = 3;
    //Response
    public static final int TYPE_PETKIT_BLE_RESPONSE = 2;

    public static final int TYPE_PETKIT_BLE_OTA_REQUEST = 0;

    private static AtomicInteger sendCount = new AtomicInteger(0);

    protected static byte[] CMD_HEADER = new byte[]{(byte) 0xfa, (byte) 0xfc, (byte) 0xfd};
    protected static byte[] STREAM_HEADER = new byte[]{(byte) 0xfa, (byte) 0xfc, (byte) 0xfe};


    public static byte[] buildOpCodeBuffer(int cmd) {
        return buildOpCodeBuffer(cmd, TYPE_PETKIT_BLE_REQUEST, null, generateSendSequence());
    }

    public static byte[] buildOpCodeBuffer(int cmd, byte[] data) {
        return buildOpCodeBuffer(cmd, TYPE_PETKIT_BLE_REQUEST, data, generateSendSequence());
    }

    public static byte[] buildOpCodeBuffer(int cmd, byte[] data, int sequence) {
        return buildOpCodeBuffer(cmd, TYPE_PETKIT_BLE_REQUEST, data, sequence);
    }

    public static byte[] buildOpCodeBuffer(int cmd, int type, byte[] data, int sequence) {
        if (cmd < 0) {
            return null;
        }

        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();

        byteOS.write(CMD_HEADER, 0, CMD_HEADER.length);
        byteOS.write(cmd);
        byteOS.write(type);
        byteOS.write(sequence);


        if (data != null && data.length > 0) {
            byteOS.write(data.length & 0xff);
            byteOS.write(data.length >> 8);

            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.BIG_ENDIAN);
            data = buffer.array();
            byteOS.write(data, 0, data.length);
        } else {
            byteOS.write(0x00);
            byteOS.write(0x00);
        }
        byteOS.write(0xfb);

        return byteOS.toByteArray();
    }

    public static PetkitBleMsg parseRawData(byte[] rawData) {
        if (rawData == null || rawData.length < 7) {
            return null;
        }

        PetkitLog.d("parseRawData  " + parse(rawData));

        if (CMD_HEADER[0] == rawData[0] && CMD_HEADER[1] == rawData[1] && CMD_HEADER[2] == rawData[2]) {//命令
            //命令字
            int cmd = (rawData[3] & 0xff);
            //命令类型
            int type = (rawData[4] & 0xff);
            //Sequence Number
            int sequence = (rawData[5] & 0xff);

            int dataLen = (short) (rawData[7] << 8) | rawData[6];
            byte[] data = new byte[dataLen];
            System.arraycopy(rawData, 8, data, 0, dataLen);

            return new PetkitBleMsg(cmd, data, type);
        }

        return null;
    }


    protected static String parse(final byte[] data) {
        if (data == null)
            return "";

        final int length = data.length;
        if (length == 0)
            return "";

        final char[] out = new char[length * 3 - 1];
        for (int j = 0; j < length; j++) {
            int v = data[j] & 0xFF;
            out[j * 3] = BLEConsts.HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = BLEConsts.HEX_ARRAY[v & 0x0F];
            if (j != length - 1)
                out[j * 3 + 2] = '-';
        }
        return new String(out);
    }

    private static int generateSendSequence() {
        return sendCount.getAndIncrement() % 256;
    }
}
