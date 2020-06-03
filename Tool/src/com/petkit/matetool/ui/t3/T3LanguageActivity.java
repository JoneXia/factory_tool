package com.petkit.matetool.ui.t3;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.petkit.android.utils.CommonUtils;
import com.petkit.android.utils.FileUtils;
import com.petkit.android.utils.PetkitToast;
import com.petkit.matetool.R;
import com.petkit.matetool.model.Tester;
import com.petkit.matetool.ui.base.BaseActivity;
import com.petkit.matetool.ui.t3.mode.LocaleStringBitmapMode;
import com.petkit.matetool.ui.t3.mode.StringBitmapMode;
import com.petkit.matetool.ui.t3.utils.BitmapConverter;
import com.petkit.matetool.ui.t3.utils.T3Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.petkit.matetool.ui.t3.utils.BitmapFormat.BITMAP_8_BIT_COLOR;

/**
 * 用于将字符串转换成位图，中文字体使用思源黑体，英文待定
 *
 * 更新字符串步骤：
 * 1. 在R.array.t3_lcd_string_list中增加key
 * 2. 在string.xml中增加对应的string，key和第一步的key一致
 * 3. 在本文件中的LocaleList中设置支持的语言，注意设置了的语言需要有对应的翻译
 *
 *
 *
 * Created by Jone on 17/9/14.
 */
public class T3LanguageActivity extends BaseActivity {

    private String[] LocaleList = new String[]{"zh", "en"};


    private Tester mTester;
    private String bmpFileDir;

    private EditText mEditText;
    private Locale mCurLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(T3Utils.EXTRA_T3_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(T3Utils.EXTRA_T3_TESTER);
        }

        setContentView(R.layout.activity_t3_language);
    }

    @Override
    protected void setupViews() {
        setTitle("多语言位图生成");

        findViewById(R.id.test_case1).setOnClickListener(this);
        findViewById(R.id.test_case2).setOnClickListener(this);

        mEditText = (EditText) findViewById(R.id.edit_text);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.test_case1:
                String text = mEditText.getEditableText().toString();

                if (TextUtils.isEmpty(text)) {
                    return;
                }

                Bitmap bitmap = createBitmapFromText(text, "en");
                String filePath = CommonUtils.getAppDirPath() + System.currentTimeMillis() + ".bmp";
                saveBmpToFile(bitmap, filePath);

                PetkitToast.showToast("转换成功，路径：" + filePath);
                break;
            case R.id.test_case2:
                startConvert();
                break;

        }
    }

    public Bitmap createBitmapFromText(String text, String locale) {
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        Typeface typeface;
        if (new Locale(locale).equals(Locale.CHINESE)) {
            typeface = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCN-Normal.ttf");
            paint.setTextSize(22);
        } else {
            typeface = Typeface.createFromAsset(getAssets(), "fonts/Open-Sans-2.ttf");
            paint.setTextSize(18);
        }

        paint.setTypeface(typeface);

        Paint.FontMetricsInt fm = paint.getFontMetricsInt();

        int width = (int)paint.measureText(text);
        int height = fm.descent - fm.ascent + 4;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
        canvas.drawText(text, 0, fm.descent - fm.ascent - (new Locale(locale).equals(Locale.ENGLISH) ? 2 : 0), paint);
        canvas.save();

        return bitmap;
    }


    private void saveBmpToFile(Bitmap bitmap, String filePath) {
        byte[] bitmaps = new BitmapConverter().convert(bitmap, BITMAP_8_BIT_COLOR);
        FileUtils.bytesToFile(bitmaps, filePath);
    }


