package com.mango.MangoDict;

import java.io.File;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebView;

public class MangoDictUtils {
	private final String TAG = "MangoDictUtils";

	private final String FILTER_SYMBOLS = "\"\\^\\$\\*\\+\\{\\}\\[\\]\\?\\(\\)\\|\\\\,.:;/=_!@#%&<>`~0123456789";

	private Context mContext = null;
	private MangoDictEng mMangoDictEng = null;


	public MangoDictUtils(Context context) {
		MyLog.v(TAG, "MangoDictUtils()");

		mContext = context;
		getDictionaryData();

       	mMangoDictEng = MangoDictEng.createMangoDictEng();
	}

	private void saveDictionaryData()
	{
		SharedPreferences settings = mContext.getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(MangoDictEng.DICT_SETTING_INDEX_CHECKED, MangoDictEng.mDictIndexChecked);
		editor.putString(MangoDictEng.DICT_SETTING_CAPTURE_CHECKED, MangoDictEng.mDictCaptureChecked);
		editor.putString(MangoDictEng.DICT_SETTING_MEMORIZE_CHECKED, MangoDictEng.mDictMemorizeChecked);
		editor.putString(MangoDictEng.DICT_SETTING_INDEX_ALL, MangoDictEng.mDictIndexAll);
		editor.putString(MangoDictEng.DICT_SETTING_CAPTURE_ALL, MangoDictEng.mDictCaptureAll);
		editor.putString(MangoDictEng.DICT_SETTING_MEMORIZE_ALL, MangoDictEng.mDictMemorizeAll);
		editor.commit();
	}

	private void getDictionaryData()
	{
		SharedPreferences settings = mContext.getSharedPreferences(MangoDictEng.DICT_SETTING_PREF_NAME, Activity.MODE_PRIVATE);

		MangoDictEng.mDictPath = settings.getString(MangoDictEng.DICT_SETTING_PATH, MangoDictEng.DICT_DEFAULT_PATH);
		MangoDictEng.mIsCapture = settings.getBoolean(MangoDictEng.DICT_SETTING_ISCAPTURE, false);

		MangoDictEng.mDictBgImage = settings.getString(MangoDictEng.DICT_SETTING_BGIMAGE, "");
		MangoDictEng.mDictColor = settings.getString(MangoDictEng.DICT_SETTING_COLOR, "#FF000000;#FF0000FF;#FFFFFFFF");	// text color, key word color and background color.

		MangoDictEng.mDictIndexChecked = settings.getString(MangoDictEng.DICT_SETTING_INDEX_CHECKED, "");
		MangoDictEng.mDictCaptureChecked = settings.getString(MangoDictEng.DICT_SETTING_CAPTURE_CHECKED, "");
		MangoDictEng.mDictMemorizeChecked = settings.getString(MangoDictEng.DICT_SETTING_MEMORIZE_CHECKED, "");
		MangoDictEng.mDictIndexAll = settings.getString(MangoDictEng.DICT_SETTING_INDEX_ALL, "");
		MangoDictEng.mDictCaptureAll = settings.getString(MangoDictEng.DICT_SETTING_CAPTURE_ALL, "");
		MangoDictEng.mDictMemorizeAll = settings.getString(MangoDictEng.DICT_SETTING_MEMORIZE_ALL, "");
	}

	private String [] lookupWord(String word, int type)
	{
		String strWordsArray[] = null;
		String keyword = word;
		String keyword2 = "";

		// step 1: remove some symbols if the type is Capture.
		if(MangoDictEng.DICT_TYPE_CAPTURE == type)
		{
			if(keyword.charAt(keyword.length() - 1) == '\'')	// Remove "'" if it is at the end of this word.
				keyword = keyword.substring(0, keyword.length() - 2);
			keyword = keyword.replaceAll("[" + FILTER_SYMBOLS + "]", "");	// Remove symbols in FILTER_SYMBOLS.
			keyword = keyword.trim();
		}
		if(keyword.length() <= 0)
			return null;
		strWordsArray = mMangoDictEng.Lookup(keyword, type);
		if(null != strWordsArray && strWordsArray.length > 0)
			return strWordsArray;

		// step 2: to lowercase.
		keyword2 = keyword.toLowerCase();
		if(keyword2.equals(keyword))
			return null;
		strWordsArray = mMangoDictEng.Lookup(keyword2, type);

		return strWordsArray;
	}

