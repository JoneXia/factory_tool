package com.petkit.matetool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileUtils {

	
	@SuppressWarnings("resource")
	public static byte[] readFileByteContent(String filePath) throws IOException {
		File file = new File(filePath);

		long fileSize = file.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.out.println("file too big...");
			return null;
		}

		FileInputStream fi = new FileInputStream(file);

		byte[] buffer = new byte[(int) fileSize];

		int offset = 0;

		int numRead = 0;

		while (offset < buffer.length

		&& (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {

			offset += numRead;

		}

		// 确保所有数据均被读取

		if (offset != buffer.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());

		}

		fi.close();

		return buffer;
	}
	
	
	/**
	 * 读文件内容
	 * @param file
	 * @return
	 */
	public static String readFileToString(File file) {

		InputStreamReader inputReader = null;
		BufferedReader bufferReader = null;
		String result = null;
		try {
			InputStream inputStream = new FileInputStream(file);
			inputReader = new InputStreamReader(inputStream);
			bufferReader = new BufferedReader(inputReader);

			// 读取一行
			String line = null;
			StringBuffer strBuffer = new StringBuffer();

			while ((line = bufferReader.readLine()) != null) {
				strBuffer.append(line);
			}

			inputReader.close();
			bufferReader.close();
			
			result = strBuffer.toString();
		} catch (IOException e) {
			CustomLog.e("readFileToString", e.getMessage());
		} 
		return result;
	}
	
	/**
	 * 写文件，默认替换文件内容
	 * @param fileName
	 * @param message
	 */
	public static void writeStringToFile(String fileName, String message) {
		writeStringToFile(fileName, message, false);
	}
	
	
	/**
	 * 写文件
	 * @param fileName
	 * @param message
	 */
	public static void writeStringToFile(String fileName, String message, boolean append) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName, append);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 写文件
	 * @param fileName
	 * @param message
	 */
	public static void writeStringToFile(String fileName, byte[] message) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = message;
			fout.write(bytes);
			fout.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 从assets中读取文件
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		try {
			InputStreamReader inputReader = new InputStreamReader(context
					.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 
}
