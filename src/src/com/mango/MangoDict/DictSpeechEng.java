package com.mango.MangoDict;

import java.util.Locale;
import android.content.Context;
import android.speech.tts.TextToSpeech;

public class DictSpeechEng {
	private final String TAG = "MangoDictUtils";

    private TextToSpeech mTts = null;
    private boolean mCanSpeak = false;

	public DictSpeechEng(Context context) {
		MyLog.v(TAG, "DictSpeechEng()");

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization completes.
		mTts = new TextToSpeech(context, new TtsInitListener());
	}

	public void destroy() {
    	MyLog.v(TAG, "destroy()");

        if (mTts != null)
        {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
	}

	public void speak(String text) {
    	MyLog.v(TAG, "speak()::text=" + text);

    	if(null != text && true == mCanSpeak)
    	{
    		mTts.speak(text, 
    				   TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
                       null);
    	}
	}

    class TtsInitListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {
        	MyLog.v(TAG, "onInit()");

            // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
            if (status == TextToSpeech.SUCCESS) {
                // Set preferred language to US english.
                // Note that a language may not be available, and the result will indicate this.
                int result = mTts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                   // Lanuage data is missing or the language is not supported.
                	MyLog.v(TAG, "onInit()::Language is not available.");
                } else {
                    // Check the documentation for other possible result codes.
                    // For example, the language may be available for the locale,
                    // but not for the specified country and variant.

                    // Now the TTS engine has been successfully initialized.
                	mCanSpeak = true;
                }
            } else {
                // Initialization failed.
            	MyLog.v(TAG, "onInit()::Could not initialize TextToSpeech.");
            }
        }
    }
}