	private void checkDict(String[] dictFolders, int k)
	{
		String[] dictIndexArray = MangoDictEng.mDictIndexAll.split(";");
		
		// Check if some dictionaries have been removed from SD card.
		if(!MangoDictEng.mDictIndexAll.equals(""))
		{
			for(int m = 0; m < dictIndexArray.length; m++)
			{
				String dictIndex = dictIndexArray[m];
				boolean bFound = false;

				for(int i = 0; i < k; i++)
				{
					if(dictFolders[i].equals(dictIndex))
					{
						bFound = true;
						break;
					}
				}

				if(false == bFound)		// Not found this dictionary, it has been removed from the SD card.
				{						// Remove it from the configuration file.
					MangoDictEng.mDictIndexChecked.replace(dictIndex + ";", "");
					MangoDictEng.mDictCaptureChecked.replace(dictIndex + ";", "");
					MangoDictEng.mDictMemorizeChecked.replace(dictIndex + ";", "");
					MangoDictEng.mDictIndexAll.replace(dictIndex + ";", "");
					MangoDictEng.mDictCaptureAll.replace(dictIndex + ";", "");
					MangoDictEng.mDictMemorizeAll.replace(dictIndex + ";", "");
				}
			}
		}
		
		// Check if some dictionaries have been added to SD card.
		for (int i = 0; i < k; i++)
		{
			boolean bFound = false;

			for (int m = 0; m < dictIndexArray.length; m++)
			{
				if(dictFolders[i].equals(dictIndexArray[m]))
				{
					bFound = true;
					break;
				}
			}

			if(false == bFound)
			{
				MangoDictEng.mDictIndexAll += dictFolders[i] + ";";
				MangoDictEng.mDictCaptureAll += dictFolders[i] + ";";
				MangoDictEng.mDictMemorizeAll += dictFolders[i] + ";";
			}
		}
	}

	private void buildDictInfo(String dictsPath, int maxDictCnt, String [] dictFolders, String [] dictNames)
	{
		String[] dictIndexArray = null;
		String[] dictCaptureArray = null;
		String[] dictMemorizeArray = null;
		int lDictCnt = 0;

		MyLog.v(TAG, "BuildDictInfo()::Begin");

		if(!MangoDictEng.mDictIndexChecked.equals(""))
		{
			dictIndexArray = MangoDictEng.mDictIndexChecked.split(";");
		}

		if(!MangoDictEng.mDictCaptureChecked.equals(""))
		{
			dictCaptureArray = MangoDictEng.mDictCaptureChecked.split(";");
		}

		if(!MangoDictEng.mDictMemorizeChecked.equals(""))
		{
			dictMemorizeArray = MangoDictEng.mDictMemorizeChecked.split(";");
		}

		String lDictPaths[] = new String[maxDictCnt];
		String lDictNames[] = new String[maxDictCnt];
		int lDictTypes[] = new int[maxDictCnt];

		if(null != dictIndexArray && dictIndexArray.length > 0)
		{
			int i = 0;
			for (i = 0; i < dictIndexArray.length; i++)
			{
				lDictPaths[i] = dictsPath + "/" + dictIndexArray[i] + "/";

				for (int k = 0; k < dictFolders.length; k++)
				{
					if(dictFolders[k].equals(dictIndexArray[i]))
						lDictNames[i] = dictNames[k];
				}
				
				lDictTypes[i] = MangoDictEng.DICT_TYPE_INDEX;
			}
			
			lDictCnt = i;
		}

		if(null != dictCaptureArray && dictCaptureArray.length > 0)
		{
			for (int i = 0; i < dictCaptureArray.length; i++)
			{
				boolean bFound = false;
				for(int m = 0; m < lDictCnt; m++)
				{
					if(lDictPaths[m].indexOf(dictCaptureArray[i]) >= 0)
					{
						bFound = true;
						lDictTypes[m] |= MangoDictEng.DICT_TYPE_CAPTURE;
						break;
					}
				}

				if(false == bFound)
				{
					lDictPaths[lDictCnt] = dictsPath + "/" + dictCaptureArray[i] + "/";
	
					for (int k = 0; k < dictFolders.length; k++)
					{
						if(dictFolders[k].equals(dictCaptureArray[i]))
							lDictNames[lDictCnt] = dictNames[k];
					}
					
					lDictTypes[lDictCnt] = MangoDictEng.DICT_TYPE_CAPTURE;

					lDictCnt++;
				}
			}
		}

		if(null != dictMemorizeArray && dictMemorizeArray.length > 0)
		{
			for (int i = 0; i < dictMemorizeArray.length; i++)
			{
				boolean bFound = false;
				for(int m = 0; m < lDictCnt; m++)
				{
					if(lDictPaths[m].indexOf(dictMemorizeArray[i]) >= 0)
					{
						bFound = true;
						lDictTypes[m] |= MangoDictEng.DICT_TYPE_MEMORIZE;
						break;
					}
				}

				if(false == bFound)
				{
					lDictPaths[lDictCnt] = dictsPath + "/" + dictMemorizeArray[i] + "/";
	
					for (int k = 0; k < dictFolders.length; k++)
					{
						if(dictFolders[k].equals(dictMemorizeArray[i]))
							lDictNames[lDictCnt] = dictNames[k];
					}
					
					lDictTypes[lDictCnt] = MangoDictEng.DICT_TYPE_MEMORIZE;

					lDictCnt++;
				}
			}
		}

		if(lDictCnt > 0)
		{
			MangoDictEng.DictPaths = new String[lDictCnt];
			MangoDictEng.DictNames = new String[lDictCnt];
			MangoDictEng.DictTypes = new int[lDictCnt];

			for(int i = 0; i < lDictCnt; i++)
			{
				MangoDictEng.DictPaths[i] = lDictPaths[i];
				MangoDictEng.DictNames[i] = lDictNames[i];
				MangoDictEng.DictTypes[i] = lDictTypes[i];
	
				MyLog.v(TAG, "BuildDictInfo()::DictPaths=" + MangoDictEng.DictPaths[i]);
				MyLog.v(TAG, "BuildDictInfo()::DictNames=" + MangoDictEng.DictNames[i]);
				MyLog.v(TAG, "BuildDictInfo()::DictTypes=" + MangoDictEng.DictTypes[i]);
			}
		}
		else
		{
			MangoDictEng.DictPaths = null;
			MangoDictEng.DictNames = null;
			MangoDictEng.DictTypes = null;
		}
		
		MyLog.v(TAG, "BuildDictInfo()::End");
	}

