package com.petkit.matetool.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.petkit.matetool.R;

import androidx.annotation.Nullable;

/**
 * Description:圆点View
 * author:jiawei.hao
 * Date:7/5/22
 */
public class SmallCircleDotView extends View {

    private Context context;
    private Paint paint;
    private int bgColorResId;

    public SmallCircleDotView(Context context) {
        super(context);
        this.context = context;
    }

    public SmallCircleDotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SmallCircleDotView);
        bgColorResId = ta.getResourceId(R.styleable.SmallCircleDotView_bg_color, R.color.red);
        ta.recycle();
        initialize();
    }

    public SmallCircleDotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SmallCircleDotView);
        bgColorResId = ta.getResourceId(R.styleable.SmallCircleDotView_bg_color, R.color.red);
        ta.recycle();
        initialize();
    }

    private void initialize(){
        paint = new Paint();
        paint.setColor(context.getResources().getColor(bgColorResId));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2,paint);
    }

    public void setBgColorResId(int resId){
        paint.setColor(context.getResources().getColor(resId));
        postInvalidate();
    }

    public void setBgColor(String colorStr){
        paint.setColor(Color.parseColor(colorStr));
        postInvalidate();
    }
}
