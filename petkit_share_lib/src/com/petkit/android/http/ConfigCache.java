package com.petkit.android.http;


import java.io.File;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.Consts;
import com.petkit.android.utils.FileUtils;

import android.util.Log;
  
  
public class ConfigCache {
    private static final String TAG = ConfigCache.class.getName();
  
    public static final long CONFIG_CACHE_MOBILE_TIMEOUT  = 3600000 * 24 * 7;  //1 weak
    public static final long CONFIG_CACHE_WIFI_TIMEOUT    = 300000 * 24;   //1 day
  
    public static String getUrlCache(String url) {
        if (url == null) {
            return null;
        }
  
        String result = null;
        File file = new File(CommonUtils.getAppCacheHttpDataDirPath(), new Md5FileNameGenerator().generate(url));
        if (file.exists() && file.isFile()) {
            long expiredTime = System.currentTimeMillis() - file.lastModified();
            Log.d(TAG, file.getAbsolutePath() + " expiredTime:" + expiredTime/60000 + "min");
            //1. in case the system time is incorrect (the time is turn back long ago)
            //2. when the network is invalid, you can only read the cache
            if (CommonUtils.getAPNType() != Consts.NETWORK_NONE && expiredTime < 0) {
                return null;
            }
            if(CommonUtils.getAPNType() == Consts.NETWORK_WIFI
                   && expiredTime > CONFIG_CACHE_WIFI_TIMEOUT) {
                return null;
            } else if (CommonUtils.getAPNType() == Consts.NETWORK_MOBILE
                   && expiredTime > CONFIG_CACHE_MOBILE_TIMEOUT) {
                return null;
            }
            result = FileUtils.readFileToString(file);
        }
        return result;
    }
  
    public static void setUrlCache(String data, String url) {
    	if(url == null){
    		return;
    	}
        String file = CommonUtils.getAppCacheHttpDataDirPath() + new Md5FileNameGenerator().generate(url);
        //创建缓存数据到磁盘，就是创建文件
        FileUtils.writeStringToFile(file, data);
    }
  
    public static String getCacheDecodeString(String url) {
        //1. 处理特殊字符
        //2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更需要如此类似处理，否则有的手机打开图库，全是我们的缓存图片)
        if (url != null) {
            return url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }
}