	private void getDictPathsANames()
	{
		int k = 0;
		String dictsPath = MangoDictEng.mDictPath + "/dicts";
		File f = new File(dictsPath);

		MyLog.v(TAG, "GetDictPathsANames()::Begin");
		MyLog.v(TAG, "GetDictPathsANames()::dictsPath=" + dictsPath);

		if(!f.exists() || !f.isDirectory())
		{
			MangoDictEng.DictPaths = null;
			MangoDictEng.DictNames = null;
			MangoDictEng.DictTypes = null;
			MangoDictEng.mDictIndexChecked = "";
			MangoDictEng.mDictCaptureChecked = "";
			MangoDictEng.mDictMemorizeChecked = "";
			MangoDictEng.mDictIndexAll = "";
			MangoDictEng.mDictCaptureAll = "";
			MangoDictEng.mDictMemorizeAll = "";
			return;
		}

		File[] files = f.listFiles();
		String dictPaths[] = new String[files.length];
		String dictFolders[] = new String[files.length];
		String dictNames[] = new String[files.length];

		for (int i = 0; i < files.length; i++) {
			if(files[i].isDirectory())
			{
				String dictName = getDictName(files[i].getPath());
				if(null != dictName)
				{
					dictPaths[k] = files[i].getPath();
					dictFolders[k] = files[i].getName();
					dictNames[k] = dictName;
					k++;
				}
			}
		}

		if(k == 0)
		{
			MangoDictEng.mDictIndexChecked = "";
			MangoDictEng.mDictCaptureChecked = "";
			MangoDictEng.mDictMemorizeChecked = "";
			MangoDictEng.mDictIndexAll = "";
			MangoDictEng.mDictCaptureAll = "";
			MangoDictEng.mDictMemorizeAll = "";
			return;
		}
		else if(k > 0)
		{
			// Check if there are any dictionaries have been removed from the SD card or some new dictionaries have been added to SD card.
			checkDict(dictFolders, k);

			// Rebuild DictIndexAll, DictCaptureAll and DictMemorizeAll.
			if(MangoDictEng.mDictIndexAll.equals("") || MangoDictEng.mDictCaptureAll.equals("") || MangoDictEng.mDictMemorizeAll.equals(""))
			{
				MyLog.v(TAG, "GetDictPathsANames()::mDictAll==''");

				for(int m = 0; m < k; m++)
				{
					MangoDictEng.mDictIndexAll += dictFolders[m] + ";";
				}

				// Also set mDictCaptureAll and mDictMemorizeAll.
				MangoDictEng.mDictCaptureAll = MangoDictEng.mDictIndexAll;
				MangoDictEng.mDictMemorizeAll = MangoDictEng.mDictIndexAll; 
			}

			if(MangoDictEng.mDictIndexChecked.equals(""))		// Make first 5 dictionaries as DictIndexChecked.
			{
				MyLog.v(TAG, "GetDictPathsANames()::mDictIndex==''");
				int j = 5;

				if(k < j)
					j = k;

				for(int m = 0; m < j; m++)
				{
					MangoDictEng.mDictIndexChecked += dictFolders[m] + ";";
				}
			}

			if(MangoDictEng.mDictCaptureChecked.equals(""))	// Make first 1 dictionary as DictCaptureChecked.
			{
				MyLog.v(TAG, "GetDictPathsANames()::mDictCapture==''");
				MangoDictEng.mDictCaptureChecked = dictFolders[0] + ";";
			}

			if(MangoDictEng.mDictMemorizeChecked.equals(""))	// Make first 1 dictionary as DictMemorizeChecked.
			{
				MyLog.v(TAG, "GetDictPathsANames()::mDictMemorize==''");
				MangoDictEng.mDictMemorizeChecked = dictFolders[0] + ";";
			}
		}

		MyLog.v(TAG, "GetDictPathsANames()::mDictIndex=" + MangoDictEng.mDictIndexChecked);
		MyLog.v(TAG, "GetDictPathsANames()::mDictCapture=" + MangoDictEng.mDictCaptureChecked);
		MyLog.v(TAG, "GetDictPathsANames()::mDictMemorize=" + MangoDictEng.mDictMemorizeChecked);

		buildDictInfo(dictsPath, k, dictFolders, dictNames);

		MyLog.v(TAG, "GetDictPathsANames()::End");
	}

