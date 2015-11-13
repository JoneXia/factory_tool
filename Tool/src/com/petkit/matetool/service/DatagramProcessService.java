package com.petkit.matetool.service;

import android.app.IntentService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.model.WifiParams;
import com.petkit.matetool.utils.Globals;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * ���ڴ���DatagramSocket�ķ��������
 *
 * Created by Jone on 2015/10/27.
 */
public class DatagramProcessService extends IntentService {

    private static final String LOG_TAG = "DatagramProcessService";

    private int head = 0;
    private int video_value = 0;
    private int video_value1 = 0;
    private int check_value = 0;
    private String sn = null;

    private boolean testPass = false;

    private byte[] buffer = new byte[DatagramConsts.MAX_DATA_PACKET_LENGTH];



    private WifiParams wifi_params = new WifiParams();
    private ArrayList<WifiParams> mFinalParams = new ArrayList<>();
    private ArrayList<String> mFinalMAC = new ArrayList<>();
    private int mFinalIndex = -1;


    private byte[] newByteForInt() {
        byte[] temp = new byte[4];

        temp[0] = 0x00;
        temp[1] = 0x00;

        return temp;
    }

    private boolean mIsReged = false;
    private boolean mIsReging = false;

//    private ArrayList<String> mTestLogResult = new ArrayList<>();
    private DatagramSocket udpSocket = null;

    private boolean mAborted;
    private int workStation;
    private int currentMode;
    private int currentCmd;

    int writeHead;
    byte[] writeRow = new byte[100];

    private Object mLock = new Object();

    private final BroadcastReceiver mActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int action = intent.getIntExtra(DatagramConsts.EXTRA_ACTION, 0);

            PetkitLog.d("DatagramProcessService onReceive action: " + action);

            switch (action) {
                case DatagramConsts.ACTION_WRITE:
                    currentCmd = intent.getIntExtra(DatagramConsts.EXTRA_WRITE_CMD, -1);
                    switch (currentCmd){
                        case DatagramConsts.SERVER_REG_MONI:
                            currentCmd = -1;
                            currentMode = intent.getIntExtra(DatagramConsts.EXTRA_CURRENT_MODE, 0);
                            break;
                        case DatagramConsts.SERVER_TEST_MODE_WRITE_SN:
                            sn = intent.getStringExtra(DatagramConsts.EXTRA_DATA);
                            break;
                    }

                    break;
                case DatagramConsts.ACTION_DESTROY:
                    mAborted = true;
                    destoryDatagram();
                    break;
            }

