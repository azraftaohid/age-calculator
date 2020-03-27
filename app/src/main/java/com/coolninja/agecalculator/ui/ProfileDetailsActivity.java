package com.coolninja.agecalculator.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.microsoft.officeuifabric.persona.PersonaView;

import java.util.Locale;

public class ProfileDetailsActivity extends AppCompatActivity {
    private PersonaView mProfileView;
    private TextView mDobTextView;
    private TextView mNextBirthdayInTextView;
    private TextView mAgeYearMonthDaysTextView;
    private TextView mAgeYearDaysTextView;
    private TextView mAgeMonthDaysTextView;
    private TextView mAgeInDaysTextView;
    private TextView mAgeInHoursTextView;
    private TextView mAgeInMinutesTextView;
    private TextView mAgeInSecondsTextView;

    private static int defaultErrorCode;
    private static int notFoundErrorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        mProfileView = findViewById(R.id.pv_subjected_profile);
        mDobTextView = findViewById(R.id.tv_dob);
        mNextBirthdayInTextView = findViewById(R.id.tv_next_birthday_in);
        mAgeYearMonthDaysTextView = findViewById(R.id.tv_age_year_month_days);
        mAgeYearDaysTextView = findViewById(R.id.tv_age_year_days);
        mAgeMonthDaysTextView = findViewById(R.id.tv_age_month_days);
        mAgeInDaysTextView = findViewById(R.id.tv_age_days);
        mAgeInHoursTextView = findViewById(R.id.tv_age_hours);
        mAgeInMinutesTextView = findViewById(R.id.tv_age_minutes);
        mAgeInSecondsTextView = findViewById(R.id.tv_age_seconds);

        defaultErrorCode = getResources().getInteger(R.integer.default_error_code);
        notFoundErrorCode = getResources().getInteger(R.integer.not_found_error_code);

        int profileId = getIntent().getIntExtra(MainActivity.EXTRA_PROFILE_ID, notFoundErrorCode);

        if (profileId == notFoundErrorCode)
            throw new AssertionError("Always pass profile ID when starting " + ProfileDetailsActivity.class.getSimpleName());
        else if (profileId == defaultErrorCode) {
            throw new AssertionError("Profile ID is an error code");
        }

        Profile profile = ProfileManager.getProfileManager(this).getProfileById(profileId);
        Birthday birthday = profile.getDateOfBirth();
        Age age = profile.getAge();

        long[] durationBeforeBirthday = age.getDurationBeforeNextBirthday(Age.MODE_MONTH_DAY);

        long[] ageYearMonthDay = age.get(Age.MODE_YEAR_MONTH_DAY);
        long[] ageYearDay = age.get(Age.MODE_YEAR_DAY);
        long[] ageMonthDay = age.get(Age.MODE_MONTH_DAY);
        long ageInDays = age.get(Age.MODE_DAY)[Age.DAY];
        long ageInHours = age.get(Age.MODE_HOUR)[Age.HOUR];
        long ageInMinutes = age.get(Age.MODE_MINUTES)[Age.MINUTE];
        long ageInSeconds = age.get(Age.MODE_SECONDS)[Age.SECOND];

        mProfileView.setName(profile.getName());
        mDobTextView.setText(String.format(Locale.ENGLISH, getString(R.string.long_date_format),
                birthday.getMonth().getShortName(), birthday.get(Birthday.DAY), birthday.get(Birthday.YEAR)));

        mNextBirthdayInTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_months_days),
                durationBeforeBirthday[Age.MONTH], durationBeforeBirthday[Age.DAY]));

        mAgeYearMonthDaysTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_years_months_days),
                ageYearMonthDay[Age.YEAR], ageYearMonthDay[Age.MONTH], ageYearMonthDay[Age.DAY]));
        mAgeYearDaysTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_years_days),
                ageYearDay[Age.YEAR], ageYearDay[Age.DAY]));
        mAgeMonthDaysTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_months_days),
                ageMonthDay[Age.MONTH], ageMonthDay[Age.DAY]));
        mAgeInDaysTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_days), ageInDays));
        mAgeInHoursTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_hours), ageInHours));
        mAgeInMinutesTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_minutes), ageInMinutes));
        mAgeInSecondsTextView.setText(String.format(Locale.ENGLISH, getString(R.string.display_seconds), ageInSeconds));

    }
}
