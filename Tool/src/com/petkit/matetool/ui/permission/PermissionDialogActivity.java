package com.petkit.matetool.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.petkit.matetool.R;
import com.petkit.matetool.ui.base.BaseApplication;
import com.petkit.matetool.ui.permission.mode.PermissionBean;
import com.petkit.matetool.ui.permission.presenter.PermissionPresenterCompl;
import com.petkit.matetool.ui.permission.view.IPermissionDialogView;
import com.petkit.matetool.utils.Globals;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class PermissionDialogActivity extends Activity implements OnClickListener, IPermissionDialogView {
	
	private PermissionPresenterCompl mPermissionPresenterCompl;
	private ArrayList<PermissionBean> permissionBeans;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(savedInstanceState != null){
			permissionBeans = (ArrayList<PermissionBean>) savedInstanceState.getSerializable(Globals.EXTRA_PERMISSION_CONTENT);
		}else {
			permissionBeans = (ArrayList<PermissionBean>) getIntent().getSerializableExtra(Globals.EXTRA_PERMISSION_CONTENT);
		}
		
		setContentView(R.layout.activity_permission);
		setFinishOnTouchOutside(false);
		
		initView();

		mPermissionPresenterCompl = new PermissionPresenterCompl(this, permissionBeans);
		
		registerBoradcastReceiver();
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putSerializable(Globals.EXTRA_PERMISSION_CONTENT, permissionBeans);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.permission_next:
			mPermissionPresenterCompl.startNext();
			break;

		default:
			break;
		}
	}

	
	@Override
	public void finish() {
		super.finish();
		
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterBroadcastReceiver();
//		Intent intent = new Intent(Constants.BROADCAST_MSG_UPGRADE_DIALOG_FINISH);
//		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) || super.dispatchKeyEvent(event);
	}
	
	private void initView(){

		findViewById(R.id.permission_next).setOnClickListener(this);

		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		int width = BaseApplication.getDisplayMetrics(this).widthPixels;
		lp.width = (int) (width * 0.8);
		lp.height = (int) (BaseApplication.getDisplayMetrics(this).heightPixels * 0.5);
		dialogWindow.setAttributes(lp);
	}
	
	
	private BroadcastReceiver mBroadcastReceiver;
	
	private void registerBoradcastReceiver(){
		mBroadcastReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent data) {
			}
		};
		
		IntentFilter filter = new IntentFilter();
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
	}
	
	private void unregisterBroadcastReceiver(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	public void startQueryPermission(List<PermissionBean> permissionBeanList) {
		if(permissionBeanList.size() == 0){
			sendPermissionFinishBroadcast(true);
			return;
		}

		String[] permissionsToRequest = new String[permissionBeanList.size()];
		for (int i = 0; i < permissionBeanList.size(); i++){
			permissionsToRequest[i] = permissionBeanList.get(i).getContent();
		}
		ActivityCompat.requestPermissions(this, permissionsToRequest, 0x001);
	}

	@Override
	public void initPermissionList(List<PermissionBean> permissionBeanList) {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.permission_content);
		linearLayout.removeAllViews();

		for (int i = 0; i < permissionBeanList.size(); i++){
			PermissionBean permissionBean = permissionBeanList.get(i);
			View view = LayoutInflater.from(this).inflate(R.layout.layout_permission_cell, linearLayout, false);
			ImageView avatar = (ImageView) view.findViewById(R.id.permission_icon);
			avatar.setImageResource(permissionBean.getIcon());

			TextView name = (TextView) view.findViewById(R.id.permission_name);
			name.setText(permissionBean.getName());

			linearLayout.addView(view);
		}
	}

	@Override
	public boolean checkSelfPermissionContent(String permissionContent) {
		return PermissionChecker.checkSelfPermission(this, permissionContent)
				== PermissionChecker.PERMISSION_GRANTED;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(requestCode == 0x001){
			if(permissions == null || grantResults == null){
				sendPermissionFinishBroadcast(false);
				return;
			}

			boolean result = true;
			for( int i = 0; i < permissions.length; i++ ) {
				if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_DENIED ) {
					result = false;
					break;
				}
			}

			if(result){
				sendPermissionFinishBroadcast(true);
			} else {
				showStoragePermissionConfirmDialog();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void showStoragePermissionConfirmDialog(){
		new AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle(R.string.Prompt)
				.setMessage(R.string.Hint_permission_storage_must_request)
				.setPositiveButton(R.string.OK,
						new DialogInterface.OnClickListener(){
							public void onClick(
									DialogInterface dialog,
									int which){
								ActivityCompat.requestPermissions(PermissionDialogActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x001);
							}
						})
				.setNegativeButton(R.string.Cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(
									DialogInterface dialog,
									int which){
								sendPermissionFinishBroadcast(false);
							}
						}).show();
	}

	private void sendPermissionFinishBroadcast(boolean result){
		Intent intent = new Intent(Globals.BROADCAST_PERMISSION_FINISHED);
		intent.putExtra("result", result);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		finish();
	}

}
