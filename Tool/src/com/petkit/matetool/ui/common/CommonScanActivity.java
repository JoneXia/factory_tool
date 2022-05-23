package com.petkit.matetool.ui.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.petkit.android.utils.LogcatStorageHelper;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.ui.utils.zxing.ScanListener;
import com.petkit.matetool.ui.utils.zxing.ScanManager;
import com.petkit.matetool.ui.utils.zxing.decode.DecodeThread;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 扫描SN条码
 *
 * Created by Jone on 17/6/2.
 */
public class CommonScanActivity extends BaseActivity implements ScanListener {

    private Tester mTester;

    SurfaceView scanPreview = null;
    View scanContainer;
    View scanCropView;
    ImageView scanLine;
    ScanManager scanManager;
    TextView iv_light;
    TextView qrcode_g_gallery;
    TextView qrcode_ic_back;

    Button rescan;
    ImageView scan_image;
    private int mDeviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
            mTester = (Tester) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
            mTester = (Tester) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_feeder_scan);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
    }

    @Override
    protected void setupViews() {

        setTitle("扫码");

        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = findViewById(R.id.capture_container);
        scanCropView = findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        qrcode_g_gallery = (TextView) findViewById(R.id.qrcode_g_gallery);
        qrcode_g_gallery.setOnClickListener(this);
        qrcode_ic_back = (TextView) findViewById(R.id.qrcode_ic_back);
        qrcode_ic_back.setOnClickListener(this);
        iv_light = (TextView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);

        rescan = (Button) findViewById(R.id.service_register_rescan);
        rescan.setOnClickListener(this);

        scan_image = (ImageView)findViewById(R.id.scan_image);
//        tv_scan_result = (TextView) findViewById(R.id.tv_scan_result);

        //构造出扫描管理器
        scanManager = new ScanManager(this, scanPreview, scanContainer, scanCropView, scanLine, 0x300, this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qrcode_g_gallery:
//                showPictures(PHOTOREQUESTCODE);
                break;
            case R.id.iv_light:
                scanManager.switchLight();
                break;
            case R.id.qrcode_ic_back:
                finish();
                break;
            case R.id.service_register_rescan://再次开启扫描
                startScan();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        scanManager.onResume();
        rescan.setVisibility(View.INVISIBLE);
        scan_image.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanManager.onPause();
    }

    void startScan() {
        if (rescan.getVisibility() == View.VISIBLE) {
            rescan.setVisibility(View.INVISIBLE);
            scan_image.setVisibility(View.GONE);
            scanManager.reScan();
        }
    }

    @Override
    public void scanResult(Result rawResult, Bundle bundle) {
        if (!scanManager.isScanning()) { //如果当前不是在扫描状态
            //设置再次扫描按钮出现
            rescan.setVisibility(View.VISIBLE);
            scan_image.setVisibility(View.VISIBLE);
            Bitmap barcode = null;
            byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
            }
            scan_image.setImageBitmap(barcode);
        }
        rescan.setVisibility(View.VISIBLE);
        scan_image.setVisibility(View.VISIBLE);

        PetkitLog.d("scan result: " + rawResult.getText());
        checkContent(rawResult.getText());
//        tv_scan_result.setVisibility(View.VISIBLE);
//        tv_scan_result.setText("结果："+rawResult.getText());
    }

    @Override
    public void scanError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        //相机扫描出错时
        if(e.getMessage()!=null&&e.getMessage().startsWith("相机")){
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    private void checkContent(String data) {

        if(data == null || data.contains(" ") || data.length() != 14) {
            showShortToast("无效的内容！");
            return;
        }
        String sn = "";

        if(data.contains("SN:")) {
            try {
                JSONObject result = JSONUtils.getJSONObject(data);
                if (result != null && !result.isNull("SN")) {
                    sn = result.getString("SN");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showShortToast("非法的SN！");
            }
        } else if (data.length() <= 16){
            sn = data;
        }

        if(isEmpty(sn)) {
            showShortToast("无效的SN");
            return;
        }

        if(DeviceCommonUtils.checkSN(sn, mDeviceType)) {
            LogcatStorageHelper.addLog("Scaned sn: " + sn);
            showConfirmDialog(sn);
        } else {
            showShortToast("SN规则不匹配，请检查！");
        }

    }

    private void showConfirmDialog(final String sn) {

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.Prompt)
                .setMessage("请确认SN: " + sn)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra(DeviceCommonUtils.EXTRA_DEVICE_SN, sn.toUpperCase());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }

}
