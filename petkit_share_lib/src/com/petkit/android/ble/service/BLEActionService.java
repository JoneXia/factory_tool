package com.petkit.android.ble.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.petkit.android.ble.BLEConsts;
import com.petkit.android.ble.Conversion;
import com.petkit.android.ble.DeviceInfo;
import com.petkit.android.ble.FormatTransfer;
import com.petkit.android.ble.WifiInfo;
import com.petkit.android.ble.data.PetkitBleMsg;
import com.petkit.android.ble.exception.BLEAbortedException;
import com.petkit.android.ble.exception.BLEErrorException;
import com.petkit.android.ble.exception.DeviceDisconnectedException;
import com.petkit.android.ble.exception.UnexpectedCompleteException;
import com.petkit.android.ble.exception.UnknownParametersException;
import com.petkit.android.ble.exception.UnknownResponseException;
import com.petkit.android.model.Device;
import com.petkit.android.model.Extra;
import com.petkit.android.model.Pet;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.DeviceActivityDataUtils;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;

import org.apache.http.util.ByteArrayBuffer;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SimpleDateFormat")
public abstract class BLEActionService extends IntentService {

	private static final String TAG = "BLEActionService";
	
	protected boolean mPaused;
	protected boolean mAborted;

	protected BluetoothAdapter mBluetoothAdapter;
	protected String mDeviceName;

	/** Lock used in synchronization purposes */
	protected final Object mLock = new Object();

	/** The number of the last error that has occurred or 0 if there was no error */
	protected int mError;
	/** The current connection state. If its value is > 0 than an error has occurred. Error number is a negative value of mConnectionState */
	protected int mConnectionState;
	
	/** Flag set when we got confirmation from the device that notifications are enabled. */
	protected boolean mNotificationsEnabled;
	/** Flag set when we got confirmation from the device that Service Changed indications are enabled. */
	protected boolean mServiceChangedIndicationsEnabled;
	

	protected long mLastProgressTime, mStartTime;
	/**
	 * Flag sent when a request has been sent that will cause the DFU target to reset. Often, after sending such command, Android throws a connection state error. If this flag is set the error will be
	 * ignored.
	 */
	protected boolean mResetRequestSent;
	/** Flag indicating whether the image size has been already transfered or not */
	protected boolean mImageSizeSent;
	/** Flag indicating whether the init packet has been already transfered or not */
	protected boolean mInitPacketSent;
	/** Flag indicating whether the request was completed or not */
	protected boolean mRequestCompleted;
	/**
	 * <p>
	 * Flag set to <code>true</code> when the DFU target had send any notification with status other than. Setting it to <code>true</code> will abort sending firmware and
	 * stop logging notifications (read below for explanation).
	 * </p>
	 * <p>
	 * The onCharacteristicWrite(..) callback is written when Android puts the packet to the outgoing queue, not when it physically send the data. Therefore, in case of invalid state of the DFU
	 * target, Android will first put up to N* packets, one by one, while in fact the first will be transmitted. In case the DFU target is in an invalid state it will notify Android with a
	 * notification 10-03-02 for each packet of firmware that has been sent. However, just after receiving the first one this service will try to send the reset command while still getting more
	 * 10-03-02 notifications. This flag will prevent from logging "Notification received..." more than once.
	 * </p>
	 * <p>
	 * Additionally, sometimes after writing the command 6 (), Android will receive a notification and update the characteristic value with 10-03-02 and the callback for write
	 * reset command will log "[DFU] Data written to ..., value (0x): 10-03-02" instead of "...(x0): 06". But this does not matter for the DFU process.
	 * </p>
	 * <p>
	 * N* - Value of Packet Receipt Notification, 10 by default.
	 * </p>
	 */
	protected boolean mRemoteErrorOccured;
	
	/** The number of packets of firmware data to be send before receiving a new Packets receipt notification. 0 disables the packets notifications */
	protected int mPacketsBeforeNotification = 10;
	protected byte[] mBuffer;
	protected InputStream mInputStream;
	/** Size of BIN content of all hex files that are going to be transmitted. */
	protected int mImageSizeInBytes;
	/** Number of bytes transmitted. */
	protected int mBytesSent;
	/** Number of bytes confirmed by the notification. */
	protected int mBytesConfirmed;
	protected int mPacketsSentSinceNotification;
	/** This value is used to calculate the current transfer speed. */
	protected int mLastBytesSent;
	/**
	 * Firmware update may require two connections: one for Soft Device and/or Bootloader upload and second for Application. This fields contains the current part number.
	 */
	protected int mPartCurrent;
	/** Total number of parts. */
	protected int mPartsTotal;
	protected int mFileType;
	
	protected String mDeviceAddress;
	

	/** Latest data received from device using notification. */
	protected byte[] mReceivedData = null;

	protected boolean mIsScanning = false;
	
	protected long targetDeviceId;
	
	protected DeviceInfo mBleDevice;
	
	protected String secret, secretKey, deviceId;
	protected String filePath, address;
	protected Pet mCurDog;
	
	protected ArrayList<byte[]> mWriteData;
	
	private String activityDataSaveUrl, dailyDetailUrl;
	private boolean isNeedStoreProgress;
	protected boolean isWriteWifiSuccess = false;
	
	protected int mDebugSyncProgressCompleteCount = 0;
	protected String mateServer;
	
	protected int reconnectTimes = 0;

    //用于mate功能控制，为当前mate对应的版本号
    protected String curMateVersion = null;
	protected int mGetWifiStep;

	private byte[] mWifiSecretKey;


	protected boolean mStep;
	protected byte[] mStepRawData;

	/**
	 * <p>
	 * Flag set to <code>true</code> when the DFU target had send a notification with status other than {@link #}. Setting it to <code>true</code> will abort sending firmware and
	 * stop logging notifications (read below for explanation).
	 * </p>
	 * <p>
	 * The onCharacteristicWrite(..) callback is called when Android writes the packet into the outgoing queue, not when it physically sends the data.
	 * This means that the service will first put up to N* packets, one by one, to the queue, while in fact the first one is transmitted.
	 * In case the DFU target is in an invalid state it will notify Android with a notification 10-03-02 for each packet of firmware that has been sent.
	 * After receiving the first such notification, the DFU service will add the reset command to the outgoing queue, but it will still be receiving such notifications
	 * until all the data packets are sent. Those notifications should be ignored. This flag will prevent from logging "Notification received..." more than once.
	 * </p>
	 * <p>
	 * Additionally, sometimes after writing the command 6 ({@link #}), Android will receive a notification and update the characteristic value with 10-03-02 and the callback for write
	 * reset command will log "[DFU] Data written to ..., value (0x): 10-03-02" instead of "...(x0): 06". But this does not matter for the DFU process.
	 * </p>
	 * <p>
	 * N* - Value of Packet Receipt Notification, 10 by default.
	 * </p>
	 */
	protected boolean mRemoteErrorOccurred;




	private final BroadcastReceiver mDfuActionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final int action = intent.getIntExtra(BLEConsts.EXTRA_ACTION, 0);

			PetkitLog.d("BLEActionService onReceive action: " + action);

