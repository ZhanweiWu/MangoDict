package com.mango.MangoDict;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class DictSettingTabActivity extends TabActivity {

	private final String TAG = "DictSettingTabActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		Intent intent = null;
		TabHost tabHost = getTabHost();

		intent = new Intent();
		intent.setClass(this, DictSettingActivity.class);
		tabHost.addTab(tabHost
				.newTabSpec("dictSetting")
				.setIndicator(getResources().getString(R.string.setting),
						getResources().getDrawable(R.drawable.ic_btn_setting))
				.setContent(intent));

		intent = new Intent();
		intent.setClass(this, DictSelectActivity.class);
		intent.putExtra(MangoDictEng.DICT_TYPE, MangoDictEng.DICT_SETTING_INDEX_CHECKED);
		tabHost.addTab(tabHost
				.newTabSpec("dictIndex")
				.setIndicator(getResources().getString(R.string.index),
						getResources().getDrawable(R.drawable.ic_btn_setting))
				.setContent(intent));

		intent = new Intent();
		intent.setClass(this, DictSelectActivity.class);
		intent.putExtra(MangoDictEng.DICT_TYPE, MangoDictEng.DICT_SETTING_CAPTURE_CHECKED);
		tabHost.addTab(tabHost
				.newTabSpec("dictCapture")
				.setIndicator(getResources().getString(R.string.capture),
						getResources().getDrawable(R.drawable.ic_btn_setting))
				.setContent(intent));

		intent = new Intent();
		intent.setClass(this, DictSelectActivity.class);
		intent.putExtra(MangoDictEng.DICT_TYPE, MangoDictEng.DICT_SETTING_MEMORIZE_CHECKED);
		tabHost.addTab(tabHost
				.newTabSpec("dictMemoryze")
				.setIndicator(getResources().getString(R.string.memorize),
						getResources().getDrawable(R.drawable.ic_btn_setting))
				.setContent(intent));
	}

}
