package com.mango.MangoDict;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

public class MemorizeSettingActivity extends Activity {

	private final String TAG = "MemorizeSettingActivity";

	private MemorizeEng mMemorizeEng = null;

	private EditText mGrade0Cards = null;
	private EditText mMaxNewCards = null;
	private EditText mMaxCards = null;
	private CheckBox mRandomNewCards = null;

	private void SaveSetting() {
		int[] settings = new int[4]; 
		
		settings[0] = Integer.parseInt(mGrade0Cards.getText().toString());
		settings[1] = Integer.parseInt(mMaxNewCards.getText().toString());
		settings[2] = Integer.parseInt(mMaxCards.getText().toString());
		if(mRandomNewCards.isChecked())
		{
			settings[3] = 1;
		}
		else
		{
			settings[3] = 0;
		}

		mMemorizeEng.SetSettings(settings);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.memorize_setting);

    	mMemorizeEng = MemorizeEng.createMemorizeEng();

		mGrade0Cards = (EditText) findViewById(R.id.grade0Cards);
		mMaxNewCards = (EditText) findViewById(R.id.maxNewCards);
		mMaxCards = (EditText) findViewById(R.id.maxCards);
		mRandomNewCards = (CheckBox) findViewById(R.id.randomNewCards);

		int[] settings = mMemorizeEng.GetSettings();
		mGrade0Cards.setText(Integer.toString(settings[0]));
		mMaxNewCards.setText(Integer.toString(settings[1]));
		mMaxCards.setText(Integer.toString(settings[2]));
		if(settings[3] > 0)
		{
			mRandomNewCards.setChecked(true);
		}
		else
		{
			mRandomNewCards.setChecked(false);
		}

		ImageButton buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "buttonConfirm.onClick()");

				SaveSetting();
				finish();
			}
		});

		ImageButton buttonCancle = (ImageButton) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()");

		mMemorizeEng.releaseMemorizeEng();

		super.onDestroy();
	}
}
