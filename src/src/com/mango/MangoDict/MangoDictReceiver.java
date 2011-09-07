package com.mango.MangoDict;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MangoDictReceiver extends BroadcastReceiver {
	private static final String TAG = "MangoDictReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLog.v(TAG, "onReceive()");

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(Intent.ACTION_RUN);
			i.setClass(context, MangoDictService.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			SharedPreferences preferenceSettings = context.getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);
			MangoDictEng.mIsCapture = preferenceSettings.getBoolean(MangoDictEng.DICT_SETTING_ISCAPTURE, false);

	        if(true == MangoDictEng.mIsCapture)
	        	context.startService(i);

			MyLog.v(TAG, "startService()");
        }

		MyLog.v(TAG, action);
	}
}
