package com.mango.MangoDict;

import java.io.File;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.Arrays;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class MemorizeSelectActivity extends ListActivity {

	private final String TAG = "MemorizeSelectActivity";

	private String   mMemorizePath = "";
	private String[] mMemorizeFiles = null;
	private String[] mMemorizeInfo = null;
	private String 	 mMemorizeName = "";
	private int      mMemorizeCnt = 0;
	private int      mCheckedIndex = -1;

	private MemorizeEng mMemorizeEng = null;
	
	private ListView mListView = null;
	
    private void getMemorizeList()
    {
		File f = new File(mMemorizePath);
		int cnt = 0;
		mMemorizeCnt = 0;

		if(!f.exists() || !f.isDirectory())
		{
			return;
		}

		File[] files = f.listFiles();
		cnt = files.length / 3;
		if(cnt <= 0 )
		{
			return;
		}

		String memorizeFiles[] = new String[cnt];

		for (int i = 0; i < files.length; i++) {
			if(files[i].isFile())
			{
				String fileName = files[i].getName();
				String extName = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();

				if(extName.equalsIgnoreCase("mem"))
				{
					memorizeFiles[mMemorizeCnt] = fileName.replace(".mem", "");
					mMemorizeCnt ++;
				}
			}
		}

		if (mMemorizeCnt > 0)
		{
			mCheckedIndex = -1;

		    Arrays.sort(memorizeFiles, Collator.getInstance(java.util.Locale.CHINA));

			mMemorizeFiles = new String[mMemorizeCnt];
			mMemorizeInfo = new String[mMemorizeCnt];
			for (int i = 0; i < mMemorizeCnt; i++)
			{
				mMemorizeFiles[i] = memorizeFiles[i];
				
				if(mMemorizeName.equalsIgnoreCase(mMemorizeFiles[i]))
				{
					mCheckedIndex = i;
				}

				String mfoFile = mMemorizePath + "/" + memorizeFiles[i] + ".mfo";
		    	int[] cardsProgress = mMemorizeEng.GetCardsProgressFromMfo(mfoFile);

	    		if(cardsProgress[1] > 0)
	    		{
		    		double progress = (double)cardsProgress[0] / (double)cardsProgress[1] * 100;
		    		progress = MangoDictUtils.roundDouble(progress, 2, BigDecimal.ROUND_HALF_UP);
		    		mMemorizeInfo[i] = mMemorizeFiles[i] + " (" + cardsProgress[0] + " / " + cardsProgress[1] + " [" + Double.toString(progress) + "%])";
	    		}
	    		else
	    		{
	    			mMemorizeInfo[i] = mMemorizeFiles[i];
	    		}
			}

        	setListAdapter(new ArrayAdapter<String>(this, R.layout.select_dialog_singlechoice, mMemorizeInfo));

        	if(mCheckedIndex >= 0)
        	{
        		mListView.setItemChecked(mCheckedIndex, true);
        	}
		}		
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

    	mMemorizeEng = MemorizeEng.createMemorizeEng();

        setContentView(R.layout.memorize_select);

		SharedPreferences settings = getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);
		MangoDictEng.mDictPath = settings.getString(MangoDictEng.DICT_SETTING_PATH, MangoDictEng.DICT_DEFAULT_PATH);
		mMemorizeName = settings.getString(MemorizeActivity.DICT_SETTING_MEMORIZE_NAME, "");
		mMemorizePath = MangoDictEng.mDictPath + "/" + MemorizeActivity.MEMORIZE_FOLDER;

		ImageButton buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "buttonConfirm.onClick()");

		        int position = mListView.getCheckedItemPosition();
		        if(AdapterView.INVALID_POSITION != position)
		        {
		        	String memorizeName = mMemorizeFiles[position];
					Intent data = new Intent(MemorizeSelectActivity.this, MemorizeActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("memorizeName", memorizeName);
					data.putExtras(bundle);
					setResult(2, data);
		        }
				finish();
			}
		});

		ImageButton buttonCancle = (ImageButton) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		ImageButton buttonGenerate = (ImageButton) findViewById(R.id.buttonGenerate);
		buttonGenerate.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "buttonGenerate.onClick()");
	        	Intent intent = new Intent(MemorizeSelectActivity.this, PathSelectActivity.class);

				Bundle bundle = new Bundle();
				
				bundle.putString(PathSelectActivity.DEFAULT_PATH, MangoDictUtils.getSDCardPath());
				bundle.putString(PathSelectActivity.CLASS_NAME, "MemorizeSelectActivity");
				bundle.putInt(PathSelectActivity.SELECT_TYPE, PathSelectActivity.SELECT_TYPE_FILE);

				intent.putExtras(bundle);

				startActivityForResult(intent, PathSelectActivity.FILE_RESULT_CODE);
			}
		});

        mListView = getListView();

        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        getMemorizeList();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (PathSelectActivity.FILE_RESULT_CODE == requestCode) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				String srcFilePath = bundle.getString("filePath");

				File f = new File(srcFilePath);
				if(f.exists() && f.isFile())
				{
					String fileName = f.getName();
					String srcUrl = srcFilePath;
					String dstUrl = mMemorizePath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".mem";

					File memorizeFolder = new File(mMemorizePath);
					if (!memorizeFolder.exists())
					{
						memorizeFolder.mkdirs();
					}

					MyLog.v(TAG, "onActivityResult()::srcUrl=" + srcUrl);
					MyLog.v(TAG, "onActivityResult()::dstURL=" + dstUrl);

					mMemorizeEng.GenerateMemorizeFile(srcUrl, dstUrl);

					getMemorizeList();
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()");
		
		mMemorizeEng.releaseMemorizeEng();

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		MyLog.v(TAG, "onPause()");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		MyLog.v(TAG, "onRestart()");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.v(TAG, "onResume()");
		super.onResume();
	}

	@Override
	protected void onStop() {
		MyLog.v(TAG, "onStop()");
		super.onStop();
	}
}
