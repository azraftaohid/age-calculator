package thegoodkid.aetate;

import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import thegoodkid.aetate.utilities.list.profile.ProfileListAdapter;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AdapterTest {
    Context mContext;
    ProfileListAdapter mAdapter;

    @Before
    public void setUp() {
        mContext = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();


    }

    @Test
    public void itemType() {

    }
}
