package com.petkit.matetool.player.ijkplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.Scroller;

/**
 * Description:
 * author:jiawei.hao
 * Date:10/18/22
 */
public class H3TextureView extends TextureView {

    private Scroller scroller;

    public H3TextureView(Context context) {
        super(context);
        initView(context);
    }

    public H3TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public H3TextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        scroller = new Scroller(context);
    }

    public Scroller getScroller() {
        return scroller;
    }

    @Override
    public void computeScroll() {
        //computeScrollOffset返回值说明：true说明滚动尚未完成，false说明滚动已经完成
        if (scroller.computeScrollOffset()){
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }


}
