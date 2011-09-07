package com.mango.MangoDict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.mango.MangoDict.Calendar.DateWidgetDayCell;
import com.mango.MangoDict.Calendar.DateWidgetDayHeader;
import com.mango.MangoDict.Calendar.DayStyle;
import com.mango.MangoDict.Calendar.SymbolButton;

public class MemorizeScheduleActivity extends Activity {

	private final String TAG = "MemorizeScheduleActivity";
	
	private MemorizeEng mMemorizeEng = null;
	int[] mScheduleData = null;
	int   mWeekOffset = 0;

	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();
	private Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();

	LinearLayout layContent = null;
	Button btnPrev = null;
	Button btnToday = null;
	Button btnNext = null;
	private TextView mCurrentDateTxtView;
	
	private int iFirstDayOfWeek = Calendar.SUNDAY;
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;

	private static int iDayHeaderHeight = 30;
	private static int iDayCellWidth = 46;
	private static int iDayCellHeight = 56;
	private static int iTotalWidth = (iDayCellWidth * 7);

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		MyLog.v(TAG, "onCreate()");

		// If the screen is portrait.
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			iDayCellWidth = 46;
			iDayCellHeight = 56;
			iTotalWidth = (iDayCellWidth * 7);
		}
		else  // If the screen is landscape.
		{
			iDayCellWidth = 66;
			iDayCellHeight = 30;
			iTotalWidth = (iDayCellWidth * 7);
		}

    	mMemorizeEng = MemorizeEng.createMemorizeEng();
		
		setContentView(generateContentView());
		calStartDate = getCalendarStartDate();
		DateWidgetDayCell daySelected = updateCalendar();
		updateControlsState();
		if (daySelected != null)
			daySelected.requestFocus();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		mMemorizeEng.releaseMemorizeEng();

		super.onDestroy();
	}

	private LinearLayout createLayout(int iOrientation) {
		LinearLayout lay = new LinearLayout(this);
		lay.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	private Button createButton(String sText, int iWidth, int iHeight) {
		Button btn = new Button(this);
		btn.setText(sText);
		btn.setLayoutParams(new LayoutParams(iWidth, iHeight));
		return btn;
	}

	private void generateTopButtons(LinearLayout layTopControls) {
		final int iHorPadding = 20;
		final int iSmallButtonWidth = 60;
		btnToday = createButton("", iTotalWidth - iSmallButtonWidth
				- iSmallButtonWidth + 6,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		btnToday.setPadding(iHorPadding, btnToday.getPaddingTop(), iHorPadding,
				btnToday.getPaddingBottom());
		btnToday.setBackgroundResource(android.R.drawable.btn_default_small);

		SymbolButton btnPrev = new SymbolButton(this,
				SymbolButton.symbol.arrowLeft);
		btnPrev.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnPrev.setBackgroundResource(android.R.drawable.btn_default_small);

		SymbolButton btnNext = new SymbolButton(this,
				SymbolButton.symbol.arrowRight);
		btnNext.setLayoutParams(new LayoutParams(iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnNext.setBackgroundResource(android.R.drawable.btn_default_small);

		// set events
		btnPrev.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				setPrevViewItem();
			}
		});
		btnToday.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {

				setTodayViewItem();
				String s = calToday.get(Calendar.YEAR) + "/"
						+ (calToday.get(Calendar.MONTH) + 1);
				btnToday.setText(s);
			}
		});
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				setNextViewItem();
			}
		});

		layTopControls.setGravity(Gravity.CENTER_HORIZONTAL);
		layTopControls.addView(btnPrev);
		layTopControls.addView(btnToday);
		layTopControls.addView(btnNext);
	}

	private View generateContentView() {
		LinearLayout layTopControls = null;
		LinearLayout layMain = createLayout(LinearLayout.VERTICAL);

		layMain.setPadding(0, 8, 0, 0);

		layTopControls = createLayout(LinearLayout.HORIZONTAL);
		layContent = createLayout(LinearLayout.VERTICAL);

		layContent.setGravity(Gravity.CENTER_HORIZONTAL);
		layMain.setGravity(Gravity.CENTER_HORIZONTAL);

		generateTopButtons(layTopControls);
		generateCalendar(layContent);
		layMain.addView(layTopControls);
		layMain.addView(layContent);

		mCurrentDateTxtView = new TextView(this);
		mCurrentDateTxtView.setWidth(iTotalWidth);
		mCurrentDateTxtView.setGravity(Gravity.CENTER_HORIZONTAL);

		layMain.addView(mCurrentDateTxtView);
		return layMain;
	}

	private View generateCalendarRow() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		layRow.setGravity(Gravity.CENTER_HORIZONTAL);

		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayCell dayCell = new DateWidgetDayCell(this, iDayCellWidth, iDayCellHeight);
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}

		return layRow;
	}

	private View generateCalendarHeader() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		layRow.setGravity(Gravity.CENTER_HORIZONTAL);
		
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayHeader day = new DateWidgetDayHeader(this,
					iDayCellWidth, iDayHeaderHeight);
			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}

		return layRow;
	}

	private void generateCalendar(LinearLayout layContent) {
		layContent.addView(generateCalendarHeader());
		days.clear();
		for (int iRow = 0; iRow < 6; iRow++) {
			layContent.addView(generateCalendarRow());
		}
	}

	private Calendar getCalendarStartDate() {
		MyLog.v(TAG, "getCalendarStartDate()");
		
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		UpdateStartDateForMonth();

		return calStartDate;
	}

	private DateWidgetDayCell updateCalendar() {
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;
		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());

		for (int i = 0; i < days.size(); i++) {
			int scheduleCnt = 0;
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
			DateWidgetDayCell dayCell = days.get(i);
			// check today
			boolean bToday = false;
			if (calToday.get(Calendar.YEAR) == iYear)
				if (calToday.get(Calendar.MONTH) == iMonth)
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
						bToday = true;
			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY)
					|| (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;

			if(null != mScheduleData && i >= mWeekOffset && i < mScheduleData.length + mWeekOffset )
			{
				scheduleCnt = mScheduleData[i - mWeekOffset];
				MyLog.v(TAG, "day=" + (i - mWeekOffset + 1) + "; mScheduleData=" + scheduleCnt);
			}

			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday, iMonthViewCurrentMonth, scheduleCnt);

			bSelected = false;
			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth)
						&& (iSelectedYear == iYear)) {
					bSelected = true;
				}
			dayCell.setSelected(bSelected);
			if (bSelected)
				daySelected = dayCell;
			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		layContent.invalidate();
		return daySelected;
	}

	private void UpdateStartDateForMonth() {
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		UpdateCurrentMonthDisplay();
		// update days for week
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
		
		mWeekOffset = iDay;
	}

	private void UpdateCurrentMonthDisplay() {
		MyLog.v(TAG, "UpdateCurrentMonthDisplay()");	

		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
		String s = calCalendar.get(Calendar.YEAR) + "/"
				+ (calCalendar.get(Calendar.MONTH) + 1);
		btnToday.setText(s);

		mScheduleData = mMemorizeEng.GetScheduleData(calCalendar.get(Calendar.YEAR), calCalendar.get(Calendar.MONTH) + 1);
	}

	private void setPrevViewItem() {
		MyLog.v(TAG, "setPrevViewItem()");

		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	private void setTodayViewItem() {
		MyLog.v(TAG, "setTodayViewItem()");
		
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);
		calStartDate.setTimeInMillis(calToday.getTimeInMillis());
		calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	private void setNextViewItem() {
		MyLog.v(TAG, "setNextViewItem()");
		
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
		public void OnClick(DateWidgetDayCell item) {
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
			item.setSelected(true);
			updateCalendar();
			updateControlsState();
		}
	};

	private void updateControlsState() {
		SimpleDateFormat dateFull = new SimpleDateFormat(getResources().getString(R.string.date_format));
		mCurrentDateTxtView.setText(new StringBuilder().append(dateFull.format(calSelected.getTime())));
	}
}
