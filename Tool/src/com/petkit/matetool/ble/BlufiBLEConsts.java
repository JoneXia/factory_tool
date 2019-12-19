package com.petkit.matetool.ble;

public class BlufiBLEConsts {


    public static final String BLUFI_BLE_EXTRA_ACTION = "BLUFI_BLE_EXTRA_ACTION";
    public static final String BLUFI_BLE_EXTRA_DEVICE = "BLUFI_BLE_EXTRA_DEVICE";
    public static final String BLUFI_BLE_EXTRA_DATA = "BLUFI_BLE_EXTRA_DATA";
    public static final String BLUFI_BLE_EXTRA_ERROR_CODE = "BLUFI_BLE_EXTRA_ERROR_CODE";

    public static final int BLUFI_BLE_ACTION_DEFAULT = 0x00;

    public static final int BLUFI_BLE_ACTION_CONNECT = 0x01;
    public static final int BLUFI_BLE_ACTION_POST = 0x02;


    public static final String BROADCAST_BLUFI_BLE_GATT_SUCCESS  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_GATT_SUCCESS";
    public static final String BROADCAST_BLUFI_BLE_GATT_FAIL  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_GATT_FAIL";
    public static final String BROADCAST_BLUFI_BLE_STATE_CONNECTED  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_STATE_CONNECTED";
    public static final String BROADCAST_BLUFI_BLE_STATE_DISCONNECTED  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_STATE_DISCONNECTED";
    public static final String BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_SUCCESS  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_SUCCESS";
    public static final String BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL";
    public static final String BROADCAST_BLUFI_BLE_RECEIVE_MESSAGE  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_RECEIVE_MESSAGE";
    public static final String BROADCAST_BLUFI_BLE_ERROR  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_ERROR";

    public static final String BROADCAST_STOP_SCAN = "com.petkit.android.dfu.broadcast.BROADCAST_STOP_SCAN";


    public static final String TYPE_POLLING_WIFI_STATUS_FROM_BLE = "TYPE_POLLING_WIFI_STATUS_FROM_BLE";
    public static final String TYPE_POLLING_WIFI_STATUS_FROM_SERVER = "TYPE_POLLING_WIFI_STATUS_FROM_SERVER";

    public static final String TYPE_POLLING_DEVICE_ONLINE_STATUS = "TYPE_POLLING_DEVICE_ONLINE_STATUS";



    public static final int MSG_BLUFI_BLE_GET_DEVICE_INFO = 110;
    public static final int MSG_BLUFI_BLE_SEND_WIFI_INFO = 151;
    public static final int MSG_BLUFI_BLE_GET_DEVICE_NET_STATUS = 112;

    /**
     * 获取设备联网状态
     * 1. 正在查找WiFi
     * 2. WiFi连接中
     * 3. 密码错误
     * 4. 找不到WiFi
     * 5. WiFi连接失败
     * 6. WiFi已连接，正在连接服务器
     * 7. 服务器连接成功
     * 8. 服务器连接失败
     * 9. 正在连接IoT
     * 10. 已在线
     */
    public static final int DEVICE_NET_STATUS_FINDING_WIFI = 1;
    public static final int DEVICE_NET_STATUS_CONNECTING_WIFI = 2;
    public static final int DEVICE_NET_STATUS_PASSWORD_ERROR = 3;
    public static final int DEVICE_NET_STATUS_NOT_FOUND_WIFI = 4;
    public static final int DEVICE_NET_STATUS_CONNECT_WIFI_FAIL = 5;
    public static final int DEVICE_NET_STATUS_CONNECTED_WIFI = 6;
    public static final int DEVICE_NET_STATUS_SERVER_CONNECT_SUCCESS = 7;
    public static final int DEVICE_NET_STATUS_SERVER_CONNECT_FAIL = 8;
    public static final int DEVICE_NET_STATUS_IOT_HTTP_CONNECTING = 9;
    public static final int DEVICE_NET_STATUS_IOT_HTTP_ONLINE = 10;


}