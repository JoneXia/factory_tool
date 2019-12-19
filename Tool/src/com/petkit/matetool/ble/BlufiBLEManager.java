package com.petkit.matetool.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.utils.PetkitToast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;


public class BlufiBLEManager {

    private BlufiClient mBlufiClient;
    protected BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter adapter;
    private Timer timer;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BlufiBLEManager() {
    }

    public static BlufiBLEManager getInstance() {
        return BlufiBLEManagerHolder.INSTANCE;
    }

    public void getBlufiClient(Context context, BluetoothDevice device) {
        close();
        mBlufiClient = new BlufiClient(context, device);
    }


    public void close() {
        if (mBlufiClient != null) {
            mBlufiClient.close();
            mBlufiClient = null;
        }
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (timer != null) {
                timer.cancel();
            }
            if (adapter != null) {
                BluetoothLeScanner inScanner = adapter.getBluetoothLeScanner();
                if (inScanner != null) {
                    inScanner.stopScan(scanCallback);
                }
            }
        } else {
            if (timer != null) {
                timer.cancel();
            }
            if (adapter != null) {
                adapter.stopLeScan(leScanCallback);
            }
        }
    }

    public void startScan(ScanFilter... scanFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothLeScanner scanner = null;
            scanner = adapter.getBluetoothLeScanner();
            if (!adapter.isEnabled() || scanner == null) {
                PetkitToast.showToast("蓝牙未开启");
                return;
            }


            scanner.startScan(Arrays.asList(scanFilter), new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (adapter != null) {
                        BluetoothLeScanner inScanner = adapter.getBluetoothLeScanner();
                        if (inScanner != null) {
                            inScanner.stopScan(scanCallback);
                        }
                    }
                }
            }, 15);

        } else {
            if (!adapter.isEnabled()) {
                PetkitToast.showToast("蓝牙未开启");
                return;
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.startLeScan(leScanCallback);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (adapter != null) {
                        adapter.stopLeScan(leScanCallback);
                    }
                }
            }, 15);
        }

    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            DeviceInfo deviceInfo = createDeviceInfo(device, rssi, scanRecord);
            final Intent broadcast = new Intent(BLEConsts.BROADCAST_SCANED_DEVICE);
            broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, deviceInfo);
            LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(broadcast);
        }
    };


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            PetkitLog.d("ScanResult: " + result.toString());
            BluetoothDevice device = result.getDevice();
            DeviceInfo deviceInfo = createDeviceInfo(device, result.getRssi(), result.getScanRecord().getBytes());
            PetkitLog.d("deviceInfo: " + deviceInfo.toString());
            final Intent broadcast = new Intent(BLEConsts.BROADCAST_SCANED_DEVICE);
            broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, deviceInfo);
            LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(broadcast);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

        }
    };

    /**
     * 连接设备
     *
     * @param context
     * @param device
     */
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connect(Context context, BluetoothDevice device) {
        getBlufiClient(context, device);
//        mBlufiClient.negotiateSecurity();
        mBlufiClient.setGattCallback(new GattCallback());
        mBlufiClient.setBlufiCallback(new BlufiCallbackMain());
        mBlufiClient.connect();
        PetkitLog.d(getClass().getSimpleName(), "connect begin:" + format.format(System.currentTimeMillis()));
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void postCustomData(byte[] data) {
        if (mBlufiClient == null) {
            PetkitLog.e(getClass().getSimpleName(), "BlufiClient is null");
            return;
        }
        PetkitLog.d(getClass().getSimpleName(), "post message:" + new String(data));
        try {
            mBlufiClient.postCustomData(data);
        } catch (Exception e) {
            PetkitLog.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            PetkitLog.d(getClass().getSimpleName(), "onConnectionStateChange time:" + format.format(System.currentTimeMillis()));

            String devAddr = gatt.getDevice().getAddress();
            PetkitLog.d(String.format("onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {// 成功执行连接
                Intent broadcast = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_GATT_SUCCESS);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(broadcast);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED: {
                        // 连接状态：已连接
                        Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_STATE_CONNECTED);
                        LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                    }
                    break;
                    case BluetoothProfile.STATE_DISCONNECTED: {
                        // 连接状态：已断开
                        Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_STATE_DISCONNECTED);
                        LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                        gatt.close();
                    }
                    break;
                }
            } else {
                Intent broadcast = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_GATT_FAIL);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(broadcast);
                gatt.close();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
