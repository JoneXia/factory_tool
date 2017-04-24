package com.petkit.matetool.ui.mate;

import com.petkit.matetool.ui.base.BaseActivity;

import android.view.View;

public class InputActivity extends BaseActivity {
    @Override
    protected void setupViews() {

    }

    @Override
    public void onClick(View view) {

    }
//	private final int Normal_mode = 0;
//	private final int Error_mode = 1;
//	private final int Loading_mode = 2;
//	private int mCurMode;
//
//	private BroadcastReceiver mBroadcastReceiver;
//	private EditText EditText1, EditText2, EditText3;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_input);
//
//		registerBoradcastReceiver();
//	}
//
//	@Override
//	public void onBackPressed() {
//		if(mCurMode != Normal_mode) {
//			Utils.sendUnmoniCMD(this);
//			setMode(Normal_mode);
//		} else {
//			super.onBackPressed();
//		}
//	}
//
//	@Override
//	protected void onDestroy() {
//		Utils.sendUnmoniCMD(this);
//		unregisterReceiver(mBroadcastReceiver);
//
//		super.onDestroy();
//	}
//
//    @Override
//    protected void setupViews() {
//        findViewById(R.id.start_test).setOnClickListener(this);
//        findViewById(R.id.test_retry).setOnClickListener(this);
//
//        EditText1 = (EditText) findViewById(R.id.date_edittxt);
//        EditText2 = (EditText) findViewById(R.id.fixture_number_edittxt);
//        if (Globals.g_station != null && !Globals.g_station.isEmpty()) {
//            EditText2.setText(Globals.g_station);
//        }
//
//        EditText2.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
//            }
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//                if (Globals.mCurCaseMode == Globals.FinalTestMode) {
//                    updateSN();
//                }
//            }
//        });
//        EditText3 = (EditText) findViewById(R.id.sn);
//
//        setMode(Normal_mode);
//    }
//
//    private boolean isBackground = false;
//
//	@Override
//	protected void onStop() {
//		isBackground = true;
//		super.onStop();
//	}
//
//	@Override
//	protected void onStart() {
//		isBackground = false;
//		super.onStart();
//	}
//
//	private void registerBoradcastReceiver() {
//		mBroadcastReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context arg0, Intent arg1) {
//				if(isBackground || isFinishing()) {
//					return;
//				}
//
//				if (arg1.getAction().equals("" + Utils.SERVER_CHECK_SYS_PASS)) {
//						if (Globals.mCurCaseMode == Globals.FocusTestMode || Globals.mCurCaseMode == Globals.FocusTestMode2) {
//							if(Globals.FocusTestImageMode) {
//								startActivityForResult(new Intent(InputActivity.this, FocusTestActivity.class), Globals.mCurCaseMode);
//							} else {
//								startActivityForResult(new Intent(InputActivity.this, PlayActivity.class), Globals.mCurCaseMode);
//							}
//						} else if (Globals.mCurCaseMode == Globals.FinalTestMode) {
//							startActivityForResult(new Intent(InputActivity.this, MainActivity.class), Globals.FinalTestMode);
//						} else if (Globals.mCurCaseMode == Globals.SpotTestMode) {
//							startActivity(new Intent(InputActivity.this, SpotActivity.class));
//						} else if (Globals.mCurCaseMode == Globals.BoardTestMode) {
//							startActivityForResult(new Intent(InputActivity.this, MainActivity.class), Globals.BoardTestMode);
//						}
//				} else if (arg1.getAction().equals("" + Utils.SERVER_CHECK_SYS_FAILED)) {
//					setMode(Error_mode);
//				} /*else if (arg1.getAction().equals(Utils.FixtureNumNotSame)) {
//					Utils.showToast(InputActivity.this,R.string.FixtureNumNotSame);
//				} */else if (arg1.getAction().equals(Utils.addWifiStatus)) {
//					if (Globals.mCurCaseMode == Globals.FinalTestMode) {
//						if (mArrayAdapter != null) {
//							mArrayAdapter.notifyDataSetChanged();
//						}
//					}
//				} else if(arg1.getAction().equals(Utils.SERVER_CHECK_HAS_SN)) {
//					Utils.showToast(InputActivity.this, "该设备SN已被写入！！！！！");
//				} else if(arg1.getAction().equals("reset")) {
//					new Handler().postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							if(!Utils.mIsReged) {
//								Utils.mIsReging =  false;
//							}
//						}
//					}, 5000);
//				}
//			}
//		};
//
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("" + Utils.SERVER_CHECK_SYS_PASS);
//		filter.addAction("" + Utils.SERVER_CHECK_SYS_FAILED);
//		filter.addAction(Utils.SERVER_CHECK_HAS_SN);
////		filter.addAction(Utils.FixtureNumNotSame);
//		filter.addAction(Utils.addWifiStatus);
//		filter.addAction("reset");
//		registerReceiver(mBroadcastReceiver, filter);
//	}
//
//
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//
//		setMode(Normal_mode);
//	}
//
//	private void updateSN() {
//		try {
//			String num = EditText2.getText().toString().trim();
////			Globals.g_station = num;
//
//			if (num.isEmpty()) {
//				return;
//			}
//
//			EditText sn = (EditText) findViewById(R.id.sn);
////			sn.setText("��ǰ��ʼSN:" + Globals.organizationSN(this));
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
//
//	// Common
//	private void collapseSoftInputMethod() {
//		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(EditText1.getWindowToken(), 0);
//	}
//
//	@Override
//	public void onClick(View view) {
//		switch (view.getId()) {
//		case R.id.test_retry:
//			collapseSoftInputMethod();
//			Utils.sendUnmoniCMD(this);
//
//			setMode(Normal_mode);
//			break;
//
//		case R.id.start_test:
//			Globals.g_date = EditText1.getText().toString().trim();
//			Globals.g_station = EditText2.getText().toString().trim();
//
//			try {
//				if (Integer.parseInt(Globals.g_station) >= 10) {
//					Utils.showToast(this, getString(R.string.input_error));
//					return;
//				}
//			} catch (Exception e) {
//				Utils.showToast(this, getString(R.string.input_error));
//				return;
//			}
//
//			if (!Globals.g_date.isEmpty() && parseTestStyle()) {
//				collapseSoftInputMethod();
//				setMode(Loading_mode);
//				try {
//					Utils.receiveData(this);
//				} catch (SocketException | UnknownHostException e1) {
//					e1.printStackTrace();
//				}
//			} else {
//				Utils.showToast(this, getString(R.string.input_error));
//				return;
//			}
//			break;
//		}
//	}
//
//	private boolean parseTestStyle() {
//		if (Globals.g_testStyle == Globals.MATE_PRO
//				|| Globals.g_testStyle == Globals.MATE_STYLE) {
//			return true;
//		}
//		return false;
//	}
//
//	private ArrayAdapter<String> mArrayAdapter;
//
//	private void setMode(int mode) {
//		mCurMode = mode;
//
//		switch (mode) {
//		case Normal_mode:
//			findViewById(R.id.test_input).setVisibility(View.VISIBLE);
//			findViewById(R.id.test_loading).setVisibility(View.GONE);
//			findViewById(R.id.test_failed).setVisibility(View.GONE);
//
//			EditText1.setText(Globals.getDateOfToday());
//
//			if (Globals.mCurCaseMode == Globals.BoardTestMode
//					|| Globals.mCurCaseMode == Globals.FocusTestMode2
//					|| Globals.mCurCaseMode == Globals.FocusTestMode) {
//				EditText1.setVisibility(View.GONE);
//				EditText3.setVisibility(View.GONE);
//			}else if(Globals.mCurCaseMode == Globals.FinalTestMode) {
//				try {
//					if (Integer.parseInt(Globals.g_station) >= 10 || Integer.parseInt(Globals.g_station) < 0) {
//						Utils.showToast(this, getString(R.string.input_error));
//					} else {
//						updateSN();
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
//			}
//			break;
//
//		case Loading_mode:
//			findViewById(R.id.test_input).setVisibility(View.GONE);
//			findViewById(R.id.test_loading).setVisibility(View.VISIBLE);
//			findViewById(R.id.test_failed).setVisibility(View.GONE);
//
//			Utils.initGlobalValues();
//			Utils.closeSocket();
//
//			updateLoadingTxt(false);
//			ListView lv = (ListView) findViewById(R.id.loading_mac_list);
//
//			if (Globals.mCurCaseMode == Globals.BoardTestMode
//					|| Globals.mCurCaseMode == Globals.FocusTestMode2
//					|| Globals.mCurCaseMode == Globals.FocusTestMode) {
//				lv.setVisibility(View.GONE);
//			} else if (Globals.mCurCaseMode == Globals.FinalTestMode) {
//				lv.setVisibility(View.VISIBLE);
//				if (mArrayAdapter == null) {
//					mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Utils.mFinalMAC);
//				}
//				lv.setAdapter(mArrayAdapter);
//				lv.setOnItemClickListener(new OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> arg0, View arg1,
//							int arg2, long arg3) {
//							Utils.mFinalIndex = arg2;
//							Utils.wifi_params = Utils.mFinalParams.get(arg2);
//							Utils.sendData(InputActivity.this, Utils.SERVER_REG_MONI, true);
//					}
//				});
//			}
//
//			break;
//		case Error_mode:
//			findViewById(R.id.test_input).setVisibility(View.GONE);
//			findViewById(R.id.test_loading).setVisibility(View.GONE);
//			findViewById(R.id.test_failed).setVisibility(View.VISIBLE);
//			break;
//		}
//	}
//
//	private void updateLoadingTxt(boolean writeflag) {
//		TextView txt = (TextView) findViewById(R.id.loading_txt);
//		if (writeflag) {
//			txt.setText("SN�ѱ�д��....");
//		} else {
//			txt.setText(R.string.loading);
//		}
//	}
}
