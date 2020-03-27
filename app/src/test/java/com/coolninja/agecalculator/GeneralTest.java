package com.coolninja.agecalculator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeneralTest {
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
}
