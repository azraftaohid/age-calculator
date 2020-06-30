package thegoodcompany.aetate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.microsoft.fluentui.listitem.ListItemDivider;
import com.microsoft.fluentui.snackbar.Snackbar;
import com.microsoft.fluentui.widget.ProgressBar;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import thegoodcompany.aetate.BuildConfig;
import thegoodcompany.aetate.R;
import thegoodcompany.aetate.databinding.ActivityHomeBinding;
import thegoodcompany.aetate.databinding.ViewWelcomeBinding;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.CommonUtilities;
import thegoodcompany.aetate.utilities.codes.Request;
import thegoodcompany.aetate.utilities.list.profile.ProfileListAdapter;
import thegoodcompany.aetate.utilities.list.profile.ProfilesGroup;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodcompany.aetate.utilities.tagmanagement.Tag;
import thegoodkid.common.utils.CalendarUtils;
import thegoodkid.common.utils.recyclerview.HeaderItem;

import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_FIRST_PROFILE_INFO;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_NEW_PROFILE_INFO;

public class HomeActivity extends AppCompatActivity
        implements ProfileManagerInterface.OnProfileAddedListener,
        ProfileManagerInterface.OnProfileUpdatedListener,
        ProfileManagerInterface.OnProfileRemovedListener,
        ProfileManagerInterface.OnProfilePinnedListener,
        ProfileInfoInputDialog.OnProfileInfoSubmitListener,
        ProfileListAdapter.StateListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private static final String DEFAULT_PREFERENCE = "thegoodcompany.aetate.pref.DEFAULT";
    private static final String HAS_ONBOARDED_KEY = "thegoodcompany.aetate.pref.key.HAS_ONBOARDED";
    private static final String FEEDBACK_EMAIL = "feedback_email";

    private static final int MIN_UPCOMING_DURATION_DAYS = 7;

    private ActivityHomeBinding binding;
    private ProfileManager mProfileManager;
    private ProfileListAdapter<Group> mAdapter;
    private FirebaseRemoteConfig mRemoteConfig = FirebaseRemoteConfig.getInstance();
    private ViewWelcomeBinding mWelcomeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = 0;
        if (LOG_D) startTime = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (BuildConfig.DEBUG) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AndroidThreeTen.init(getApplicationContext());
        setupAppbar();
        initializeRemoteConfig();
        checkFirstTimeUser();

        mProfileManager = ProfileManager.getInstance(this);
        mProfileManager.addOnProfileAddedListener(this);
        mProfileManager.addOnProfilePinnedListener(this);
        mProfileManager.addOnProfileRemovedListener(this);
        mProfileManager.addOnProfileUpdatedListener(this);

        initializeProfileViewsAdapter();
        binding.profilesContainer.setAdapter(mAdapter);
        binding.profilesContainer.addItemDecoration(new ListItemDivider(this, DividerItemDecoration.VERTICAL));
        mAdapter.queryState();

        binding.profilesRefresher.setOnRefreshListener(this::refreshProfiles);

        if (BuildConfig.DEBUG) {
            binding.fabAddProfile.setOnLongClickListener(v -> {
                generateDummyProfiles(1);
                return true;
            });
        }

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (System.currentTimeMillis() - startTime)
                    + " milliseconds to show " + HomeActivity.class.getSimpleName());
        }
    }

    private void setupAppbar() {
        binding.appBar.getToolbar().setTitle(R.string.home_activity_label);
        setSupportActionBar(binding.appBar.getToolbar());
    }

    private void initializeRemoteConfig() {
        mRemoteConfig.activate();
        mRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : TimeUnit.HOURS.toSeconds(12))
                .build());

        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(FEEDBACK_EMAIL, "azraftaohid@outlook.com");
        mRemoteConfig.setDefaultsAsync(defaults);

        mRemoteConfig.fetch().addOnSuccessListener(aVoid -> mRemoteConfig.activate());
    }

    private void checkFirstTimeUser() {
        SharedPreferences pref = getSharedPreferences(DEFAULT_PREFERENCE, MODE_PRIVATE);
        if (!pref.getBoolean(HAS_ONBOARDED_KEY, false)) {
            introduceFirstTimeUser();
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(HAS_ONBOARDED_KEY, true);
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        Drawable feedbackIcon = CommonUtilities.getTintedDrawable(this, R.drawable.ic_fluent_bug_report_24_regular,
                R.attr.fluentuiToolbarIconColor);
        MenuItem feedbackItem = menu.findItem(R.id.action_send_feedback);
        if (feedbackIcon != null) feedbackItem.setIcon(feedbackIcon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshProfiles();
                return true;
            case R.id.action_send_feedback:
                dispatchFeedbackIntent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mProfileManager.removeOnProfileAddedListener(this);
        mProfileManager.removeOnProfileUpdatedListener(this);
        mProfileManager.removeOnProfileRemovedListener(this);
        mProfileManager.removeOnProfilePinnedListener(this);
    }

    void introduceFirstTimeUser() {
        OnboardingDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.onboarding_tag));
    }

    private void dispatchFeedbackIntent() {
        ProgressBar progressBar = new ProgressBar(this, null, 0, R.style.Widget_FluentUI_CircularProgress_Small);
        DrawableCompat.setTint(progressBar.getIndeterminateDrawable(), ContextCompat.getColor(this, R.color.snackbar_drawable));

        Snackbar.Companion.make(binding.getRoot(), getText(R.string.gather_device_info), Snackbar.LENGTH_LONG, Snackbar.Style.REGULAR)
                .setCustomView(progressBar, Snackbar.CustomViewSize.SMALL)
                .show();

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

    public void showAddProfileDialog(@Nullable View view) {
        ProfileInfoInputDialog dialog;
        if (view != null && view.getId() == R.id.setup_first_profile)
            dialog = ProfileInfoInputDialog.newInstance(REQUEST_FIRST_PROFILE_INFO);
        else dialog = ProfileInfoInputDialog.newInstance(REQUEST_NEW_PROFILE_INFO);

        dialog.show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
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

    @Override
    public void onProfileInfoSubmit(int requestCode, Avatar avatar, @NonNull String name, @NonNull Birthday dateOfBirth) {
        Profile profile = createProfile(avatar, name, dateOfBirth);
        mProfileManager.addProfile(profile);

        if (requestCode == REQUEST_FIRST_PROFILE_INFO) {
            mProfileManager.pinProfile(profile.getId(), true);
        }
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        mAdapter.refreshAge(profileId);
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        mAdapter.refreshName(profileId);
    }

    @Override
    public void onProfileAvatarChanged(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        mAdapter.refreshAvatar(profileId);
    }

    @Override
    public void onProfilePinned(int profileId, boolean isPinned) {
        Group dest = hasUpcomingBirthday(mProfileManager.getProfileById(profileId)) ?
                Group.UPCOMING : (isPinned ? Group.PINNED : Group.MORE);
        int newPosition = 0;

        if (dest == Group.UPCOMING && !isPinned) {
            ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
            newPosition = extractProfilesWithUpcomingBirthdays(pinnedProfiles, false).size();
        }

        Profile profile = mProfileManager.getProfileById(profileId);
        if (!mAdapter.hasSection(dest)) {
            mAdapter.addSection(dest, createInitSection(dest));
        }
        mAdapter.moveItem(profile, dest, newPosition);

        binding.profilesContainer.scrollToPosition(mAdapter.getProfilePos(profile)
                - (dest == Group.UPCOMING && isPinned ? 1 : 0));
    }

    @Override
    public void onProfileAdded(@NonNull Profile profile) {
        Group sectionKey = hasUpcomingBirthday(profile) ? Group.UPCOMING : Group.MORE;

        if (mAdapter.hasSection(sectionKey))
            mAdapter.addItem(sectionKey, profile);
        else {
            ProfilesGroup group = createInitSection(sectionKey);
            group.addItem(profile);
            mAdapter.addSection(sectionKey, group);
        }

        binding.profilesContainer.smoothScrollToPosition(mAdapter.getProfilePos(profile));
    }

    @Override
    public void onProfileRemoved(@NonNull Profile profile, List<Tag> removedTags) {
        if (hasUpcomingBirthday(profile)) {
            mAdapter.removeItemFromSection(Group.UPCOMING, profile);
        } else if (removedTags.contains(Tag.PIN)) {
            mAdapter.removeItemFromSection(Group.PINNED, profile);
        } else {
            mAdapter.removeItemFromSection(Group.MORE, profile);
        }
    }

    @NotNull
    private Profile createProfile(@Nullable Avatar avatar, String name, Birthday birthday) {
        Profile profile = new Profile(name, birthday);
        profile.setAvatar(avatar);

        return profile;
    }

    private void refreshProfiles() {
        if (!binding.profilesRefresher.isRefreshing())
            binding.profilesRefresher.setRefreshing(true);

        initializeProfileViewsAdapter();
        binding.profilesContainer.setAdapter(mAdapter);

        binding.profilesRefresher.setRefreshing(false);
    }

    private boolean hasUpcomingBirthday(@NotNull Profile profile) {
        Calendar c = Calendar.getInstance();
        return CommonUtilities.calculateDaysLeftForBirthday(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                profile.getBirthday(), CalendarUtils.MODE_DAY)[CalendarUtils.DAY] <= MIN_UPCOMING_DURATION_DAYS;
    }

    private void initializeProfileViewsAdapter() {
        mAdapter = new ProfileListAdapter<>(this, createGroupMap());
        mAdapter.setRespectSectionOrder(true);
    }

    @NotNull
    private ArrayList<Profile> extractProfilesWithUpcomingBirthdays(@NotNull ArrayList<Profile> source, boolean removeFromSource) {
        ArrayList<Profile> upcoming = new ArrayList<>();

        int sourceSize = source.size();
        for (int i = 0; i < sourceSize; i++) {
            if (hasUpcomingBirthday(source.get(i))) {
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

    @NonNull
    @Contract("_ -> new")
    private ProfilesGroup createInitSection(@NotNull Group group) {
        switch (group) {
            case PINNED:
                return new ProfilesGroup(new HeaderItem(getString(R.string.pinned)), new ArrayList<>());
            case UPCOMING:
                return new ProfilesGroup(new HeaderItem(getString(R.string.upcoming_birthday)), new ArrayList<>());
            case MORE:
                return new ProfilesGroup(new HeaderItem(getString(R.string.more)), new ArrayList<>());
        }

        throw new IllegalArgumentException("Unsupported group: " + group);
    }

    @NotNull
    private LinkedHashMap<Group, ProfilesGroup> createGroupMap() {
        ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
        ArrayList<Profile> unpinnedProfiles = mProfileManager.getUnpinnedProfiles();
        ArrayList<Profile> upcomingBdaysProfile = extractProfilesWithUpcomingBirthdays(pinnedProfiles, true);
        upcomingBdaysProfile.addAll(extractProfilesWithUpcomingBirthdays(unpinnedProfiles, true));

        LinkedHashMap<Group, ProfilesGroup> sectionMap = new LinkedHashMap<>();
        if (pinnedProfiles.size() > 0)
            sectionMap.put(Group.PINNED, new ProfilesGroup(new HeaderItem(getString(R.string.pinned)), pinnedProfiles));
        if (upcomingBdaysProfile.size() > 0)
            sectionMap.put(Group.UPCOMING, new ProfilesGroup(new HeaderItem(getString(R.string.upcoming_birthday)), upcomingBdaysProfile));
        if (unpinnedProfiles.size() > 0)
            sectionMap.put(Group.MORE, new ProfilesGroup(new HeaderItem(getString(R.string.more)), unpinnedProfiles));

        return sectionMap;
    }

    @NotNull
    @Contract(pure = true)
    private String generateFeedbackMessage() {
        return "\n\nWrite above this line" +
                "\nDevice: " +
                Build.MODEL +
                "\nAPI level: " +
                Build.VERSION.SDK_INT +
                "\nBuild version " +
                BuildConfig.VERSION_CODE;
    }

    @Override
    public void onFirstProfileAdded() {
        if (mWelcomeBinding != null) {
            mWelcomeBinding.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onGroupEmptied(Enum<? extends Enum<?>> group) {
        mAdapter.removeSection((Group) group);
    }

    @Override
    public void onListEmptied() {
        if (binding.welcomeView.getParent() == null) {
            mWelcomeBinding.getRoot().setVisibility(View.VISIBLE);
            return;
        }

        mWelcomeBinding = ViewWelcomeBinding.bind(binding.welcomeView.inflate());
        mWelcomeBinding.setupFirstProfile.setOnClickListener(this::showAddProfileDialog);
    }

    private enum Group {
        PINNED, UPCOMING, MORE
    }
}
