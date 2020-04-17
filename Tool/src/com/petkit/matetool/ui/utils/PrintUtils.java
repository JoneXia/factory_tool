package com.petkit.matetool.ui.utils;

import android.content.Context;
import android.os.Bundle;

import com.dothantech.lpapi.IAtBitmap;
import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.petkit.android.widget.LoadDialog;

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
//    private static LPAPI api;
    private static PrintResultCallback callback;

    public static boolean isPrinterConnected() {
        // 调用LPAPI对象的getPrinterState方法获取当前打印机的连接状态
        IDzPrinter.PrinterState state = IDzPrinter.Factory.getInstance().getPrinterState();

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

    public static void setCallback(PrintResultCallback callback) {
        PrintUtils.callback = callback;
    }

    public static void init(Context context) {
        IDzPrinter.Factory.getInstance().init(context, mCallback);
    }


    public static void quit() {
        IDzPrinter.Factory.getInstance().quit();
    }


    public static boolean printText(String onedBarcde, String twodBarcde, int copies, PrintResultCallback printResultCallback) {
        callback = printResultCallback;
        IAtBitmap api = IAtBitmap.Factory.createInstance();

        api.startJob(48 * 100, 30 * 100);
        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
        api.draw2DQRCode(twodBarcde, 16 * 100, 2 * 100, 15 * 100);
        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0 * 100, 18 * 100, 48 * 100, 7 * 100, 0);
        api.drawText(onedBarcde, 0 * 100, 25 * 100, 48 * 100, 3 *100, 280, IAtBitmap.FontStyle.REGULAR);
        api.endJob();

        return IDzPrinter.Factory.getInstance().print(api, getPrintParam(copies, 0));
    }


    public static boolean printText(String onedBarcde, String twodBarcde) {
        IAtBitmap api = IAtBitmap.Factory.createInstance();

        api.startJob(48 * 100, 30 * 100);
        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
        api.draw2DQRCode(twodBarcde, 16 * 100, 2 * 100, 15 * 100);
        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0 * 100, 18 * 100, 48 * 100, 7 * 100, 0);
        api.drawText(onedBarcde, 0 * 100, 25 * 100, 48 * 100, 3 *100, 280, IAtBitmap.FontStyle.REGULAR);
        api.endJob();

        return IDzPrinter.Factory.getInstance().print(api, getPrintParam(1, 0));
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
                    if (callback != null) {
                        callback.onConnected();
                    }
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

    // 获取打印时需要的打印参数
    private static Bundle getPrintParam(int copies, int orientation) {
        Bundle param = new Bundle();
        param.putInt(IDzPrinter.PrintParamName.PRINT_DIRECTION, orientation);
        param.putInt(IDzPrinter.PrintParamName.PRINT_COPIES, copies);
        param.putInt(IDzPrinter.PrintParamName.GAP_TYPE, 2);
        param.putInt(IDzPrinter.PrintParamName.PRINT_DENSITY, 14);
        param.putInt(IDzPrinter.PrintParamName.PRINT_SPEED, 2);

        return param;
    }


}
