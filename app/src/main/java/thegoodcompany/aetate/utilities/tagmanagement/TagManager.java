package thegoodcompany.aetate.utilities.tagmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;

public class TagManager {
    public static final int TAG_NONE = -1;
    public static final int TAG_PIN = Tag.PIN.ordinal();

    private static final String LOG_TAG = TagManager.class.getSimpleName();
    private static final String PERFORMANCE_TAG = LOG_TAG + ".performance";

    private static TagManager instance;

    private static final String PREF_KEY = "thegoodcompany.aetate.pref.TAGMANAGER";
    private static final String TAGS_KEY = "thegoodcompany.aetate.pref.TAGMANAGER.TAGS";
    private SharedPreferences mPreference;
    private JSONArray mTaggedArray = new JSONArray();
    private ArrayList<OnTaggedListener> mOnTaggedListeners = new ArrayList<>();

    private TagManager(Context context) {
        if (LOG_V) Log.v(LOG_TAG, "Constructing a new Tag Manager");

        mPreference = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    @NonNull
    public static TagManager getTagManager(Context context) {
        if (instance == null) {
            if (LOG_V) Log.v(LOG_TAG, "Retrieving Tag Manager");
            instance = new TagManager(context);

            if (instance.mPreference.contains(TAGS_KEY)) {
                try {
                    instance.mTaggedArray = new JSONArray(instance.mPreference.getString(TAGS_KEY, ""));
                    if (LOG_D)
                        Log.d(LOG_TAG, "Found tagged record: " + instance.mTaggedArray.toString(4));

                    for (int i = 0; i < instance.mTaggedArray.length(); i++) {
                        JSONObject object = instance.mTaggedArray.getJSONObject(i);

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
                if (LOG_V) Log.v(LOG_TAG, "No record found for tagged profile");

                for (Tag t : Tag.values()) {
                    instance.mTaggedArray.put(t.initializerToJsonObject());
                }
            }
        }

        return instance;
    }

    public static ArrayList<Integer> getTaggedIds(@NonNull Tag which) {
        return which.getProfileIds();
    }

    public void addOnTaggedListener(OnTaggedListener listener) {
        mOnTaggedListeners.add(listener);
    }

    public void tagProfile(int profileId, Tag which) {
        if (LOG_V) Log.v(LOG_TAG, "Tagging profile w/ ID " + profileId);

        if (which.getProfileIds().contains(profileId)) {
            removeTagFromProfile(profileId, which);
        }

        which.addProfile(profileId);

        try {
            mTaggedArray.getJSONObject(which.ordinal()).getJSONArray(Tag.PROFILE_IDS_KEY).put(profileId);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to put profile w/ ID " + profileId + " into the tagged profiles json");
            e.printStackTrace();
        }

        updatePreference();
        triggerOnTaggedListener(which, profileId);
    }

    private boolean removeTagFromProfileSilent(int profileId, @NonNull Tag which) {
        if (which.removeProfile(profileId)) {
            try {
                removeProfileFromJsonArray(profileId, mTaggedArray.getJSONObject(which.ordinal()).getJSONArray(Tag.PROFILE_IDS_KEY));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public boolean removeTagFromProfile(int profileId, Tag which) {
        if (LOG_V)
            Log.v(LOG_TAG, "Removing " + which.getSimpleName() + " tag from profile w/ ID " + profileId);

        if (which.removeProfile(profileId)) {
            try {
                removeProfileFromJsonArray(profileId, mTaggedArray.getJSONObject(which.ordinal()).getJSONArray(Tag.PROFILE_IDS_KEY));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to remove profile w/ ID " + profileId + " into the tagged profiles json");
                e.printStackTrace();
            }

            triggerOnTagRemovedListener(which, profileId);
            return true;
        }

        return false;
    }

    public void removeAllTagsFromProfileSilent(int profileId) {
        for (Tag t : Tag.values()) {
            removeTagFromProfileSilent(profileId, t);
        }
    }

    public List<Tag> removeAllTagsFromProfile(int profileId) {
        ArrayList<Tag> removedTags = new ArrayList<>();
        for (Tag t : Tag.values()) {
            if (removeTagFromProfile(profileId, t)) removedTags.add(t);
        }

        return removedTags;
    }

    public List<Tag> prepareProfileForRemove(int profileId) {
        ArrayList<Tag> removedTags = new ArrayList<>();
        for (Tag t : Tag.values()) {
            if (removeTagFromProfileSilent(profileId, t)) removedTags.add(t);
        }

        return removedTags;
    }

    private void removeProfileFromJsonArray(int profileId, @NotNull JSONArray jsonArray) {
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
                Log.v(LOG_TAG, "Updating tags preference w/ " + mTaggedArray.toString(4));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error logging statement");
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(TAGS_KEY, mTaggedArray.toString());
        editor.apply();
    }

    private void triggerOnTaggedListener(Tag which, int id) {
        boolean needsCleanup = false;

        for (OnTaggedListener listener : mOnTaggedListeners) {
            if (listener != null) listener.onTagged(which, id);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnTaggedListeners.removeAll(Collections.singleton(null));
    }

    private void triggerOnTagRemovedListener(Tag which, int id) {
        boolean needsCleanup = false;

        for (OnTaggedListener listener : mOnTaggedListeners) {
            if (listener != null) listener.onTagRemoved(which, id);
            else needsCleanup = true;
        }

        if (needsCleanup) mOnTaggedListeners.removeAll(Collections.singleton(null));
    }

    public interface OnTaggedListener {
        void onTagged(Tag which, int id);

        void onTagRemoved(Tag which, int id);
    }

}
