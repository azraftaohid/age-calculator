package thegoodcompany.aetate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.ProfileViewsAdapter;
import thegoodcompany.aetate.utilities.ProfilesSection;
import thegoodcompany.aetate.utilities.codes.Request;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;

import static thegoodcompany.aetate.utilities.codes.Error.NOT_FOUND;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_AVATAR_FILE_NAME;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_DAY;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_MONTH;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_NAME;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_YEAR;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_NEW_PROFILE_INFO;
import static thegoodcompany.aetate.utilities.tagmanagement.TagManager.NO_TAG;
import static thegoodcompany.aetate.utilities.tagmanagement.TagManager.TAG_PIN;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener,
        ProfileManagerInterface.onProfilePinnedListener, ProfileManagerInterface.onProfileAddedListener,
        ProfileInfoInputDialog.OnProfileInfoSubmitListener, ProfileManagerInterface.onProfileRemovedListener {

    //Change log level to limit logging scopes
    private static final int LOG_LEVEL = Log.WARN;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_V = LOG_LEVEL <= Log.DEBUG;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_D = LOG_LEVEL <= Log.DEBUG;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_I = LOG_LEVEL <= Log.INFO;
    @SuppressWarnings("ConstantConditions")
    public static final boolean LOG_W = LOG_LEVEL <= Log.WARN;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private static final String DEFAULT_PREFERENCE = "thegoodcompany.aetate.pref.DEFAULT";
    private static final String HAS_ONBOARDED_KEY = "thegoodcompany.aetate.pref.key.HAS_ONBOARDED";

    private RecyclerView mProfilesRecyclerView;
    private TextView mEmptyProfilesTextView;
    private Button mSetupButton;
    private FloatingActionButton mAddProfileFab;

    private ProfileManager mProfileManager;
    private ProfileViewsAdapter mProfileViewsAdapter;
    private SwipeRefreshLayout mRefreshProfilesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(DEFAULT_PREFERENCE, MODE_PRIVATE);
        if (!pref.getBoolean(HAS_ONBOARDED_KEY, false)) {
            introduceFirstTimeUser();
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(HAS_ONBOARDED_KEY, true);
            editor.apply();
        }

        mProfilesRecyclerView = findViewById(R.id.rv_profiles);
        mEmptyProfilesTextView = findViewById(R.id.tv_empty_profiles);
        mSetupButton = findViewById(R.id.bt_setup_first_profile);
        mRefreshProfilesLayout = findViewById(R.id.srl_refresh_profiles);
        mAddProfileFab = findViewById(R.id.fab_add_profile);

        mProfileManager = ProfileManager.getProfileManager(this);

        initializeProfileViewsAdapter(false);
        mProfilesRecyclerView.setAdapter(mProfileViewsAdapter);

        synchronizeVisibleStatus();

        mRefreshProfilesLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProfiles();
            }
        });

