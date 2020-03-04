package com.coolninja.agecalculator.utilities.profilemanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coolninja.agecalculator.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ProfileManager implements ProfileManagerInterface.onProfileUpdateListener {
    private static final String PROFILE_MANAGER_PREF = "com.coolninja.agecalculator.agecalculator.pref.PROFILEMANAGER";
    private static final String NAME_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.NAMEKEYFORPROFILE%d";
    private static final String BIRTH_YEAR_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHYEARKEYFORPROFILE%d";
    private static final String BIRTH_MONTH_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHMONTHKEYFORPROFILE%d";
    private static final String BIRTH_DAY_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHDAYKEYFORPROFILE%d";
    private static final String PROFILE_IDS_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.IDs";
    private static final String PINNED_PROFILE_IDS_KEY = "com.coolninja.agecalculator.agecalculator.pref.PROFILEMANAGER.PINNEDPROFILEIDS";

    private static final String LOG_TAG = ProfileManager.class.getSimpleName();
    private final int DEFAULT_ERROR_CODE;

    private static int nextProfileId;

    private Context mContext;
    private SharedPreferences mPref;
    private ArrayList<Profile> mProfiles = new ArrayList<>();
    private ArrayList<Integer> mPinnedIds = new ArrayList<>();

    private ProfileManager(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        DEFAULT_ERROR_CODE = Integer.parseInt(mContext.getString(R.string.default_error_value));
    }

    public static ProfileManager getProfileManager(Context context) {
        Log.i(LOG_TAG, "Initiating profile manager");
        final ProfileManager manager = new ProfileManager(context);

        SharedPreferences pref = context.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        Set<String> ids = pref.getStringSet(PROFILE_IDS_KEY, new HashSet<String>());

        for (String strId : ids) {
            int id = Integer.parseInt(strId);
            Log.i(LOG_TAG, "Profile ID found: " + id);

            String name = pref.getString(String.format(Locale.ENGLISH, NAME_KEY, id),
                    context.getString(R.string.default_name));
            if (name.equals(context.getString(R.string.default_name)))
                Log.w(LOG_TAG, "Profile name is missing");

            int year = pref.getInt(String.format(Locale.ENGLISH, BIRTH_YEAR_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_year)));
            if (year == Integer.parseInt(context.getString(R.string.default_birth_year)))
                Log.w(LOG_TAG, "Profile birth year is missing");

            int month = pref.getInt(String.format(Locale.ENGLISH, BIRTH_MONTH_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_month)));
            if (month == Integer.parseInt(context.getString(R.string.default_birth_month)))
                Log.w(LOG_TAG, "Profile birth month is missing");

            int day = pref.getInt(String.format(Locale.ENGLISH, BIRTH_DAY_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_day)));
            if (day == Integer.parseInt(context.getString(R.string.default_birth_day)))
                Log.w(LOG_TAG, "Profile birth year is missing");

            final Calendar dob = Calendar.getInstance();
            dob.set(year, month, day);

            if (nextProfileId < id) {
                nextProfileId = id;
                Log.i(LOG_TAG, "Max already set profile id is now: " + nextProfileId);
            }

            Profile profile = new Profile(name, dob, manager);
            profile.setId(id);
            manager.addProfile(profile);
        }

        Set<String> strPinnedIds = pref.getStringSet(PINNED_PROFILE_IDS_KEY, new HashSet<String>());
        ArrayList<Integer> pinnedIds = new ArrayList<>();
        for (String pinnedId : strPinnedIds) {
            pinnedIds.add(Integer.parseInt(pinnedId));
        }

        manager.setPinnedIds(pinnedIds);
        
        return manager;
    }

    public void addProfile(Profile profile) {
        ArrayList<Integer> ids = getProfileIds();
        if (profile.getId() == Profile.DEFAULT_ERROR_CODE) {
            do {
                profile.setId(nextProfileId);
            } while (ids.contains(nextProfileId++));
        }

        mProfiles.add(profile);
        ids.add(profile.getId());

        HashSet<String> strIds = new HashSet<>();
        for (Integer id : getProfileIds()) {
            strIds.add(id.toString());
        }

        SharedPreferences.Editor editor = mPref.edit();
        editor.putStringSet(PROFILE_IDS_KEY, strIds);
        editor.apply();
    }

    @Override
    public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
        SharedPreferences.Editor editor = mPref.edit();

        editor.putInt(String.format(Locale.ENGLISH, BIRTH_YEAR_KEY, profileId), newDateOfBirth.get(Calendar.YEAR));
        editor.putInt(String.format(Locale.ENGLISH, BIRTH_MONTH_KEY, profileId), newDateOfBirth.get(Calendar.MONTH));
        editor.putInt(String.format(Locale.ENGLISH, BIRTH_DAY_KEY, profileId), newDateOfBirth.get(Calendar.DAY_OF_MONTH));

        editor.apply();

        if (mContext instanceof ProfileManagerInterface.onProfileUpdateListener)
            ((ProfileManagerInterface.onProfileUpdateListener) mContext).onProfileDateOfBirthChange(profileId, newDateOfBirth,previousDateOfBirth);
    }

    @Override
    public void onProfileNameChange(int profileId, String newName, String previousName) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(String.format(Locale.ENGLISH, NAME_KEY, profileId), newName);
        editor.apply();

        if (mContext instanceof ProfileManagerInterface.onProfileUpdateListener)
            ((ProfileManagerInterface.onProfileUpdateListener) mContext).onProfileNameChange(profileId, newName, previousName);
    }


    public ArrayList<Integer> getProfileIds() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Profile profile : getProfiles()) {
            ids.add(profile.getId());
        }

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

        for (Profile profile : getProfiles()) {
            if (profile.getId() == id) {
                return profile;
            }
        }
        Log.w(LOG_TAG, "No profile found with the profile id: " + id);

        return null;
    }

    private void setPinnedIds(ArrayList<Integer> pinnedIds) {
        this.mPinnedIds = pinnedIds;
    }

    public void pinProfile(int profileId, boolean isPinned) {
        if (isPinned)
            mPinnedIds.add(profileId);
        else
            mPinnedIds.remove(Integer.valueOf(profileId));

        HashSet<String> ids = new HashSet<>();
        for (int id : mPinnedIds) {
            ids.add(String.valueOf(id));
        }

        SharedPreferences.Editor editor = mPref.edit();
        editor.putStringSet(PINNED_PROFILE_IDS_KEY, ids);
        editor.apply();

        if (mContext instanceof ProfileManagerInterface.onProfilePinListener) {
            ((ProfileManagerInterface.onProfilePinListener) mContext).onProfilePin(profileId, isPinned);
        }
    }

    public ArrayList<Integer> getPinnedProfileIds() {
        return mPinnedIds;
    }

    static int generateProfileId() {
        return nextProfileId++;
    }
}
