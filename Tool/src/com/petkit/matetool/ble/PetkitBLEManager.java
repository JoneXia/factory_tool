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
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.utils.PetkitToast;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import blufi.espressif.BlufiCallback;
import blufi.espressif.BlufiClient;
import blufi.espressif.response.BlufiScanResult;
import blufi.espressif.response.BlufiStatusResponse;
import blufi.espressif.response.BlufiVersionResponse;


public class PetkitBLEManager {

    private BlufiClient mBlufiClient;
    private BluetoothAdapter adapter;
    private Timer timer;
    private GattCallback gattCallback;
    private BlufiCallbackMain blufiCallbackMain;
    private SharedPreferences mSettingsShared;
    private Context context;
    private BluetoothDevice bluetoothDevice;
    public static final int BLE_SCAN_TIME = 15000;

    private onPetkitBleListener mBleListener;

    private PetkitBLEManager() {

    }

    public static PetkitBLEManager getInstance() {
        return BlufiBLEManagerHolder.INSTANCE;
    }

    public void setBleListener(onPetkitBleListener bleListener) {
        mBleListener = bleListener;
    }

    public void getBlufiClient(Context context, BluetoothDevice device) {
        this.context = context;
        close();
        mSettingsShared = context.getSharedPreferences(PetkitBLEConsts.PREF_SETTINGS_NAME, Context.MODE_PRIVATE);
        mBlufiClient = new BlufiClient(context, device);

//        mBlufiClient.negotiateSecurity();//加密
//        mBlufiClient.setPostPackageLengthLimit(128);// 发送数据每包数据最大长度
    }


