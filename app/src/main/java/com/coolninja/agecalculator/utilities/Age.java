package com.coolninja.agecalculator.utilities;

import java.util.Calendar;

public class Age {
    private int mBirthYear;
    private int mBirthMonth;
    private int mBirthDay;
    private int mAgeYears;
    private int mAgeMonths;
    private int mAgeDays;

    public Age(int birthYear, int birthMonth, int birthDay) {
        mBirthYear = birthYear;
        mBirthMonth = birthMonth;
        mBirthDay = birthDay;

        calculateAge();
    }

    public Age(Birthday dateOfBirth) {
        mBirthYear = dateOfBirth.get(Birthday.YEAR);
        mBirthMonth = dateOfBirth.get(Birthday.MONTH);
        mBirthDay = dateOfBirth.get(Birthday.DAY);

        calculateAge();
    }

    private void calculateAge() {
        Calendar c = Calendar.getInstance();
        long toDaysDivisor = 1000 * 60 * 60 * 24; //millis to seconds to minutes to days
        long toYearsDivisor = toDaysDivisor * 365; //365 is important and can not be 366

        Calendar dateOfBirth = Calendar.getInstance();
        dateOfBirth.set(Calendar.YEAR, mBirthYear);
        dateOfBirth.set(Calendar.MONTH, mBirthMonth);
        dateOfBirth.set(Calendar.DAY_OF_MONTH, mBirthDay);

        long ageInMillis = c.getTimeInMillis() - dateOfBirth.getTimeInMillis();
        int ageInDays = Long.valueOf((ageInMillis / toDaysDivisor) % 365).intValue() - getNumberOfLeapDays(dateOfBirth, c);
        mAgeDays = getDurationInDays(Month.values()[dateOfBirth.get(Calendar.MONTH)],
                dateOfBirth.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));
        mAgeMonths = getDurationInMonths(dateOfBirth.get(Calendar.MONTH), c.get(Calendar.MONTH),
                dateOfBirth.get(Calendar.DAY_OF_MONTH) > c.get(Calendar.DAY_OF_MONTH));
        mAgeYears = Double.valueOf(Math.floor((float) ageInMillis / toYearsDivisor)).intValue();

        if (ageInDays < 0) {
            mAgeYears--;
            ageInDays += 365;
        }
    }

    private boolean isLeapYear(int year) {
        return (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0));
    }

    private int getDurationInMonths(int startMonth, int endMonth, boolean isStartDateGreaterThanEndDate) {
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

    private int getDurationInDays(Month startMonth, int startDay, int endDay) {
        if (endDay > startDay) {
            return endDay - startDay;
        } else {
            int days = (startMonth.getNumberOfDays() - startDay) + endDay;
            return days == startMonth.getNumberOfDays()? 0 : days;
        }
    }

    private int getNumberOfLeapDays(Calendar startDate, Calendar endDate) {
        int leapDays = 0;
        boolean isLeapYear = isLeapYear(startDate.get(Calendar.YEAR));

        if (isLeapYear && startDate.get(Calendar.MONTH) <= Calendar.FEBRUARY) {
            leapDays++;
        }

        if (isLeapYear && (endDate.get(Calendar.MONTH) >= Calendar.FEBRUARY
                || (endDate.get(Calendar.MONTH) == Calendar.FEBRUARY && endDate.get(Calendar.DAY_OF_MONTH) == 29))) {
            leapDays++;
        }

        for (int i = startDate.get(Calendar.YEAR) + 1; i < endDate.get(Calendar.YEAR); i++) {
            if (isLeapYear(i)) {
                leapDays++;
            }
        }

        return leapDays;
    }

    public int getDays() {
        return mAgeDays;
    }

    public int getMonths() {
        return mAgeMonths;
    }

    public int getYears() {
        return mAgeYears;
    }
}
