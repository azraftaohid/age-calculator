package thegoodcompany.aetate.utilities.profilemanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.Error;
import thegoodcompany.aetate.utilities.tagmanagement.Tag;
import thegoodcompany.aetate.utilities.tagmanagement.TagManager;

import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_I;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;
import static thegoodcompany.aetate.utilities.profilemanagement.Profile.KEY_AVATAR_NAME;

public class ProfileManager implements ProfileManagerInterface.OnProfileUpdatedListener, TagManager.OnTaggedListener {
    private static final String PREFERENCE_KEY = "thegoodcompany.aetate.pref.PROFILEMANAGER";

    private static final String LOG_TAG = ProfileManager.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = ProfileManager.class.getSimpleName() + ".performance";
    private static final String JSON_TREE_PROFILES_KEY = "thegoodcompany.aetate.pref.PROFILEMANAGER.JSONPROFILES";
    private static ProfileManager instance;

    private static final int NOT_FOUND = Error.NOT_FOUND.getCode();

    private static int nextProfileId = 1001;
    private SharedPreferences mPreference;
    private TagManager mTagManager;
    private JSONArray mJsonProfiles = new JSONArray();
    private ArrayList<Profile> mProfiles = new ArrayList<>();
    private ArrayList<ProfileManagerInterface.OnProfileAddedListener> mOnProfileAddedListeners = new ArrayList<>();
    private ArrayList<ProfileManagerInterface.OnProfileUpdatedListener> mOnProfileUpdatedListeners = new ArrayList<>();
    private ArrayList<ProfileManagerInterface.OnProfileRemovedListener> mOnProfileRemovedListeners = new ArrayList<>();
    private ArrayList<ProfileManagerInterface.OnProfilePinnedListener> mOnProfilePinnedListeners = new ArrayList<>();

    private ProfileManager(@NonNull Context context) {
        if (LOG_V) Log.v(LOG_TAG, "Constructing a new Profile Manager");

        mPreference = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mTagManager = TagManager.getTagManager(context);
        mTagManager.addOnTaggedListener(this);
    }

