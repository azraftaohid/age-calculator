package com.coolninja.agecalculator.utilities.profilemanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Avatar;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.tagmanagement.TagManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_I;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_W;
import static com.coolninja.agecalculator.utilities.profilemanagement.Profile.AVATAR_NAME;

public class ProfileManager implements ProfileManagerInterface.onProfileUpdatedListener {
    private static final String LOG_TAG = ProfileManager.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = ProfileManager.class.getSimpleName() + ".Performance";

    private static final String PROFILE_MANAGER_PREF = "com.coolninja.agecalculator.agecalculator.pref.PROFILEMANAGER";
    private static final String JSON_PROFILES_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.JSONPROFILES";

    private static int nextProfileId = 1001;

    private Context mContext;
    private SharedPreferences mPref;
    private TagManager mTagManager;
    private ArrayList<Profile> mProfiles = new ArrayList<>();
    private JSONArray mJsonProfiles = new JSONArray();

    private ProfileManager(Context context) {
        if (LOG_V) Log.v(LOG_TAG, "Constructing a new Profile Manager");

        mContext = context;
        mPref = mContext.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        mTagManager = TagManager.getTagManager(mContext);
    }

    public static ProfileManager getProfileManager(Context context) {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Retrieving Profile Manager");
        ProfileManager manager = new ProfileManager(context);

        SharedPreferences pref = context.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);

