package com.coolninja.agecalculator.utilities.profilemanagement;

import android.util.Log;

import com.coolninja.agecalculator.ui.MainActivity;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.Age;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile {
    private static final String LOG_TAG = Profile.class.getSimpleName();

    static final String ID = "profile.id";
    static final String NAME = "profile.name";
    static final String BIRTH_YEAR = "profile.dob.year";
    static final String BIRTH_MONTH = "profile.dob.month";
    static final String BIRTH_DAY = "profile.dob.day";
    static final int DEFAULT_ERROR_CODE = -1;

    private int mId = DEFAULT_ERROR_CODE; //Precaution step; in case constructor did not set id
    private Birthday mDateOfBirth;

    private String mName;
    private ProfileManagerInterface.onProfileUpdatedListener mOnProfileUpdateListener;

    public Profile(String name, Birthday birthday, ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mOnProfileUpdateListener = onProfileUpdatedListener;
        mId = ProfileManager.generateProfileId();
        mName = name;
        mDateOfBirth = birthday;
    }

    Profile(String name, Birthday dateOfBirth, int id, ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mOnProfileUpdateListener = onProfileUpdatedListener;
        mId = id;
        mName = name;
        mDateOfBirth = dateOfBirth;
    }

    public void setName(String newName) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Setting a new name for profile w/ ID " + mId);

        String previousName = mName;
        mName = newName;

        if (mOnProfileUpdateListener != null)
            mOnProfileUpdateListener.onProfileNameChanged(mId, newName, previousName);
    }

    public void setDateOfBirth(int year, int month, int day) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Setting a new date of birth for profile w/ ID " + mId);

        Birthday prevDateOfBirth = mDateOfBirth;
        mDateOfBirth.set(Birthday.YEAR, year);
        mDateOfBirth.set(Birthday.MONTH, month);
        mDateOfBirth.set(Birthday.DAY, day);

        mOnProfileUpdateListener.onProfileDateOfBirthChanged(mId, year, month, day, prevDateOfBirth);
    }

    void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public Birthday getDateOfBirth() {
        return mDateOfBirth;
    }

    public int getId() {
        return mId;
    }

    public Age getAge() {
        return new Age(mDateOfBirth);
    }

    JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put(ID, mId);
        } catch (JSONException e) {
            Log.wtf(LOG_TAG, "Couldn't put ID in the json object for profile w/ ID: " + mId);
            e.printStackTrace();
        } try {
            object.put(NAME, mName);
        } catch (JSONException e) {
            Log.wtf(LOG_TAG, "Couldn't put Name in the json object for profile w/ ID: " + mId);
            e.printStackTrace();
        } try {
            object.put(BIRTH_YEAR, mDateOfBirth.get(Birthday.YEAR));
            object.put(BIRTH_MONTH, mDateOfBirth.get(Birthday.MONTH));
            object.put(BIRTH_DAY, mDateOfBirth.get(Birthday.DAY));
        } catch (JSONException e) {
            Log.wtf(LOG_TAG, "Couldn't put date of birth in the json object for profile w/ ID: " + mId);
            e.printStackTrace();
        }

        return object;
    }

}
