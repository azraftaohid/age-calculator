package com.coolninja.agecalculator.utilities;

import android.util.Log;

import java.util.Calendar;

public class Age {
    public static final int MODE_YEAR_MONTH_DAY = 0;
    public static final int MODE_YEAR_DAY = 1;
    public static final int MODE_MONTH_DAY = 2;
    public static final int MODE_DAY = 3;
    public static final int MODE_HOUR = 4;
    public static final int MODE_MINUTES = 5;
    public static final int MODE_SECONDS = 6;
    //Age format: {[YEAR], [MONTH], [DAY], [HOUR], [MINUTE], [SECOND]}
    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;
    public static final int HOUR = 3;
    public static final int MINUTE = 4;
    public static final int SECOND = 5;
    private static final String LOG_TAG = Age.class.getSimpleName();
    private static final int ELEMENTS = 6;

    //Base value must be in seconds
    private static final long TO_MINUTES_DIVISOR = 60; //Seconds to minutes
    private static final long TO_HOURS_DIVISOR = TO_MINUTES_DIVISOR * 60;
    private static final long TO_DAYS_DIVISOR = TO_HOURS_DIVISOR * 24;
    private static final long TO_YEARS_DIVISOR = TO_DAYS_DIVISOR * 365; //365 is important and can not be 366

    private int mBirthYear;
    private int mBirthMonth;
    private int mBirthDay;

    public Age(int birthYear, int birthMonth, int birthDay) {
        mBirthYear = birthYear;
        mBirthMonth = birthMonth;
        mBirthDay = birthDay;
    }

    public Age(Birthday dateOfBirth) {
        mBirthYear = dateOfBirth.get(Birthday.YEAR);
        mBirthMonth = dateOfBirth.get(Birthday.MONTH);
        mBirthDay = dateOfBirth.get(Birthday.DAY);
    }

    public static long[] calculateDuration(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay, int returnMode) {
        long[] duration = new long[ELEMENTS];

        Calendar endDate = Calendar.getInstance();
        endDate.set(endYear, endMonth, endDay);

        Calendar startDate = Calendar.getInstance();
        startDate.set(startYear, startMonth, startDay, 0, 0, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        long durationInSeconds = (endDate.getTimeInMillis() - startDate.getTimeInMillis()) / 1000;
        int durationInDays = Long.valueOf(durationInSeconds / TO_DAYS_DIVISOR).intValue();

        if (returnMode == MODE_YEAR_MONTH_DAY || returnMode == MODE_YEAR_DAY || returnMode == MODE_MONTH_DAY) {
            int remainderDaysAfterYear = durationInDays % 365 - getNumberOfLeapDays(startYear, startMonth, startDay, endYear, endMonth, endDay);

            if (isLeapYear(startYear) && (startMonth < Calendar.FEBRUARY || (startMonth == Calendar.FEBRUARY && startDay < 29))) {
                remainderDaysAfterYear++;
            }

            int remainderDaysAfterMonth = getDurationInRemainderDaysAfterMonth(Month.values()[startMonth], startDay, endDay);
            int remainderMonthsAfterYear = getDurationInRemainderMonthsAfterYear(startMonth, endMonth,
                    startDay > endDay);
            int durationInYears = Double.valueOf(Math.floor((float) durationInSeconds / TO_YEARS_DIVISOR)).intValue();

            if (remainderDaysAfterYear < 0) {
                durationInYears--;
                remainderDaysAfterYear += 365;
            }


            switch (returnMode) {
                case MODE_YEAR_MONTH_DAY:
                    duration[YEAR] = durationInYears;
                    duration[MONTH] = remainderMonthsAfterYear;
                    duration[DAY] = remainderDaysAfterMonth;
                    return duration;
                case MODE_YEAR_DAY:
                    duration[YEAR] = durationInYears;
                    duration[DAY] = remainderDaysAfterYear;
                    return duration;
                case MODE_MONTH_DAY:
                    duration[MONTH] = (durationInYears * 12) + remainderMonthsAfterYear;
                    duration[DAY] = remainderDaysAfterMonth;
                    return duration;
            }

        } else if (returnMode == MODE_DAY) {
            duration[DAY] = durationInDays;
            return duration;
        } else if (returnMode == MODE_HOUR) {
            duration[HOUR] = durationInSeconds / TO_HOURS_DIVISOR;
            return duration;
        } else if (returnMode == MODE_MINUTES) {
            duration[MINUTE] = durationInSeconds / TO_MINUTES_DIVISOR;
            return duration;
        } else if (returnMode == MODE_SECONDS) {
            duration[SECOND] = durationInSeconds;
            return duration;
        } else {
            Log.w(LOG_TAG, "Unsupported mode passed: " + returnMode);
        }

        return duration;
    }

    private static boolean isLeapYear(int year) {
        return (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0));
    }

