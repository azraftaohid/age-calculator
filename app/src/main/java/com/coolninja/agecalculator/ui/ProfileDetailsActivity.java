package com.coolninja.agecalculator.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Avatar;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.CommonUtilities;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.codes.Extra;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.microsoft.officeuifabric.persona.PersonaView;

import java.util.Calendar;
import java.util.Locale;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_I;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;

public class ProfileDetailsActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener {
    private final static String LOG_TAG = ProfileDetailsActivity.class.getSimpleName();
    private final static String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private Profile mProfile;
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

        final int profileId = getIntent().getIntExtra(Extra.EXTRA_PROFILE_ID, Error.NOT_FOUND);

        if (profileId == Error.NOT_FOUND)
            throw new AssertionError("Always pass a valid profile ID when starting " + ProfileDetailsActivity.class.getSimpleName());
        else if (profileId == Error.DEFAULT) {
            throw new AssertionError("Profile ID is an error code");
        }

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                mProfile = ProfileManager.getProfileManager(ProfileDetailsActivity.this).getProfileById(profileId);
                initUi();
            }
        }).start();

        ImageView accessoryView = CommonUtilities.generateCustomAccessoryView(this, R.drawable.ic_edit);
        accessoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });
        mProfileView.setCustomAccessoryView(accessoryView);

        if (MainActivity.LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + ProfileDetailsActivity.class.getSimpleName());
        }
    }

    private void initUi() {
        Calendar start;
        if (LOG_D) start = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Initializing UI");

        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (LOG_I) Log.i(LOG_TAG, "Current thread wasn't on main ui");
            if (LOG_V) Log.v(LOG_TAG, "Initializing UI by running UI thread");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initUi0();
                }
            });

        } else {
            initUi0();
        }

        if (LOG_D) Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis()) +
                " milliseconds to initialize UI");
    }

    private void initUi0() {
        mProfileView.setName(mProfile.getName());

        Avatar avatar = mProfile.getAvatar();
        if (avatar != null) mProfileView.setAvatarImageBitmap(avatar.getBitmap());

        updateAgeRelatedUi();
    }

    private void updateAgeRelatedUi() {
        Birthday birthday = mProfile.getBirthday();
        Age age = mProfile.getAge();

        long[] durationBeforeBirthday = age.getDurationBeforeNextBirthday(Age.MODE_MONTH_DAY);
        long[] ageYearMonthDay = age.get(Age.MODE_YEAR_MONTH_DAY);
        long[] ageYearDay = age.get(Age.MODE_YEAR_DAY);
        long[] ageMonthDay = age.get(Age.MODE_MONTH_DAY);
        long ageInDays = age.get(Age.MODE_DAY)[Age.DAY];
        long ageInHours = age.get(Age.MODE_HOUR)[Age.HOUR];
        long ageInMinutes = age.get(Age.MODE_MINUTES)[Age.MINUTE];
        long ageInSeconds = age.get(Age.MODE_SECONDS)[Age.SECOND];

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

    private void showRenameDialog() {
        RenameDialog.newInstance(mProfile).show(getSupportFragmentManager(), getString(R.string.rename_dialog_tag));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        updateAgeRelatedUi();
    }

    @Override
    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
        mProfileView.setName(newName);
    }

    @Override
    public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        mProfileView.setAvatarImageDrawable(newAvatar.getCircularDrawable());
    }

    private void refresh() {
        if (LOG_V) Log.v(LOG_TAG, "Refreshing profile details");
        if (!mRefreshLayout.isRefreshing()) mRefreshLayout.setRefreshing(true);

        initUi0();
        mRefreshLayout.setRefreshing(false);
    }

    public void copyValue(View view) {
        if (LOG_V) Log.v(LOG_TAG, "Trying to copy property value into clipboard");

        if (!(view instanceof TextView)) {
            Log.e(LOG_TAG, "Couldn't not copy value; view wasn't a text view");
            return;
        }

        TextView textView = (TextView) view;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(null, textView.getText());

        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(data);
            if (LOG_D) Log.d(LOG_TAG, "Copied data");

            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        }
        else Log.e(LOG_TAG, "Couldn't get clipboard manager");
    }
}
