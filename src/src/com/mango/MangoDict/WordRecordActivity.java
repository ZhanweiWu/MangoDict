package com.mango.MangoDict;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class WordRecordActivity extends Activity implements OnCompletionListener, OnErrorListener {

	private final String TAG = "WordRecordActivity";

	public final static int RECORD_RESULT_CODE = 1;
	public final static String RECORD_WORD = "RecordWord";
	
	private final String RECORD_FILE_FORMAT = "3gpp";
	private final String RECORD_FILE_NAME = "reocrd." + RECORD_FILE_FORMAT;
	final static String RECORD_FOLDER = "record";

	private final int IDLE_STATE = 0;
	private final int RECORDING_STATE = 1;
	private final int PLAYING_STATE = 2;
	
	private TextView mStateMessage = null;
	private ImageView mStateIcon = null;
	private ImageButton mRecordButton = null;
	private ImageButton mPlayButton = null;
	private ImageButton mStopButton = null;
	private ImageButton mWordButton = null;

    private int mStatus = IDLE_STATE;
    private boolean mRecorded = false;
    private String mRecordWord = null;
    private String mRecordPath = null;
    private String mRecordFolderPath = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
	private DictSpeechEng mDictSpeechEng = null;

	private void UpdateUIStatus() {
		switch (mStatus) {
			case IDLE_STATE:
			{
                mRecordButton.setEnabled(true);
                if(true == mRecorded)
                {
                	mPlayButton.setEnabled(true);
                }
                else
                {
                	mPlayButton.setEnabled(false);
                }
                mStopButton.setEnabled(false);

                mStateIcon.setImageResource(R.drawable.record_idle_icon);
                mStateMessage.setText(R.string.press_record);
				break;
			}
			
			case RECORDING_STATE:
			{
                mRecordButton.setEnabled(false);
            	mPlayButton.setEnabled(false);
                mStopButton.setEnabled(true);
                mStateIcon.setImageResource(R.drawable.record_recording_icon);
                mStateMessage.setText(R.string.recording);
				break;
			}
			
			case PLAYING_STATE:
			{
                mRecordButton.setEnabled(true);
            	mPlayButton.setEnabled(false);
                mStopButton.setEnabled(true);
                mStateIcon.setImageResource(R.drawable.record_recording_icon);
                mStateMessage.setText(R.string.playing);
				break;
			}
		}
	}
	
	private void removeFile() {
		File recordFile = new File(mRecordPath);
		if (recordFile.exists())
		{
			recordFile.delete();
		}
	}

	private void removeFolder() {
		removeFile();

		File recordFolder = new File(mRecordFolderPath);
		if (recordFolder.exists())
		{
			recordFolder.delete();
		}
	}

	private void startRecord() {
		boolean bError = false;
		stop();

		removeFile();

        mRecorder = new MediaRecorder();
        try {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // MPEG_4
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // AAC
        //mRecorder.setAudioEncodingBitRate(12200); // Max value: 128000
        //mRecorder.setAudioSamplingRate(18000);	// Max value: 48000

        mRecorder.setOutputFile(mRecordPath);
        
        mRecorder.prepare();
        mRecorder.start();
        } catch (IOException exception) {
    		MyLog.v(TAG, "startRecord()::IOException");
    		
    		bError = true;
        } catch (RuntimeException localRuntimeException) {
    		MyLog.v(TAG, "startRecord()::RuntimeException");

    		bError = true;
        }

        if (true == bError)
        {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;

	        mStatus = IDLE_STATE;
	        mRecorded = false;
        }
        else
        {
	        mStatus = RECORDING_STATE;
	        mRecorded = true;
        }

        UpdateUIStatus();
	}

	private void startPlay() {
		stop();

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mRecordPath);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            mPlayer = null;
            return;
        } catch (IOException e) {
            mPlayer = null;
            return;
        }

        mStatus = PLAYING_STATE;
        
        UpdateUIStatus();
	}
	
	private void stop() {
		if(null != mRecorder)
		{
	        mRecorder.stop();
	        mRecorder.reset();
	        mRecorder.release();
	        mRecorder = null;
		}

		if(null != mPlayer)
		{
	        mPlayer.stop();
	        mPlayer.release();
	        mPlayer = null;
		}
		
        mStatus = IDLE_STATE;
        
        UpdateUIStatus();
	}

	public boolean onError(MediaPlayer mp, int what, int extra) {
		stop();
		return false;
	}

	public void onCompletion(MediaPlayer mp) {
		stop();		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.word_record);

    	mDictSpeechEng = new DictSpeechEng(this);
    	
    	mStateMessage = (TextView) findViewById(R.id.stateMessage);
    	mStateIcon = (ImageView)  findViewById(R.id.stateIcon);
    	mRecordButton = (ImageButton)  findViewById(R.id.recordButton);
    	mPlayButton = (ImageButton)  findViewById(R.id.playButton);
    	mStopButton = (ImageButton)  findViewById(R.id.stopButton);
    	mWordButton = (ImageButton)  findViewById(R.id.wordButton);
    	
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		if(null != bundle)
		{
			mRecordWord = bundle.getString(RECORD_WORD);
			
			MyLog.v(TAG, "onCreate()::mRecordWord=" + mRecordWord);
		}

		mRecordFolderPath = MangoDictEng.mDictPath + "/" + RECORD_FOLDER;

		File recordFolder = new File(mRecordFolderPath);
		if (!recordFolder.exists())
		{
			recordFolder.mkdirs();
		}

		mRecordPath = mRecordFolderPath + "/" + RECORD_FILE_NAME;

		mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startRecord();
            }
        });

		mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startPlay();
            }
        });

		mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	stop();
            }
        });

		mWordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(null != mRecordWord)
            	{
            		mDictSpeechEng.speak(mRecordWord);
            	}
            }
        });
		
    	UpdateUIStatus();
	}

	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()");

		stop();

		removeFolder();
		
		mDictSpeechEng.destroy();
		mDictSpeechEng = null;

		super.onDestroy();
	}
}
