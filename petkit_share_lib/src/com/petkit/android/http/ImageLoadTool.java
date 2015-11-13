package com.petkit.android.http;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.PetkitLog;

public class ImageLoadTool {

	private static ImageLoader imageLoader = ImageLoader.getInstance();
//	private static DisplayImageOptions options = new DisplayImageOptions.Builder()
////            .resetViewBeforeLoading(true)
//            .cacheInMemory(true)
//            .cacheOnDisk(true)
//            .considerExifParams(true)
//            .bitmapConfig(Bitmap.Config.RGB_565)
//            .displayer(new SimpleBitmapDisplayer())
//            .imageScaleType(ImageScaleType.EXACTLY)
//            .build();

	public static ImageLoader getImageLoader(){
		return imageLoader;
	}
	public static boolean checkImageLoader(){
		return imageLoader.isInited();
	}
	
//	public static void disPlay(String uri, ImageAware imageAware,int default_pic){
//		disPlay(uri, imageAware, default_pic, default_pic);
//	}
//
//	public static void disPlay(String uri, ImageAware imageAware,int default_pic, int load_pic){
//		DisplayImageOptions options = new DisplayImageOptions.Builder()
//			.showImageOnLoading(load_pic)
//			.showImageForEmptyUri(default_pic)
//			.showImageOnFail(default_pic)
//			.cacheInMemory(true)
//			.bitmapConfig(Bitmap.Config.RGB_565)
//			.displayer(new SimpleBitmapDisplayer())
//            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//			.build();
//
//		imageLoader.displayImage(uri, imageAware, options);
//	}

    public static void disPlay(String uri, final ImageViewAware imageViewAware, final View spinner, final int default_pic){
//        if(CommonUtils.isEmpty(uri)){
//            imageView.setImageResource(default_pic);
//            return;
//        }
//        if(spinner == null){
//            imageViewAware.setImageResource(default_pic);
//        }

        DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(default_pic)
			.showImageForEmptyUri(default_pic)
			.showImageOnFail(default_pic)
			.cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new SimpleBitmapDisplayer())
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.build();

        imageViewAware.getWrappedView().setTag(uri);
        imageViewAware.setUri(uri);
        PetkitLog.d("displayImage uri: " + uri);
        PetkitLog.d("displayImage imageView: " + imageViewAware.getWrappedView().toString());

        imageLoader.displayImage(uri, imageViewAware, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                if (spinner != null) {
                    spinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                if (spinner != null) {
                    spinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (spinner != null) {
                    spinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                if (spinner != null) {
                    spinner.setVisibility(View.GONE);
                }
            }
        });
    }

    public static void disPlay(String uri, final ImageView imageView, final View spinner, final int default_pic) {
        disPlay(uri, new ImageViewAware(imageView), spinner, default_pic);
    }

    public static void disPlay(String uri, ImageView imageView, int default_pic){
        disPlay(uri, imageView, null, default_pic);
    }

	@SuppressWarnings("deprecation")
	public static String getBitmapPath(String uri){
		if(imageLoader.getDiscCache() instanceof UnlimitedDiskCache){
			UnlimitedDiskCache discCache = (UnlimitedDiskCache)imageLoader.getDiscCache();
			return discCache.get(uri).getAbsolutePath();
		} else if(imageLoader.getDiscCache() instanceof LimitedAgeDiskCache){
			LimitedAgeDiskCache discCache = (LimitedAgeDiskCache)imageLoader.getDiscCache();
			return discCache.get(uri).getAbsolutePath();
		}
		return "";
	}
	

	@SuppressWarnings("deprecation")
	public static void clear(){
		clearMemoryCache();
		imageLoader.clearDiscCache();
	}
	
	public static void clearMemoryCache(){
		imageLoader.clearMemoryCache();		
	}
	
	
	public static void resume(){
		imageLoader.resume();
	}
	/**
	 * 暂停加载
	 */
	public static void pause(){
		imageLoader.pause();
	}
	/**
	 * 停止加载
	 */
	public static void stop(){
		imageLoader.stop();
	}
	/**
	 * 销毁加载
	 */
	public static void destroy() {
		imageLoader.destroy();
	}
}
