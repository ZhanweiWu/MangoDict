package com.mango.MangoDict;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PathSelectActivity extends ListActivity {
	private final String TAG = "PathSelectActivity";
	
	public static final int FILE_RESULT_CODE = 1;
	public static final int SELECT_TYPE_FOLDER 	= 0x000;
	public static final int SELECT_TYPE_FILE 	= 0x001;

	public static final String DEFAULT_PATH		= "DEFAULT_PATH";
	public static final String SELECT_TYPE		= "SELECT_TYPE";
	public static final String CLASS_NAME		= "CLASS_NAME";
	
	private String rootPath = MangoDictUtils.getSDCardPath();
	private String curPath = null;

	private TextView mDictPath = null;
    private String mFilePath = null;
	private List<String> items = null;
	private List<String> paths = null;

	private int mSelectType = 0;
	private String mClassName = null;
	private Activity mActivity = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		MyLog.v(TAG, "onCreate()");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.file_select);
		mActivity = this;

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		if(null != bundle)
		{
			curPath = bundle.getString(DEFAULT_PATH);
			mClassName = bundle.getString(CLASS_NAME);
			mSelectType = bundle.getInt(SELECT_TYPE);
		}

		if(null == curPath)
		{
			curPath = rootPath;
		}
		if(null == mClassName)
		{
			mClassName = "DictSettingActivity";
		}
		if(0 == mSelectType)
		{
			mSelectType = SELECT_TYPE_FOLDER;
		}

		MyLog.v(TAG, "onCreate()::curPath = " + curPath);
		MyLog.v(TAG, "onCreate()::mClassName = " + mClassName);
		MyLog.v(TAG, "onCreate()::mSelectType = " + mSelectType);

		mDictPath = (TextView) findViewById(R.id.dictPath);
		ImageButton buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);

		buttonConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String filePath = null;
				if(SELECT_TYPE_FILE == mSelectType)
				{
					if(null == mFilePath)
					{
						finish();
						return;
					}
					
					filePath = mFilePath;
				}
				else
				{
					filePath = curPath;
				}

				Intent intent = new Intent();
				intent.setClassName(mActivity, mClassName);

				Bundle bundle = new Bundle();
				bundle.putString("filePath", filePath);

				intent.putExtras(bundle);
				setResult(2, intent);

				finish();
			}
		});

		ImageButton buttonCancle = (ImageButton) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

		getFileDir(curPath);
	}

	private void getFileDir(String filePath) {
		File f = new File(filePath);

		if(!f.exists())
		{
			f = new File(MangoDictEng.DICT_DEFAULT_PATH);
		}

		if(!f.canRead())
			return;

		mDictPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();

		File[] files = f.listFiles();

		if (!rootPath.startsWith(filePath)) {
			items.add("b1");
			paths.add(rootPath);
			
			if(null != f.getParent())
			{
				items.add("b2");
				paths.add(f.getParent());
			}
		}

		if (f.exists()) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if(file.canRead())
				{
					items.add(file.getName());
					paths.add(file.getPath());
				}
			}
		}

		setListAdapter(new FileManagerAdapter(this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String filePath = paths.get(position);
		File file = new File(filePath);
		if (file.isDirectory()) {
			curPath = paths.get(position);
			getFileDir(curPath);
			mFilePath = null;
		}
		else if(SELECT_TYPE_FILE == mSelectType)
		{
			mDictPath.setText(filePath);
			mFilePath = filePath;
		}
	}

	// Adapter
	private class FileManagerAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mIcon1;
		private Bitmap mIcon2;
		private Bitmap mIcon3;
		private Bitmap mIcon4;
		private List<String> items;
		private List<String> filePaths;

		public FileManagerAdapter(Context context, List<String> it,
				List<String> pa) {
			mInflater = LayoutInflater.from(context);
			items = it;
			filePaths = pa;
			mIcon1 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_btn_folderback);
			mIcon2 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_btn_folderback);
			mIcon3 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_btn_folder);
			mIcon4 = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_btn_file);
		}

		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.file_row, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			File f = new File(filePaths.get(position).toString());

			if (items.get(position).toString().equals("b1")) {
				holder.text.setText("  /");
				holder.icon.setImageBitmap(mIcon1);
			} else if (items.get(position).toString().equals("b2")) {
				holder.text.setText("  ..");
				holder.icon.setImageBitmap(mIcon2);
			} else {
				holder.text.setText("  " + f.getName());
				if (f.isDirectory()) {
					holder.icon.setImageBitmap(mIcon3);
				} else {
					holder.icon.setImageBitmap(mIcon4);
				}
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text;
			ImageView icon;
		}
	}
}