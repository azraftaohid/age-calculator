package thegoodcompany.aetate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;

import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.codes.Error;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ExampleInstrumentedTest {
    private static final String LOG_TAG = ExampleInstrumentedTest.class.getSimpleName();
    private static final String PROFILE_MANAGER_PREF = "com.coolninja.aetate.aetate.pref.PROFILEMANAGER";
    private static final String TEST_PREF = "com.coolninja.aetate.pref.TEST";
    private static final String TEST_STRING_KEY = "com.coolninja.aetate.pref.TEST.STRING";

    private Context mContext;
    private Resources mResources;

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

    @Test
    public void jsonObjectValueReplace() {
        Profile profile = new Profile("Simple Name", new Birthday(2000, 5, 13), null);
        JSONObject obj = profile.toJSONObject();

        try {
            Log.v(LOG_TAG, "JSON Object before: \n" + obj.toString(4));
            obj.put("profile.name", "Updated Name");
            Log.v(LOG_TAG, "JSON Object after: \n" + obj.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Before
    public void setUp() {
        mContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();
        mResources = mContext.getResources();
    }

    @Test
    public void staticVsEnum() {
        int attempts = 400000;

        Calendar startTimeForStatic = Calendar.getInstance();
        for (int i = 0; i < attempts; i++) {
            int x = Error.DEFAULT;
        }
        Calendar endTImeForStatic = Calendar.getInstance();

        Calendar startTimeForEnum = Calendar.getInstance();
        for (int i = 0; i < attempts; i++) {
            int x = thegoodcompany.aetate.utilities.Error.DEFAULT.getCode();
        }
        Calendar endTimeForEnum = Calendar.getInstance();

        long timeTakenForStatic = endTImeForStatic.getTimeInMillis() - startTimeForStatic.getTimeInMillis();
        long timeTakenForEnum = endTimeForEnum.getTimeInMillis() - startTimeForEnum.getTimeInMillis();

        assertThat(timeTakenForEnum, is(lessThan(timeTakenForStatic)));
    }

    @Test
    public void resourceVsEnum() {
        int attempts = 400000;

        Calendar startTimeForResource = Calendar.getInstance();
        for (int i = 0; i < attempts; i++) {
            int x = mResources.getInteger(R.integer.default_error_code);
        }
        Calendar endTImeForResource = Calendar.getInstance();

        Calendar startTimeForEnum = Calendar.getInstance();
        for (int i = 0; i < attempts; i++) {
            int x = thegoodcompany.aetate.utilities.Error.DEFAULT.getCode();
        }
        Calendar endTimeForEnum = Calendar.getInstance();

        long timeTakenForResource = endTImeForResource.getTimeInMillis() - startTimeForResource.getTimeInMillis();
        long timeTakenForEnum = endTimeForEnum.getTimeInMillis() - startTimeForEnum.getTimeInMillis();

        assertThat(timeTakenForEnum, is(greaterThan(timeTakenForResource)));
    }

    @Test
    public void sharedPrefDelay() {
        int attempts = 10;
        int i = 0;

        Calendar startTime = Calendar.getInstance();
        for (; i < attempts; i++) {
            SharedPreferences pref = mContext.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        }
        Calendar endTime = Calendar.getInstance();

        long timeTaken = endTime.getTimeInMillis() - startTime.getTimeInMillis();

        Log.d(LOG_TAG, "Time taken to invoke shared preference for " + attempts + " times: " + timeTaken + " milliseconds");
    }

    @Test
    public void gettingCalenderInstanceLatency() {
        int attempts = 10000;
        int i = 0;

        Calendar startTime = Calendar.getInstance();
        for (; i < attempts; i++) {
            Calendar c = Calendar.getInstance();
        }
        Calendar endTime = Calendar.getInstance();

        long timeTaken = endTime.getTimeInMillis() - startTime.getTimeInMillis();

        Log.d(LOG_TAG, "Time taken to get new calender instance for " + attempts + " times: " + timeTaken + " milliseconds");
    }

    @Before
    public void gettingReadyPreferenceDelay() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < 200; i++) {
            for (int i2 = 0; i2 < 5; i2++) {
                str.append("tadatada ttaaddaa tttaaadddaaa ");
            }
        }


        Calendar startTime = Calendar.getInstance();

        SharedPreferences pref = mContext.getSharedPreferences(TEST_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TEST_STRING_KEY, str.toString());
        editor.apply();

        long timeTaken = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();

        Log.d(LOG_TAG, "Time taken to put long string into preference manager: " + timeTaken + " milliseconds");
    }

    @Test
    public void gettingPreferenceDelay() {
        int attempts = 100;
        Calendar startTime = Calendar.getInstance();

        for (int i = 0; i < attempts; i++) {
            SharedPreferences pref = mContext.getSharedPreferences(TEST_PREF, Context.MODE_PRIVATE);
            String x = pref.getString(TEST_STRING_KEY, "");
        }

        long timeTaken = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();
        Log.d(LOG_TAG, "Time taken to get long string from preference manager for " + attempts + " times: " + timeTaken + " milliseconds");
    }

    @Test
    public void arrayToString() {
        String[] array = {"str1", "str2", "str3"};
        Log.v(LOG_TAG, array.toString());
        Log.v(LOG_TAG, Arrays.toString(array));
    }

}
