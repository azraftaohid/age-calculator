package com.coolninja.agecalculator.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coolninja.agecalculator.utilities.AddProfileDialog;
import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.utilities.Age;
import com.coolninja.agecalculator.utilities.Birthday;
import com.coolninja.agecalculator.utilities.ChangeDateOfBirthDialog;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.RenameDialog;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.microsoft.officeuifabric.persona.PersonaView;
import com.microsoft.officeuifabric.popupmenu.PopupMenu;
import com.microsoft.officeuifabric.popupmenu.PopupMenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface.onProfileUpdatedListener,
        ProfileManagerInterface.onProfilePinnedListener, ProfileManagerInterface.onProfileAddedListener,
        AddProfileDialog.OnProfileSubmissionListener, ProfileManagerInterface.onProfileRemovedListener {
    public static final String EXTRA_NAME = "com.coolninja.agecalculator.extra.NAME";
    public static final String EXTRA_YEAR = "com.coolninja.agecalculator.extra.YEAR";
    public static final String EXTRA_MONTH = "com.coolninja.agecalculator.extra.MONTH";
    public static final String EXTRA_DAY = "com.coolninja.agecalculator.extra.DAY";
    public static final String EXTRA_PROFILE_ID = "com.coolninja.agecalculator.extra.PROFILE_ID";

    public static final int LOG_LEVEL = Log.VERBOSE;
    public static final boolean LOG_V = LOG_LEVEL <= Log.DEBUG;
    public static final boolean LOG_D = LOG_LEVEL <= Log.DEBUG;
    public static final boolean LOG_I = LOG_LEVEL <= Log.INFO;
    public static final boolean LOG_W = LOG_LEVEL <= Log.WARN;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String LOG_TAG_PERFORMANCE = MainActivity.class.getSimpleName() + ".Performance";

    private LinearLayout mPinnedProfilesListView;
    private LinearLayout mOtherProfilesListView;
    private LinearLayout mPinnedListView;
    private LinearLayout mOthersListView;
    private ScrollView mProfilesScrollView;
    private TextView mEmptyProfilesTextView;

    private ProfileManager mProfileManager;

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
        mPinnedProfilesListView = findViewById(R.id.ll_pinned_profiles);
        mOtherProfilesListView = findViewById(R.id.ll_other_profiles);
        mProfilesScrollView = findViewById(R.id.sv_profiles);
        mEmptyProfilesTextView = findViewById(R.id.tv_empty_profiles);

        mProfileManager = ProfileManager.getProfileManager(this);

//        showNumberOfProfiles();

//        generateDummyProfiles(150);

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to show " + MainActivity.class.getSimpleName());
        }

    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, DOB_REQUEST);
    }

    public void launchProfileDetailsActivity(View view) {
        Intent showDetailsIntent = new Intent(this, ProfileDetailsActivity.class);
        showDetailsIntent.putExtra(EXTRA_PROFILE_ID, view.getId());
        startActivity(showDetailsIntent);
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
                    public void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                        mProfileManager.onProfileDateOfBirthChanged(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
                    }

                    @Override
                    public void onProfileNameChanged(int profileId, String newName, String previousName) {
                        mProfileManager.onProfileNameChanged(profileId, newName, previousName);
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

    private void reRegisterProfileViews() {
        mPinnedProfilesListView.removeAllViews();
        mOtherProfilesListView.removeAllViews();

        for (Profile profile : mProfileManager.getProfiles()) {
            PersonaView personaView = generateProfileView(profile);

            if (ProfileManager.isPinned(profile.getId()))
                mPinnedProfilesListView.addView(personaView);
            else
                mOtherProfilesListView.addView(personaView);
        }

        synchronizeVisibleStatus();
    }

    private PersonaView generateProfileView(Profile profile) {
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        if (LOG_V) Log.v(LOG_TAG, "Getting an instance of persona view");
        final PersonaView personaView = new PersonaView(this);

        if (LOG_V) Log.v(LOG_TAG, "Getting age from given profile");
        long[] age = profile.getAge().get(Age.MODE_YEAR_MONTH_DAY);

        if (LOG_V) Log.v(LOG_TAG, "Setting attributes for persona view");
        personaView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        personaView.setName(profile.getName());
        personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_years_months_days),
                age[Age.YEAR], age[Age.MONTH], age[Age.DAY]));
        personaView.setCustomAccessoryView(getCustomAccessoryView(getDrawable(R.drawable.ic_more_vertical)));
        personaView.setLongClickable(true);
        personaView.setId(profile.getId());

        if (LOG_V) Log.v(LOG_TAG, "Setting on click listener for persona view");
        Objects.requireNonNull(personaView.getCustomAccessoryView()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(personaView);
            }
        });

        personaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchProfileDetailsActivity(personaView);
            }
        });

        personaView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMoreOptions(personaView);
                return true;
            }
        });

        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                    + " milliseconds to generate a profile view");
        }
        return personaView;
    }

    private void showMoreOptions(final PersonaView personaView) {
        assert personaView.getCustomAccessoryView() != null : "PersonaView with ID " + personaView.getId() + " must have a custom accessory view";

        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        ArrayList<PopupMenuItem> popupMenuItems = new ArrayList<>();
        final int id = personaView.getId();

        if (ProfileManager.isPinned(id)) popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_unpin, getString(R.string.unpin)));
        else popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_pin, getString(R.string.pin)));
        popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_rename, getString(R.string.rename)));
        popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_change_dob, getString(R.string.change_date_of_birth)));
        popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_delete, getString(R.string.delete)));

        final Profile profile = mProfileManager.getProfileById(personaView.getId());

        final ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener = new ProfileManagerInterface.onProfileUpdatedListener() {
            @Override
            public void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                profile.setDateOfBirth(newBirthYear, newBirthMonth, newBirthDay);
            }

            @Override
            public void onProfileNameChanged(int profileId, String newName, String previousName) {
                profile.setName(newName);
            }
        };

        final PopupMenu popupMenu = new PopupMenu(this, personaView.getCustomAccessoryView(), popupMenuItems, PopupMenu.ItemCheckableBehavior.NONE);
        popupMenu.setOnItemClickListener(new PopupMenuItem.OnClickListener() {
            @Override
            public void onPopupMenuItemClicked(@NotNull PopupMenuItem popupMenuItem) {
                if (popupMenuItem.getId() == R.id.popup_menu_pin) {
                    mProfileManager.pinProfile(id, true);
                } else if (popupMenuItem.getId() == R.id.popup_menu_unpin) {
                    mProfileManager.pinProfile(id, false);
                } else if (popupMenuItem.getId() == R.id.popup_menu_rename) {
                    RenameDialog renameDialog = RenameDialog.newInstance(onProfileUpdatedListener);
                    renameDialog.show(getSupportFragmentManager(), getString(R.string.rename_dialog_tag));
                } else if (popupMenuItem.getId() == R.id.popup_menu_change_dob) {
                    Birthday bDay = mProfileManager.getProfileById(personaView.getId()).getDateOfBirth();
                    ChangeDateOfBirthDialog changeDateOfBirthDialog = ChangeDateOfBirthDialog.newInstance(onProfileUpdatedListener,
                            bDay.get(Birthday.YEAR), bDay.get(Birthday.MONTH), bDay.get(Birthday.DAY));
                    changeDateOfBirthDialog.show(getSupportFragmentManager(), getString(R.string.change_dob_dialog_tag));
                } else if (popupMenuItem.getId() == R.id.popup_menu_delete) {
                    mProfileManager.removeProfile(id);
                }
            }
        });


        popupMenu.show();
        if (LOG_D) {
            Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) + "milliseconds" +
                    "to show popup menu");
        }
    }

    private PersonaView getPersonaViewById(int id) {
        View view = findViewInLayout(mPinnedProfilesListView, id);
        if (view == null) view = findViewInLayout(mOtherProfilesListView, id);

        if (view instanceof PersonaView) {
            return (PersonaView) view;
        }

        Log.w(LOG_TAG, "Couldn't find any persona view with id: " + id);
        Log.i(LOG_TAG, "Current max id is " + ProfileManager.getMaxedId());
        return null;
    }

    private View findViewInLayout(LinearLayout linearLayout, int viewId) {
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);

            if (view.getId() == viewId) {
                return view;
            }
        }

        return null;
    }

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
        Calendar startTime;
        if (LOG_D) startTime = Calendar.getInstance();

        boolean isEmpty = true;

        if (mPinnedProfilesListView.getChildCount() == 0) mPinnedListView.setVisibility(View.GONE);
        else {
            mPinnedListView.setVisibility(View.VISIBLE);
            isEmpty = false;
        }

        if (mOtherProfilesListView.getChildCount() == 0) mOthersListView.setVisibility(View.GONE);
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
            public void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
                mProfileManager.onProfileDateOfBirthChanged(profileId, newBirthYear, newBirthMonth, newBirthDay, previousBirthDay);
            }

            @Override
            public void onProfileNameChanged(int profileId, String newName, String previousName) {
                mProfileManager.onProfileNameChanged(profileId, newName, previousName);
            }
        });

        mProfileManager.addProfile(profile);
    }

    @Override
    public void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        long[] age = new Age(newBirthYear, newBirthMonth, newBirthDay).get(Age.MODE_YEAR_MONTH_DAY);
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_years_months_days),
                age[Age.YEAR], age[Age.MONTH], age[Age.DAY]));
    }

    @Override
    public void onProfileNameChanged(int profileId, String newName, String previousName) {
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        personaView.setName(newName);
    }

    @Override
    public void onProfilePinned(int profileId, boolean isPinned) {
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        ((LinearLayout)personaView.getParent()).removeView(personaView);
        if (isPinned) {
            mPinnedProfilesListView.addView(personaView, 0);
        } else {
            mOtherProfilesListView.addView(personaView, 0);
        }

        synchronizeVisibleStatus();
    }

    private ImageView getCustomAccessoryView(Drawable drawable) {
        final ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(drawable);
        imageView.setClickable(true);
        imageView.setFocusable(true);
        imageView.setLongClickable(true);
        return imageView;
    }

    @Override
    public void onProfileAdded(Profile profile) {
        PersonaView personaView = generateProfileView(profile);
        mOtherProfilesListView.addView(personaView);

        synchronizeVisibleStatus();
    }

    @Override
    public void onProfileRemoved(int profileId) {
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView != null) {
            ((LinearLayout)personaView.getParent()).removeView(personaView);
        }

        synchronizeVisibleStatus();
    }
}
