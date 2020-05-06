package com.coolninja.agecalculator;

import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.coolninja.agecalculator.utilities.ProfileViewsAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AdapterTest {
    Context mContext;
    ProfileViewsAdapter mAdapter;

    @Before
    public void setUp() {
        mContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();


    }

    @Test
    public void itemType() {

    }
}
