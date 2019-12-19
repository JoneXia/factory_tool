package com.petkit.matetool.ui.t3;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.permission.PermissionDialogActivity;
import com.petkit.matetool.ui.permission.mode.PermissionBean;
import com.petkit.matetool.ui.t3.utils.T3Utils;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

/**
 *
 * Created by Jone on 17/9/14.
 */
public class T3StorageActivity extends BaseActivity {

    private Tester mTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(T3Utils.EXTRA_T3_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(T3Utils.EXTRA_T3_TESTER);
        }

        setContentView(R.layout.activity_feeder_storage);
    }

    @Override
    protected void setupViews() {
        setTitle("入库");

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.test_case1:
                Bundle bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                startActivityWithData(T3StorageFileActivity.class, bundle, false);
                break;
            case R.id.test_case2:
                if(!Globals.checkPermission(this, Manifest.permission.CAMERA)
                        || !Globals.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
                    bundle = new Bundle();
                    ArrayList<PermissionBean> permissionBeens = new ArrayList<>();
                    if(!Globals.checkPermission(this, Manifest.permission.CAMERA)) {
                        permissionBeens.add(new PermissionBean(Manifest.permission.CAMERA, R.string.Camera, R.drawable.permission_camera));
                    }

                    if(!Globals.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
                        permissionBeens.add(new PermissionBean(Manifest.permission.RECORD_AUDIO, R.string.Mate_microphone, R.drawable.permission_mic));
                    }

                    bundle.putSerializable(Globals.EXTRA_PERMISSION_CONTENT, permissionBeens);
                    startActivityWithData(PermissionDialogActivity.class, bundle, false);
                    return;
                }

                bundle = new Bundle();
                bundle.putSerializable(T3Utils.EXTRA_T3_TESTER, mTester);
                startActivityWithData(T3ScanActivity.class, bundle, false);
                break;
        }
    }
}
