package com.petkit.matetool.ui.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.dothantech.printer.IDzPrinter.PrintProgress;
import com.dothantech.printer.IDzPrinter.PrinterAddress;
import com.dothantech.printer.IDzPrinter.PrinterState;
import com.dothantech.printer.IDzPrinter.ProgressInfo;

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

    private static PrintResultCallback callback;
    private static LPAPI api;

    // 用于处理各种通知消息，刷新界面的handler
    private static final Handler mHandler = new Handler();

    /********************************************************************************************************************************************/
    // DzPrinter连接打印功能相关
    /********************************************************************************************************************************************/

    // LPAPI 打印机操作相关的回调函数。
    private static final LPAPI.Callback mCallback = new LPAPI.Callback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。
        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(PrinterAddress arg0, PrinterState arg1) {
            final PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    // 打印机连接成功，发送通知，刷新界面提示
                    mHandler.post(() -> {
                        if (callback != null) {
                            callback.onConnected();
                        }
                    });
                    break;

                case Disconnected:
                    break;

                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(ProgressInfo arg0, Object arg1) {
        }


        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(PrinterAddress address, Object bitmapData, PrintProgress progress, Object addiInfo) {
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    mHandler.post(() -> {
                        if (callback != null) {
                            callback.onPrintSuccess();
                        };
                    });
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    mHandler.post(() -> {
                        if (callback != null) {
                            callback.onPrintFailed();
                        };
                    });
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onPrinterDiscovery(PrinterAddress address, IDzPrinter.PrinterInfo info) {

        }
    };

    public static LPAPI getApi() {
        return api;
    }

    public static boolean isPrinterConnected() {
        // 调用LPAPI对象的getPrinterState方法获取当前打印机的连接状态
        PrinterState state = api.getPrinterState();

        // 打印机未连接
        if (state == null || state.equals(PrinterState.Disconnected)) {
            return false;
        }

        // 打印机正在连接
        if (state.equals(PrinterState.Connecting)) {
            return false;
        }

        // 打印机已连接
        return true;
    }

    public static void setCallback(PrintResultCallback callback) {
        PrintUtils.callback = callback;
    }

    public static void init(Context context) {
        api = LPAPI.Factory.createInstance(mCallback);
    }


    public static void quit() {
        IDzPrinter.Factory.getInstance().quit();
    }


    public static boolean printText(String onedBarcde, String twodBarcde, int copies, PrintResultCallback printResultCallback) {
//        callback = printResultCallback;
//        IAtBitmap api = IAtBitmap.Factory.createInstance();
//
//        api.startJob(48 * 100, 30 * 100);
//        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
//        api.draw2DQRCode(twodBarcde, 16 * 100, 2 * 100, 15 * 100);
//        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0 * 100, 18 * 100, 48 * 100, 7 * 100, 0);
//        api.drawText(onedBarcde, 0 * 100, 25 * 100, 48 * 100, 3 *100, 280, IAtBitmap.FontStyle.REGULAR);
//        api.endJob();

//        return IDzPrinter.Factory.getInstance().print(api, getPrintParam(copies, 0));
        return false;
    }


    public static boolean printText(String onedBarcde, String twodBarcde, int copies) {

        api.startJob(42, 23, 0);
        // 设置之后绘制的对象内容旋转180度
//        api.setItemOrientation(180);
//        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
        // 开始一个页面的绘制，绘制文本字符串
        // 传入参数(需要绘制的文本字符串, 绘制的文本框左上角水平位置, 绘制的文本框左上角垂直位置, 绘制的文本框水平宽度, 绘制的文本框垂直高度, 文字大小, 字体风格)
        api.drawText(onedBarcde, 3, 18, 32, 4, 4);

        // 绘制一维码，此一维码绘制时内容会旋转180度，
        // 传入参数(需要绘制的一维码的数据, 绘制的一维码左上角水平位置, 绘制的一维码左上角垂直位置, 绘制的一维码水平宽度, 绘制的一维码垂直高度)
//        api.draw1DBarcode(onedBarcde, LPAPI.BarcodeType.CODE128, 0, 18, 48, 7, 0);

        // 开始一个页面的绘制，绘制二维码
        // 传入参数(需要绘制的二维码的数据, 绘制的二维码左上角水平位置, 绘制的二维码左上角垂直位置, 绘制的二维码的宽度(宽高相同))
        api.draw2DQRCode(twodBarcde, 11, 2, 15);

        // 结束绘图任务提交打印
        return api.commitJob();

//        IAtBitmap api = IAtBitmap.Factory.createInstance();
//
//        api.startJob(48 * 100, 30 * 100);
//        api.setItemHorizontalAlignment(IAtBitmap.ItemAlignment.MIDDLE);
//        api.draw2DQRCode(twodBarcde, 16 * 100, 2 * 100, 15 * 100);
//        api.draw1DBarcode(onedBarcde, IAtBitmap.BarcodeType1D.CODE128, 0 * 100, 18 * 100, 48 * 100, 7 * 100, 0);
//        api.drawText(onedBarcde, 0 * 100, 25 * 100, 48 * 100, 3 *100, 280, IAtBitmap.FontStyle.REGULAR);
//        api.endJob();
//
//        return IDzPrinter.Factory.getInstance().print(api, getPrintParam(copies, 0));
    }

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
