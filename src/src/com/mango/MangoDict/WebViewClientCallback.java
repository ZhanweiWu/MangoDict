package com.mango.MangoDict;

public class WebViewClientCallback {
	private final String TAG = "MangoDictService";
	
	public WebViewClientCallback() {
		MyLog.v(TAG, "WebViewClientCallback()");
	}
	
	public void shouldOverrideUrlLoading(String word) {
		MyLog.v(TAG, "WebViewClientCallback()::shouldOverrideUrlLoading()");
	}
}
