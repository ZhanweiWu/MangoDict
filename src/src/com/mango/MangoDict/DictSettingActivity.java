package com.mango.MangoDict;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mango.MangoDict.ColorPicker.ColorPickerDialog;

public class DictSettingActivity extends Activity implements ColorPickerDialog.OnColorChangedListener{

	private final String TAG = "DictSettingActivity";

	private final int COLOR_BG = 0;
	private final int COLOR_TEXT = 1;
	private final int COLOR_WORD = 2;
	
	private MangoDictUtils mMangoDictUtils = null;
	private Button mDictPathBtn = null;
	private CheckBox mDictIsCaptureBox = null;
	private TextView mDictPathView = null;
	private TextView mSampleTextView = null;
	private TextView mWordTextView = null;
	private String mDictPath = null;
	private int mColorType = COLOR_BG;
    private int mTextColor = 0;
    private int mBgColor = 0;
    private int mWordColor = 0;

	private void SaveSetting() {
		MangoDictEng.mDictPath = mDictPath;
		MangoDictEng.mIsCapture = mDictIsCaptureBox.isChecked();
 
		MangoDictEng.mDictColor = MangoDictUtils.ColorToARGB(mTextColor) + ";"
								+ MangoDictUtils.ColorToARGB(mWordColor) + ";"
								+ MangoDictUtils.ColorToARGB(mBgColor);

		SharedPreferences settings = getSharedPreferences(
				MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(MangoDictEng.DICT_SETTING_PATH, MangoDictEng.mDictPath);
		editor.putBoolean(MangoDictEng.DICT_SETTING_ISCAPTURE, MangoDictEng.mIsCapture);
		editor.putString(MangoDictEng.DICT_SETTING_COLOR, MangoDictEng.mDictColor);

		editor.commit();

        Intent i = new Intent(Intent.ACTION_RUN);
        i.setClass(this, MangoDictService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(true == MangoDictEng.mIsCapture)
        	this.startService(i);
        else
        	this.stopService(i);

        mMangoDictUtils.initDicts();
        
		Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
	}

	public void onColorChanged(int color) {

		MyLog.v(TAG, "onColorChanged()::color=" + color);

		if(COLOR_BG == mColorType)
		{
			mBgColor = color;
			mSampleTextView.setBackgroundColor(mBgColor);
			mWordTextView.setBackgroundColor(mBgColor);
		}
		else if(COLOR_TEXT == mColorType)
		{
			mTextColor = color;
			mSampleTextView.setTextColor(mTextColor);
		}
		else
		{
			mWordColor = color;
			mWordTextView.setTextColor(mWordColor);			
		}
	}

	private void selectColor() {
		ColorPickerDialog picker = null;
		
		if(COLOR_BG == mColorType)
		{
			picker = new ColorPickerDialog(this, mBgColor);
			picker.setAlphaSliderVisible(true);
		}
		else if(COLOR_TEXT == mColorType)
		{
			picker = new ColorPickerDialog(this, mTextColor);
		}
		else
		{
			picker = new ColorPickerDialog(this, mWordColor);
		}

		picker.setOnColorChangedListener(this);
		picker.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		setContentView(R.layout.dict_setting);

		mMangoDictUtils = new MangoDictUtils(this);

		mTextColor = MangoDictUtils.GetTextColor();
		mWordColor = MangoDictUtils.GetWordColor();
		mBgColor = MangoDictUtils.GetBgColor();

		mDictPath = MangoDictEng.mDictPath;

		mDictPathBtn = (Button) findViewById(R.id.dictPathBtn);
		mDictPathView = (TextView) findViewById(R.id.dictPathTxt);
		mDictIsCaptureBox = (CheckBox) findViewById(R.id.dictIsCaptureBox);
		mDictPathView.setText(MangoDictEng.mDictPath);
		mDictIsCaptureBox.setChecked(MangoDictEng.mIsCapture);

		Button mBgColorBtn = (Button) findViewById(R.id.bgColorBtn);
		Button mTextColorBtn = (Button) findViewById(R.id.textColorBtn);
		Button mWordColorBtn = (Button) findViewById(R.id.wordColorBtn);
		mSampleTextView = (TextView) findViewById(R.id.sampleText);
		mWordTextView = (TextView) findViewById(R.id.wordText);

		// Initial the color
		mSampleTextView.setBackgroundColor(mBgColor);
		mSampleTextView.setTextColor(mTextColor);
		mWordTextView.setBackgroundColor(mBgColor);
		mWordTextView.setTextColor(mWordColor);


		mBgColorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mColorType = COLOR_BG;
				selectColor();
			}
		});
		
		mTextColorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mColorType = COLOR_TEXT;
				selectColor();
			}
		});
		
		mWordColorBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mColorType = COLOR_WORD;
				selectColor();
			}
		});
		
		mDictPathBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "mDictPathBtn.setOnClickListener()");

				Intent intent = new Intent(DictSettingActivity.this, PathSelectActivity.class);
				Bundle bundle = new Bundle();
				
				bundle.putString(PathSelectActivity.DEFAULT_PATH, MangoDictEng.mDictPath);
				bundle.putString(PathSelectActivity.CLASS_NAME, "DictSettingActivity");
				bundle.putInt(PathSelectActivity.SELECT_TYPE, PathSelectActivity.SELECT_TYPE_FOLDER);

				intent.putExtras(bundle);

				startActivityForResult(intent, PathSelectActivity.FILE_RESULT_CODE);
			}
		});
		
		ImageButton buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "buttonConfirm.onClick()");

				SaveSetting();
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

		mMangoDictUtils.destroy();

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (PathSelectActivity.FILE_RESULT_CODE == requestCode) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				mDictPath = bundle.getString("filePath");
				mDictPathView.setText(mDictPath);
			}
		}
	}
}
