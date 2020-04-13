package com.coolninja.agecalculator;

import com.coolninja.agecalculator.utilities.Birthday;

import org.junit.Test;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeneralTest {
    private static final String LOG_TAG = GeneralTest.class.getSimpleName();

    @Test
    public void random() {
        assertThat(LOG_TAG, is(equalTo("com.coolninja.agecalculator.GeneralTest")));
    }

    @Test
    public void unsignedIntegerTest() {
        int i = 2147483647;
        int j = i + 2;
        assertThat(j, is(equalTo(-2147483647)));
    }

    @Test
    public void arrayTest() {
        int[] array = new int[6];
        array[2] = 69;

        assertThat(array[2], is(equalTo(69)));
        assertThat(array[0], is(equalTo(0)));
    }

    @Test
    public void general() {
        String x = "tomato";
        String y = x;

        x.concat("es");

        Birthday a = new Birthday(2000, 11, 5);
        Birthday b = a;

        a.set(Birthday.DAY, 9);

        assertThat(y, is(equalTo("tomatoes"))); //false; a new instance of String was returned when it was updated
        assertThat(b.get(Birthday.DAY), is(equalTo(9))); //true
    }
}
