package com.mango.MangoDict;



public class MemorizeEng {
	private final String TAG = "MemorizeEng";

	public final static int BUILD_QUEUE_NONE 		= -1;
	public final static int BUILD_QUEUE_SCHEDULED 	= 0;
	public final static int BUILD_QUEUE_NEWCARD 	= 1;
	public final static int BUILD_QUEUE_LEARNAHEAD	= 2;

	private static int mReferenct = 0;
	private static MemorizeEng gMemorizeEng = null;

	public MemorizeEng() {
		MyLog.v(TAG, "MemorizeEng()");
	}

	public static MemorizeEng createMemorizeEng() {
		MyLog.v("MemorizeEng", "createMemorizeEng()");

		if(null == gMemorizeEng)
			gMemorizeEng = new MemorizeEng();

		mReferenct ++;

		return gMemorizeEng; 
	}

	public void releaseMemorizeEng() {

		mReferenct --;

		MyLog.v(TAG, "releaseMemorizeEng()::mReferenct=" + mReferenct);

		if(0 == mReferenct)
		{ 
			UnloadMemorizeFile();
			gMemorizeEng = null;
		}
	}

    //-----------------------------------------------------------------------------------------------------//

    // Native function in DictMemorizeEng.c
	public native int[]     GetCardsProgressFromMfo(String mfoPath);
	public native int[]     GetDateOfStart();
	public native int[]     GetCardsProgress();
	public native int[]     GetScheduleStatus();
	public native int[]     GetCurrentCardData();
	public native int[]     GetGradeData();
	public native int[]     GetScheduleData(int nYear, int nMonth);
    public native void 		SetSettings(int[] settings);
    public native int[]		GetSettings();
	public native int       GetCurQueueType();
	public native void      GradeAnswer(int grade);
	public native boolean   RebuildRevisionQueue(int queueType);
	public native boolean   NewQuestion();
	public native String    GetMemorizeWord();
	public native void 		GenerateMemorizeFile(String srcUrl, String dstUrl);
	public native void 		UnloadMemorizeFile();
	public native boolean	LoadMemorizeFile(String memfile);

    static {
        System.loadLibrary("memorizeeng");
    }
}
