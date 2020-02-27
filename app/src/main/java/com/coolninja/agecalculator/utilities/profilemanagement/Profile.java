package com.coolninja.agecalculator.utilities.profilemanagement;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coolninja.agecalculator.utilities.BirthdayPickerDialog;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Month;

import java.util.Calendar;

public class Profile {
    private int mId = -1;
    private String mName;
    private Calendar mDateOfBirth;
    private int mAgeInYear;
    private int mAgeInYearMonth;
    private int mAgeInMonthDays;
    private int mAgeInYearDays;

    private BirthdayPickerDialog mBirthdayPicker;

    public Profile() {
        Calendar current = Calendar.getInstance();
        mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDateOfBirth.set(Calendar.YEAR, year);
                mDateOfBirth.set(Calendar.MONTH, month);
                mDateOfBirth.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        }, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
    }

    public Profile(@Nullable String name, @NonNull Calendar dateOfBirth) {
        mName = name;
        mDateOfBirth = dateOfBirth;

        mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mDateOfBirth.set(Calendar.YEAR, year);
                mDateOfBirth.set(Calendar.MONTH, month);
                mDateOfBirth.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        }, dateOfBirth.get(Calendar.YEAR), dateOfBirth.get(Calendar.MONTH), dateOfBirth.get(Calendar.DAY_OF_MONTH));
    }

    private void calculateAge() {
        Calendar c = Calendar.getInstance();
        long toDaysDivisor = 1000 * 60 * 60 * 24; //millis to seconds to minutes to days
        long toYearsDivisor = toDaysDivisor * 365; //365 is important and can not be 366

        long ageInMillis = c.getTimeInMillis() - mDateOfBirth.getTimeInMillis();
        mAgeInYearDays = Long.valueOf((ageInMillis / toDaysDivisor) % 365).intValue() - getNumberOfLeapDays(mDateOfBirth, c);
        mAgeInMonthDays = getDurationInDays(Month.values()[mDateOfBirth.get(Calendar.MONTH)],
                mDateOfBirth.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));
        mAgeInYearMonth = getDurationInMonths(mDateOfBirth.get(Calendar.MONTH), c.get(Calendar.MONTH),
                mDateOfBirth.get(Calendar.DAY_OF_MONTH) > c.get(Calendar.DAY_OF_MONTH));
        mAgeInYear = Double.valueOf(Math.floor((float) ageInMillis / toYearsDivisor)).intValue();

        if (mAgeInYearDays < 0) {
            mAgeInYear--;
            mAgeInYearDays += 365;
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

    public Age getAge() {
        calculateAge();

        return new Age(mAgeInYear, mAgeInYearMonth, mAgeInMonthDays);
    }

    void setName(String name) {
        mName = mName;
    }

    public String getName() {
        return mName;
    }

    public void setBirthdayPicker(BirthdayPickerDialog birthdayPicker) {
        mBirthdayPicker = birthdayPicker;
    }

    public BirthdayPickerDialog getBirthdayPicker() {
        return mBirthdayPicker;
    }

    void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public Calendar getDateOfBirth() {
        return mDateOfBirth;
    }

    void setDateOfBirth(Calendar dateOfBirth) {
        this.mDateOfBirth = dateOfBirth;
    }
}
