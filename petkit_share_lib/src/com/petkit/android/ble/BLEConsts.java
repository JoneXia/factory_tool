package com.petkit.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.text.TextUtils;

import com.petkit.android.utils.DateUtil;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BLEConsts {

	public final static boolean DEBUG = true;

    public final static int WRITE_M_CMD_TIMES = 1;

	public static final String PET_HOME = "pethome";
	public static final String PET_MATE = "petmate";
	public static final String PET_FIT = "PETKIT";
	public static final String PET_FIT2 = "PETKIT2";
	public static final String PET_FIT_DISPLAY_NAME = "Fit P1";
	public static final String PET_FIT2_DISPLAY_NAME = "Fit P2";
	public static final String GO_DISPLAY_NAME = "petGO";
	public static final String K2_DISPLAY_NAME = "Petkit_K2";
	public static final String T3_DISPLAY_NAME = "Petkit_T3";
	public static final String D3_DISPLAY_NAME = "Petkit_D3";
	public static final String D4_DISPLAY_NAME = "Petkit_D4";
	public static final String AQ_DISPLAY_NAME = "Petkit_AQ";
	public static final String P3_DISPLAY_NAME = "Petkit_P3";
	public static final String W5_DISPLAY_NAME = "Petkit_W5C";
	public static final String T4_DISPLAY_NAME = "Petkit_T4";
	public static final String K3_DISPLAY_NAME = "Petkit_K3";
	public static final String AQR_DISPLAY_NAME = "Petkit_AQR";

	public static final String[] DeviceFilter = new String[]{"PETKIT", "PETKIT2", PET_FIT_DISPLAY_NAME, PET_FIT2_DISPLAY_NAME,
			PET_HOME, PET_MATE, GO_DISPLAY_NAME, K2_DISPLAY_NAME, T3_DISPLAY_NAME, D3_DISPLAY_NAME, D4_DISPLAY_NAME,
			AQ_DISPLAY_NAME, P3_DISPLAY_NAME, W5_DISPLAY_NAME, T4_DISPLAY_NAME, K3_DISPLAY_NAME, AQR_DISPLAY_NAME};

	public static final int BLE_ACTION_SYNC				= 0x1;
	public static final int BLE_ACTION_CHECK			= 0x2;
	public static final int BLE_ACTION_OTA				= 0x3;
	public static final int BLE_ACTION_SCAN				= 0x4;
	public static final int BLE_ACTION_INIT				= 0x5;
	public static final int BLE_ACTION_CHANGE			= 0x6;
	public static final int BLE_ACTION_OTA_RECONNECT	= 0x7;
	public static final int BLE_ACTION_BEACONS			= 0x8;
	public static final int BLE_ACTION_HS_INIT_WIFI	    = 0x9;
	public static final int BLE_ACTION_INIT_HS			= 0x0A;
	public static final int BLE_ACTION_CHANGE_HS		= 0x0B;
	public static final int BLE_ACTION_OTA_GO			= 0x0C;
	public static final int BLE_ACTION_OTA_GO_RECONNECT			= 0x0D;
	public static final int BLE_ACTION_GO_INIT				= 0xE;
	public static final int BLE_ACTION_GO_CHANGE			= 0xF;
	public static final int BLE_ACTION_GO_SAMPLING			= 0x10;
	public static final int BLE_ACTION_AQ_TEST			= 0x11;
	public static final int BLE_ACTION_P3_TEST			= 0x12;
	public static final int BLE_ACTION_W5_TEST			= 0x13;
	public static final int BLE_ACTION_K3_TEST			= 0x14;
	public static final int BLE_ACTION_AQR_TEST			= 0x15;


	public static final int ACTION_PAUSE = 0;
	public static final int ACTION_RESUME = 1;
	public static final int ACTION_ABORT = 2;
	public static final int ACTION_STEP_ENTRY = 3;
	public static final int ACTION_STEP_QUIT = 4;
	
	public static final int NOTIFICATIONS = 1;
	public static final int INDICATIONS = 2;

	public final static long SCAN_DURATION = 10000;
	
	public final static int MAX_RECONNECT_TIMES = 2;

	/** The address of the device. */
	public static final String EXTRA_DEVICE_ADDRESS = "com.petkit.android.extra.EXTRA_DEVICE_ADDRESS";
	/** The secret key of the device. */
	public static final String EXTRA_SECRET_KEY = "com.petkit.android.extra.EXTRA_SECRET_KEY";
	/** The secret key of the device. */
	public static final String EXTRA_WIFI_SECRET_KEY = "com.petkit.android.extra.EXTRA_WIFI_SECRET_KEY";
	/** The secret of the device. */
	public static final String EXTRA_SECRET = "com.petkit.android.extra.EXTRA_SECRET";
	/** The device id of the device. */
	public static final String EXTRA_DEVICE_ID = "com.petkit.android.extra.EXTRA_DEVICE_ID";
	public static final String EXTRA_NEED_WIFILIST = "com.petkit.android.extra.EXTRA_NEED_WIFILIST";
	
	/** The optional device name. This name will be shown in the notification. */
	public static final String EXTRA_DEVICE_NAME = "com.petkit.android.extra.EXTRA_DEVICE_NAME";
	/**
	 * <p>
	 * If the new firmware does not share the bond information with the old one the bond information is lost. Set this flag to <code>true</code> to make the service create new bond with the new
	 * application when the upload is done (and remove the old one). When set to <code>false</code> (default), the DFU service assumes that the LTK is shared between them. Currently it is not possible
	 * to remove the old bond and not creating a new one so if your old application supported bonding while the new one does not you have to modify the source code yourself.
	 * </p>
	 * <p>
	 * In case of updating the soft device and bootloader the application is always removed so the bond information with it.
	 * </p>
	 * <p>
	 * Search for occurrences of EXTRA_RESTORE_BOND in this file to check the implementation and get more details.
	 * </p>
	 */
	public static final String EXTRA_RESTORE_BOND = "com.petkit.android.extra.EXTRA_RESTORE_BOND";
	public static final String EXTRA_LOG_URI = "com.petkit.android.extra.EXTRA_LOG_URI";
	public static final String EXTRA_FILE_PATH = "com.petkit.android.extra.EXTRA_FILE_PATH";
	public static final String EXTRA_FILE_URI = "com.petkit.android.extra.EXTRA_FILE_URI";
	/**
	 * The Init packet URI. This file is required if Extended Init Packet is required. Must point to a 'dat' file corresponding with the selected firmware. The Init packet may contain just the CRC
	 * (in case of older versions of DFU) or the Extended Init Packet in binary format.
	 */
	public static final String EXTRA_INIT_FILE_PATH = "com.petkit.android.extra.EXTRA_INIT_FILE_PATH";
	/**
	 * The Init packet path. This file is required if Extended Init Packet is required. Must point to a 'dat' file corresponding with the selected firmware. The Init packet may contain just the CRC
	 * (in case of older versions of DFU) or the Extended Init Packet in binary format.
	 */
	public static final String EXTRA_INIT_FILE_URI = "com.petkit.android.extra.EXTRA_INIT_FILE_URI";

	/**
	 * The input file mime-type. Currently only "application/zip" (ZIP) or "application/octet-stream" (HEX or BIN) are supported. If this parameter is empty the "application/octet-stream" is assumed.
	 */
	public static final String EXTRA_FILE_MIME_TYPE = "com.petkit.android.extra.EXTRA_MIME_TYPE";

	/**
	 * This optional extra parameter may contain a file type. Currently supported are:
	 * <ul>
	 * <li>{@link #TYPE_SOFT_DEVICE} - only Soft Device update</li>
	 * <li>{@link #TYPE_BOOTLOADER} - only Bootloader update</li>
	 * <li>{@link #TYPE_APPLICATION} - only application update</li>
	 * <li>{@link #TYPE_AUTO} -the file is a ZIP file that may contain more than one HEX. The ZIP file MAY contain only the following files: <b>softdevice.hex</b>, <b>bootloader.hex</b>,
	 * <b>application.hex</b> to determine the type based on its name. At lease one of them MUST be present.</li>
	 * </ul>
	 * If this parameter is not provided the type is assumed as follows:
	 * <ol>
	 * <li>If the {@link #EXTRA_FILE_MIME_TYPE} field is <code>null</code> or is equal to {@value #MIME_TYPE_OCTET_STREAM} - the {@link #TYPE_APPLICATION} is assumed.</li>
	 * <li>If the {@link #EXTRA_FILE_MIME_TYPE} field is equal to {@value #MIME_TYPE_ZIP} - the {@link #TYPE_AUTO} is assumed.</li>
	 * </ol>
	 */
	public static final String EXTRA_FILE_TYPE = "com.petkit.android.extra.EXTRA_FILE_TYPE";
	
	

	public static final String EXTRA_DATA = "com.petkit.android.extra.EXTRA_DATA";
	
	public static final String EXTRA_PROGRESS = "com.petkit.android.extra.EXTRA_PROGRESS";
	
	
	public static final String EXTRA_DOG = "com.petkit.android.extra.EXTRA_DOG";
	
	public static final String EXTRA_URL_DATA_SAVE = "com.petkit.android.extra.EXTRA_URL_DATA_SAVE";
	
	public static final String EXTRA_URL_DAILY_DETAIL = "com.petkit.android.extra.EXTRA_URL_DAILY_DETAIL";

	public static final String EXTRA_BOOLEAN_STORE_PROGRESS = "com.petkit.android.extra.EXTRA_BOOLEAN_STORE_PROGRESS";

	public static final String EXTRA_BOOLEAN_LOGOUT = "com.petkit.android.extra.EXTRA_BOOLEAN_LOGOUT";

	public static final String EXTRA_DEVICE_RECONNECT_TIMES = "com.petkit.android.extra.EXTRA_DEVICE_RECONNECT_TIMES";
	
	/**
	 * The broadcast error message contains the following extras:
	 * <ul>
	 * <li>{@link #EXTRA_DATA} - the error number. Use {@link GattError#parse(int)} to get String representation</li>
	 * <li>{@link #EXTRA_DEVICE_ADDRESS} - the target device address</li>
	 * </ul>
	 */
	public static final String BROADCAST_ERROR = "com.petkit.android.dfu.broadcast.BROADCAST_ERROR";
	/**
	 * <p>
	 * The file contains a new version of Soft Device.
	 * </p>
	 * <p>
	 * Since DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader. The Init packet for the Soft Device must be
	 * placed in the [firmware name].dat file as binary file (in the same location).
	 * </p>
	 * 
	 * @see #EXTRA_FILE_TYPE
	 */
	public static final int TYPE_SOFT_DEVICE = 0x01;
	/**
	 * <p>
	 * The file contains a new version of Bootloader.
	 * </p>
	 * <p>
	 * Since DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader. The Init packet for the bootloader must be
	 * placed in the [firmware name].dat file as binary file (in the same location).
	 * </p>
	 * 
	 * @see #EXTRA_FILE_TYPE
	 */
	public static final int TYPE_BOOTLOADER = 0x02;
	/**
	 * <p>
	 * The file contains a new version of Application.
	 * </p>
	 * <p>
	 * Since DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader. The Init packet for the application must be
	 * placed in the [firmware name].dat file as binary file (in the same location).
	 * </p>
	 * 
	 * @see #EXTRA_FILE_TYPE
	 */
	public static final int TYPE_APPLICATION = 0x04;
	/**
	 * <p>
	 * A ZIP file that combines more than 1 HEX file. The names of files in the ZIP must be: <b>softdevice.hex</b> (or .bin), <b>bootloader.hex</b> (or .bin), <b>application.hex</b> (or .bin) in order
	 * to be read correctly. In the DFU version 2 the Soft Device and Bootloader may be sent together. In case of additional application file included, the service will try to send Soft Device,
	 * Bootloader and Application together (not supported by DFU v.2) and if it fails, send first SD+BL, reconnect and send application.
	 * </p>
	 * <p>
	 * Since the DFU Library 7.0 all firmware may contain an Init packet. The Init packet is required if Extended Init Packet is used by the DFU bootloader. The Init packet for the Soft Device and
	 * Bootloader must be in 'system.dat' file while for the application in the 'application.dat' file (included in the ZIP). The CRC in the 'system.dat' must be a CRC of both BIN contents if both a
	 * Soft Device and a Bootloader is present.
	 * </p>
	 * 
	 * @see #EXTRA_FILE_TYPE
	 */
	public static final int TYPE_AUTO = 0x00;
	
	// Since the DFU Library version 7.0 both HEX and BIN files are supported. As both files have the same MIME TYPE the distinction is made based on the file extension. 
	public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
	public static final String MIME_TYPE_ZIP = "application/zip";
		
	/**
	 * The number of currently transferred part. The SoftDevice and Bootloader may be send together as one part. If user wants to upload them together with an application it has to be sent
	 * in another connection as the second part.
	 * 
	 */
	public static final String EXTRA_PART_CURRENT = "no.nordicsemi.android.dfu.extra.EXTRA_PART_CURRENT";
	/**
	 * Number of parts in total.
	 * 
	 */
	public static final String EXTRA_PARTS_TOTAL = "no.nordicsemi.android.dfu.extra.EXTRA_PARTS_TOTAL";
	/** The current upload speed in bytes/millisecond. */
	public static final String EXTRA_SPEED_B_PER_MS = "no.nordicsemi.android.dfu.extra.EXTRA_SPEED_B_PER_MS";
	/** The average upload speed in bytes/millisecond for the current part. */
	public static final String EXTRA_AVG_SPEED_B_PER_MS = "no.nordicsemi.android.dfu.extra.EXTRA_AVG_SPEED_B_PER_MS";
		
	
	public static final String BROADCAST_LOG = "com.petkit.android.broadcast.BROADCAST_LOG";
	public static final String EXTRA_LOG_MESSAGE = "com.petkit.android.extra.EXTRA_LOG_INFO";
	public static final String EXTRA_LOG_LEVEL = "com.petkit.android.extra.EXTRA_LOG_LEVEL";

	/** Activity may broadcast this broadcast in order to pause, resume or abort BLE process. */
	public static final String BROADCAST_ACTION = "com.petkit.android.broadcast.BROADCAST_ACTION";
	public static final String EXTRA_ACTION = "com.petkit.android.extra.EXTRA_ACTION";
	
	public static final String BROADCAST_SCANED_DEVICE  = "com.petkit.android.broadcast.BROADCAST_SCANED_DEVICE";
	public static final String EXTRA_DEVICE_INFO	 = "com.petkit.android.extra.EXTRA_DEVICE_INFO";
	
	///////////////
	public static final String BROADCAST_SCANED_WIFI  = "com.petkit.android.broadcast.BROADCAST_SCANED_WIFI";
	public static final String BROADCAST_SCANED_WIFI_COMPLETED  = "com.petkit.android.broadcast.BROADCAST_SCANED_WIFI_COMPLETED";
	public static final String EXTRA_WIFI_INFO	 = "com.petkit.android.extra.EXTRA_WIFI_INFO";
	////////////////
    public static final String EXTRA_MATE_VERSION	 = "com.petkit.android.extra.EXTRA_MATE_VERSION";

	/** An extra private field indicating which attempt is being performed. In case of error 133 the service will retry to connect one more time. */
	public static final String EXTRA_ATTEMPT = "no.nordicsemi.android.dfu.extra.EXTRA_ATTEMPT";

	/**
	 * <p>This flag indicated whether the bond information should be kept or removed after an upgrade of the Application.
	 * If an application is being updated on a bonded device with the DFU Bootloader that has been configured to preserve the bond information for the new application,
	 * set it to <code>true</code>.</p>
	 *
	 * <p>By default the DFU Bootloader clears the whole application's memory. It may be however configured in the \Nordic\nrf51\components\libraries\bootloader_dfu\dfu_types.h
	 * file (line 56: <code>#define DFU_APP_DATA_RESERVED 0x0000</code>) to preserve some pages. The BLE_APP_HRM_DFU sample app stores the LTK and System Attributes in the first
	 * two pages, so in order to preserve the bond information this value should be changed to 0x0800 or more.
	 * When those data are preserved, the new Application will notify the app with the Service Changed indication when launched for the first time. Otherwise this
	 * service will remove the bond information from the phone and force to refresh the device cache (see {@link #(android.bluetooth.BluetoothGatt, boolean)}).</p>
	 *
	 * <p>In contrast to {@link #EXTRA_RESTORE_BOND} this flag will not remove the old bonding and recreate a new one, but will keep the bond information untouched.</p>
	 * <p>The default value of this flag is <code>false</code></p>
	 */
	public static final String EXTRA_KEEP_BOND = "no.nordicsemi.android.dfu.extra.EXTRA_KEEP_BOND";

	/**
	 * See {@link #EXTRA_FILE_PATH} for details.
	 */
	public static final String EXTRA_FILE_RES_ID = "no.nordicsemi.android.dfu.extra.EXTRA_FILE_RES_ID";
	/**
	 * The Init packet URI. This file is required if the Extended Init Packet is required (SDK 7.0+). Must point to a 'dat' file corresponding with the selected firmware.
	 * The Init packet may contain just the CRC (in case of older versions of DFU) or the Extended Init Packet in binary format (SDK 7.0+).
	 */
	public static final String EXTRA_INIT_FILE_RES_ID = "no.nordicsemi.android.dfu.extra.EXTRA_INIT_FILE_RES_ID";
	/**
	 * This property must contain a boolean value.
	 * <p>The {@link }, when connected to a DFU target will check whether it is in application or in DFU bootloader mode. For DFU implementations from SDK 7.0 or newer
	 * this is done by reading the value of DFU Version characteristic. If the returned value is equal to 0x0100 (major = 0, minor = 1) it means that we are in the application mode and
	 * jump to the bootloader mode is required.
	 * <p>However, for DFU implementations from older SDKs, where there was no DFU Version characteristic, the service must guess. If this option is set to false (default) it will count
	 * number of device's services. If the count is equal to 3 (Generic Access, Generic Attribute, DFU Service) it will assume that it's in DFU mode. If greater than 3 - in app mode.
	 * This guessing may not be always correct. One situation may be when the nRF chip is used to flash update on external MCU using DFU. The DFU procedure may be implemented in the
	 * application, which may (and usually does) have more services. In such case set the value of this property to true.
	 */
	public static final String SETTINGS_ASSUME_DFU_NODE = "settings_assume_dfu_mode";


	public final static int STATE_DISCONNECTED = 0;
	public final static int STATE_CONNECTING = -1;
	public final static int STATE_CONNECTED = -2;
	public final static int STATE_CONNECTED_AND_READY = -3; // indicates that services were discovered
	public final static int STATE_DISCONNECTING = -4;
	public final static int STATE_CLOSED = -5;
	public final static int STATE_SYNC_INIT_SUCCESS = -6;
	public final static int STATE_SYNC_COMPLETE = -7;
	public final static int STATE_SCANING = -8;
	public final static int STATE_SCANED = -9;
	public final static int STATE_PREPARE = -10;		//samsung ble, registe application suuccess
	
	public static final String BROADCAST_PROGRESS = "com.petkit.android.broadcast.BROADCAST_PROGRESS";
	/** Service is connecting to the remote DFU target. */
	public static final int PROGRESS_CONNECTING = -1;
	/** Service is enabling notifications and starting transmission. */
	public static final int PROGRESS_STARTING = -2;
	
	/**
	 * Service has triggered a switch to bootloader mode. It waits for the link loss (this may take up to several seconds) and will:
	 * <ul>
	 * <li>Connect back to the same device, now started in the bootloader mode, if the device is bonded.</li>
	 * <li>Scan for a device with the same address (DFU Version 5) or a device with incremented the last byte of the device address (DFU Version 6+) if the device is not bonded.</li>
	 * </ul>
	 */
	public static final int PROGRESS_ENABLING_DFU_MODE 			= -3;
	/** Service is sending validation request to the remote DFU target. */
	public static final int PROGRESS_VALIDATING 				= -4;
	/** Service is disconnecting from the DFU target. */
	public static final int PROGRESS_DISCONNECTING 				= -5;
	/** The connection is successful. */
	public static final int PROGRESS_BLE_COMPLETED				= -6;
	
	public static final int PROGRESS_SCANING					= -8;

	/**
	 * attention: broadcast PROGRESS_VERIFY with json string (which is DeviceInfor) in extra key EXTRA_DATA
	 */
	public static final int PROGRESS_VERIFY						= -9;

	public static final int PROGRESS_SYNC_TIME					= -10;
	
	/**
	 * attention: broadcast this PROGRESS_SYNC_BATTERY with a battery string in extra key EXTRA_DATA
	 */
	public static final int PROGRESS_SYNC_BATTERY				= -11;

	public static final int PROGRESS_SYNC_DEBUG_INFOR			= -12;

	public static final int PROGRESS_SYNC_DATA					= -13;

	public static final int PROGRESS_SCANING_FAILED				= -14;

	public static final int PROGRESS_SCANING_TIMEOUT			= -15;

	public static final int PROGRESS_CONNECTED = -16;

	public static final int PROGRESS_SCANED_TARGET = -17;

	/** The connection is successful. */
	public static final int PROGRESS_SYNC_DATA_COMPLETED 		= -18;
	
	public static final int PROGRESS_BLE_NOT_SUPPORT 		= -19;

	public static final int PROGRESS_OTA_START 		= -20;

	public static final int PROGRESS_DATA_SAVED 		= -21;

	public static final int PROGRESS_BLE_START 		= -22;
	
	public static final int PROGRESS_UPLOAD_ACTIVITY_DATA 		= -23;

	public static final int PROGRESS_DOWNLOAD_DAILY_DETAIL		= -24;

	public static final int PROGRESS_NETWORK_COMPLETED			= -25;
	
	public static final int PROGRESS_WIFI_SET_RESULT 		= -26;

	public static final int PROGRESS_WIFI_SET_START 		= -27;

	public static final int PROGRESS_MATE_SERVER_SET_COMPLETE 		= -28;

    public static final int PROGRESS_MATE_WIFI_MAC_COMPLETE 		= -29;
	/** STEP response data. */
	public static final int PROGRESS_STEP_DATA				= -30;


	/**
	 * If this bit is set than the progress value indicates an error. Use {@link GattError#parse(int)} to obtain error name.
	 */
	public static final int ERROR_MASK = 0x1000; // When user tries to connect to more than 6 devices on Nexus devices (Android 4.4.4) the 0x101 error is thrown. Mask changed 0x0100 -> 0x1000 to avoid collision. 
	public static final int ERROR_DEVICE_DISCONNECTED = ERROR_MASK | 0x00;

	/** The upload has been aborted. Previous software version will be restored on the target. */
	public static final int ERROR_ABORTED 					= ERROR_MASK | 0x01;
	
	public static final int ERROR_FILE_NOT_FOUND = ERROR_MASK | 0x02;
	/** Thrown if service was unable to open the file ({@link IOException} has been thrown). */
	public static final int ERROR_FILE_ERROR = ERROR_MASK | 0x03;
	/** Thrown then input file is not a valid HEX or ZIP file. */
	public static final int ERROR_FILE_INVALID = ERROR_MASK | 0x04;
	/** Thrown when {@link IOException} occurred when reading from file. */
	public static final int ERROR_FILE_IO_EXCEPTION = ERROR_MASK | 0x05;
	/** Error thrown then {@code gatt.discoverServices();} returns false. */
	public static final int ERROR_SERVICE_DISCOVERY_NOT_STARTED = ERROR_MASK | 0x06;
	/** Thrown when the service discovery has finished but the DFU service has not been found. The device does not support DFU of is not in DFU mode. */
	public static final int ERROR_SERVICE_NOT_FOUND = ERROR_MASK | 0x07;
	/** Thrown when the required DFU service has been found but at least one of the DFU characteristics is absent. */
	public static final int ERROR_CHARACTERISTICS_NOT_FOUND = ERROR_MASK | 0x08;
	/** Thrown when unknown response has been obtained from the target. The DFU target must follow specification. */
	public static final int ERROR_INVALID_RESPONSE = ERROR_MASK | 0x09;
	/** Thrown when the the service does not support given type or mime-type. */
	public static final int ERROR_FILE_TYPE_UNSUPPORTED = ERROR_MASK | 0x0a;
	/** Thrown when unknown response has been obtained from the target. The DFU target must follow specification. */
	public static final int ERROR_INVALID_PARAMETERS = ERROR_MASK | 0x0b;
	/** Flag set then the DFU target returned a DFU error. Look for DFU specification to get error codes. */
	public static final int ERROR_REMOTE_MASK = 0x2000;
	/** The flag set when one of {@link BluetoothGattCallback} methods was called with status other than {@link BluetoothGatt#GATT_SUCCESS}. */
	public static final int ERROR_CONNECTION_MASK = 0x4000;

	/**
	 * The flag set when the {@link android.bluetooth.BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} method was called with
	 * status other than {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
	 */
	public static final int ERROR_CONNECTION_STATE_MASK = 0x8000;
	/**
	 * Thrown when the the Bluetooth adapter is disabled.
	 */
	public static final int ERROR_BLUETOOTH_DISABLED = ERROR_MASK | 0x0c;
	/**
	 * DFU Bootloader version 0.6+ requires sending the Init packet. If such bootloader version is detected, but the init packet has not been set this error is thrown.
	 */
	public static final int ERROR_INIT_PACKET_REQUIRED = ERROR_MASK | 0x0d;
	/**
	 * Thrown when the firmware file is not word-aligned. The firmware size must be dividable by 4 bytes.
	 */
	public static final int ERROR_FILE_SIZE_INVALID = ERROR_MASK | 0x0e;


	public static final int ERROR_SYNC_TIMEOUT = ERROR_MASK | 0x0f;

	public static final int ERROR_PREPARE_FAILED = ERROR_MASK | 0x11;

	public static final int ERROR_NETWORK_FAILED = ERROR_MASK | 0x12;

	public static final int ERROR_UNSUPPORTED_ENCODING = ERROR_MASK | 0x13;

	public static final UUID GENERIC_ATTRIBUTE_SERVICE_UUID = new UUID(0x0000180100001000l, 0x800000805F9B34FBl);
	public static final UUID SERVICE_CHANGED_UUID = new UUID(0x00002A0500001000l, 0x800000805F9B34FBl);


	/**
	 * The upload has been aborted. Previous software version will be restored on the target.
	 */
	public static final int PROGRESS_ABORTED = ERROR_ABORTED;
	
	/*--------------------------------------DFU----------------------------------------*/
	
	public static final UUID DFU_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");//new UUID(0x000015301212EFDEl, 0x1523785FEABCD123l);
	public static final UUID DFU_CONTROL_POINT_UUID = UUID.fromString("f000ffc1-0451-4000-b000-000000000000");//new UUID(0x000015311212EFDEl, 0x1523785FEABCD123l);
	public static final UUID DFU_PACKET_UUID = UUID.fromString("f000ffc2-0451-4000-b000-000000000000");//new UUID(0x000015321212EFDEl, 0x1523785FEABCD123l);
	public static final UUID DFU_VERSION = new UUID(0x000015341212EFDEl, 0x1523785FEABCD123l);
	
	public static final UUID CLIENT_CHARACTERISTIC_CONFIG = new UUID(0x0000290200001000l, 0x800000805f9b34fbl);
	
	
	public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	
	public final static int MAX_PACKET_SIZE = 20;
	
	
	public static final int DFU_STATUS_SUCCESS = 1;
	public static final int DFU_STATUS_INVALID_STATE = 2;
	public static final int DFU_STATUS_NOT_SUPPORTED = 3;
	public static final int DFU_STATUS_DATA_SIZE_EXCEEDS_LIMIT = 4;
	public static final int DFU_STATUS_CRC_ERROR = 5;
	public static final int DFU_STATUS_OPERATION_FAILED = 6;
	
	public static final int OP_CODE_START_DFU_KEY = 0x01; // 1
	public static final int OP_CODE_INIT_DFU_PARAMS_KEY = 0x02; // 2
	public static final int OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY = 0x03; // 3
	public static final int OP_CODE_VALIDATE_KEY = 0x04; // 4
	public static final int OP_CODE_ACTIVATE_AND_RESET_KEY = 0x05; // 5
	public static final int OP_CODE_RESET_KEY = 0x06; // 6
	public static final int OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY = 0x08; // 8
	public static final int OP_CODE_RESPONSE_CODE_KEY = 0x10; // 16
	public static final int OP_CODE_PACKET_RECEIPT_NOTIF_KEY = 0x11; // 11
	
	public static final byte[] OP_CODE_START_DFU = new byte[] { OP_CODE_START_DFU_KEY, 0x00 };
	public static final byte[] OP_CODE_INIT_DFU_PARAMS_START = new byte[] { OP_CODE_INIT_DFU_PARAMS_KEY, 0x00 };
	public static final byte[] OP_CODE_INIT_DFU_PARAMS_COMPLETE = new byte[] { OP_CODE_INIT_DFU_PARAMS_KEY, 0x01 };
	public static final byte[] OP_CODE_RECEIVE_FIRMWARE_IMAGE = new byte[] { OP_CODE_RECEIVE_FIRMWARE_IMAGE_KEY };
	public static final byte[] OP_CODE_VALIDATE = new byte[] { OP_CODE_VALIDATE_KEY };
	public static final byte[] OP_CODE_ACTIVATE_AND_RESET = new byte[] { OP_CODE_ACTIVATE_AND_RESET_KEY };
	public static final byte[] OP_CODE_RESET = new byte[] { OP_CODE_RESET_KEY };
	public static final byte[] OP_CODE_PACKET_RECEIPT_NOTIF_REQ = new byte[] { OP_CODE_PACKET_RECEIPT_NOTIF_REQ_KEY, 0x00, 0x00 };
	

	public static final String SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED = "settings_packet_receipt_notification_enabled";
	public static final String SETTINGS_NUMBER_OF_PACKETS = "settings_number_of_packets";
	public static final String SETTINGS_MBR_SIZE = "settings_mbr_size";
	public static final int SETTINGS_DEFAULT_MBR_SIZE = 0x1000;
	public static final int SETTINGS_NUMBER_OF_PACKETS_DEFAULT = 10;
	
	
	/*--------------------------------------SYNC----------------------------------------*/

	public final static String BASE_TIMELINE = "2000-01-01 00:00:00";
	public final static String BASE_TIMELINE_NEW = "2000-01-01T00:00:00.000+0000";
	
	

	
	public static final UUID ACC_SERVICE_UUID = UUID.fromString("0000aaa0-0000-1000-8000-00805f9b34fb");
	public static final UUID ACC_DATA_UUID = UUID.fromString("0000aaa1-0000-1000-8000-00805f9b34fb");
	public static final UUID ACC_CONTROL_UUID = UUID.fromString("0000aaa2-0000-1000-8000-00805f9b34fb");
	
	public static final UUID BAT_SERV_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"); 
	public static final UUID BAT_DATA_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

	public static final char DATA_SPIT = ',';

	public static int MAX_BLOCK_SIZE = 32;
	public static int MAX_DATA_CONTROL_WRITE_SIZE = 13;
	
	public static final int OP_CODE_DEVICE_INIT_KEY					= 'I';
	public static final int OP_CODE_VERIFY_KEY						= 'V';
	public static final int OP_CODE_TIME_SYNC_KEY					= 'T';
	public static final int OP_CODE_DEBUG_INFOR_KEY					= 'M';
	public static final int OP_CODE_DEBUG_INFOR_2_KEY				= 'G';
	public static final int OP_CODE_DATA_READ_KEY					= 'D';
	public static final int OP_CODE_DATA_CONFIRM_KEY				= 'C';
	public static final int OP_CODE_DATA_COMPLETE_KEY				= 'E';
	public static final int OP_CODE_DEVICE_DOWNLOAD_KEY				= 'S';
	public static final int OP_CODE_TRUN_OFF_SENSOR_KEY				= 'H';
	public static final int OP_CODE_START_RESET_DEBUG_INFOR_KEY		= 'W';
	public static final int OP_CODE_BATTERY_KEY						= 'B';
	public static final int OP_CODE_AQ_TEST_ENTRY					= 240;

	/** P3 cmd **/
	public static final int OP_CODE_GET_INFO						= 213;
	public static final int OP_CODE_P3_TEST_ENTRY					= 240;
	public static final int OP_CODE_P3_SENSOR_DATA					= 241;
	public static final int OP_CODE_P3_TEST_RESULT					= 242;
	public static final int OP_CODE_P3_WRITE_SN						= 243;
	public static final int OP_CODE_P3_RING							= 220;

	/** W5 cmd **/
	public static final int OP_CODE_W5_TEST_START 					= 240;
	public static final int OP_CODE_W5_PUMP_DATA					= 241;
	public static final int OP_CODE_W5_TEST_STEP					= 242;
	public static final int OP_CODE_W5_TEST_RESULT					= 243;
	public static final int OP_CODE_W5_WRITE_SN						= 244;

	/** K3 cmd **/
	public static final int OP_CODE_K3_TEST_START					= 240;
	public static final int OP_CODE_K3_TEST_STEP					= 242;
	public static final int OP_CODE_K3_TEST_RESULT					= 243;
	public static final int OP_CODE_K3_WRITE_SN						= 244;

	/** AQR cmd **/
	public static final int OP_CODE_AQR_TEST_START					= 240;
	public static final int OP_CODE_AQR_TEST_STEP					= 242;
	public static final int OP_CODE_AQR_TEST_RESULT					= 243;
	public static final int OP_CODE_AQR_WRITE_SN						= 244;


	public static final int ERROR_SYNC_MASK = 0x8000;
	public static final int ERROR_SYNC_INIT_FAIL	= ERROR_SYNC_MASK | 0x01;
	public static final int ERROR_SYNC_VERIFY_FAIL = 	ERROR_SYNC_MASK | 0x02;
	
	
	public static final int ERROR_DEVICE_ID_NULL = 		ERROR_SYNC_MASK | 0xa9;

	/*--------------------------------------HS WIFI ERROR CODE----------------------------------------*/
	public static final int WIFI_CFG_OK                = 0;    //wifi配置正常
	public static final int WIFI_GETIP_OK             = 1;    //wifi获取IP正常
	public static final int WIFI_NETDNS_OK         = 2;    //网络DNS链接正常
	public static final int ERR_WIFI_NOSERVER   = 3;    //wifi配置服务不存在
	public static final int ERR_WIFI_PINGDNS      = 4;    //网络DNS链接异常
	public static final int ERR_WIFI_CONNWPA    = 5;    //wifi wpa 服务连接失败
	public static final int ERR_WIFI_OPENCONF   = 6;   //wifi cli 连接失败
	public static final int ERR_WIFI_SCANAP        = 7;   //扫描wifi ap热点失败
	public static final int ERR_WIFI_ADDAPID      = 8;   //添加wifi连接新节点失败
	public static final int ERR_WIFI_ADDAPSSID  = 9;    //添加新节点的ssid失败
	public static final int ERR_WIFI_NULLSSID    = 10;  //新节点ssid为空，失败
	public static final int ERR_WIFI_ADDAPPSK    = 11;  //添加新节点的密码失败
	public static final int ERR_WIFI_ADDAPBSSID = 12;  //添加新节点的bssid失败
	public static final int ERR_WIFI_SELECT        = 13;   //启用新节点失败
	public static final int ERR_WIFI_ERRPSK       = 14;   //新节点的密码错误
	public static final int ERR_WIFI_APTIMEOUT = 15;    //链接新节点超时
	public static final int ERR_WIFI_FILESAVE     = 16;    //保存新节点失败
	public static final int ERR_WIFI_GETIP          = 17;    //获取新节点IP失败
	public static final int ERR_WIFI_SETTING        = 18;    //设置wifi忙
	public static final int ERR_WIFI_LISTTIMEOUT    = 19; //读取wifi超时
	public static final int ERR_WIFI_CLEAN           = 20;  //清楚wifi列表错误
	public static final int WIFI_DO_CONNECTING      = 21;
	public static final int WIFI_DO_IDLE            = 22;
	public static final int WIFI_DO_CLEAN           = 23;
//	public static final int ST_WIFI_MAX              = 24;  //默认-不定义说明


	public static final int MATE_OP_CODE_REQUEST_KEY			= 'X';
	public static final int MATE_OP_CODE_CONFIRM_KEY			= 'Y';
	public static final int MATE_OP_CODE_COMPLETE_KEY			= 'Z';

	public static final int MATE_COMMAND_GET_WIFI_PAIRED 	= 304;
	public static final int MATE_COMMAND_GET_WIFI 			= 305;
	public static final int MATE_COMMAND_GET_SN 			= 324;
	public static final int MATE_COMMAND_WRITE_WIFI 		= 301;
	public static final int MATE_COMMAND_WRITE_WIFI_CONFIRM 		= 302;
	public static final int MATE_COMMAND_WRITE_SERVER 		= 622;
    public static final int MATE_COMMAND_WRITE_ALIVE 		= 624;

    public static final String MATE_BASE_VERSION_FOR_ALIVE_CMD        = "1.1550.1";
    public static final String MATE_BASE_VERSION_FOR_GET_WIFI        = "1.1550.1";

    public static String convertErrorCode(int code){
        switch (code){
            case ERROR_ABORTED:
                return "abort";
            case ERROR_CHARACTERISTICS_NOT_FOUND:
                return "characteristics not found";
            case ERROR_DEVICE_DISCONNECTED:
                return "device disconnected";
            case ERROR_DEVICE_ID_NULL:
                return "device id is null";
            case ERROR_FILE_ERROR:
                return "file error";
            case ERROR_FILE_INVALID:
                return "file invalid";
            case ERROR_FILE_NOT_FOUND:
                return "file not found";
            case ERROR_FILE_IO_EXCEPTION:
                return "file io exception";
            case ERROR_FILE_TYPE_UNSUPPORTED:
                return "file type unsupported";
            case ERROR_INVALID_PARAMETERS:
                return "invalid parameters";
            case ERROR_INVALID_RESPONSE:
                return "invalid response";
            case ERROR_NETWORK_FAILED:
                return "network failed";
            case ERROR_SERVICE_DISCOVERY_NOT_STARTED:
                return "service discovery not started";
            case ERROR_PREPARE_FAILED:
                return "prepare failed";
            case ERROR_SYNC_TIMEOUT:
                return "sync timeout";
            case ERROR_SYNC_INIT_FAIL:
                return "sync init failed";
            case ERROR_SERVICE_NOT_FOUND:
                return "service not found";
            case ERROR_SYNC_VERIFY_FAIL:
                return "sync verify failed";
            case ERROR_UNSUPPORTED_ENCODING:
                return "unsupported encoding";
        }

        if(BLEConsts.ERROR_CONNECTION_MASK < code){
            return String.valueOf(code - BLEConsts.ERROR_CONNECTION_MASK);
        }
        return String.valueOf(code);
    }



	/**
	 * 版本比较，根据.分割version，然后对每位进行比较
	 *
	 * 版本的格式为：xxx.xxx.xxx
	 *
	 * @param curVersion  当前版本
	 * @param baseVersion  基础版本
	 * @return  返回-1 0 1
	 */
	public static int compareMateVersion(String curVersion, String baseVersion){
		PetkitLog.d("compareMateVersion current version: " + curVersion + " base version: " + baseVersion);
		LogcatStorageHelper.addLog("compareMateVersion current version: " + curVersion + " base version: " + baseVersion);
		if(TextUtils.isEmpty(curVersion)){
			return -1;
		}

		String[] split = curVersion.split("\\.");
		if(split.length != 3){
			return -1;
		}

		String[] splitBase = baseVersion.split("\\.");

		try {
			int split1 = Integer.valueOf(split[0]);
			int split2 = Integer.valueOf(split[1]);
			int split3 = Integer.valueOf(split[2]);

			int splitBase1 = Integer.valueOf(splitBase[0]);
			int splitBase2 = Integer.valueOf(splitBase[1]);
			int splitBase3 = Integer.valueOf(splitBase[2]);

			if(split1 == splitBase1 && split2 == splitBase2 && split3 == splitBase3){
				return 0;
			} else if(split1 >= splitBase1){
				if(split1 == splitBase1) {
					if(split2 >= splitBase2) {
						if (split2 == splitBase2) {
							if (split3 >= splitBase3) {
								return 1;
							}
						} else {
							return 1;
						}
					}
				} else {
					return 1;
				}
			}
		} catch (NumberFormatException e) {
			return -1;
		}

		return -1;
	}


	/**
	 * 获取写入设备中的时间，基于BLEConsts.BASE_TIMELINE
	 *
	 * @return
	 */
	public static int getSeconds() {
		long quot;
		int seconds = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date1 = ft.parse(BLEConsts.BASE_TIMELINE);
			Date date2 = ft.parse(ft.format(new Date()));
			quot = date2.getTime() - date1.getTime();
			seconds = (int) (quot / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return seconds;
	}

	/**
	 *
	 * @return
	 */
	public static int getSecondsWithoutTimeZone() {
		long quot = 0;
		int seconds = 0;
		SimpleDateFormat ft = new SimpleDateFormat(DateUtil.ISO8601DATE_WITH_ZONE_MILLS_FORMAT);
		try {
			Date date1 = ft.parse(BASE_TIMELINE_NEW);
			Date date2 = ft.parse(ft.format(new Date()));
			quot = date2.getTime() - date1.getTime();
			seconds = (int) (quot / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		LogcatStorageHelper.addLog("write device time ： " + seconds);
		PetkitLog.d("write device time ： " + seconds);
		return seconds;
	}


}
