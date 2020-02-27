package com.coolninja.agecalculator.utilities.profilemanagement;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.DatePicker;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.BirthdayPickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ProfileManager {
    private static final String PROFILE_MANAGER_PREF = "com.coolninja.agecalculator.agecalculator.pref.PROFILEMANAGER";
    private static final String BIRTH_YEAR_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHYEARKEYFORPROFILE%d";
    private static final String BIRTH_MONTH_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHMONTHKEYFORPROFILE%d";
    private static final String BIRTH_DAY_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.BIRTHDAYKEYFORPROFILE%d";
    private static final String PROFILE_IDs_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.IDs";
    private static final String DEFAULT_PROFILE_ID_KEY = "com.coolninja.agecalculator.pref.PROFILEMANAGER.DEFAULTID";

    private static final String LOG_TAG = ProfileManager.class.getSimpleName();
    private final int DEFAULT_ERROR_CODE;

    private static int profileId;

    private Context mContext;
    private SharedPreferences mPref;
    private ArrayList<Profile> mProfiles = new ArrayList<>();

    private ProfileManager(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        DEFAULT_ERROR_CODE = Integer.parseInt(mContext.getString(R.string.default_error_value));
    }

    public static ProfileManager getProfileManager(Context context) {
        Log.i(LOG_TAG, "Initiating profile manager");
        final ProfileManager manager = new ProfileManager(context);

        SharedPreferences pref = context.getSharedPreferences(PROFILE_MANAGER_PREF, Context.MODE_PRIVATE);
        Set<String> ids = pref.getStringSet(PROFILE_IDs_KEY, new HashSet<String>());
        int defaultProfileId = pref.getInt(DEFAULT_PROFILE_ID_KEY, Integer.parseInt(context
                .getString(R.string.default_error_value)));

        for (String strId : ids) {
            int id = Integer.parseInt(strId);
            Log.i(LOG_TAG, "ID found: " + id);

            int year = pref.getInt(String.format(Locale.ENGLISH, BIRTH_YEAR_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_year_key)));
            Log.i(LOG_TAG, "Birth year: " + year);
            int month = pref.getInt(String.format(Locale.ENGLISH, BIRTH_MONTH_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_month_key)));
            Log.i(LOG_TAG, "Birth month: " + month);
            int day = pref.getInt(String.format(Locale.ENGLISH, BIRTH_DAY_KEY, id),
                    Integer.parseInt(context.getString(R.string.default_birth_day_key)));
            Log.i(LOG_TAG, "Birth day: " + day);

            final Calendar dob = Calendar.getInstance();
            dob.set(year, month, day);

            if (profileId < id) {
                profileId = id;
                Log.i(LOG_TAG, "Max already set profile id is now: " + profileId);
            }

            final Profile profile = new Profile(null, dob);
            profile.setBirthdayPicker(BirthdayPickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar dob = Calendar.getInstance();
                    dob.set(Calendar.YEAR, year);
                    dob.set(Calendar.MONTH, month);
                    dob.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    Log.i(LOG_TAG, "Updating date of birth for profile ID: " + profile.getId());

                    profile.setDateOfBirth(dob);
                    manager.updateProfileInPreference(profile);
                }
            }, year, month, day));

            if (id == defaultProfileId) {
                Log.i(LOG_TAG, "Default profile found, ID: " + id);
                manager.addProfile(profile, id, true);
            } else {
                manager.addProfile(profile, id);
            }

        }
        
        return manager;
    }

    public void addProfile(Profile profile) {
        ArrayList<Integer> ids = getProfileIds();
        if (profile.getId() == -1) {
            do {
                profile.setId(profileId);
            } while (ids.contains(profileId++));
        }

        mProfiles.add(profile);
        ids.add(profile.getId());

        HashSet<String> strIds = new HashSet<>();
        for (Integer id : getProfileIds()) {
            strIds.add(id.toString());
        }

        SharedPreferences.Editor editor = mPref.edit();

        editor.putStringSet(PROFILE_IDs_KEY, strIds);
        updateProfileInPreference(profile);
        editor.apply();
    }

    private void updateProfileInPreference(Profile profile) {
        SharedPreferences.Editor editor = mPref.edit();

        editor.putInt(String.format(Locale.ENGLISH, BIRTH_YEAR_KEY, profile.getId()), profile.getDateOfBirth().get(Calendar.YEAR));
        editor.putInt(String.format(Locale.ENGLISH, BIRTH_MONTH_KEY, profile.getId()), profile.getDateOfBirth().get(Calendar.MONTH));
        editor.putInt(String.format(Locale.ENGLISH, BIRTH_DAY_KEY, profile.getId()), profile.getDateOfBirth().get(Calendar.DAY_OF_MONTH));

        Log.i(LOG_TAG, "Date of birth for profile " + profile.getId() + " is set to: " +
                profile.getDateOfBirth().get(Calendar.MONTH) + "/" +
                profile.getDateOfBirth().get(Calendar.DAY_OF_MONTH) + "/" +
                profile.getDateOfBirth().get(Calendar.YEAR));

        editor.apply();
    }

    private void addProfile(Profile profile, int id) {
        if (getProfileIds().contains(id))
            throw new AssertionError("Another profile with the same id already exists");

        profile.setId(id);
        addProfile(profile);
    }

    public void addProfile(Profile profile, boolean setDefault) {
        addProfile(profile);

        if (setDefault) {
            setDefaultProfile(profile);
        }
    }

    public void addProfile(Profile profile, int id, boolean setDefault) {
        addProfile(profile, id);

        if (setDefault) {
            setDefaultProfile(profile);
        }
    }

    private ArrayList<Integer> getProfileIds() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Profile profile : getProfiles()) {
            ids.add(profile.getId());
        }

        return ids;
    }

    public void setDefaultProfile(Profile profile) {
        SharedPreferences.Editor editor = mPref.edit();

        editor.putInt(DEFAULT_PROFILE_ID_KEY, profile.getId());

        editor.apply();
    }

    public Profile getDefaultProfile() {
        return getProfileById(mPref.getInt(DEFAULT_PROFILE_ID_KEY, DEFAULT_ERROR_CODE));
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

    private void setContext(Context context) {
        mContext = context;
    }
}
