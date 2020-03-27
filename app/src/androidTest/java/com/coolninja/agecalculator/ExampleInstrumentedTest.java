package com.coolninja.agecalculator;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.coolninja.agecalculator.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.example.agecalculator", appContext.getPackageName());
    }

    @Test
    public void compareJsonObject() {
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        try {
            obj1.put("tada", 69);
            obj1.put("tadda", 46);
            obj2.put("tada", 69);
            obj2.put("tadda", 46);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals(obj1.toString(), obj2.toString());
    }
}
