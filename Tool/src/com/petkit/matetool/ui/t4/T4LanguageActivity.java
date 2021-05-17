package com.petkit.matetool.ui.t4;

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
import com.petkit.matetool.ui.t4.utils.T4Utils;
import com.petkit.matetool.widget.LoadDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.petkit.matetool.ui.t3.utils.BitmapFormat.BITMAP_8_BIT_COLOR;

/**
 * 用于将字符串转换成位图，中文字体使用思源黑体，英文待定
 *
 * 更新字符串步骤：
 * 1. 在R.array.t4_lcd_string_list中增加key
 * 2. 在string.xml中增加对应的string，key和第一步的key一致
 * 3. 在本文件中的LocaleList中设置支持的语言，注意设置了的语言需要有对应的翻译
 *
 *
 *
 * Created by Jone on 17/9/14.
 */
public class T4LanguageActivity extends BaseActivity {

    private String[] LocaleList = new String[]{"zh_CN", "en_US"}; //, "zh_TW", "es_ES", "ko_KR", "it_IT", "ja_JP", "pt_PT", "de_DE", "fr_FR", "ru_RU"


    private Tester mTester;
    private String bmpFileDir;

    private EditText mEditText;
    private Locale mCurLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mTester = (Tester) savedInstanceState.getSerializable(T4Utils.EXTRA_T4_TESTER);
        } else {
            mTester = (Tester) getIntent().getSerializableExtra(T4Utils.EXTRA_T4_TESTER);
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
        if ("zh_CN".equalsIgnoreCase(locale) || "zh_TW".equalsIgnoreCase(locale)) {
            typeface = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansCN-Normal.ttf");
            paint.setTextSize(22);
        } else {
            typeface = Typeface.createFromAsset(getAssets(), "fonts/Open-Sans-2.ttf");
            paint.setTextSize(18);
        }

        paint.setTypeface(typeface);

        Paint.FontMetricsInt fm = paint.getFontMetricsInt();

        int width = (int)paint.measureText(text) + 2;
        int height = fm.descent - fm.ascent + 4;

        if ((width % 2) != 0) {
            width++;
        }

        if ((height % 2) != 0) {
            height++;
        }

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

    private void saveConfigFileForLocaleStringBitmapMode(LocaleStringBitmapMode mode, String file) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("pic_bin_name,length,x_size,y_size").append("\n");
        for (StringBitmapMode mode1 : mode.getStrings()) {
            stringBuffer.append(mode1.getConfigString()).append("\n");
        }

        FileUtils.writeStringToFile(file, stringBuffer.toString());
    }

    private void startConvert() {

        bmpFileDir = CommonUtils.getAppDirPath() + System.currentTimeMillis() + "/";
        if (new File(bmpFileDir).exists()) {
            new File(bmpFileDir).delete();
        }

        new File(bmpFileDir).mkdirs();

        ArrayList<LocaleStringBitmapMode> localeStringBitmapModes = getAllString();

        ConvertTask task = new ConvertTask(bmpFileDir, localeStringBitmapModes,
                getResources().getStringArray(R.array.t4_lcd_string_list).length * LocaleList.length);
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
        String[] strings = res.getStringArray(R.array.t4_lcd_string_list);
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
        if (localeName != null && localeName.contains("_")) {
            String[] params = localeName.split("_");
            conf.locale = new Locale(params[0], params[1]);
        } else {
            conf.locale = new Locale(localeName);
        }

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
            LoadDialog.show(T4LanguageActivity.this, "开始..." + "0/" + total);
        }


        @Override
        protected Boolean doInBackground(LocaleStringBitmapMode... lists) {

            for (LocaleStringBitmapMode mode : localeStringBitmapModes) {
                new File(bmpFileDir + mode.getLocale()).mkdirs();

                for (StringBitmapMode mode1 : mode.getStrings()) {
                    Bitmap bitmap = createBitmapFromText(mode1.getText(), mode.getLocale());

                    mode1.setWidth(bitmap.getWidth());
                    mode1.setHeight(bitmap.getHeight());
                    mode1.setSize((int) (bitmap.getWidth() * Math.ceil(bitmap.getHeight() / 8d)));

                    saveBmpToFile(bitmap, bmpFileDir + mode.getLocale() + "/" + mode1.getName() + ".bmp");
                    publishProgress(current++);
                }

                saveConfigFileForLocaleStringBitmapMode(mode, bmpFileDir + mode.getLocale() + String.format("/head_config_lan_%s.csv", mode.getLocale()));
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
