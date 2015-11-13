package com.petkit.matetool.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.utils.Globals;
import com.petkit.matetool.utils.Utils;

public class SettingActivity extends BaseActivity {
	private EditText edit1, edit2,edit3, edit4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
	}

    @Override
    protected void setupViews() {
        edit1 = (EditText) findViewById(R.id.ble_value);
        edit2 = (EditText) findViewById(R.id.volt_value_min);
        edit3 = (EditText) findViewById(R.id.volt_value_max);
        edit4 = (EditText) findViewById(R.id.ble_value2);

        edit1.setText(""+CommonUtils.getSysIntMap(SettingActivity.this, Globals.SHARED_BLE_VALUE, -50));
        edit2.setText(CommonUtils.getSysMap(SettingActivity.this, Globals.SHARED_VOLT_MIN, "4.0"));
        edit3.setText(CommonUtils.getSysMap(SettingActivity.this, Globals.SHARED_VOLT_MAX, "5.3"));
        edit4.setText(""+CommonUtils.getSysIntMap(SettingActivity.this, Globals.SHARED_BLE_VALUE2, -60));

        findViewById(R.id.button1).setOnClickListener(this);
    }

    @Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.button1:
			if(saveSettingValue()) {
				 Utils.showToast(this, "保存成功！");
				finish();
			}
			break;
		}
	}
	
	private boolean saveSettingValue() {

        String ble_value = edit1.getEditableText().toString().trim();
        String volt_min = edit2.getEditableText().toString().trim();
        String volt_max = edit3.getEditableText().toString().trim();

        String ble_value2 = edit4.getEditableText().toString().trim();

        try {
            int ble = Integer.parseInt(ble_value);
            if(ble < -70) {
                Utils.showToast(this, "板测蓝牙信号值输入不正确,需大于-70！");
                return false;
            }
            CommonUtils.addSysIntMap(this, Globals.SHARED_BLE_VALUE, ble);

            int ble2 = Integer.parseInt(ble_value2);
            if(ble2 < -70) {
                Utils.showToast(this, "终测蓝牙信号值输入不正确,需大于-70！");
                return false;
            }
            CommonUtils.addSysIntMap(this, Globals.SHARED_BLE_VALUE, ble2);

            float min =  Float.parseFloat(volt_min);
            float max = Float.parseFloat(volt_max);

            if(min < (4.0f) || max > (5.3f)) {
                Utils.showToast(this, "电压值输入不正确！需>=4.0 & <=5.3");
                return false;
            }

            CommonUtils.addSysMap(this, Globals.SHARED_VOLT_MIN, volt_min);
            CommonUtils.addSysMap(this, Globals.SHARED_VOLT_MAX, volt_max);
        } catch (Exception e) {
            Utils.showToast(this, "输入不正确！");
            return false;
        }
        return true;

    }
}
