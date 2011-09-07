package com.mango.MangoDict;



public class MangoDictEng {
	private final String TAG = "MangoDictEng";

	final static String MIME_TYPE = "text/html";
	final static String HTML_ENCODING = "utf-8";
	final static String BWORD_URL = "bword://";

	final static String DICT_SETTING_PREF_NAME 			= "MangoDictSetting";
	final static String DICT_SETTING_PATH				= "DictPath";
	final static String DICT_TYPE 						= "DictType";

	final static String DICT_SETTING_COLOR				= "DictColor";
	final static String DICT_SETTING_BGIMAGE			= "DictBgImage";
	
	final static String DICT_SETTING_INDEX_CHECKED 		= "DictIndexChecked";
	final static String DICT_SETTING_CAPTURE_CHECKED	= "DictCaptureChecked";
	final static String DICT_SETTING_MEMORIZE_CHECKED	= "DictMemorizeChecked";
	final static String DICT_SETTING_INDEX_ALL 			= "DictIndexALL";
	final static String DICT_SETTING_CAPTURE_ALL		= "DictCaptureALL";
	final static String DICT_SETTING_MEMORIZE_ALL		= "DictMemorizeALL";
	final static String DICT_SETTING_ISCAPTURE			= "DictIsCapture";

	final static String DICT_DEFAULT_PATH 				= "/sdcard";

	final static int DICT_TYPE_INDEX 			= 0x0001;
	final static int DICT_TYPE_CAPTURE 			= 0x0010;
	final static int DICT_TYPE_MEMORIZE 		= 0x0100;


	public static String mDictPath = null;
	public static boolean mIsCapture = false;

	public static String mDictBgImage = null;
	public static String mDictColor = null;

	public static String mDictIndexChecked = null;
	public static String mDictCaptureChecked = null;
	public static String mDictMemorizeChecked = null;
	public static String mDictIndexAll = null;
	public static String mDictCaptureAll = null;
	public static String mDictMemorizeAll = null;

	public static String DictPaths[] = null;
	public static String DictNames[] = null;
	public static int 	 DictTypes[] = null;

	private static int mReferenct = 0;
	private static MangoDictEng gMangoDictEng = null;

	public MangoDictEng() {
		MyLog.v(TAG, "MangoDictEng()");
	}

	public static MangoDictEng createMangoDictEng() {
		MyLog.v("MangoDictEng", "createMangoDictEng()");

		if(null == gMangoDictEng)
			gMangoDictEng = new MangoDictEng();

		mReferenct ++;

		return gMangoDictEng; 
	}

	public void releaseMangoDictEng() {

		mReferenct --;

		MyLog.v(TAG, "releaseMangoDictEng()::mReferenct=" + mReferenct);

		if(0 == mReferenct)
		{
			gMangoDictEng = null; 
			UnloadDicts();
		}
	}

	// This function is called in JNI C code, it must be 'static' function.
	private static void lookupProgressCB(int progress)
	{
		MangoDictActivity.lookupProgressCB(progress);
	}

    //-----------------------------------------------------------------------------------------------------//

    // Native function in MangoDict.c
	public native void 		CancelLookup();
    public native String[] 	Lookup(String word, int type);	// This function for DICT_TYPE_INDEX, DICT_TYPE_CAPTURE and DICT_TYPE_MEMORIZE.
    public native String[] 	ListWords(String word);			// This function is only for the type DICT_TYPE_INDEX.
    public native String[] 	FuzzyListWords(String word);	// This function is only for the type DICT_TYPE_INDEX.
    public native String[] 	PatternListWords(String word);	// This function is only for the type DICT_TYPE_INDEX.
    public native String[] 	FullTextListWords(String word);	// This function is only for the type DICT_TYPE_INDEX.
    public native String 	GetBookName(String ifoPath);
    public native boolean 	LoadDicts(String[] paths, String[] names, int[] types);
    public native void 		UnloadDicts();

    static {
        System.loadLibrary("mangodicteng");
    }
}