    public static ProfileManager getInstance(@Nullable Context context) {
        if (instance == null) {
            Calendar startTime = null;
            if (LOG_D) startTime = Calendar.getInstance();
            if (LOG_V) Log.v(LOG_TAG, "Retrieving Profile Manager");

            if (context == null)
                throw new NullPointerException("Profile Manager wasn't initiated/retrieved before. " +
                        "Initiating/retrieving a new Profile Manager requires context.");

            instance = new ProfileManager(context);

            if (instance.mPreference.contains(JSON_TREE_PROFILES_KEY)) {
                try {
                    instance.mJsonProfiles = new JSONArray(instance.mPreference.getString(JSON_TREE_PROFILES_KEY, ""));
                    if (LOG_V)
                        Log.v(LOG_TAG, "Retrieved json profiles array from preference\n" + instance.mJsonProfiles.toString(4));

                    for (int i = 0; i < instance.mJsonProfiles.length(); i++) {
                        JSONObject jsonProfile = instance.mJsonProfiles.getJSONObject(i);

                        int id = jsonProfile.getInt(Profile.KEY_ID);
                        if (LOG_I) Log.i(LOG_TAG, "Found profile w/ ID: " + id);

                        String name = jsonProfile.getString(Profile.KEY_NAME);
                        if (name.equals(context.getString(R.string.default_name)))
                            Log.w(LOG_TAG, "Profile name is missing for profile w/ ID: " + id);
                        else if (LOG_I) Log.i(LOG_TAG, "Profile name found: " + name);

                        int year = jsonProfile.getInt(Profile.KEY_BIRTH_YEAR);
                        if (year == NOT_FOUND)
                            Log.w(LOG_TAG, "Birth year is missing for profile w/ ID: " + id);
                        else if (LOG_I) Log.i(LOG_TAG, "Birth year found: " + year);

                        int month = jsonProfile.getInt(Profile.KEY_BIRTH_MONTH);
                        if (month == NOT_FOUND)
                            Log.w(LOG_TAG, "Birth month is missing for profile w/ ID: " + id);
                        else if (LOG_I) Log.i(LOG_TAG, "Birth month found: " + month);

                        int day = jsonProfile.getInt(Profile.KEY_BIRTH_DAY);
                        if (day == NOT_FOUND)
                            Log.w(LOG_TAG, "Profile birth year is missing for profile w/ ID: " + id);
                        else if (LOG_I) Log.i(LOG_TAG, "Birth day found: " + day);

                        Birthday dob = new Birthday(year, month, day);

                        Avatar avatar = null;
                        if (jsonProfile.has(KEY_AVATAR_NAME)) {
                            avatar = Avatar.retrieveAvatar(context, jsonProfile.getString(KEY_AVATAR_NAME));
                            if (LOG_I)
                                Log.i(LOG_TAG, "Found avatar: " + avatar.getAvatarFileName());
                        }

                        if (nextProfileId <= id) {
                            nextProfileId = id + 1;
                            if (LOG_I) Log.i(LOG_TAG, "Max set profile id is now: " + getMaxedId());
                        }

                        Profile profile = new Profile(name, dob, id);
                        profile.setOnProfileUpdatedListener(instance);
                        profile.setAvatar(avatar);

                        instance.mProfiles.add(profile);
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
        }

        return instance;
    }

    public static boolean isPinned(int profileId) {
        if (LOG_D) Log.d(LOG_TAG, "Checking if profile w/ ID " + profileId + " is pinned");
        boolean result = TagManager.getTaggedIds(Tag.PIN).contains(profileId);
        if (LOG_I) Log.i(LOG_TAG, (result ? "Affirmative" : "Negative") + " on is pinned check");

        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public static int getMaxedId() {
        return nextProfileId - 1;
    }

    static int assignProfileId() {
        Log.i(LOG_TAG, "Generating new profile ID");
        return nextProfileId++;
    }

    public void addProfile(Profile profile) {
        Calendar startTime = null;
        if (LOG_D) startTime = Calendar.getInstance();

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

        profile.setOnProfileUpdatedListener(this);
        triggerProfileAdded(profile);

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to add a new profile");
        }
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        if (LOG_V)
            Log.v(LOG_TAG, "Setting new date of birth in json for profile w/ ID " + profileId);

        JSONObject object = getJsonProfile(profileId);
        assert object != null : "No json object for profile w/ ID " + profileId + " was found";

        try {
            object.put(Profile.KEY_BIRTH_YEAR, newBirthYear);
            object.put(Profile.KEY_BIRTH_MONTH, newBirthMonth);
            object.put(Profile.KEY_BIRTH_DAY, newBirthDay);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error updating json object for profile w/ ID " + profileId);
            e.printStackTrace();
        }

        updatePreference();
        triggerProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        if (LOG_V)
            Log.v(LOG_TAG, "Name changed from " + previousName + " to " + newName + " for profile w/ ID " + profileId);

        JSONObject object = getJsonProfile(profileId);
        assert object != null : "No json object for profile w/ ID " + profileId + " was found";

        try {
            object.put(Profile.KEY_NAME, newName);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error updating json object for profile w/ ID " + profileId);
            e.printStackTrace();
        }

        updatePreference();
        TriggerProfileNameChanged(profileId, newName, previousName);
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

    @Override
    public void onProfileAvatarChanged(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        if (LOG_V) Log.v(LOG_TAG, "Avatar changed for profile w/ ID: " + profileId);

        newAvatar.storePermanently();

        JSONObject object = getJsonProfile(profileId);

        if (object == null) {
            Log.w(LOG_TAG, "No json object for profile w/ ID " + profileId + " was found in json profiles array");

            object = getProfileById(profileId).toJSONObject();
            mJsonProfiles.put(object); //It replaces the old value
        }

        try {
            object.put(KEY_AVATAR_NAME, newAvatar.getAvatarFileName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        updatePreference();
        triggerProfileAvatarChanged(profileId, newAvatar, previousAvatar);
    }

    public void pinProfile(int profileId, boolean isPinned) {
        if (LOG_V)
            Log.v(LOG_TAG, (isPinned ? "Pinning " : "Unpinning ") + "Profile w/ ID " + profileId);

        bringProfileOnTop(profileId);

        if (isPinned) mTagManager.tagProfile(profileId, Tag.PIN);
        else mTagManager.removeTagFromProfile(profileId, Tag.PIN);
    }

    public Profile getProfileById(int id) {
        if (id == NOT_FOUND) {
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
                if (object.getInt(Profile.KEY_ID) == profileId) specifiedProfileIndex = i;
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

    public ArrayList<Profile> getPinnedProfiles() {
        if (LOG_I) Log.i(LOG_TAG, "Getting pinned profiles");

        ArrayList<Profile> profiles = new ArrayList<>();
        ArrayList<Integer> ids = TagManager.getTaggedIds(Tag.PIN);
        if (LOG_V) Log.v(LOG_TAG, "Number of pinned ids: " + ids.size());

        for (int id : ids) {
            profiles.add(getProfileById(id));
        }

        if (LOG_V) Log.v(LOG_TAG, "Associated pinned profile ids: " + ids);

        return profiles;
    }

    public ArrayList<Profile> getUnpinnedProfiles() {
        if (LOG_I) Log.i(LOG_TAG, "Getting other profiles");

        ArrayList<Profile> profiles = new ArrayList<>();
        ArrayList<Integer> ids = TagManager.getTaggedIds(Tag.PIN);
        ArrayList<Integer> allIds = getProfileIds();

        allIds.removeAll(ids);
        for (int id : allIds) {
            profiles.add(getProfileById(id));
        }

        if (LOG_I) Log.i(LOG_TAG, "Associated other profile ids: " + allIds);

        return profiles;
    }

    public void removeProfile(final int profileId) {
        if (LOG_V) Log.v(LOG_TAG, "Removing profile w/ ID: " + profileId);

        Profile profile = getProfileById(profileId);
        final Avatar avatar = profile.getAvatar();

        if (avatar != null) {
            new Thread(() -> {
                if (LOG_I)
                    Log.i(LOG_TAG, "A new thread to delete avatar image file has been started");
                if (!avatar.deleteAvatarFile()) {
                    Log.e(LOG_TAG, "There was an error deleting avatar file of profile w/ ID: " + profileId);
                }
            }).start();
        }

        List<Tag> removedTags = mTagManager.prepareProfileForRemove(profileId);
        removeFromJsonArray(profileId);
        Log.i(LOG_TAG, "Removed profile w/ ID: " + profileId + " (" + mProfiles.remove(profile) + ")");

        updatePreference();

        triggerProfileRemoved(profile, removedTags);
    }

    private JSONObject getJsonProfile(int profileId) {
        JSONObject object;
        for (int i = 0; i < mJsonProfiles.length(); i++) {
            try {
                object = mJsonProfiles.getJSONObject(i);
                if (object.getInt(Profile.KEY_ID) == profileId) return object;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting json profile");
                e.printStackTrace();
            }
        }

        Log.w(LOG_TAG, "No json profile w/ ID " + profileId + " was found");
        return null;
    }

    private void removeFromJsonArray(int profileId) {
        for (int i = 0; i < mJsonProfiles.length(); i++) {
            try {
                if (mJsonProfiles.getJSONObject(i).getInt(Profile.KEY_ID) == profileId) {
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

    public void addOnProfileAddedListener(ProfileManagerInterface.OnProfileAddedListener listener) {
        mOnProfileAddedListeners.add(listener);
    }

    public void removeOnProfileAddedListener(ProfileManagerInterface.OnProfileAddedListener listener) {
        mOnProfileAddedListeners.remove(listener);
    }

    public void addOnProfileUpdatedListener(ProfileManagerInterface.OnProfileUpdatedListener listener) {
        mOnProfileUpdatedListeners.add(listener);
    }

    public void removeOnProfileUpdatedListener(ProfileManagerInterface.OnProfileUpdatedListener listener) {
        mOnProfileUpdatedListeners.remove(listener);
    }

    public void addOnProfileRemovedListener(ProfileManagerInterface.OnProfileRemovedListener listener) {
        mOnProfileRemovedListeners.add(listener);
    }

    public void removeOnProfileRemovedListener(ProfileManagerInterface.OnProfileRemovedListener listener) {
        mOnProfileRemovedListeners.remove(listener);
    }

    public void addOnProfilePinnedListener(ProfileManagerInterface.OnProfilePinnedListener listener) {
        mOnProfilePinnedListeners.add(listener);
    }

    public void removeOnProfilePinnedListener(ProfileManagerInterface.OnProfilePinnedListener listener) {
        mOnProfilePinnedListeners.remove(listener);
    }

    private void triggerProfileAdded(Profile profile) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfileAddedListener listener : mOnProfileAddedListeners) {
            if (listener != null) listener.onProfileAdded(profile);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileAddedListeners.removeAll(Collections.singleton(null));
    }

    private void triggerProfileRemoved(Profile profile, List<Tag> removedTags) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfileRemovedListener listener : mOnProfileRemovedListeners) {
            if (listener != null) listener.onProfileRemoved(profile, removedTags);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileAddedListeners.removeAll(Collections.singleton(null));
    }

    private void triggerProfilePinned(int profileId, boolean isPinned) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfilePinnedListener listener : mOnProfilePinnedListeners) {
            if (listener != null) listener.onProfilePinned(profileId, isPinned);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileAddedListeners.removeAll(Collections.singleton(null));
    }

    private void TriggerProfileNameChanged(int id, String newName, String prevName) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfileUpdatedListener listener : mOnProfileUpdatedListeners) {
            if (listener != null) listener.onProfileNameChanged(id, newName, prevName);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileUpdatedListeners.removeAll(Collections.singleton(null));
    }

    private void triggerProfileDateOfBirthUpdated(int id, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfileUpdatedListener listener : mOnProfileUpdatedListeners) {
            if (listener != null)
                listener.onProfileDateOfBirthUpdated(id, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileUpdatedListeners.removeAll(Collections.singleton(null));
    }

    private void triggerProfileAvatarChanged(int id, Avatar newAvatar, Avatar prevAvatar) {
        boolean needsCleanup = false;

        for (ProfileManagerInterface.OnProfileUpdatedListener listener : mOnProfileUpdatedListeners) {
            if (listener != null) listener.onProfileAvatarChanged(id, newAvatar, prevAvatar);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnProfileUpdatedListeners.removeAll(Collections.singleton(null));
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

        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(JSON_TREE_PROFILES_KEY, mJsonProfiles.toString());
        editor.apply();
    }

    @Override
    public void onTagged(Tag which, int id) {
        if (which == Tag.PIN) triggerProfilePinned(id, true);
    }

    @Override
    public void onTagRemoved(Tag which, int id) {
        if (which == Tag.PIN) triggerProfilePinned(id, false);
    }
}