            // notify waiting thread
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    };

    private IntentFilter makeActionIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DatagramConsts.BROADCAST_ACTION);
        return intentFilter;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        final IntentFilter actionFilter = makeActionIntentFilter();
        manager.registerReceiver(mActionReceiver, actionFilter);
        // We must register this as a non-local receiver to get broadcasts from the notification action
        registerReceiver(mActionReceiver, actionFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(mActionReceiver);

        unregisterReceiver(mActionReceiver);
    }


    public DatagramProcessService() {
        super("DatagramProcessService");

    }

    private void closeSocket() {
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        System.gc();
    }

    private synchronized void clearTestLog() {
//        mTestLogResult.clear();
    }

    private void initDatagram(){
        head = 0;
        sn = null;
        mIsReged = false;
        wifi_params = new WifiParams();
        testPass = false;

        if(mFinalParams != null) {
            mFinalParams.clear();
        }
        if(mFinalMAC != null) {
            mFinalMAC.clear();
        }
        mFinalIndex = -1;
        currentMode = -1;

        mAborted = false;
    }

    private void destoryDatagram(){
        closeSocket();
        clearTestLog();
    }

    private void updateProgressNotification(int progress, String data) {
        PetkitLog.d("updateProgressNotification progress: " + progress);
        LogcatStorageHelper.addLog("[broadcast] progress : " + progress + "  data = " + data);

        final Intent broadcast = new Intent(DatagramConsts.BROADCAST_PROGRESS);
        broadcast.putExtra(DatagramConsts.EXTRA_DATA, data);
//        broadcast.putExtra(DatagramConsts.EXTRA_DEVICE_INFO, mBleDevice);
        broadcast.putExtra(DatagramConsts.EXTRA_PROGRESS, progress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        initDatagram();

        workStation = intent.getIntExtra(DatagramConsts.EXTRA_WORK_STATION, -1);

        try {
            if(udpSocket == null){
                udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress(DatagramConsts.DEFAULT_PORT));
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!mAborted) {
                        if(currentCmd >= 0){
                            sendData(currentCmd);
                            currentCmd = -1;
                        }

                        synchronized (mLock) {
                            try {
                                mLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            updateProgressNotification(DatagramConsts.DATAGRAM_START, "");

            while (!mAborted) {
                DatagramPacket recivedata = new DatagramPacket(buffer, DatagramConsts.MAX_DATA_PACKET_LENGTH);
                udpSocket.receive(recivedata);

                if (recivedata.getLength() != 0) {
                    byte[] temp = new byte[4];
                    System.arraycopy(buffer, 0, temp, 0, 4);
                    int g_head = bytesToInt2(temp, 0);
                    int index = 8;
                    byte[] data = recivedata.getData();
                    String logString = "";

                    LogcatStorageHelper.addLog("[R] cmd: " + g_head + " data: " + parse(data));
                    PetkitLog.e(LOG_TAG, "receive..." + g_head);
                    switch (g_head) {
                        case DatagramConsts.SERVER_TEST_MODE:
                            index = 8;
                            byte data1 = data[index++];

                            if(writeHead == DatagramConsts.SERVER_TEST_MODE && writeRow != null) {
                                if(writeRow[1] == 1){
                                    if(writeRow[0] == 0){
                                        if(writeRow[3] == data1){
                                            updateProgressNotification(DatagramConsts.WRITEOK, String.valueOf(writeRow[3]));
                                        }
                                    } else if(writeRow[0] == data1) {
                                        updateProgressNotification(DatagramConsts.WRITEOK, String.valueOf(writeRow[3]));
                                    }
                                } else {
                                    updateProgressNotification(DatagramConsts.SERVER_TEST_MODE, String.valueOf(convertToLocalMode(data1) == currentMode));
                                    mAborted = convertToLocalMode(data1) != currentMode;
                                }
                            }
                            break;
                        case DatagramConsts.SERVER_REG_MONI:
                            Log.e(LOG_TAG, "receive........ SERVER_REG_MONI");
                            index = 9;
                            mIsReging = false;
                            mIsReged = data[index] == 0;
                            if (mIsReged) {
                                sendData(DatagramConsts.SERVER_TEST_MODE);
                            } else {
                                currentMode = -1;//反注册怎么判断来清除状态 //TODO:
                            }
                            updateProgressNotification(DatagramConsts.SERVER_CHECK_SYS_PASS, String.valueOf(mIsReged));
                            break;
                        case DatagramConsts.SERVER_WIFI_PARAM:
                            logString += "receive...SERVER_WIFI_PARAM\n";
                            if (mIsReged || mIsReging) {
                                logString +="reg succeed...";
                                return;
                            }

                            WifiParams params = new WifiParams();
                            temp = new byte[16];
                            System.arraycopy(data, index, temp, 0, 16);
                            params.local_rtp_ip = new String(temp);
                            index += 16;

                            temp = newByteForInt();
                            System.arraycopy(data, index, temp, 2, 2);
                            params.local_port = bytesToInt2(temp, 0);
                            index += 2;

                            temp = new byte[64];
                            System.arraycopy(data, index, temp, 0, 64);
                            params.ssid = new String(temp);
                            index += 64;

                            byte[] tempchar = new byte[1];
                            System.arraycopy(data, index, tempchar, 0, 1);
                            params.rsq = tempchar[0];
                            index++;

                            System.arraycopy(data, index, tempchar, 0, 1);
                            params.state = tempchar[0];
                            index++;

                            System.arraycopy(data, index, tempchar, 0, 1);
                            params.index = tempchar[0];
                            index++;

                            // mac
                            temp = new byte[20];
                            System.arraycopy(data, index, temp, 0, 20);
                            String mactemp = new String(temp);
                            params.mac = getmac(mactemp.trim());
                            index += 20;

                            //sn
                            temp = new byte[32];
                            System.arraycopy(data, index, temp, 0, 32);
                            params.sn = new String(temp);
                            index += 32;

                            //status
                            System.arraycopy(data, index, tempchar, 0, 1);
                            params.status = tempchar[0];
                            index++;

//                            if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                if(params.status != 0) {
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            } else if(Globals.mCurCaseMode == Globals.FocusTestMode) {
//                                if(params.status != 1) {
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            } else if(Globals.mCurCaseMode == Globals.FocusTestMode2) {
//                                if(params.status != 2) {
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                if(params.status != 3) {
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            } else if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                if(params.status < 3 || (params.sn != null && !params.sn.trim().equals(sn))) {
//                                    if(params.status >= 3 && params.sn != null && !params.sn.trim().equals(sn)) {
//                                        sendBroadcast(context, "snerror");
//                                    }
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            }
//
//                            if(Globals.mCurCaseMode == Globals.BoardTestMode
//                                    || Globals.mCurCaseMode == Globals.FocusTestMode2
//                                    || Globals.mCurCaseMode == Globals.FocusTestMode) {
//                                if(!mIsReging) {
//                                    mIsReging = true;
//                                } else {
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            }

                            //version
                            temp = new byte[32];
                            System.arraycopy(data, index, temp, 0, 32);
                            params.version = new String(temp);

//                            if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                if (!String.valueOf(params.index).equals(Globals.g_station)) {
////									sendBroadcast(context, FixtureNumNotSame);
////									logString += "Fixture Num Not Same\n";
//                                    mIsReging = false;
//                                    receiveData(context);
//                                    return;
//                                }
//                            }
//                            if(Globals.mCurCaseMode == Globals.BoardTestMode
//                                    || Globals.mCurCaseMode == Globals.FocusTestMode
//                                    || Globals.mCurCaseMode == Globals.FocusTestMode2
//                                    || Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                // update wifi info
//                                head = g_head;
//                                wifi_params.local_rtp_ip = params.local_rtp_ip.replace("/", "");
//                                wifi_params.local_port = params.local_port;
//                                wifi_params.ssid = params.ssid;
//                                wifi_params.rsq = params.rsq;
//                                wifi_params.state = params.state;
//                                wifi_params.index = params.index;
//                                wifi_params.mac = params.mac;
//                                wifi_params.sn = params.sn;
//                                wifi_params.status = params.status;
//                                wifi_params.version = params.version;
//
//                                if(Globals.mCurCaseMode == Globals.BoardTestMode) {
//                                    sendBroadcast(context, updateWifiStatus);
//                                }
//                                sendData(context, SERVER_REG_MONI, true);
//                            } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                // add wifi info
//                                if(!isExist(params.mac)) {
//                                    if(params.mac != null) {
//                                        mFinalParams.add(params);
//                                        mFinalMAC.add(params.mac);
//                                        sendBroadcast(context, addWifiStatus);
//                                    }
//                                }
//                                receiveData(context);
//                                return;
//                            }

                            logString += "ip="+ params.local_rtp_ip + "\n";
                            logString += "port=" + params.local_port + "\n";
                            logString += "ssid=" + params.ssid + "\n";
                            logString += "rsq=" + params.rsq + "\n";
                            logString += "state=" + params.state + "\n";
                            logString += "index=" + params.index + "\n";
                            logString += "mac=" + params.mac + "\n\n";
                            logString += "sn=" + params.sn + "\n\n";
                            logString += "status=" + params.status + "\n";
                            logString += "version=" + params.version + "\n\n";

                            if(workStation == params.index && convertToLocalMode(params.status) == currentMode){
                                head = g_head;
                                wifi_params.local_rtp_ip = params.local_rtp_ip.replace("/", "");
                                wifi_params.local_port = params.local_port;
                                wifi_params.ssid = params.ssid;
                                wifi_params.rsq = params.rsq;
                                wifi_params.state = params.state;
                                wifi_params.index = params.index;
                                wifi_params.mac = params.mac;
                                wifi_params.sn = params.sn;
                                wifi_params.status = params.status;
                                wifi_params.version = params.version;

                                updateProgressNotification(DatagramConsts.SERVER_WIFI_PARAM, new Gson().toJson(wifi_params));

                                currentCmd = DatagramConsts.SERVER_REG_MONI;
                                synchronized (mLock){
                                    mLock.notifyAll();
                                }
//                                sendData(DatagramConsts.SERVER_REG_MONI);
                            }
                            break;
                        case DatagramConsts.SERVER_DEVSTA_PARAM:
                            logString += "receive.......SERVER_DEVSTA_PARAM\n";
                            index = 8;
                            byte devid = data[index++];
                            byte command = data[index++];
                            byte dir = data[index++];

                            switch (devid) {
                                case DatagramConsts.IOD_GPIO_LIGHT_BELT:
                                    logString += "LIGHT_BELT\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_LIGHT_LED1:
                                    logString += "LED1\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_LIGHT_LED2:
                                    logString += "LIGHT_LED2\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_LIGHT_IR:
                                    logString += "LIGHT_IR\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_CTL_PA:
                                    logString += "CTL_PA\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_CTL_VPOW:
                                    logString += "CTL_VPOW\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_CTL_VRST:
                                    logString += "CTL_VRST\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_CTL_USB_POW:
                                    logString += "USB_POW\n";
                                    break;
                                case DatagramConsts.IOD_ADC_TEMP:
                                    logString += "ADC_TEMP\n";
                                    break;
                                case DatagramConsts.IOD_GPIO_KEY_IN:
                                    updateProgressNotification(DatagramConsts.IOD_GPIO_KEY_IN, null);
                                    break;
                                case DatagramConsts.IOD_ADC_VOLT:
                                    updateProgressNotification(DatagramConsts.IOD_ADC_VOLT, String.valueOf((float) dir / 10));
                                    break;
                                case DatagramConsts.IOD_MOTO_PTZ:
                                    logString += "MOTO_PTZ\n";
                                    break;
                                case DatagramConsts.IOD_MOTO_LAS:
                                    logString += "MOTO_LAS\n";
                                    break;
                                case DatagramConsts.IOD_LED_LAS:
                                    logString += "LED_LAS\n";
                                    break;
                                case DatagramConsts.IOD_MAX_BUTT:
                                    logString += "MAX_BUTT\n";
                                    break;
                                case DatagramConsts.VIDEO_VALUE:
                                    logString += "VIDEO_VALUE\n";
                                    video_value = dir;
                                    updateProgressNotification(DatagramConsts.VIDEO_VALUE, String.valueOf(video_value));
                                    break;
                                default:
                                    return;
                            }

                            if(command == -1) {
                                updateProgressNotification(DatagramConsts.TESTFAILED, "");
                                logString += " Failed!";
                            } else if (command == 0) {
                                updateProgressNotification(DatagramConsts.TESTOK, "");
                                logString += " Pass!\n";
                            }
                            break;
//                        case DatagramConsts.SERVER_TEST_MODE:
//                            logString += "SERVER_TEST_MODE ";
//                            index = 8;
//
//                            byte data0 = data[index++];
//                            byte data1 = data[index++];
//
//                            index = 50;
//                            byte data50 = data[index++];
//                            byte data51 = data[index++];
//
//                            if(data1 == 0x00) { //no sn
//                                if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                    sendBroadcast(context, SERVER_CHECK_HAS_NO_SN);
//                                    mIsReging = false;
//                                    return;
//                                }
//
//                                if ((data50 & data51) == 0x1) {
//                                    sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                } else {
////									mIsReged = true;
////									sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                }
//                            } else if(data1 == 0x01) { //has sn
//                                String rtnsn = new String(data, 8 + 3, 46).trim();
//                                if(Globals.mCurCaseMode == Globals.SpotTestMode) {
//                                    if(sn != null && rtnsn.equals(sn)) {
//                                        if((data50 & data51) != 0x1) {
////											mIsReged = true;
////											sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                        } else {
//                                            sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                        }
//                                    } else {
//                                        sendBroadcast(context, "snerror");
//                                        return;
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//                                    if(rtnsn != null) {
//                                        sendBroadcast(context, SERVER_CHECK_HAS_SN);
//                                        if(sn != null) {
//                                            if(rtnsn.equals(sn)) {
////												mIsReged = true;
////												sendBroadcast(context, "" + SERVER_WRITE_SN_PASS);
//                                                sendBroadcast(context, writesnok);
//                                                logString += "write sn pass=====  sn=" + sn + "\n";
//                                            }else {
//                                                sendBroadcast(context, "" + SERVER_WRITE_SN_FAILED);
//                                                sendBroadcast(context, writesnfailed);
//                                                logString += "write sn failed===== sn=" + sn + "\n";
//                                            }
//                                        }
//                                    } else {
//                                        sendBroadcast(context, SERVER_CHECK_HAS_NO_SN);
//                                    }
//                                } else if(Globals.mCurCaseMode == Globals.FocusTestMode
//                                        || Globals.mCurCaseMode == Globals.FocusTestMode2) {
//                                    if ((data50 & data51) == 0x01) {
//                                        sendBroadcast(context, "" + SERVER_CHECK_SYS_FAILED);
//                                    } else {
////										mIsReged = true;
////										sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                    }
//                                } else {
////									mIsReged = true;
//                                    sendBroadcast(context, SERVER_CHECK_HAS_SN);
////									sendBroadcast(context, "" + SERVER_CHECK_SYS_PASS);
//                                }
//                            }
//
//                            mIsReging = false;
//                            break;
                    }

                    addTestLog(logString);
                }
            }

            sendData(DatagramConsts.SERVER_REG_UNMONI);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        destoryDatagram();
        updateProgressNotification(DatagramConsts.DATAGRAM_DESTROY, "");

    }

    public void sendData(final int cmd) {

        {
            try {
                if (wifi_params.local_rtp_ip == null) {
                    Log.e(LOG_TAG, "local_rtp_ip == null");
                    return;
                }

                byte[] data = null;
                byte[] head = null, len = null, row = new byte[100];
                Log.e(LOG_TAG, "send..." + cmd);

                switch (cmd) {
                    case DatagramConsts.SERVER_RTP_SESSION_CREATE:
                        head = intToBytes2(DatagramConsts.SERVER_RTP_SESSION_CREATE - 100);
                        row[0] = 0;
                        row[1] = 0;
                        break;
                    case DatagramConsts.SERVER_RTP_SESSION_STOP:
                        head = intToBytes2(DatagramConsts.SERVER_RTP_SESSION_STOP - 100);
                        row[0] = 0;
                        row[1] = 0;
                        break;
                    //FOR VIDEO BY IMAGE =======
                    case DatagramConsts.SERVER_RTP_SESSION_CREATE_IMG:
                        head = intToBytes2(DatagramConsts.SERVER_RTP_SESSION_CREATE - 100);
                        row[0] = 1;
                        row[1] = 1;
                        break;
                    case DatagramConsts.SERVER_RTP_SESSION_STOP_IMG:
                        head = intToBytes2(DatagramConsts.SERVER_RTP_SESSION_STOP - 100);
                        row[0] = 1;
                        row[1] = 1;
                        break;
                    //FOR VIDEO BY IMAGE =======
//					case SERVER_TEST_MODE_OFF:
//						head = intToBytes2(SERVER_TEST_MODE);
//						row[0] = 2;
//						row[1] = 0;
//						break;
//					case TEST_PUSH_RESULT:
//						head = intToBytes2(SERVER_TEST_MODE);
//						row[0] = 0;
//						break;
                    case DatagramConsts.SERVER_REG_UNMONI:
                        mIsReged = false;
                        mIsReging = false;
                        head = intToBytes2(DatagramConsts.SERVER_REG_MONI);
                        row[0] = 0;
                        row[1] = (byte) (currentMode >= DatagramConsts.FocusTestMode2 ? DatagramConsts.FocusTestMode : currentMode);
                        row[2] = (byte) workStation;
                        break;
                    case DatagramConsts.SERVER_REG_MONI:
                        mIsReging = true;
                        head = intToBytes2(DatagramConsts.SERVER_REG_MONI);
                        row[0] = 1;
                        row[1] = (byte) (currentMode >= DatagramConsts.FocusTestMode2 ? DatagramConsts.FocusTestMode : currentMode);
                        row[2] = (byte) workStation;
                        break;
                    case DatagramConsts.SERVER_TEST_MODE:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 1;
                        row[1] = 0;
                        break;
//					case SERVER_TEST_EXIT:
//						head = intToBytes2(SERVER_TEST_MODE);
//						row[0] = 0;
//						row[1] = 1;
//						row[2] = 1;
//						row[3] = 0;
//						break;
                    case DatagramConsts.SERVER_TEST_EXIT_TO_BORAD:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 0;
                        row[1] = 1;
                        row[2] = 1;
                        row[3] = 0;
                        break;
                    case DatagramConsts.SERVER_TEST_EXIT_TO_FOCUS:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 0;
                        row[1] = 1;
                        row[2] = 1;
                        row[3] = 1;
                        break;
                    case DatagramConsts.SERVER_TEST_EXIT_TO_FOCUS2:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 0;
                        row[1] = 1;
                        row[2] = 1;
                        row[3] = 2;
                        break;

                    case DatagramConsts.SERVER_TEST_BORAD:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 1;
                        row[1] = 1;
                        break;
                    case DatagramConsts.SERVER_TEST_FOCUS:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 2;
                        row[1] = 1;
                        break;
                    case DatagramConsts.SERVER_TEST_FOCUS2:
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 3;
                        row[1] = 1;
                        break;
//					case SERVER_TEST_MODE_RETEST:
//						head = intToBytes2(SERVER_TEST_MODE);
//						row[0] = 1;
//						break;
                    case DatagramConsts.SERVER_TEST_MODE_WRITE_SN:
                        if(sn == null || sn.isEmpty()) {
                            PetkitLog.e(LOG_TAG, "SN is null");
                            return;
                        }
                        head = intToBytes2(DatagramConsts.SERVER_TEST_MODE);
                        row[0] = 4;
                        row[1] = 1;
                        row[2] = (byte) sn.getBytes().length; //SN len
                        System.arraycopy(sn.getBytes(), 0, row, 3, sn.getBytes().length);
                        break;
                    // GPIO
                    case DatagramConsts.IOD_GPIO_LIGHT_BELT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_BELT;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_BELT_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_BELT;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_LED1:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_LED1;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_LED1_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_LED1;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_LED2:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_LED2;
                        row[1] = 1;
                        row[2] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_LED2_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_LED2;
                        row[1] = 0;
                        row[2] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_IR:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_IR;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_LIGHT_IR_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_LIGHT_IR;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_CTL_PA:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_CTL_PA;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_CTL_PA_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_CTL_PA;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_CTL_VPOW:
                        break;
                    case DatagramConsts.IOD_GPIO_CTL_VRST:
                        break;
                    case DatagramConsts.IOD_GPIO_KEY_IN:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_KEY_IN;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_GPIO_KEY_IN_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_GPIO_KEY_IN;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_GPIO_CTL_USB_POW:
                        break;
                    case DatagramConsts.IOD_ADC_TEMP:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_ADC_TEMP;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_ADC_TEMP_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_ADC_TEMP;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_ADC_VOLT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_ADC_VOLT;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_ADC_VOLT_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_ADC_VOLT;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_MOTO_PTZ_LEFT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_PTZ;
                        row[1] = 1;
                        row[2] = 1;
                        break;
                    case DatagramConsts.IOD_MOTO_PTZ_RIGHT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_PTZ;
                        row[1] = 1;
                        row[2] = 0;
                        break;
                    case DatagramConsts.IOD_MOTO_PTZ_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_PTZ;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_MOTO_LAS_LEFT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_LAS;
                        row[1] = 1;
                        row[2] = 1;
                        break;
                    case DatagramConsts.IOD_MOTO_LAS_RIGHT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_LAS;
                        row[1] = 1;
                        row[2] = 0;
                        break;
                    case DatagramConsts.IOD_MOTO_LAS_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MOTO_LAS;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_LED_LAS:
                        Log.e(LOG_TAG, "send........ IOD_LED_LAS");
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_LED_LAS;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_LED_LAS_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_LED_LAS;
                        row[1] = 0;
                        break;
                    case DatagramConsts.IOD_MAX_BUTT:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MAX_BUTT;
                        row[1] = 1;
                        break;
                    case DatagramConsts.IOD_MAX_BUTT_OFF:
                        head = intToBytes2(DatagramConsts.SERVER_DEVSTA_PARAM);
                        row[0] = DatagramConsts.IOD_MAX_BUTT;
                        row[1] = 0;
                        break;
                    default:
                        return;
                }

                len = intToBytes2(0);
                writeHead = bytesToInt2(head, 0);
                writeRow = row;
                data = new byte[head.length + len.length + row.length];
                System.arraycopy(head, 0, data, 0, head.length);
                System.arraycopy(len, 0, data, head.length, len.length);
                System.arraycopy(row, 0, data, head.length + len.length, row.length);

                InetAddress addr;
//                DatagramSocket ds = new DatagramSocket();
                DatagramPacket dp = null;

//                if(Globals.mCurCaseMode == Globals.BoardTestMode
//                        || Globals.mCurCaseMode == Globals.FocusTestMode
//                        || Globals.mCurCaseMode == Globals.FocusTestMode2
//                        || Globals.mCurCaseMode == Globals.SpotTestMode) {
                    addr = InetAddress.getByName(wifi_params.local_rtp_ip.trim().replace("/", ""));
                    dp = new DatagramPacket(data, data.length, addr, wifi_params.local_port);
                LogcatStorageHelper.addLog("[W] cmd: " + cmd + " addr: " + addr + " port: " + wifi_params.local_port + " data: " + parse(data));
//                } else {
//                    addr = InetAddress.getByName(mFinalParams.get(mFinalIndex).local_rtp_ip.trim().replace("/", ""));
//                    dp = new DatagramPacket(data, data.length, addr, mFinalParams.get(mFinalIndex).local_port);
//                }
                udpSocket.send(dp);

//                if( cmd== DatagramConsts.SERVER_REG_UNMONI) {
//                    Thread.sleep(500);
//                    ds.send(dp);
//                    ds.send(dp);
//                } else if(cmd >= SERVER_TEST_MODE_WRITE_SN && cmd <= SERVER_TEST_EXIT_TO_FOCUS2){
//                    Thread.sleep(1000);
//                    ds.send(dp);
//                    Thread.sleep(1000);
//                    ds.send(dp);
//                    Thread.sleep(1000);
//                    ds.send(dp);
//                    Thread.sleep(1000);
//                    ds.send(dp);
//                }

//                ds.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }

            addTestLog("send cmd=" + cmd);
        }
    }


    public synchronized void addTestLog(String str) {
//		synchronized (object) {
        PetkitLog.d("addTestLog " + str);
        LogcatStorageHelper.addLog("[Log]: " + str);
//        mTestLogResult.add(str);
//		}
    }

    /**
     * ��int��ֵת��Ϊռ�ĸ��ֽڵ�byte���飬������������(��λ��ǰ����λ�ں�)��˳�� ��bytesToInt2��������ʹ��
     */
    public byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte������ȡint��ֵ��������������(��λ��ǰ����λ�ں�)��˳�򣬺ͺ�intToBytes��������ʹ��
     *
     * @param src
     *            byte����
     * @param offset
     *            ������ĵ�offsetλ��ʼ
     * @return int��ֵ
     */
    public int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * byte������ȡint��ֵ��������������(��λ�ں󣬸�λ��ǰ)��˳�򡣺�intToBytes2��������ʹ��
     */
    public int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
        return value;
    }

    public String intToIp(int i) {
        return (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." +  ( i >> 24 & 0xFF);
    }

    private String getmac(String mactemp) {
        if(mactemp == null || mactemp.length() != 17) {
            return null;
        }

        String temp = mactemp.replace(":", "");
        String mac = "";

        for(int i = 10; i < temp.length() && i >= 0; i -= 2) {
            mac += (temp.substring(i, i + 2)/* + (i == 0 ? "" : ":")*/);
        }

        return mac.toUpperCase();
    }

    private int convertToDestMode(){
        switch (currentMode) {
            case DatagramConsts.BoardTestMode:
                return 0;
            case DatagramConsts.FocusTestMode:
                return 1;
            case DatagramConsts.FocusTestMode2:
                return 2;
            case DatagramConsts.FinalTestMode:
                return 3;
            case DatagramConsts.SpotTestMode:
                return 4;

        }
        return -1;
    }

    private int convertToLocalMode(int mode){
        switch (mode) {
            case 0:
                return DatagramConsts.BoardTestMode;
            case 1:
                return DatagramConsts.FocusTestMode;
            case 2:
                return DatagramConsts.FocusTestMode2;
            case 3:
                return DatagramConsts.FinalTestMode;
            case 4:
                return DatagramConsts.SpotTestMode;

        }
        return -1;
    }


    public String parse(final byte[] data) {
        if (data == null)
            return "";
        final int length = data.length;
        if (length == 0)
            return "";

        final char[] out = new char[length * 3 - 1];
        for (int j = 0; j < length; j++) {
            int v = data[j] & 0xFF;
            out[j * 3] = BLEConsts.HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = BLEConsts.HEX_ARRAY[v & 0x0F];
            if (j != length - 1)
                out[j * 3 + 2] = '-';
        }
        return new String(out);
    }


}
