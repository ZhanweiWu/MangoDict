package com.mango.MangoDict;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class MemorizeActivity extends Activity {
	private final String TAG = "MemorizeActivity";

	public static String DICT_SETTING_MEMORIZE_NAME = "MemorizeName";
	final static String MEMORIZE_FOLDER = "memorize";

	final int MEMORIZE_RESULT_CODE 	= 1;

	final int MENU_MEMORIZE_SCHEDULED	= 0;
	final int MENU_MEMORIZE_NEWCARD 	= 1;
	final int MENU_MEMORIZE_LEARNAHEAD	= 2;
	final int MENU_MEMORIZE_PLAYWORD	= 3;
	final int MENU_MEMORIZE_SCHEDULE	= 4;
	final int MENU_MEMORIZE_STATISTIC	= 5;
	final int MENU_MEMORIZE_SELECT 		= 6;
	final int MENU_MEMORIZE_SETTING 	= 7;


	final int MEMORIZE_STATUS_NONE 			= -1;
	final int MEMORIZE_STATUS_QUESTION		= 0;
	final int MEMORIZE_STATUS_ANSWER		= 1;

	public static MemorizeEng mMemorizeEng = null;
	private MangoDictUtils mMangoDictUtils = null;
	
	private String mMemorizeName = null;
	private String mMemorizeFilePath = null;

	private int mMemorizeStatus = MEMORIZE_STATUS_NONE;
	private int mCardType = MemorizeEng.BUILD_QUEUE_NONE;
	private String mQuestion = null;

	private LinearLayout mAnswerBarLayout = null;
	private TableLayout mGradeBarLayout = null;
	private Button mShowAnswerBtn = null;
	private TextView mMemorizeTxtView = null;
	private TextView mMemorizeStatusView = null;
	private LinearLayout mParentViewLayout = null;
	private WebView mDictContentView = null;


	public class MyWebViewClientCallback extends WebViewClientCallback {
		public MyWebViewClientCallback() {
			MyLog.v(TAG, "MyWebViewClientCallback()");
		}

		public void shouldOverrideUrlLoading(String word) {
			MyLog.v(TAG, "MyWebViewClientCallback()::shouldOverrideUrlLoading()");
		}
	}

	private void changeStatus()
	{
		MyLog.v(TAG, "changeStatus()::mMemorizeStatus=" + mMemorizeStatus);
		MyLog.v(TAG, "changeStatus()::mCardType=" + mCardType);

		if(MEMORIZE_STATUS_NONE == mMemorizeStatus)
		{
			mGradeBarLayout.setVisibility(View.GONE);
			mAnswerBarLayout.setVisibility(View.VISIBLE);

			if (MemorizeEng.BUILD_QUEUE_NONE == mCardType || MemorizeEng.BUILD_QUEUE_LEARNAHEAD == mCardType)
			{
				mShowAnswerBtn.setText(R.string.memorizeselect);
			}
			else if (MemorizeEng.BUILD_QUEUE_SCHEDULED == mCardType)
			{
				mShowAnswerBtn.setText(R.string.learnnewcard);
			}
			else if (MemorizeEng.BUILD_QUEUE_NEWCARD == mCardType)
			{
				mShowAnswerBtn.setText(R.string.learnaheadcard);
			}
		}
		else if(MEMORIZE_STATUS_QUESTION == mMemorizeStatus)
		{
			mGradeBarLayout.setVisibility(View.GONE);
			mAnswerBarLayout.setVisibility(View.VISIBLE);
			mShowAnswerBtn.setText(R.string.showanswer);			
		}
		else if(MEMORIZE_STATUS_ANSWER == mMemorizeStatus)
		{
			mGradeBarLayout.setVisibility(View.VISIBLE);
			mAnswerBarLayout.setVisibility(View.GONE);	
		}
	}

	private void rebuildRevisionQueue(int queueType, int resId)
	{
		boolean bSuccess = false;

		bSuccess = mMemorizeEng.RebuildRevisionQueue(queueType);

		mCardType = queueType;

		MyLog.v(TAG, "rebuildRevisionQueue()::queueType=" + queueType);

		if(true == bSuccess)
		{
			newQuestion();
			mMemorizeStatus = MEMORIZE_STATUS_QUESTION;
		}
		else
		{
			mMemorizeTxtView.setText("");
			mMemorizeStatusView.setText("");
			mMemorizeStatus = MEMORIZE_STATUS_NONE;
			mMangoDictUtils.showHtmlByResId(resId, mDictContentView);
		}

		changeStatus();
	}

	private void initMemorize()
	{
		MyLog.v(TAG, "initMemorize()::mCardType=" + mCardType);

		if (MemorizeEng.BUILD_QUEUE_SCHEDULED == mCardType)
		{
			MyLog.v(TAG, "initMemorize()::mMemorizeFilePath=" + mMemorizeFilePath);

			if(true == mMemorizeEng.LoadMemorizeFile(mMemorizeFilePath))
			{
				rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_SCHEDULED, R.string.noschedulecard);
			}
			else	// Load Memorize file failed.
			{
				mCardType = MemorizeEng.BUILD_QUEUE_NONE;
			}
		}

		changeStatus();
	}

	private void showQuestion()
	{
		MyLog.v(TAG, "showQuestion()");
		
		String StatusText = "";

		mMangoDictUtils.showHtmlContent(" <br><br> ", mDictContentView);

		mMemorizeStatus = MEMORIZE_STATUS_QUESTION;

		mQuestion = mMemorizeEng.GetMemorizeWord();

		mMemorizeTxtView.setText(mQuestion);

		if(MemorizeEng.BUILD_QUEUE_SCHEDULED == mCardType)
		{
			StatusText = getResources().getString(R.string.learnscheduledcard);
		}
		else if(MemorizeEng.BUILD_QUEUE_NEWCARD == mCardType)
		{
			StatusText = getResources().getString(R.string.learnnewcard);
		}
		else if(MemorizeEng.BUILD_QUEUE_LEARNAHEAD == mCardType)
		{
			StatusText = getResources().getString(R.string.learnaheadcard);
		}
		else
		{
			StatusText = "";
		}

		if(!StatusText.equals(""))
		{
			int[] infoData = mMemorizeEng.GetScheduleStatus();
			StatusText += ":" + infoData[0] + "/" + infoData[1];
		}

		mMemorizeStatusView.setText(StatusText);

		changeStatus();
	}

	private void showAnswer()
	{
		MyLog.v(TAG, "showAnswer()");
		
		mMemorizeStatus = MEMORIZE_STATUS_ANSWER;

		String htmlContent = mMangoDictUtils.generateHtmlContent(mQuestion, MangoDictEng.DICT_TYPE_MEMORIZE);
		mMangoDictUtils.showHtmlContent(htmlContent, mDictContentView);

		changeStatus();
	}

	private void gradeAnswer(int grade)
	{
		MyLog.v(TAG, "showAnswer()::grade=" + grade);

		mMemorizeEng.GradeAnswer(grade);

		newQuestion();
	}

	private void newQuestion()
	{
		MyLog.v(TAG, "newQuestion()");

		if(true == mMemorizeEng.NewQuestion())
		{
			showQuestion();
		}
		else
		{
			mMemorizeStatus = MEMORIZE_STATUS_NONE;
			mMemorizeTxtView.setText("");
			mMemorizeStatusView.setText("");

			if(MemorizeEng.BUILD_QUEUE_SCHEDULED == mCardType)
			{
				mMangoDictUtils.showHtmlByResId(R.string.noschedulecard, mDictContentView);
			}
			else if(MemorizeEng.BUILD_QUEUE_NEWCARD == mCardType)
			{
				mMangoDictUtils.showHtmlByResId(R.string.nonewcard, mDictContentView);
			}
			else if(MemorizeEng.BUILD_QUEUE_LEARNAHEAD == mCardType)
			{
				mMangoDictUtils.showHtmlByResId(R.string.nocard, mDictContentView);				
			}

			changeStatus();
		}
	}

	private void selectMemorize()
	{
		MyLog.v(TAG, "selectMemorize()");

    	Intent intent = new Intent();
        intent.setClass(this, MemorizeSelectActivity.class);
		startActivityForResult(intent, MEMORIZE_RESULT_CODE);	
	}

    //-----------------------------------------------------------------------------------------------------//

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.memorize);

		MyLog.v(TAG, "onCreate()");

    	mMemorizeEng = MemorizeEng.createMemorizeEng();
       	mMangoDictUtils = new MangoDictUtils(this);
        mMangoDictUtils.initDicts();
        
    	mAnswerBarLayout = (LinearLayout)findViewById(R.id.answerBarLayout);
    	mGradeBarLayout = (TableLayout)findViewById(R.id.gradeBarLayout);
        mDictContentView = (WebView) findViewById(R.id.dictContentView);
        mMemorizeTxtView = (TextView) findViewById(R.id.memorizeTxtView);
        mMemorizeStatusView = (TextView) findViewById(R.id.memorizeStatusView);

        int textColor = MangoDictUtils.GetTextColor();
        mMemorizeTxtView.setTextColor(textColor);
        mMemorizeStatusView.setTextColor(textColor);
        
        int bgColor = MangoDictUtils.GetBgColor();
        mParentViewLayout = (LinearLayout) findViewById(R.id.parentView);
        mParentViewLayout.setBackgroundColor(bgColor);
        
        mShowAnswerBtn = (Button) findViewById(R.id.showAnswerBtn);

        Button gradeABtn = (Button) findViewById(R.id.gradeABtn);
        Button gradeBBtn = (Button) findViewById(R.id.gradeBBtn);
        Button gradeCBtn = (Button) findViewById(R.id.gradeCBtn);
        Button gradeDBtn = (Button) findViewById(R.id.gradeDBtn);
        Button gradeEBtn = (Button) findViewById(R.id.gradeEBtn);
        Button gradeFBtn = (Button) findViewById(R.id.gradeFBtn);

        mDictContentView.setWebViewClient(new DictWebViewClient(new MyWebViewClientCallback()));
        mDictContentView.setBackgroundColor(bgColor);
        
        WebSettings webSettings = mDictContentView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        mShowAnswerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
        		MyLog.v(TAG, "mShowAnswerBtn.onClick()::mMemorizeStatus=" + mMemorizeStatus);
        		MyLog.v(TAG, "mShowAnswerBtn.onClick()::mCardType=" + mCardType);

            	if(MEMORIZE_STATUS_QUESTION == mMemorizeStatus)
        		{
        			showAnswer();
        		}
            	else if(MEMORIZE_STATUS_NONE == mMemorizeStatus)
            	{
            		if(MemorizeEng.BUILD_QUEUE_NONE == mCardType || MemorizeEng.BUILD_QUEUE_LEARNAHEAD == mCardType)
            		{
            			selectMemorize();
            		}
            		else if(MemorizeEng.BUILD_QUEUE_SCHEDULED == mCardType)
            		{
            			rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_NEWCARD, R.string.nonewcard);
            		}
            		else if(MemorizeEng.BUILD_QUEUE_NEWCARD == mCardType)
            		{
            			rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_LEARNAHEAD, R.string.nocard);
            		}
            	}
            }
        });

        gradeABtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(5);
            }
        });

        gradeBBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(4);
            }
        });

        gradeCBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(3);
            }
        });

        gradeDBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(2);
            }
        });

        gradeEBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(1);
            }
        });

        gradeFBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	gradeAnswer(0);
            }
        });

		SharedPreferences settings = getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);
		mMemorizeName = settings.getString(DICT_SETTING_MEMORIZE_NAME, null);
		if(null != mMemorizeName)
		{
			MangoDictEng.mDictPath = settings.getString(MangoDictEng.DICT_SETTING_PATH, MangoDictEng.DICT_DEFAULT_PATH);
			mMemorizeFilePath = MangoDictEng.mDictPath + "/" + MEMORIZE_FOLDER + "/" + mMemorizeName + ".mem";

			mCardType = MemorizeEng.BUILD_QUEUE_SCHEDULED;
		}
		else
		{
			mCardType = MemorizeEng.BUILD_QUEUE_NONE;
		}

		initMemorize();
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {

    	menu.clear();

		if(MemorizeEng.BUILD_QUEUE_NONE == mCardType)
		{
			MyLog.v(TAG, "rebuildRevisionQueue()::Hasn't load the memorize file.");
			menu.add(0, MENU_MEMORIZE_SELECT, 0, R.string.memorizeselect).setIcon(R.drawable.ic_btn_memorizeselect);
		}
		else
		{
			menu.add(0, MENU_MEMORIZE_SCHEDULED, 0, R.string.learnscheduledcard).setIcon(R.drawable.ic_btn_newcards);
			menu.add(0, MENU_MEMORIZE_NEWCARD, 0, R.string.learnnewcard).setIcon(R.drawable.ic_btn_newcards);
			menu.add(0, MENU_MEMORIZE_LEARNAHEAD, 0, R.string.learnaheadcard).setIcon(R.drawable.ic_btn_newcards);
			menu.add(0, MENU_MEMORIZE_PLAYWORD, 0, R.string.play_word).setIcon(R.drawable.ic_btn_speech);
			menu.add(0, MENU_MEMORIZE_SCHEDULE, 0, R.string.schedule).setIcon(R.drawable.ic_btn_schedule);
			menu.add(0, MENU_MEMORIZE_STATISTIC, 0, R.string.statistic).setIcon(R.drawable.ic_btn_statistic);
			menu.add(0, MENU_MEMORIZE_SETTING, 0, R.string.memerizeconfig).setIcon(R.drawable.ic_btn_setting);
			menu.add(0, MENU_MEMORIZE_SELECT, 0, R.string.memorizeselect).setIcon(R.drawable.ic_btn_memorizeselect);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_MEMORIZE_SCHEDULED:
	        {
	        	rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_SCHEDULED, R.string.noschedulecard);
	        	break;
	        }

	        case MENU_MEMORIZE_NEWCARD:
	        {
	        	rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_NEWCARD, R.string.nonewcard);
	        	break;
	        }

	        case MENU_MEMORIZE_LEARNAHEAD:
	        {
	        	rebuildRevisionQueue(MemorizeEng.BUILD_QUEUE_LEARNAHEAD, R.string.nocard);
	        	break;
	        }

	        case MENU_MEMORIZE_PLAYWORD:
	        {
				Intent intent = new Intent(this, WordRecordActivity.class);
				Bundle bundle = new Bundle();
				
				bundle.putString(WordRecordActivity.RECORD_WORD, mQuestion);

				intent.putExtras(bundle);

				startActivityForResult(intent, WordRecordActivity.RECORD_RESULT_CODE);
				break;
	        }

	        case MENU_MEMORIZE_SCHEDULE:
	        {
	        	Intent intent = new Intent();
	            intent.setClass(this, MemorizeScheduleActivity.class);
	    		startActivity(intent);
	        	break;
	        }

	        case MENU_MEMORIZE_STATISTIC:
	        {
	        	Intent intent = new Intent();
	            intent.setClass(this, MemorizeStatisticActivity.class);
	    		startActivity(intent);
	        	break;
	        }
	        
	        case MENU_MEMORIZE_SELECT:
	        {
	        	selectMemorize();
	        	break;
	        }

	        case MENU_MEMORIZE_SETTING:
	        {
	        	Intent intent = new Intent();
	            intent.setClass(this, MemorizeSettingActivity.class);
	    		startActivity(intent);
	        	break;
	        }
	    }

		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (MEMORIZE_RESULT_CODE == requestCode) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				String memorizeName = bundle.getString("memorizeName");
				MyLog.v(TAG, "onActivityResult()::memorizeName=" + memorizeName);

				SharedPreferences settings = getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(DICT_SETTING_MEMORIZE_NAME, memorizeName);
				editor.commit();

				mMemorizeName = memorizeName;
				mMemorizeFilePath = MangoDictEng.mDictPath + "/" + MEMORIZE_FOLDER + "/" + mMemorizeName + ".mem";

				mCardType = MemorizeEng.BUILD_QUEUE_SCHEDULED;
				initMemorize();
			}
		}
	}


	@Override
	protected void onPause() {
		MyLog.v(TAG, "onPause()");
		super.onPause();
	}


	@Override
	protected void onResume() {
		MyLog.v(TAG, "onResume()");
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

		mMemorizeEng.releaseMemorizeEng();
		mMangoDictUtils.destroy();

		mDictContentView.destroy();
		mDictContentView = null;

		super.onDestroy();
	}
}
