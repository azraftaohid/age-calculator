package com.coolninja.agecalculator.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.microsoft.officeuifabric.persona.PersonaView;

import java.util.Calendar;
import java.util.Locale;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_W;

public class ProfileDetailsActivity extends AppCompatActivity {
    private final static String LOG_TAG = ProfileDetailsActivity.class.getSimpleName();

    private ProfileManager mProfileManager;
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
    private SwipeRefreshLayout mRefreshLayout;

    private Thread mDisplayDetailsThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Calendar startTime;
        if (MainActivity.LOG_D) startTime = Calendar.getInstance();

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
        mRefreshLayout = findViewById(R.id.srl_refresh_details);

        final int profileId = getIntent().getIntExtra(MainActivity.EXTRA_PROFILE_ID, Error.NOT_FOUND);

        if (profileId == Error.NOT_FOUND)
            throw new AssertionError("Always pass profile ID when starting " + ProfileDetailsActivity.class.getSimpleName());
        else if (profileId == Error.DEFAULT) {
            throw new AssertionError("Profile ID is an error code");
        }

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mDisplayDetailsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mProfileManager == null) mProfileManager = ProfileManager.getProfileManager(ProfileDetailsActivity.this);

                final Profile profile = mProfileManager.getProfileById(profileId);
                final Birthday birthday = profile.getBirthday();
                Age age = profile.getAge();

                final long[] durationBeforeBirthday = age.getDurationBeforeNextBirthday(Age.MODE_MONTH_DAY);
                final long[] ageYearMonthDay = age.get(Age.MODE_YEAR_MONTH_DAY);
                final long[] ageYearDay = age.get(Age.MODE_YEAR_DAY);
                final long[] ageMonthDay = age.get(Age.MODE_MONTH_DAY);
                final long ageInDays = age.get(Age.MODE_DAY)[Age.DAY];
                final long ageInHours = age.get(Age.MODE_HOUR)[Age.HOUR];
                final long ageInMinutes = age.get(Age.MODE_MINUTES)[Age.MINUTE];
                final long ageInSeconds = age.get(Age.MODE_SECONDS)[Age.SECOND];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                });

            }
        });

        mDisplayDetailsThread.start();

        if (MainActivity.LOG_D) {
            Log.d(LOG_TAG, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + ProfileDetailsActivity.class.getSimpleName());
        }
    }

    private void refresh() {
        if (LOG_V) Log.v(LOG_TAG, "Refreshing profile details");

        if (mDisplayDetailsThread.isAlive()) {
            if (LOG_W) Log.w(LOG_TAG, "Thread display details is already running");
            return;
        }

        //noinspection CallToThreadRun
        mDisplayDetailsThread.run(); //No need to start a new thread now, besides, it is illegal to start a thread more than twice
        mRefreshLayout.setRefreshing(false);
    }
}
