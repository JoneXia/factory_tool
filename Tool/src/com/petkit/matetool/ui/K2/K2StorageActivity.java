package com.petkit.matetool.ui.K2;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.K2.utils.K2Utils;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.permission.PermissionDialogActivity;
import com.petkit.matetool.ui.permission.mode.PermissionBean;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;

/**
 *
 * Created by Jone on 17/9/14.
 */
public class K2StorageActivity extends BaseActivity {

    private Tester mTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(K2Utils.EXTRA_K2_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(K2Utils.EXTRA_K2_TESTER);
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
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                startActivityWithData(K2StorageFileActivity.class, bundle, false);
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
                bundle.putSerializable(K2Utils.EXTRA_K2_TESTER, mTester);
                startActivityWithData(K2ScanActivity.class, bundle, false);
                break;
        }
    }
}