    /**
    *
    * Replace label attribute content.
    * @param str: the string to be replaced
    * @param beforeTag: the label to be replaced
    * @param tagAttrib: the lable's attribute to be replaced
    * @param startTag: the begin of the new label
    * @param endTag: the end of the new label
    */
    private String replaceHtmlTag(String str, String beforeTag, String tagAttrib, String startTag, String endTag) {

            String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)>";
    	    String regxpForTagAttrib = tagAttrib + "=([^\\s]+)\\s*";

	       Pattern patternForTag = Pattern.compile (regxpForTag, Pattern.CASE_INSENSITIVE );
	       Pattern patternForAttrib = Pattern.compile (regxpForTagAttrib, Pattern.CASE_INSENSITIVE );   
	       Matcher matcherForTag = patternForTag.matcher(str);

	       StringBuffer sb = new StringBuffer ();
	       boolean result = matcherForTag.find();

	       // go through all <img> lable.
	       while (result) {
	           StringBuffer sbreplace = new StringBuffer( "<img " );
	           Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag.group(1));

	           if (matcherForAttrib.find()) {
	        	  if (null == startTag)		// Just remove tag.
	        	  {
	        		  matcherForAttrib.appendReplacement(sbreplace, "");
	        	  }
	        	  else
	        	  {
		        	  String matcherString = matcherForAttrib.group(1);
		        	  matcherString = matcherString.replace("'", "");
		        	  matcherString = matcherString.replace("\"", "");
		        	  matcherString = matcherString.replace("", "");	// replace '1E'
		        	  matcherString = matcherString.replace("", "");	// replace '1F'

	        		  matcherForAttrib.appendReplacement(sbreplace, startTag + matcherString + endTag);
	        	  }
	           }

