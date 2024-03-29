package com.petkit.matetool.ui.D4;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.widget.LoadDialog;
import com.petkit.matetool.R;
import com.petkit.matetool.http.ApiTools;
import com.petkit.matetool.http.AsyncHttpRespHandler;
import com.petkit.matetool.model.DevicesError;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.D4.utils.D4Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.common.utils.DeviceCommonUtils;
import com.petkit.matetool.utils.FileUtils;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.JSONUtils;
import com.petkit.matetool.utils.TesterManagerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static com.petkit.matetool.ui.D4.utils.D4Utils.FILE_CHECK_INFO_NAME;
import static com.petkit.matetool.ui.D4.utils.D4Utils.FILE_MAINTAIN_INFO_NAME;


/**
 * D4测试，测试准备，需要登录，已经缓存数据处理
 *
 * Created by Jone on 17/4/19.
 */
public class D4TestPrepareActivity extends BaseActivity {

    private Tester mTester;
    private EditText nameEdit, pwEdit;
    private TextView promptText, testerInfoTextView;
    private Button actionBtn, uploadBtn;

    private DevicesError mDevicesError;
    private boolean isLogining;
    private int mDeviceType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mDeviceType = savedInstanceState.getInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE);
        } else {
            mDeviceType = getIntent().getIntExtra(DeviceCommonUtils.EXTRA_DEVICE_TYPE, 0);
        }
        setContentView(R.layout.activity_feeder_prepare);

        ApiTools.setApiBaseUrl();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
    }

    @Override
    protected void setupViews() {
        setTitle(R.string.Title_prepare);

        promptText = (TextView) findViewById(R.id.test_prompt);
        actionBtn = (Button) findViewById(R.id.login);
        actionBtn.setOnClickListener(this);
        uploadBtn = (Button) findViewById(R.id.upload);
        uploadBtn.setOnClickListener(this);
        nameEdit = (EditText) findViewById(R.id.input_login_name);
        pwEdit = (EditText) findViewById(R.id.input_login_password);
        testerInfoTextView = (TextView) findViewById(R.id.tester_info);

        findViewById(R.id.logout).setOnClickListener(this);

        mTester = TesterManagerUtils.getCurrentTesterForType(Globals.D4);
//        mTester = new Tester();
//        mTester.setCode("00");
//        mTester.setName("写死的账号");
//        mTester.setStation("1");
        if (mTester != null) {
            if (!isEmpty(mTester.getName())) {
                testerInfoTextView.setText("当前用户名：" + mTester.getName());
            }
            AsyncHttpUtil.addHttpHeader("F-Session", mTester.getSession());
        }

        mDevicesError = D4Utils.getDevicesErrorMsg();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if(mTester != null) {
                    if (D4Utils.checkHasSnCache()) {
                        LoadDialog.show(this);
                        startUploadSn();
                    } else {
                        startTest();
                    }
                } else {
                    String name = nameEdit.getEditableText().toString();
                    String pw = pwEdit.getEditableText().toString();
                    if(isEmpty(name)) {
                        showShortToast("请输入用户名");
                        return;
                    }
                    if(isEmpty(pw)) {
                        showShortToast("请输入密码");
                        return;
                    }
                    login(name, pw);
                }
                break;
            case R.id.logout:
                if(mDevicesError != null) {
                    showShortToast("还有异常数据需要处理，完成后才能登出！");
                } else if(D4Utils.checkHasSnCache()) {
                    showShortToast("还有未上传的测试数据，需要先上传完成才能登出！");
                } else {
                    logout();
                }
                break;
            case R.id.upload:
                if(mDevicesError != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
                    bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
                    startActivityWithData(D4ErrorListActivity.class, bundle, false);
                } else {
                    LoadDialog.show(this);
                    startUploadSn();
                }
                break;
        }
    }

    private void updateView () {
        if(mTester != null) {
            mDevicesError = D4Utils.getDevicesErrorMsg();
            if(mDevicesError != null) {
                promptText.setText("检测到异常数据，需要先处理，才能开始测试！");
                actionBtn.setVisibility(View.GONE);
                uploadBtn.setVisibility(View.VISIBLE);
                uploadBtn.setText("处理异常");
            } else if(D4Utils.checkHasSnCache()) {
                promptText.setText("还有未上传的测试数据，需要先上传完成才能开始测试！");
                actionBtn.setVisibility(View.GONE);
                uploadBtn.setVisibility(View.VISIBLE);
                uploadBtn.setText("开始上传");
            } else {
                promptText.setText("可以开始测试啦！");
                actionBtn.setText("进入测试");
                actionBtn.setVisibility(View.VISIBLE);

                File dir = new File(D4Utils.getD4StoryDir());
                String[] files = dir.list();
                if(files != null && files.length > 0) {
                    uploadBtn.setVisibility(View.VISIBLE);
                    uploadBtn.setText("开始上传");
                } else {
                    uploadBtn.setVisibility(View.GONE);
                }
            }
            nameEdit.setVisibility(View.INVISIBLE);
            pwEdit.setVisibility(View.INVISIBLE);

            findViewById(R.id.logout).setVisibility(View.VISIBLE);
        } else {
            actionBtn.setText(R.string.Login);
            actionBtn.setVisibility(View.VISIBLE);
            promptText.setText("请先完成登录");
            nameEdit.setVisibility(View.VISIBLE);
            pwEdit.setVisibility(View.VISIBLE);
            uploadBtn.setVisibility(View.GONE);
            findViewById(R.id.logout).setVisibility(View.INVISIBLE);
        }
    }

    private void startTest() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(D4Utils.EXTRA_D4_TESTER, mTester);
        bundle.putInt(DeviceCommonUtils.EXTRA_DEVICE_TYPE, mDeviceType);
        startActivityWithData(D4StartActivity.class, bundle, false);
    }


    private void login(final String name, String pw) {
        LoadDialog.show(this);

        HashMap<String, String> params = new HashMap<>();
        params.put("username", name);
        params.put("password", pw);
        params.put("device", Globals.getDeviceInfo(this));

        AsyncHttpUtil.post("/api/login", params, new AsyncHttpRespHandler(this) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                JSONObject jsonObject = JSONUtils.getJSONObject(responseResult);
                JSONObject dataObj = JSONUtils.getJSONObject(jsonObject, "data");
                if(dataObj != null) {
                    try {
                        String token = JSONUtils.getValue(dataObj, "token");
                        long timestamp = dataObj.getLong("timestamp");
                        String factory = dataObj.getString("factory");
                        String station = dataObj.getString("station");
                        if(Math.abs(System.currentTimeMillis() - timestamp) < 60 * 60 * 1000) {
                            mTester = new Tester();
                            mTester.setCode(factory);
                            mTester.setStation(station);
                            mTester.setName(name);
                            mTester.setSession(token);
                            TesterManagerUtils.addTesterForType(Globals.D4, mTester);

                            AsyncHttpUtil.addHttpHeader("F-Session", token);
                            testerInfoTextView.setText("当前用户名：" + mTester.getName());

                            if(D4Utils.checkHasSnCache()) {
                                isLogining = true;
                                startUploadSn();
                            } else {
                                getLastSN();
                            }
                        } else {
                            showShortToast("系统时间不对，请先设置！");
                            loginFailed();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loginFailed();
                    }
                } else {
                    loginFailed();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                switch (statusCode) {
                    case 463:
                        showShortToast("密码错误");
                        break;
                    case 461:
                        showShortToast("账号被锁定，不能在多个设备上登录同一个账号！");
                        break;
                    case 462:
                        showShortToast("账号已被禁用！");
                        break;
                    case 464:
                        showShortToast("账号不存在！");
                        break;
                    default:
                        showShortToast("登录失败！");
                        break;
                }
                loginFailed();
            }
        });
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<>();
        params.put("device", Globals.getDeviceInfo(this));

        AsyncHttpUtil.post("/api/logout", params, new AsyncHttpRespHandler(this, true) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                loginFailed();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                switch (statusCode) {
                    case 463:
                        showShortToast("密码错误");
                        break;
                    case 461:
                        showShortToast("账号被锁定，不能在多个设备上登录同一个账号！");
                        break;
                    case 462:
                        showShortToast("账号已被禁用！");
                        break;
                    case 464:
                        showShortToast("账号不存在！");
                        break;
                    default:
                        showShortToast("登出失败！");
                        break;
                }
            }
        });
    }

    private void getLastSN() {
        AsyncHttpUtil.get("/api/d4/latest", new AsyncHttpRespHandler(this) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                isLogining = false;

                JSONObject jsonObject = JSONUtils.getJSONObject(responseResult);
                JSONObject dataObj = JSONUtils.getJSONObject(jsonObject, "data");
                try {
                    if(jsonObject.getInt("code") == 0 && dataObj != null && !dataObj.isNull("sn")) {
                        String sn = dataObj.getString("sn");
                        D4Utils.initSnSerializableNumber(sn);
                    }
                    LoadDialog.dismissDialog();
                    updateView();
                } catch (JSONException e) {
                    e.printStackTrace();
                    loginFailed();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);

                loginFailed();
            }
        });
    }

    private void loginFailed() {
        LoadDialog.dismissDialog();
        AsyncHttpUtil.addHttpHeader("F-Session", "");

        TesterManagerUtils.removeTesterForType(Globals.D4);
        mTester = null;
        updateView();
    }

    private void startUploadSn() {
        File dir = new File(D4Utils.getD4StoryDir());
        String[] files = dir.list();

        if(files != null && files.length > 0) {
            uploadSn(new File(dir, files[0]));
        } else if (isLogining) {
            getLastSN();
        } else {
            LoadDialog.dismissDialog();
            showShortToast("上传完成");
            updateView();
        }
    }

    private void uploadSnFailed() {
        LoadDialog.dismissDialog();
    }

    private void uploadSn(final File file) {
        String content = FileUtils.readFileToString(file);

        if(isEmpty(content)) {
            file.delete();
            startUploadSn();
            return;
        }

        String api;
        if(FILE_MAINTAIN_INFO_NAME.equals(file.getName())) {
            api = "/api/d4/maintain/repair";
        } else if(FILE_CHECK_INFO_NAME.equals(file.getName())) {
            api = "/api/d4/maintain/inspect";
        } else {
            api = "/api/d4/batch";
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("snList", "{\"snList\":[" + content.substring(0, content.length() - 1) + "]}");

        AsyncHttpUtil.post(api, params, new AsyncHttpRespHandler(this) {

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
                                    file.delete();
                                    startUploadSn();
                                    break;
                                default:
                                    if(!result.isNull("data")) {
                                        mDevicesError = gson.fromJson(result.getString("data"), DevicesError.class);
                                        D4Utils.storeDuplicatedInfo(mDevicesError);
                                        file.delete();
                                        if (isLogining) {
                                            getLastSN();
                                        } else {
                                            LoadDialog.dismissDialog();
                                            updateView();
                                        }
                                    } else {
                                        file.delete();
                                        startUploadSn();
                                    }
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    uploadSnFailed();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                uploadSnFailed();
            }
        });
    }


}
