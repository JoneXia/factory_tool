package com.petkit.android.utils;

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


import com.loopj.android.http.AsyncHttpResponseHandler;
import com.petkit.android.http.AsyncHttpUtil;

import android.annotation.SuppressLint;
import android.content.Context;  
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cz.msebera.android.httpclient.Header;


/**
 * 日志文件类，初始化时上传上次的日志文件到服务器
 * 
 * @author Jone
 *
 */
@SuppressLint("SimpleDateFormat") 
public class LogcatStorageHelper {  
  
    private static LogcatStorageHelper INSTANCE = null;  
    private static String PATH_LOGCAT;  
    private LogDumper mLogDumper = null;  
    private int mPId;
    private String logPostUrl;
  
    /** 
     *  
     * 初始化目录 
     *  
     * */  
    public void init(Context context, String url) {  
    	logPostUrl = url;
        PATH_LOGCAT = CommonUtils.getAppCacheDirPath() + "/logs/";
        File file = new File(PATH_LOGCAT);  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
    }  
  
    public static LogcatStorageHelper getInstance(Context context, String url) {  
        if (INSTANCE == null) {  
            INSTANCE = new LogcatStorageHelper(context, url);  
        }  
        return INSTANCE;  
    }  


    public static void uploadLog(){
        if (INSTANCE != null) {
            INSTANCE.stop();
            INSTANCE.start();
        }
    }


    public static void addLog(String logData){
    	if (INSTANCE != null) {  
    		if (INSTANCE.mLogDumper != null)  {
    			INSTANCE.mLogDumper.addLog(logData);
        	}
    	}
    }
  
    private LogcatStorageHelper(Context context, String url) {  
        init(context, url);  
        mPId = android.os.Process.myPid();  
    }  
  
    public void start() {  
        checkUploadDebugFile();
        
        if (mLogDumper == null)  {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);  
        }
        if(!mLogDumper.isAlive()){
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
    
    private void checkUploadDebugFile(){
    	File dir = new File(PATH_LOGCAT);
    	String[] files = dir.list();
    	if(files != null && files.length > 0){
    		uploadDebugFile(files);
    	}
    }
  
    private void uploadDebugFile(String... filesname){
    	HashMap<String, String> paramsHashMap = new HashMap<>();
    	
    	HashMap<String, List<String>> files = new HashMap<>();
    	List<String> listsStrings = new ArrayList<>();
    	final Collection<File> files2 = new ArrayList<>();
    	for(int i = 0; i < filesname.length; i++){
    		if(mLogDumper == null || !mLogDumper.getCurFileName().equals(filesname[i])){
    			files2.add(new File(PATH_LOGCAT + filesname[i]));
    		}
    	}
    	final String zipfileString = PATH_LOGCAT + System.currentTimeMillis() +".zip";
    	try {
			ZipUtils.zipFiles(files2, new File(zipfileString));
			listsStrings.add(zipfileString);
			for(File tempFile : files2){
//				if(!tempFile.getAbsolutePath().endsWith("zipFile.zip")){
					tempFile.delete();
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
    	
    	files.put("logfile", listsStrings);
    	AsyncHttpUtil.post(logPostUrl, paramsHashMap, files, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				String responseResult = new String(responseBody);
				PetkitLog.d("onSuccess: " + responseResult);
				
				if(responseResult != null && responseResult.contains("success")){
					new File(zipfileString).delete();
				}
			}

			@Override
			public void readCacheMessage() {
			}

			@Override
			public void setCacheFilePathString(String arg0) {
			}

			@Override
			public void writeCacheMessage(byte[] arg0) {
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
			}
    	});
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
        
        public void addLog(String logData){
        	if(mHandler != null){
        		Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putString(LOG_DATA, logData);
                msg.setData(b);
                
                mHandler.sendMessage(msg);
        	}
        }
  
        private boolean initFile(){
        	try {  
            	File outFile = new File(dir, getFileName() + ".log");
            	int i = 1;
            	while(outFile.exists()){
            		outFile = new File(dir, getFileName() + "-" + (i++) + ".log");
            	}
            	curFileName = outFile.getName();
                out = new FileOutputStream(outFile);  
                
                if(out != null){
                	try {
                		out.write("****************************************************************************\n".getBytes());
						out.write((getDateEN() + "  " + Build.MODEL  + "  SDK VERSION: " + Build.VERSION.RELEASE + 
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
        
        public String getCurFileName(){
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
  
        @SuppressLint("HandlerLeak") @Override  
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
     
  
}  