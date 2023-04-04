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
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageLoadTool {

	private static ImageLoader imageLoader = ImageLoader.getInstance();

	public static ImageLoader getImageLoader(){
		return imageLoader;
	}
	public static boolean checkImageLoader(){
		return imageLoader.isInited();
	}

    public static void disPlay(String uri, final ImageViewAware imageViewAware, final View spinner, final int default_pic){
        disPlay(uri,imageViewAware,spinner,default_pic,true);
    }
	
    public static void disPlay(String uri, final ImageViewAware imageViewAware, final View spinner, final int default_pic,boolean isCacheInMemory){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(default_pic)
			.showImageForEmptyUri(default_pic)
			.showImageOnFail(default_pic)
			.cacheInMemory(isCacheInMemory)
            .cacheOnDisk(true)
            .considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new SimpleBitmapDisplayer())
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.build();

        imageViewAware.getWrappedView().setTag(uri);
        imageViewAware.setUri(uri);

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

    public static void disPlay(String uri, final ImageView imageView, final View spinner, final int default_pic, boolean isCacheInMemory) {
        disPlay(uri, new ImageViewAware(imageView), spinner, default_pic, isCacheInMemory);
    }

    public static void disPlay(String uri, ImageView imageView, int default_pic){
        disPlay(uri, imageView, null, default_pic);
    }

    public static void disPlay(String uri, ImageView imageView, int default_pic, boolean isCacheInMemory){
        disPlay(uri, imageView, null, default_pic, isCacheInMemory);
    }

    public static void disPlay(String uri, final ImageView imageView, final ImageLoadingListener listener){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .build();

        ImageViewAware imageViewAware = new ImageViewAware(imageView);
        imageViewAware.getWrappedView().setTag(uri);
        imageViewAware.setUri(uri);

        imageLoader.displayImage(uri, imageViewAware, options, listener);
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
     * 停止加载
     */
    public static void destory(){
        imageLoader.destroy();
    }
	/**
	 * 销毁加载
	 */
	public static void destroy() {
		imageLoader.destroy();
	}
}
