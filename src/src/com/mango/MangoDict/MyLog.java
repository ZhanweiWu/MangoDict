package com.mango.MangoDict;

import android.util.Log;


public class MyLog {
	private static final String MYTAG = "MangoDict";

	public static int v(String TAG, String log) {
		return Log.v(MYTAG, getLog(TAG, log));
	}

	public static int d(String TAG, String log) {
		return Log.d(MYTAG, getLog(TAG, log));
	}

	public static int i(String TAG, String log) {
		return Log.i(MYTAG, getLog(TAG, log));
	}

	public static int w(String TAG, String log) {
		return Log.w(MYTAG, getLog(TAG, log));
	}

	public static int e(String TAG, String log) {
		return Log.e(MYTAG, getLog(TAG, log));
	}

	private static String getLog(String TAG, String log) {
		return "[" + TAG + "]:" + log;
	}
}
