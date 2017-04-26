package com.petkit.matetool.ui.feeder.utils;

import com.dothantech.common.DzToast;
import com.dothantech.printer.IDzPrinter;
import com.petkit.matetool.R;

/**
 * Created by Jone on 17/4/24.
 */

public class PrintUtils {

    // 保存各种信息时的名称
    public static final String KeyPrintQuality = "PrintQuality";
    public static final String KeyPrintDensity = "PrintDensity";
    public static final String KeyPrintSpeed = "PrintSpeed";
    public static final String KeyGapType = "GapType";

    public static final String KeyLastPrinterMac = "LastPrinterMac";
    public static final String KeyLastPrinterName = "LastPrinterName";
    public static final String KeyLastPrinterType = "LastPrinterType";

    public static final String KeyDefaultText1 = "DefaultText1";
    public static final String KeyDefaultText2 = "DefaultText2";
    public static final String KeyDefault1dBarcode = "Default1dBarcode";
    public static final String KeyDefault2dBarcode = "Default2dBarcode";

    public static boolean isPrinterConnected() {
        // 调用IDzPrinter对象的getPrinterState方法获取当前打印机的连接状态
        IDzPrinter.PrinterState state = IDzPrinter.Factory.getInstance().getPrinterState();

        // 打印机未连接
        if (state == null || state.equals(IDzPrinter.PrinterState.Disconnected)) {
            DzToast.show(R.string.pleaseconnectprinter);
            return false;
        }

        // 打印机正在连接
        if (state.equals(IDzPrinter.PrinterState.Connecting)) {
            DzToast.show(R.string.waitconnectingprinter);
            return false;
        }

        // 打印机已连接
        return true;
    }
    
}
