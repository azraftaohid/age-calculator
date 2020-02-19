package com.coolninja.agecalculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Calendar mDateOfBirth;

    private TextView mCurrentAgeTextView;
    private SharedPreferences pref;

    private final int EXPECTATION_RESULT_CODE = 1111;
    private final int DOB_REQUEST = 1112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences(getString(R.string.user_pref_key), MODE_PRIVATE);

        if (!pref.contains(getString(R.string.birth_day_key))) {
            launchWelcomeActivity();
        }

        setContentView(R.layout.activity_main);
        mCurrentAgeTextView = findViewById(R.id.tv_current_age);
        mDateOfBirth = Calendar.getInstance();

        if (pref.contains(getString(R.string.birth_day_key))) {
            int birthDay = pref.getInt(getString(R.string.birth_day_key), Integer.parseInt(getString(R.string.default_birth_day_key)));
            int birthMonth = pref.getInt(getString(R.string.birth_month_key), Integer.parseInt(getString(R.string.default_birth_month_key)));
            int birthYear = pref.getInt(getString(R.string.birth_year_key), Integer.parseInt(getString(R.string.default_birth_year_key)));
            mDateOfBirth.set(birthYear, birthMonth, birthDay);
            displayAge();
        }

    }

    private void displayAge() {
        Calendar c = Calendar.getInstance();
        long toDaysDivider = 1000 * 60 * 60 * 24; //millis to seconds to minutes to days
        long toYearsDivider = toDaysDivider * 365; //365 is important and can not be 366

        long ageInMillis = c.getTimeInMillis() - mDateOfBirth.getTimeInMillis();
        int ageDays = Long.valueOf((ageInMillis / toDaysDivider) % 365).intValue() - getNumberOfLeapDays(mDateOfBirth, c);
        int ageReminderDays = getDurationInDays(Month.values()[c.get(Calendar.MONTH)],
                mDateOfBirth.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));
        int ageMonths = getDurationInMonths(mDateOfBirth.get(Calendar.MONTH), c.get(Calendar.MONTH));
        int ageYears = Double.valueOf(Math.floor(ageInMillis / toYearsDivider)).intValue();

        if (ageDays < 0) {
            ageYears--;
            ageDays += 365;
        }

        mCurrentAgeTextView.setText(String.format(getString(R.string.display_age_years_months_days), ageYears, ageMonths, ageReminderDays));
    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, DOB_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DOB_REQUEST && resultCode == RESULT_OK) {
            int birthDay = pref.getInt(getString(R.string.birth_day_key), Integer.parseInt(getString(R.string.default_birth_day_key)));
            int birthMonth = pref.getInt(getString(R.string.birth_month_key), Integer.parseInt(getString(R.string.default_birth_month_key)));
            int birthYear = pref.getInt(getString(R.string.birth_year_key), Integer.parseInt(getString(R.string.default_birth_year_key)));
            mDateOfBirth.set(birthYear, birthMonth, birthDay);
            displayAge();
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

    private boolean isLeapYear(int year) {
        return (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0));
    }

    private int getDurationInMonths(int startMonth, int endMonth) {
        return (endMonth - startMonth) >= 0 ? endMonth - startMonth : 12 - (startMonth - endMonth);
    }

    private int getDurationInDays(Month startMonth, int startDay, int endDay) {
        if (endDay > startDay) {
            return endDay - startDay;
        } else {
            return (startMonth.getNumberOfDays() - startDay) + endDay;
        }
    }
}
