package com.petkit.matetool.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.zhy.autolayout.AutoFrameLayout;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;

import java.lang.reflect.Field;

import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by zhiyicx on 2016/3/17.
 */
public class ThirdViewUtil {
    public static int USE_AUTOLAYOUT = -1;//0 说明 AndroidManifest 里面没有使用 AutoLauout 的Meta,即不使用 AutoLayout,1 为有 Meta ,即需要使用
    private static String LAYOUT_LINEARLAYOUT = "LinearLayout";
    private static String LAYOUT_FRAMELAYOUT = "FrameLayout";
    private static String LAYOUT_RELATIVELAYOUT = "RelativeLayout";
    private static String ACTIVITY_DELEGATE = "activity_delegate";
    private ThirdViewUtil() {
    }

    public static Unbinder bindTarget(Object target, Object source) {
        if (source instanceof Activity) {
            return ButterKnife.bind(target, (Activity) source);
        } else if (source instanceof View) {
            return ButterKnife.bind(target, (View) source);
        } else if (source instanceof Dialog) {
            return ButterKnife.bind(target, (Dialog) source);
        } else {
            return Unbinder.EMPTY;
        }
    }

    @Nullable
    public static View convertAutoView(String name, Context context, AttributeSet attrs) {
        //本框架并不强制你使用 AutoLayout
        //如果你不想使用 AutoLayout ,就不要在 AndroidManifest 中声明, AutoLayout 的 Meta属性(design_width,design_height)
        if (USE_AUTOLAYOUT == -1) {
            USE_AUTOLAYOUT = 1;
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = packageManager.getApplicationInfo(context
                        .getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo == null || applicationInfo.metaData == null
                        || !applicationInfo.metaData.containsKey("design_width")
                        || !applicationInfo.metaData.containsKey("design_height")) {
                    USE_AUTOLAYOUT = 0;
                }
            } catch (PackageManager.NameNotFoundException e) {
                USE_AUTOLAYOUT = 0;
            }
        }

        if (USE_AUTOLAYOUT == 0) {
            return null;
        }

        View view = null;
        if (name.equals(LAYOUT_FRAMELAYOUT)) {
            view = new AutoFrameLayout(context, attrs);
        } else if (name.equals(LAYOUT_LINEARLAYOUT)) {
            view = new AutoLinearLayout(context, attrs);
        } else if (name.equals(LAYOUT_RELATIVELAYOUT)) {
            view = new AutoRelativeLayout(context, attrs);
        }
        return view;
    }


    /**
     * 解决InputMethodManager中存在的Memory Leak
     * @param destContext destContext
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String [] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object obj_get = null;
        for (int i = 0;i < arr.length;i ++) {
            String param = arr[i];
            try{
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false) {
                    f.setAccessible(true);
                } // author: sodino mail:sodino@qq.com
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View) {
                    View v_get = (View) obj_get;
                    if (v_get.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    } else {
                        break;
                    }
                }
            }catch(Throwable t){
                t.printStackTrace();
            }
        }
    }
}
