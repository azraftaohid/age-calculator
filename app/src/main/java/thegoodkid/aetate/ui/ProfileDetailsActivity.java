package thegoodkid.aetate.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.microsoft.fluentui.listitem.ListItemDivider;
import com.microsoft.fluentui.listitem.ListItemView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import thegoodcompany.common.utils.CalendarUtils;
import thegoodcompany.common.utils.StringUtils;
import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodkid.aetate.BuildConfig;
import thegoodkid.aetate.R;
import thegoodkid.aetate.databinding.ActivityProfileDetailsBinding;
import thegoodkid.aetate.utilities.AgeReminderEventReceiver;
import thegoodkid.aetate.utilities.Avatar;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.CommonUtilities;
import thegoodkid.aetate.utilities.DateStringUtils;
import thegoodkid.aetate.utilities.Error;
import thegoodkid.aetate.utilities.Reporter;
import thegoodkid.aetate.utilities.list.profiledetails.DetailItem;
import thegoodkid.aetate.utilities.list.profiledetails.DetailListAdapter;
import thegoodkid.aetate.utilities.list.profiledetails.DetailSection;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodkid.aetate.utilities.tagmanagement.Tag;

import static thegoodkid.aetate.utilities.codes.Extra.EXTRA_PROFILE_ID;

