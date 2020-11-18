package com.petkit.matetool.ui.wifi;

import android.net.wifi.ScanResult;

import java.util.Comparator;

public class ScanResultSortUtil implements Comparator<ScanResult> {

    public int compare(ScanResult o1, ScanResult o2) {
        ScanResult scanResult = o1;
        ScanResult scanResult1 = o2;

        return scanResult1.level - scanResult.level;
    }
}