//
//    /**
//     * 生成视图的预览
//     * @param activity
//     * @param v
//     * @return  视图生成失败返回null
//     *          视图生成成功返回视图的绝对路径
//     */
//    public String saveImageForView (Activity activity, View v) {
//        Bitmap bitmap;
//        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
//        String fileName = time.format(System.currentTimeMillis());
//        String path = CommonUtils.getAppDirPath() + fileName + "-2.jpg";
//        String bitmapPath = CommonUtils.getAppDirPath() + fileName + "-3.bmp";
//        View view = activity.getWindow().getDecorView();
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        bitmap = view.getDrawingCache();
//        Rect frame = new Rect();
//        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//        int[] location = new int[2];
//        v.getLocationOnScreen(location);
//        try {
//            bitmap = Bitmap.createBitmap(bitmap, location[0], location[1], v.getWidth(), v.getHeight());
//
//            FileOutputStream fout = new FileOutputStream(path);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
//            return path;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            // 清理缓存
//            view.destroyDrawingCache();
//        }
//        return null;
//
//    }


    private void startConvert() {

        bmpFileDir = CommonUtils.getAppDirPath() + System.currentTimeMillis() + "/";
        if (new File(bmpFileDir).exists()) {
            new File(bmpFileDir).delete();
        }

        new File(bmpFileDir).mkdirs();

        ArrayList<LocaleStringBitmapMode> localeStringBitmapModes = getAllString();

        ConvertTask task = new ConvertTask(bmpFileDir, localeStringBitmapModes,
                getResources().getStringArray(R.array.t3_lcd_string_list).length * LocaleList.length);
        task.execute();
    }


    private ArrayList<LocaleStringBitmapMode> getAllString() {
        Resources res = getResources();
        if(res != null) {
            mCurLocale = res.getConfiguration().locale;  //得到当前的语言
        }

        ArrayList<LocaleStringBitmapMode> localeStringBitmapModes = new ArrayList<>();
        for (int i = 0; i < LocaleList.length; i++) {
            LocaleStringBitmapMode localeStringBitmapMode = new LocaleStringBitmapMode();
            localeStringBitmapMode.setLocale(LocaleList[i]);
            localeStringBitmapMode.setStrings(getStringListByLocale(res, LocaleList[i]));
            localeStringBitmapModes.add(localeStringBitmapMode);
        }

        resetLocale(res);

        return localeStringBitmapModes;
    }

    private ArrayList<StringBitmapMode> getStringListByLocale(Resources res, String localeName) {
        ArrayList<StringBitmapMode> stringBitmapModes = new ArrayList<>();

        Resources localeRes = getResourcesByLocale(res, localeName);
        String[] strings = res.getStringArray(R.array.t3_lcd_string_list);
        for (int i = 0; i < strings.length; i++) {
            StringBitmapMode mode = new StringBitmapMode();
            mode.setName(strings[i]);
            mode.setText(localeRes.getString(localeRes.getIdentifier(strings[i], "string", getPackageName())));
            stringBitmapModes.add(mode);
        }

        return stringBitmapModes;
    }


    Resources getResourcesByLocale( Resources res, String localeName ) {
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = new Locale(localeName);
        return new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }

    private void resetLocale(Resources res){
        Configuration conf = new Configuration(res.getConfiguration());
        conf.locale = mCurLocale;
        new Resources(res.getAssets(), res.getDisplayMetrics(), conf);
    }


    private class ConvertTask extends AsyncTask<LocaleStringBitmapMode, Integer, Boolean> {

        private String bmpFileDir;
        private ArrayList<LocaleStringBitmapMode> localeStringBitmapModes;
        private int total;
        private int current = 0;

        public ConvertTask(String bmpFileDir, ArrayList<LocaleStringBitmapMode> localeStringBitmapModes, int total) {
            this.bmpFileDir = bmpFileDir;
            this.localeStringBitmapModes = localeStringBitmapModes;
            this.total = total;
        }

        @Override
        protected void onPreExecute() {
            LoadDialog.show(T3LanguageActivity.this, "开始..." + "0/" + total);
        }


        @Override
        protected Boolean doInBackground(LocaleStringBitmapMode... lists) {

            for (LocaleStringBitmapMode mode : localeStringBitmapModes) {
                new File(bmpFileDir + mode.getLocale()).mkdirs();

                for (StringBitmapMode mode1 : mode.getStrings()) {
                    Bitmap bitmap = createBitmapFromText(mode1.getText(), mode.getLocale());
                    saveBmpToFile(bitmap, bmpFileDir + mode.getLocale() + "/" + mode1.getName() + ".bmp");
                    publishProgress(current++);
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {
            LoadDialog.updateText("转换中..." + progresses[0] + "/" + total);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            LoadDialog.dismissDialog();
            PetkitToast.showToast("转换完成，路径为：" + bmpFileDir);
        }

        // 方法5：onCancelled()
        // 作用：将异步任务设置为：取消状态
        @Override
        protected void onCancelled() {


        }
    }



}
