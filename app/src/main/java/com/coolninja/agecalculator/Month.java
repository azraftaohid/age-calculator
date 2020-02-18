package com.coolninja.agecalculator;

public enum Month {
    JANUARY(31),
    FEBRUARY(28),
    MARCH(31),
    APRIL(30),
    MAY(31),
    JUNE(30),
    JULY(31),
    AUGUST(31),
    SPETEMBER(30),
    OCTOBER(31),
    NOVEMBER(30),
    DECEMBER(31);

    private int mNumberOfDays;

    Month(int numberOfDays) {
        mNumberOfDays = numberOfDays;
    }

    public int getNumberOfDays() {
        return mNumberOfDays;
    }
}
