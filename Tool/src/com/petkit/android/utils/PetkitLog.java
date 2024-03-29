/**
 * Copyright (c) 2013 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.petkit.android.utils;

import android.content.Context;
import android.util.Log;


/** 
 * PetkitLog 
 * @author Jone
 */
public final class PetkitLog {
	/** Used to tag logs */
	//@SuppressWarnings("unused")
	private static final String TAG = "Petkit";
	
    //------------------------------------------------------
    // Private constructor for utility class
    private PetkitLog() {
        throw new UnsupportedOperationException("Sorry, you cannot instantiate an utility class!");
    }
    //------------------------------------------------------
	
    private static boolean sLogEnabled = true;

    public static boolean isLogEnabled(){
        return sLogEnabled;
    }
    
    /**
     * Initialize the DebugLog by enabling it if the debuggable attribute is set in the manifest, or disabling it
     * otherwise.
     * @param context
     */
    public static void init(Context context) {
    	sLogEnabled = CommonUtils.isDebuggable(context);
    	if(sLogEnabled) {
    		Log.w(TAG, "=========================================================================");
    		Log.w(TAG, "= /!\\ Warning: DEBUGGABLE IS TRUE -> DEBUG LOG ENABLED.");
    	}
    }
    
    /**
     * print a {@link Log#DEBUG} log message ONLY if the app is debuggable. Use that instead of the regular Log.d
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param format The format string for the message being logged.
     * @param args Arguments of the format string.
     */
    public static void d(String tag, String format, Object... args) {
        if(sLogEnabled) {
        	Log.d(tag, String.format(format, args));
        }
    }
    
    /**
     * print a {@link Log#DEBUG} log message ONLY if the app is debuggable. Use that instead of the regular Log.d
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param text The text for the message being logged.
     */
    public static void d(String tag, String text) {
        if(sLogEnabled) {
        	Log.d(tag, text);
        }
    }
    
    public static void d(String text){
    	d(TAG, text);
    }
    
    /**
     * 
     * @param tag
     * @param text
     */
    public static void e(String tag, String text){
    	if(sLogEnabled){
    		Log.e(tag, text);
    	}
    }
    
    /**
     * 
     * @param tag
     * @param text
     */
    public static void w(String tag, String text){
    	if(sLogEnabled){
    		Log.w(tag, text);
    	}
    }

    /**
     *
     * @param tag
     * @param text
     */
    public static void i(String tag, String text){
    	if(sLogEnabled){
    		Log.i(tag, text);
    	}
    }

    /**
     * 使用“类名_方法名_行数“作为tag打印日志
     * @param string 需要打印的信息（一般）
     */

    public static void info(String string)
    {
        if(sLogEnabled) Log.i(getLogTag(),string);
    }

    /**
     * 使用“类名_方法名_行数“作为tag打印日志
     * @param string 需要打印的信息(警告)
     */
    public static void warn(String string) {
        if(sLogEnabled) Log.w(getLogTag(), string);
    }

    /**
     ** 使用“类名_方法名_行数“作为tag打印日志
     * @param string 需要打印的信息（错误）
     */
    public static void er(String string) {
        if(sLogEnabled) Log.e(getLogTag(),string);
    }

    /**
     * 获取打印log的语句的类名+方法名+行数，将其作为tag
     * @return “类名_方法名_行数“
     */
    private static String getLogTag()
    {
        try
        {
            Exception exception = new Exception();
            StackTraceElement[] elements = exception.getStackTrace();
            if(elements.length<3)
            {
                return "LogTag_error";
            }
            String className = elements[2].getClassName();
            int index = className.lastIndexOf(".");
            className = className.substring(index+1);
            String tag = className+"_"+elements[2].getMethodName()+"_"+elements[2].getLineNumber();
            return tag;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return "LogTag_error";
        }
    }
}
