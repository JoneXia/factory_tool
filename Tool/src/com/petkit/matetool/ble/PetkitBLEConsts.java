package com.petkit.matetool.ble;

import java.util.UUID;

import blufi.espressif.params.BlufiParameter;

public class PetkitBLEConsts {


    public enum ConnectState{
        BLE_STATE_CONNECTING,
        BLE_STATE_CONNECTED,
        BLE_STATE_CONNECT_FAILED,
        BLE_STATE_GATT_SUCCESS,
        BLE_STATE_GATT_FAILED,
        BLE_STATE_DISCONNECTED,
        BLE_STATE_SERVICE_DISCOVERED_SUCCESS,
        BLE_STATE_SERVICE_DISCOVERED_FAILED
    };

    public static final String BLUFI_BLE_EXTRA_ACTION = "BLUFI_BLE_EXTRA_ACTION";
    public static final String BLUFI_BLE_EXTRA_DEVICE = "BLUFI_BLE_EXTRA_DEVICE";
    public static final String BLUFI_BLE_EXTRA_DATA = "BLUFI_BLE_EXTRA_DATA";
    public static final String BLUFI_BLE_EXTRA_ERROR_CODE = "BLUFI_BLE_EXTRA_ERROR_CODE";



    public static final String BROADCAST_BLUFI_BLE_GATT_SUCCESS  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_GATT_SUCCESS";
    public static final String BROADCAST_BLUFI_BLE_GATT_FAIL  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_GATT_FAIL";
    public static final String BROADCAST_BLUFI_BLE_STATE_CONNECTED  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_STATE_CONNECTED";
    public static final String BROADCAST_BLUFI_BLE_STATE_DISCONNECTED  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_STATE_DISCONNECTED";
    public static final String BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_SUCCESS  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_SUCCESS";
    public static final String BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_SERVICE_DISCOVERED_FAIL";
    public static final String BROADCAST_BLUFI_BLE_RECEIVE_MESSAGE  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_RECEIVE_MESSAGE";
    public static final String BROADCAST_BLUFI_BLE_ERROR  = "com.petkit.android.broadcast.BROADCAST_BLUFI_BLE_ERROR";



    //获取设备信息
    public static final int MSG_BLUFI_BLE_GET_DEVICE_INFO = 110;
    //设置Wi-Fi和server参数
    public static final int MSG_BLUFI_BLE_SEND_WIFI_INFO = 151;
    //获取设备联网状态
    public static final int MSG_BLUFI_BLE_GET_DEVICE_NET_STATUS = 112;
    //获取设备网络信息
    public static final int MSG_BLUFI_BLE_GET_DEVICE_NET_INFO = 111;
    //设备log发送
    public static final int MSG_BLUFI_BLE_RECEIVE_DEVICE_LOG = 200;
    //断开连接
    public static final int MSG_BLUFI_BLE_DISCONNECT = 101;

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


    public static final int TYPE_BLE_SCAN = 1;//蓝牙扫描
    public static final int TYPE_BLE_CONNECT = 2;//蓝牙连接
    public static final int TYPE_BLE_SEND_MESSAGE = 3;//消息交互
    public static final int TYPE_BLE_GET_WIFI_INFO = 4;//获取Wi-Fi状态
    public static final int TYPE_BLE_BIND = 5;//绑定
    public static final int TYPE_BLE_GET_DEVICE_STATUS = 6;//获取设备状态
    public static final int TYPE_BLE_COMPLETE = 7;//完成


    public static final int TYPE_BLE_CONNECT_ROUTER_FAIL = 8;//连接路由失败
    public static final int TYPE_BLE_CONNECT_SERVER_FAIL = 9;//连接服务器失败


    public static final String PREF_SETTINGS_NAME = "esp_settings";
    public static final String PREF_SETTINGS_KEY_MTU_LENGTH = "esp_settings_mtu_length";
    public static final String PREF_SETTINGS_KEY_BLE_PREFIX = "esp_settings_ble_prefix";

    public static final String BLUFI_PREFIX = "BLUFI";

    public static final UUID UUID_SERVICE = BlufiParameter.UUID_SERVICE;
    public static final UUID UUID_WRITE_CHARACTERISTIC = BlufiParameter.UUID_WRITE_CHARACTERISTIC;
    public static final UUID UUID_NOTIFICATION_CHARACTERISTIC = BlufiParameter.UUID_NOTIFICATION_CHARACTERISTIC;

    public static final String KEY_BLE_DEVICE = "key_ble_device";

    public static final String KEY_CONFIGURE_PARAM = "configure_param";

    public static final int DEFAULT_MTU_LENGTH = 128;
    public static final int MIN_MTU_LENGTH = 15;
    public static final int MAX_MTU_LENGTH = 512;

}