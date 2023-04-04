package com.petkit.android.utils;

import android.app.Activity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.http.CustomImageDownloader;

/**
 * 定位，回调返回经纬度
 * 
 * @author Jone
 *
 */
public class LocationUtils {

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	private ILocationListener mLocationListener;
	
	public void startGetLocation(Activity activity, ILocationListener listener){

		mLocationListener = listener;

		locationClient = new AMapLocationClient(activity.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(new AMapLocationListener() {

			@Override
			public void onLocationChanged(AMapLocation loc) {
				if (loc!=null && loc.getErrorCode() == 0) {
					String locationString = loc.getLongitude() + "," + loc.getLatitude();
					CommonUtils.addSysMap(CommonUtils.getAppContext(), Consts.HTTP_HEADER_LOCATION, locationString);
					AsyncHttpUtil.addHttpHeader(Consts.HTTP_HEADER_LOCATION, locationString);
					CustomImageDownloader.header.put(Consts.HTTP_HEADER_LOCATION, locationString);

					if(mLocationListener != null){
						mLocationListener.onReceiveLocation(loc);
					}
				}
			}
		});

		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(false);
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(false);
		locationOption.setOnceLocation(true);
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();

	}
	
	
	public interface ILocationListener{
		void onReceiveLocation(AMapLocation aMapLocation);
	}
	
}
