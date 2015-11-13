package com.petkit.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DogAvatarEffectImageView extends ImageView{

	public DogAvatarEffectImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DogAvatarEffectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DogAvatarEffectImageView(Context context) {
		super(context);
		init();
	}

	private void init(){
		setScaleType(ScaleType.CENTER_CROP);
	}

	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
	    int length = bitmap.getWidth() <= bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
	    
	    Bitmap output = Bitmap.createBitmap(length,
	    		length, Config.ARGB_8888);
		if(output==null)
			return null;
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    final Rect desRect = new Rect(0, 0, length, length);
	    final RectF rectF = new RectF(desRect);
	    final float roundPx = length / 2;

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, desRect, paint);

	    return output;
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		if(bm != null)
			super.setImageBitmap(getRoundedCornerBitmap(bm));
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}  

}
