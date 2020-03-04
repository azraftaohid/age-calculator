package com.coolninja.agecalculator.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.coolninja.agecalculator.utilities.AddProfileDialog;
import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.microsoft.officeuifabric.persona.PersonaView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface
        .onProfileUpdateListener, ProfileManagerInterface.onProfilePinListener, AddProfileDialog.OnNewProfileAddedListener {
    public static final String EXTRA_NAME = "com.coolninja.agecalculator.extra.NAME";
    public static final String EXTRA_YEAR = "com.coolninja.agecalculator.extra.YEAR";
    public static final String EXTRA_MONTH = "com.coolninja.agecalculator.extra.MONTH";
    public static final String EXTRA_DAY = "com.coolninja.agecalculator.extra.DAY";

    private LinearLayout mPinnedProfilesListView;
    private LinearLayout mOtherProfilesListView;

    private ProfileManager mProfileManager;

    private int mDefaultErrorCode;
    static final int DEFAULT_DOB_REQUEST = 1111;

    //TODO Add ability to choose between different age viewing formats
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProfileManager = ProfileManager.getProfileManager(this);

        if (mProfileManager.getProfiles().size() == 0) {
            launchWelcomeActivity();
        }

        setContentView(R.layout.activity_main);

        mDefaultErrorCode = Integer.parseInt(getString(R.string.default_error_value));
        mPinnedProfilesListView = findViewById(R.id.ll_pinned_profiles);
        mOtherProfilesListView = findViewById(R.id.ll_other_profiles);

        if (mProfileManager.getProfiles().size() != 0) {
            refreshProfileViews();
        }

    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, DEFAULT_DOB_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_DOB_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra(EXTRA_NAME);

                Calendar dob = Calendar.getInstance();
                dob.set(data.getIntExtra(EXTRA_YEAR, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_MONTH, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_DAY, mDefaultErrorCode));

                Profile profile = new Profile(name, dob, new ProfileManagerInterface.onProfileUpdateListener() {
                    @Override
                    public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
                        mProfileManager.onProfileDateOfBirthChange(profileId, newDateOfBirth, previousDateOfBirth);
                    }

                    @Override
                    public void onProfileNameChange(int profileId, String newName, String previousName) {
                        mProfileManager.onProfileNameChange(profileId, newName, previousName);
                    }
                });

                mProfileManager.addProfile(profile);
                PersonaView personaView = new PersonaView(this);
                Age age = profile.getAge();

                personaView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                personaView.setName(profile.getName());
                personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                        age.getYear(), age.getMonth(), age.getDay()));
                personaView.setId(profile.getId());
                mOtherProfilesListView.addView(personaView);
            }
        }
    }

    public void showAddProfileDialog(View view) {
        AddProfileDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    private void refreshProfileViews() {
        for (Profile profile : mProfileManager.getProfiles()) {
            Age age = profile.getAge();
            PersonaView personaView = new PersonaView(this);

            personaView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            personaView.setName(profile.getName());
            personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                    age.getYear(), age.getMonth(), age.getDay()));
            personaView.setId(profile.getId());

            if (mProfileManager.getPinnedProfileIds().contains(profile.getId()))
                mPinnedProfilesListView.addView(personaView);
            else
                mOtherProfilesListView.addView(personaView);

        }
    }

    private void updateViewForProfile(int profileId) {
        for (int i = 0; i < mPinnedProfilesListView.getChildCount(); i++) {
            PersonaView personaView = (PersonaView) mPinnedProfilesListView.getChildAt(i);

            if (personaView.getId() == profileId) {
                Profile profile = mProfileManager.getProfileById(profileId);
                Age age = profile.getAge();

                personaView.setName(profile.getName());
                personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                        age.getYear(), age.getMonth(), age.getDay()));
                return;
            }
        }

        for (int i = 0; i < mOtherProfilesListView.getChildCount(); i++) {
            PersonaView personaView = (PersonaView) mOtherProfilesListView.getChildAt(i);

            if (personaView.getId() == profileId) {
                Profile profile = mProfileManager.getProfileById(profileId);
                Age age = profile.getAge();

                personaView.setName(profile.getName());
                personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                        age.getYear(), age.getMonth(), age.getDay()));
                return;
            }
        }

    }

    @Override
    public void onSubmit(String name, Calendar dateOfBirth) {
        Profile profile = new Profile(name, dateOfBirth, new ProfileManagerInterface.onProfileUpdateListener() {
            @Override
            public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
                mProfileManager.onProfileDateOfBirthChange(profileId, newDateOfBirth, previousDateOfBirth);
            }

            @Override
            public void onProfileNameChange(int profileId, String newName, String previousName) {
                mProfileManager.onProfileNameChange(profileId, newName, previousName);
            }
        });

        mProfileManager.addProfile(profile);

        PersonaView personaView = new PersonaView(this);
        Age age = profile.getAge();

        personaView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        personaView.setName(name);
        personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                age.getYear(), age.getMonth(), age.getDay()));
        personaView.setId(profile.getId());
        mOtherProfilesListView.addView(personaView);
    }

    @Override
    public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {

    }

    @Override
    public void onProfileNameChange(int profileId, String newName, String previousName) {

    }

    @Override
    public void onProfilePin(int profileId, boolean isPinned) {

    }
}
