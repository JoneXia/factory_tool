package com.petkit.android.utils;


import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * In Java, it don't support unsigned int, so we use char to replace uint8.
 * The range of byte is [-128,127], and the range of char is [0,65535].
 * So the byte could used to store the uint8.
 * (We assume that the String could be mapped to assic)
 *
 * @author afunx
 */
public class ByteUtil {

    public static final String ESPTOUCH_ENCODING_CHARSET = "ISO-8859-1";

    /**
     * Put String to byte[]
     *
     * @param destbytes  the byte[] of dest
     * @param srcString  the String of src
     * @param destOffset the offset of byte[]
     * @param srcOffset  the offset of String
     * @param count      the count of dest, and the count of src as well
     */
    public static void putString2bytes(byte[] destbytes, String srcString,
                                       int destOffset, int srcOffset, int count) {
        for (int i = 0; i < count; i++) {
            destbytes[count + i] = srcString.getBytes()[i];
        }
    }

    /**
     * Convert uint8 into char( we treat char as uint8)
     *
     * @param uint8 the unit8 to be converted
     * @return the byte of the unint8
     */
    public static byte convertUint8toByte(char uint8) {
        if (uint8 > Byte.MAX_VALUE - Byte.MIN_VALUE) {
            throw new RuntimeException("Out of Boundary");
        }
        return (byte) uint8;
    }

    /**
     * Convert char into uint8( we treat char as uint8 )
     *
     * @param b the byte to be converted
     * @return the char(uint8)
     */
    public static char convertByte2Uint8(byte b) {
        // char will be promoted to int for char don't support & operator
        // & 0xff could make negatvie value to positive
        return (char) (b & 0xff);
    }

    /**
     * Convert byte[] into char[]( we treat char[] as uint8[])
     *
     * @param bytes the byte[] to be converted
     * @return the char[](uint8[])
     */
    public static char[] convertBytes2Uint8s(byte[] bytes) {
        int len = bytes.length;
        char[] uint8s = new char[len];
        for (int i = 0; i < len; i++) {
            uint8s[i] = convertByte2Uint8(bytes[i]);
        }
        return uint8s;
    }

    /**
     * Put byte[] into char[]( we treat char[] as uint8[])
     *
     * @param dest       the char[](uint8[]) array
     * @param src        the byte[]
     * @param destOffset the offset of char[](uint8[])
     * @param srcOffset  the offset of byte[]
     * @param count      the count of dest, and the count of src as well
     */
    public static void putbytes2Uint8s(char[] destUint8s, byte[] srcBytes,
                                       int destOffset, int srcOffset, int count) {
        for (int i = 0; i < count; i++) {
            destUint8s[destOffset + i] = convertByte2Uint8(srcBytes[srcOffset
                    + i]);
        }
    }

    /**
     * Convert byte to Hex String
     *
     * @param b the byte to be converted
     * @return the Hex String
     */
    public static String convertByte2HexString(byte b) {
        char u8 = convertByte2Uint8(b);
        return Integer.toHexString(u8);
    }

    /**
     * Convert char(uint8) to Hex String
     *
     * @param u8 the char(uint8) to be converted
     * @return the Hex String
     */
    public static String convertU8ToHexString(char u8) {
        return Integer.toHexString(u8);
    }

    /**
     * Split uint8 to 2 bytes of high byte and low byte. e.g. 20 = 0x14 should
     * be split to [0x01,0x04] 0x01 is high byte and 0x04 is low byte
     *
     * @param uint8 the char(uint8)
     * @return the high and low bytes be split, byte[0] is high and byte[1] is
     * low
     */
    public static byte[] splitUint8To2bytes(char uint8) {
        if (uint8 < 0 || uint8 > 0xff) {
            throw new RuntimeException("Out of Boundary");
        }
        String hexString = Integer.toHexString(uint8);
        byte low;
        byte high;
        if (hexString.length() > 1) {
            high = (byte) Integer.parseInt(hexString.substring(0, 1), 16);
            low = (byte) Integer.parseInt(hexString.substring(1, 2), 16);
        } else {
            high = 0;
            low = (byte) Integer.parseInt(hexString.substring(0, 1), 16);
        }
        byte[] result = new byte[]{high, low};
        return result;
    }