//        generateDummyProfiles(125);

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

    void introduceFirstTimeUser() {
        OnboardingDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.onboarding_tag));
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

                Avatar avatar = null;
                if (data.hasExtra(EXTRA_AVATAR_FILE_NAME)) {
                    avatar = Avatar.retrieveAvatar(this, data.getStringExtra(EXTRA_AVATAR_FILE_NAME));
                }

                Profile profile = new Profile(name, dob, new ProfileManagerInterface.onProfileUpdatedListener() {
                    @Override
                    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                        mProfileManager.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
                    }

                    @Override
                    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
                        mProfileManager.onProfileNameUpdated(profileId, newName, previousName);
                    }

                    @Override
                    public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
                        mProfileManager.onProfileAvatarUpdated(profileId, newAvatar, previousAvatar);
                    }
                });

                if (avatar != null) profile.setAvatar(avatar);

                mProfileManager.addProfile(profile);
                mProfileManager.pinProfile(profile.getId(), true);
            }
        }
    }

    public void showAddProfileDialog(View view) {
        ProfileInfoInputDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO).show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private void generateDummyProfiles(int howMany) {
        if (LOG_V) Log.v(LOG_TAG, "Generating " + howMany + " dummy profiles");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < howMany; i++) {
            onProfileInfoSubmit(Request.REQUEST_NEW_PROFILE_INFO, null, ("Dummy " + (i + 100)), new Birthday(1920, 5, 24));
        }

        long requiredTime = System.currentTimeMillis() - startTime;
        if (LOG_D) Log.d(LOG_TAG, "It took " + requiredTime + " milliseconds to generate them");
    }

    private void synchronizeVisibleStatus() {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();
        if (LOG_V) Log.v(LOG_TAG, "Synchronizing visible status");

        boolean isEmpty = true;

        ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
        ArrayList<Profile> otherProfiles = mProfileManager.getOtherProfiles();

        int pinnedItemCounts = pinnedProfiles.size();
        if (LOG_V) Log.v(LOG_TAG, "Pinned items: " + pinnedItemCounts);
        if (pinnedItemCounts == 0) {
            if (mProfileViewsAdapter.containsSection(TAG_PIN))
                mProfileViewsAdapter.removeSection(TAG_PIN);
        }
        else {
            if (!mProfileViewsAdapter.containsSection(TAG_PIN))
                mProfileViewsAdapter.addSection(TAG_PIN,
                        new ProfilesSection(pinnedProfiles, getString(R.string.pinned)));
            isEmpty = false;
        }

        int otherItemCounts = otherProfiles.size();
        if (LOG_V) Log.v(LOG_TAG, "Other items: " + otherItemCounts);
        if (otherItemCounts == 0) {
            if (mProfileViewsAdapter.containsSection(NO_TAG))
                mProfileViewsAdapter.removeSection(NO_TAG);
        }
        else {
            if (!mProfileViewsAdapter.containsSection(NO_TAG))
                mProfileViewsAdapter.addSection(NO_TAG, new ProfilesSection(otherProfiles, getString(R.string.others)));
            isEmpty = false;
        }

        if (isEmpty) {
            mProfilesRecyclerView.setVisibility(View.GONE);
            mAddProfileFab.hide();
            mEmptyProfilesTextView.setVisibility(View.VISIBLE);
            mSetupButton.setVisibility(View.VISIBLE);
        } else {
            mEmptyProfilesTextView.setVisibility(View.GONE);
            mSetupButton.setVisibility(View.GONE);
            mProfilesRecyclerView.setVisibility(View.VISIBLE);
            mAddProfileFab.show();
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to synchronize visible status");
        }
    }

    @Override
    public void onProfileInfoSubmit(int requestCode, Avatar avatar, String name, Birthday dateOfBirth) {
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

                @Override
                public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
                    mProfileManager.onProfileAvatarUpdated(profileId, newAvatar, previousAvatar);
                }
            });
            profile.setAvatar(avatar);

            mProfileManager.addProfile(profile);
        }
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        mProfileViewsAdapter.onProfileDateOfBirthUpdated(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
    }

    @Override
    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
        mProfileViewsAdapter.onProfileNameUpdated(profileId, newName, previousName);
    }

    @Override
    public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        mProfileViewsAdapter.onProfileAvatarUpdated(profileId, newAvatar, previousAvatar);
    }

    @Override
    public void onProfilePinned(int profileId, boolean isPinned) {
        int sectionKey = isPinned ? TAG_PIN : NO_TAG;

        if (mProfileViewsAdapter.containsSection(sectionKey)) {
            mProfileViewsAdapter.moveProfile(profileId, sectionKey, 0);
        } else {
            ProfilesSection section;
            if (isPinned)
                section = new ProfilesSection(new ArrayList<Profile>(), getString(R.string.pinned));
            else
                section = new ProfilesSection(new ArrayList<Profile>(), getString(R.string.others));

            mProfileViewsAdapter.addSection(sectionKey, section);
            mProfileViewsAdapter.moveProfile(profileId, sectionKey, 0);
        }

        synchronizeVisibleStatus();
    }

    @Override
    public void onProfileAdded(Profile profile) {
        if (mProfileViewsAdapter.containsSection(NO_TAG))
            mProfileViewsAdapter.addProfile(NO_TAG, profile);
        else
            mProfileViewsAdapter.addSection(NO_TAG, new ProfilesSection(mProfileManager.getOtherProfiles(),
                    getString(R.string.others)));

        synchronizeVisibleStatus();
        mProfilesRecyclerView.smoothScrollToPosition(mProfileViewsAdapter.getItemCount() - 1);
    }

    @Override
    public void onProfileRemoved(int profileId) {
        mProfileViewsAdapter.removeProfile(profileId);
        synchronizeVisibleStatus();
    }

    private void refreshProfiles() {
        if (!mRefreshProfilesLayout.isRefreshing()) mRefreshProfilesLayout.setRefreshing(true);

        initializeProfileViewsAdapter(true);
        mProfilesRecyclerView.setAdapter(mProfileViewsAdapter);

        mRefreshProfilesLayout.setRefreshing(false);
    }

    private void initializeProfileViewsAdapter(boolean fromScratch) {
        if (fromScratch) mProfileManager = ProfileManager.getProfileManager(this);

        mProfileViewsAdapter = new ProfileViewsAdapter(this, mProfileManager, generateSectionMap());
        mProfileViewsAdapter.setSectionOrder(new int[]{TAG_PIN, NO_TAG});
    }

    private HashMap<Integer, ProfilesSection> generateSectionMap() {
        ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
        ArrayList<Profile> otherProfiles = mProfileManager.getOtherProfiles();

        HashMap<Integer, ProfilesSection> sectionMap = new HashMap<>(2);
        if (pinnedProfiles.size() > 0)
            sectionMap.put(TAG_PIN, new ProfilesSection(mProfileManager.getPinnedProfiles(), getString(R.string.pinned)));
        if (otherProfiles.size() > 0)
            sectionMap.put(NO_TAG, new ProfilesSection(mProfileManager.getOtherProfiles(), getString(R.string.others)));

        return sectionMap;
    }
}
