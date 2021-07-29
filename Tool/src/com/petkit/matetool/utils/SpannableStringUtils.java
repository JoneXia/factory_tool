package com.petkit.matetool.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.petkit.android.utils.CommonUtils;
import com.petkit.matetool.R;

import java.util.ArrayList;
import java.util.Collections;

public class SpannableStringUtils {
    public static class PetkitURLSpan extends android.text.style.ClickableSpan {
        public String linkUrl;
        public Context context;
        public int linkColor;
        public boolean isNeedUnderLine = true;

        
        /**
         * 
         * @param linkUrlStr
         * @param contextObj
         */
        public PetkitURLSpan(String linkUrlStr, Context contextObj) {
            this.linkUrl = linkUrlStr;
            this.context = contextObj;
            linkColor = CommonUtils.getColorById(R.color.white);
        }
        
        /**
         * 
         * @param linkUrlStr
         * @param contextObj
         */
        public PetkitURLSpan(String linkUrlStr, Context contextObj, int color, boolean isNeedUnderLine) {
            this.linkUrl = linkUrlStr;
            this.context = contextObj;
            this.linkColor = color;
            this.isNeedUnderLine = isNeedUnderLine;
        }
        
        @Override
        public void onClick(View widget) {
        	if(context != null && linkUrl != null){



        	}
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(linkColor);
            ds.setUnderlineText(isNeedUnderLine);
        }

    }
    
    public static class SpanText {
        public String text;
        public String linkUrl;
        public Context context;

        public float relativeFontSize = 1.0f;
        public int textColor = Color.BLACK;
        public int backgroundColor = Color.TRANSPARENT;
        public boolean isNeedUnderLine = false;
        public boolean isNeedStrike = false;
        
        /**
         *  默认构造函数
         */
        public SpanText() {
            
        }
        
        /**
         * 
         * @param textd
         * @param colord
         * @param relativeSize 构造函数，初始化text，颜色和字体大小
         */
        public SpanText(String textd, int colord, float relativeSize) {
            this.text = textd;
            this.textColor = colord;
            if (relativeSize <= 0.0f) {
                relativeSize = 1.0f;
            }
            this.relativeFontSize = relativeSize;
        }

        /**
        public SpanText(Context context, String textd, int colord, float relativeSize, boolean isNeedStrike) {
            this.text = textd;
            this.textColor = colord;
            if (relativeSize <= 0.0f) {
                relativeSize = 1.0f;
            }
            this.relativeFontSize = relativeSize;
            this.isNeedStrike = isNeedStrike;
            this.context = context;
        }

        /**
         *
         * @param context
         * @param textd
         * @param colord
         * @param relativeSize
         * @param linkUrl
         * @param isNeedUnderLine
         */
        public SpanText(Context context, String textd, int colord, float relativeSize, String linkUrl, boolean isNeedUnderLine) {
            this.text = textd;
            this.textColor = colord;
            if (relativeSize <= 0.0f) {
                relativeSize = 1.0f;
            }
            this.relativeFontSize = relativeSize;
            this.linkUrl = linkUrl;
            this.context = context;
        }

        /**
         *
         * @param context
         * @param textd
         * @param colord
         * @param relativeSize
         * @param linkUrl
         * @param backgroundColor
         */
        public SpanText(Context context, String textd, int colord, float relativeSize, String linkUrl, int backgroundColor) {
            this.text = textd;
            this.textColor = colord;
            if (relativeSize <= 0.0f) {
                relativeSize = 1.0f;
            }
            this.relativeFontSize = relativeSize;
            this.linkUrl = linkUrl;
            this.context = context;
            this.backgroundColor = backgroundColor;
        }
        