        if (pref.contains(JSON_PROFILES_KEY)) {
            try {
                manager.mJsonProfiles = new JSONArray(pref.getString(JSON_PROFILES_KEY, ""));
                if (LOG_V)
                    Log.v(LOG_TAG, "Retrieved json profiles array from preference\n" + manager.mJsonProfiles.toString(4));

                for (int i = 0; i < manager.mJsonProfiles.length(); i++) {
                    Calendar RetrieveOneStartTime;
                    if (LOG_D) RetrieveOneStartTime = Calendar.getInstance();

                    JSONObject jsonProfile = manager.mJsonProfiles.getJSONObject(i);

                    int id = jsonProfile.getInt(Profile.ID);
                    if (LOG_I) Log.i(LOG_TAG, "Found profile w/ ID: " + id);

                    String name = jsonProfile.getString(Profile.NAME);
                    if (name.equals(context.getString(R.string.default_name)))
                        Log.w(LOG_TAG, "Profile name is missing for profile w/ ID: " + id);
                    else if (LOG_I) Log.i(LOG_TAG, "Profile name found: " + name);

                    int year = jsonProfile.getInt(Profile.BIRTH_YEAR);
                    if (year == Integer.parseInt(context.getString(R.string.default_birth_year)))
                        Log.w(LOG_TAG, "Birth year is missing for profile w/ ID: " + id);
                    else if (LOG_I) Log.i(LOG_TAG, "Birth year found: " + year);

                    int month = jsonProfile.getInt(Profile.BIRTH_MONTH);
                    if (month == Integer.parseInt(context.getString(R.string.default_birth_month)))
                        Log.w(LOG_TAG, "Birth month is missing for profile w/ ID: " + id);
                    else if (LOG_I) Log.i(LOG_TAG, "Birth month found: " + month);

                    int day = jsonProfile.getInt(Profile.BIRTH_DAY);
                    if (day == Integer.parseInt(context.getString(R.string.default_birth_day)))
                        Log.w(LOG_TAG, "Profile birth year is missing for profile w/ ID: " + id);
                    else if (LOG_I) Log.i(LOG_TAG, "Birth day found: " + day);

                    Birthday dob = new Birthday(year, month, day);

                    Avatar avatar = null;
                    if (jsonProfile.has(AVATAR_NAME)) {
                        if (LOG_I) Log.i(LOG_TAG, "Found avatar for profile w/ ID: " + id);
                        avatar = Avatar.retrieveAvatar(context, jsonProfile.getString(AVATAR_NAME));
                    }

                    if (nextProfileId <= id) {
                        nextProfileId = id + 1;
                        if (LOG_I) Log.i(LOG_TAG, "Max set profile id is now: " + getMaxedId());
                    }

                    Profile profile = new Profile(name, dob, id, manager);
                    if (avatar != null) profile.setAvatar(avatar);

                    manager.mProfiles.add(profile);

                    if (LOG_D) {
                        Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - RetrieveOneStartTime.getTimeInMillis())
                                + " milliseconds to retrieve one profile from json array");
                    }

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing json array from preference");
                e.printStackTrace();
            }
        } else {
            if (LOG_V) Log.v(LOG_TAG, "Preference doesn't contain json profiles");
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to get " + ProfileManager.class.getSimpleName());
        }

        return manager;
    }

    public static boolean isPinned(int profileId) {
        if (LOG_D) Log.d(LOG_TAG, "Checking if profile w/ ID " + profileId + " is pinned");
        boolean result = TagManager.getTaggedIds(TagManager.TAG_PIN).contains(profileId);
        if (LOG_D) Log.d(LOG_TAG, (result ? "Affirmative" : "Negative") + " on is pinned check");

        return result;
    }

    static int generateProfileId() {
        Log.i(LOG_TAG, "Generating new profile ID");
        return nextProfileId++;
    }

    @SuppressWarnings("WeakerAccess")
    public static int getMaxedId() {
        return nextProfileId - 1;
    }

    public static boolean containsProfilePreference(Context context) {
        return context.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE).contains(JSON_PROFILES_KEY);
    }

    public void addProfile(Profile profile) {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        if (profile.getId() == Error.NOT_FOUND) { //Precaution step; plan of removal exists
            Log.wtf(LOG_TAG, "Profile ID was not set for the profile w/ name: " + profile.getName());
            ArrayList<Integer> ids = getProfileIds();
            do {
                profile.setId(nextProfileId);
            } while (ids.contains(nextProfileId++));
        }

        if (getProfileIds().contains(profile.getId())) {
            Log.w(LOG_TAG, "Couldn't add profile because another profile with the same ID already exists");
            return;
        }

        Avatar avatar = profile.getAvatar();
        if (avatar != null) avatar.storePermanently();

        if (LOG_V)
            Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " to the profile manager");
        mProfiles.add(profile);
        mJsonProfiles.put(profile.toJSONObject());
        if (LOG_D) Log.d(LOG_TAG, "Last profile index: " + (mProfiles.size() - 1)
                + "\nLast profile index in json array: " + (mJsonProfiles.length() - 1));

        updatePreference();

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to add a new profile");
        }

        if (mContext instanceof ProfileManagerInterface.onProfileAddedListener) {
            ((ProfileManagerInterface.onProfileAddedListener) mContext).onProfileAdded(profile);
        } else {
            if (LOG_W)
                Log.w(LOG_TAG, mContext.getClass().getSimpleName() + " does not not implement " + ProfileManagerInterface.onProfileAddedListener.class.getSimpleName() + " interface");
        }
    }

    private void updatePreference() {
        if (LOG_V) {
            try {
                Log.v(LOG_TAG, "Updating preference w/\n" + mJsonProfiles.toString(4));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Couldn't log statement of updating preference");
                if (LOG_D) Log.d(LOG_TAG, "JSONArray: " + mJsonProfiles.toString());
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(JSON_PROFILES_KEY, mJsonProfiles.toString());
        editor.apply();
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        if (LOG_V)
            Log.v(LOG_TAG, "Setting new date of birth in json for profile w/ ID " + profileId);

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
            ((ProfileManagerInterface.onProfileUpdatedListener) mContext).onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
    }

    @Override
    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
        if (LOG_V) Log.v(LOG_TAG, "Name changed from " + previousName + " to " + newName + " for profile w/ ID " + profileId);

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
            ((ProfileManagerInterface.onProfileUpdatedListener) mContext).onProfileNameUpdated(profileId, newName, previousName);
    }

    @Override
    public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        if (LOG_V) Log.v(LOG_TAG, "Avatar changed for profile w/ ID: " + profileId);

        JSONObject object = getJsonProfile(profileId);

        if (object == null) {
            Log.w(LOG_TAG, "No json object for profile w/ ID " + profileId + " was found in json profiles array");

            object = getProfileById(profileId).toJSONObject();
            mJsonProfiles.put(object); //It replaces the old value
        }

        try {
            object.put(AVATAR_NAME, newAvatar.getAvatarFileName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfileUpdatedListener) {
            ((ProfileManagerInterface.onProfileUpdatedListener) mContext).onProfileAvatarUpdated(profileId, newAvatar, previousAvatar);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public ArrayList<Integer> getProfileIds() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Profile profile : mProfiles) {
            ids.add(profile.getId());
        }

        Log.i(LOG_TAG, "Currently associated IDs: " + ids);

        return ids;
    }

    @SuppressWarnings("unused")
    public ArrayList<Profile> getProfiles() {
        return mProfiles;
    }

    public ArrayList<Profile> getPinnedProfiles() {
        if (LOG_I) Log.i(LOG_TAG, "Getting pinned profiles");

        ArrayList<Profile> profiles = new ArrayList<>();
        ArrayList<Integer> ids = TagManager.getTaggedIds(TagManager.TAG_PIN);
        if (LOG_V) Log.v(LOG_TAG, "Number of pinned ids: " + ids.size());

        for (int id : ids) {
            profiles.add(getProfileById(id));
        }

        if (LOG_V) Log.v(LOG_TAG, "Associated pinned profile ids: " + ids);

        return profiles;
    }

    public ArrayList<Profile> getOtherProfiles() {
        if (LOG_I) Log.i(LOG_TAG, "Getting other profiles");

        ArrayList<Profile> profiles = new ArrayList<>();
        ArrayList<Integer> ids = TagManager.getTaggedIds(TagManager.TAG_PIN);
        ArrayList<Integer> allIds = getProfileIds();

        allIds.removeAll(ids);
        for (int id : allIds) {
            profiles.add(getProfileById(id));
        }

        if (LOG_I) Log.i(LOG_TAG, "Associated other profile ids: " + allIds);

        return profiles;
    }

    public Profile getProfileById(int id) {
        if (id == Error.NOT_FOUND) {
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

    public void removeProfile(final int profileId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Avatar avatar = getProfileById(profileId).getAvatar();
                if (avatar != null) {
                    if (!avatar.deleteAvatarFile()) {
                        Log.e(LOG_TAG, "There was an error deleting avatar file of profile w/ ID: " + profileId);
                    }
                }
            }
        }).start();

        mTagManager.removeAllTagsFromProfile(profileId);
        removeJsonProfile(profileId);
        Log.i(LOG_TAG, "Removed profile w/ ID: " + profileId + "(" + mProfiles.remove(getProfileById(profileId)) + ")");

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfileRemovedListener) {
            ((ProfileManagerInterface.onProfileRemovedListener) mContext).onProfileRemoved(profileId);
        }
    }

    public void pinProfile(int profileId, boolean isPinned) {
        if (LOG_V)
            Log.v(LOG_TAG, (isPinned ? "Pinning " : "Unpinning ") + "Profile w/ ID " + profileId);

        if (isPinned) {
            mTagManager.tagProfile(profileId, TagManager.TAG_PIN);
        } else {
            mTagManager.removeTagFromProfile(profileId, TagManager.TAG_PIN);
        }

        bringProfileOnTop(profileId);
    }

    private void bringProfileOnTop(int profileId) {
        Log.v(LOG_TAG, "Bringing profile w/ ID " + profileId + " on top");

        Profile profile = getProfileById(profileId);
        mProfiles.remove(profile);
        mProfiles.add(0, profile);

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
                    if (LOG_V)
                        Log.v(LOG_TAG, "Removed profile w/ ID " + profileId + " from the json profiles");

                    break;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting json profile when index number is " + i + " and json profiles length is " + mJsonProfiles.length());
                e.printStackTrace();
            }
        }
    }
}
