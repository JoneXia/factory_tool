package com.petkit.matetool.ui.W5;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.utils.PetkitLog;
import com.petkit.matetool.R;
import com.petkit.matetool.http.AsyncHttpRespHandler;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.W5.utils.W5Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.qqtheme.framework.picker.FilePicker;
import cn.qqtheme.framework.util.StorageUtils;
import cz.msebera.android.httpclient.Header;

/**
 *
 * Created by Jone on 17/9/14.
 */
public class W5StorageFileActivity extends BaseActivity {

    private Tester mTester;
    private String fileName;

    private TextView mFileNameTextView, mUploadTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(W5Utils.EXTRA_W5_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(W5Utils.EXTRA_W5_TESTER);
        }

        setContentView(R.layout.activity_feeder_storage_file);
    }

    @Override
    protected void setupViews() {
        setTitle("扫码枪文件导入");

        findViewById(R.id.file_select).setOnClickListener(this);

        mFileNameTextView = (TextView) findViewById(R.id.file_name);
        mUploadTextView = (TextView) findViewById(R.id.file_upload);
        mUploadTextView.setOnClickListener(W5StorageFileActivity.this);
        mUploadTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.file_select:
                FilePicker picker = new FilePicker(this, FilePicker.FILE);
                picker.setRootPath(StorageUtils.getExternalRootPath());
                picker.setItemHeight(30);
                picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                    @Override
                    public void onFilePicked(String currentPath) {
                        fileName = currentPath;
                        mFileNameTextView.setText("选择文件： " + currentPath);
                        mUploadTextView.setVisibility(View.VISIBLE);
                    }
                });
                picker.show();
                break;
            case R.id.file_upload:
                uploadFile(fileName);
                break;
        }
    }


    private void uploadFile(String filename) {
        if (isEmpty(filename) || !filename.endsWith("xlsx")) {
            PetkitLog.d("非法文件！");
            return;
        }

        HashMap<String, String> params = new HashMap<>();

        HashMap<String, List<String>> params2 = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add(filename);
        params2.put("feederFile", files);

        AsyncHttpUtil.post("/api/W5/batch/file", params, params2, new AsyncHttpRespHandler(this, true) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);

                JSONObject result = JSONUtils.getJSONObject(responseResult);
                try {
                    if (!result.isNull("code")) {
                        int code = result.getInt("code");
                        if (code == 0 || code == 2000) {
                            showShortToast("上传成功！");
                            mUploadTextView.setVisibility(View.INVISIBLE);
                            mFileNameTextView.setText("");
                        } else if (!result.isNull("message")){
                            showShortToast(result.getString("message"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                showShortToast(R.string.Hint_network_failed);
            }
        });
    }


}
