package com.coolninja.agecalculator.utilities;

public class Age {
    private int mYear;
    private int mMonth;
    private int mDay;

    public Age(int years, int months, int days) {
        mYear = years;
        mMonth = months;
        mDay = days;
    }

    public int getDay() {
        return mDay;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }
}
