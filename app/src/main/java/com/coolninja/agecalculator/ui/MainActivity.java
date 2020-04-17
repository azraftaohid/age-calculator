package com.coolninja.agecalculator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.ProfileInfoDialog;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.ProfileViewsAdapter;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.codes.Request;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.coolninja.agecalculator.utilities.tagmanagement.TagManager;

import java.util.Calendar;

import static com.coolninja.agecalculator.utilities.codes.Error.NOT_FOUND;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_DAY;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_MONTH;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_NAME;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_YEAR;
import static com.coolninja.agecalculator.utilities.codes.Request.REQUEST_DATE_OF_BIRTH;
import static com.coolninja.agecalculator.utilities.codes.Request.REQUEST_NEW_PROFILE_INFO;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener,
        ProfileManagerInterface.onProfilePinnedListener, ProfileManagerInterface.onProfileAddedListener,
        ProfileInfoDialog.OnProfileInfoSubmitListener, ProfileManagerInterface.onProfileRemovedListener {

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
    private SwipeRefreshLayout mRefreshProfilesLayout;

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
        mRefreshProfilesLayout = findViewById(R.id.srl_refresh_profiles);

        mProfileManager = ProfileManager.getProfileManager(this);

        mPinnedProfileViewsAdapter = new ProfileViewsAdapter(this, mProfileManager, mProfileManager.getPinnedProfiles());
        mPinnedProfilesRecyclerView.setAdapter(mPinnedProfileViewsAdapter);
        mPinnedProfilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mOtherProfileViewsAdapter = new ProfileViewsAdapter(this, mProfileManager, mProfileManager.getOtherProfiles());
        mOtherProfilesRecyclerView.setAdapter(mOtherProfileViewsAdapter);
        mOtherProfilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        synchronizeVisibleStatus();

        mRefreshProfilesLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProfiles();
            }
        });

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + MainActivity.class.getSimpleName());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refreshProfiles();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, REQUEST_NEW_PROFILE_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_PROFILE_INFO && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra(EXTRA_NAME);
                Birthday dob = new Birthday(data.getIntExtra(EXTRA_YEAR, NOT_FOUND),
                        data.getIntExtra(EXTRA_MONTH, NOT_FOUND),
                        data.getIntExtra(EXTRA_DAY, NOT_FOUND));

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
        ProfileInfoDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO).show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private void generateDummyProfiles(int howMany) {
        if (LOG_V) Log.v(LOG_TAG, "Generating " + howMany + " dummy profiles");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < howMany; i++) {
            onProfileInfoSubmit(Request.REQUEST_NEW_PROFILE_INFO, ("Dummy " + (i + 100)), new Birthday(1920, 5, 24));
        }

        long requiredTime = System.currentTimeMillis() - startTime;
        if (LOG_D) Log.d(LOG_TAG, "It took " + requiredTime + " milliseconds to generate them");
    }

    private void synchronizeVisibleStatus() {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();
        if (LOG_V) Log.v(LOG_TAG, "Synchronizing visible status");

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
        } else {
            mEmptyProfilesTextView.setVisibility(View.GONE);
            mProfilesScrollView.setVisibility(View.VISIBLE);
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to synchronize visible status");
        }
    }

    @Override
    public void onProfileInfoSubmit(int requestCode, String name, Birthday dateOfBirth) {
        if (requestCode == Request.REQUEST_NEW_PROFILE_INFO) {
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
            mPinnedProfileViewsAdapter.addProfile(0, profile);
        } else {
            mPinnedProfileViewsAdapter.removeProfile(profileId);
            mOtherProfileViewsAdapter.addProfile(0, profile);
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

    private void refreshProfiles() {
        if (!mRefreshProfilesLayout.isRefreshing()) mRefreshProfilesLayout.setRefreshing(true);
        mPinnedProfileViewsAdapter.refresh();
        mOtherProfileViewsAdapter.refresh();

        mRefreshProfilesLayout.setRefreshing(false);
    }
}
