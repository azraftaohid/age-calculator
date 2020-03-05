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

public class MainActivity extends AppCompatActivity implements ProfileManagerInterface
        .onProfileUpdateListener, ProfileManagerInterface.onProfilePinListener, AddProfileDialog.OnNewProfileAddedListener {
    public static final String EXTRA_NAME = "com.coolninja.agecalculator.extra.NAME";
    public static final String EXTRA_YEAR = "com.coolninja.agecalculator.extra.YEAR";
    public static final String EXTRA_MONTH = "com.coolninja.agecalculator.extra.MONTH";
    public static final String EXTRA_DAY = "com.coolninja.agecalculator.extra.DAY";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private LinearLayout mPinnedProfilesListView;
    private LinearLayout mOtherProfilesListView;
    private TextView mPinnedProfilesTextView;
    private TextView mOtherProfilesTextView;
    private ScrollView mPinnedProfilesScrollView;
    private ScrollView mOtherProfilesScrollView;

    private ProfileManager mProfileManager;

    private int mDefaultErrorCode;
    static final int DEFAULT_DOB_REQUEST = 1111;

    //TODO implement a way to show popup when clicked on accessory view
    //TODO Add ability to choose between different age viewing formats
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDefaultErrorCode = Integer.parseInt(getString(R.string.default_error_value));
        mPinnedProfilesListView = findViewById(R.id.ll_pinned_profiles);
        mOtherProfilesListView = findViewById(R.id.ll_other_profiles);
        mPinnedProfilesTextView = findViewById(R.id.tv_pinned_profiles);
        mOtherProfilesTextView = findViewById(R.id.tv_other_profiles);
        mPinnedProfilesScrollView = findViewById(R.id.sv_pinned);
        mOtherProfilesScrollView = findViewById(R.id.sv_others);

        mProfileManager = ProfileManager.getProfileManager(this);

        if (mProfileManager.getProfiles().size() == 0) {
            launchWelcomeActivity();
        }

        if (mProfileManager.getProfiles().size() != 0) {
            reRegisterProfileViews();
        }

    }

    void launchWelcomeActivity() {
        Intent setUpIntent = new Intent(this, WelcomeActivity.class);
        startActivityForResult(setUpIntent, DEFAULT_DOB_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DEFAULT_DOB_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String name = data.getStringExtra(EXTRA_NAME);

                Calendar dob = Calendar.getInstance();
                dob.set(data.getIntExtra(EXTRA_YEAR, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_MONTH, mDefaultErrorCode),
                        data.getIntExtra(EXTRA_DAY, mDefaultErrorCode));

                Profile profile = new Profile(name, dob, new ProfileManagerInterface.onProfileUpdateListener() {
                    @Override
                    public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
                        mProfileManager.onProfileDateOfBirthChange(profileId, newDateOfBirth, previousDateOfBirth);
                    }

                    @Override
                    public void onProfileNameChange(int profileId, String newName, String previousName) {
                        mProfileManager.onProfileNameChange(profileId, newName, previousName);
                    }
                });

                mProfileManager.addProfile(profile);
                PersonaView personaView = generateProfileView(profile);
                mOtherProfilesListView.addView(personaView);
                mProfileManager.pinProfile(profile.getId(), true);

                synchronizeVisibleStatus();
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

            if (mProfileManager.isProfilePinned(profile.getId()))
                mPinnedProfilesListView.addView(personaView);
            else
                mOtherProfilesListView.addView(personaView);
        }

        synchronizeVisibleStatus();
    }

    private PersonaView generateProfileView(Profile profile) {
        final PersonaView personaView = new PersonaView(this);
        Age age = profile.getAge();

        personaView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        personaView.setName(profile.getName());
        personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                age.getYear(), age.getMonth(), age.getDay()));
        personaView.setCustomAccessoryView(getCustomAccessoryView(getDrawable(R.drawable.ic_more_vertical)));
        personaView.setLongClickable(true);
        personaView.setId(profile.getId());

        Objects.requireNonNull(personaView.getCustomAccessoryView()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(personaView);
            }
        });

        personaView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMoreOptions(personaView);
                return true;
            }
        });

        return personaView;
    }

    private void showMoreOptions(final PersonaView personaView) {
        assert personaView.getCustomAccessoryView() != null : "PersonaView with ID " + personaView.getId() + " must have a custom accessory view";

        ArrayList<PopupMenuItem> popupMenuItems = new ArrayList<>();

        if (mProfileManager.isProfilePinned(personaView.getId())) popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_unpin, getString(R.string.unpin)));
        else popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_pin, getString(R.string.pin)));
        popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_rename, getString(R.string.rename)));
        popupMenuItems.add(new PopupMenuItem(R.id.popup_menu_change_dob, getString(R.string.change_date_of_birth)));

        final PopupMenu popupMenu = new PopupMenu(this, personaView.getCustomAccessoryView(), popupMenuItems, PopupMenu.ItemCheckableBehavior.NONE);
        popupMenu.setOnItemClickListener(new PopupMenuItem.OnClickListener() {
            @Override
            public void onPopupMenuItemClicked(@NotNull PopupMenuItem popupMenuItem) {
                if (popupMenuItem.getId() == R.id.popup_menu_pin) {
                    mProfileManager.pinProfile(personaView.getId(), true);
                } else if (popupMenuItem.getId() == R.id.popup_menu_unpin) {
                    mProfileManager.pinProfile(personaView.getId(), false);
                }
            }
        });

        popupMenu.show();
    }

    private PersonaView getPersonaViewById(int id) {
        for (int i = 0; i < mPinnedProfilesListView.getChildCount(); i++) {
            PersonaView personaView = (PersonaView) mPinnedProfilesListView.getChildAt(i);
            if (personaView.getId() == id)
                return personaView;
        }

        for (int i = 0; i < mOtherProfilesListView.getChildCount(); i++) {
            PersonaView personaView = (PersonaView) mOtherProfilesListView.getChildAt(i);
            if (personaView.getId() == id)
                return personaView;
        }

        Log.w(LOG_TAG, "Couldn't find any persona view with id: " + id);
        return null;
    }

    private void synchronizeVisibleStatus() {
        if (mPinnedProfilesListView.getChildCount() == 0) {
            mPinnedProfilesScrollView.setVisibility(View.GONE);
            mPinnedProfilesTextView.setVisibility(View.GONE);
        } else {
            mPinnedProfilesScrollView.setVisibility(View.VISIBLE);
            mPinnedProfilesTextView.setVisibility(View.VISIBLE);
        }

        if (mOtherProfilesListView.getChildCount() == 0) {
            mOtherProfilesScrollView.setVisibility(View.GONE);
            mOtherProfilesTextView.setVisibility(View.GONE);
        } else {
            mOtherProfilesScrollView.setVisibility(View.VISIBLE);
            mOtherProfilesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSubmit(String name, Calendar dateOfBirth) {
        Profile profile = new Profile(name, dateOfBirth, new ProfileManagerInterface.onProfileUpdateListener() {
            @Override
            public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
                mProfileManager.onProfileDateOfBirthChange(profileId, newDateOfBirth, previousDateOfBirth);
            }

            @Override
            public void onProfileNameChange(int profileId, String newName, String previousName) {
                mProfileManager.onProfileNameChange(profileId, newName, previousName);
            }
        });

        mProfileManager.addProfile(profile);

        PersonaView personaView = generateProfileView(profile);
        mOtherProfilesListView.addView(personaView);

        synchronizeVisibleStatus();
    }

    @Override
    public void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth) {
        Age age = new Age(newDateOfBirth.get(Calendar.YEAR), newDateOfBirth.get(Calendar.MONTH), newDateOfBirth.get(Calendar.DAY_OF_MONTH));
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        personaView.setSubtitle(String.format(Locale.ENGLISH, getString(R.string.display_age_years_months_days),
                age.getYear(), age.getMonth(), age.getDay()));
    }

    @Override
    public void onProfileNameChange(int profileId, String newName, String previousName) {
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        personaView.setName(newName);
    }

    @Override
    public void onProfilePin(int profileId, boolean isPinned) {
        PersonaView personaView = getPersonaViewById(profileId);
        if (personaView == null) {
            Log.w(LOG_TAG, "Couldn't find persona view for profile + " + profileId);
            return;
        }

        ((LinearLayout)personaView.getParent()).removeView(personaView);
        if (isPinned) {
            mPinnedProfilesListView.addView(personaView);
        } else {
            mOtherProfilesListView.addView(personaView);
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
}
