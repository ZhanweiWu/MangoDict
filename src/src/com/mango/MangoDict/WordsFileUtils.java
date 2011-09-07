package com.mango.MangoDict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class WordsFileUtils {

	private final String TAG = "WordsFileUtils";

	private final int MAX_COUNT = 99;

	final static String WORDSLIST_FOLDER = "wordslist/";
	final static String NEWWORDS_FILENAME = "newwords.wrd";
	final static String HISWORDS_FILENAME = "hiswords.wrd";

	private String mPath = null;
	private String mName = null;
	private ArrayList<String> mWordsArrayList = null;

	public WordsFileUtils(String path, String name) {
		mPath = path;
		mName = name;
		String[] wordsList = null;

		MyLog.v(TAG, "WordsFileUtils()::path=" + mPath);

		mWordsArrayList = new ArrayList<String>();
		
		if(null == MangoDictEng.mDictIndexAll || MangoDictEng.mDictIndexAll.equals(""))
		{
			return;
		}

		File f = new File(path);
		if(!f.exists())
		{
			f.mkdirs();
		}
		f = null;

		String data = read();
		if(null == data)
			return;
		
		wordsList = data.split(";");

		if (null != wordsList)
		{
			for (int i = 0; i < wordsList.length; i++)
			{
				mWordsArrayList.add(wordsList[i]);
			}
		}
	}

	public ArrayList<String> getArrayList() {
		return mWordsArrayList;
	}	
	
	public void addWord(String word) {
		String newword = word.replace(";", ""); // remove ';' if it exists in the word.

		mWordsArrayList.remove(newword);

    	if (mWordsArrayList.size() > MAX_COUNT)
    	{
    		mWordsArrayList.remove(mWordsArrayList.size() - 1);
    	}

		if(null != mWordsArrayList)
		{
			mWordsArrayList.add(0, newword);
		}		
	}


    public void save() {
    	BufferedWriter writer = null;
    	String data = "";
    	int cnt = mWordsArrayList.size();

		if(null == MangoDictEng.mDictIndexAll || MangoDictEng.mDictIndexAll.equals(""))
		{
			return;
		}

    	if(cnt <= 0)
    	{
    		return;
    	}
    	
    	if (cnt > MAX_COUNT)
    		cnt = MAX_COUNT;

    	for (int i = 0; i < cnt; i++)
    	{
    		data += mWordsArrayList.get(i) + ";";
    	}

		MyLog.v(TAG, "save()::data=" + data);

        try {
            File f = new File(mPath + mName);
            if (f.exists()) {
            	f.delete();
            }

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)), 8192);
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
    		MyLog.v(TAG, "save()::IOException");
        } finally {
            try {
                if (writer != null)
                	writer.close();
            } catch (IOException e) {
            }
        }
    }

    private String read() {
    	BufferedReader reader = null;
    	String data = null;

        try {
            File f = new File(mPath + mName);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                data = reader.readLine();
            }
        } catch (IOException e) {
    		MyLog.v(TAG, "read()::IOException");
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
            }
        }

        return data;
    }
}
