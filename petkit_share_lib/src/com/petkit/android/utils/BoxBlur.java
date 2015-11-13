package com.petkit.android.utils;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.petkit.android.http.CheckImageLoaderConfiguration;
import com.petkit.android.http.ImageLoadTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BoxBlur {
	
	/** 模糊迭代度 */
	private static int iterations = 40;
	
//	private static LimitedAgeDiskCache limitedAgeDiscCache = new LimitedAgeDiskCache(StorageUtils.getCacheDirectory(
//			CommonUtils.getAppContext()),new Md5FileNameGenerator(), CheckImageLoaderConfiguration.discCacheLimitTime);
	
	
	public static Drawable doBlur(Context context, String url, int defaultResId) {  
		if(CommonUtils.isEmpty(url)){
			return context.getResources().getDrawable(defaultResId);
		}
		String path = ImageLoadTool.getBitmapPath(url);//limitedAgeDiscCache.get(url).getAbsolutePath();
		Bitmap bmp = BitmapFactory.decodeFile(path);
		if(bmp == null){
			return context.getResources().getDrawable(defaultResId);
		}
		
		try {
			return doBlur(context, path);
		} catch (OutOfMemoryError e) {
			System.gc();
			try {
				return context.getResources().getDrawable(defaultResId);
			} catch (OutOfMemoryError e1) {
				System.gc();
				return null;
			}
		}
	}
	
	
    @SuppressWarnings("deprecation")
	public static Drawable doBlur(Context context, String path) throws OutOfMemoryError{  
    	int radius = iterations;
    	boolean canReuseInBitmap = true;
        // Stack Blur v1.0 from  
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html  
        //  
        // Java Author: Mario Klingemann <mario at quasimondo.com>  
        // http://incubator.quasimondo.com  
        // created Feburary 29, 2004  
        // Android port : Yahel Bouaziz <yahel at kayenko.com>  
        // http://www.kayenko.com  
        // ported april 5th, 2012  
  
        // This is a compromise between Gaussian Blur and Box blur  
        // It creates much better looking blurs than Box Blur, but is  
        // 7x faster than my Gaussian Blur implementation.  
        //  
        // I called it Stack Blur because this describes best how this  
        // filter works internally: it creates a kind of moving stack  
        // of colors whilst scanning through the image. Thereby it  
        // just has to add one new block of color to the right side  
        // of the stack and remove the leftmost color. The remaining  
        // colors on the topmost layer of the stack are either added on  
        // or reduced by one, depending on if they are on the right or  
        // on the left side of the stack.  
        //  
        // If you are using this algorithm in your code please add  
        // the following line:  
        //  
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>  
  
    	if(CommonUtils.getAndroidSDKVersion() < 16){
    		return new BitmapDrawable();
    	}
    	
		Bitmap bmp = BitmapFactory.decodeFile(path);
		if(bmp == null){
			return new BitmapDrawable();
		}
		
        Bitmap bitmap;  
        if (canReuseInBitmap) {  
            bitmap = bmp;  
        } else {  
            bitmap = bmp.copy(bmp.getConfig(), true);  
        }  
  
        if (radius < 1) {  
            return (null);  
        }  
        
        try {
        	int w = bitmap.getWidth();  
            int h = bitmap.getHeight();  
            
            Bitmap outBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            int[] pix = new int[w * h];  
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);  
      
            int wm = w - 1;  
            int hm = h - 1;  
            int wh = w * h;  
            int div = radius + radius + 1;  
      
            int r[] = new int[wh];  
            int g[] = new int[wh];  
            int b[] = new int[wh];  
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;  
            int vmin[] = new int[Math.max(w, h)];  
      
            int divsum = (div + 1) >> 1;  
            divsum *= divsum;  
            int dv[] = new int[256 * divsum];  
            for (i = 0; i < 256 * divsum; i++) {  
                dv[i] = (i / divsum);  
            }  
      
            yw = yi = 0;  
      
            int[][] stack = new int[div][3];  
            int stackpointer;  
            int stackstart;  
            int[] sir;  
            int rbs;  
            int r1 = radius + 1;  
            int routsum, goutsum, boutsum;  
            int rinsum, ginsum, binsum;  
      
            for (y = 0; y < h; y++) {  
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
                for (i = -radius; i <= radius; i++) {  
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];  
                    sir = stack[i + radius];  
                    sir[0] = (p & 0xff0000) >> 16;  
                    sir[1] = (p & 0x00ff00) >> 8;  
                    sir[2] = (p & 0x0000ff);  
                    rbs = r1 - Math.abs(i);  
                    rsum += sir[0] * rbs;  
                    gsum += sir[1] * rbs;  
                    bsum += sir[2] * rbs;  
                    if (i > 0) {  
                        rinsum += sir[0];  
                        ginsum += sir[1];  
                        binsum += sir[2];  
                    } else {  
                        routsum += sir[0];  
                        goutsum += sir[1];  
                        boutsum += sir[2];  
                    }  
                }  
                stackpointer = radius;  
      
                for (x = 0; x < w; x++) {  
      
                    r[yi] = dv[rsum];  
                    g[yi] = dv[gsum];  
                    b[yi] = dv[bsum];  
      
                    rsum -= routsum;  
                    gsum -= goutsum;  
                    bsum -= boutsum;  
      
                    stackstart = stackpointer - radius + div;  
                    sir = stack[stackstart % div];  
      
                    routsum -= sir[0];  
                    goutsum -= sir[1];  
                    boutsum -= sir[2];  
      
                    if (y == 0) {  
                        vmin[x] = Math.min(x + radius + 1, wm);  
                    }  
                    p = pix[yw + vmin[x]];  
      
                    sir[0] = (p & 0xff0000) >> 16;  
                    sir[1] = (p & 0x00ff00) >> 8;  
                    sir[2] = (p & 0x0000ff);  
      
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
      
                    rsum += rinsum;  
                    gsum += ginsum;  
                    bsum += binsum;  
      
                    stackpointer = (stackpointer + 1) % div;  
                    sir = stack[(stackpointer) % div];  
      
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
      
                    rinsum -= sir[0];  
                    ginsum -= sir[1];  
                    binsum -= sir[2];  
      
                    yi++;  
                }  
                yw += w;  
            }  
            for (x = 0; x < w; x++) {  
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;  
                yp = -radius * w;  
                for (i = -radius; i <= radius; i++) {  
                    yi = Math.max(0, yp) + x;  
      
                    sir = stack[i + radius];  
      
                    sir[0] = r[yi];  
                    sir[1] = g[yi];  
                    sir[2] = b[yi];  
      
                    rbs = r1 - Math.abs(i);  
      
                    rsum += r[yi] * rbs;  
                    gsum += g[yi] * rbs;  
                    bsum += b[yi] * rbs;  
      
                    if (i > 0) {  
                        rinsum += sir[0];  
                        ginsum += sir[1];  
                        binsum += sir[2];  
                    } else {  
                        routsum += sir[0];  
                        goutsum += sir[1];  
                        boutsum += sir[2];  
                    }  
      
                    if (i < hm) {  
                        yp += w;  
                    }  
                }  
                yi = x;  
                stackpointer = radius;  
                for (y = 0; y < h; y++) {  
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )  
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];  
      
                    rsum -= routsum;  
                    gsum -= goutsum;  
                    bsum -= boutsum;  
      
                    stackstart = stackpointer - radius + div;  
                    sir = stack[stackstart % div];  
      
                    routsum -= sir[0];  
                    goutsum -= sir[1];  
                    boutsum -= sir[2];  
      
                    if (x == 0) {  
                        vmin[y] = Math.min(y + r1, hm) * w;  
                    }  
                    p = x + vmin[y];  
      
                    sir[0] = r[p];  
                    sir[1] = g[p];  
                    sir[2] = b[p];  
      
                    rinsum += sir[0];  
                    ginsum += sir[1];  
                    binsum += sir[2];  
      
                    rsum += rinsum;  
                    gsum += ginsum;  
                    bsum += binsum;  
      
                    stackpointer = (stackpointer + 1) % div;  
                    sir = stack[stackpointer];  
      
                    routsum += sir[0];  
                    goutsum += sir[1];  
                    boutsum += sir[2];  
      
                    rinsum -= sir[0];  
                    ginsum -= sir[1];  
                    binsum -= sir[2];  
      
                    yi += w;  
                }  
            }  
      
            outBitmap.setPixels(pix, 0, w, 0, 0, w, h);  
            Drawable drawable = new BitmapDrawable(outBitmap);
            return drawable;
		} catch (OutOfMemoryError e) {
			throw e;
		}
  
    }  
}
