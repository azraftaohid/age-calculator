package com.coolninja.agecalculator.utilities.tagmanagement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

enum Tag {
    PIN("pin");

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

    ArrayList<Integer> getProfileIds() {
        return mProfileIds;
    }

    void removeProfile(int profileId) {
        mProfileIds.remove(Integer.valueOf(profileId));
    }

    @SuppressWarnings("unused")
    void removeProfileAt(int position) {
        mProfileIds.remove(position);
    }

    String getSimpleName() {
        return mSimpleName;
    }

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
