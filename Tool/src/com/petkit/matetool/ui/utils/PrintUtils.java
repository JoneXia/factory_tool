package com.petkit.matetool.ui.utils;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.dothantech.lpapi.IAtBitmap;
import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.mate.MainActivity;

import static com.petkit.matetool.utils.Globals.TEST_PASS;

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
    private static LPAPI api;
    private static PrintResultCallback callback;

    public static boolean isPrinterConnected() {
        // 调用LPAPI对象的getPrinterState方法获取当前打印机的连接状态
        IDzPrinter.PrinterState state = api.getPrinterState();

        // 打印机未连接
        if (state == null || state.equals(IDzPrinter.PrinterState.Disconnected)) {
            return false;
        }

        // 打印机正在连接
        if (state.equals(IDzPrinter.PrinterState.Connecting)) {
            return false;
        }

        // 打印机已连接
        return true;
    }

    public static void initApi(PrintResultCallback printResultCallback) {
        callback = printResultCallback;
        api = LPAPI.Factory.createInstance(mCallback);
    }

    public static  LPAPI getApi() {
        return api;
    }


    public static void quit() {
        if (api != null) {
            api.quit();
            api = null;
        }
    }


    public static boolean printText(String onedBarcde, String twodBarcde) {
        api.startJob(48, 30, 0);
        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
        api.draw2DQRCode(twodBarcde, 16, 2, 15);
        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0, 18, 48, 7, 0);
        api.drawText(onedBarcde, 0, 25, 48, 3, IAtBitmap.FontStyle.REGULAR);
        return api.commitJob();
    }

    private static final LPAPI.Callback mCallback = new LPAPI.Callback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。

        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterState arg1) {
            final IDzPrinter.PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    break;
                case Disconnected:
                    break;
                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(IDzPrinter.ProgressInfo arg0, Object arg1) {
        }

        @Override
        public void onPrinterDiscovery(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterInfo arg1) {
        }

        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(IDzPrinter.PrinterAddress address, Object bitmapData, IDzPrinter.PrintProgress progress, Object addiInfo) {
            LoadDialog.dismissDialog();
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    if (callback != null) {
                        callback.onPrintSuccess();
                    }
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    if (callback != null) {
                        callback.onPrintFailed();
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