	           matcherForAttrib.appendTail(sbreplace);
	           matcherForTag.appendReplacement(sb, sbreplace.toString() + ">");
	           result = matcherForTag.find();
	       }

	       matcherForTag.appendTail(sb);

	       return sb.toString();
    }

    //-----------------------------------------------------------------------------------------------------//
	// Public static class
    
	public static String getSDCardPath() {
		File SDCardFolder = null;
		boolean SDCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		
		MyLog.v("MangoDict", "getSDCardPath()");
		
		if (SDCardExist) {
			SDCardFolder = Environment.getExternalStorageDirectory();

			MyLog.v("MangoDict", "getSDCardPath()::SDCardFolder=" + SDCardFolder.toString());

			return SDCardFolder.toString();
		}

		return null;
	}
    
    public static double roundDouble(double value, int scale, int roundingMode) {  
        BigDecimal bd = new BigDecimal(value);  
        bd = bd.setScale(scale, roundingMode);  
        double d = bd.doubleValue();  
        bd = null;
        return d;
    }
    
    public static int ARGBToColor(String color) {
    	return Color.parseColor(color);
    }

    public static String ColorToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

	public static int GetTextColor() {
		return ARGBToColor(MangoDictEng.mDictColor.split(";")[0]);
	}

	public static int GetWordColor() {
		return ARGBToColor(MangoDictEng.mDictColor.split(";")[1]);
	}

	public static int GetBgColor() {
		return ARGBToColor(MangoDictEng.mDictColor.split(";")[2]);
	}

	public static String GetTextHtmlColor() {
		return "#" + MangoDictEng.mDictColor.split(";")[0].substring(3);
	}
	
	public static String GetWordHtmlColor() {
		return "#" + MangoDictEng.mDictColor.split(";")[1].substring(3);
	}
	
	public static String getDictName(String dictFolderPath)
	{
		String dictName = null;

		File f = new File(dictFolderPath);

		if(!f.exists())
			return null;

		File[] files = f.listFiles();

		for (int i = 0; i < files.length; i++)
		{
			if(files[i].isFile())
			{
				if(files[i].getName().endsWith(".ifo"))
				{
					dictName = files[i].getName().replace(".ifo", "");
					break;
				}
			}
		}

		return dictName;
	}
	
    //-----------------------------------------------------------------------------------------------------//
	// Public class

	public int GetDensityDpi() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE)))
				.getDefaultDisplay().getMetrics(displayMetrics);

		return displayMetrics.densityDpi;
	}

	public String generateHtmlContent(String word, int type)
	{
		String html = "";
		String dictHtmlData = "";
		String[] dictCheckedArray = null;

		String strWordsArray[] = lookupWord(word, type);

		if(null == strWordsArray)
			return mContext.getResources().getString(R.string.keywords_null);

		if(type == MangoDictEng.DICT_TYPE_INDEX)
		{
			dictCheckedArray = MangoDictEng.mDictIndexChecked.split(";");
		}
		else if(type == MangoDictEng.DICT_TYPE_CAPTURE)
		{
			dictCheckedArray = MangoDictEng.mDictCaptureChecked.split(";");
		}
		else if(type == MangoDictEng.DICT_TYPE_MEMORIZE)
		{
			dictCheckedArray = MangoDictEng.mDictMemorizeChecked.split(";");
		}

		// This is for dictionary order.
		String dictContentArray[] = new String[dictCheckedArray.length];
		
		for (int i = 0; i < strWordsArray.length;) {
			String dictContent = "";
			
			if (null != strWordsArray[i] && null != strWordsArray[i + 1] && null != strWordsArray[i + 2])
			{
				int dictID =  Integer.parseInt(strWordsArray[i]);
				String strResPath = "file://" + MangoDictEng.DictPaths[dictID] + "res/";
				String strDictID = "conID" + strWordsArray[i++];

				String strDictName = strWordsArray[i++];
				String strDictContent = strWordsArray[i++];

				// Add "hide/show" script to dictionary name <div>, this can hide/show the dictionary content.
				strDictName = "<div onclick=\"javascript:var obj=document.getElementById('" 
					          + strDictID + "');if(obj.innerHTML==''){obj.innerHTML=" + strDictID + ";}else{"
					          + strDictID + "=obj.innerHTML;obj.innerHTML='';}\">" + strDictName + "</div>";

				// Change the style of dictionary name.
				strDictName = "<TABLE border=0 cellSpacing=0 cellPadding=0><TR><TD style=\"PADDING-LEFT:6px;PADDING-RIGHT:6px;BORDER-BOTTOM:#92b0dd 1px solid;"
							  + "BORDER-LEFT:#92b0dd 1px solid;BACKGROUND:#cfddf0;COLOR:#0000FF;BORDER-TOP:#92b0dd 1px solid;BORDER-RIGHT:#92b0dd 1px solid\" noWrap>"
							  + strDictName + "</TD><TD style=\"BORDER-BOTTOM:#92b0dd 1px solid\" height=\"36px\" width=\"100%\">&nbsp;</TD></TR></TABLE>";

				// Change '<img src="8CB0DC57.png">' to '<img src="/sdcard/.../res/8CB0DC57.png">'
				// Change '<IMG src='8CB0DC57.png'>' to '<img src='/sdcard/.../res/8CB0DC57.png'>'
				strDictContent = replaceHtmlTag(strDictContent, "img", "src", "src='" + strResPath, "' ");
				strDictContent = replaceHtmlTag(strDictContent, "img", "width", null, null);
				strDictContent = replaceHtmlTag(strDictContent, "img", "height", null, null);
				
				// Wrap a <div> to the dictionary content.
				strDictContent = "<div id='" + strDictID + "'>" + strDictContent + "</div>";

				dictContent = strDictName + strDictContent + "<br>";
				
				// This is for dictionary order.
				for(int k = 0; k < dictCheckedArray.length; k++)
				{
					if(MangoDictEng.DictPaths[dictID].indexOf(dictCheckedArray[k]) >= 0)
					{
						dictContentArray[k] = dictContent;
						break;
					}
				}
			}
			else
			{
				i += 3;	// Ignore this dictionary.
			}
		}

		for(int k = 0; k < dictContentArray.length; k++)
		{
			if(null != dictContentArray[k])
				dictHtmlData += dictContentArray[k];
		}

		if ("" == dictHtmlData) {
			return mContext.getResources().getString(R.string.keywords_null);
		}

		String textColor = GetTextHtmlColor();
		String wordColor = GetWordHtmlColor();
		String head = "<head><style>@font-face {font-family:'Unicode';src:url('file:///android_asset/fonts/Unicode.ttf');}";
		head += "@font-face {font-family:'KPhonetic';src:url('file:///android_asset/fonts/KPhonetic.ttf');}";
		head += " body,font{font-family:'Unicode';} i,b{font-family: sans-serif;}</style></head>";
		
		html = "<html>" + head + "<body style='color:" + textColor + "'>" + dictHtmlData + "</body></html>";

		html = html.replace("color:#TOBEREPLACE;", "color:" + wordColor + ";");

		return html;
	}

	public void showHtmlByResId(int resId, WebView webView)
	{
		showHtmlContent(mContext.getResources().getString(resId), webView);
	}
	
	public void showHtmlContent(String content, WebView webView)
	{
		if (content.indexOf("<body style='color:") < 0)
		{
			content = "<body style='color:" + GetTextHtmlColor() + "'>" + content + "</body>";
		}

		MyLog.v(TAG, "showHtmlContent()::content=" + content);
		
        try {
        	webView.loadDataWithBaseURL(null, content, MangoDictEng.MIME_TYPE, MangoDictEng.HTML_ENCODING, null);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        webView.scrollTo(0, 0);
	}

	public void initDicts() {
		mMangoDictEng.UnloadDicts();
		MyLog.v(TAG, "initDicts()::LoadDicts()::Begin");
		getDictPathsANames();
		saveDictionaryData();
		mMangoDictEng.LoadDicts(MangoDictEng.DictPaths, MangoDictEng.DictNames, MangoDictEng.DictTypes);
		MyLog.v(TAG, "initDicts()::LoadDicts()::End");
	}

    public String[] listWords(String word) {
    	return mMangoDictEng.ListWords(word);
    }

    public String[] fuzzyListWords(String word) {
    	return mMangoDictEng.FuzzyListWords(word);
    }
    
    public String[] patternListWords(String word) {
    	return mMangoDictEng.PatternListWords(word);
    }
    
    public String[] fullTextListWords(String word) {
    	return mMangoDictEng.FullTextListWords(word);
    }

	public String getBookName(String ifoPath) {
		return mMangoDictEng.GetBookName(ifoPath);
	}

	public void CancelLookup() {
		mMangoDictEng.CancelLookup();
	}

	public void destroy() {
		mMangoDictEng.releaseMangoDictEng();
	}
}
