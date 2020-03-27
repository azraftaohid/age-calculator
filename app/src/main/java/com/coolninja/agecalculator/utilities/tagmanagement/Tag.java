package com.coolninja.agecalculator.utilities.tagmanagement;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

enum Tag {
    PIN("pin");

    private String mSimpleName;
    static final String TAG_NAME_KEY = "tag.NAME";
    static final String PROFILE_IDS_KEY = "tag.PROFILE_IDS";
    ArrayList<Integer> mProfileIds = new ArrayList<>();

    Tag(String simpleName) {
        mSimpleName = simpleName;
    }

    void addProfile(int profileId) {
        mProfileIds.add(0, profileId);
    }

    int getMostRecentlyAdded() {
        return mProfileIds.get(0);
    }

    int getProfileId(int index) {
        return mProfileIds.get(index);
    }

    ArrayList<Integer> getProfileIds() {
        return mProfileIds;
    }

    void removeProfile(int profileId) {
        int size = mProfileIds.size(); //So don't have to count size in each loop
        for (int i = 0; i < size; i++) {
            if (mProfileIds.get(i) == profileId) {
                mProfileIds.remove(i);
                break; //Must call the break statement
            }
        }
    }

    String getSimpleName() {
        return mSimpleName;
    }

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
