package com.petkit.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.petkit.android.http.AsyncHttpUtil;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


/**
 * 日志文件类，初始化时上传上次的日志文件到服务器
 *
 * @author Jone
 */
@SuppressLint("SimpleDateFormat")
public class LogcatStorageHelper {

    private final static int MAX_RETRY_TIMES = 1;
    private final static String tag = "LogcatStorageHelper";
    private static LogcatStorageHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    private String logPostUrl;
    private String qiNiuUrl;
    private boolean httpSucced;

    private Context context;
    private String qiNiuKey, qiNiuToken;
    private int fileCount, retryTimes;   //上传至七牛的文件数量
    private String zipfileString;
    private String qiNiuResult;
    private QiNiuResultRecord qiNiuResultRecord = null;
    private int resultCount;  //sharedpreference数据记录器

    private Collection<File> mUploadingFiles;

    /**
     * 初始化目录
     */
    public void init(Context context, String logPostUrl, String qiNiuUrl) {
        this.logPostUrl = logPostUrl;
        this.qiNiuUrl = qiNiuUrl;
        this.context = context;
        PATH_LOGCAT = CommonUtils.getAppCacheDirPath() + "/logs/";
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static LogcatStorageHelper getInstance(Context context, String logPostUrl, String qiNiuUrl) {
        if (INSTANCE == null) {
            synchronized (LogcatStorageHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LogcatStorageHelper(context, logPostUrl, qiNiuUrl);
                }
            }
        }
        return INSTANCE;
    }

    public static void uploadLog() {
        if (INSTANCE != null) {
            INSTANCE.stop();
            INSTANCE.start();
        }
    }


    public static void addLog(String logData) {
        if (INSTANCE != null) {
            if (INSTANCE.mLogDumper != null) {
                INSTANCE.mLogDumper.addLog(logData);
            }
        }
    }

    private LogcatStorageHelper(Context context, String logPostUrl, String qiNiuUrl) {
        init(context, logPostUrl, qiNiuUrl);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        checkUploadDebugFile();

        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        }
        if (!mLogDumper.isAlive()) {
            try {
                mLogDumper.start();
            } catch (IllegalThreadStateException e) {
            }
        }
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private void checkUploadDebugFile() {
        File dir = new File(PATH_LOGCAT);
        String[] files = dir.list();
        if (files != null && files.length > 0) {
            uploadDebugFile(files);
        }
    }