    public void close() {

        if (mBlufiClient != null) {
            mBlufiClient.requestCloseConnection();
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

    public void startScan(ScanFilter... filters) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothLeScanner scanner = null;
            scanner = adapter.getBluetoothLeScanner();
            if (!adapter.isEnabled() || scanner == null) {
                PetkitToast.showToast("蓝牙未开启");
                return;
            }
            scanner.startScan(filters.length > 0 && filters[0] != null ? Arrays.asList(filters) : null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback);

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
            }, BLE_SCAN_TIME);

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
            }, BLE_SCAN_TIME);
        }

    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            DeviceInfo deviceInfo = createDeviceInfo(device, rssi, scanRecord);

            if (mBleListener != null) {
                mBleListener.onLeScan(device, deviceInfo);
            }
        }
    };


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            DeviceInfo deviceInfo = createDeviceInfo(device, result.getRssi(), result.getScanRecord().getBytes());

            if (mBleListener != null) {
                mBleListener.onLeScan(device, deviceInfo);
            }

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
        bluetoothDevice = device;
        getBlufiClient(context, device);
        gattCallback = new GattCallback();
        blufiCallbackMain = new BlufiCallbackMain();
        mBlufiClient.setGattCallback(gattCallback);
        mBlufiClient.setBlufiCallback(blufiCallbackMain);
        mBlufiClient.connect();

        changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_CONNECTING);
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void postCustomData(String data) {
        if (mBlufiClient == null) {
            PetkitLog.d(getClass().getSimpleName(), "BlufiClient is null");
            return;
        }
        PetkitLog.d(getClass().getSimpleName(), "postCustomData:" + data);
        try {
            mBlufiClient.postCustomData(data.getBytes());
        } catch (Exception e) {
            PetkitLog.d(getClass().getSimpleName(), e.getMessage());
            //TODO: RETRY?
        }
    }

    /**
     * mBlufiClient call onCharacteristicWrite and onCharacteristicChanged is required
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String devAddr = gatt.getDevice().getAddress();
            PetkitLog.d(String.format("onConnectionStateChange addr=%s, status=%d, newState=%d",
                    devAddr, status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {// 成功执行连接
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_GATT_SUCCESS);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED: {
                        // 连接状态：已连接
                        changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_CONNECTED);
                    }
                    break;
                    case BluetoothProfile.STATE_DISCONNECTED: {
                        // 连接状态：已断开
                        changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_DISCONNECTED);
                        gatt.close();
                    }
                    break;
                }
            } else {

                gatt.disconnect();
                gatt.close();

                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_GATT_FAILED);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            PetkitLog.d(String.format(Locale.ENGLISH, "onMtuChanged status=%d, mtu=%d", status, mtu));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                PetkitLog.d(String.format(Locale.ENGLISH, "Set mtu complete, mtu=%d ", mtu));

                if (mtu > 0 && mBlufiClient != null) {
                    int blufiPkgLenLimit = mtu - 3;
                    PetkitLog.d("BluFiClient setPostPackageLengthLimit " + blufiPkgLenLimit);
                    mBlufiClient.setPostPackageLengthLimit(blufiPkgLenLimit);
                }
            } else {
                PetkitLog.d(String.format(Locale.ENGLISH, "Set mtu failed, mtu=%d, status=%d", mtu, status));
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            PetkitLog.d(String.format("onServicesDiscovered status=%d", status));

            //onConnectionStateChange成功调用 gatt.discoverService 方法。
            //当这一个方法被调用之后，系统会异步执行发现服务的过程，直到onServicesDiscovered 被系统回调之后，手机设备和蓝牙设备才算是真正建立了可通信的连接。
            if (status != BluetoothGatt.GATT_SUCCESS) {
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_SERVICE_DISCOVERED_FAILED);
                gatt.disconnect();
            } else {
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_SERVICE_DISCOVERED_SUCCESS);

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            PetkitLog.d(" onCharacteristicWrite status:" + (status == BluetoothGatt.GATT_SUCCESS));
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect();
                PetkitLog.d(String.format(Locale.ENGLISH, "WriteChar error status %d", status));
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
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_SERVICE_DISCOVERED_FAILED);
                PetkitLog.warn("Discover service failed");
                gatt.disconnect();
                return;
            }
            if (writeChar == null) {
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_SERVICE_DISCOVERED_FAILED);
                PetkitLog.warn("Get write characteristic failed");
                gatt.disconnect();
                return;
            }
            if (notifyChar == null) {
                changeConnectState(PetkitBLEConsts.ConnectState.BLE_STATE_SERVICE_DISCOVERED_FAILED);
                PetkitLog.warn("Get notification characteristic failed");
                gatt.disconnect();
                return;
            }

            int mtu = (int) settingsGet(PetkitBLEConsts.PREF_SETTINGS_KEY_MTU_LENGTH, PetkitBLEConsts.DEFAULT_MTU_LENGTH);
            boolean requestMtu = gatt.requestMtu(mtu);
            if (!requestMtu) {
                PetkitLog.warn("Request mtu failed");
                PetkitLog.d(String.format(Locale.ENGLISH, "Request mtu %d failed", mtu));
            }
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
            switch (status) {
                case STATUS_SUCCESS:
                    PetkitLog.d("Negotiate security complete");
                    break;
                default:
                    PetkitLog.d("Negotiate security failed， code=" + status);
                    break;
            }
//
        }

        /**
         * 发送配置信息的结果
         *
         * @param client BlufiClient
         * @param status {@link #STATUS_SUCCESS} means post data success
         */
        @Override
        public void onConfigureResult(BlufiClient client, int status) {
            switch (status) {
                case STATUS_SUCCESS:
                    PetkitLog.d("Post configure params complete");
                    break;
                default:
                    PetkitLog.d("Post configure params failed, code=" + status);
                    break;
            }
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
            switch (status) {
                case STATUS_SUCCESS:
                    StringBuilder msg = new StringBuilder();
                    msg.append("Receive device scan result:\n");
                    for (BlufiScanResult scanResult : results) {
                        msg.append(scanResult.toString()).append("\n");
                    }
                    PetkitLog.d(msg.toString());
                    break;
                default:
                    PetkitLog.d("Device scan result error, code=" + status);
                    break;
            }
//
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
            switch (status) {
                case STATUS_SUCCESS:
                    PetkitLog.d(String.format("Receive device version: %s", response.getVersionString()));
                    break;
                default:
                    PetkitLog.d("Device version error, code=" + status);
                    break;
            }
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

            String dataStr = new String(data);
            switch (status) {
                case STATUS_SUCCESS:
                    PetkitLog.d("onPostCustomDataResult:complete status:" + status + " data:" + dataStr);

                    break;
                default:
                    PetkitLog.d("onPostCustomDataResult:failed status:" + status + " data:" + dataStr);

                    break;
            }
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
            LogcatStorageHelper.addLog(" Receive custom data, status:" + status + ", data: " + new String(data));
            switch (status) {
                case STATUS_SUCCESS:
                    JSONObject jsonObject = JSONUtils.getJSONObject(new String(data));
                    if(jsonObject != null && !jsonObject.isNull("key")) {
                        try {
                            int key = jsonObject.getInt("key");

                            if(mBleListener != null) {
                                mBleListener.onReceiveCustomData(key, jsonObject.isNull("payload") ? "" :
                                    jsonObject.getJSONObject("payload").toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            LogcatStorageHelper.addLog(" data format error " );
                        }
                    }
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
            if (mBleListener != null) {
                mBleListener.onError(errCode);
            }
        }
    }

    protected DeviceInfo createDeviceInfo(BluetoothDevice device, int rssi,
                                          byte[] scanRecord) {
        DeviceInfo deviceInfo = new DeviceInfo(device, rssi, scanRecord);
        return deviceInfo;
    }

    private static class BlufiBLEManagerHolder {
        private static PetkitBLEManager INSTANCE = new PetkitBLEManager();
    }

    public Object settingsGet(String key, Object defaultValue) {
        if (defaultValue instanceof String) {
            return mSettingsShared.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return mSettingsShared.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            return mSettingsShared.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return mSettingsShared.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            return mSettingsShared.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Set) {
            //noinspection unchecked
            return mSettingsShared.getStringSet(key, (Set<String>) defaultValue);
        } else {
            return null;
        }
    }

    private void changeConnectState(PetkitBLEConsts.ConnectState state) {
        if (mBleListener != null) {
            mBleListener.onStateChanged(state);
        }
    }


    public interface onPetkitBleListener {
        void onLeScan(BluetoothDevice device, DeviceInfo deviceInfo);
        void onStateChanged(PetkitBLEConsts.ConnectState state);
        void onReceiveCustomData(int key, String data);
        void onError(int errCode);
    }


}