//            PetkitLog.d(String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                updateMessage(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu), false);
//
//                if (mtu > 0 && mBlufiClient != null) {
//                    int blufiPkgLenLimit = mtu - 3;
//                    PetkitLog.d("BluFiClient setPostPackageLengthLimit " + blufiPkgLenLimit);
//                    mBlufiClient.setPostPackageLengthLimit(blufiPkgLenLimit);
//                }
//            } else {
//                updateMessage(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status), false);
//            }
//
//            onGattServiceCharacteristicDiscovered();

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            PetkitLog.d(String.format("onServicesDiscovered status=%d", status));
            PetkitLog.d(getClass().getSimpleName(), "onServicesDiscovered time:" + format.format(System.currentTimeMillis()));

            //onConnectionStateChange成功调用 gatt.discoverService 方法。
            //当这一个方法被调用之后，系统会异步执行发现服务的过程，直到onServicesDiscovered 被系统回调之后，手机设备和蓝牙设备才算是真正建立了可通信的连接。
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                gatt.disconnect();
            } else {
                Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_SUCCESS);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            PetkitLog.d(characteristic.getValue() + " status:" + (status == BluetoothGatt.GATT_SUCCESS));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
//                updateMessage(String.format(Locale.ENGLISH, "WriteChar error status %d", status), false);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class BlufiCallbackMain extends BlufiCallback {
        /**
         * 当扫描 Gatt 服务结束后调用该方法
         * service, writeChar, notifyChar 中有 null 的时候表示扫描失败
         *
         * @param client     BlufiClient
         * @param gatt       BluetoothGatt
         * @param service    null if discover Blufi GattService failed
         * @param writeChar  null if discover Blufi write GattCharacteristic failed
         * @param notifyChar null if discover Blufi notify GattCharacteristic failed
         */
        @Override
        public void onGattPrepared(BlufiClient client, BluetoothGatt gatt, BluetoothGattService service,
                                   BluetoothGattCharacteristic writeChar, BluetoothGattCharacteristic notifyChar) {
            if (service == null) {
                Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                PetkitLog.warn("Discover service failed");
                gatt.disconnect();
                return;
            }
            if (writeChar == null) {
                Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                PetkitLog.warn("Get write characteristic failed");
                gatt.disconnect();
                return;
            }
            if (notifyChar == null) {
                Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL);
                LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                PetkitLog.warn("Get notification characteristic failed");
                gatt.disconnect();
                return;
            }

