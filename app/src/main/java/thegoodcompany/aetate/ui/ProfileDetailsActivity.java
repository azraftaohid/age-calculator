package thegoodcompany.aetate.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.microsoft.fluentui.listitem.ListItemDivider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import thegoodcompany.aetate.BuildConfig;
import thegoodcompany.aetate.R;
import thegoodcompany.aetate.databinding.ActivityProfileDetailsBinding;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.CommonUtilities;
import thegoodcompany.aetate.utilities.DateStringUtils;
import thegoodcompany.aetate.utilities.Error;
import thegoodcompany.aetate.utilities.codes.Extra;
import thegoodcompany.aetate.utilities.list.profiledetails.DetailItem;
import thegoodcompany.aetate.utilities.list.profiledetails.DetailListAdapter;
import thegoodcompany.aetate.utilities.list.profiledetails.DetailSection;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodkid.common.utils.CalendarUtils;
import thegoodkid.common.utils.StringUtils;
import thegoodkid.common.utils.recyclerview.HeaderItem;

import static thegoodcompany.aetate.utilities.Logging.LOG_D;
import static thegoodcompany.aetate.utilities.Logging.LOG_V;

public class ProfileDetailsActivity extends AppCompatActivity implements ProfileManagerInterface.OnProfileUpdatedListener {
    private final static String LOG_TAG = ProfileDetailsActivity.class.getSimpleName();
    private final static String LOG_TAG_PERFORMANCE = LOG_TAG + ".performance";

    private static final int NOT_FOUND = Error.NOT_FOUND.getCode();
    private static final int DEFAULT = Error.DEFAULT.getCode();

    Profile profile;
    private ActivityProfileDetailsBinding binding;
    private DetailListAdapter adapter;

    public ProfileDetailsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = 0;
        if (LOG_D) startTime = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (BuildConfig.DEBUG) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupAppbar();

        int profileId = getIntent().getIntExtra(Extra.EXTRA_PROFILE_ID, NOT_FOUND);
        if (profileId == NOT_FOUND) throw new IllegalStateException("Profile ID not found");
        else if (profileId == DEFAULT) throw new UnknownError("Profile ID is an error code");

        profile = ProfileManager.getInstance(this).getProfileById(profileId);
        setAppbarProfile();

        adapter = new DetailListAdapter(this, binding.getRoot(), createSectionMap());
        binding.container.addItemDecoration(new ListItemDivider(this, DividerItemDecoration.VERTICAL));
        binding.container.setAdapter(adapter);

        binding.modify.setOnClickListener(v -> showModifyDialog());
        binding.detailsRefresher.setOnRefreshListener(this::refresh);
        ProfileManager.getInstance(this).addOnProfileUpdatedListener(this);

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (System.currentTimeMillis() - startTime)
                    + " milliseconds to show " + ProfileDetailsActivity.class.getSimpleName());
        }
    }

    private void setupAppbar() {
        setSupportActionBar(binding.appBar.getToolbar());
        binding.appBar.getToolbar().setTitle(null);
        binding.appBar.getToolbar().setNavigationIcon(CommonUtilities.createNavigationBackDrawable(this));
        binding.appBar.getToolbar().setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setAppbarProfile() {
        Birthday birthday = profile.getBirthday();

        binding.name.setText(profile.getName());
        binding.birthday.setText(DateStringUtils.formatDateAbbrev(this, birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth()));

        Avatar avatar = profile.getAvatar();
        if (avatar != null) binding.avatar.setAvatarImageBitmap(avatar.getBitmap());
        else binding.avatar.setAvatarImageDrawable(getDrawable(R.drawable.img_defaut_avatar));
    }

    @NotNull
    private LinkedHashMap<DetailListAdapter.Section, DetailSection> createSectionMap() {
        LinkedHashMap<DetailListAdapter.Section, DetailSection> sectionMap = new LinkedHashMap<>();
        sectionMap.put(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        sectionMap.put(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));

        return sectionMap;
    }

    @NotNull
    private DetailSection createSection(@NotNull DetailListAdapter.Section section) {
        int[] appendants = CommonUtilities.createCalendarAppendants();

        Calendar c = Calendar.getInstance();
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentMonth = c.get(Calendar.MONTH);
        int currentYear = c.get(Calendar.YEAR);

        switch (section) {
            case BASIC:
                ArrayList<DetailItem> basicItems = new ArrayList<>();

                long[] daysLeftForBirthday = CommonUtilities.calculateDaysLeftForBirthday(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH), profile.getBirthday(), CalendarUtils.MODE_MONTH_DAY);

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

                return new DetailSection(null, basicItems);
            case AGE:
                Birthday birthday = profile.getBirthday();
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
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_yd_24px)));
                ageItems.add(new DetailItem(StringUtils.joinUnless(getResources(), ageMonthDay, appendants, ", ", 0))
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_md_24px)));
                ageItems.add(new DetailItem(ageInDays + getResources().getQuantityString(R.plurals.suffix_day, getQuantity(ageInDays)))
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_day_24px)));
                ageItems.add(new DetailItem(ageInHours + getResources().getQuantityString(R.plurals.suffix_hour, getQuantity(ageInHours)))
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_hour_24px)));
                ageItems.add(new DetailItem(ageInMinutes + getResources().getQuantityString(R.plurals.suffix_minute, getQuantity(ageInMinutes)))
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_min_24px)));
                ageItems.add(new DetailItem(ageInSeconds + getResources().getQuantityString(R.plurals.suffix_second, getQuantity(ageInSeconds)))
                        .setSubtitle(getString(R.string.or))
                        .setCustomView(createCustomView(R.drawable.ic_sec_24px)));

                return new DetailSection(new HeaderItem(getString(R.string.age)), ageItems);
        }

        throw new IllegalStateException("Unhandled section: " + section);
    }

    @NonNull
    private ImageView createCustomView(@DrawableRes int drawable) {
        ImageView view = new ImageView(this);
        view.setImageDrawable(CommonUtilities.getTintedDrawable(this, drawable, R.attr.fluentuiForegroundSecondaryIconColor));
        return view;
    }

    private int getQuantity(long i) {
        return i > Integer.MAX_VALUE || i < Integer.MIN_VALUE ? Integer.MAX_VALUE : (int) i;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ProfileManager.getInstance(this).removeOnProfileUpdatedListener(this);
    }

    private void showModifyDialog() {
        ProfileInfoInputDialog.newInstance(profile)
                .show(getSupportFragmentManager(), getString(R.string.modify_profile_dialog_tag));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_details_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProfileAvatarChanged(int profileId, @Nullable Avatar newAvatar, Avatar previousAvatar) {
        if (newAvatar != null)
            binding.avatar.setAvatarImageDrawable(newAvatar.getCircularDrawable());
        else binding.avatar.setAvatarImageDrawable(null);
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        binding.name.setText(newName);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        binding.birthday.setText(DateStringUtils.formatDateAbbrev(this, newBirthYear, newBirthMonth, newBirthDay));

        adapter.replaceSection(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        adapter.replaceSection(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));
    }

    private void refresh() {
        if (LOG_V) Log.v(LOG_TAG, "Refreshing profile details");
        if (!binding.detailsRefresher.isRefreshing()) binding.detailsRefresher.setRefreshing(true);

        setAppbarProfile();

        adapter.clear();
        adapter.addSection(DetailListAdapter.Section.BASIC, createSection(DetailListAdapter.Section.BASIC));
        adapter.addSection(DetailListAdapter.Section.AGE, createSection(DetailListAdapter.Section.AGE));

        binding.detailsRefresher.setRefreshing(false);
    }
}
