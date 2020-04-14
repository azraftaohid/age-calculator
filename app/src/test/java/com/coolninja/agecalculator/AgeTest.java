package com.coolninja.agecalculator;

import com.coolninja.agecalculator.utilities.Age;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AgeTest {
    private Age mAge;

    @Before
    public void setUp() {
        mAge = new Age(2019, 1, 28);
    }

    @Test
    public void checkAgeCalculator() {
        long[] ageYMD = mAge.get(Age.MODE_YEAR_MONTH_DAY);
        long[] ageYD = mAge.get(Age.MODE_YEAR_DAY);

        assertThat(ageYD[1], Is.is(CoreMatchers.equalTo((long) 28)));
    }

    @Test
    public void checkLeapDaysCalculator() {
        Calendar start = Calendar.getInstance();
        Calendar c = Calendar.getInstance();

        start.set(Calendar.YEAR, 2016);
        start.set(Calendar.MONTH, 1);
        start.set(Calendar.DAY_OF_MONTH, 28);

        assertThat(Age.getNumberOfLeapDays(start, c), is(equalTo(2)));
    }

    @Test
    public void checkDurationCalculator() {
        long year1 = Age.calculateDuration(2019, 3, 28, 2020, 3, 28, Age.MODE_YEAR_MONTH_DAY)[Age.YEAR];

        assertThat(year1, is(equalTo((long) 1)));
    }

    @Test
    public void generalTesting() {
        Calendar c = Calendar.getInstance();
        Calendar prevC = Calendar.getInstance();

        prevC.set(Calendar.YEAR, 2020);
        prevC.set(Calendar.MONTH, 2);
        prevC.set(Calendar.DAY_OF_MONTH, 25);

        long daysDivisor = 1000 * 60 * 60 * 24;

        long durationInMillis = c.getTimeInMillis() - prevC.getTimeInMillis();
        assertThat((Long.valueOf((durationInMillis / daysDivisor) % 365).intValue()), is(equalTo(2)));
    }
}
