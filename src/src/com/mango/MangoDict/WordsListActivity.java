package com.mango.MangoDict;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

public class WordsListActivity extends ListActivity implements Filter.FilterListener {

	private final String TAG = "WordsListActivity";

	public static final int WORDS_RESULT_CODE = 1;

	public static final String WORDS_TYPE = "wordsType";
	public static final int TYPE_HISWORDS = 0;
	public static final int TYPE_NEWWORDS = 1;

	private int mWordsType = TYPE_HISWORDS;
	WordsListAdapter mWordsListAdapter = null;
	private EditText mKeywordTxt = null;
	
	private ArrayList<String> mWordsArrayList = null;

    private void getWordsList()
    {
    	if(TYPE_HISWORDS == mWordsType)
    	{
    		mWordsArrayList = MangoDictActivity.mWordsFileUtilsHis.getArrayList();
    		setTitle(R.string.his_words);
    	}
    	else
    	{
    		mWordsArrayList = MangoDictActivity.mWordsFileUtilsNew.getArrayList();
    		setTitle(R.string.new_words);
    	}

    	mWordsListAdapter = new WordsListAdapter();
		setListAdapter(mWordsListAdapter);
    }
    
    public void onFilterComplete(int count) {

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

        setContentView(R.layout.words_list);

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		mWordsType = bundle.getInt(WORDS_TYPE);

		MyLog.v(TAG, "onCreate()::mWordsType = " + mWordsType);

		mKeywordTxt =  (EditText) findViewById(R.id.keywordTxt);
		mKeywordTxt.requestFocus();
		
		mKeywordTxt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String filterStr = s.toString();
        		Filter filter = mWordsListAdapter.getFilter();
            	if(filterStr.length() <= 0)
            	{
            		filter.filter(null);
            	}
            	else
            	{
            		filter.filter(filterStr, WordsListActivity.this);
            	}
            }
        });

        getWordsList();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String word = mWordsListAdapter.getItem(position);

		Intent data = new Intent(WordsListActivity.this, MangoDictActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("word", word);
		data.putExtras(bundle);
		setResult(2, data);

		finish();
	}

	class WordsListAdapter extends ArrayAdapter<String> {

		WordsListAdapter() {
			super(WordsListActivity.this, R.layout.simple_list_item_1, mWordsArrayList);
		}

		public ArrayList<String> getList() {
			return mWordsArrayList;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = (TextView)convertView;

			if (text == null) {
				LayoutInflater inflater = getLayoutInflater();
				text = (TextView) inflater.inflate(R.layout.simple_list_item_1, null);
			}

			if(mWordsArrayList.size() == 0)
				return null;

			text.setText(mWordsListAdapter.getItem(position));

			return (text);
		}
	}
}
