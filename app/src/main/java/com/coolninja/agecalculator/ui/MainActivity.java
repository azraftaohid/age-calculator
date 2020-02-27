package com.coolninja.agecalculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.coolninja.agecalculator.utilities.AddProfileDialog;
import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microsoft.officeuifabric.persona.PersonaListView;
import com.microsoft.officeuifabric.persona.PersonaView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static String EXTRA_YEAR = "com.coolninja.agecalculator.extra.year";
    public static String EXTRA_MONTH = "com.coolninja.agecalculator.extra.month";
    public static String EXTRA_DAY = "com.coolninja.agecalculator.extra.day";

    private PersonaView mPrimaryUserPersonaView;
    private PersonaListView mPersonaListView;
    private FloatingActionButton addProfileFab;

    private AddProfileDialog mAddProfileDialog;
    private ProfileManager mProfileManager;

    private int mDefaultErrorCode;
    static final int DEFAULT_DOB_REQUEST = 1111;

    //TODO Add ability to choose between different age viewing formats
    //TODO Add ability to add more users birthday
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProfileManager = ProfileManager.getProfileManager(this);

        if (mProfileManager.getDefaultProfile() == null) {
            launchWelcomeActivity();
        }

        setContentView(R.layout.activity_main);

        mDefaultErrorCode = Integer.parseInt(getString(R.string.default_error_value));

        mPrimaryUserPersonaView = findViewById(R.id.pv_primary_user);
        mPersonaListView = findViewById(R.id.lv_persona);

        if (mProfileManager.getDefaultProfile() != null) {
            refreshProfileViews();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_change_dob) {
            Profile defProfile = mProfileManager.getDefaultProfile();
            defProfile.getBirthdayPicker().show(getSupportFragmentManager(),
                    String.format(Locale.ENGLISH, getString(R.string.birthday_picker_tag), defProfile.getId()));
        }

        return super.onOptionsItemSelected(item);
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
                Calendar dob = Calendar.getInstance();
                dob.set(data.getIntExtra(EXTRA_YEAR, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_MONTH, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_DAY, mDefaultErrorCode));
                Profile profile = new Profile(null, dob);
                mProfileManager.addProfile(profile, true);
                updateAgeForProfile(profile);
            }
        }
    }

    private void refreshProfileViews() {
        int defaultProfileId = mProfileManager.getDefaultProfile().getId();

        for (Profile profile : mProfileManager.getProfiles()) {
            Age age = profile.getAge();

            if (defaultProfileId == profile.getId()) {
                mPrimaryUserPersonaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                        age.getYear(), age.getMonth(), age.getDay()));
            }

        }
    }

    private void updateAgeForProfile(Profile profile) {
        int defaultProfileId = mProfileManager.getDefaultProfile().getId();

        if (profile.getId() == defaultProfileId) {
            Age age = profile.getAge();

            if (profile.getName() != null) {
                mPrimaryUserPersonaView.setName(profile.getName());
            }
            mPrimaryUserPersonaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                    age.getYear(), age.getMonth(), age.getDay()));
        }
    }

}