//            int mtu = (int) BlufiApp.getInstance().settingsGet(
//                    SettingsConstants.PREF_SETTINGS_KEY_MTU_LENGTH, BlufiConstants.DEFAULT_MTU_LENGTH);
//            boolean requestMtu = gatt.requestMtu(mtu);
//            if (!requestMtu) {
//                PetkitLog.warn("Request mtu failed");
//                updateMessage(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu), false);
//                onGattServiceCharacteristicDiscovered();
//            }
        }

        /**
         * 收到 Device 的通知数据
         * 返回 false 表示处理尚未结束，交给 BlufiClient 继续后续处理
         * 返回 true 表示处理结束，后续将不再解析该数据，也不会调用回调方法
         *
         * @param client  BlufiClient
         * @param pkgType Blufi package type
         * @param subType Blufi subtype
         * @param data    notified
         * @return
         */
        @Override
        public boolean onGattNotification(BlufiClient client, int pkgType, int subType, byte[] data) {
            return super.onGattNotification(client, pkgType, subType, data);
        }

        /**
         * 与 Device 协商加密的结果
         *
         * @param client BlufiClient
         * @param status {@link #STATUS_SUCCESS} means negotiate security success
         */
        @Override
        public void onNegotiateSecurityResult(BlufiClient client, int status) {
            PetkitLog.d("onNegotiateSecurityResult:status:" + status);
//            switch (status) {
//                case STATUS_SUCCESS:
//                    updateMessage("Negotiate security complete", false);
//                    break;
//                default:
//                    updateMessage("Negotiate security failed， code=" + status, false);
//                    break;
//            }
//
//            mBlufiSecurityBtn.setEnabled(mConnected);
        }

        /**
         * 发送配置信息的结果
         *
         * @param client BlufiClient
         * @param status {@link #STATUS_SUCCESS} means post data success
         */
        @Override
        public void onConfigureResult(BlufiClient client, int status) {
//            switch (status) {
//                case STATUS_SUCCESS:
//                    updateMessage("Post configure params complete", false);
//                    break;
//                default:
//                    updateMessage("Post configure params failed, code=" + status, false);
//                    break;
//            }
//
//            mBlufiConfigureBtn.setEnabled(mConnected);
        }

        /**
         * 收到 Device 的状态信息
         *
         * @param client   BlufiClient
         * @param status   {@link #STATUS_SUCCESS} means response is valid
         * @param response BlufiStatusResponse
         */
        @Override
        public void onDeviceStatusResponse(BlufiClient client, int status, BlufiStatusResponse response) {
            switch (status) {
                case STATUS_SUCCESS:
                    PetkitLog.d(String.format("Receive device status response:\n%s", response.generateValidInfo()));
                    break;
                default:
                    PetkitLog.d("Device status response error, code=" + status);
                    break;
            }

//            mBlufiDeviceStatusBtn.setEnabled(mConnected);
        }

        /**
         * 收到 Device 扫描到的 Wi-Fi 信息
         *
         * @param client  BlufiClient
         * @param status  {@link #STATUS_SUCCESS} means response is valid
         * @param results scan result list
         */
        @Override
        public void onDeviceScanResult(BlufiClient client, int status, List<BlufiScanResult> results) {
//            switch (status) {
//                case STATUS_SUCCESS:
//                    StringBuilder msg = new StringBuilder();
//                    msg.append("Receive device scan result:\n");
//                    for (BlufiScanResult scanResult : results) {
//                        msg.append(scanResult.toString()).append("\n");
//                    }
//                    updateMessage(msg.toString(), true);
//                    break;
//                default:
//                    updateMessage("Device scan result error, code=" + status, false);
//                    break;
//            }
//
//            mBlufiDeviceScanBtn.setEnabled(mConnected);
        }

        /**
         * 收到 Device 的版本信息
         *
         * @param client   BlufiClient
         * @param status   {@link #STATUS_SUCCESS} means response is valid
         * @param response BlufiVersionResponse
         */
        @Override
        public void onDeviceVersionResponse(BlufiClient client, int status, BlufiVersionResponse response) {
//            switch (status) {
//                case STATUS_SUCCESS:
//                    updateMessage(String.format("Receive device version: %s", response.getVersionString()),
//                            true);
//                    break;
//                default:
//                    updateMessage("Device version error, code=" + status, false);
//                    break;
//            }
//
//            mBlufiVersionBtn.setEnabled(mConnected);
        }

        /**
         * 发送自定义数据的结果
         *
         * @param client BlufiClient
         * @param status {@link #STATUS_SUCCESS} means post data success
         * @param data   posted
         */
        @Override
        public void onPostCustomDataResult(BlufiClient client, int status, byte[] data) {

            PetkitLog.d("onPostCustomDataResult:status:" + status + " data:" + new String(data));

//            String dataStr = new String(data);
//            String format = "Post data %s %s";
//            switch (status) {
//                case STATUS_SUCCESS:
//                    updateMessage(String.format(format, dataStr, "complete"), false);
//                    break;
//                default:
//                    updateMessage(String.format(format, dataStr, "failed"), false);
//                    break;
//            }
        }

        /**
         * 收到 Device 的自定义信息
         *
         * @param client BlufiClient
         * @param status {@link #STATUS_SUCCESS} means receive data success
         * @param data   received
         */
        @Override
        public void onReceiveCustomData(BlufiClient client, int status, byte[] data) {
            switch (status) {
                case STATUS_SUCCESS:
                    Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_RECEIVE_MESSAGE);
                    intent.putExtra(BlufiBLEConsts.BLUFI_BLE_EXTRA_DATA, data);
                    LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
                    break;
                default:
                    PetkitLog.e(getClass().getSimpleName(), "Receive custom data error, code=" + status);
                    break;
            }


        }

        /**
         * 收到 Device 发出的错误代码
         *
         * @param client  BlufiClient
         * @param errCode received
         */
        @Override
        public void onError(BlufiClient client, int errCode) {
            Intent intent = new Intent(BlufiBLEConsts.BROADCAST_BLUFI_BLE_ERROR);
            intent.putExtra(BlufiBLEConsts.BLUFI_BLE_EXTRA_ERROR_CODE, errCode);
            LocalBroadcastManager.getInstance(CommonUtils.getAppContext()).sendBroadcast(intent);
        }
    }

    protected DeviceInfo createDeviceInfo(BluetoothDevice device, int rssi,
                                          byte[] scanRecord) {
        DeviceInfo deviceInfo = new DeviceInfo(device, rssi, scanRecord);
        return deviceInfo;
    }

    private static class BlufiBLEManagerHolder {
        private static BlufiBLEManager INSTANCE = new BlufiBLEManager();
    }

}
