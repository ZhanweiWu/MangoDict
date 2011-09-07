package com.mango.MangoDict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class DictSelectActivity extends ListActivity {

	private final String TAG = "DictSelectActivity";
	
	private final String DICT_FOLDER 	= "DictFolder";
	private final String DICT_BOOKNAME 	= "DictBookName";
	private final String DICT_CHECKED 	= "DictChecked";

	private MangoDictUtils mMangoDictUtils = null;
	private String mDictType = null;
	TouchInterceptor mTrackList = null;
	private DictSelectAdapter mDictAdapter = null;
	private ArrayList<Map<String, Object>> mDictInfoArray;

    private void getDictInfo() {
		MyLog.v(TAG, "getDictInfo()::Begin");

    	int k = 0;
		String dictsPath = MangoDictEng.mDictPath + "/dicts";

		String[] dictAllArray = null; 
		String dictChecked = null;

		mDictInfoArray = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;

		if(mDictType.equals(MangoDictEng.DICT_SETTING_INDEX_CHECKED))
		{
			dictChecked = MangoDictEng.mDictIndexChecked;
			dictAllArray = MangoDictEng.mDictIndexAll.split(";");
		}
		else if(mDictType.equals(MangoDictEng.DICT_SETTING_CAPTURE_CHECKED))
		{
			dictChecked = MangoDictEng.mDictCaptureChecked;
			dictAllArray = MangoDictEng.mDictCaptureAll.split(";");
		}
		else if(mDictType.equals(MangoDictEng.DICT_SETTING_MEMORIZE_CHECKED))
		{
			dictChecked = MangoDictEng.mDictMemorizeChecked;
			dictAllArray = MangoDictEng.mDictMemorizeAll.split(";");
		}

		if(dictAllArray.length > 0 && dictAllArray[0].length() <= 0)
			return;

		for (int i = 0; i < dictAllArray.length; i++) {
			String dictPath = dictsPath + "/" + dictAllArray[i];
			String dictName = MangoDictUtils.getDictName(dictPath);
			if(null == dictName)
				continue;

			String dictFolder = null;

			if(null != dictName)
			{
				String bookIfoPath = "";
				boolean bChecked = false;
				dictFolder = dictAllArray[i];
				bookIfoPath = dictPath + "/" + dictName + ".ifo";

				map = new HashMap<String, Object>();
				map.put(DICT_FOLDER, dictFolder);
				map.put(DICT_BOOKNAME, mMangoDictUtils.getBookName(bookIfoPath));

				if(dictChecked.indexOf(dictFolder) >= 0)
				{
					bChecked = true;
				}

				map.put(DICT_CHECKED, bChecked);

				mDictInfoArray.add(map);

				k++;
			}
		}

		MyLog.v(TAG, "getDictInfo()::End");
    }

    private void saveDictInfo() {
		MyLog.v(TAG, "saveDictInfo()");

		String dictAll = ""; 
		String dictChecked = "";

		for(int i = 0; i < mDictInfoArray.size(); i++)
		{
			boolean bChecked = Boolean.valueOf(mDictInfoArray.get(i).get(DICT_CHECKED).toString());
			String dictFolder = mDictInfoArray.get(i).get(DICT_FOLDER).toString();

			dictAll += dictFolder + ";";
			if(true == bChecked)
			{
				dictChecked += dictFolder + ";";
			}
		}

		if(mDictType.equals(MangoDictEng.DICT_SETTING_INDEX_CHECKED))
		{
			MangoDictEng.mDictIndexChecked = dictChecked;
			MangoDictEng.mDictIndexAll = dictAll;
		}
		else if(mDictType.equals(MangoDictEng.DICT_SETTING_CAPTURE_CHECKED))
		{
			MangoDictEng.mDictCaptureChecked = dictChecked;
			MangoDictEng.mDictCaptureAll = dictAll;
		}
		else if(mDictType.equals(MangoDictEng.DICT_SETTING_MEMORIZE_CHECKED))
		{
			MangoDictEng.mDictMemorizeChecked = dictChecked;
			MangoDictEng.mDictMemorizeAll = dictAll;
		}

		mMangoDictUtils.initDicts();
		
		Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

    private TouchInterceptor.DropListener mDropListener =
        new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
			Map<String, Object> item = mDictAdapter.getItem(from);
			mDictAdapter.remove(item);
			mDictAdapter.insert(item, to);
        }
    };

    /*
    private TouchInterceptor.RemoveListener mRemoveListener =
        new TouchInterceptor.RemoveListener() {
        public void remove(int which) {
			Map item = mDictAdapter.getItem(which);
			mDictAdapter.remove(item);
        }
    };
     */

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		boolean bChecked = !Boolean.valueOf(mDictInfoArray.get(position).get(DICT_CHECKED).toString());
		CheckedTextView item = (CheckedTextView) v.findViewById(R.id.checkedTextView);
		mDictInfoArray.get(position).put(DICT_CHECKED, bChecked);

		item.setChecked(bChecked);

		MyLog.v(TAG, "onListItemClick()::bChecked=" + bChecked);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

        setContentView(R.layout.dict_select);

		mMangoDictUtils = new MangoDictUtils(this);

        mTrackList = (TouchInterceptor)getListView();
        mTrackList.setDropListener(mDropListener);
        //mTrackList.setRemoveListener(mRemoveListener);

		ImageButton buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MyLog.v(TAG, "buttonConfirm.onClick()");

				saveDictInfo();
			}
		});

		ImageButton buttonCancle = (ImageButton) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		if(null != bundle)
			mDictType = bundle.getString(MangoDictEng.DICT_TYPE);

		MyLog.v(TAG, "onCreate()::mDictType=" + mDictType);

        //ListView listView = getListView();
        //listView.setItemsCanFocus(false);
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()::mDictType=" + mDictType);

        // clear the listeners so we won't get any more callbacks
        ListView lv = getListView();
        ((TouchInterceptor) lv).setDropListener(null);
        //((TouchInterceptor) lv).setRemoveListener(null);
		mMangoDictUtils.destroy();

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		MyLog.v(TAG, "onPause()::mDictType=" + mDictType);
		super.onPause();
	}

	@Override
	protected void onRestart() {
		MyLog.v(TAG, "onRestart()::mDictType=" + mDictType);
		super.onRestart();
	}

	@Override
	protected void onResume() {
		MyLog.v(TAG, "onResume()::mDictType=" + mDictType);

		getDictInfo();

		mDictAdapter = new DictSelectAdapter();
		setListAdapter(mDictAdapter);

		super.onResume();
	}

	@Override
	protected void onStop() {
		MyLog.v(TAG, "onStop()::mDictType=" + mDictType);
		super.onStop();
	}

	class DictSelectAdapter extends ArrayAdapter<Map<String, Object>> {

		DictSelectAdapter() {
			super(DictSelectActivity.this, R.layout.select_dialog_multichoice, mDictInfoArray);
		}

		public ArrayList<Map<String, Object>> getList() {
			return mDictInfoArray;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.select_dialog_multichoice, null);
			}

			if(mDictInfoArray.size() == 0)
				return null;

			CheckedTextView item = (CheckedTextView) row.findViewById(R.id.checkedTextView);
			item.setText(mDictInfoArray.get(position).get(DICT_BOOKNAME).toString());
			item.setChecked(Boolean.valueOf(mDictInfoArray.get(position).get(DICT_CHECKED).toString()));

			return (row);
		}
	}
}
