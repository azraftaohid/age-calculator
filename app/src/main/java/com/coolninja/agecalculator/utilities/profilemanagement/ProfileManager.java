package com.coolninja.agecalculator.utilities.profilemanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.ui.MainActivity;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.tagmanagement.TagManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileManager implements ProfileManagerInterface.onProfileUpdatedListener {
    private static final String LOG_TAG = ProfileManager.class.getSimpleName();
    private static final String PROFILE_MANAGER_PREF = "com.coolninja.agecalculator.agecalculator.pref.PROFILEMANAGER";
    private static final String JSON_PROFILES_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.JSONPROFILES";

    private final int DEFAULT_ERROR_CODE;

    private static int nextProfileId = 1001;

    private Context mContext;
    private SharedPreferences mPref;
    private TagManager mTagManager;
    private ArrayList<Profile> mProfiles = new ArrayList<>();
    private JSONArray mJsonProfiles = new JSONArray();

    private ProfileManager(Context context) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Constructing a new Profile Manager");

        mContext = context;
        mPref = mContext.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        mTagManager = TagManager.getTagManager(mContext);
        DEFAULT_ERROR_CODE = Integer.parseInt(mContext.getString(R.string.default_error_value));
    }

    public static ProfileManager getProfileManager(Context context) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Retrieving Profile Manager");
        ProfileManager manager = new ProfileManager(context);

        SharedPreferences pref = context.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);

        JSONArray jsonProfiles;
        if (pref.contains(JSON_PROFILES_KEY)) {
            try {
                jsonProfiles = new JSONArray(pref.getString(JSON_PROFILES_KEY, ""));
                if (MainActivity.LOG_V) Log.v(LOG_TAG, "Retrieved json profiles array from preference\n" + jsonProfiles.toString(4));

                for (int i = 0; i < jsonProfiles.length(); i++) {
                    JSONObject jsonProfile = jsonProfiles.getJSONObject(i);

                    int id = jsonProfile.getInt(Profile.ID);
                    if (MainActivity.LOG_I) Log.i(LOG_TAG, "Found profile w/ ID: " + id);

                    String name = jsonProfile.getString(Profile.NAME);
                    if (name.equals(context.getString(R.string.default_name))) Log.w(LOG_TAG, "Profile name is missing for profile w/ ID: " + id);
                    else if (MainActivity.LOG_I) Log.i(LOG_TAG, "Profile name found: " + name);

                    int year = jsonProfile.getInt(Profile.BIRTH_YEAR);
                    if (year == Integer.parseInt(context.getString(R.string.default_birth_year))) Log.w(LOG_TAG, "Birth year is missing for profile w/ ID: " + id);
                    else if (MainActivity.LOG_I) Log.i(LOG_TAG, "Birth year found: " + year);

                    int month = jsonProfile.getInt(Profile.BIRTH_MONTH);
                    if (month == Integer.parseInt(context.getString(R.string.default_birth_month))) Log.w(LOG_TAG, "Birth month is missing for profile w/ ID: " + id);
                    else if (MainActivity.LOG_I) Log.i(LOG_TAG, "Birth month found: " + month);

                    int day = jsonProfile.getInt(Profile.BIRTH_DAY);
                    if (day == Integer.parseInt(context.getString(R.string.default_birth_day))) Log.w(LOG_TAG, "Profile birth year is missing for profile w/ ID: " + id);
                    else if (MainActivity.LOG_I) Log.i(LOG_TAG, "Birth day found: " + day);

                    Birthday dob = new Birthday(year, month, day);

                    if (nextProfileId <= id) {
                        nextProfileId = id + 1;
                        if (MainActivity.LOG_I) Log.i(LOG_TAG, "Max set profile id is now: " + getMaxedId());
                    }

                    Profile profile = new Profile(name, dob, id, manager);
                    manager.addProfile(profile);

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing json array from preference");
                e.printStackTrace();
            }
        } else {
            if (MainActivity.LOG_V) Log.v(LOG_TAG, "Preference doesn't contain json profiles");
        }
        
        return manager;
    }

    public void addProfile(Profile profile) {
        ArrayList<Integer> ids = getProfileIds();

        if (profile.getId() == Profile.DEFAULT_ERROR_CODE) { //Precaution step; plan of removal exists
            Log.wtf(LOG_TAG, "Profile ID was not set for the profile w/ name: " + profile.getName());
            do {
                profile.setId(nextProfileId);
            } while (ids.contains(nextProfileId++));
        }

        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " to the profile manager");
        mProfiles.add(profile);
        mJsonProfiles.put(profile.toJSONObject());
        if (MainActivity.LOG_D) Log.d(LOG_TAG, "Last profile index: " + (mProfiles.size() - 1)
                + "\nLast profile index in json array: " + (mJsonProfiles.length() - 1));

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfileAddedListener) {
            ((ProfileManagerInterface.onProfileAddedListener) mContext).onProfileAdded(profile);
        } else {
            if (MainActivity.LOG_W) Log.w(LOG_TAG, mContext.getClass().getSimpleName() + " does not not implement " + ProfileManagerInterface.onProfileAddedListener.class.getSimpleName() +" interface");
        }
    }

    private void updatePreference() {
        if (MainActivity.LOG_V) {
            try {
                Log.v(LOG_TAG, "Updating preference w/\n" + mJsonProfiles.toString(4));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Couldn't log statement of updating preference");
                if (MainActivity.LOG_D) Log.d(LOG_TAG, "JSONArray: " + mJsonProfiles.toString());
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(JSON_PROFILES_KEY, mJsonProfiles.toString());
        editor.apply();
    }

    @Override
    public void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Setting new date of birth in json for profile w/ ID " + profileId);

        JSONObject object = getJsonProfile(profileId);
        assert object != null : "No json object for profile w/ ID " + profileId + " was found";

        try {
            object.put(Profile.BIRTH_YEAR, newBirthYear);
            object.put(Profile.BIRTH_MONTH, newBirthMonth);
            object.put(Profile.BIRTH_DAY, newBirthDay);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error updating json object for profile w/ ID " + profileId);
            e.printStackTrace();
        }

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfileUpdatedListener)
            ((ProfileManagerInterface.onProfileUpdatedListener) mContext).onProfileDateOfBirthChanged(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
    }

    @Override
    public void onProfileNameChanged(int profileId, String newName, String previousName) {
        Log.i(LOG_TAG, "Renaming profile w/ ID " + profileId + " from " + previousName + " to " + newName);

        JSONObject object = getJsonProfile(profileId);
        assert object != null : "No json object for profile w/ ID " + profileId + " was found";

        try {
            object.put(Profile.NAME, newName);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error updating json object for profile w/ ID " + profileId);
            e.printStackTrace();
        }

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfileUpdatedListener)
            ((ProfileManagerInterface.onProfileUpdatedListener) mContext).onProfileNameChanged(profileId, newName, previousName);
    }


    public ArrayList<Integer> getProfileIds() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Profile profile : mProfiles) {
            ids.add(profile.getId());
        }

        Log.i(LOG_TAG, "Currently associated IDs: " + ids);

        return ids;
    }

    public ArrayList<Profile> getProfiles() {
        return mProfiles;
    }

    public Profile getProfileById(int id) {
        if (id == DEFAULT_ERROR_CODE) {
            Log.w(LOG_TAG, "Profile id is a error code; it sure does not exist");
            return null;
        }

        for (Profile profile : mProfiles) {
            if (profile.getId() == id) {
                return profile;
            }
        }
        Log.w(LOG_TAG, "No profile found with the profile id: " + id);

        return null;
    }

    public void removeProfile(int profileId) {
        removeJsonProfile(profileId);
        Log.i(LOG_TAG, "Removed profile w/ ID: " + profileId + "(" + mProfiles.remove(getProfileById(profileId)) + ")");

        if (mContext instanceof ProfileManagerInterface.onProfileRemovedListener) {
            ((ProfileManagerInterface.onProfileRemovedListener) mContext).onProfileRemoved(profileId);
        }
    }

    public void pinProfile(int profileId, boolean isPinned) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, (isPinned? "Pinning " : "Unpinning ") + "Profile w/ ID " + profileId);

        if (isPinned) {
            mTagManager.tagProfile(profileId, TagManager.PIN);
        } else {
            mTagManager.removeTagFromProfile(profileId, TagManager.PIN);
        }

        bringProfileOnTop(profileId);

        if (mContext instanceof ProfileManagerInterface.onProfilePinnedListener) {
            ((ProfileManagerInterface.onProfilePinnedListener) mContext).onProfilePinned(profileId, isPinned);
        }
    }

    private void bringProfileOnTop(int profileId) {
        Log.v(LOG_TAG, "Bringing profile w/ ID " + profileId + " on top");
        int numberOfProfiles = mJsonProfiles.length();
        JSONObject[] arrayProfiles = new JSONObject[numberOfProfiles];

        int specifiedProfileIndex = -1;
        for (int i = 0; i < numberOfProfiles; i++) {
            try {
                JSONObject object = mJsonProfiles.getJSONObject(i);
                arrayProfiles[i] = object;
                if (object.getInt(Profile.ID) == profileId) specifiedProfileIndex = i;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Couldn't parse json object");
                e.printStackTrace();
            }
        }

        for (int i = numberOfProfiles - 1; i >= 0; i--) {
            mJsonProfiles.remove(i);
        }

        if (specifiedProfileIndex != -1) {
            mJsonProfiles.put(arrayProfiles[specifiedProfileIndex]);
        }

        for (int i = 0; i < numberOfProfiles; i++) {
            if (i != specifiedProfileIndex) {
                mJsonProfiles.put(arrayProfiles[i]);
            }
        }

        updatePreference();
    }

    public static boolean isPinned(int profileId) {
        if (MainActivity.LOG_D) Log.d(LOG_TAG, "Checking if profile w/ ID " + profileId + " is pinned");
        boolean result = TagManager.getTaggedIds(TagManager.PIN).contains(profileId);
        if (MainActivity.LOG_D) Log.d(LOG_TAG, (result? "Affirmative" : "Negative") + " on is pinned check");

        return result;
    }

    private JSONObject getJsonProfile(int profileId) {
        JSONObject object;
        for (int i = 0; i < mJsonProfiles.length(); i++) {
            try {
                object = mJsonProfiles.getJSONObject(i);
                if (object.getInt(Profile.ID) == profileId) return object;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting json profile");
                e.printStackTrace();
            }
        }

        Log.w(LOG_TAG, "No json profile w/ ID " + profileId + " was found");
        return null;
    }

    private void removeJsonProfile(int profileId) {
        for (int i = 0; i < mJsonProfiles.length(); i++) {
            try {
                if (mJsonProfiles.getJSONObject(i).getInt(Profile.ID) == profileId) {
                    mJsonProfiles.remove(i);
                    if (MainActivity.LOG_V) Log.v(LOG_TAG, "Removed profile w/ ID " + profileId + " from the json profiles");

                    break;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting json profile when index number is " + i + " and json profiles length is " + mJsonProfiles.length());
                e.printStackTrace();
            }
        }
    }

    static int generateProfileId() {
        Log.i(LOG_TAG, "Generating new profile ID");
        return nextProfileId++;
    }

    public static int getMaxedId() {
        return nextProfileId - 1;
    }
}
