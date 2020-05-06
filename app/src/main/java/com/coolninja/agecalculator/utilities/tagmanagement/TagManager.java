package com.coolninja.agecalculator.utilities.tagmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_W;

public class TagManager {
    public static final int TAG_PIN = 0;
    public static final int NO_TAG = -1;

    private static final String LOG_TAG = TagManager.class.getSimpleName();
    @SuppressWarnings("unused")
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";
    private static final String PREF_KEY = "com.coolninja.agecalculator.pref.TAGMANAGER";
    private static final String TAGS_KEY = "com.coolninja.agecalculator.pref.TAGMANAGER.TAGS";

    private Context mContext;
    private JSONArray mTaggedProfilesJson = new JSONArray();
    private SharedPreferences mPref;

    private TagManager(Context context) {
        if (LOG_V) Log.v(LOG_TAG, "Constructing a new Tag Manager");

        mContext = context;
        mPref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    public static TagManager getTagManager(Context context) {
        if (LOG_V) Log.v(LOG_TAG, "Retrieving Tag Manager");
        TagManager tagManager = new TagManager(context);

        SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);

        if (pref.contains(TAGS_KEY)) {
            try {
                tagManager.mTaggedProfilesJson = new JSONArray(pref.getString(TAGS_KEY, ""));
                if (LOG_D)
                    Log.d(LOG_TAG, "Found tag record: " + tagManager.mTaggedProfilesJson.toString(4));

                for (int i = 0; i < tagManager.mTaggedProfilesJson.length(); i++) {
                    JSONObject object = tagManager.mTaggedProfilesJson.getJSONObject(i);

                    Tag tag = Tag.values()[object.getInt(Tag.TAG_NAME_KEY)];
                    JSONArray ids = object.getJSONArray(Tag.PROFILE_IDS_KEY);
                    for (int i2 = 0; i2 < ids.length(); i2++) {
                        int id = ids.getInt(i2);
                        if (tag.getProfileIds().contains(id)) {
                            tag.removeProfile(id);
                        }
                        tag.addProfile(id);
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Couldn't parse tagged ids into json array");
                e.printStackTrace();
            }
        } else {
            if (LOG_V) Log.v(LOG_TAG, "No record found of tagged profile");

            for (Tag t : Tag.values()) {
                tagManager.mTaggedProfilesJson.put(t.initializerToJsonObject());
            }
        }


        return tagManager;
    }

    public static ArrayList<Integer> getTaggedIds(int whichTag) {
        return Tag.values()[whichTag].getProfileIds();
    }

    public void tagProfile(int profileId, int what) {
        if (LOG_V) Log.v(LOG_TAG, "Tagging profile w/ ID " + profileId);

        Tag t = Tag.values()[what];
        if (t.getProfileIds().contains(profileId)) {
            removeTagFromProfile(profileId, what);
        }

        t.addProfile(profileId);

        try {
            mTaggedProfilesJson.getJSONObject(what).getJSONArray(Tag.PROFILE_IDS_KEY).put(profileId);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to put profile w/ ID " + profileId + " into the tagged profiles json");
            e.printStackTrace();
        }

        updatePreference();

        if (mContext instanceof ProfileManagerInterface.onProfilePinnedListener) {
            if (what == TAG_PIN) {
                ((ProfileManagerInterface.onProfilePinnedListener) mContext).onProfilePinned(profileId, true);
            }
        }
    }

    public void removeTagFromProfile(int profileId, int whichTag) {
        Tag tag = Tag.values()[whichTag];
        if (LOG_V)
            Log.v(LOG_TAG, "Removing " + tag.getSimpleName() + " tag from profile w/ ID " + profileId);

        if (!tag.getProfileIds().contains(profileId)) {
            if (LOG_W)
                Log.w(LOG_TAG, "Profile w/ ID " + profileId + " wasn't tagged with " + tag.getSimpleName());
            return;
        }

        tag.removeProfile(profileId);
        try {
            removeProfileFromJsonArray(profileId, mTaggedProfilesJson.getJSONObject(whichTag).getJSONArray(Tag.PROFILE_IDS_KEY));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to remove profile w/ ID " + profileId + " into the tagged profiles json");
            e.printStackTrace();
        }


        if (mContext instanceof ProfileManagerInterface.onProfilePinnedListener) {
            if (whichTag == TAG_PIN) {
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
                    if (LOG_V)
                        Log.v(LOG_TAG, "Removed profile w/ ID " + profileId + " from the tagged profiles json array");
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
        if (LOG_V) {
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

}