    /**
     * Combine 2 bytes (high byte and low byte) to one whole byte
     *
     * @param high the high byte
     * @param low  the low byte
     * @return the whole byte
     */
    public static byte combine2bytesToOne(byte high, byte low) {
        if (high < 0 || high > 0xf || low < 0 || low > 0xf) {
            throw new RuntimeException("Out of Boundary");
        }
        return (byte) (high << 4 | low);
    }

    /**
     * Combine 2 bytes (high byte and low byte) to
     *
     * @param high the high byte
     * @param low  the low byte
     * @return the char(u8)
     */
    public static char combine2bytesToU16(byte high, byte low) {
        char highU8 = convertByte2Uint8(high);
        char lowU8 = convertByte2Uint8(low);
        return (char) (highU8 << 8 | lowU8);
    }

    /**
     * Generate the random byte to be sent
     *
     * @return the random byte
     */
    private static byte randomByte() {
        return (byte) (127 - new Random().nextInt(256));
    }

    /**
     * Generate the random byte to be sent
     *
     * @param len the len presented by u8
     * @return the byte[] to be sent
     */
    public static byte[] randomBytes(char len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = randomByte();
        }
        return data;
    }

    public static byte[] genSpecBytes(char len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = '1';
        }
        return data;
    }

    /**
     * Generate the random byte to be sent
     *
     * @param len the len presented by byte
     * @return the byte[] to be sent
     */
    public static byte[] randomBytes(byte len) {
        char u8 = convertByte2Uint8(len);
        return randomBytes(u8);
    }

    /**
     * Generate the specific byte to be sent
     *
     * @param len the len presented by byte
     * @return the byte[]
     */
    public static byte[] genSpecBytes(byte len) {
        char u8 = convertByte2Uint8(len);
        return genSpecBytes(u8);
    }

    public static String parseBssid(byte[] bssidBytes, int offset, int count) {
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = bssidBytes[i + offset];
        }
        return parseBssid(bytes);
    }

    /**
     * parse "24,-2,52,-102,-93,-60" to "18,fe,34,9a,a3,c4"
     * parse the bssid from hex to String
     *
     * @param bssidBytes the hex bytes bssid, e.g. {24,-2,52,-102,-93,-60}
     * @return the String of bssid, e.g. 18fe349aa3c4
     */
    public static String parseBssid(byte[] bssidBytes) {
        StringBuilder sb = new StringBuilder();
        int k;
        String hexK;
        String str;
        for (int i = 0; i < bssidBytes.length; i++) {
            k = 0xff & bssidBytes[i];
            hexK = Integer.toHexString(k);
            str = ((k < 16) ? ("0" + hexK) : (hexK));
            System.out.println(str);
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * @param string the string to be used
     * @return the byte[] of String according to {@link #ESPTOUCH_ENCODING_CHARSET}
     */
    public static byte[] getBytesByString(String string) {
        try {
            return string.getBytes(ESPTOUCH_ENCODING_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("the charset is invalid");
        }
    }

    private static void test_splitUint8To2bytes() {
        // 20 = 0x14
        byte[] result = splitUint8To2bytes((char) 20);
        if (result[0] == 1 && result[1] == 4) {
            System.out.println("test_splitUint8To2bytes(): pass");
        } else {
            System.out.println("test_splitUint8To2bytes(): fail");
        }
    }

    private static void test_combine2bytesToOne() {
        byte high = 0x01;
        byte low = 0x04;
        if (combine2bytesToOne(high, low) == 20) {
            System.out.println("test_combine2bytesToOne(): pass");
        } else {
            System.out.println("test_combine2bytesToOne(): fail");
        }
    }

    private static void test_convertChar2Uint8() {
        byte b1 = 'a';
        // -128: 1000 0000 should be 128 in unsigned char
        // -1: 1111 1111 should be 255 in unsigned char
        byte b2 = (byte) -128;
        byte b3 = (byte) -1;
        if (convertByte2Uint8(b1) == 97 && convertByte2Uint8(b2) == 128
                && convertByte2Uint8(b3) == 255) {
            System.out.println("test_convertChar2Uint8(): pass");
        } else {
            System.out.println("test_convertChar2Uint8(): fail");
        }
    }

    private static void test_convertUint8toByte() {
        char c1 = 'a';
        // 128: 1000 0000 should be -128 in byte
        // 255: 1111 1111 should be -1 in byte
        char c2 = 128;
        char c3 = 255;
        if (convertUint8toByte(c1) == 97 && convertUint8toByte(c2) == -128
                && convertUint8toByte(c3) == -1) {
            System.out.println("test_convertUint8toByte(): pass");
        } else {
            System.out.println("test_convertUint8toByte(): fail");
        }
    }

    private static void test_parseBssid() {
        byte b[] = {15, -2, 52, -102, -93, -60};
        if (parseBssid(b).equals("0ffe349aa3c4")) {
            System.out.println("test_parseBssid(): pass");
        } else {
            System.out.println("test_parseBssid(): fail");
        }
    }

    public static void main(String args[]) {
        test_convertUint8toByte();
        test_convertChar2Uint8();
        test_splitUint8To2bytes();
        test_combine2bytesToOne();
        test_parseBssid();
    }


    public static byte[] makeUpBtyesForward(byte[] data, int len) {
        byte[] res = new byte[len];
        if (data.length < len) {
            int num = 0;
            for (int count = 0; count < len; count++) {
                if (count < (len - data.length)) {
                    res[count] = 0x00;
                } else {
                    res[count] = data[num];
                    num++;
                }
            }
        } else {
            return data;
        }
        return res;
    }


    public static byte[] makeUpBtyesBackward(byte[] data, int len) {
        byte[] res = new byte[len];
        if (data.length < len) {
            for (int count = 0; count < len; count++) {
                if (count < data.length) {
                    res[count] = data[count];
                } else {
                    res[count] = 0x00;
                }
            }
        } else {
            return data;
        }
        return res;
    }

    public static byte[] mergeBytes(byte[] data1, byte[] data2) {
        byte[] res = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, res, 0, data1.length);
        System.arraycopy(data2, 0, res, data1.length, data2.length);
        return res;
    }

    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }



    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static int bytes2Int(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < 4; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    public static String toHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int number = b & 0xff;
            String str = Integer.toHexString(number);
            if (str.length() == 1) {
                sb.append("0");
            }
            sb.append(str);
        }
        return sb.toString();
    }


    public static byte[] short2Bytes(short s) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = 16 - (i + 1) * 8;
            b[i] = (byte) ((s >> offset) & 0xff);
        }
        return b;
    }

    public static short bytes2Short(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8;
            l |= (b[i] & 0xff);
        }
        return l;
    }

    public static int toInt(byte b) {
        return b & 0xff;
    }

    public static byte[] toByte(int i) {
        byte[] b = new byte[1];
        b[0] = (byte) (i & 0xff);
        return b;
    }

    public static byte[] reverseBytes(byte[] bytes) {
        byte[] b = new byte[bytes.length];
        for (int count = 0; count < bytes.length; count++) {
            b[count] = bytes[bytes.length - 1 - count];
        }
        return b;
    }

    public static byte[] byteToBit(byte a) {
        byte[] temp = new byte[8];
        for (int i = 7; i >= 0; i--) {
            temp[i] = (byte) ((a >> i) & 1);
        }
        return temp;
    }

    public static byte bitToByte(byte[] bits) {
        byte temp = (byte) 0;
        for (int i = 0; i < bits.length; i++) {
            temp = (byte) (temp | bits[i] << i);
        }
        return temp;
    }

}
