package com.nuvoton.nuoneex;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.petkit.android.utils.PetkitLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *  D3设备录音文件格式(NUO)编解码转换方法
 */
public class MainActivity {

    static {
        System.loadLibrary("NuOneExLib");
    }


    public native int EncodeFile(byte[] FileData, int FileSize, short BPS, byte[] pi16CompressBuffer, int SampleRate);

    public native int DecodeFile(byte[] FileData, int FileSize, byte[] pi16PcmBuffer);


    public void encodePcmToNuo(Context context,int sampleRate, String src, String dst) {
        PetkitLog.d("encodePcmToNuo", "src:" + src + " dst:" + dst);

        String srcPath = src;
        FileInputStream srcFile = null;
        BufferedInputStream srcBuf = null;

        String dstPath = dst;
        FileOutputStream dstFile = null;
        BufferedOutputStream dstBuf = null;

        try {
            srcFile = new FileInputStream(srcPath);
            srcBuf = new BufferedInputStream(srcFile);
            byte[] FileData = new byte[srcBuf.available()];

            short BPS = 320; // 1.0
            dstFile = new FileOutputStream(dstPath);
            dstBuf = new BufferedOutputStream(dstFile);
            byte[] pi16CompressBuffer = new byte[((srcBuf.available() + 640 - 1) / 640 + 1) * (BPS / 8) + 12];
            int BufSize = 0;

            int FileSize = srcBuf.read(FileData);
            BufSize = EncodeFile(FileData, FileSize, (short) BPS, pi16CompressBuffer, sampleRate);

            dstBuf.write(pi16CompressBuffer, 0, BufSize);

            srcBuf.close();
            dstBuf.close();
        } catch (Exception e) {
            PetkitLog.d("encodePcmToNuo", "error:" + e.getMessage());
            e.printStackTrace();
        }
        scanFile(context, dstPath);
    }


    public void DecodeNuoToPcm(Context context, String src, String dst) {
        String srcPath = src;
        FileInputStream srcFile = null;
        BufferedInputStream srcBuf = null;

        String dstPath = dst;
        FileOutputStream dstFile = null;
        BufferedOutputStream dstBuf = null;

        try {
            srcFile = new FileInputStream(srcPath);
            srcBuf = new BufferedInputStream(srcFile);
            byte[] FileData = new byte[srcBuf.available()];
            int FileSize = srcBuf.read(FileData);

            dstFile = new FileOutputStream(dstPath);
            dstBuf = new BufferedOutputStream(dstFile);
            int BPS = FileData[9] * 256 + FileData[8];

            int SampleCount = ((FileSize - 12) / (BPS / 8) * 320);
            byte[] pi16PcmBuffer = new byte[SampleCount * 2];
            SampleCount = DecodeFile(FileData, FileSize, pi16PcmBuffer);

            dstBuf.write(pi16PcmBuffer);

            srcBuf.close();
            dstBuf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanFile(context, dstPath);
    }


    private void scanFile(Context context, String filePath) {
        final String[] arrayOfPaths = {filePath};
        MediaScannerConnection.scanFile(context, arrayOfPaths, null, null);
    }

}