			switch (action) {
			case BLEConsts.ACTION_PAUSE:
				mPaused = true;
				break;
			case BLEConsts.ACTION_RESUME:
				mPaused = false;
				refreshHeartbeatTime();

				secretKey = intent.getStringExtra(BLEConsts.EXTRA_SECRET_KEY);
				mWifiSecretKey = intent.getByteArrayExtra(BLEConsts.EXTRA_WIFI_SECRET_KEY);
				secret = intent.getStringExtra(BLEConsts.EXTRA_SECRET);
				deviceId = intent.getStringExtra(BLEConsts.EXTRA_DEVICE_ID);
				filePath = intent.getStringExtra(BLEConsts.EXTRA_FILE_PATH);
				
				address = intent.getStringExtra(BLEConsts.EXTRA_DEVICE_ADDRESS);
                if(TextUtils.isEmpty(curMateVersion)){
                    curMateVersion = intent.getStringExtra(BLEConsts.EXTRA_MATE_VERSION);
                }
				
				// notify waiting thread
				synchronized (mLock) {
					mLock.notifyAll();
				}
				break;
			case BLEConsts.ACTION_ABORT:
				mPaused = false;
				mAborted = true;

				// notify waiting thread
				synchronized (mLock) {
					mLock.notifyAll();
				}
				break;
			case BLEConsts.ACTION_STEP_ENTRY:
				mPaused = false;
				mStepRawData = intent.getByteArrayExtra(BLEConsts.EXTRA_DATA);
				// notify waiting thread
				synchronized (mLock) {
					mLock.notifyAll();
				}
				break;
			case BLEConsts.ACTION_STEP_QUIT:
				mStep = false;
				mPaused = false;
				// notify waiting thread
				synchronized (mLock) {
					mLock.notifyAll();
				}
				break;
			}
		}
	};
	
	private final BroadcastReceiver mConnectionStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// obtain the device and check it this is the one that we are connected to 
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (!device.getAddress().equals(mDeviceAddress))
				return;

			final String action = intent.getAction();

			logi("Action received: " + action);
			if(mConnectionState != BLEConsts.STATE_SCANING)
				mConnectionState = BLEConsts.STATE_DISCONNECTED;

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		}
	};
	
	
	private final BroadcastReceiver mBondStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// obtain the device and check it this is the one that we are connected to
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (!device.getAddress().equals(mDeviceAddress))
				return;

			// read bond state
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			if (bondState == BluetoothDevice.BOND_BONDING)
				return;

			mRequestCompleted = true;

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
				return;
			}
		};
	};



	public BLEActionService() {
		super("BLEActionService");
		
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		final IntentFilter actionFilter = makeDfuActionIntentFilter();
		manager.registerReceiver(mDfuActionReceiver, actionFilter);
		// We must register this as a non-local receiver to get broadcasts from the notification action
		registerReceiver(mDfuActionReceiver, actionFilter);
		
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(mConnectionStateBroadcastReceiver, filter);

		final IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(mBondStateBroadcastReceiver, bondFilter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		manager.unregisterReceiver(mDfuActionReceiver);

		unregisterReceiver(mDfuActionReceiver);
		unregisterReceiver(mConnectionStateBroadcastReceiver);
		unregisterReceiver(mBondStateBroadcastReceiver);
	}

	private String getActionTypeString(int action){
		switch (action) {
		case BLEConsts.BLE_ACTION_SYNC:
			return "SYNC";
		case BLEConsts.BLE_ACTION_CHECK:
			return "CHECK";
		case BLEConsts.BLE_ACTION_CHANGE:
			return "Fit CHANGE";
		case BLEConsts.BLE_ACTION_CHANGE_HS:
			return "CHANGE_HS";
		case BLEConsts.BLE_ACTION_HS_INIT_WIFI:
			return "HS_INIT_WIFI";
		case BLEConsts.BLE_ACTION_INIT_HS:
			return "HS_INIT";
		case BLEConsts.BLE_ACTION_OTA:
			return "OTA";
		case BLEConsts.BLE_ACTION_OTA_RECONNECT:
			return "OTA RECONNECT";
		case BLEConsts.BLE_ACTION_SCAN:
			return "SCAN";
		case BLEConsts.BLE_ACTION_INIT:
			return "Fit init";
		}
		
		return "";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PetkitLog.d(TAG + " onHandleIntent");
		
		final int action = intent.getIntExtra(BLEConsts.EXTRA_ACTION, BLEConsts.BLE_ACTION_SYNC);
		
		mBytesSent = 0;
		mBytesConfirmed = 0;
		mLastProgressTime = 0;
		mError = 0;
		mAborted = false;
		mPaused = false;
		mNotificationsEnabled = false;
		mRemoteErrorOccured = false;
		mResetRequestSent = false;
		mRequestCompleted = false;
		isNeedStoreProgress = false;
		mCurDog = null;
		mDeviceInfoList.clear();
		activityDataSaveUrl = intent.getStringExtra(BLEConsts.EXTRA_URL_DATA_SAVE);
		dailyDetailUrl = intent.getStringExtra(BLEConsts.EXTRA_URL_DAILY_DETAIL);
		mDebugSyncProgressCompleteCount  = 0;
		reconnectTimes = intent.getIntExtra(BLEConsts.EXTRA_DEVICE_RECONNECT_TIMES, 0);
        curMateVersion = intent.getStringExtra(BLEConsts.EXTRA_MATE_VERSION);
		
		if(reconnectTimes == 0){
			mBleDevice = null;
		}
		
		LogcatStorageHelper.addLog("ble start, action: " + getActionTypeString(action));

		if(!initialize(action)){
			updateProgressNotification(BLEConsts.PROGRESS_BLE_NOT_SUPPORT);
			return;
		}
		
		switch (action) {
		case BLEConsts.BLE_ACTION_SYNC:
			isNeedStoreProgress = true;
		case BLEConsts.BLE_ACTION_CHECK:
			mCurDog = (Pet) intent.getSerializableExtra(BLEConsts.EXTRA_DOG);
			if(mCurDog == null || mCurDog.getDevice() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			targetDeviceId = mCurDog.getDevice().getId();
			deviceState.setId(targetDeviceId);
			if(mBleDevice == null){
				mBleDevice = startScan(intent);
			}
			
			if(mAborted){
				sendErrorBroadcast(BLEConsts.ERROR_ABORTED);
			} else if(mBleDevice == null){
				LogcatStorageHelper.addLog("scan failed, retry times: " + reconnectTimes);

				if(reconnectTimes < BLEConsts.MAX_RECONNECT_TIMES){
					final Intent newIntent = new Intent();
					newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
					newIntent.putExtra(BLEConsts.EXTRA_DEVICE_RECONNECT_TIMES, ++reconnectTimes);
					startService(newIntent);
				} else {
					updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
				}
			} else {
				startSyncDevice(intent);
				if(isNeedStoreProgress){
					CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, 0xff);
				}
			}
			break;
		case BLEConsts.BLE_ACTION_OTA:
		case BLEConsts.BLE_ACTION_OTA_RECONNECT:
			mCurDog = (Pet) intent.getSerializableExtra(BLEConsts.EXTRA_DOG);
			if(mCurDog == null || mCurDog.getDevice() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			targetDeviceId = mCurDog.getDevice().getId();
			deviceState.setId(targetDeviceId);
			if(mBleDevice == null){
				mBleDevice = startScan(intent);
			}
			
			if(mAborted){
				sendErrorBroadcast(BLEConsts.ERROR_ABORTED);
			}else if(mBleDevice == null){
				LogcatStorageHelper.addLog("scan failed, retry times: " + reconnectTimes);
				if(reconnectTimes < BLEConsts.MAX_RECONNECT_TIMES){
					final Intent newIntent = new Intent();
					newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
					newIntent.putExtra(BLEConsts.EXTRA_DEVICE_RECONNECT_TIMES, ++reconnectTimes);
					startService(newIntent);
				} else {
					updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
				}
			}else {
				startOTA(intent);
			}
			break;
		case BLEConsts.BLE_ACTION_CHANGE:
		case BLEConsts.BLE_ACTION_INIT:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startInitAndChangeFit(intent);
			break;
		case BLEConsts.BLE_ACTION_CHANGE_HS:
		case BLEConsts.BLE_ACTION_INIT_HS:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			mateServer = intent.getStringExtra(BLEConsts.EXTRA_DATA);
			secret = intent.getStringExtra(BLEConsts.EXTRA_SECRET);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startInitAndChangeMate(intent);
			break;
		case BLEConsts.BLE_ACTION_BEACONS:
			if(!mBluetoothAdapter.isEnabled()){
				return;
			}
			startScan(intent);
			updateProgressNotification(BLEConsts.PROGRESS_SCANING_TIMEOUT);
			break;
		case BLEConsts.BLE_ACTION_SCAN:
			startScan(intent);
			updateProgressNotification(BLEConsts.PROGRESS_SCANING_TIMEOUT);
			break;
		case BLEConsts.BLE_ACTION_HS_INIT_WIFI:
			targetDeviceId = Long.valueOf(intent.getStringExtra(BLEConsts.EXTRA_DEVICE_ID));
			secret = intent.getStringExtra(BLEConsts.EXTRA_SECRET);
			startScan(intent);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
			} else {
				startMateWifiInit(intent);
			}
			break;
		case BLEConsts.BLE_ACTION_OTA_GO:
		case BLEConsts.BLE_ACTION_OTA_GO_RECONNECT:
			targetDeviceId = intent.getLongExtra(BLEConsts.EXTRA_DEVICE_ID, 0);
			deviceState.setId(targetDeviceId);
			if(mBleDevice == null){
				mBleDevice = startScan(intent);
			}

			if(mAborted){
				sendErrorBroadcast(BLEConsts.ERROR_ABORTED);
			}else if(mBleDevice == null){
				LogcatStorageHelper.addLog("scan failed, retry times: " + reconnectTimes);
				if(reconnectTimes < BLEConsts.MAX_RECONNECT_TIMES){
					final Intent newIntent = new Intent();
					newIntent.fillIn(intent, Intent.FILL_IN_COMPONENT | Intent.FILL_IN_PACKAGE);
					newIntent.putExtra(BLEConsts.EXTRA_DEVICE_RECONNECT_TIMES, ++reconnectTimes);
					startService(newIntent);
				} else {
					updateProgressNotification(BLEConsts.PROGRESS_SCANING_FAILED);
				}
			}else {
				startGoOTA(intent);
			}
			break;
		case BLEConsts.BLE_ACTION_GO_INIT:
		case BLEConsts.BLE_ACTION_GO_CHANGE:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startInitAndChangeGo(intent);
			break;
		case BLEConsts.BLE_ACTION_GO_SAMPLING:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startGoSampling(intent);
			break;
		case BLEConsts.BLE_ACTION_AQ_TEST:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startAQTest(intent);
			break;
		case BLEConsts.BLE_ACTION_W5_TEST:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startW5Test(intent);
			break;
		case BLEConsts.BLE_ACTION_DEVICE_TEST:
			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
			if(mBleDevice == null || mBleDevice.getAddress() == null){
				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
				return;
			}
			startBleDeviceTest(intent);
			break;
//		case BLEConsts.BLE_ACTION_K3_TEST:
//			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
//			if(mBleDevice == null || mBleDevice.getAddress() == null){
//				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
//				return;
//			}
//			startK3Test(intent);
//			break;
//		case BLEConsts.BLE_ACTION_AQR_TEST:
//			mBleDevice = (DeviceInfo) intent.getSerializableExtra(BLEConsts.EXTRA_DEVICE_INFO);
//			if(mBleDevice == null || mBleDevice.getAddress() == null){
//				updateProgressNotification(BLEConsts.ERROR_DEVICE_ID_NULL);
//				return;
//			}
//			startAQRTest(intent);
//			break;
		default:
			break;
		}
		
		stopHeartbeat();
		stop();
	}
	
	
	/**
	 * Checks whether the response received is valid and returns the status code.
	 * 
	 * @param response
	 *            the response received from the DFU device.
	 * @param request
	 *            the expected Op Code
	 * @return the status code
	 * @throws UnknownResponseException
	 *             if response was not valid
	 */
	protected int getStatusCode(final byte[] response, final int request) throws UnknownResponseException {
		if (response == null || response.length != 3 || response[0] != BLEConsts.OP_CODE_RESPONSE_CODE_KEY || response[1] != request || response[2] < 1 || response[2] > 6)
			throw new UnknownResponseException("Invalid response received", response, request);
		return response[2];
	}
	
	/**
	 * Wait until the connection state will change to or until an error occurs.
	 */
	protected void waitUntilDisconnected() {
        stopHeartbeat();

		try {
			synchronized (mLock) {
				while (mConnectionState != BLEConsts.STATE_DISCONNECTED && mError == 0)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
	}
	
	protected boolean timeOut = false;
	protected void waitUntilTimeOut(long duraion) {
		
		if(mTimer == null){
			mTimer = new Timer();
		}
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				timeOut = true;
				synchronized (mLock) {
					mLock.notifyAll();
				}
			}
		}, duraion);
		timeOut = false;
	}
	
	protected void waitIfPaused(boolean isKeepAlive) {
		synchronized (mLock) {
			try {
				while (mPaused && !mAborted)
					mLock.wait();
			} catch (final InterruptedException e) {
				loge("Sleeping interrupted", e);
			}
		}
	}

	/** Stores the last progress percent. Used to lower number of calls of {@link #updateProgressNotification(int)}. */
	protected int mLastProgress = -1;
	
	/**
	 * Creates or updates the notification in the Notification Manager. Sends broadcast with current progress to the activity.
	 */
	protected void updateProgressNotification() {
		int progress = (int) (100.0f * mBytesSent / mImageSizeInBytes);
		if(progress > 100){
			progress = 100;
		}
		if (mLastProgress == progress)
			return;

		LogcatStorageHelper.addLog(String.format("[R-D] data sync progress: %d.", progress));

		mLastProgress = progress;
		updateProgressNotification(progress);
	}
	
	protected void updateProgressNotification(int progress) {
		updateProgressNotification(progress, "");
	}
	

	protected void updateProgressNotification(int progress, String data) {
		PetkitLog.d("updateProgressNotification progress: " + progress);
		if(isNeedStoreProgress)
			CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, progress);
		
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_PROGRESS);
		broadcast.putExtra(BLEConsts.EXTRA_DATA, data);
		broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, mBleDevice);
		broadcast.putExtra(BLEConsts.EXTRA_PROGRESS, progress);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}


	protected void updateProgressNotification(int progress, ArrayList<PetkitBleMsg> msgs) {
		PetkitLog.d("updateProgressNotification progress: " + progress);
		if(isNeedStoreProgress)
			CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, progress);

		final Intent broadcast = new Intent(BLEConsts.BROADCAST_PROGRESS);
		broadcast.putExtra(BLEConsts.EXTRA_DATA, msgs);
		broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, mBleDevice);
		broadcast.putExtra(BLEConsts.EXTRA_PROGRESS, progress);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	
	private static IntentFilter makeDfuActionIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BLEConsts.BROADCAST_ACTION);
		return intentFilter;
	}
	
	protected void sendLogBroadcast(final String message) {
		final String fullMessage = "[BLE] " + message;
		// the log provider is not installed, use broadcast action 
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_LOG);
		broadcast.putExtra(BLEConsts.EXTRA_LOG_MESSAGE, fullMessage);
		broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, mBleDevice);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		LogcatStorageHelper.addLog(message);
	}
	
	protected void sendErrorBroadcast(final int error) {
		if(isNeedStoreProgress)
			CommonUtils.addSysIntMap(this, Consts.SHARED_DEVICE_CONNECT_STATE, 0xff);
		
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_ERROR);
		broadcast.putExtra(BLEConsts.EXTRA_DATA, error & ~BLEConsts.ERROR_CONNECTION_MASK);
		broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, mBleDevice);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		
		LogcatStorageHelper.addLog("Unexpected error : " + BLEConsts.convertErrorCode(error));
	}
	
	protected void sendScanedDeviceBroadcast(final DeviceInfo deviceInfo) {
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_SCANED_DEVICE);
		broadcast.putExtra(BLEConsts.EXTRA_DEVICE_INFO, deviceInfo);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	
	protected void sendScanedWifiBroadcast(final WifiInfo wifiinfo) {
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_SCANED_WIFI);
		broadcast.putExtra(BLEConsts.EXTRA_WIFI_INFO, wifiinfo);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	
	protected void sendScanedWifiCompletedBroadcast() {
		final Intent broadcast = new Intent(BLEConsts.BROADCAST_SCANED_WIFI_COMPLETED);
		broadcast.putExtra(Consts.MATE_GET_WIFI_STEP_KEY, mGetWifiStep);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	
	protected void loge(final String message) {
		if (BLEConsts.DEBUG)
			Log.e(TAG, message);
	}

	protected void loge(final String message, final Throwable e) {
		if (BLEConsts.DEBUG)
			Log.e(TAG, message, e);
	}

	protected void logw(final String message) {
		if (BLEConsts.DEBUG)
			Log.w(TAG, message);
	}

	protected void logi(final String message) {
		if (BLEConsts.DEBUG)
			Log.i(TAG, message);
	}

	protected void logd(final String message) {
		if (BLEConsts.DEBUG)
			Log.d(TAG, message);
	}

	
	/**
	 * Initializes bluetooth adapter
	 * 
	 * @return <code>true</code> if initialization was successful
	 */
	
	protected abstract boolean initialize(final int action); 
	
	/**
	 * Waits until the notification will arrive. Returns the data returned by the notification. This method will block the thread if response is not ready or connection state will change from
	 * . If connection state will change, or an error will occur, an exception will be thrown.
	 *
	 * @return the value returned by the Control Point notification
	 * @throws DeviceDisconnectedException
	 * @throws BLEErrorException
	 * @throws BLEAbortedException
	 */
	protected byte[] readNotificationResponse() throws DeviceDisconnectedException, BLEErrorException, BLEAbortedException {
		// do not clear the mReceiveData here. The response might already be obtained. Clear it in write request instead.
		mError = 0;
		try {
			synchronized (mLock) {
				while ((mReceivedData == null && mConnectionState == BLEConsts.STATE_CONNECTED_AND_READY && mError == 0 && !mAborted) || mPaused)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		if (mAborted)
			throw new BLEAbortedException();
		if (mError != 0)
			throw new BLEErrorException("Unable to write Op Code", mError);
		if (mConnectionState != BLEConsts.STATE_CONNECTED_AND_READY)
			throw new DeviceDisconnectedException("Unable to write Op Code", mConnectionState);
		return mReceivedData;
	}
	
	
	/**
	 * Sets number of data packets that will be send before the notification will be received.
	 * 
	 * @param data
	 *            control point data packet
	 * @param value
	 *            number of packets before receiving notification. If this value is 0, then the notification of packet receipt will be disabled by the DFU target.
	 */
	protected void setNumberOfPackets(final byte[] data, final int value) {
		data[1] = (byte) (value & 0xFF);
		data[2] = (byte) ((value >> 8) & 0xFF);
	}
	
	
	protected String parse(final byte[] data) {
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
	
	
	@SuppressLint("NewApi")
	public boolean createBond(final BluetoothDevice device) {
		if (device.getBondState() == BluetoothDevice.BOND_BONDED)
			return true;

		boolean result = false;
		mRequestCompleted = false;

		sendLogBroadcast("Starting pairing...");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			sendLogBroadcast("gatt.getDevice().createBond()");
			result = device.createBond();
		} else {
			result = createBondApi18(device);
		}

		// We have to wait until device is bounded
		try {
			synchronized (mLock) {
				while (mRequestCompleted == false && !mAborted)
					mLock.wait();
			}
		} catch (final InterruptedException e) {
			loge("Sleeping interrupted", e);
		}
		return result;
	}

	public boolean createBondApi18(final BluetoothDevice device) {
		/*
		 * There is a createBond() method in BluetoothDevice class but for now it's hidden. We will call it using reflections. It has been revealed in KitKat (Api19)
		 */
		try {
			final Method createBond = device.getClass().getMethod("createBond");
			if (createBond != null) {
				sendLogBroadcast("gatt.getDevice().createBond() (hidden)");
				return (Boolean) createBond.invoke(device);
			}
		} catch (final Exception e) {
			Log.w(TAG, "An exception occurred while creating bond", e);
		}
		return false;
	}

	/**
	 * Removes the bond information for the given device.
	 * 
	 * @param device
	 *            the device to unbound
	 * @return <code>true</code> if operation succeeded, <code>false</code> otherwise
	 */
	public boolean removeBond(final BluetoothDevice device) {
		if (device.getBondState() == BluetoothDevice.BOND_NONE)
			return true;

		sendLogBroadcast("Removing bond information...");
		boolean result = false;
		/*
		 * There is a removeBond() method in BluetoothDevice class but for now it's hidden. We will call it using reflections.
		 */
		try {
			final Method removeBond = device.getClass().getMethod("removeBond");
			if (removeBond != null) {
				mRequestCompleted = false;
				sendLogBroadcast("gatt.getDevice().removeBond() (hidden)");
				result = (Boolean) removeBond.invoke(device);

				// We have to wait until device is unbounded
				try {
					synchronized (mLock) {
						while (mRequestCompleted == false && !mAborted)
							mLock.wait();
					}
				} catch (final InterruptedException e) {
					loge("Sleeping interrupted", e);
				}
			}
			result = true;
		} catch (final Exception e) {
			Log.w(TAG, "An exception occurred while removing bond information", e);
		}
		return result;
	}
	
	/*----------------------------Scan part-------------------------------------------*/

	protected List<DeviceInfo> mDeviceInfoList = new ArrayList<>();

    protected boolean checkDeviceFilter(BluetoothDevice device) {
        return checkDeviceFilter(device.getName());
    }
	
	protected boolean checkDeviceFilter(String deviceName) {
		int n = BLEConsts.DeviceFilter.length;
		if(deviceName == null){
			return false;
		}
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equalsIgnoreCase(BLEConsts.DeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

    protected String getDeviceNameByScanRecord(int value){
        LogcatStorageHelper.addLog("getDeviceNameByScanRecord, value: " + value);
        switch (value){
            case 0xC5:
                return BLEConsts.PET_FIT_DISPLAY_NAME;
            case 0xC3:
                return BLEConsts.PET_FIT2_DISPLAY_NAME;
            default:
                return BLEConsts.PET_MATE;
        }
    }
	
	protected boolean deviceInfoExists(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getAddress()
					.equals(address)) {
				return true;
			}
		}
		return false;
	}

	protected DeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getAddress()
					.equals(device.getAddress())) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}
	

	protected void addScanedDevice(DeviceInfo device) {
		LogcatStorageHelper.addLog("find device: " + (device == null ? "device == null" : device.toString()));
		PetkitLog.d("find device: " + (device == null ? "device == null" : device.toString()));

//		if(device != null){
//			InforCollectUtils.insertBeaconsInfor(this, CommonUtils.getCurrentUserId(), device);
//		}
		
		mDeviceInfoList.add(device);
	}

	protected DeviceInfo createDeviceInfo(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		DeviceInfo deviceInfo = new DeviceInfo(device, rssi, scanRecord);
		return deviceInfo;
	}
	
	
	
	/*----------------------------Sync part-------------------------------------------*/
	
	protected Device deviceState = new Device();

	protected int mComdLength = 0;
	protected List<StringBuffer> mDataBuffers = new ArrayList<StringBuffer>();
	protected SparseArray<StringBuffer> mTempDataBuffers = new SparseArray<StringBuffer>();
	protected StringBuffer mDebugInfor;

	protected int DataConfirmFlag = 0x00000000;
	protected boolean mDataMissMode = false;
	
	private int curSectionBlockSize = 0;
	
	protected SparseArray<byte[]> mTempReceivedWifiData = new SparseArray<byte[]>();
	protected ByteArrayBuffer mReceivedWifiData;
	
	
	protected byte[] buildOpCodeBuffer(int key, String... params) throws UnknownParametersException, BLEAbortedException {
		byte[] codeBuffer = new byte[13];
		
		if(mAborted){
			throw new BLEAbortedException();
		}
		
		switch (key) {
		case BLEConsts.OP_CODE_DEVICE_INIT_KEY:
			if(params == null || params.length != 3 || params[0] == null || params[2] == null){
				throw new UnknownParametersException("Invalid parameters", key, params);
			}
			if(params[0].length() == 16){
				codeBuffer = new byte[20];
			}
			int offset = 0;
			codeBuffer[offset++] = BLEConsts.OP_CODE_DEVICE_INIT_KEY;

			String hexString = Long.toHexString(Long.valueOf(params[0]));
			int idLength = hexString.length();
			int total;
			if(params[0].length() == 16){
				total = 8;
				for(int i = 0; i < 16 - idLength;i++){
					hexString = "0" + hexString;
					idLength = hexString.length();
				}
			}else {
				total = 4;
			}
			
			for(int i = 0; i < Math.ceil(((float)idLength)/2); i++){
				int startPos = idLength - 2*(i+1);
				if(startPos < 0){
					startPos = 0;
				}
				codeBuffer[total-i] = ((Integer)Integer.parseInt(hexString.substring(startPos, idLength-2*i), 16)).byteValue();
				offset++;
			}
			
//			codeBuffer[offset++] = ((Integer)Integer.parseInt(params[1].substring(0, 2), 16)).byteValue();
//			codeBuffer[offset++] = ((Integer)Integer.parseInt(params[1].substring(2, 4), 16)).byteValue();
//			codeBuffer[offset++] = ((Integer)Integer.parseInt(params[1].substring(4, 6), 16)).byteValue();
//			codeBuffer[offset++] = ((Integer)Integer.parseInt(params[1].substring(6, 8), 16)).byteValue();

			for(int i = 0; i < params[2].length()/2; i++) {
				codeBuffer[9 + i] = ((Integer)Integer.parseInt(params[2].substring(i*2, i*2+2), 16)).byteValue();
			}

//			codeBuffer[9] = ((Integer)Integer.parseInt(params[2].substring(0, 2), 16)).byteValue();
//			codeBuffer[10] = ((Integer)Integer.parseInt(params[2].substring(2, 4), 16)).byteValue();
//			codeBuffer[11] = ((Integer)Integer.parseInt(params[2].substring(4, 6), 16)).byteValue();
//			codeBuffer[12] = ((Integer)Integer.parseInt(params[2].substring(6, 8), 16)).byteValue();
			break;
		case BLEConsts.OP_CODE_VERIFY_KEY:
			if(params == null || params.length != 1 || params[0] == null || params[0].length() < 8){
				throw new UnknownParametersException("Invalid parameters", key, params);
			}
			
			codeBuffer[0] = BLEConsts.OP_CODE_VERIFY_KEY;
			for(int i = 0; i < params[0].length()/2; i++) {
				codeBuffer[1 + i] = ((Integer)Integer.parseInt(params[0].substring(i*2, i*2+2), 16)).byteValue();
			}
//			codeBuffer[1] = ((Integer)Integer.parseInt(params[0].substring(0, 2), 16)).byteValue();
//			codeBuffer[2] = ((Integer)Integer.parseInt(params[0].substring(2, 4), 16)).byteValue();
//			codeBuffer[3] = ((Integer)Integer.parseInt(params[0].substring(4, 6), 16)).byteValue();
//			codeBuffer[4] = ((Integer)Integer.parseInt(params[0].substring(6, 8), 16)).byteValue();
			break;
		case BLEConsts.OP_CODE_DEBUG_INFOR_KEY:
			int address = (deviceState.getExtra().getImageType().equals("B") ? 0x8c00 : 0x2000) + mComdLength * 2;
			String addressString = Integer.toHexString(address);
			
			codeBuffer[0] = BLEConsts.OP_CODE_DEBUG_INFOR_KEY;
			codeBuffer[1] = ((Integer)Integer.parseInt(addressString.substring(0, 2), 16)).byteValue();
			codeBuffer[2] = ((Integer)Integer.parseInt(addressString.substring(2, 4), 16)).byteValue();
			
			mComdLength++;
			break;
			
		case BLEConsts.MATE_OP_CODE_CONFIRM_KEY:
		case BLEConsts.OP_CODE_DATA_CONFIRM_KEY:
			int confirmbit = DataConfirmFlag;
			if (curSectionBlockSize < BLEConsts.MAX_BLOCK_SIZE) {
				for (int i = BLEConsts.MAX_BLOCK_SIZE - 1; i >= curSectionBlockSize; i--) {
					confirmbit = confirmbit | (1 << i);
				}
			}
			
			if(confirmbit == -1){
				if(key == BLEConsts.OP_CODE_DATA_CONFIRM_KEY) {
					saveConfimedSectionData();
				} else {
					saveConfimedWifiData();
				}
				DataConfirmFlag = 0; 
				mDataMissMode = false;
			}else{
				mDataMissMode = true;
				LogcatStorageHelper.addLog("start reget mode, some data dismiss");
			}
			
			String confirmbitString = Integer.toHexString(confirmbit);
			
			if(key == BLEConsts.OP_CODE_DATA_CONFIRM_KEY) {
				codeBuffer[0] = BLEConsts.OP_CODE_DATA_CONFIRM_KEY;
			} else {
				codeBuffer[0] = BLEConsts.MATE_OP_CODE_CONFIRM_KEY;
			}
			
			codeBuffer[1] = ((Integer)Integer.parseInt(confirmbitString.substring(0, 2), 16)).byteValue();
			codeBuffer[2] = ((Integer)Integer.parseInt(confirmbitString.substring(2, 4), 16)).byteValue();
			codeBuffer[3] = ((Integer)Integer.parseInt(confirmbitString.substring(4, 6), 16)).byteValue();
			codeBuffer[4] = ((Integer)Integer.parseInt(confirmbitString.substring(6, 8), 16)).byteValue();
			
			break;
		case BLEConsts.OP_CODE_TIME_SYNC_KEY:
			int sec = BLEConsts.getSeconds();

			if(mBleDevice.getType() == DeviceInfo.DEVICE_TYPE_GO) {
				sec = BLEConsts.getSecondsWithoutTimeZone();

				TimeZone timeZone = TimeZone.getDefault();
				offset = timeZone.getRawOffset();
				int offsetHour = (int) ((offset) / (3600 * 1000f));
				codeBuffer[6] = (byte) ((offsetHour + 12) & 0xff);
			}
			codeBuffer[0] = BLEConsts.OP_CODE_TIME_SYNC_KEY;
			codeBuffer[2] = (byte) ((sec >> 24) & 0xFF);
			codeBuffer[3] = (byte) ((sec >> 16) & 0xFF);
			codeBuffer[4] = (byte) ((sec >> 8) & 0xFF);
			codeBuffer[5] = (byte) ((sec >> 0) & 0xFF);
			break;
		case BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY:
		case BLEConsts.OP_CODE_DATA_READ_KEY:
		case BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY:
		case BLEConsts.OP_CODE_DATA_COMPLETE_KEY:
		case BLEConsts.OP_CODE_BATTERY_KEY:
		case BLEConsts.MATE_OP_CODE_COMPLETE_KEY:
		case 213:
			codeBuffer[0] = (byte) key;
			break;
		case BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY:
			codeBuffer[0] = 'H';
			codeBuffer[1] = 'W';
			codeBuffer[2] = 0x1b;
			codeBuffer[3] = 0x00;
			codeBuffer[4] = 0x1e;
			codeBuffer[5] = 0x00;
			break;
		case BLEConsts.OP_CODE_INIT_DFU_PARAMS_KEY:
			codeBuffer[0] = Conversion.loUint16(mFileImgHdr.ver);
			codeBuffer[1] = Conversion.hiUint16(mFileImgHdr.ver);
			codeBuffer[2] = Conversion.loUint16(mFileImgHdr.len);
			codeBuffer[3] = Conversion.hiUint16(mFileImgHdr.len);

		    System.arraycopy(mFileImgHdr.uid, 0, codeBuffer, 4, 4);
			break;
		case BLEConsts.OP_CODE_START_RESET_DEBUG_INFOR_KEY:
			address = deviceState.getExtra().getImageType().equals("B") ? 0x8c00 : 0x2000;
			addressString = Integer.toHexString(address);
			
			codeBuffer[0] = BLEConsts.OP_CODE_DATA_COMPLETE_KEY;
			codeBuffer[1] = ((Integer)Integer.parseInt(addressString.substring(0, 2), 16)).byteValue();
			codeBuffer[2] = ((Integer)Integer.parseInt(addressString.substring(2, 4), 16)).byteValue();
			break;
		case BLEConsts.OP_CODE_AQ_TEST_ENTRY:
				codeBuffer = new byte[9];
				codeBuffer[0] = (byte) 0xfa;
				codeBuffer[1] = (byte) 0xfc;
				codeBuffer[2] = (byte) 0xfd;
				codeBuffer[3] = (byte) 240;
				codeBuffer[4] = (byte) 1;
				codeBuffer[5] = (byte) 1;
				codeBuffer[6] = (byte) 0;
				codeBuffer[7] = (byte) 0;
				codeBuffer[8] = (byte) 0xfb;
			break;

		default:
			codeBuffer[0] = (byte) key;
			break;
		}

		char c = (char) codeBuffer[0];
		LogcatStorageHelper.addLog("[W-" + c + "] send data: " + parse(codeBuffer));
		
		return codeBuffer;
	}
	
	protected byte[] buildMateOpCodeBuffer(int key, String... params) throws UnknownParametersException, BLEAbortedException {
		byte[] codeBuffer = new byte[20];
		
		if(mAborted){
			throw new BLEAbortedException();
		}
		
		switch (key) {
		case BLEConsts.MATE_OP_CODE_REQUEST_KEY:
			if(mWriteData != null && mWriteData.size() > 0){
				if(mPartCurrent < mWriteData.size()){
                    LogcatStorageHelper.addLog("[W-" + (char) mWriteData.get(mPartCurrent)[0] + "] : " + parse(mWriteData.get(mPartCurrent)));
					return mWriteData.get(mPartCurrent);
				}
			}
			break;
		case BLEConsts.MATE_COMMAND_WRITE_WIFI:
			if(mWriteData != null && mWriteData.size() > 0){
				if(mPartCurrent < mWriteData.size()){
					return mWriteData.get(mPartCurrent);
				}
			}
			
			if(params == null || params.length < 2 || mWifiSecretKey == null){
				throw new UnknownParametersException("Invalid parameters", key, params);
			}
			
			try {
				byte[] curOriBuffer3 = mWifiSecretKey;
				byte[] curOriBuffer4 = null;
				byte[] curOriBuffer5 = null;

				int buffer4Length = 0;
				int buffer5Length = 0;

				if(params[1] != null){
					curOriBuffer4 = params[1].getBytes("UTF-8");
					buffer4Length = curOriBuffer4.length;
				}

				if(params.length == 3 && params[2] != null){
					curOriBuffer5 = params[2].getBytes("UTF-8");
					buffer5Length = curOriBuffer5.length;
				}

				byte[] oriBuffer1 = new byte[curOriBuffer3.length + buffer4Length + buffer5Length + 6];
				oriBuffer1[0] = 0x0;                //SSID
				oriBuffer1[1] = (byte) (curOriBuffer3.length & 0xff);
				System.arraycopy(curOriBuffer3, 0, oriBuffer1, 2, curOriBuffer3.length);

				oriBuffer1[curOriBuffer3.length + 2] = 0x3;   //secret
				if(curOriBuffer4 != null) {
					oriBuffer1[curOriBuffer3.length + 3] = (byte) (buffer4Length & 0xff);
					System.arraycopy(curOriBuffer4, 0, oriBuffer1, curOriBuffer3.length + 4, buffer4Length);
				}

				if (curOriBuffer5 != null) {  //mac
					oriBuffer1[curOriBuffer3.length + buffer4Length + 4] = 0x4;
					oriBuffer1[curOriBuffer3.length + buffer4Length + 5] = (byte) (buffer5Length & 0xff);
					System.arraycopy(curOriBuffer5, 0, oriBuffer1, curOriBuffer3.length + buffer4Length + 6, buffer5Length);
				}

				LogcatStorageHelper.addLog("set wifi name: " + params[0] + "  wifi pw: " + params[1]);
				buildWriteDataArray(BLEConsts.MATE_COMMAND_GET_WIFI, oriBuffer1);
				
				if(mWriteData != null && mWriteData.size() > 0){
					mPartCurrent = 0;
					char c = (char) mWriteData.get(0)[0];
					LogcatStorageHelper.addLog("[W-" + c + "] send data: " + parse(mWriteData.get(0)));
					return mWriteData.get(0);
				} else {
					throw new UnknownParametersException("get wifi failed, params null", BLEConsts.MATE_COMMAND_GET_WIFI);
				}
			} catch (UnsupportedEncodingException e) {
				throw new UnknownParametersException("Invalid parameters", key, params);
			}
		case BLEConsts.MATE_COMMAND_GET_WIFI_PAIRED:
			codeBuffer[0] = BLEConsts.MATE_OP_CODE_REQUEST_KEY;
			codeBuffer[1] = (byte) (0 & 0xff);
			codeBuffer[2] = (byte) (1 & 0x0f);
			
			System.arraycopy(FormatTransfer.toHH(301), 0, codeBuffer, 3, 4);
			System.arraycopy(FormatTransfer.toHH(6), 0, codeBuffer, 7, 4);
			
			codeBuffer[11] = (byte) 0xC8;////200
			codeBuffer[12] = (byte) 0x01;
			codeBuffer[13] = (byte) 0x06;
			codeBuffer[14] = (byte) 0x05;
			codeBuffer[15] = (byte) 0x01;
			codeBuffer[16] = (byte) 0x20;
			
			PetkitLog.d("[X]" + parse(codeBuffer));
			LogcatStorageHelper.addLog("[W-" + (char) codeBuffer[0] + "] get wifi paired send data: " + parse(codeBuffer));
			break;
		case BLEConsts.MATE_COMMAND_GET_WIFI:
			codeBuffer[0] = BLEConsts.MATE_OP_CODE_REQUEST_KEY;
			codeBuffer[1] = (byte) (0 & 0xff);
			codeBuffer[2] = (byte) (1 & 0x0f);

			System.arraycopy(FormatTransfer.toHH(301), 0, codeBuffer, 3, 4);
			System.arraycopy(FormatTransfer.toHH(6), 0, codeBuffer, 7, 4);

			codeBuffer[11] = (byte) 0xC8;////200
			codeBuffer[12] = (byte) 0x01;
			codeBuffer[13] = (byte) 0x05;
			codeBuffer[14] = (byte) 0x05;
			codeBuffer[15] = (byte) 0x01;
			codeBuffer[16] = (byte) 0x20;

			PetkitLog.d("[X]" + parse(codeBuffer));
			LogcatStorageHelper.addLog("[W-" + (char) codeBuffer[0] + "] get wifi send data: " + parse(codeBuffer));
			break;
		case BLEConsts.MATE_COMMAND_GET_SN:
			byte[] writeBuf1 = new byte[20];
			writeBuf1[0] = BLEConsts.MATE_OP_CODE_REQUEST_KEY;
			writeBuf1[1] = (byte) (0 & 0xff);
			writeBuf1[2] = (byte) (1 & 0x0f);
			
			System.arraycopy(FormatTransfer.toHH(301), 0, writeBuf1, 3, 4);
			System.arraycopy(FormatTransfer.toHH(6), 0, writeBuf1, 7, 4);
			
			writeBuf1[11] = (byte) 0xC8;////200
			writeBuf1[12] = (byte) 0x01;
		    writeBuf1[13] = (byte) 0x18;
            writeBuf1[14] = (byte) 0x05;
            writeBuf1[15] = (byte) 0x01;
            writeBuf1[16] = (byte) 0x0f;
			LogcatStorageHelper.addLog("[W-" + (char) codeBuffer[0] + "] get sn: " + parse(writeBuf1));
			return writeBuf1;
		case BLEConsts.MATE_COMMAND_WRITE_SERVER:
			try {
				byte[] data = params[0].getBytes("UTF8");
				LogcatStorageHelper.addLog("set server address: " + params[0]);
				buildWriteDataArray(BLEConsts.MATE_COMMAND_WRITE_SERVER, data);
				
				if(mWriteData != null && mWriteData.size() > 0){
					mPartCurrent = 0;
					char c = (char) mWriteData.get(0)[0];
					LogcatStorageHelper.addLog("[W-" + c + "] send data: " + parse(mWriteData.get(0)));
					return mWriteData.get(0);
				} else {
					throw new UnknownParametersException("write server failed", BLEConsts.MATE_COMMAND_WRITE_SERVER);
				}
			} catch (UnsupportedEncodingException e) {
				throw new UnknownParametersException("Invalid parameters", key, params);
			}
        case BLEConsts.MATE_COMMAND_WRITE_ALIVE:
            writeBuf1 = new byte[20];
            writeBuf1[0] = BLEConsts.MATE_OP_CODE_REQUEST_KEY;
            writeBuf1[1] = (byte) (0 & 0xff);
            writeBuf1[2] = (byte) (1 & 0x0f);

            System.arraycopy(FormatTransfer.toHH(BLEConsts.MATE_COMMAND_WRITE_ALIVE), 0, writeBuf1, 3, 4);
            System.arraycopy(FormatTransfer.toHH(0), 0, writeBuf1, 7, 4);

            LogcatStorageHelper.addLog("[W-" + (char) codeBuffer[0] + "] write alive code: " + parse(writeBuf1));
            return writeBuf1;

		default:
			throw new UnknownParametersException("invalid mate op code", key);
		}
		return codeBuffer;
	}
	
	
	protected void buildWriteDataArray(int cmd, byte[] data) {
		mWriteData = new ArrayList<>();
		mPartCurrent = 0;
		
		int index = 0, tempSize = 0;
		int totalBlockSize = 0;
		byte[] writeBuf = null;
		while (index < data.length) {
			writeBuf = new byte[20];
			writeBuf[0] = BLEConsts.MATE_OP_CODE_REQUEST_KEY;
			writeBuf[1] = (byte) (totalBlockSize & 0xff);
			
			if(index == 0){
				System.arraycopy(FormatTransfer.toHH(cmd), 0, writeBuf, 3, 4);
				System.arraycopy(FormatTransfer.toHH(data.length), 0, writeBuf, 7, 4);

				tempSize = (data.length - index) > 9 ? 9 : data.length - index;
				System.arraycopy(data, index, writeBuf, 11, tempSize);
			}else {
				tempSize = (data.length - index) > 17 ? 17 : data.length - index;
				System.arraycopy(data, index, writeBuf, 3, tempSize);
			}
			
			index += tempSize;
			totalBlockSize++;
			
			mWriteData.add(writeBuf);
		}
		
		for (byte[] tempData : mWriteData) {
			tempData[2] = (byte) (mWriteData.size() & 0xff);
			PetkitLog.d("[X]" + parse(tempData));
		}
	}
	
	protected void checkResponseValid(byte[] response, int request) throws UnknownResponseException {
		switch (request) {
		case BLEConsts.OP_CODE_DEVICE_INIT_KEY:
		case BLEConsts.OP_CODE_VERIFY_KEY:
		case BLEConsts.OP_CODE_TIME_SYNC_KEY:
		case BLEConsts.OP_CODE_DEBUG_INFOR_KEY:
		case BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY:
		case BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY:
		case BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY:
		case BLEConsts.OP_CODE_DATA_COMPLETE_KEY:
		case BLEConsts.MATE_OP_CODE_COMPLETE_KEY:
		case BLEConsts.MATE_OP_CODE_REQUEST_KEY:
		case BLEConsts.OP_CODE_BATTERY_KEY:
			if(response[0] != request){
				throw new UnknownResponseException("Invalid response received", response, request);
			}
			break;
		case BLEConsts.OP_CODE_DATA_READ_KEY:
			if(response[0] != BLEConsts.OP_CODE_DATA_READ_KEY
				&& response[0] != BLEConsts.OP_CODE_DATA_CONFIRM_KEY
				&& response[0] != BLEConsts.OP_CODE_DATA_COMPLETE_KEY){
				throw new UnknownResponseException("Invalid response received", response, request);
			}
			break;
		case BLEConsts.OP_CODE_DATA_CONFIRM_KEY:
			if(response[0] != BLEConsts.OP_CODE_DATA_COMPLETE_KEY){
				throw new UnknownResponseException("Invalid response received", response, request);
			}
			break;
		case BLEConsts.OP_CODE_START_RESET_DEBUG_INFOR_KEY:
			if(response[0] != BLEConsts.OP_CODE_DATA_COMPLETE_KEY){
				throw new UnknownResponseException("Invalid response received", response, request);
			}
			break;
		default:
			if(response[0] != request){
				throw new UnknownResponseException("Invalid response received", response, request);
			}
			break;
		}
		
	}
	
	protected void parserReceivedData(byte[] byteChar) throws UnexpectedCompleteException, UnknownResponseException, UnsupportedEncodingException {
		
//		StringBuilder stringBuilder1 = new StringBuilder(byteChar.length);
//		for (int i = 0; i < byteChar.length; i++){
//			byte tempByte = byteChar[i];
//			stringBuilder1.append(String.format("%02X ", tempByte));
//		}
//		LogcatStorageHelper.addLog("DataCommandParse byteChar:" + stringBuilder1.toString());
		
		switch (byteChar[0]) {
		case BLEConsts.OP_CODE_DATA_READ_KEY:
			int index;
			index = byteChar[1] & 0xFF;
			curSectionBlockSize = byteChar[2] & 0xFF;
			PetkitLog.d(TAG, "[R-D] process data index:" + String.valueOf(index) + '/' + String.valueOf(curSectionBlockSize));
			StringBuilder stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 2; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[R-D] receive index:" + String.valueOf(index) + '/' + String.valueOf(curSectionBlockSize)
					+ " data: " + stringBuilder.toString());
			
			if(dataConfirm(index)){
				parseActivityData(byteChar);
			}
			break;
			////////////////
		case BLEConsts.MATE_OP_CODE_REQUEST_KEY:
			byte[] tempBytes;
			
			curSectionBlockSize = byteChar[2] & 0xFF;
			int index1 = byteChar[1] & 0xFF;
			tempBytes = new byte[byteChar.length - 3];
			System.arraycopy(byteChar, 3, tempBytes, 0, byteChar.length - 3);
			
			if(index1 + 1 == curSectionBlockSize && curSectionBlockSize == 32 && DataConfirmFlag == 0){
				return;
			}
			
			LogcatStorageHelper.addLog("[R-X] " + parse(byteChar));
			mTempReceivedWifiData.put((byteChar[1] & 0xFF), tempBytes);
			dataConfirm(index1);
			break;
		case BLEConsts.OP_CODE_TIME_SYNC_KEY:
			for (int i = 0; i < 4; i++) {
				mImageSizeInBytes += (byteChar[2+i] & 0x000000FF) << (8*(3 - i));
			}
			PetkitLog.d(TAG, "data length:" + String.valueOf(mImageSizeInBytes));
			LogcatStorageHelper.addLog("[R-T] data length:" + String.valueOf(mImageSizeInBytes));

			updateProgressNotification(BLEConsts.PROGRESS_SYNC_TIME, String.valueOf(mImageSizeInBytes));
			break;
		case BLEConsts.OP_CODE_VERIFY_KEY:
			deviceState.setVerify(byteChar[1] != 0);
			PetkitLog.d(TAG, "verify:" + String.valueOf(deviceState.isVerify()));
			PetkitLog.d(TAG, "compile time:" + byteChar);
			
			deviceState.setHardware(byteChar[1]);
			deviceState.setFirmware((byteChar[2]));

			if(BLEConsts.PET_FIT_DISPLAY_NAME.equals(mBleDevice.getName()) && deviceState.getHardware() == 1){
				deviceState.setExtra(new Extra(deviceState.getFirmware() % 2 == 0 ? "A" : "B"));
				deviceState.setFirmware(deviceState.getFirmware() / 2);
			}else {
				deviceState.setExtra(new Extra("A"));
				deviceState.setFirmware(deviceState.getFirmware());
			}
			
			deviceState.setFrequence(byteChar[3] & 0xFF);

			PetkitLog.d(TAG, "hardware: " + deviceState.getHardware());
			PetkitLog.d(TAG, "firmware: " + deviceState.getFirmware());
			PetkitLog.d(TAG, "frequence: " + deviceState.getFrequence());
			
			LogcatStorageHelper.addLog("Current dog id: " + (mCurDog == null ? "null" : mCurDog.getId()) + ", device id: " + targetDeviceId);
			LogcatStorageHelper.addLog("[R-V] verify:" + String.valueOf(deviceState.isVerify()) + "  hardware: " + deviceState.getHardware()
					+ "  firmware: " + deviceState.getFirmware() + "  frequence: " + deviceState.getFrequence());
			
			if(!deviceState.isVerify()){
				mError = BLEConsts.ERROR_SYNC_VERIFY_FAIL;
				throw new UnexpectedCompleteException("Device verify failed", mError);
			}

			updateProgressNotification(BLEConsts.PROGRESS_VERIFY, new Gson().toJson(deviceState));
			break;
		case BLEConsts.OP_CODE_DEVICE_INIT_KEY:		
			if (byteChar[1] != 0x1) {
				mError = BLEConsts.ERROR_SYNC_INIT_FAIL;
				updateProgressNotification(BLEConsts.ERROR_SYNC_INIT_FAIL);
				throw new UnexpectedCompleteException("Device init failed", mError);
			}
			break;
		case BLEConsts.OP_CODE_DATA_COMPLETE_KEY:
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 0; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[R-E] receive " + " data: " + stringBuilder.toString());
			saveConfimedSectionData();
			break;
		case BLEConsts.OP_CODE_DEBUG_INFOR_KEY:
			PetkitLog.d(TAG, "m command: " + mComdLength);
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 2; i < byteChar.length && i < 10; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			mDebugInfor.append(stringBuilder.toString());
			
			if(mComdLength >= BLEConsts.WRITE_M_CMD_TIMES){
				LogcatStorageHelper.addLog("[R-M] debug information: " + mDebugInfor.toString());
			}
			break;
		case BLEConsts.OP_CODE_DEBUG_INFOR_2_KEY:
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 0; i < byteChar.length; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[R-G] receive " + " data: " + stringBuilder.toString());
			break;
		case BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY:
			boolean result = byteChar[1] != 0;
			if(result){
				LogcatStorageHelper.addLog("[R-S] start download success");
			}else{
				LogcatStorageHelper.addLog("[R-S] start download fail");
				throw new UnknownResponseException("Invalid response received", byteChar, BLEConsts.OP_CODE_DEVICE_DOWNLOAD_KEY);
			}
			break;
		case BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY:
			if(byteChar[1] == 'W'){
				LogcatStorageHelper.addLog("[R-HW] start HW-command success");
			}else{
				LogcatStorageHelper.addLog("[R-HW] start HW-command fail");
				throw new UnknownResponseException("Invalid response received", byteChar, BLEConsts.OP_CODE_TRUN_OFF_SENSOR_KEY);
			}
			break;	
		case BLEConsts.OP_CODE_BATTERY_KEY:
			stringBuilder = new StringBuilder(byteChar.length);
			for (int i = 1; i < byteChar.length && i < 10; i++){
				byte tempByte = byteChar[i];
				stringBuilder.append(String.format("%02X ", tempByte));
			}
			LogcatStorageHelper.addLog("[R-B] " + stringBuilder.toString());
			
			int voltage = byteChar[1] * 16 * 16 + (byteChar[2] & 0xff);
			deviceState.setVoltage(voltage / 1000f);
			deviceState.setBattery(byteChar[3]);
			if (deviceState.getBattery() < Consts.DEVICE_LOW_BATTERY) {
				if (!CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).contains("&" + deviceState.getId())) {
					CommonUtils.addSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG, CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG) + "&"
							+ deviceState.getId());
				}
			} else {
				if (CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).contains("&" + deviceState.getId())) {
					CommonUtils.addSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG,
					CommonUtils.getSysMap(Consts.SHARED_DEVICE_BATTERY_LOW_FLAG).replace("&" + deviceState.getId(), ""));
				}
			}
			break;
		case BLEConsts.MATE_OP_CODE_COMPLETE_KEY:
