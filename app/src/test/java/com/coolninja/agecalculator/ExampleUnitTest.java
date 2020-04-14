package com.coolninja.agecalculator;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

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

        assertEquals(obj1, obj2);
    }

    @Before
    public void setUp() {

    }
}