    private static int getDurationInRemainderMonthsAfterYear(int startMonth, int endMonth, boolean isStartDateGreaterThanEndDate) {
        if (endMonth > startMonth) {
            if (isStartDateGreaterThanEndDate)
                return endMonth - (startMonth + 1);
            return endMonth - startMonth;
        } else if (endMonth < startMonth) {
            return 12 - (startMonth - endMonth);
        } else if (isStartDateGreaterThanEndDate) { //and startMonth == endMonth
            return 11; //If you were born after a date in the same month, you're 11 months and a few days old on that day
        } else { //startMonth == endMonth and you were born before or on that day of the month
            return 0;
        }
    }

    private static int getDurationInRemainderDaysAfterMonth(Month startMonth, int startDay, int endDay) {
        if (endDay > startDay) {
            return endDay - startDay;
        } else {
            int days = (startMonth.getNumberOfDays() - startDay) + endDay;
            return days == startMonth.getNumberOfDays() ? 0 : days;
        }
    }

    private static int getNumberOfLeapDays(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        int leapDays = 0;

        boolean isStartYearALeapYear = isLeapYear(startYear);
        boolean isEndYearALeapYear = isLeapYear(endYear);

        if (isStartYearALeapYear && (startMonth < Calendar.FEBRUARY || (startMonth == Calendar.FEBRUARY && startDay < 29))) {
            leapDays++;
        }

        if (endYear != startYear && (isEndYearALeapYear && (endMonth > Calendar.FEBRUARY || (endMonth == Calendar.FEBRUARY &&
                endDay == 29)))) {
            leapDays++;
        }

        for (int i = startYear + 1; i < endYear; i++) {
            if (isLeapYear(i)) {
                leapDays++;
            }
        }

        return leapDays;
    }

    @Deprecated
    public static int getNumberOfLeapDays(Calendar startDate, Calendar endDate) {
        int leapDays = 0;

        int startMonth = startDate.get(Calendar.MONTH);
        int startYear = startDate.get(Calendar.YEAR);
        int startDay = startDate.get(Calendar.DAY_OF_MONTH);
        int endMonth = endDate.get(Calendar.MONTH);
        int endYear = endDate.get(Calendar.YEAR);
        int endDay = endDate.get(Calendar.DAY_OF_MONTH);

        boolean isStartYearALeapYear = isLeapYear(startYear);
        boolean isEndYearALeapYear = isLeapYear(endYear);

        if (isStartYearALeapYear && (startMonth < Calendar.FEBRUARY || (startMonth == Calendar.FEBRUARY && startDay < 29))) {
            leapDays++;
        }

        if (endYear != startYear && (isEndYearALeapYear && (endMonth > Calendar.FEBRUARY || (endMonth == Calendar.FEBRUARY &&
                endDay == 29)))) {
            leapDays++;
        }

        for (int i = startDate.get(Calendar.YEAR) + 1; i < endDate.get(Calendar.YEAR); i++) {
            if (isLeapYear(i)) {
                leapDays++;
            }
        }

        return leapDays;
    }

    public long[] get(int mode) {
        Calendar c = Calendar.getInstance();
        return calculateDuration(mBirthYear, mBirthMonth, mBirthDay, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), mode);
    }

    public long[] getDurationBeforeNextBirthday(int mode) {
        Calendar c = Calendar.getInstance();

        if (c.get(Calendar.MONTH) < mBirthMonth || (c.get(Calendar.MONTH) == mBirthMonth && c.get(Calendar.DAY_OF_MONTH) < mBirthDay)) {
            //birth date - today
            return calculateDuration(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR), mBirthMonth, mBirthDay, mode);
        } else {
            // birth date in next year - today
            return calculateDuration(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.YEAR) + 1, mBirthMonth, mBirthDay, mode);
        }

    }

}