        /**
         * 
         * @return 对象转换成SpannableString
         */
        public SpannableString toSpanString() {
            if (CommonUtils.isEmpty(text)) {
                return null;
            }
            
            SpannableString sb = new SpannableString(text);
            int spanStyle = SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE;
            sb.setSpan(new RelativeSizeSpan(this.relativeFontSize), 0, sb.length(), spanStyle);
            sb.setSpan(new ForegroundColorSpan(this.textColor), 0, sb.length(), spanStyle);
            
            if (this.backgroundColor != Color.TRANSPARENT) {
//                sb.setSpan(new BackgroundColorSpan(this.backgroundColor), 0, sb.length(), spanStyle);
                Parcel p = Parcel.obtain();
                p.writeInt(backgroundColor);
                p.setDataPosition(0);
                BackgroundColorSpan bcs = new BackgroundColorSpan(p);
                sb.setSpan(bcs, 0, sb.length(), spanStyle);
                p.recycle();
            }
            
            if (this.isNeedStrike) {
                sb.setSpan(new StrikethroughSpan(), 0, sb.length(), spanStyle);         
            }
            
            if (this.isNeedUnderLine) {
                sb.setSpan(new UnderlineSpan(), 0, sb.length(), spanStyle);
            }
            
            if (!CommonUtils.isEmpty(linkUrl) && context != null) {
                PetkitURLSpan mdSpan = new PetkitURLSpan(linkUrl, context, textColor, isNeedUnderLine);
                mdSpan.linkColor = textColor;
                sb.setSpan(mdSpan, 0, sb.length(), spanStyle);                  
            }
            
            return sb;
        }
    }
    
    /**
     * 
     * @param moreSpanText 可变长SpanText 组装SpanString
     * @return
     */
    public static SpannableStringBuilder makeSpannableString(SpanText... moreSpanText) {

        if(moreSpanText == null || moreSpanText.length == 0){
            return null;
        }
        ArrayList<SpanText> textList = new ArrayList<SpanText>();
        Collections.addAll(textList, moreSpanText);
        return makeSpannableString(textList);
    }
    
    /**
     * 
     * @param textList  SpanText数组 组装SpanString
     * @return
     */
    public static SpannableStringBuilder makeSpannableString(ArrayList<SpanText> textList) {
        if (textList.size() > 0) {
            SpannableStringBuilder ret = new SpannableStringBuilder();
            for (SpanText text : textList) {
                if(text != null){
                    ret.append(text.toSpanString());
                }
            }

            return ret;
        }
        return null;
    }
    
    
//	public static void checkSpannableText(TextView textView, int color, boolean isNeedUnderLine) {
//		CharSequence text = textView.getText();
//		if (text instanceof Spannable) {
//			int end = text.length();
//			Spannable sp = (Spannable) textView.getText();
//			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
//			SpannableStringBuilder style = new SpannableStringBuilder(text);
//			style.clearSpans();// should clear old spans
//
//			// 循环把链接发过去
//			for (URLSpan url : urls) {
//				PetkitURLSpan myURLSpan = new PetkitURLSpan(url.getURL(),
//						textView.getContext(), color, isNeedUnderLine);
//				style.setSpan(myURLSpan, sp.getSpanStart(url),
//						sp.getSpanEnd(url), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//			}
//			textView.setText("");
//			textView.append(style);
////			textView.setText(style);
//		} else 	if(text instanceof SpannedString){
//			int end = text.length();
//			SpannedString ss = (SpannedString) textView.getText();
//
//			URLSpan[] urls = ss.getSpans(0, end, URLSpan.class);
//			SpannableStringBuilder style = new SpannableStringBuilder(text);
//			style.clearSpans();// should clear old spans
//			// 循环把链接发过去
//			for (URLSpan url : urls) {
//				PetkitURLSpan myURLSpan = new PetkitURLSpan(url.getURL(),
//						textView.getContext(), color, isNeedUnderLine);
//				style.setSpan(myURLSpan, ss.getSpanStart(url),
//						ss.getSpanEnd(url), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//			}
//			textView.setText("");
//			textView.append(style);
//		}
//	}
}