//			stringBuilder = new StringBuilder(byteChar.length);
//			LogcatStorageHelper.addLog("[R-Z] " + stringBuilder.toString());
			parseReceiveData();
			break;
		case (byte) 200:
			mBleDevice.setHardware(byteChar[1]);
			mBleDevice.setFireware(byteChar[2]);
			byte[] dateArray = new byte[7];
			byte[] timeArray = new byte[8];

			System.arraycopy(byteChar, 3, dateArray, 0, 7);
			System.arraycopy(byteChar, 10, timeArray, 0, 8);

			PetkitLog.d("dateString: " + new String(dateArray));
			PetkitLog.d("timeString " + new String(timeArray));
			mBleDevice.setBuildDate(new String(dateArray) + " " + new String(timeArray));
			break;
		case (byte) 0xfa:
			if (byteChar[3]  == 240) {
				mBleDevice.setTestResult(byteChar[8]);
			}
			if (byteChar[3]  == 240 && byteChar[8] == 1) {
				PetkitLog.d("AQ entry test success");
			} else {
				PetkitLog.d("AQ entry test failed");
			}
			break;

		default:
			break;
		}
	}
	
	private boolean dataConfirm(int index) {
		boolean result = (DataConfirmFlag & (1 << index)) == 0;
		
		if(result){
			DataConfirmFlag = DataConfirmFlag | (1 << index);
		}
		return result;
	}
	
	private void parseActivityData(byte[] byteRaw) {
		StringBuffer tempBuffer = new StringBuffer();
		
		if (byteRaw[3] == 0x00){ // time data
			// caculate the data time
			int date_time_offset = 0;
			
			for (int i = 0; i < 4; i++) {
				date_time_offset += ((byteRaw[4+i] & 0x000000FF) << (8*(3 - i))); 
			}

			for (int i = 0; i < 4; i++) {
				date_time_offset += ((byteRaw[8+i] & 0x000000FF) << (8*(3 - i))); 
			}
			
			tempBuffer.append(date_time_offset);
			calculateEnergy(byteRaw, 12, tempBuffer);
		}else{ //byteRaw.charAt(3) == 0xFF
			calculateEnergy(byteRaw, 4, tempBuffer);
		}
		PetkitLog.d("mTempDataBuffers put " + "index: " + (byteRaw[1] & 0xFF) + "data: " + tempBuffer.toString());
		mTempDataBuffers.put((byteRaw[1] & 0xFF), tempBuffer);
		mBytesSent += 16;
	}
	
	
	private void calculateEnergy(byte[] byteRaw, int index, StringBuffer tempBuffer) {
		int type, energy = 0, len = 0, m = index;
		while (m < BLEConsts.MAX_PACKET_SIZE) {
			energy = 0;
			type = byteRaw[m] >> 4;
			len = byteRaw[m] & 0x0f;
			if (type == 0){ // 0x03 for data
				for (int i = 0; i < len; i++) {
				    energy += (((byteRaw[m+1+i])&0xFF)<<((2-i)*8)); //offset the timeline
	            }
				tempBuffer.append(BLEConsts.DATA_SPIT).append(energy);
			}
			if (type == 1) {// 0x13 for compact data
				for (int i = 0; i < len; i++) {
				    energy += ((byteRaw[m+1+i]&0xFF)<<(8*(2-i))); //offset the timeline
				}
				for (int i = 0; i < energy; i++) {
					tempBuffer.append(BLEConsts.DATA_SPIT).append(0);
				}
			}
			m = m + len + 1;
		}
	}
	
	protected boolean checkSectionDataComplete(byte[] response) {
		int index = response[1] & 0xFF;
		int blocksize = response[2] & 0xFF;
		if ((index+1) == blocksize || (mDataMissMode && checkSectionDataConfirmFlag(DataConfirmFlag, blocksize))) {
			PetkitLog.d(TAG, "send confirmbit to get next packages data. mDataMissMode: " + mDataMissMode);
			LogcatStorageHelper.addLog("[R-D] send confirmbit to get next packages data.");
			return true;
		}
		
		return false;
	}
	
	private boolean checkSectionDataConfirmFlag(int dataConfirm, int blockSize){
		int confirmbit = 0;
		for (int i = 0; i < blockSize; i++) {
			confirmbit = confirmbit | (1 << i);
		}
		return dataConfirm == confirmbit;
	}
	
	
	protected void saveConfimedSectionData(){
		if(mTempDataBuffers.size() > 0){
			StringBuffer tempBuffer = mTempDataBuffers.get(0);
			if(tempBuffer == null){
				PetkitLog.d("saveConfimData tempBuffer == null");
			}
			for(int i = 1; i < mTempDataBuffers.size(); i++){
				tempBuffer.append(mTempDataBuffers.get(i));
			}
			
			if(mDataBuffers.size() == 0){
				if(tempBuffer.charAt(0) == BLEConsts.DATA_SPIT){
					tempBuffer.insert(0, 0);
				}
				mDataBuffers.add(tempBuffer);
			}else{
				if(tempBuffer.charAt(0) == BLEConsts.DATA_SPIT){
					mDataBuffers.get(mDataBuffers.size() - 1).append(tempBuffer);
				}else{
					saveConfirmedData(mCurDog);
					mDataBuffers.add(tempBuffer);
				}
			}
			mTempDataBuffers.clear();
		}
	}
	
	////////////
	protected void saveConfimedWifiData(){
		if(mTempReceivedWifiData.size() > 0){
			for(int i = 0;  i < mTempReceivedWifiData.size(); i++) {
				mReceivedWifiData.append(mTempReceivedWifiData.get(i), 0, mTempReceivedWifiData.get(i).length);
			}
			mTempReceivedWifiData.clear();
		}
	}
	
	protected void parseReceiveData() throws UnknownResponseException, UnsupportedEncodingException {
//		try {
			final byte[] data = mReceivedWifiData.toByteArray();

			byte[] cmdArray = new byte[4];
			System.arraycopy(data, 0, cmdArray, 0, 4);
			int cmd = (int) (cmdArray[0] * Math.pow(16, 6) + cmdArray[1] * Math.pow(16, 4) + cmdArray[2] * Math.pow(16, 2) + cmdArray[3]);//Integer.valueOf(new String(cmdArray), 16);
			
			byte[] lengthArray = new byte[4];
			System.arraycopy(data, 4, lengthArray, 0, 4);
			int length = (int) (lengthArray[0] * Math.pow(16, 6) + lengthArray[1] * Math.pow(16, 4) + lengthArray[2] * Math.pow(16, 2) + lengthArray[3]);//Integer.valueOf(new String(cmdArray), 16);

			PetkitLog.d("parseReceiveData cmd: " + cmd);
			LogcatStorageHelper.addLog("[R-Z] parse received data, cmd: " + cmd + "  data: " + parse(data));
			
			switch (cmd) {
			case BLEConsts.MATE_COMMAND_GET_WIFI:
				LogcatStorageHelper.addLog("[R-Z] parserWifiList wifi number = " + data[10]);
				parserWifiList(data, 8+3);
				break;
			case BLEConsts.MATE_COMMAND_GET_SN:
				byte[] dataArray = new byte[length];
				System.arraycopy(data, 8, dataArray, 0, length);
				LogcatStorageHelper.addLog("[R-Z] sn = " + new String(dataArray));
				updateProgressNotification(BLEConsts.PROGRESS_SYNC_DATA, new String(dataArray));
				break;
			case BLEConsts.MATE_COMMAND_WRITE_SERVER:
				if(data[8] != 1){
					throw new UnknownResponseException("write mate server failed!", data, 1);
				}
				break;
			case BLEConsts.MATE_COMMAND_WRITE_WIFI_CONFIRM:
				if(data[10] >= BLEConsts.WIFI_CFG_OK && data[10] <= BLEConsts.WIFI_NETDNS_OK) { //BRANKER 20150323
					LogcatStorageHelper.addLog("[R-A] start Pethome success");//TODO
					isWriteWifiSuccess = true;
				}else{
					LogcatStorageHelper.addLog("[R-A] start Pethome failed error code = " + data[10]);
					isWriteWifiSuccess = false;
				}
				updateProgressNotification(BLEConsts.PROGRESS_WIFI_SET_RESULT, "" + data[10]);
				break;

			default:
				break;
			}
			
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//		mReceivedWifiData.clear();	
//		}
		mReceivedWifiData.clear();
	}
	
	
	private void parserWifiList(byte[] data, int startPosition) throws UnsupportedEncodingException{
		int index = startPosition;
		byte start = data[index];
		
		WifiInfo wifiinfo = null;
		
		try {
			while(index < data.length) {
				byte type = data[index++];
				if(start == type) {
					if(wifiinfo != null && wifiinfo.getLevel() > 0) {
						LogcatStorageHelper.addLog("[R-Z] parse wifi: " + new Gson().toJson(wifiinfo));
						sendScanedWifiBroadcast(wifiinfo);
					}
					wifiinfo = new WifiInfo();
				}

                if(index >= data.length) {
                    break;
                }
				int len = data[index++];
				
				if(len == 0) {
					continue;
				}
				
				byte[] temp = new byte[len];
				System.arraycopy(data, index, temp, 0, len);
				index += len;

				if(wifiinfo == null){
					continue;
				}
				switch(type) {
				case 0:
					wifiinfo.setSSID(temp);
                    wifiinfo.setDisplayName(new String(temp, "UTF8"));
					break;
				case 1:
					wifiinfo.setLevel(temp[0]);
					break;
				case 2:
//					wifiinfo.setCapabilities(new String(temp));
					wifiinfo.setPassword(temp[0]);
					break;
//				case 3:
//					wifiinfo.setPassword(temp[0]);
//					break;
				case 4:
					wifiinfo.setBSSID(new String(temp));
					break;
				case 6:
					wifiinfo.setAddress(new String(temp));
					break;
                case 7:
                    wifiinfo.setDeviceMac(new String(temp));
                    break;
				case 8:
					wifiinfo.setDisplayName(new String(temp, "GBK"));
					break;
				default:
//					mReceivedWifiData.clear();
//					sendScanedWifiCompletedBroadcast();
//					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(wifiinfo != null && wifiinfo.getLevel() > 0) {
				LogcatStorageHelper.addLog("[R-Z] parse wifi: " + new Gson().toJson(wifiinfo));
				sendScanedWifiBroadcast(wifiinfo);
			}
			sendScanedWifiCompletedBroadcast();
		}
	}
	
	protected void saveConfirmedData(Pet dog){
		if(mDataBuffers == null || mDataBuffers.size() == 0){
			return;
		}
		
		DeviceActivityDataUtils.saveChangedActivityData(mDataBuffers.get(0), dog, 10);
		mDataBuffers.remove(0);
//		mDataBuffers.clear();
		
		updateProgressNotification(BLEConsts.PROGRESS_DATA_SAVED);
	}
	
	
	protected void startServiceToUploadData() {
		if(!CommonUtils.isEmpty(activityDataSaveUrl) && !CommonUtils.isEmpty(dailyDetailUrl)){
            LogcatStorageHelper.addLog("ble sync complete, start service to upload data");
			Intent intent2 = new Intent(this, ActivityDataProcessService.class);
			intent2.putExtra(BLEConsts.EXTRA_DEVICE_INFO, deviceState);
			intent2.putExtra(BLEConsts.EXTRA_URL_DATA_SAVE, activityDataSaveUrl);
			intent2.putExtra(BLEConsts.EXTRA_URL_DAILY_DETAIL, dailyDetailUrl);
			intent2.putExtra(BLEConsts.EXTRA_DOG, mCurDog);
			intent2.putExtra(BLEConsts.EXTRA_BOOLEAN_STORE_PROGRESS, isNeedStoreProgress);
			startService(intent2);
		}
	}
	
	/********************************** PETKIT V1 OTA  ****************************************/
	protected ImgHdr mFileImgHdr = new ImgHdr();
	protected ProgInfo mProgInfo = new ProgInfo();
	
	protected static final int OAD_BLOCK_SIZE = 16;
	protected static final int HAL_FLASH_WORD_SIZE = 4;
	protected static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
	protected static final int PKT_INTERVAL = 60; // Milliseconds

	protected TimerTask mTimerTask = null;
	protected Timer mTimer = null;
	protected boolean mProgramming;
	
	// Programming
	protected byte[] mFileBuffer;
	
	
	protected boolean initBufferData(byte[] oadBuffer) {

		// Show image info
		mFileImgHdr.ver = Conversion
				.buildUint16(mFileBuffer[5], mFileBuffer[4]);
		mFileImgHdr.len = Conversion
				.buildUint16(mFileBuffer[7], mFileBuffer[6]);
		mFileImgHdr.imgType = (mFileImgHdr.ver & 1);
		System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);

		// Verify image types
		return mFileImgHdr.imgType != (deviceState.getExtra().getImageType().equals("A") ? 0 : 1);
	}
	

	protected class ImgHdr {
		public short ver;
		public short len;
		public int imgType;
		public byte[] uid = new byte[4];
	}

	protected class ProgInfo {
		public int iBytes = 0; // Number of bytes programmed
		public short iBlocks = 0; // Number of blocks programmed
		public short nBlocks = 0; // Total number of blocks

		public void reset() {
			iBytes = 0;
			iBlocks = 0;
			nBlocks = (short) (mFileImgHdr.len / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
			mBytesSent = 0;
			mImageSizeInBytes = nBlocks;
			PetkitLog.d("nBlocks: " + nBlocks);
		}
	}
	
	protected boolean checkOTADebugInforResetSuccess() throws UnknownResponseException{
		if(mDebugInfor == null){
			throw new UnknownResponseException("reset debug infor failed", mDebugInfor.toString().getBytes(), BLEConsts.OP_CODE_DEBUG_INFOR_KEY);
		}
		String debugString = mDebugInfor.toString();
		for (int i = 0; i < debugString.length(); i++){
			char chatTemp = debugString.charAt(i);
			if(chatTemp == ' ' || chatTemp == 'F'){
				continue;
			}else {
				throw new UnknownResponseException("reset debug infor failed", mDebugInfor.toString().getBytes(), BLEConsts.OP_CODE_DEBUG_INFOR_KEY);
			}
		}
		
		return true;
	}
	
	
	//------------------------- Heartbeat ------------

	
	private Timer mHeartbeaTimer;
//	private final static int Heart_Beat_Duration = 30000;
	protected long lastHeartbeatTimeMillis;
	protected int lastHeartbeatDuration;
	
	protected class HeartbeatTimerTask extends TimerTask {
		
		@Override
		public void run() {
			PetkitLog.d("HeartbeatTimerTask lastHeartbeatTimeMillis: " + lastHeartbeatTimeMillis + ", currentTimeMillis： " + System.currentTimeMillis());
			if(!mPaused && System.currentTimeMillis() - lastHeartbeatTimeMillis > lastHeartbeatDuration){
				mError = BLEConsts.ERROR_SYNC_TIMEOUT;
				stopHeartbeat();
				
				// notify waiting thread
				synchronized (mLock) {
					mLock.notifyAll();
					return;
				}
			}
		}
	}
	
	
	protected void refreshHeartbeatTime() {
        if(mKeepAliveTimer == null) {
            lastHeartbeatTimeMillis = System.currentTimeMillis();
        }
	}
	
	protected void stopHeartbeat() {
		if(mHeartbeaTimer != null){
			mHeartbeaTimer.cancel();
			mHeartbeaTimer = null;
			
			if(mTimerTask != null){
				mTimerTask.cancel();
				mTimerTask = null;
			}
		}
	}
	
	protected void startHeartbeatToCheckActionInNormal(int duration) {
		if(lastHeartbeatDuration == duration){
			return;
		}
		
		stopHeartbeat();
		
		lastHeartbeatDuration = duration;
		PetkitLog.d("startHeartbeatToCheckActionInNormal duration: " + duration);
		lastHeartbeatTimeMillis = System.currentTimeMillis();
		mHeartbeaTimer = new Timer();
		mTimerTask = new HeartbeatTimerTask();
		mHeartbeaTimer.schedule(mTimerTask, lastHeartbeatDuration, lastHeartbeatDuration);
	}


	// -------------------KeepAlive----------

	protected Timer mKeepAliveTimer;
	protected TimerTask mKeepAliveTimerTask;
	private final static int write_duration = 2000;
	protected class KeepAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			if(mPaused && !mAborted && mError == 0){
				sendKeepAliveMessage();
			}

		}
	}

	protected void stopKeepAlive() {
		if(mKeepAliveTimer != null){
			mKeepAliveTimer.cancel();
			mKeepAliveTimer = null;

			if(mKeepAliveTimerTask != null){
				mKeepAliveTimerTask.cancel();
				mKeepAliveTimerTask = null;
			}
			PetkitLog.d("stopKeepAlive");
		}
	}

	protected void startKeepAlive() {

        if(BLEConsts.compareMateVersion(curMateVersion, BLEConsts.MATE_BASE_VERSION_FOR_ALIVE_CMD) < 0){
            return;
        }
        LogcatStorageHelper.addLog("startKeepAlive");

		if(mKeepAliveTimer == null){
			mKeepAliveTimer = new Timer();
			mKeepAliveTimerTask = new KeepAliveTimerTask();
			mKeepAliveTimer.schedule(mKeepAliveTimerTask, write_duration, write_duration);
			PetkitLog.d("startKeepAlive");
		}
	}



	
	protected abstract void startOTA(final Intent intent); 
	protected abstract void startSyncDevice(final Intent intent); 
	protected abstract void startInitAndChangeFit(final Intent intent); 
	protected abstract DeviceInfo startScan(final Intent intent);
	protected abstract void startMateWifiInit(final Intent intent);
	protected abstract void startInitAndChangeMate(final Intent intent);
	protected abstract void stop();
	protected abstract void sendKeepAliveMessage();
	protected abstract void startGoOTA(final Intent intent);
	protected abstract void startInitAndChangeGo(final Intent intent);
	protected abstract void startGoSampling(final Intent intent);
	protected abstract void startAQTest(final Intent intent);
	protected abstract void startW5Test(final Intent intent);
	protected abstract void startBleDeviceTest(final Intent intent);
//	protected abstract void startP3Test(final Intent intent);
//	protected abstract void startAQRTest(final Intent intent);

	protected abstract void onBlockTimer();
	
}
