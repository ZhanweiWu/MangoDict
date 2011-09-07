package com.mango.MangoDict;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DictWebViewClient extends WebViewClient {
	private final String TAG = "DictWebViewClient";

	private WebViewClientCallback mWebViewClientCallback = null;

	public DictWebViewClient(WebViewClientCallback wvCB) {
		mWebViewClientCallback = wvCB;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		MyLog.v(TAG, "shouldOverrideUrlLoading()::URL=" + url);

		if(url.startsWith(MangoDictEng.BWORD_URL))
		{
			String keyword = url.substring(MangoDictEng.BWORD_URL.length());

			if(null != mWebViewClientCallback)
			{
				mWebViewClientCallback.shouldOverrideUrlLoading(keyword);
			}

			MyLog.v(TAG, "shouldOverrideUrlLoading()::keyword=" + keyword);
		}

		return true;
	}
}
