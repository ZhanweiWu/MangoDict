package com.mango.MangoDict;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MangoDictActivity extends Activity {
	private final String TAG = "MangoDictActivity";

	final int POPUPWORDSLIST_TIMER = 1000;

	final int LIST_WORDS_NORMAL 	= 0;
	final int LIST_WORDS_FUZZY 		= 1;
	final int LIST_WORDS_PATTERN 	= 2;
	final int LIST_WORDS_FULLTEXT 	= 3;

	final int MENU_PLAYWORD 		= 0;
	final int MENU_PLAYTEXT 		= MENU_PLAYWORD + 1;
	final int MENU_HISWORDS 		= MENU_PLAYWORD + 2;
	final int MENU_NEWWORDS	 		= MENU_PLAYWORD + 3;
	final int MENU_MEMORIZE 		= MENU_PLAYWORD + 4;
	final int MENU_SETTING 			= MENU_PLAYWORD + 5;
	final int MENU_FINDONPAGE		= MENU_PLAYWORD + 6;
	final int MENU_ADDWORD			= MENU_PLAYWORD + 7;

	public static WordsFileUtils mWordsFileUtilsHis = null;
	public static WordsFileUtils mWordsFileUtilsNew = null;

	private MangoDictUtils mMangoDictUtils = null;
	private DictSpeechEng mDictSpeechEng = null;
	
	private LinearLayout mParentViewLayout = null;
	private WebView mDictContentView = null;

	private ImageButton mDictSearchBtn = null;
	private ImageButton mDictWordsListBtn = null;
	private ImageButton mDictWordsViewBtn = null;	// View the last words list.
	private LinearLayout mDictSearchBarLayout = null;
	private LinearLayout mDictToolBarLayout = null;	
	
    private DictEditTextView mDictKeywordView = null;
    private PopupWindow mDictKeywordsPopup = null;
    private DropDownListView mDictKeywordsPopupList = null; 
    private int mPopupItemCount = 0; 
    private int mDictContentViewHeight = 0;
    private Handler mPopupWordsListHandler = null;
    private Runnable mPopupWordsListRunnable = null;
    private boolean	 mReplaceKeyword = false;

    private ListWordsTask listWordsTask = null;
    private boolean mIsTaskRunning = false;
    private ProgressDialog mProgressDialog = null;
    private static Handler mProgressCBHandler = null;

	private int mNumberOfMatches = 0;
    private String mReadmeHtml = null;

	public class MyWebViewClientCallback extends WebViewClientCallback {
		public MyWebViewClientCallback() {
			MyLog.v(TAG, "MyWebViewClientCallback()");
		}

		public void shouldOverrideUrlLoading(String word) {
			MyLog.v(TAG, "MyWebViewClientCallback()::shouldOverrideUrlLoading()");

			makeDictContent(word);
		}
	}

	private void makeDictContent(String word) {
		String htmlContent = mMangoDictUtils.generateHtmlContent(word, MangoDictEng.DICT_TYPE_INDEX);

		mMangoDictUtils.showHtmlContent(htmlContent, mDictContentView);

    	if(null != word && word.length() > 0)
    	{
        	mWordsFileUtilsHis.addWord(word);
    	}
	}

	private void showSearchContent()
	{
		String keyword = mDictKeywordView.getText().toString();

		if (keyword.length() <= 0) {
			mMangoDictUtils.showHtmlContent(mReadmeHtml, mDictContentView);
		}else if(keyword.charAt(0) == '/') {
			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.fuzzy_query_prompt), mDictContentView);
		}else if(keyword.charAt(0) == ':'){
			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.fulltext_query_prompt), mDictContentView);
		}else if((keyword.indexOf('*') >= 0) ||  (keyword.indexOf('?') >= 0)){
			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.pattern_query_prompt), mDictContentView);
		} else {
	        makeDictContent(keyword);
		}
	}

	public static void lookupProgressCB(int progress)
	{
		Message m = Message.obtain();
		m.arg1 = progress;
		m.setTarget(mProgressCBHandler);
		m.sendToTarget();
	}

	private void showProgressDialog()
	{
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMessage(getResources().getString(R.string.keywords_search));
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(getResources().getString(R.string.cancel), 
				                  new DialogInterface.OnClickListener() {
			 public void onClick(DialogInterface dialog, int i) {
				mMangoDictUtils.CancelLookup();
				dialog.cancel();
			 }
			});
		mProgressDialog.show();
	}

	private void startKeywordsList()
	{
		int listType = LIST_WORDS_NORMAL;
		String keyword = mDictKeywordView.getText().toString();
		keyword = keyword.trim();

		if(keyword.length() <= 0)
		{
			if(mDictKeywordsPopup.isShowing())
				mDictKeywordsPopup.dismiss();

			mMangoDictUtils.showHtmlContent(mReadmeHtml, mDictContentView);	// Show the readme information.
			return;
		}

		if((keyword.charAt(0) == '/') || (keyword.charAt(0) == ':') || (keyword.indexOf('*') >= 0) ||  (keyword.indexOf('?') >= 0))
		{
			if(keyword.charAt(0) == '/')
			{
				keyword = keyword.substring(1);
				listType = LIST_WORDS_FUZZY;
			}
			else if(keyword.charAt(0) == ':')
			{
				keyword = keyword.substring(1);
				listType = LIST_WORDS_FULLTEXT;
			}
			else
			{
				listType = LIST_WORDS_PATTERN;
			}
		}

    	if(true == mIsTaskRunning)	// One task is running.
    	{
    		return;
    	}
    	else
    	{
    		mIsTaskRunning = true;

    		if(LIST_WORDS_NORMAL != listType)
    			showProgressDialog();

    		listWordsTask = new ListWordsTask(keyword, listType);
    		listWordsTask.execute();
    	}
	}

	private void setPopupHeight() {
		MyLog.v(TAG, "setPopupHeight()");
		
		ListAdapter listAdapter = mDictKeywordsPopupList.getAdapter();
		View listItem = listAdapter.getView(0, null, mDictKeywordsPopupList);
		listItem.measure(0, 0);

		int height = listItem.getMeasuredHeight() * mPopupItemCount;
		mDictContentViewHeight = mDictContentView.getHeight();
		if (height > mDictContentViewHeight)
			height = mDictContentViewHeight;

		mDictKeywordsPopup.setHeight(height);
	}
	
	private void showKeywordsList(String[] strWordsList) {

		ArrayAdapter<String> keywordsAdapter = new ArrayAdapter<String>(
											   this, R.layout.simple_dropdown_item_1line, strWordsList);
		mDictKeywordsPopupList.setAdapter(keywordsAdapter);

		mPopupItemCount = strWordsList.length;
		setPopupHeight();

		mDictKeywordsPopup.setWidth(mDictKeywordView.getWidth());

		MyLog.v(TAG, "startKeywordsList()::Show KeywordsList");

		if (!mDictKeywordsPopup.isShowing()) {
			mDictKeywordsPopup.showAsDropDown(mDictKeywordView);
		}
	}

    private void updateMatchesString() {
    	int findIndex = 0;

    	if(mNumberOfMatches > 0)
    		mDictToolBarLayout.findViewById(R.id.matches_view).setVisibility(View.VISIBLE);
    	else
    	{
    		mDictToolBarLayout.findViewById(R.id.matches_view).setVisibility(View.GONE);
    		return;
    	}

        try
        {
            Method m = WebView.class.getMethod("findIndex");
            findIndex = (Integer) m.invoke(mDictContentView);
        }
        catch (Throwable ignored){}

        String template = getResources().
                getQuantityString(R.plurals.matches_found, mNumberOfMatches, findIndex + 1, mNumberOfMatches);

        ((TextView)mDictToolBarLayout.findViewById(R.id.matches)).setText(template);
    }

	private void showFindBar()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        LinearLayout webviewFindLayout = (LinearLayout)factory.inflate(R.layout.webview_find, null);

        EditText editText = (EditText)webviewFindLayout.findViewById(R.id.edit);
        ImageButton prevBtn = (ImageButton)webviewFindLayout.findViewById(R.id.previous);
        ImageButton nextBtn = (ImageButton)webviewFindLayout.findViewById(R.id.next);
        ImageButton doneBtn = (ImageButton)webviewFindLayout.findViewById(R.id.done);

        try
        {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(mDictContentView, true);
        } catch (Throwable ignored){}

        editText.addTextChangedListener(new TextWatcher() {
        	private void disableButtons()
        	{
        		mDictToolBarLayout.findViewById(R.id.previous).setEnabled(false);
        		mDictToolBarLayout.findViewById(R.id.next).setEnabled(false);
        	}

        	private void enableButtons()
        	{
        		mDictToolBarLayout.findViewById(R.id.previous).setEnabled(true);
        		mDictToolBarLayout.findViewById(R.id.next).setEnabled(true);
        	}

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String findStr = s.toString();
            	if(findStr.length() <= 0)
            	{
            		disableButtons();
            		mDictToolBarLayout.findViewById(R.id.matches_view).setVisibility(View.GONE);
            		mDictContentView.clearMatches();
            	}
            	else
            	{
            		int found = mDictContentView.findAll(findStr);

            		mNumberOfMatches = found;
            		if(found < 2)
            		{
            			disableButtons();
            		}
            		else
            		{
            			enableButtons();
            		}
            	}

            	updateMatchesString();
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mDictContentView.findNext(false);
            	updateMatchesString();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mDictContentView.findNext(true);
            	updateMatchesString();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try
                {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(mDictContentView, false);
                } catch (Throwable ignored){}

        		mDictContentView.clearMatches();
                mDictToolBarLayout.removeAllViews();
                mDictToolBarLayout.setVisibility(View.GONE);
                mDictSearchBarLayout.setVisibility(View.VISIBLE);
                mDictKeywordView.requestFocus();
            }
        });

        mDictToolBarLayout.addView(webviewFindLayout, 0, new LayoutParams(0, LayoutParams.FILL_PARENT, 1));
        mDictToolBarLayout.setVisibility(View.VISIBLE);
        mDictSearchBarLayout.setVisibility(View.GONE);
        editText.requestFocus();
    }

	private void InitPopupWindow()
	{
        mDictKeywordsPopupList = new DropDownListView(this);
        mDictKeywordsPopupList.setFocusable(true);
        mDictKeywordsPopupList.setFocusableInTouchMode(true);
        mDictKeywordsPopupList.mListSelectionHidden = false;

        mDictKeywordsPopup = new PopupWindow(this);
        mDictKeywordsPopup.setContentView(mDictKeywordsPopupList);
        mDictKeywordsPopup.setTouchable(true);
        mDictKeywordsPopup.setFocusable(false);
        mDictKeywordsPopup.setOutsideTouchable(true);
        
    	mDictKeywordsPopupList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

				TextView textView = (TextView) v;
				String keyword = textView.getText().toString();
				
				mReplaceKeyword = true;  	// Don't response the onTextChanged event this time.

				mDictKeywordView.setText(keyword);

				// make sure we keep the caret at the end of the text view
		        Editable spannable = mDictKeywordView.getText();
		        Selection.setSelection(spannable, spannable.length());

				mDictKeywordsPopup.dismiss();
				showSearchContent();
			}
		});

		mPopupWordsListHandler = new Handler();
		mPopupWordsListRunnable = new Runnable() {
			public void run() {
        		MyLog.v(TAG, "mPopupWordsListRunnable::run()::PopupWordsList Handler");
                startKeywordsList();
			}
		};

	}

    private void startService()
    {
        Intent i = new Intent(Intent.ACTION_RUN);
        i.setClass(this, MangoDictService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(true == MangoDictEng.mIsCapture)
        	this.startService(i);
        else
        	this.stopService(i);
    }

    //-----------------------------------------------------------------------------------------------------//
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

       	mMangoDictUtils = new MangoDictUtils(this);
        mMangoDictUtils.initDicts();
    	mDictSpeechEng = new DictSpeechEng(this);
    	mWordsFileUtilsHis = new WordsFileUtils(MangoDictEng.mDictPath + "/" + WordsFileUtils.WORDSLIST_FOLDER, 
    										 WordsFileUtils.HISWORDS_FILENAME);

    	mWordsFileUtilsNew = new WordsFileUtils(MangoDictEng.mDictPath + "/" + WordsFileUtils.WORDSLIST_FOLDER, 
				 WordsFileUtils.NEWWORDS_FILENAME);

        startService();

        mReadmeHtml = getResources().getString(R.string.readme_text);

        mDictToolBarLayout = (LinearLayout)findViewById(R.id.dictToolBarLayout);
        mDictSearchBarLayout = (LinearLayout)findViewById(R.id.dictSearchBarLayout);
        mDictKeywordView = new DictEditTextView(this);
        mDictKeywordView.setBackgroundResource(R.drawable.textfield_default);
        mDictKeywordView.setPadding(3, 3, 3, 3);
        mDictKeywordView.setGravity(Gravity.CENTER_VERTICAL);
        mDictKeywordView.setSingleLine();
        mDictSearchBarLayout.setGravity(Gravity.CENTER_VERTICAL);
        mDictSearchBarLayout.addView(mDictKeywordView, 0, new LayoutParams(0, LayoutParams.FILL_PARENT, 1));
        mDictKeywordView.requestFocus();

        int bgColor = MangoDictUtils.GetBgColor();
        mParentViewLayout = (LinearLayout) findViewById(R.id.parentView);
        mParentViewLayout.setBackgroundColor(bgColor);
        
        mDictContentView = (WebView) findViewById(R.id.dictContentView);

        mDictContentView.setWebViewClient(new DictWebViewClient(new MyWebViewClientCallback()));
        mDictContentView.setBackgroundColor(bgColor);   
        
        mDictContentView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(!mDictContentView.hasFocus())
					mDictContentView.requestFocus();
				return false;
			}
        });

        WebSettings webSettings = mDictContentView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        mDictSearchBtn = (ImageButton) findViewById(R.id.dictSearchBtn);
        mDictWordsListBtn = (ImageButton) findViewById(R.id.dictWordsListBtn);
        mDictWordsViewBtn = (ImageButton) findViewById(R.id.dictWordsViewBtn);

        InitPopupWindow();

        mProgressCBHandler = new Handler() {
        	public void handleMessage(Message msg) {
        		int progress = msg.arg1;
        		
        		if(null != mProgressDialog)
        			mProgressDialog.setProgress(progress);
        	}
        };

        mMangoDictUtils.showHtmlContent(mReadmeHtml, mDictContentView);	// Show the readme information.

        mDictSearchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSearchContent();
            }
        });

        mDictWordsListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startKeywordsList();
            }
        });

        mDictWordsViewBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		if (null != mDictKeywordsPopupList.getAdapter() && 
        		    !mDictKeywordsPopupList.getAdapter().isEmpty() && !mDictKeywordsPopup.isShowing()) {
        			
        			if(mDictContentViewHeight != mDictContentView.getHeight())
        			{
        				setPopupHeight();
        			}
        			mDictKeywordsPopup.showAsDropDown(mDictKeywordView);
        		}
            }
        });
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mDictKeywordsPopup.isShowing())
			mDictKeywordsPopup.dismiss();

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_PLAYWORD, 0, R.string.play_word).setIcon(R.drawable.ic_btn_speech);
		menu.add(0, MENU_PLAYTEXT, 0, R.string.play_text).setIcon(R.drawable.ic_btn_speech);
		menu.add(0, MENU_ADDWORD, 0, R.string.add_word).setIcon(R.drawable.ic_btn_addword);
		menu.add(0, MENU_HISWORDS, 0, R.string.his_words).setIcon(R.drawable.ic_btn_wordslist);
		menu.add(0, MENU_NEWWORDS, 0, R.string.new_words).setIcon(R.drawable.ic_btn_wordslist);
		menu.add(0, MENU_FINDONPAGE, 0, R.string.findonpage).setIcon(R.drawable.ic_btn_findonpage);
		menu.add(0, MENU_SETTING, 0, R.string.setting).setIcon(R.drawable.ic_btn_setting);
		menu.add(0, MENU_MEMORIZE, 0, R.string.memorize).setIcon(R.drawable.ic_btn_memorize);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
        switch (itemId) {
	        case MENU_PLAYWORD:
	        {
				Intent intent = new Intent(this, WordRecordActivity.class);
				Bundle bundle = new Bundle();
				
				bundle.putString(WordRecordActivity.RECORD_WORD, mDictKeywordView.getText().toString());

				intent.putExtras(bundle);

				startActivityForResult(intent, WordRecordActivity.RECORD_RESULT_CODE);
	        	break;
	        }

	        case MENU_PLAYTEXT:
	        {
	        	ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	    		if(clipboardManager.hasText())
	    		{
		        	mDictSpeechEng.speak(clipboardManager.getText().toString());
	    		}
	        	break;
	        }

	        case MENU_HISWORDS:
	        case MENU_NEWWORDS:
	        {
	        	int type = WordsListActivity.TYPE_HISWORDS;
				if(MENU_NEWWORDS == itemId)
				{
					type = WordsListActivity.TYPE_NEWWORDS;
				}

				Intent intent = new Intent(MangoDictActivity.this, WordsListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt(WordsListActivity.WORDS_TYPE, type);
				intent.putExtras(bundle);
				startActivityForResult(intent, WordsListActivity.WORDS_RESULT_CODE);
	        	break;
	        }
	        
	        case MENU_MEMORIZE:
	        {
	        	Intent intent = new Intent();
                intent.setClass(this, MemorizeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
	        	break;
	        }

	        case MENU_SETTING:
	        {
	        	Intent intent = new Intent();
                intent.setClass(this, DictSettingTabActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
	        	break;
	        }
	        
	        case MENU_FINDONPAGE:
	        {
            	showFindBar();
	        	break;
	        }

	        case MENU_ADDWORD:
	        {
            	String word = mDictKeywordView.getText().toString();

            	if(null != word && word.length() > 0)
            	{
	            	mWordsFileUtilsNew.addWord(word);
            	}
	        	break;
	        }
        }
        
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (WordsListActivity.WORDS_RESULT_CODE == requestCode) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				String word = bundle.getString("word");
				MyLog.v(TAG, "onActivityResult()::word=" + word);
				
				mReplaceKeyword = true;  	// Don't response the onTextChanged event this time.

				mDictKeywordView.setText(word);

				// make sure we keep the caret at the end of the text view
		        Editable spannable = mDictKeywordView.getText();
		        Selection.setSelection(spannable, spannable.length());

				showSearchContent();
			}
		}
	}

	@Override
	protected void onPause() {
		MyLog.v(TAG, "onPause()");

		mPopupWordsListHandler.removeCallbacks(mPopupWordsListRunnable);

		super.onPause();
	}


	@Override
	protected void onResume() {
		MyLog.v(TAG, "onResume()");

        int bgColor = MangoDictUtils.GetBgColor();
        mParentViewLayout.setBackgroundColor(bgColor);
        mDictContentView.setBackgroundColor(bgColor);

		super.onResume();
	}


	@Override
	protected void onStart() {
		MyLog.v(TAG, "onStart()");
		super.onStart();
	}


	@Override
	protected void onStop() {
		MyLog.v(TAG, "onStop()");
		super.onStop();
	}


	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()");

		if(mDictKeywordsPopup.isShowing())
			mDictKeywordsPopup.dismiss();

        mDictContentView.destroy();
        mDictContentView = null;

		mMangoDictUtils.destroy();

		mDictSpeechEng.destroy();
		mDictSpeechEng = null;

		mWordsFileUtilsNew.save();
		mWordsFileUtilsNew = null;
		
		mWordsFileUtilsHis.save();
		mWordsFileUtilsHis = null;

		super.onDestroy();
	}

    //-----------------------------------------------------------------------------------------------------//

    private class ListWordsTask extends AsyncTask<Void, Void, String[]> {

    	private String keyword = null;
    	private int listType = LIST_WORDS_NORMAL;

    	public ListWordsTask(String keyword, int listType)
    	{
    		this.keyword = keyword;
    		this.listType = listType;
    	}

        @Override
        protected String[] doInBackground(Void... params) {
        	String strWordsList[] = null;

			switch (listType) {
				case LIST_WORDS_NORMAL: {
					strWordsList = mMangoDictUtils.listWords(keyword);
					break;
				}

				case LIST_WORDS_FUZZY: {
					strWordsList = mMangoDictUtils.fuzzyListWords(keyword);
					break;
				}

				case LIST_WORDS_PATTERN: {
					strWordsList = mMangoDictUtils.patternListWords(keyword);
					break;
				}

				case LIST_WORDS_FULLTEXT:{
					strWordsList = mMangoDictUtils.fullTextListWords(keyword);
					break;
				}
			} // End switch (listType) {       	

            return strWordsList;
        }

        @Override
        protected void onPostExecute(String[] strWordsList) {

        	mIsTaskRunning = false;	// Task has stopped.
        	
        	if(null != mProgressDialog && mProgressDialog.isShowing())
        		mProgressDialog.cancel();

        	// Haven't got any words, not show the words list popup window.
			if(null == strWordsList || strWordsList.length <= 0)
			{
				if(LIST_WORDS_NORMAL != listType)
				{
					mMangoDictUtils.showHtmlContent(getResources().getString(R.string.spell_error), mDictContentView);					
				}

				if(mDictKeywordsPopup.isShowing())
					mDictKeywordsPopup.dismiss();

				return;
			} // if(null == strWordsList || strWordsList.length <= 0)

			showKeywordsList(strWordsList);
        }
    }

    //-----------------------------------------------------------------------------------------------------//
    
    // Extend classes.
    private class DictEditTextView extends EditText {
        public DictEditTextView(Context context) {
            super(context, null);
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mDictKeywordsPopup.isShowing()) {
                // special case for the back key, we do not even try to send it
                // to the drop down list but instead, consume it immediately
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    getKeyDispatcherState().startTracking(event, this);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    getKeyDispatcherState().handleUpEvent(event);
                    if (event.isTracking() && !event.isCanceled()) {
                    	mDictKeywordsPopup.dismiss();
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }

		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			if(mDictKeywordsPopup.isShowing())
			{
				if (keyCode != KeyEvent.KEYCODE_SPACE
						&& (mDictKeywordsPopupList.getSelectedItemPosition() >= 0
								|| (keyCode != KeyEvent.KEYCODE_ENTER && keyCode != KeyEvent.KEYCODE_DPAD_CENTER))) {
					mDictKeywordsPopupList.onKeyUp(keyCode, event);
				}

				switch (keyCode) {
				// avoid passing the focus from the text view to the next component
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					return true;
				}
			}

			return super.onKeyUp(keyCode, event);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(mDictKeywordsPopup.isShowing())
			{
				if (keyCode != KeyEvent.KEYCODE_SPACE
						&& (mDictKeywordsPopupList.getSelectedItemPosition() >= 0
								|| (keyCode != KeyEvent.KEYCODE_ENTER && keyCode != KeyEvent.KEYCODE_DPAD_CENTER))) {
					mDictKeywordsPopupList.requestFocusFromTouch();
					mDictKeywordsPopupList.onKeyDown(keyCode, event);
				}

				switch (keyCode) {
				// avoid passing the focus from the text view to the next component
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
					mDictKeywordsPopupList.mListSelectionHidden = false;
					return true;
				}
			}
			else
			{
				if (KeyEvent.KEYCODE_ENTER == keyCode || KeyEvent.KEYCODE_DPAD_CENTER == keyCode)
				{
					showSearchContent();
				}
			}

			return super.onKeyDown(keyCode, event);
		}

		@Override
		protected void onTextChanged(CharSequence text, int start, int before, int after) {
			if (null != mDictKeywordsPopup)
			{
				mPopupWordsListHandler.removeCallbacks(mPopupWordsListRunnable);

				if(mReplaceKeyword == true)
				{
					mReplaceKeyword = false;
				}
				else
				{
					String keyword = text.toString();

	        		MyLog.v(TAG, "onTextChanged()::keywords changed");

	                if(keyword.length() > 0 && keyword.charAt(0) == '/')
	                {
	        			if(mDictKeywordsPopup.isShowing())
	        				mDictKeywordsPopup.dismiss();

	                	// show fuzzy_query_prompt string.
	        			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.fuzzy_query_prompt), mDictContentView);
	                }
	                else if(keyword.length() > 0 && keyword.charAt(0) == ':')
	                {
	        			if(mDictKeywordsPopup.isShowing())
	        				mDictKeywordsPopup.dismiss();

	                	// show fulltext_query_prompt string.
	        			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.fulltext_query_prompt), mDictContentView);	                	
	                }
	                else if(keyword.length() > 0 && ((keyword.indexOf('*') >= 0) ||  (keyword.indexOf('?') >= 0)))
	                {
	        			if(mDictKeywordsPopup.isShowing())
	        				mDictKeywordsPopup.dismiss();

	                	// show pattern_query_prompt string.
	        			mMangoDictUtils.showHtmlContent(getResources().getString(R.string.pattern_query_prompt), mDictContentView);	                	
	                }
	                else
	                {
	                	mMangoDictUtils.showHtmlContent("<br> <br>", mDictContentView);
	                	mPopupWordsListHandler.postDelayed(mPopupWordsListRunnable, POPUPWORDSLIST_TIMER);
	                }
				}
			}

			super.onTextChanged(text, start, before, after);
		}
    }

    //-----------------------------------------------------------------------------------------------------//
    
    private class DropDownListView extends ListView {
        private boolean mListSelectionHidden;

        public DropDownListView(Context context) {
            super(context, null, android.R.attr.dropDownListViewStyle);
        }

        @Override
        public boolean isInTouchMode() {
            return mListSelectionHidden || super.isInTouchMode();
        }

        @Override
        public boolean hasWindowFocus() {
            return true;
        }

        @Override
        public boolean isFocused() {
            return true;
        }

        @Override
		public boolean onTouchEvent(MotionEvent ev) {
        	mListSelectionHidden = true;
			return super.onTouchEvent(ev);
		}

		@Override
        public boolean hasFocus() {
            return true;
        }
    }
}
