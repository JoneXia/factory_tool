package com.petkit.matetool.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.android.utils.PetkitToast;
import com.petkit.matetool.utils.JSONUtils;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Socket实例
 *
 * Created by Jone on 17/3/20.
 */
public class PetkitSocketInstance implements SocketClientDelegate {

    private static PetkitSocketInstance instance;
    private PetkitSocketClient mSocketClient;
    private WifiAdminSimple mWifiAdmin;

    private IPetkitSocketListener mPetkitSocketListener;

    public PetkitSocketInstance() {
        mSocketClient = new PetkitSocketClient();
        mWifiAdmin = new WifiAdminSimple(CommonUtils.getAppContext());
    }

    public static PetkitSocketInstance getInstance() {
        if(instance == null) {
            instance = new PetkitSocketInstance();
        }

        return instance;
    }

    public void setPetkitSocketListener(IPetkitSocketListener listener) {
        mPetkitSocketListener = listener;
    }

    public void startConnect(String remoteIp, int port) {
        PetkitLog.d("start connect " + remoteIp);
        LogcatStorageHelper.addLog("[SOCKET] start connect " + remoteIp + ", port: " + port);
        if ("0.0.0.0".equalsIgnoreCase(remoteIp)) {
            PetkitToast.showToast("无效的IP，请重启设备后再试！");
            return;
        }
        mSocketClient.connect(remoteIp, port);
        mSocketClient.registerSocketClientDelegate(this);
    }

    public boolean isConnected() {
        return mSocketClient.isConnected();
    }


    public void sendString(String data) {
        PetkitLog.d("send data: " + data);
        LogcatStorageHelper.addLog("[SOCKET] send data: " + data);
        mSocketClient.sendString(data);
    }

    public void disconnect() {
        mSocketClient.disconnect();
        LogcatStorageHelper.addLog("[SOCKET] disconnect socket");
    }

    @Override
    public void onConnected(SocketClient client) {
        LogcatStorageHelper.addLog("[SOCKET] socket connected");
        if(mPetkitSocketListener != null) {
            mPetkitSocketListener.onConnected();
        }
    }

    @Override
    public void onDisconnected(SocketClient client) {
        LogcatStorageHelper.addLog("[SOCKET] socket disconnected");
        if(mPetkitSocketListener != null) {
            mPetkitSocketListener.onDisconnected();
        }
    }

    @Override
    public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
        PetkitLog.d("onResponse: " + responsePacket.getMessage());
        LogcatStorageHelper.addLog("[SOCKET] onResponse: " + responsePacket.getMessage());
        if(responsePacket.getMessage() != null && responsePacket.getMessage().length() > 0) {
            String responseData = responsePacket.getMessage();
            JSONObject jsonObject = JSONUtils.getJSONObject(responseData);
            try {
                if(jsonObject != null && !jsonObject.isNull("key")) {
                    int key = jsonObject.getInt("key");

                    if(mPetkitSocketListener != null) {
                        mPetkitSocketListener.onResponse(key, jsonObject.isNull("payload") ? "" :
                                jsonObject.getJSONObject("payload").toString());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    private String getWifiConnectedSsidAscii(String ssid) {
        final long timeout = 100;
        final long interval = 20;
        String ssidAscii = ssid;

        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) CommonUtils.getAppContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();

        boolean isBreak = false;
        long start = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException ignore) {
                isBreak = true;
                break;
            }
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                if (scanResult.SSID != null && scanResult.SSID.equals(ssid)) {
                    isBreak = true;
                    try {
                        Field wifiSsidfield = ScanResult.class
                                .getDeclaredField("wifiSsid");
                        wifiSsidfield.setAccessible(true);
                        Class<?> wifiSsidClass = wifiSsidfield.getType();
                        Object wifiSsid = wifiSsidfield.get(scanResult);
                        Method method = wifiSsidClass
                                .getDeclaredMethod("getOctets");
                        byte[] bytes = (byte[]) method.invoke(wifiSsid);
                        ssidAscii = new String(bytes, "ISO-8859-1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } while (System.currentTimeMillis() - start < timeout && !isBreak);

        return ssidAscii;
    }


    public interface IPetkitSocketListener {
        void onConnected();
        void onDisconnected();
        void onResponse(int key, String data);
    }



}
