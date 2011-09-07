package com.mango.MangoDict;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MangoDictService extends Service {
	private final String TAG = "MangoDictService";

	private final int CLIPBOARD_TIMER = 2000;

	private final int MAX_WORDS_SIZE = 256;
	private final int WINDOW_WIDTH_PADDING = 40;
	private final int WINDOW_HEIGHT_PADDING = 60;
	private final int WINDOW_MIN_HEIGHT = 100;
	private final int WINDOW_MAX_OFFSET = 45;

	private final int LOW_DPI_STATUS_BAR_HEIGHT = 19;
	private final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;
	private final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;
	//private final int XHIGH_DPI_STATUS_BAR_HEIGHT = 50;

	private MangoDictUtils mMangoDictUtils = null;

	private boolean bHasLoadDict = false;
	private Handler mHandler = null;
	private Runnable mClipboardTask = null;
	private String mClipboardText = "";
	private ClipboardManager mClipboardManager = null;
	private LinearLayout mCaptureWindowLayout = null;
	private WindowManager mWindowManager = null;
	private WindowManager.LayoutParams mWindowParams = null;
	private boolean bHasAddedView = false;

	private ImageButton mWindowCloseBtn = null;
	private ImageButton mWindowResizeBtn = null;
	private ImageButton mWindowMoveBtn = null;
	private ImageButton mWindowSchBtn = null;
	private EditText mKeywordEdit = null;

	private LinearLayout mParentViewLayout = null;
	private View mLineView0 = null;
	private View mLineView1 = null;
	private View mLineView2 = null;
	private View mLineView3 = null;
	private View mLineView4 = null;
	private WebView mDictContentView = null;

	private int mWinStatus = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0; 
	private int mWindow_Default_X = 0;
	private int mWindow_Default_Y = 0;
	private int mWindow_Default_W = 0;
	private int mWindow_Default_H = 0;
	private int mTouchStartX = 0;
	private int mTouchStartY = 0;
	private int mTouchEndX = 0;
	private int mTouchEndY = 0;
	private int mStatusBarHeight = 0;


	public class ServiceWebViewClientCallback extends WebViewClientCallback {
		public ServiceWebViewClientCallback() {
			MyLog.v(TAG, "ServiceWebViewClientCallback()");
		}

		public void shouldOverrideUrlLoading(String word) {
			MyLog.v(TAG, "ServiceWebViewClientCallback()::shouldOverrideUrlLoading()");

			makeDictContent(word);
			setTextToKeywordEdit(word);
		}
	}

	private void setTextToKeywordEdit(String word) {
		mKeywordEdit.setText(word);

		// make sure we keep the caret at the end of the text view
        Editable spannable = mKeywordEdit.getText();
        Selection.setSelection(spannable, spannable.length());
	}

	private void makeDictContent(String word) {
		if(false == bHasLoadDict)
		{
			bHasLoadDict = true;
			mMangoDictUtils.initDicts();
		}

		String htmlContent = mMangoDictUtils.generateHtmlContent(word, MangoDictEng.DICT_TYPE_CAPTURE);

		mMangoDictUtils.showHtmlContent(htmlContent, mDictContentView); 
	}

	private void clipboardCheck() {
		MyLog.v(TAG, "clipboardCheck()");
		
		String clipboardText = "";
		
		if(mClipboardManager.hasText())
		{
			clipboardText = mClipboardManager.getText().toString();
		}

		if(clipboardText.length() > MAX_WORDS_SIZE)
			clipboardText = clipboardText.substring(0, MAX_WORDS_SIZE);

		if(mClipboardText.equals(clipboardText))
			return;

		if(clipboardText.length() > 0)
		{
			mClipboardText = clipboardText;

			showCaptureWindow();
		}
	}

    //-----------------------------------------------------------------------------------------------------//
	// WindowManger

	private void closeCaptureWindow() {
		mWindowManager.removeView(mCaptureWindowLayout);
		bHasAddedView = false;
		mMangoDictUtils.showHtmlContent("<br> <br>", mDictContentView);
	}

	private void showCaptureWindow() {

		MyLog.v(TAG, "showCaptureWindow()::" + mClipboardText);

		setTextToKeywordEdit(mClipboardText);

		makeDictContent(mClipboardText);

		if(!mDictContentView.hasFocus())
			mDictContentView.requestFocus();

		if(false == bHasAddedView)
		{
	        int bgColor = MangoDictUtils.GetBgColor();
	        int textColor = MangoDictUtils.GetTextColor();
	        mParentViewLayout.setBackgroundColor(bgColor);
	        mDictContentView.setBackgroundColor(bgColor);
	        mLineView0.setBackgroundColor(textColor);
	        mLineView1.setBackgroundColor(textColor);
	        mLineView2.setBackgroundColor(textColor);
	        mLineView3.setBackgroundColor(textColor);
	        mLineView4.setBackgroundColor(textColor);	        

			updateDisplaySize(true);

			mWindowParams.x = mWindow_Default_X;
            mWindowParams.y = mWindow_Default_Y;

            mWinStatus = 0;
    		mWindowResizeBtn.setImageResource(R.drawable.ic_btn_max_win);

			mWindowManager.addView(mCaptureWindowLayout, mWindowParams);
			bHasAddedView = true;
		}
	}

	private void updateDisplaySize(boolean resetSize)
	{
	    Display display = mWindowManager.getDefaultDisplay();
	    mScreenWidth = display.getWidth();
	    mScreenHeight = display.getHeight() - mStatusBarHeight;

        mWindow_Default_W = mScreenWidth - WINDOW_WIDTH_PADDING;
        mWindow_Default_H = mScreenHeight - WINDOW_HEIGHT_PADDING;

	    if(true == resetSize)
	    {
	        mWindowParams.width = mWindow_Default_W;
	        mWindowParams.height = mWindow_Default_H;
	    }
	}

	private int getStatusBarHeight()
	{
		int statusBarHeight = -1;

		switch (mMangoDictUtils.GetDensityDpi()) {
		    //case DisplayMetrics.DENSITY_XHIGH:
		    //    statusBarHeight = XHIGH_DPI_STATUS_BAR_HEIGHT;
		    //    break;
		    case DisplayMetrics.DENSITY_HIGH:
		        statusBarHeight = HIGH_DPI_STATUS_BAR_HEIGHT;
		        break;
		    case DisplayMetrics.DENSITY_MEDIUM:
		        statusBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
		        break;
		    case DisplayMetrics.DENSITY_LOW:
		        statusBarHeight = LOW_DPI_STATUS_BAR_HEIGHT;
		        break;
		    default:
		        statusBarHeight = -1;
		}

		if (-1 == statusBarHeight)
		{
			int notificationBarResources[] = {
		            android.R.drawable.stat_sys_phone_call,
		            android.R.drawable.stat_notify_call_mute,
		            android.R.drawable.stat_notify_sdcard,
		            android.R.drawable.stat_notify_sync,
		            android.R.drawable.stat_notify_missed_call,
		            android.R.drawable.stat_sys_headset,
		            android.R.drawable.stat_sys_warning };
	
		    for (int i = 0; i < notificationBarResources.length; i++) {
		        try {
		            Drawable drawable = getResources().getDrawable( notificationBarResources[i] );
		            if ((statusBarHeight = drawable.getIntrinsicHeight()) != -1) {
		                break;
		            }
		        }
		        catch (Resources.NotFoundException e) {
		        }
		    }
		}

		MyLog.v(TAG, "getStatusBarHeight()::statusBarHeight=" + statusBarHeight);

	    return statusBarHeight;
	}

    //-----------------------------------------------------------------------------------------------------//

	private void initService() {
		MyLog.v(TAG, "initService()");

		mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

		if(mClipboardManager.hasText())
		{
			mClipboardText = mClipboardManager.getText().toString();
		}

        LayoutInflater factory = LayoutInflater.from(this);
        mCaptureWindowLayout = (LinearLayout)factory.inflate(R.layout.capture_window, null);

        mWindowCloseBtn = (ImageButton)mCaptureWindowLayout.findViewById(R.id.windowCloseBtn);
        mWindowResizeBtn = (ImageButton)mCaptureWindowLayout.findViewById(R.id.windowResizeBtn);
        mWindowMoveBtn = (ImageButton)mCaptureWindowLayout.findViewById(R.id.windowMoveBtn);
        mWindowSchBtn = (ImageButton)mCaptureWindowLayout.findViewById(R.id.windowSchBtn);

        mKeywordEdit = (EditText)mCaptureWindowLayout.findViewById(R.id.keywordTxt);
        
        mParentViewLayout = (LinearLayout) mCaptureWindowLayout.findViewById(R.id.parentView);
        mLineView0 = (View) mCaptureWindowLayout.findViewById(R.id.lineView0);
        mLineView1 = (View) mCaptureWindowLayout.findViewById(R.id.lineView1);
        mLineView2 = (View) mCaptureWindowLayout.findViewById(R.id.lineView2);
        mLineView3 = (View) mCaptureWindowLayout.findViewById(R.id.lineView3);
        mLineView4 = (View) mCaptureWindowLayout.findViewById(R.id.lineView4);
        mDictContentView = (WebView)mCaptureWindowLayout.findViewById(R.id.dictContentWindow);
        mDictContentView.setWebViewClient(new DictWebViewClient(new ServiceWebViewClientCallback()));

        WebSettings webSettings = mDictContentView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        //webSettings.setBuiltInZoomControls(true); 	// This will cause crash some times, it seems windowManager has some bug with WebView.
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        View.OnKeyListener closeWindowKeyListener = new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
				{
					MyLog.v(TAG, "mDictContentView::onKey()");
					closeCaptureWindow();
				}

				return false;
			}
		};

        mDictContentView.setOnKeyListener(closeWindowKeyListener);
        mWindowCloseBtn.setOnKeyListener(closeWindowKeyListener);
        mWindowResizeBtn.setOnKeyListener(closeWindowKeyListener);
        mWindowMoveBtn.setOnKeyListener(closeWindowKeyListener);
        mWindowSchBtn.setOnKeyListener(closeWindowKeyListener);
        mKeywordEdit.setOnKeyListener(closeWindowKeyListener);

        mDictContentView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(!mDictContentView.hasFocus())
				{
					MyLog.v(TAG, "mDictContentView::requestFocus()");
					mDictContentView.requestFocus();
				}
				return false;
			}
        });

        mWindowMoveBtn.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				if(1 == mWinStatus)	// Max window, don't move the window.
				{
					return false;
				}
				switch (event.getAction()) {
			        case MotionEvent.ACTION_DOWN:
			        	mTouchStartX =  (int)event.getX();  
			        	mTouchStartY =  (int)event.getY();
			            break;

			        case MotionEvent.ACTION_MOVE:	            
			            {
			            	updateDisplaySize(false);

							mTouchEndX = (int)event.getRawX();  
			                mTouchEndY = (int)event.getRawY() - mStatusBarHeight;

		                    mWindowParams.x = mTouchEndX - mTouchStartX;
		                    mWindowParams.y = mTouchEndY - mTouchStartY;

		                    if(mWindowParams.x < 0)
		                    	mWindowParams.x = 0;

		                    if(mWindowParams.x > mScreenWidth - WINDOW_MAX_OFFSET)
		                    	mWindowParams.x = mScreenWidth - WINDOW_MAX_OFFSET;

		                    if(mWindowParams.y < 0)
		                    	mWindowParams.y = 0;

		                    if(mWindowParams.y > mScreenHeight - WINDOW_MAX_OFFSET)
		                    	mWindowParams.y = mScreenHeight - WINDOW_MAX_OFFSET;		                    

		        			mWindowManager.updateViewLayout(mCaptureWindowLayout, mWindowParams);
			            }
			            break;

			        case MotionEvent.ACTION_UP:
			        	break;
			        }

				return true;
			}
        });
        
        mWindowSchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String keyword = mKeywordEdit.getText().toString();
            	if(keyword.length() > 0)
            		makeDictContent(keyword);
            }
        });

        mWindowCloseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	closeCaptureWindow();
            }
        });

        mWindowResizeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	updateDisplaySize(true);
            	
            	if(0 == mWinStatus)								// Make max.
            	{
            		mWinStatus ++;

            		mWindowResizeBtn.setImageResource(R.drawable.ic_btn_nor_win);
                    mWindowParams.x = 0;
                    mWindowParams.y = 0;
                    mWindowParams.width = mScreenWidth;
                    mWindowParams.height = mScreenHeight;
            	}
            	else if(1 == mWinStatus || 3 == mWinStatus) 	// Make normal.
            	{
            		if(3 == mWinStatus)
            		{
            			mWinStatus = 0;
            			
                		mWindowResizeBtn.setImageResource(R.drawable.ic_btn_max_win);
            		}
            		else
            		{
            			mWinStatus ++;
            			
                		mWindowResizeBtn.setImageResource(R.drawable.ic_btn_min_win);
            		}

                    mWindowParams.x = mWindow_Default_X;
                    mWindowParams.y = mWindow_Default_Y;
                    mWindowParams.width = mWindow_Default_W;
                    mWindowParams.height = mWindow_Default_H;
            	}
        		else if(2 == mWinStatus)						// Make min.
		        {
            		mWinStatus ++;

            		mWindowResizeBtn.setImageResource(R.drawable.ic_btn_nor_win);
                    mWindowParams.height = WINDOW_MIN_HEIGHT;
		        }

    			mWindowManager.updateViewLayout(mCaptureWindowLayout, mWindowParams);
            }
        });

        mStatusBarHeight = getStatusBarHeight();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mWindowParams = new WindowManager.LayoutParams(
        					ViewGroup.LayoutParams.FILL_PARENT,
                		    ViewGroup.LayoutParams.WRAP_CONTENT,
                		    2002,	// WindowManager.LayoutParams.TYPE_PHONE
                		    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                		    PixelFormat.TRANSLUCENT);

        updateDisplaySize(true);

        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowParams.windowAnimations = R.style.WindowAnimal;
        mWindowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;

        mWindow_Default_X = WINDOW_WIDTH_PADDING / 2;
        mWindow_Default_Y = WINDOW_HEIGHT_PADDING / 2;

        mWindowParams.x = mWindow_Default_X;
        mWindowParams.y = mWindow_Default_Y;
	}

    //-----------------------------------------------------------------------------------------------------//
    
	public MangoDictService() {
		MyLog.v(TAG, "MangoDictService()");
	}

	// This is the object that receives interactions from clients.
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		MangoDictService getService() {
			MyLog.v(TAG, "getService()");

			return MangoDictService.this;
		}
	}
	
	@Override
	public void onCreate() {
		MyLog.v(TAG, "onCreate()");

       	mMangoDictUtils = new MangoDictUtils(this);

		mHandler = new Handler();

		mClipboardTask = new Runnable() {
			public void run() {

				clipboardCheck();

				mHandler.postDelayed(mClipboardTask, CLIPBOARD_TIMER);
			}
		};

		Runnable initServiceTask = new Runnable() {
			public void run() {

				initService();

				mHandler.postDelayed(mClipboardTask, CLIPBOARD_TIMER);
			}
		};

		mHandler.postDelayed(initServiceTask, 5000);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		MyLog.v(TAG, "onDestroy()");

		mHandler.removeCallbacks(mClipboardTask);
		mMangoDictUtils.destroy();
		
		mDictContentView.destroy();
		mDictContentView = null;

		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		MyLog.v(TAG, "onStart()");

		super.onStart(intent, startId);
	}

	@Override
	protected void finalize() throws Throwable {
		MyLog.v(TAG, "finalize()");

		super.finalize();
	}

	@Override
	public IBinder onBind(Intent intent) {
		MyLog.v(TAG, "onBind()");

		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		MyLog.v(TAG, "onRebind()");

		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		MyLog.v(TAG, "onUnbind()");

		return super.onUnbind(intent);
	}	
}
