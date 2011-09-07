package com.mango.MangoDict;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MemorizeStatisticActivity extends Activity {

	private final String TAG = "MemorizeStatisticActivity";

	private MemorizeEng mMemorizeEng = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.v(TAG, "onCreate()");

		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.memorize_statistic);

    	mMemorizeEng = MemorizeEng.createMemorizeEng();
    	
    	TextView gradeTxt = (TextView) findViewById(R.id.gradeTxt);
    	TextView easinessTxt = (TextView) findViewById(R.id.easinessTxt);
    	TextView repetitionsTxt = (TextView) findViewById(R.id.repetitionsTxt);
    	TextView lapsesTxt = (TextView) findViewById(R.id.lapsesTxt);
    	TextView dayssincelastrepTxt = (TextView) findViewById(R.id.dayssincelastrepTxt);
    	TextView dayssincenextrepTxt = (TextView) findViewById(R.id.dayssincenextrepTxt);
    	
    	TextView grade0Txt = (TextView) findViewById(R.id.grade0Txt);
    	TextView grade1Txt = (TextView) findViewById(R.id.grade1Txt);
    	TextView grade2Txt = (TextView) findViewById(R.id.grade2Txt);
    	TextView grade3Txt = (TextView) findViewById(R.id.grade3Txt);
    	TextView grade4Txt = (TextView) findViewById(R.id.grade4Txt);
    	TextView grade5Txt = (TextView) findViewById(R.id.grade5Txt);
    	
    	TextView progressTxt = (TextView) findViewById(R.id.progressTxt);
    	TextView dateOfStartTxt = (TextView) findViewById(R.id.dateOfStartTxt);
    	
    	int[] cardData = mMemorizeEng.GetCurrentCardData();
    	int[] gradeData = mMemorizeEng.GetGradeData();
    	int[] cardsProgress = mMemorizeEng.GetCardsProgress();
    	int[] dateOfStart = mMemorizeEng.GetDateOfStart();

    	if (null != dateOfStart)
    	{
    		Calendar calendar = Calendar.getInstance();
    		SimpleDateFormat dateFull = new SimpleDateFormat(getResources().getString(R.string.date_format));
    		calendar.set(dateOfStart[0], dateOfStart[1] - 1, dateOfStart[2]);
    		dateOfStartTxt.setText(new StringBuilder().append(dateFull.format(calendar.getTime())));
    	}

    	if (null != cardsProgress)
    	{
    		if(cardsProgress[1] > 0)
    		{
	    		double progress = (double)cardsProgress[0] / (double)cardsProgress[1] * 100;
	    		progress = MangoDictUtils.roundDouble(progress, 2, BigDecimal.ROUND_HALF_UP);
	    		progressTxt.setText(cardsProgress[0] + " / " + cardsProgress[1] + " [" + Double.toString(progress) + "%]");
    		}
    	}

    	if (null != cardData)
    	{
    		gradeTxt.setText(Integer.toString(cardData[0]));
    		easinessTxt.setText(Double.toString((double)cardData[1] / 100));
    		repetitionsTxt.setText(Integer.toString(cardData[2]));
    		lapsesTxt.setText(Integer.toString(cardData[3]));
    		dayssincelastrepTxt.setText(Integer.toString(cardData[4]));
    		dayssincenextrepTxt.setText(Integer.toString(cardData[5]));
    	}

    	if (null != gradeData)
    	{
    		double[] grades = new double[6];
    		long total = gradeData[0] + gradeData[1] + gradeData[2] + gradeData[3] + gradeData[4] + gradeData[5];
    		
    		if(total > 0)
    		{
	    		grades[0] = (double)gradeData[0] / total * 100;
	    		grades[1] = (double)gradeData[1] / total * 100;
	    		grades[2] = (double)gradeData[2] / total * 100;
	    		grades[3] = (double)gradeData[3] / total * 100;
	    		grades[4] = (double)gradeData[4] / total * 100;
	    		grades[5] = (double)gradeData[5] / total * 100;
	    		
	    		grades[0] = MangoDictUtils.roundDouble(grades[0], 2, BigDecimal.ROUND_HALF_UP); 
	    		grades[1] = MangoDictUtils.roundDouble(grades[1], 2, BigDecimal.ROUND_HALF_UP);
	    		grades[2] = MangoDictUtils.roundDouble(grades[2], 2, BigDecimal.ROUND_HALF_UP);
	    		grades[3] = MangoDictUtils.roundDouble(grades[3], 2, BigDecimal.ROUND_HALF_UP);
	    		grades[4] = MangoDictUtils.roundDouble(grades[4], 2, BigDecimal.ROUND_HALF_UP);
	    		grades[5] = MangoDictUtils.roundDouble(grades[5], 2, BigDecimal.ROUND_HALF_UP);
    		}
    		else
    		{
    			grades[0] = 0;
    			grades[1] = 0;
    			grades[2] = 0;
    			grades[3] = 0;
    			grades[4] = 0;
    			grades[5] = 0;
    		}

    		grade0Txt.setText(gradeData[0] + " [" + Double.toString(grades[0]) + "%]");
    		grade1Txt.setText(gradeData[1] + " [" + Double.toString(grades[1]) + "%]");
    		grade2Txt.setText(gradeData[2] + " [" + Double.toString(grades[2]) + "%]");
    		grade3Txt.setText(gradeData[3] + " [" + Double.toString(grades[3]) + "%]");
    		grade4Txt.setText(gradeData[4] + " [" + Double.toString(grades[4]) + "%]");
    		grade5Txt.setText(gradeData[5] + " [" + Double.toString(grades[5]) + "%]");
    	}
	}

	@Override
	protected void onDestroy() {
		MyLog.v(TAG, "onDestroy()");

		mMemorizeEng.releaseMemorizeEng();

		super.onDestroy();
	}
}
