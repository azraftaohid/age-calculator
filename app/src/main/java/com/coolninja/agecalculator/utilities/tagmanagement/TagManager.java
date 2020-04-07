package com.coolninja.agecalculator.utilities.tagmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coolninja.agecalculator.ui.MainActivity;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TagManager {
    private static final String LOG_TAG = TagManager.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = TagManager.class.getSimpleName() + ".Performance";

    private static final String PREF_KEY = "com.coolninja.agecalculator.pref.TAGMANAGER";
    private static final String TAGS_KEY = "com.coolninja.agecalculator.pref.TAGMANAGER.TAGS";

    public static final int PIN = 0;

    private Context mContext;
    private JSONArray mTaggedProfilesJson = new JSONArray();
    private SharedPreferences mPref;

    private TagManager(Context context) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Constructing a new Tag Manager");

        mContext = context;
        mPref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    public static TagManager getTagManager(Context context) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Retrieving Tag Manager");
        TagManager tagManager = new TagManager(context);

        for (Tag t : Tag.values()) {
            tagManager.mTaggedProfilesJson.put(t.initializerToJsonObject());
        }

        SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);

        if (pref.contains(TAGS_KEY)) {
            try {
                JSONArray jsonArray = new JSONArray(pref.getString(TAGS_KEY, ""));
                if (MainActivity.LOG_D) Log.d(LOG_TAG, "Found tag record: " + jsonArray.toString(4));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    int tag = object.getInt(Tag.TAG_NAME_KEY);
                    JSONArray ids = object.getJSONArray(Tag.PROFILE_IDS_KEY);
                    for (int i2 = 0; i2 < ids.length(); i2++) {
                        tagManager.tagProfile(ids.getInt(i2), tag);
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Couldn't parse tagged ids into json array");
                e.printStackTrace();
            }
        } else {
            if (MainActivity.LOG_V) Log.v(LOG_TAG, "No record found of tagged profile");
        }


        return tagManager;
    }

    public void tagProfile(int profileId, int what) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Tagging profile w/ ID " + profileId);

        Tag t = Tag.values()[what];
        if (t.getProfileIds().contains(profileId)) {
            removeTagFromProfile(profileId, what);
        }

        t.addProfile(profileId);

        if (mContext instanceof ProfileManagerInterface.onProfilePinnedListener) {
            if (what == PIN) {
                ((ProfileManagerInterface.onProfilePinnedListener) mContext).onProfilePinned(profileId, true);
            }
        }

        try {
            mTaggedProfilesJson.getJSONObject(what).getJSONArray(Tag.PROFILE_IDS_KEY).put(profileId);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to put profile w/ ID " + profileId + " into the tagged profiles json");
            e.printStackTrace();
        }

        updatePreference();
    }

    public void removeTagFromProfile(int profileId, int whichTag) {
        if (MainActivity.LOG_V) Log.v(LOG_TAG, "Removing tag from profile w/ ID " + profileId);

        try {
            removeProfileFromJsonArray(profileId, mTaggedProfilesJson.getJSONObject(whichTag).getJSONArray(Tag.PROFILE_IDS_KEY));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to remove profile w/ ID " + profileId + " into the tagged profiles json");
            e.printStackTrace();
        }

        Tag.values()[whichTag].removeProfile(profileId);

        if (mContext instanceof ProfileManagerInterface.onProfilePinnedListener) {
            if (whichTag == PIN) {
                ((ProfileManagerInterface.onProfilePinnedListener) mContext).onProfilePinned(profileId, false);
            }
        }
    }

    public void removeAllTagsFromProfile(int profileId) {
        for (Tag t : Tag.values()) {
            removeTagFromProfile(profileId, t.ordinal());
        }
    }

    private void removeProfileFromJsonArray(int profileId, JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (jsonArray.getInt(i) == profileId) {
                    jsonArray.remove(i);
                    if (MainActivity.LOG_V) Log.v(LOG_TAG, "Removed profile w/ ID " + profileId + " from the tagged profiles json array");
                    break;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting json profile when index number is " + i + " and json profiles length is " + jsonArray.length());
                e.printStackTrace();
            }
        }

        updatePreference();
    }

    private void updatePreference() {
        if (MainActivity.LOG_V) {
            try {
                Log.v(LOG_TAG, "Updating tags preference w/ " + mTaggedProfilesJson.toString(4));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error logging statement");
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(TAGS_KEY, mTaggedProfilesJson.toString());
        editor.apply();
    }

    public static ArrayList<Integer> getTaggedIds(int whichTag) {
        if (whichTag == PIN) {
            return Tag.PIN.getProfileIds();
        }

        return new ArrayList<>();
    }

}
