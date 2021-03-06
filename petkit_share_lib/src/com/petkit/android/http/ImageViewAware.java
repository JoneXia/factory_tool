package com.petkit.android.http;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ViewAware;
import com.nostra13.universalimageloader.utils.L;

import java.lang.reflect.Field;

/**
 * Wrapper for Android {@link android.widget.ImageView ImageView}. Keeps weak reference of ImageView to prevent memory
 * leaks.
 *
 * @author Jone
 * @since 1.9.4
 */
public class ImageViewAware extends ViewAware {

    private String uri;

    /**
     * Constructor. <br />
     * References {@link #ImageViewAware(android.widget.ImageView, boolean) ImageViewAware(imageView, true)}.
     *
     * @param imageView {@link android.widget.ImageView ImageView} to work with
     */
    public ImageViewAware(ImageView imageView) {
        super(imageView);
    }

    /**
     * Constructor
     *
     * @param imageView           {@link android.widget.ImageView ImageView} to work with
     * @param checkActualViewSize <b>true</b> - then {@link #getWidth()} and {@link #getHeight()} will check actual
     *                            size of ImageView. It can cause known issues like
     *                            <a href="https://github.com/nostra13/Android-Universal-Image-Loader/issues/376">this</a>.
     *                            But it helps to save memory because memory cache keeps bitmaps of actual (less in
     *                            general) size.
     *                            <p/>
     *                            <b>false</b> - then {@link #getWidth()} and {@link #getHeight()} will <b>NOT</b>
     *                            consider actual size of ImageView, just layout parameters. <br /> If you set 'false'
     *                            it's recommended 'android:layout_width' and 'android:layout_height' (or
     *                            'android:maxWidth' and 'android:maxHeight') are set with concrete values. It helps to
     *                            save memory.
     *                            <p/>
     */
    public ImageViewAware(ImageView imageView, boolean checkActualViewSize) {
        super(imageView, checkActualViewSize);
    }

    /**
     * {@inheritDoc}
     * <br />
     * 3) Get <b>maxWidth</b>.
     */
    @Override
    public int getWidth() {
        int width = super.getWidth();
        if (width <= 0) {
            ImageView imageView = (ImageView) viewRef.get();
            if (imageView != null) {
                width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check maxWidth parameter
            }
        }
        return width;
    }

    /**
     * {@inheritDoc}
     * <br />
     * 3) Get <b>maxHeight</b>
     */
    @Override
    public int getHeight() {
        int height = super.getHeight();
        if (height <= 0) {
            ImageView imageView = (ImageView) viewRef.get();
            if (imageView != null) {
                height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check maxHeight parameter
            }
        }
        return height;
    }

    @Override
    public ViewScaleType getScaleType() {
        ImageView imageView = (ImageView) viewRef.get();
        if (imageView != null) {
            return ViewScaleType.fromImageView(imageView);
        }
        return super.getScaleType();
    }

    @Override
    public ImageView getWrappedView() {
        return (ImageView) super.getWrappedView();
    }

    @Override
    protected void setImageDrawableInto(Drawable drawable, View view) {
        if(checkInvalid(view)){
            return;
        }

        ((ImageView) view).setImageDrawable(drawable);
        if (drawable instanceof AnimationDrawable) {
            ((AnimationDrawable)drawable).start();
        }
    }

    @Override
    protected void setImageBitmapInto(Bitmap bitmap, View view) {
        if(checkInvalid(view)){
            return;
        }

        ((ImageView) view).setImageBitmap(bitmap);
    }

    private boolean checkInvalid(View view){
        return uri != null && !uri.equals(view.getTag());
    }

    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            L.e(e);
        }
        return value;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
