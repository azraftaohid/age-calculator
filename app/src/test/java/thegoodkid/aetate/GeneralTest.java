package thegoodkid.aetate;

import org.junit.Test;

import java.util.Calendar;

import thegoodkid.aetate.utilities.Birthday;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static thegoodkid.aetate.utilities.codes.Request.REQUEST_CIRCULAR_CROP;
import static thegoodkid.aetate.utilities.codes.Request.REQUEST_DATE_OF_BIRTH;
import static thegoodkid.aetate.utilities.codes.Request.REQUEST_MODIFY_PROFILE_INFO;

public class GeneralTest {
    private static final String LOG_TAG = GeneralTest.class.getSimpleName();

    @Test
    public void random() {
        assertThat(LOG_TAG, is(equalTo("com.coolninja.aetate.GeneralTest")));
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

    @Test
    public void assigningNullVsNothing() {
        long attempts = 8000000;

        Calendar startTimeForNothing = Calendar.getInstance();

        for (long i = 0; i < attempts; i++) {
            Calendar x;
        }

        long timeTakenForNothing = Calendar.getInstance().getTimeInMillis() - startTimeForNothing.getTimeInMillis();

        Calendar startTimeForNull = Calendar.getInstance();

        for (long i = 0; i < attempts; i++) {
            Calendar x = null;
        }

        long timeTakenForNull = Calendar.getInstance().getTimeInMillis() - startTimeForNull.getTimeInMillis();

        assertThat(timeTakenForNull, is(equalTo(timeTakenForNothing)));
    }

    @Test
    public void requestCodeCheck() {
        assertThat(REQUEST_CIRCULAR_CROP, is(equalTo(1103)));
        assertThat(REQUEST_MODIFY_PROFILE_INFO, is(equalTo(1104)));
        assertThat(REQUEST_MODIFY_PROFILE_INFO, is(equalTo(1104)));
        assertThat(REQUEST_DATE_OF_BIRTH, is(equalTo(1100)));
        assertThat(REQUEST_CIRCULAR_CROP, is(equalTo(1103)));
    }

}
