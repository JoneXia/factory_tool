package com.petkit.android.utils;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.petkit.android.http.AsyncHttpUtil;
import com.petkit.android.http.CustomImageDownloader;

/**
 * 定位，回调返回经纬度
 * 
 * @author Jone
 *
 */
public class LocationUtils {

	private LocationManagerProxy mLocationManagerProxy;
	private ILocationListener mLocationListener;
	
	public void startGetLocation(Activity activity, ILocationListener listener){
		mLocationListener = listener;
		
		// 初始化定位，只采用网络定位
		mLocationManagerProxy = LocationManagerProxy.getInstance(activity);
		mLocationManagerProxy.setGpsEnable(false);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次,
		//在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, -1, -1, new AMapLocationListener() {
					
					@Override
					public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderEnabled(String arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderDisabled(String arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(Location arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(AMapLocation amapLocation) {
						if (amapLocation!=null&&amapLocation.getAMapException().getErrorCode() == 0) {
							String locationString = amapLocation.getLongitude() + "," + amapLocation.getLatitude();
							CommonUtils.addSysMap(CommonUtils.getAppContext(), Consts.HTTP_HEADER_LOCATION, locationString);
							AsyncHttpUtil.addHttpHeader(Consts.HTTP_HEADER_LOCATION, locationString);
							CustomImageDownloader.header.put(Consts.HTTP_HEADER_LOCATION, locationString);
							
							if(mLocationListener != null){
								mLocationListener.onReceiveLocation(amapLocation);
							}
						}
					}
				});
	}
	
	
	public interface ILocationListener{
		public void onReceiveLocation(AMapLocation aMapLocation);
	}
	
}
