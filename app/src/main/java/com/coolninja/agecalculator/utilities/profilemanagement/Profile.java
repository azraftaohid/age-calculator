package com.coolninja.agecalculator.utilities.profilemanagement;

import android.util.Log;

import com.coolninja.agecalculator.ui.MainActivity;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.codes.Error;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile implements ProfileManagerInterface.updatable {
    private static final String LOG_TAG = Profile.class.getSimpleName();

    static final String ID = "profile.id";
    static final String NAME = "profile.name";
    static final String BIRTH_YEAR = "profile.dob.year";
    static final String BIRTH_MONTH = "profile.dob.month";
    static final String BIRTH_DAY = "profile.dob.day";

    @SuppressWarnings("UnusedAssignment")
    private int mId = Error.NOT_FOUND; //Precaution step; in case constructor did not set id
    private Birthday mDateOfBirth;
    private Birthday mPrevDateOfBirth;

    private String mName;
    private String mPrevName;
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

    @Override
    public void updateName(String newName) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Setting a new name for profile w/ ID " + mId);

        mPrevName = mName;
        mName = newName;

        if (mOnProfileUpdateListener != null)
            mOnProfileUpdateListener.onProfileNameUpdated(mId, newName, mPrevName);
    }

    @Override
    public void updateBirthday(int year, int month, int day) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Setting a new date of birth for profile w/ ID " + mId);

        mPrevDateOfBirth = new Birthday(mDateOfBirth.get(Birthday.YEAR), mDateOfBirth.get(Birthday.MONTH), mDateOfBirth.get(Birthday.YEAR));

        mDateOfBirth.set(Birthday.YEAR, year);
        mDateOfBirth.set(Birthday.MONTH, month);
        mDateOfBirth.set(Birthday.DAY, day);

        mOnProfileUpdateListener.onProfileDateOfBirthUpdated(mId, year, month, day, mPrevDateOfBirth);
    }

    void setId(int id) {
        mId = id;
    }

    @Override
    public String getPreviousName() {
        return mPrevName;
    }

    @Override
    public Birthday getPreviousBirthday() {
        return mPrevDateOfBirth;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Birthday getBirthday() {
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
