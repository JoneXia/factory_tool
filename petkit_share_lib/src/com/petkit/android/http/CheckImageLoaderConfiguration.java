package com.petkit.android.http;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**    
 */
public class CheckImageLoaderConfiguration {
	
	public static final long discCacheLimitTime = 3600*24*15L;

	public static void checkImageLoaderConfiguration(Context context){
		if(!ImageLoadTool.checkImageLoader()){
			// This configuration tuning is custom. You can tune every option, you may tune some of them,
			// or you can create default configuration by
			// ImageLoaderConfiguration.createDefault(this);
			// method.
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
					.threadPriority(Thread.NORM_PRIORITY)
					.denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new WeakMemoryCache())
					.diskCacheFileNameGenerator(new Md5FileNameGenerator())
					.diskCache(new LimitedAgeDiskCache(StorageUtils.getCacheDirectory(context), null, new Md5FileNameGenerator(), discCacheLimitTime))
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.imageDownloader(new CustomImageDownloader(context))
					.build();
			// Initialize ImageLoader with configuration.
			ImageLoader.getInstance().init(config);
		}
	}
}
