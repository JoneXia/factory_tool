package com.petkit.android.widget;

import com.petkit.android.utils.ConvertDipPx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class UserAvatarEffectImageView extends ImageView{

	public UserAvatarEffectImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public UserAvatarEffectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public UserAvatarEffectImageView(Context context) {
		super(context);
		init();
	}

	private void init(){
		setScaleType(ScaleType.CENTER_CROP);
	}
	
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Config.ARGB_8888);
            if(output == null){
                return bitmap;
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = ConvertDipPx.dip2px(getContext(), 6);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        } catch (OutOfMemoryError er){
            return bitmap;
        }
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(getRoundedCornerBitmap(bm));
	}
	
	
	@Override
	public void setImageResource(int resId) {
		BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(resId);
		Bitmap bm = bd.getBitmap();
		this.setImageBitmap(bm);
	}
	

}