public class ProfileDetailsActivity extends BaseAppActivity implements
        ProfileManagerInterface.OnProfileUpdatedListener,
        ProfileManagerInterface.OnProfileRemovedListener,
        ReminderDialog.OnReminderSetListener {
    private final static String LOG_TAG = ProfileDetailsActivity.class.getSimpleName();
    private static final String TAG_REMINDER_SET = "reminder_set_dialog";

    private static final int NOT_FOUND = Error.NOT_FOUND.getCode();
    private static final int DEFAULT = Error.DEFAULT.getCode();

    private ActivityProfileDetailsBinding binding;
    private DetailListAdapter mAdapter;
    private ProfileManager mProfileManager;
    @Nullable
    private Profile mProfile;

    public ProfileDetailsActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupAppbar();

        mProfileManager = ProfileManager.getInstance(this);
        init(getIntent());

        mAdapter = new DetailListAdapter(this, binding.getRoot(), createSectionMap());
        binding.container.addItemDecoration(new ListItemDivider(this, DividerItemDecoration.VERTICAL));
        binding.container.setAdapter(mAdapter);

        mProfileManager.addOnProfileUpdatedListener(this);
        mProfileManager.addOnProfileRemovedListener(this);
        binding.modify.setOnClickListener(v -> showModifyDialog());
        binding.detailsRefresher.setOnRefreshListener(this::refresh);

        Drawable deleteDrawable = CommonUtilities.mutateAndTintDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_fluent_delete_24_regular),
                ContextCompat.getColor(this, R.color.delete_item_foreground_tint));
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageDrawable(deleteDrawable);

        binding.delete.setCustomView(deleteIcon);

        if (BuildConfig.DEBUG) {
            ListItemView mockNotificationView = new ListItemView(this);
            mockNotificationView.setTitle("Mock Notification");
            mockNotificationView.setCustomViewSize(ListItemView.CustomViewSize.SMALL);
            mockNotificationView.setLayoutParams(binding.delete.getLayoutParams());

            mockNotificationView.setCustomView(CommonUtilities.createCustomView(this, R.drawable.ic_fluent_service_bell_24_regular));
            mockNotificationView.setOnClickListener(view -> onReminderSet(System.currentTimeMillis() + 1000));
            mockNotificationView.setOnLongClickListener(view -> {
                onReminderSet(System.currentTimeMillis() + 5000);
                return true;
            });

            binding.delete.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            binding.baseContainer.addView(mockNotificationView, binding.baseContainer.getChildCount() - 1);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        init(intent);
        if (mAdapter.hasSection(DetailListAdapter.Section.BASIC))
            mAdapter.replaceSectionItems(DetailListAdapter.Section.BASIC, createBasicItems());
        else
            mAdapter.addSection(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        if (mAdapter.hasSection(DetailListAdapter.Section.AGE))
            mAdapter.replaceSectionItems(DetailListAdapter.Section.AGE, createAgeItems());
        else
            mAdapter.addSection(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));
    }

    private void init(@NonNull Intent intent) {
        int profileId = intent.getIntExtra(EXTRA_PROFILE_ID, NOT_FOUND);
        if (profileId == NOT_FOUND || profileId == DEFAULT)
            Reporter.reportError(LOG_TAG, "Either profile ID not found or is an error code");

        mProfile = mProfileManager.getProfileById(profileId);

        if (mProfile == null) binding.delete.setVisibility(View.GONE);
        binding.delete.setOnClickListener(view -> {
            if (mProfile == null) finish();
            mProfileManager.removeProfile(mProfile.getId());
        });

        setAppbarProfile();
    }

    private void setupAppbar() {
        setSupportActionBar(binding.appBar.getToolbar());
        binding.appBar.getToolbar().setTitle(null);
        binding.appBar.getToolbar().setNavigationIcon(CommonUtilities.createNavigationBackDrawable(this));
        binding.appBar.getToolbar().setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setAppbarProfile() {
        if (mProfile == null) return;
        Birthday birthday = mProfile.getBirthday();

        binding.name.setText(mProfile.getName());
        binding.birthday.setText(DateStringUtils.formatDateAbbrev(this, birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth()));

        Avatar avatar = mProfile.getAvatar();
        if (avatar != null) binding.avatar.setAvatarImageBitmap(avatar.getBitmap());
        else
            binding.avatar.setAvatarImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_defaut_avatar));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_details_activity, menu);

        if (BuildConfig.DEBUG) {
            MenuItem reminderItem = menu.findItem(R.id.action_set_reminder);
            Drawable reminderIcon = CommonUtilities.createTintedDrawable(this, R.drawable.ic_fluent_channel_notifications_24_regular, R.attr.fluentuiToolbarIconColor);

            reminderItem.setIcon(reminderIcon);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_set_reminder:
                if (mProfile == null) return true;
                ReminderDialog dialog = ReminderDialog.newInstance(mProfile.getBirthday());
                dialog.show(getSupportFragmentManager(), TAG_REMINDER_SET);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProfileManager profileManager = ProfileManager.getInstance(this);
        profileManager.removeOnProfileUpdatedListener(this);
        profileManager.removeOnProfileRemovedListener(this);
    }

    @NonNull
    private LinkedHashMap<DetailListAdapter.Section, DetailSection> createSectionMap() {
        if (mProfile == null) return new LinkedHashMap<>();

        LinkedHashMap<DetailListAdapter.Section, DetailSection> sectionMap = new LinkedHashMap<>();
        sectionMap.put(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        sectionMap.put(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));

        return sectionMap;
    }

    @NonNull
    private DetailSection createSection(@NonNull DetailListAdapter.Section section) {
        switch (section) {
            case BASIC:
                return new DetailSection(null, createBasicItems());
            case AGE:
                return new DetailSection(new HeaderItem(getString(R.string.age)), createAgeItems());
        }

        throw new IllegalStateException("Unhandled section: " + section);
    }

    @NonNull
    private ArrayList<DetailItem> createBasicItems() {
        if (mProfile == null) return new ArrayList<>();

        int[] appendants = CommonUtilities.createCalendarAppendants();
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);

        ArrayList<DetailItem> basicItems = new ArrayList<>();

        long[] daysLeftForBirthday = CommonUtilities.calculateDaysLeftForBirthday(currentYear, currentMonth,
                currentDay, mProfile.getBirthday(), CalendarUtils.MODE_MONTH_DAY);

        String title = StringUtils.joinUnless(getResources(), daysLeftForBirthday, appendants, ", ", 0);
        String subtitle;

        if (title.length() == 0) {
            title = getString(R.string.birthday_today);
            subtitle = getString(R.string.subtext_on_birthday);
        } else {
            subtitle = getString(R.string.left_days_birthday);
        }

        basicItems.add(new DetailItem(title)
                .setSubtitle(subtitle)
                .setCustomView(CommonUtilities.createCustomView(this, R.drawable.ic_cake_24dp, R.attr.fluentuiForegroundSecondaryIconColor)));

        return basicItems;
    }

    @NonNull
    private ArrayList<DetailItem> createAgeItems() {
        if (mProfile == null) return new ArrayList<>();

        int[] appendants = CommonUtilities.createCalendarAppendants();
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);

        Birthday birthday = mProfile.getBirthday();
        int birthDayOfMonth = birthday.getDayOfMonth();
        int birthMonth = birthday.getMonthValue();
        int birthYear = birthday.getYear();

        ArrayList<DetailItem> ageItems = new ArrayList<>();
        long[] ageYearMonthDay = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_YEAR_MONTH_DAY);
        long[] ageYearDay = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_YEAR_DAY);
        long[] ageMonthDay = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_MONTH_DAY);
        long ageInDays = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_DAY)[CalendarUtils.DAY];
        long ageInHours = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_HOUR)[CalendarUtils.HOUR];
        long ageInMinutes = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_MINUTE)[CalendarUtils.MINUTE];
        long ageInSeconds = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthYear, birthMonth, birthDayOfMonth, CalendarUtils.MODE_SECOND)[CalendarUtils.SECOND];

        ageItems.add(new DetailItem(StringUtils.joinUnless(getResources(), ageYearMonthDay, appendants, ", ", 0))
                .setCustomView(createCustomView(R.drawable.ic_ymd_24px)));
        ageItems.add(new DetailItem(StringUtils.joinUnless(getResources(), ageYearDay, appendants, ", ", 0))
                .setCustomView(createCustomView(R.drawable.ic_yd_24px)));
        ageItems.add(new DetailItem(StringUtils.joinUnless(getResources(), ageMonthDay, appendants, ", ", 0))
                .setCustomView(createCustomView(R.drawable.ic_md_24px)));
        ageItems.add(new DetailItem(ageInDays + getResources().getQuantityString(R.plurals.suffix_day, getQuantity(ageInDays)))
                .setCustomView(createCustomView(R.drawable.ic_day_24px)));
        ageItems.add(new DetailItem(ageInHours + getResources().getQuantityString(R.plurals.suffix_hour, getQuantity(ageInHours)))
                .setCustomView(createCustomView(R.drawable.ic_hour_24px)));
        ageItems.add(new DetailItem(ageInMinutes + getResources().getQuantityString(R.plurals.suffix_minute, getQuantity(ageInMinutes)))
                .setCustomView(createCustomView(R.drawable.ic_min_24px)));
        ageItems.add(new DetailItem(ageInSeconds + getResources().getQuantityString(R.plurals.suffix_second, getQuantity(ageInSeconds)))
                .setCustomView(createCustomView(R.drawable.ic_sec_24px)));

        return ageItems;
    }

    @NonNull
    private ImageView createCustomView(@DrawableRes int drawable) {
        ImageView view = new ImageView(this);
        view.setImageDrawable(CommonUtilities.createTintedDrawable(this, drawable, R.attr.fluentuiForegroundSecondaryIconColor));
        return view;
    }

    private int getQuantity(long i) {
        return i > Integer.MAX_VALUE || i < Integer.MIN_VALUE ? Integer.MAX_VALUE : (int) i;
    }

    private void showModifyDialog() {
        if (mProfile == null) return;
        ProfileInfoInputDialog.modifyProfile(mProfile)
                .show(getSupportFragmentManager(), getString(R.string.modify_profile_dialog_tag));
    }

    @Override
    public void onProfileAvatarChanged(int profileId, @Nullable Avatar newAvatar, Avatar previousAvatar) {
        if (newAvatar == null) binding.avatar.setAvatarImageDrawable(null);
        else binding.avatar.setAvatarImageDrawable(newAvatar.getCircularDrawable());
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        binding.name.setText(newName);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        binding.birthday.setText(DateStringUtils.formatDateAbbrev(this, newBirthYear, newBirthMonth, newBirthDay));

        mAdapter.replaceSection(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        mAdapter.replaceSection(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));
    }

    private void refresh() {
        if (Reporter.LOG_V) Log.v(LOG_TAG, "Refreshing profile details");
        if (!binding.detailsRefresher.isRefreshing()) binding.detailsRefresher.setRefreshing(true);

        setAppbarProfile();

        mAdapter.replaceSection(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        mAdapter.replaceSection(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));

        binding.detailsRefresher.setRefreshing(false);
    }

    @Override
    public void onProfileRemoved(@NonNull Profile profile, List<Tag> removedTags) {
        if (mProfile != null && profile.getId() == this.mProfile.getId()) finish();
    }

    @Override
    public void onReminderSet(long atMillis) {
        if (mProfile == null) return;
        Reporter.reportVerbose(LOG_TAG, "Request to get reminded received");

        Intent notifyIntent = new Intent(this, AgeReminderEventReceiver.class);
        notifyIntent.putExtra(EXTRA_PROFILE_ID, mProfile.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mProfile.getId(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Reporter.reportDebug(LOG_TAG, "Setting broadcast receiver on: " + atMillis);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent);
    }
}
