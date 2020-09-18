package thegoodkid.aetate.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.microsoft.fluentui.listitem.ListItemDivider;
import com.microsoft.fluentui.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import thegoodcompany.common.utils.CalendarUtils;
import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodkid.aetate.BuildConfig;
import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.ActivityHomeBinding;
import thegoodkid.aetate.databinding.ViewWelcomeBinding;
import thegoodkid.aetate.utilities.Avatar;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.CommonUtilities;
import thegoodkid.aetate.utilities.Error;
import thegoodkid.aetate.utilities.Reporter;
import thegoodkid.aetate.utilities.codes.Request;
import thegoodkid.aetate.utilities.list.profile.ProfileListAdapter;
import thegoodkid.aetate.utilities.list.profile.ProfilesGroup;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodkid.aetate.utilities.tagmanagement.Tag;
import thegoodkid.aetate.utilities.tagmanagement.TagManager;

import static thegoodkid.aetate.utilities.CommonUtilities.createTintedDrawable;

public class HomeActivity extends BaseAppActivity
        implements ProfileManagerInterface.OnProfileAddedListener,
        ProfileManagerInterface.OnProfileUpdatedListener,
        ProfileManagerInterface.OnProfileRemovedListener,
        ProfileManagerInterface.OnProfilePinnedListener,
        ProfileInfoInputDialog.OnProfileInfoSubmitListener,
        ProfileListAdapter.StateListener,
        SearchView.OnQueryTextListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();

    private static final String DEFAULT_PREFERENCE = "thegoodkid.aetate.pref.DEFAULT";
    private static final String HAS_ONBOARDED_KEY = "thegoodkid.aetate.pref.key.HAS_ONBOARDED";
    private static final String SEARCH_FRAGMENT_TAG = "thegoodkid.aetate.ui.HomeActivity.tag.FRAGMENT_SEARCH";

    private static final int MIN_UPCOMING_DURATION_DAYS = 7;
    private static final int SEARCH_DELAY_MILLIS = 500;

    private static final String DUMMY_NAME = "dummy_name";
    private static final String DUMMY_YEAR = "dummy_year";
    private static final String DUMMY_MONTH = "dummy_month";
    private static final String DUMMY_DAY = "dummy_day";

    private ActivityHomeBinding binding;
    private ProfileManager mProfileManager;
    private ProfileListAdapter<Group> mAdapter;
    private Handler mQueryHandler;
    private LinkedList<Runnable> mQueries = new LinkedList<>();
    private ViewWelcomeBinding mWelcomeBinding;

    private ProfileManagerInterface.OnProfileAddedInBatchListener mOnBatchAddedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AndroidThreeTen.init(getApplicationContext());
        setupAppbar();
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
            mOnBatchAddedListener = profiles -> {
                if (mAdapter.hasSection(Group.MORE))
                    mAdapter.addProfileInBatch(Group.MORE, profiles);
                else {
                    ProfilesGroup group = createInitSection(Group.MORE);
                    group.addAll(profiles);
                    mAdapter.addSection(Group.MORE, group);
                }

                binding.appBar.setExpanded(false, true);
                binding.profilesContainer.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            };

            mProfileManager.addOnProfileAddedInBatchListener(mOnBatchAddedListener);

            binding.fabAddProfile.setOnLongClickListener(v -> {
                int amount = 1;

                addDummies(amount);
                Snackbar.Companion.make(binding.getRoot(), getResources().getQuantityText(R.plurals.dummies_generated, amount), Snackbar.LENGTH_SHORT, Snackbar.Style.REGULAR)
                        .show();
                return true;
            });
        }
    }

    private void setupAppbar() {
        binding.appBar.getToolbar().setTitle(R.string.home_activity_label);
        setSupportActionBar(binding.appBar.getToolbar());

        mQueryHandler = new Handler(getMainLooper());

        binding.searchbar.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b) {
                FragmentManager manager = getSupportFragmentManager();
                if (manager.findFragmentByTag(SEARCH_FRAGMENT_TAG) != null) return;

                ProfileSearchFragment fragment = ProfileSearchFragment.newInstance();

                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(binding.searchResultFragmentContainer.getId(), fragment, SEARCH_FRAGMENT_TAG);
                transaction.commit();

                binding.fabAddProfile.hide();
            }
        });

        binding.searchbar.setOnQueryTextListener(this);

        binding.searchbar.setOnCloseListener(() -> {
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (fragment != null) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }

            binding.fabAddProfile.show();
            return true;
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        binding.searchbar.setShowSearchProgress(true);
        clearPendingQueries();

        Runnable query = () -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (!(fragment instanceof ProfileSearchFragment)) return;

            ((ProfileSearchFragment) fragment).setQueryText(newText);
            binding.searchbar.setShowSearchProgress(false);
        };

        mQueries.addLast(query);
        mQueryHandler.postDelayed(query, SEARCH_DELAY_MILLIS);

        return true;
    }

    private void clearPendingQueries() {
        int count = mQueries.size();

        for (; count > 0; count--) {
            Runnable r = mQueries.pollFirst();
            if (r != null) mQueryHandler.removeCallbacks(r);
        }
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem settingsItem = menu.findItem(R.id.menu_settings);
        if (settingsItem != null)
            settingsItem.setIcon(createTintedDrawable(this, R.drawable.ic_fluent_settings_24_regular, R.attr.fluentuiToolbarIconColor));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshProfiles();
                return true;
            case R.id.menu_settings:
                Intent openSettings = new Intent(this, SettingsActivity.class);
                startActivity(openSettings);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(SEARCH_FRAGMENT_TAG);
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
            binding.fabAddProfile.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mProfileManager.removeOnProfileAddedListener(this);
        mProfileManager.removeOnProfileUpdatedListener(this);
        mProfileManager.removeOnProfileRemovedListener(this);
        mProfileManager.removeOnProfilePinnedListener(this);
        if (Reporter.LOG_D)
            mProfileManager.removeOnProfileAddedInBatchListener(mOnBatchAddedListener);
    }

    void introduceFirstTimeUser() {
        OnboardingDialog.newInstance().show(getSupportFragmentManager(), getString(R.string.onboarding_tag));
    }

    public void showAddProfileDialog(@Nullable View view) {
        ProfileInfoInputDialog dialog;
        if (view != null && view.getId() == R.id.setup_first_profile)
            dialog = ProfileInfoInputDialog.newInstance(Request.REQUEST_FIRST_PROFILE_INFO);
        else dialog = ProfileInfoInputDialog.newInstance(Request.REQUEST_NEW_PROFILE_INFO);

        dialog.show(getSupportFragmentManager(), getString(R.string.add_profile_dialog_tag));
    }

    @Override
    public void onProfileInfoSubmit(int requestCode, Avatar avatar, @NonNull String name, @NonNull Birthday dateOfBirth) {
        Profile profile = createProfile(avatar, name, dateOfBirth);
        mProfileManager.addProfile(profile);

        if (requestCode == Request.REQUEST_FIRST_PROFILE_INFO) {
            mProfileManager.pinProfile(profile.getId(), true);
        }
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

        binding.appBar.setExpanded(false, true);
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

        Snackbar.Companion.make(binding.getRoot(), getString(R.string.info_profile_removed), Snackbar.LENGTH_LONG, Snackbar.Style.REGULAR)
                .setAction(getString(R.string.undo), view -> {
                    mProfileManager.addProfile(profile);

                    TagManager tagManager = TagManager.getTagManager(this);
                    for (Tag removedTag : removedTags)
                        tagManager.tagProfile(profile.getId(), removedTag);
                })
                .show();
    }

    @Override
    public void onProfileAvatarChanged(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        mAdapter.refreshAvatar(profileId);
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        mAdapter.refreshName(profileId);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        mAdapter.refreshAge(profileId);
    }

    @Override
    public void onProfilePinned(int profileId, boolean isPinned) {
        Profile profile = mProfileManager.getProfileById(profileId);
        if (profile == null) {
            Reporter.reportError(LOG_TAG, Error.createDevMessage(Error.INCONSISTENT_PINNED_ABSENT));
            return;
        }

        Group dest = hasUpcomingBirthday(profile) ?
                Group.UPCOMING : (isPinned ? Group.PINNED : Group.MORE);
        int newPosition = 0;

        if (dest == Group.UPCOMING && !isPinned) {
            ArrayList<Profile> pinnedProfiles = mProfileManager.getPinnedProfiles();
            newPosition = extractProfilesWithUpcomingBirthday(pinnedProfiles, false).size();
        }

        if (!mAdapter.hasSection(dest)) {
            mAdapter.addSection(dest, createInitSection(dest));
        }

        if (mAdapter.moveItem(profile, dest, newPosition)) {
            binding.profilesContainer.scrollToPosition(mAdapter.getProfilePos(profile)
                    - (dest == Group.UPCOMING && isPinned ? 1 : 0));
        } else {
            Log.e(LOG_TAG, "Failed moving profile (destination: " + dest + ")");
        }
    }

    @NotNull
    private Profile createProfile(@Nullable Avatar avatar, String name, Birthday birthday) {
        Profile profile = new Profile(name, birthday);
        profile.setAvatar(avatar);

        return profile;
    }

    @NotNull
    private ArrayList<Profile> extractProfilesWithUpcomingBirthday(@NotNull ArrayList<Profile> source, boolean removeFromSource) {
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

    private boolean hasUpcomingBirthday(@NotNull Profile profile) {
        Calendar c = Calendar.getInstance();
        return CommonUtilities.calculateDaysLeftForBirthday(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
                profile.getBirthday(), CalendarUtils.MODE_DAY)[CalendarUtils.DAY] <= MIN_UPCOMING_DURATION_DAYS;
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

    private void initializeProfileViewsAdapter() {
        mAdapter = new ProfileListAdapter<>(this, createGroupMap());
        mAdapter.setRespectSectionOrder(true);
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
        ArrayList<Profile> upcomingBdaysProfile = extractProfilesWithUpcomingBirthday(pinnedProfiles, true);
        upcomingBdaysProfile.addAll(extractProfilesWithUpcomingBirthday(unpinnedProfiles, true));

        LinkedHashMap<Group, ProfilesGroup> sectionMap = new LinkedHashMap<>();
        if (pinnedProfiles.size() > 0)
            sectionMap.put(Group.PINNED, new ProfilesGroup(new HeaderItem(getString(R.string.pinned)), pinnedProfiles));
        if (upcomingBdaysProfile.size() > 0)
            sectionMap.put(Group.UPCOMING, new ProfilesGroup(new HeaderItem(getString(R.string.upcoming_birthday)), upcomingBdaysProfile));
        if (unpinnedProfiles.size() > 0)
            sectionMap.put(Group.MORE, new ProfilesGroup(new HeaderItem(getString(R.string.more)), unpinnedProfiles));

        return sectionMap;
    }

    private void refreshProfiles() {
        if (!binding.profilesRefresher.isRefreshing())
            binding.profilesRefresher.setRefreshing(true);

        initializeProfileViewsAdapter();
        binding.profilesContainer.swapAdapter(mAdapter, true);

        binding.profilesRefresher.setRefreshing(false);
    }

    private void addDummies(int amount) {
        if (amount == 1) {
            Bundle dummy = createDummy();
            onProfileInfoSubmit(Request.REQUEST_NEW_PROFILE_INFO, null, Objects.requireNonNull(dummy.getString(DUMMY_NAME)),
                    new Birthday(dummy.getInt(DUMMY_YEAR), dummy.getInt(DUMMY_MONTH), dummy.getInt(DUMMY_DAY)));
            return;
        }

        Handler handler = new Handler(getMainLooper());

        Thread t = new Thread(() -> {
            ArrayList<Profile> dummies = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                Bundle dummy = createDummy();
                Profile profile = new Profile(Objects.requireNonNull(dummy.getString(DUMMY_NAME)),
                        new Birthday(dummy.getInt(DUMMY_YEAR), dummy.getInt(DUMMY_MONTH), dummy.getInt(DUMMY_DAY)));

                dummies.add(profile);
            }

            handler.post(() -> mProfileManager.addProfiles(dummies));
        });

        t.start();
    }

    @NonNull
    private Bundle createDummy() {
        int odds1 = Math.random() > 0.5 ? 1 : 2;
        int odds2 = Math.random() > 0.5 ? 1 : 2;

        int rand1 = (int) (Math.random() * 40);
        int rand2 = (int) (Math.random() * 12);
        int rand3 = (int) (Math.random() * 28);

        int extension = (int) (Math.random() * (odds1 == 1 ? 100000 : 50000));

        Bundle bundle = new Bundle();
        bundle.putString(DUMMY_NAME, "Dummy " + extension);
        bundle.putInt(DUMMY_YEAR, 1975 + (odds2 == 1 ? rand1 : (-rand1)));
        bundle.putInt(DUMMY_MONTH, Math.round(rand2));
        bundle.putInt(DUMMY_DAY, Math.round(rand3));

        return bundle;
    }

    private enum Group {
        PINNED, UPCOMING, MORE
    }
}
