package com.coolninja.agecalculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.AddProfileDialog;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.ProfileViewsAdapter;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.coolninja.agecalculator.utilities.tagmanagement.TagManager;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener,
        ProfileManagerInterface.onProfilePinnedListener, ProfileManagerInterface.onProfileAddedListener,
        AddProfileDialog.OnProfileSubmissionListener, ProfileManagerInterface.onProfileRemovedListener {
    public static final String EXTRA_NAME = "com.coolninja.agecalculator.extra.NAME";
    public static final String EXTRA_YEAR = "com.coolninja.agecalculator.extra.YEAR";
    public static final String EXTRA_MONTH = "com.coolninja.agecalculator.extra.MONTH";
    public static final String EXTRA_DAY = "com.coolninja.agecalculator.extra.DAY";
    public static final String EXTRA_PROFILE_ID = "com.coolninja.agecalculator.extra.PROFILE_ID";

    public static final int LOG_LEVEL = Log.VERBOSE;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_V = LOG_LEVEL <= Log.DEBUG;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_D = LOG_LEVEL <= Log.DEBUG;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_I = LOG_LEVEL <= Log.INFO;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_W = LOG_LEVEL <= Log.WARN;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = MainActivity.class.getSimpleName() + ".Performance";

    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView mPinnedProfilesRecyclerView;
    private RecyclerView mOtherProfilesRecyclerView;
    private LinearLayout mPinnedListView;
    private LinearLayout mOthersListView;
    private NestedScrollView mProfilesScrollView;
    private TextView mEmptyProfilesTextView;

    private ProfileManager mProfileManager;
    private ProfileViewsAdapter mPinnedProfileViewsAdapter;
    private ProfileViewsAdapter mOtherProfileViewsAdapter;

    private static final int DOB_REQUEST = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        if (!ProfileManager.containsProfilePreference(this)) {
            launchWelcomeActivity();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPinnedListView = findViewById(R.id.ll_pinned);
        mOthersListView = findViewById(R.id.ll_others);
        mPinnedProfilesRecyclerView = findViewById(R.id.rv_pinned_profiles);
        mOtherProfilesRecyclerView = findViewById(R.id.rv_other_profiles);
        mProfilesScrollView = findViewById(R.id.sv_profiles);
        mEmptyProfilesTextView = findViewById(R.id.tv_empty_profiles);

        mProfileManager = ProfileManager.getProfileManager(this);

        mPinnedProfileViewsAdapter = new ProfileViewsAdapter(this, mProfileManager, mProfileManager.getPinnedProfiles());
        mPinnedProfilesRecyclerView.setAdapter(mPinnedProfileViewsAdapter);
        mPinnedProfilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mOtherProfileViewsAdapter = new ProfileViewsAdapter(this, mProfileManager, mProfileManager.getOtherProfiles());
        mOtherProfilesRecyclerView.setAdapter(mOtherProfileViewsAdapter);
        mOtherProfilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        synchronizeVisibleStatus();

//        showNumberOfProfiles();

//        generateDummyProfiles(15);

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + MainActivity.class.getSimpleName());
        }

    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, DOB_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DOB_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra(EXTRA_NAME);
                Birthday dob = new Birthday(data.getIntExtra(EXTRA_YEAR, Error.NOT_FOUND),
                        data.getIntExtra(EXTRA_MONTH, Error.NOT_FOUND),
                        data.getIntExtra(EXTRA_DAY, Error.NOT_FOUND));

                Profile profile = new Profile(name, dob, new ProfileManagerInterface.onProfileUpdatedListener() {
                    @Override
                    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                        mProfileManager.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
                    }

                    @Override
                    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
                        mProfileManager.onProfileNameUpdated(profileId, newName, previousName);
                    }
                });

                mProfileManager.addProfile(profile);
                mProfileManager.pinProfile(profile.getId(), true);
            }
        }
    }

    public void showAddProfileDialog(View view) {
        AddProfileDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private void generateDummyProfiles(int howMany) {
        if (LOG_V) Log.v(LOG_TAG, "Generating " + howMany + " dummy profiles");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < howMany; i++) {
            onSubmit(("Dummy " + (i + 100)), new Birthday(1920, 5, 24));
        }

        long requiredTime = System.currentTimeMillis() - startTime;
        if (LOG_D) Log.d(LOG_TAG, "It took " + requiredTime + " milliseconds to generate them");
    }

    private void synchronizeVisibleStatus() {
        if (LOG_V) Log.v(LOG_TAG, "Synchronizing visible status");
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        boolean isEmpty = true;

        int pinnedItemCounts = mPinnedProfileViewsAdapter.getItemCount();
        if (LOG_V) Log.v(LOG_TAG, "Pinned items: " + pinnedItemCounts);
        if (pinnedItemCounts == 0) mPinnedListView.setVisibility(View.GONE);
        else {
            mPinnedListView.setVisibility(View.VISIBLE);
            isEmpty = false;
        }

        int otherItemCounts = mOtherProfileViewsAdapter.getItemCount();
        if (LOG_V) Log.v(LOG_TAG, "Other items: " + otherItemCounts);
        if (otherItemCounts == 0) mOthersListView.setVisibility(View.GONE);
        else {
            mOthersListView.setVisibility(View.VISIBLE);
            isEmpty = false;
        }

        if (isEmpty) {
            mProfilesScrollView.setVisibility(View.GONE);
            mEmptyProfilesTextView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyProfilesTextView.setVisibility(View.GONE);
            mProfilesScrollView.setVisibility(View.VISIBLE);
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to synchronize visible status");
        }
    }

    @Override
    public void onSubmit(String name, Birthday dateOfBirth) {
        Profile profile = new Profile(name, dateOfBirth, new ProfileManagerInterface.onProfileUpdatedListener() {
            @Override
            public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                mProfileManager.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
            }

            @Override
            public void onProfileNameUpdated(int profileId, String newName, String previousName) {
                mProfileManager.onProfileNameUpdated(profileId, newName, previousName);
            }
        });

        mProfileManager.addProfile(profile);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        if (TagManager.getTaggedIds(TagManager.TAG_PIN).contains(profileId)) {
            mPinnedProfileViewsAdapter.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
        } else {
            mOtherProfileViewsAdapter.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
        }
    }

    @Override
    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
        if (TagManager.getTaggedIds(TagManager.TAG_PIN).contains(profileId)) {
            mPinnedProfileViewsAdapter.onProfileNameUpdated(profileId, newName, previousName);
        } else {
            mOtherProfileViewsAdapter.onProfileNameUpdated(profileId, newName, previousName);
        }
    }

    @Override
    public void onProfilePinned(int profileId, boolean isPinned) {
        Profile profile = mProfileManager.getProfileById(profileId);
        if (isPinned) {
            mOtherProfileViewsAdapter.removeProfile(profileId);
            mPinnedProfileViewsAdapter.addProfile(profile);
        } else {
            mPinnedProfileViewsAdapter.removeProfile(profileId);
            mOtherProfileViewsAdapter.addProfile(profile);
        }

        synchronizeVisibleStatus();
    }

    @Override
    public void onProfileAdded(Profile profile) {
        mOtherProfileViewsAdapter.addProfile(profile);
        synchronizeVisibleStatus();
        mOtherProfilesRecyclerView.smoothScrollToPosition(mOtherProfileViewsAdapter.getItemCount());
    }

    @Override
    public void onProfileRemoved(int profileId) {
        if (TagManager.getTaggedIds(TagManager.TAG_PIN).contains(profileId)) {
            mPinnedProfileViewsAdapter.removeProfile(profileId);
        } else {
            mOtherProfileViewsAdapter.removeProfile(profileId);
        }

        synchronizeVisibleStatus();
    }
}
