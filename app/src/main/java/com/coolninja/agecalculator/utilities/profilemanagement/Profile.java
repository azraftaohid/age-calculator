package com.coolninja.agecalculator.utilities.profilemanagement;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import com.coolninja.agecalculator.utilities.BirthdayPickerDialog;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Month;

import java.util.Calendar;

public class Profile {
    static final int DEFAULT_ERROR_CODE = -1;
    private int mId = DEFAULT_ERROR_CODE;
    private String mName;
    private Calendar mDateOfBirth;
    private int mAgeInYear;
    private int mAgeInYearMonth;
    private int mAgeInMonthDays;
    private int mAgeInYearDays;

    private BirthdayPickerDialog mBirthdayPicker;
    private ProfileManagerInterface.onProfileUpdateListener mProfileUpdateListener;

    public Profile() {
        mId = ProfileManager.generateProfileId();
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

    public Profile(String name, Calendar dateOfBirth, ProfileManagerInterface.onProfileUpdateListener onProfileUpdateListener) {
        mProfileUpdateListener = onProfileUpdateListener;
        mId = ProfileManager.generateProfileId();
        setName(name);
        setDateOfBirth(dateOfBirth);

        mBirthdayPicker = BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDateOfBirth = Calendar.getInstance();
                newDateOfBirth.set(Calendar.YEAR, year);
                newDateOfBirth.set(Calendar.MONTH, month);
                newDateOfBirth.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                setDateOfBirth(newDateOfBirth);
            }
        }, dateOfBirth.get(Calendar.YEAR), dateOfBirth.get(Calendar.MONTH), dateOfBirth.get(Calendar.DAY_OF_MONTH));
    }

    public void setName(String newName) {
        String previousName = mName;
        mName = newName;

        if (mProfileUpdateListener != null)
            mProfileUpdateListener.onProfileNameChange(mId, newName, previousName);
    }

    public void setDateOfBirth(Calendar newDateOfBirth) {
        Calendar previousDob = getDateOfBirth();
        mDateOfBirth = newDateOfBirth;

        if (mProfileUpdateListener != null)
            mProfileUpdateListener.onProfileDateOfBirthChange(mId, newDateOfBirth, previousDob);
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

    public String getName() {
        return mName;
    }

    public void setBirthdayPicker(BirthdayPickerDialog birthdayPicker) {
        mBirthdayPicker = birthdayPicker;
    }

    public BirthdayPickerDialog getBirthdayPicker() {
        return mBirthdayPicker;
    }

    public int getId() {
        return mId;
    }

    public Calendar getDateOfBirth() {
        return mDateOfBirth;
    }

    void setId(int id) {
        mId = id;
    }

}