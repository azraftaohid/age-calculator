package com.coolninja.agecalculator.utilities;

import com.coolninja.agecalculator.utilities.profilemanagement.Profile;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class ProfilesSection {
    private String mTitle;
    private ArrayList<Profile> mProfiles;

    public ProfilesSection(ArrayList<Profile> profiles, String title) {
        mProfiles = profiles;
        mTitle = title;
    }

    public void addProfile(Profile profile) {
        mProfiles.add(profile);
    }

    public void addProfile(int index, Profile profile) {
        mProfiles.add(index, profile);
    }

    public String getTitle() {
        return mTitle;
    }

    public Profile getProfile(int index) {
        return mProfiles.get(index);
    }

    public int getProfileCount() {
        return mProfiles.size();
    }

    public int getItemCount() {
        return mProfiles.size() + 1;
    }

    public void removeProfile(int index) {
        mProfiles.remove(index);
    }

    @SuppressWarnings("unused")
    public void removeProfile(Profile profile) {
        mProfiles.remove(profile);
    }
}