    private void getQiNiuLogToken() {
        if (retryTimes > MAX_RETRY_TIMES || CommonUtils.isEmpty(qiNiuUrl)) {
            processFailed();
            return;   //失败重连次数超过最大限制
        }
        HashMap<String, String> param = new HashMap<>();
        param.put("type", "other");
        param.put("namespace", "applog");
        param.put("count", fileCount + "");
        httpSucced = false;
        AsyncHttpUtil.post(qiNiuUrl, param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                String responseResult = new String(bytes);
                try {
                    JSONObject jsonObject = new JSONObject(responseResult);
                    JSONArray jsonArray = jsonObject.optJSONArray("result");
                    jsonObject = jsonArray.getJSONObject(0);
                    qiNiuKey = jsonObject.optString("key");
                    qiNiuToken = jsonObject.optString("token");
                    httpSucced = true;
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (!httpSucced) {
                    retryTimes++;
                    getQiNiuLogToken();
                } else {
                    retryTimes = 0;
                    uploadLogZipToQiNiu();
                }
            }
        });
    }

    private void uploadDebugFile(String... filesname) {
        List<String> listsStrings = new ArrayList<>();
        final Collection<File> files2 = new ArrayList<>();
        for (int i = 0; i < filesname.length; i++) {
            if (mLogDumper == null || !mLogDumper.getCurFileName().equals(filesname[i])) {
                files2.add(new File(PATH_LOGCAT + filesname[i]));
            }
        }
        zipfileString = PATH_LOGCAT + System.currentTimeMillis() + ".zip";
        try {
            ZipUtils.zipFiles(files2, new File(zipfileString));
            listsStrings.add(zipfileString);
            mUploadingFiles = files2;
//			for(File tempFile : files2){
//                tempFile.delete();
//			}
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        fileCount = listsStrings.size();
        retryTimes = 0;
        getQiNiuLogToken();
    }

    private void uploadLogZipToQiNiu() {
        if ((zipfileString == null) || (retryTimes > MAX_RETRY_TIMES)){
            processFailed();
            return;
        }
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(zipfileString, qiNiuKey, qiNiuToken, new UpCompletionHandler() {
            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    qiNiuResult = jsonObject.toString().replaceAll("\\\\", "");
                    new File(zipfileString).delete();
                    qiNiuResultRecord = QiNiuResultRecord.getInstance(context);   // 保存七牛返回的zip上传成功数据
                    qiNiuResultRecord.writeQiNiuResult(qiNiuResult);
                    retryTimes = 0;
                    uploadQiNiuResultToApi();
                } else {
                    retryTimes++;
                    uploadLogZipToQiNiu();
                }
            }
        }, null);
    }

    private void uploadQiNiuResultToApi() {
//		if(retryTimes>=MAX_RETRY_TIMES) return;
        Map<String, String> qiNiuResults = qiNiuResultRecord.readQiNiuResults();
        for (resultCount = 0; resultCount < qiNiuResultRecord.MAX_COUNT; resultCount++) {
            qiNiuResult = qiNiuResults.get(qiNiuResultRecord.RECORD + resultCount);
            if ((qiNiuResult != null) && (qiNiuResult.length() > 0)) {
                httpReuqest(qiNiuResult, resultCount);
            }
        }
    }

    private void httpReuqest(final String qiNiuResult, final int resultCount) {
        if (retryTimes > MAX_RETRY_TIMES || CommonUtils.isEmpty(logPostUrl)) {
            processFailed();
            return;
        }

        Map<String, String> param = new HashMap<>();
        param.put("log", qiNiuResult);
        AsyncHttpUtil.post(logPostUrl, param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(bytes));
                    String result = jsonObject.optString("result", " ");
                    if (result.equals("success")) {
                        qiNiuResultRecord.deleteQiNiuResult(qiNiuResultRecord.RECORD + resultCount);

                        if (mUploadingFiles != null) {
                            for (File tempFile : mUploadingFiles) {
                                tempFile.delete();
                            }
                            mUploadingFiles = null;
                        }
                    } else {
                        retryTimes++;
                        httpReuqest(qiNiuResult, resultCount);
                    }
                } catch (JSONException e) {
                    retryTimes++;
                    httpReuqest(qiNiuResult, resultCount);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                retryTimes++;
                httpReuqest(qiNiuResult, resultCount);
            }
        }, false);
    }

    private void processFailed() {
        if(zipfileString != null) {
            new File(zipfileString).delete();
            zipfileString = null;
        }

        if(mUploadingFiles != null) {
            mUploadingFiles = null;
        }
    }


    private class LogDumper extends Thread {

        public static final String LOG_DATA = "LOG_DATA";

        private boolean mRunning = true;
        private FileOutputStream out = null;
        private String dir;
        private String curFileName;

        private Handler mHandler;

        public LogDumper(String pid, String dir) {
            this.dir = dir;
            initFile();
        }

        public void addLog(String logData) {
            if (mHandler != null) {
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putString(LOG_DATA, logData);
                msg.setData(b);

                mHandler.sendMessage(msg);
            }
        }

        private boolean initFile() {
            try {
                File outFile = new File(dir, getFileName() + ".log");
                int i = 1;

                boolean legal = false;
                while (!legal) {
                    if(outFile.exists()) {
                        outFile = new File(dir, getFileName() + "-" + (i++) + ".log");
                    } else {
                        if(mUploadingFiles != null) {
                            for (File tempFile : mUploadingFiles) {
                                if(tempFile.equals(outFile)) {
                                    outFile = new File(dir, getFileName() + "-" + (i++) + ".log");
                                    continue;
                                }
                            }
                        }

                        legal = true;
                    }
                }


                curFileName = outFile.getName();
                out = new FileOutputStream(outFile);

                if (out != null) {
                    try {
                        out.write("****************************************************************************\n".getBytes());
                        out.write((getDateEN() + "  " + Build.MODEL + "  SDK VERSION: " + Build.VERSION.RELEASE +
                                "  APP VERSION: " + CommonUtils.getAppVersionName(CommonUtils.getAppContext()) + "\n").getBytes());
                        out.write("****************************************************************************\n".getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        public String getCurFileName() {
            return curFileName;
        }

        public void stopLogs() {
            mRunning = false;

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {

            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    // process incoming message here

                    if (!mRunning) {
                        return;
                    }

                    if (out == null) {
                        if (!initFile()) {
                            return;
                        }
                    }

                    try {
                        String data = msg.getData().getString(LOG_DATA);

                        if (data != null && data.length() > 0 && out != null) {
                            out.write((getDateEN() + "  " + data + "\n")
                                    .getBytes());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };

            Looper.loop();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31  
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31  
    }


    /**
     * 将上传zip至七牛后返回的结果保存至sharedpreference中
     */
    private static class QiNiuResultRecord {
        private static QiNiuResultRecord qiNiuResultRecord;
        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;
        public static final String RECORD = "record";
        public static final int MAX_COUNT = 3;

        private QiNiuResultRecord(Context context) {
            sharedPreferences = context.getSharedPreferences("qiNiuResultRecord", Context.MODE_APPEND);
            editor = sharedPreferences.edit();
            editor.putInt("count", 0);   //result记录数目
        }

        public static QiNiuResultRecord getInstance(Context context) {
            if (qiNiuResultRecord == null) {
                qiNiuResultRecord = new QiNiuResultRecord((context));
            }
            return qiNiuResultRecord;
        }

        public void writeQiNiuResult(String result) {
            int count = sharedPreferences.getInt("count", 0);
            editor.putString(RECORD + count, result);
            count = ++count % MAX_COUNT;
            editor.putInt("count", count);
            editor.commit();
        }

        public Map<String, String> readQiNiuResults() {
            Map<String, String> results = new HashMap<>();
            for (int i = 0; i < MAX_COUNT; i++) {
                results.put(RECORD + i, sharedPreferences.getString(RECORD + i, ""));
            }
            return results;
        }

        public void deleteQiNiuResult(String key) {
            if (sharedPreferences.contains(key)) {
                editor.remove(key);
                editor.commit();
            }
        }
    }


}  