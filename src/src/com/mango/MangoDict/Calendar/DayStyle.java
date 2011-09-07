package com.mango.MangoDict.Calendar;

import java.util.*;

public class DayStyle {

	private static String[] getWeekDayNames() {
		String[] vec = new String[10];
		vec[Calendar.SUNDAY] = "Sun";
		vec[Calendar.MONDAY] = "Mon";
		vec[Calendar.TUESDAY] = "Tue";
		vec[Calendar.WEDNESDAY] = "Wed";
		vec[Calendar.THURSDAY] = "Thu";
		vec[Calendar.FRIDAY] = "Fri";
		vec[Calendar.SATURDAY] = "Sat";

		return vec;
	}

	public static String getWeekDayName(int iDay) {
		return vecStrWeekDayNames[iDay];
	}

	// fields
	private final static String[] vecStrWeekDayNames = getWeekDayNames();

	// fields
	public final static int iColorFrameHeader = 0xff666666;
	public final static int iColorFrameHeaderHoliday = 0xff707070;
	public final static int iColorTextHeader = 0xffcccccc;
	public final static int iColorTextHeaderHoliday = 0xffd0d0d0;

	public final static int iColorText = 0xffdddddd;
	public final static int iColorBkg = 0xff888888;
	public final static int iColorTextHoliday = 0xfff0f0f0;
	public final static int iColorBkgHoliday = 0xffaaaaaa;

	public final static int iColorTextToday = 0xff002200;
	public final static int iColorBkgToday = 0xff88bb88;

	public final static int iColorTextSelected = 0xff001122;
	public final static int iColorBkgSelectedLight = 0xffbbddff;
	public final static int iColorBkgSelectedDark = 0xff225599;
	public final static int iColorRev = 0xff0000ff;
	
	public final static int iColorTextFocused = 0xff221100;
	public final static int iColorBkgFocusLight = 0xffffddbb;
	public final static int iColorBkgFocusDark = 0xffaa5500;

	// methods
	public static int getColorFrameHeader(boolean bHoliday) {
		if (bHoliday)
			return iColorFrameHeaderHoliday;
		return iColorFrameHeader;
	}

	public static int getColorTextHeader(boolean bHoliday) {
		if (bHoliday)
			return iColorTextHeaderHoliday;
		return iColorTextHeader;
	}

	public static int getColorText(boolean bHoliday, boolean bToday) {
		if (bToday)
			return iColorTextToday;
		if (bHoliday)
			return iColorTextHoliday;
		return iColorText;
	}

	public static int getColorBkg(boolean bHoliday, boolean bToday) {
		if (bToday)
			return iColorBkgToday;
		if (bHoliday)
			return iColorBkgHoliday;
		return iColorBkg;
	}

	public static int getWeekDay(int index, int iFirstDayOfWeek) {
		int iWeekDay = -1;

		if (iFirstDayOfWeek == Calendar.MONDAY) {
			iWeekDay = index + Calendar.MONDAY;
			if (iWeekDay > Calendar.SATURDAY)
				iWeekDay = Calendar.SUNDAY;
		}

		if (iFirstDayOfWeek == Calendar.SUNDAY) {
			iWeekDay = index + Calendar.SUNDAY;
		}

		return iWeekDay;
	}

}
