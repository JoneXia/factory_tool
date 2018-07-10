package com.petkit.matetool.ui.feederMini;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.http.AsyncHttpRespHandler;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.feeder.mode.FeederTester;
import com.petkit.matetool.ui.feeder.zxing.ScanListener;
import com.petkit.matetool.ui.feeder.zxing.ScanManager;
import com.petkit.matetool.ui.feeder.zxing.decode.DecodeThread;
import com.petkit.matetool.ui.feederMini.utils.FeederMiniUtils;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * 扫描入库
 *
 * Created by Jone on 17/6/2.
 */
public class FeederMiniScanActivity extends BaseActivity implements ScanListener {

    private FeederTester mTester;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mTester = (FeederTester) savedInstanceState.getSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        } else {
            mTester = (FeederTester) getIntent().getSerializableExtra(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_feeder_scan);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FeederMiniUtils.EXTRA_FEEDER_MINI_TESTER, mTester);
    }

    @Override
    protected void setupViews() {

        setTitle("入库");

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
        uploadSn(rawResult.getText());
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

    private void uploadSn(String data) {

        if(data == null) {
            showShortToast("无效的内容！");
            return;
        }
        String sn = "", mac = "";
        if(!data.startsWith("SN:")) {
            try {
                JSONObject result = JSONUtils.getJSONObject(data);
                if (result != null && !result.isNull("MAC")) {
                    mac = result.getString("MAC");
                }
                if (result != null && !result.isNull("SN")) {
                    sn = result.getString("SN");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            sn = data.substring(3);
        }

        if(isEmpty(sn)) {
            showShortToast("无效的SN");
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("snList", String.format("{\"snList\":[{\"sn\":\"%s\",\"mac\":\"%s\"}]}", sn, mac));

        AsyncHttpUtil.post("/api/feedermini/storage/stock", params, new AsyncHttpRespHandler(this, true) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                if (statusCode == 200) {
                    try {
                        JSONObject result = JSONUtils.getJSONObject(responseResult);
                        if (!result.isNull("code")) {
                            int code = result.getInt("code");
                            switch (code) {
                                case 0:
                                    uploadSnSuccessful();
                                    break;
                                default:
                                    uploadSnFailed(result.getString("message"));
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        uploadSnFailed("解析数据错误");
                    }
                } else {
                    uploadSnFailed("网络请求结果： " + statusCode);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                uploadSnFailed("网络请求结果： " + statusCode);
            }
        });
    }

    private void uploadSnSuccessful() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.Prompt)
                .setCancelable(false)
                .setMessage("入库成功")
                .setPositiveButton(R.string.OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                            }
                        }).show();
    }

    private void uploadSnFailed(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.Prompt)
                .setCancelable(false)
                .setMessage("入库失败，原因：" + message)
                .setPositiveButton(R.string.OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                            }
                        }).show();
    }

}
