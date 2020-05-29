package thegoodcompany.aetate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import thegoodcompany.aetate.BuildConfig;
import thegoodcompany.aetate.R;
import thegoodcompany.aetate.utilities.Age;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.Error;
import thegoodcompany.aetate.utilities.ProfileViewsAdapter;
import thegoodcompany.aetate.utilities.ProfilesSection;
import thegoodcompany.aetate.utilities.codes.Request;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodcompany.aetate.utilities.tagmanagement.TagManager;

import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_AVATAR_FILE_NAME;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_DAY;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_MONTH;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_NAME;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_YEAR;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_FIRST_PROFILE_INFO;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_NEW_PROFILE_INFO;
import static thegoodcompany.aetate.utilities.tagmanagement.TagManager.NO_TAG;
import static thegoodcompany.aetate.utilities.tagmanagement.TagManager.TAG_PIN;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener,
        ProfileManagerInterface.onProfilePinnedListener, ProfileManagerInterface.onProfileAddedListener,
        ProfileInfoInputDialog.OnProfileInfoSubmitListener, ProfileManagerInterface.onProfileRemovedListener {

    //Change log level to limit logging scopes
    private static final int LOG_LEVEL = BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR;
    public static final boolean LOG_V = LOG_LEVEL <= Log.VERBOSE;
    public static final boolean LOG_D = LOG_LEVEL <= Log.DEBUG;
    public static final boolean LOG_I = LOG_LEVEL <= Log.INFO;
    public static final boolean LOG_W = LOG_LEVEL <= Log.WARN;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private static final String DEFAULT_PREFERENCE = "thegoodcompany.aetate.pref.DEFAULT";
    private static final String HAS_ONBOARDED_KEY = "thegoodcompany.aetate.pref.key.HAS_ONBOARDED";
    private static final String FEEDBACK_EMAIL = "feedback_email";

    private static final int UPCOMING_BIRTHDAY_SECTION_KEY = 1200;

    private static final int MIN_UPCOMING_DURATION_DAYS = 7;

    private RecyclerView mProfilesRecyclerView;
    private TextView mEmptyProfilesTextView;
    private Button mSetupButton;
    private FloatingActionButton mAddProfileFab;
    private SwipeRefreshLayout mRefreshProfilesLayout;

    private ProfileManager mProfileManager;
    private ProfileViewsAdapter mProfileViewsAdapter;
    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Calendar startTime = null;
        if (LOG_D) startTime = Calendar.getInstance();

        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.main_activity_label));
        setContentView(R.layout.activity_main);

        mRemoteConfig.activate();

        mRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12))
                .build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(FEEDBACK_EMAIL, "azraftaohid@outlook.com");
        mRemoteConfig.setDefaultsAsync(defaults);

        mRemoteConfig.fetch().addOnSuccessListener(aVoid -> mRemoteConfig.activate());

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

        mRefreshProfilesLayout.setOnRefreshListener(this::refreshProfiles);

        if (BuildConfig.DEBUG) {
            mAddProfileFab.setOnLongClickListener(v -> {
                generateDummyProfiles(10);
                return false;
            });
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + MainActivity.class.getSimpleName());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshProfiles();
                return true;
            case R.id.menu_send_feedback:
                dispatchFeedbackIntent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void introduceFirstTimeUser() {
        OnboardingDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.onboarding_tag));
    }

    private void dispatchFeedbackIntent() {
        String data = generateFeedbackMessage();

        Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
        feedbackIntent.setData(Uri.parse("mailto:"));

        String sender = mRemoteConfig.getString(FEEDBACK_EMAIL);
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{sender});
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "(Age Calculator) User Feedback");
        feedbackIntent.putExtra(Intent.EXTRA_TEXT, data);

        if (feedbackIntent.resolveActivity(getPackageManager()) != null)
            startActivity(feedbackIntent);
        else {
            Toast.makeText(this, getString(R.string.not_resolved_activity), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log("User has no email client to send bug report");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_PROFILE_INFO && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra(EXTRA_NAME);
                Birthday dob = new Birthday(data.getIntExtra(EXTRA_YEAR, Error.NOT_FOUND.getCode()),
                        data.getIntExtra(EXTRA_MONTH, Error.NOT_FOUND.getCode()),
                        data.getIntExtra(EXTRA_DAY, Error.NOT_FOUND.getCode()));

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
        if (view.getId() == R.id.bt_setup_first_profile) {
            ProfileInfoInputDialog.newInstance(Request.REQUEST_FIRST_PROFILE_INFO).show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
            return;
        }

        ProfileInfoInputDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO).show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private void generateDummyProfiles(int howMany) {
        if (LOG_V) Log.v(LOG_TAG, "Generating " + howMany + " dummy profiles");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < howMany; i++) {
            int odds1 = Math.random() > 0.5 ? 1 : 2;
            int odds2 = Math.random() > 0.5 ? 1 : 2;

            int rand1 = (int) (Math.random() * 40);
            int rand2 = (int) (Math.random() * 12);
            int rand3 = (int) (Math.random() * 28);

            int extension = (int) (Math.random() * (odds1 == 1 ? 100000 : 50000));
            onProfileInfoSubmit(Request.REQUEST_NEW_PROFILE_INFO, null, ("Dummy " + (i + extension)),
                    new Birthday(1975 + (odds2 == 1 ? rand1 : (-rand1)), Math.round(rand2), Math.round(rand3)));
        }

        long requiredTime = System.currentTimeMillis() - startTime;
        if (LOG_D) Log.d(LOG_TAG, "It took " + requiredTime + " milliseconds to generate them");
    }

    private void synchronizeVisibleStatus() {
        Calendar startTime = null;
        if (LOG_D) startTime = Calendar.getInstance();
        if (LOG_V) Log.v(LOG_TAG, "Synchronizing visible status");

        boolean isEmpty = true;

        LinkedHashMap<Integer, ProfilesSection> initSectionMap = generateInitSectionMap();
        LinkedHashMap<Integer, ProfilesSection> sortedSectionMap = generateSectionMap();

        for (int key : initSectionMap.keySet()) {
            if (!sortedSectionMap.containsKey(key)) {
                if (mProfileViewsAdapter.containsSection(key))
                    mProfileViewsAdapter.removeSection(key);

                continue;
            }

            ProfilesSection section = sortedSectionMap.get(key);
            int profileCount = Objects.requireNonNull(section).getProfileCount();
            if (LOG_I)
                Log.i(LOG_TAG, "Profile count (section key: " + key + "; count: " + profileCount + ")");

            if (!mProfileViewsAdapter.containsSection(key))
                mProfileViewsAdapter.addSection(key, section);

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

        if (avatar != null) profile.setAvatar(avatar);

        mProfileManager.addProfile(profile);

        if (requestCode == REQUEST_FIRST_PROFILE_INFO) {
            mProfileManager.pinProfile(profile.getId(), true);
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
        int newPosition = 0;

        if (mProfileManager.getProfileById(profileId).getAge().getDurationBeforeNextBirthday(Age.MODE_DAY)[Age.DAY] <= MIN_UPCOMING_DURATION_DAYS) {
            sectionKey = UPCOMING_BIRTHDAY_SECTION_KEY;
        }

        if (sectionKey == UPCOMING_BIRTHDAY_SECTION_KEY && !isPinned) {
            LinkedHashMap<Integer, ProfilesSection> sectionMap = generateSectionMap();

            ProfilesSection section = sectionMap.get(sectionKey);
            ArrayList<Integer> pinnedIds = TagManager.getTaggedIds(TAG_PIN);

            int totalProfiles = Objects.requireNonNull(section).getProfileCount();
            int totalPinned = 0;
            for (int i = 0; i < totalProfiles; i++) {
                int sectionProfileId = section.getProfile(i).getId();
                if (pinnedIds.contains(sectionProfileId)) {
                    totalPinned++;
                }
            }

            newPosition = totalPinned;
        }

        if (mProfileViewsAdapter.containsSection(sectionKey)) {
            mProfileViewsAdapter.moveProfile(profileId, sectionKey, newPosition);
        } else {
            ProfilesSection section = generateInitSection(sectionKey);
            mProfileViewsAdapter.addSection(sectionKey, section);
            mProfileViewsAdapter.moveProfile(profileId, sectionKey, 0);
        }

        synchronizeVisibleStatus();
        mProfilesRecyclerView.smoothScrollToPosition(mProfileViewsAdapter.getProfilePosition(profileId)
                - (sectionKey == UPCOMING_BIRTHDAY_SECTION_KEY && !isPinned ? 0 : 1));
    }

    @Override
    public void onProfileAdded(Profile profile) {
        int sectionKey = profile.getAge().getDurationBeforeNextBirthday(Age.MODE_DAY)[Age.DAY] <= MIN_UPCOMING_DURATION_DAYS ?
                UPCOMING_BIRTHDAY_SECTION_KEY : NO_TAG;

        if (mProfileViewsAdapter.containsSection(sectionKey))
            mProfileViewsAdapter.addProfile(sectionKey, profile);
        else {
            ProfilesSection section = generateInitSection(sectionKey);
            section.addProfile(profile);
            mProfileViewsAdapter.addSection(sectionKey, section);
        }

        synchronizeVisibleStatus();
        mProfilesRecyclerView.smoothScrollToPosition(mProfileViewsAdapter.getProfilePosition(profile.getId()));
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
        mProfileViewsAdapter.setSectionOrder(new int[]{TAG_PIN, UPCOMING_BIRTHDAY_SECTION_KEY, NO_TAG});
    }

    private ArrayList<Profile> getUpcomingBirthdayProfiles(ArrayList<Profile> source,
                                                           @SuppressWarnings("SameParameterValue") boolean removeFromSource) {
        ArrayList<Profile> upcoming = new ArrayList<>();

        int sourceSize = source.size();
        for (int i = 0; i < sourceSize; i++) {
            if (source.get(i).getAge().getDurationBeforeNextBirthday(Age.MODE_DAY)[Age.DAY] <= MIN_UPCOMING_DURATION_DAYS) {
                upcoming.add(source.get(i));
                if (removeFromSource) {
                    source.remove(i);
                    sourceSize--;
                    i--;
                }
            }

        }

        return upcoming;
    }

    private ProfilesSection generateInitSection(int sectionKey) {
        switch (sectionKey) {
            case TAG_PIN:
                return new ProfilesSection(new ArrayList<>(), getString(R.string.pinned));
            case UPCOMING_BIRTHDAY_SECTION_KEY:
                return new ProfilesSection(new ArrayList<>(), getString(R.string.upcoming_birthday));
            default:
                return new ProfilesSection(new ArrayList<>(), getString(R.string.others));
        }
    }

    private LinkedHashMap<Integer, ProfilesSection> generateInitSectionMap() {
        LinkedHashMap<Integer, ProfilesSection> sectionMap = new LinkedHashMap<>();
        sectionMap.put(TAG_PIN, generateInitSection(TAG_PIN));
        sectionMap.put(UPCOMING_BIRTHDAY_SECTION_KEY, generateInitSection(UPCOMING_BIRTHDAY_SECTION_KEY));
        sectionMap.put(NO_TAG, generateInitSection(NO_TAG));

        return sectionMap;
    }

    private LinkedHashMap<Integer, ProfilesSection> generateSectionMap() {
        ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
        ArrayList<Profile> otherProfiles = mProfileManager.getOtherProfiles();
        ArrayList<Profile> upcomingBdaysProfile = getUpcomingBirthdayProfiles(pinnedProfiles, true);
        upcomingBdaysProfile.addAll(getUpcomingBirthdayProfiles(otherProfiles, true));

        LinkedHashMap<Integer, ProfilesSection> sectionMap = new LinkedHashMap<>();
        if (pinnedProfiles.size() > 0)
            sectionMap.put(TAG_PIN, new ProfilesSection(pinnedProfiles, getString(R.string.pinned)));
        if (upcomingBdaysProfile.size() > 0)
            sectionMap.put(UPCOMING_BIRTHDAY_SECTION_KEY, new ProfilesSection(upcomingBdaysProfile, getString(R.string.upcoming_birthday)));
        if (otherProfiles.size() > 0)
            sectionMap.put(NO_TAG, new ProfilesSection(otherProfiles, getString(R.string.others)));

        return sectionMap;
    }

    private String generateFeedbackMessage() {
        return "\n\nWrite above this line" +
                "\nDevice: " +
                Build.MODEL +
                "\nAPI level: " +
                Build.VERSION.SDK_INT +
                "\nBuild version " +
                BuildConfig.VERSION_CODE;
    }
}
