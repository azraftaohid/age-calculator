package thegoodcompany.aetate.utilities.tagmanagement;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public enum Tag {
    PIN("Pinned");

    static final String TAG_NAME_KEY = "tag.NAME";
    static final String PROFILE_IDS_KEY = "tag.PROFILE_IDS";
    ArrayList<Integer> mProfileIds = new ArrayList<>();
    private String mSimpleName;

    Tag(String simpleName) {
        mSimpleName = simpleName;
    }

    void addProfile(int profileId) {
        mProfileIds.add(0, profileId);
    }

    @SuppressWarnings("unused")
    int getMostRecentlyAdded() {
        return mProfileIds.get(0);
    }

    @SuppressWarnings("unused")
    int getProfileId(int index) {
        return mProfileIds.get(index);
    }

    @NonNull
    ArrayList<Integer> getProfileIds() {
        return mProfileIds;
    }

    boolean removeProfile(int profileId) {
        return mProfileIds.remove(Integer.valueOf(profileId));
    }

    @SuppressWarnings("unused")
    void removeProfileAt(int position) {
        mProfileIds.remove(position);
    }

    String getSimpleName() {
        return mSimpleName;
    }

    @NotNull
    @SuppressWarnings("unused")
    JSONObject toJsonObject() {
        JSONObject object = new JSONObject();

        try {
            object.put(TAG_NAME_KEY, this.ordinal());
            object.put(PROFILE_IDS_KEY, new JSONArray(mProfileIds.toArray()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @NotNull
    JSONObject initializerToJsonObject() {
        JSONObject object = new JSONObject();

        try {
            object.put(TAG_NAME_KEY, this.ordinal());
            object.put(PROFILE_IDS_KEY, new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

}
