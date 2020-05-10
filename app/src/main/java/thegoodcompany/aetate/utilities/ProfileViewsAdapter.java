package thegoodcompany.aetate.utilities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.microsoft.officeuifabric.bottomsheet.BottomSheetDialog;
import com.microsoft.officeuifabric.bottomsheet.BottomSheetItem;
import com.microsoft.officeuifabric.persona.PersonaView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.ui.ProfileDetailsActivity;
import thegoodcompany.aetate.ui.ProfileInfoInputDialog;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManagerInterface;

import static thegoodcompany.aetate.ui.MainActivity.LOG_D;
import static thegoodcompany.aetate.ui.MainActivity.LOG_I;
import static thegoodcompany.aetate.ui.MainActivity.LOG_V;
import static thegoodcompany.aetate.utilities.Age.DAY;
import static thegoodcompany.aetate.utilities.Age.MONTH;
import static thegoodcompany.aetate.utilities.Age.YEAR;
import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_PROFILE_ID;
import static thegoodcompany.aetate.utilities.codes.Request.REQUEST_MODIFY_PROFILE_INFO;

public class ProfileViewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        ProfileManagerInterface.onProfileUpdatedListener {

    private static final int VIEW_TYPE_HEADER = 1000;
    private static final int VIEW_TYPE_PROFILE = 1001;

    private static String LOG_TAG = ProfileViewsAdapter.class.getSimpleName();
    private static String LOG_TAG_PERFORMANCE = ProfileViewsAdapter.class.getSimpleName() + ".performance";

    private Context mContext;
    private LayoutInflater mInflater;
    private ProfileManager mProfileManager;
    private HashMap<Integer, ProfilesSection> mSectionMap;
    @Nullable
    private int[] mSectionOrder;

    public ProfileViewsAdapter(Context context, ProfileManager profileManager, HashMap<Integer, ProfilesSection> sectionMap) {
        if (LOG_V)
            Log.v(LOG_TAG, "Initializing profile views adapter with " + sectionMap.size() + " sections");

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mProfileManager = profileManager;
        mSectionMap = sectionMap;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (LOG_V) Log.v(LOG_TAG, "Creating profile view holder type: " + viewType);

        if (viewType == VIEW_TYPE_PROFILE) {
            return new ProfileViewHolder(mInflater.inflate(R.layout.profile_view, parent, false));
        }

        return new HeaderViewHolder(mInflater.inflate(R.layout.header_profiles_section, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (LOG_V) Log.v(LOG_TAG, "Binding view holder; position: " + position);

        int headerIndex = 0;
        int profileIndex = 0;
        ProfilesSection[] sections = mSectionMap.values().toArray(new ProfilesSection[0]);

        int evaluatingPosition = 0;
        for (ProfilesSection section : sections) {
            if (LOG_V) Log.v(LOG_TAG, "Evaluating section: " + section.getTitle());
            evaluatingPosition += section.getItemCount();

            if (evaluatingPosition > position) {
                profileIndex = section.getProfileCount() - (evaluatingPosition - position);

                if (LOG_I) {
                    Log.i(LOG_TAG, "Header index: " + headerIndex);
                    Log.i(LOG_TAG, "Profile index: " + profileIndex);
                }

                break;
            }

            headerIndex++;
        }

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            if (LOG_V)
                Log.v(LOG_TAG, "Setting header title, index: " + headerIndex + "; title: " + sections[headerIndex].getTitle());

            HeaderViewHolder headerViewHolder = ((HeaderViewHolder) holder);
            if (position == 0) {
                LinearLayout container = headerViewHolder.mContainerLinearLayout;
                container.setPadding(container.getPaddingLeft(), 0, container.getPaddingRight(), container.getPaddingBottom());
                headerViewHolder.mContainerLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
            }

            headerViewHolder.mTitleTextView.setText(sections[headerIndex].getTitle());

            return;
        }

        ProfileViewHolder profileViewHolder = (ProfileViewHolder) holder;

        if (LOG_V) Log.v(LOG_TAG, "Binding profile view with adapter");

        Profile profile = sections[headerIndex].getProfile(profileIndex);
        if (LOG_V) Log.v(LOG_TAG, "Position: " + position + "; ID: " + profile.getId());

        if (LOG_V) Log.v(LOG_TAG, "Setting name on the view holder");
        profileViewHolder.mProfileView.setName(profile.getName());

        long[] age = profile.getAge().get(Age.MODE_YEAR_MONTH_DAY);
        if (LOG_V) Log.v(LOG_TAG, "Setting age on view holder, position: " + position);
        profileViewHolder.mProfileView.setSubtitle(String.format(Locale.ENGLISH, mContext.getString(R.string.display_years_months_days),
                age[YEAR], age[MONTH], age[DAY]));

        Avatar avatar = profile.getAvatar();
        if (avatar != null) {
            if (LOG_V) Log.v(LOG_TAG, "Setting avatar on the view holder");
            profileViewHolder.mProfileView.setAvatarImageBitmap(profile.getAvatar().getBitmap());
        } else {
            profileViewHolder.mProfileView.setAvatarImageBitmap(null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int headerPosition = 0;
        for (ProfilesSection section : mSectionMap.values()) {
            if (headerPosition == position) {
                if (LOG_V)
                    Log.v(LOG_TAG, "View type of item (Position: " + position + "Type: Header)");
                return VIEW_TYPE_HEADER;
            }

            headerPosition += section.getItemCount();
        }

        if (LOG_V)
            Log.v(LOG_TAG, "View type of item (Position: " + position + "Type: Profile)");
        return VIEW_TYPE_PROFILE;
    }

    @Override
    public int getItemCount() {
        int size = 0;
        for (ProfilesSection section : mSectionMap.values()) {
            size += section.getItemCount();
        }

        if (LOG_V) Log.v(LOG_TAG, "Total items in profile views adapter: " + size);
        return size;
    }

    @SuppressWarnings("unused")
    public int getProfileCount(int sectionKey) {
        return Objects.requireNonNull(mSectionMap.get(sectionKey)).getProfileCount();
    }

    public boolean containsSection(int sectionKey) {
        return mSectionMap.containsKey(sectionKey);
    }

    private void addProfileSilent(int sectionKey, int positionInSection, Profile profile) {
        try {
            Objects.requireNonNull(mSectionMap.get(sectionKey)).addProfile(positionInSection, profile);
        } catch (IndexOutOfBoundsException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(mContext, Error.DEFAULT.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addProfile(int sectionKey, Profile profile) {
        if (LOG_V)
            Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " to section w/ key: " + sectionKey);

        ProfilesSection section = mSectionMap.get(sectionKey);
        Objects.requireNonNull(section).addProfile(profile);

        this.notifyItemInserted(getHeaderPosition(sectionKey) + section.getProfileCount());
    }

    @SuppressWarnings("unused")
    public void addProfile(int sectionKey, int positionInSection, Profile profile) {
        if (LOG_V)
            Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " to section w/ key: " + sectionKey
                    + " at position" + positionInSection);

        addProfileSilent(sectionKey, positionInSection, profile);

        this.notifyItemInserted(getHeaderPosition(sectionKey) + 1 + positionInSection);
    }

    public void addSection(int sectionKey, ProfilesSection section) {
        if (LOG_V) Log.v(LOG_TAG, "Adding section (Key: " + sectionKey + ")");
        mSectionMap.put(sectionKey, section);

        if (mSectionOrder != null) {
            if (LOG_V) Log.v(LOG_TAG, "Reordering sections");
            HashMap<Integer, ProfilesSection> newlySortedSections = new HashMap<>();

            for (int key : mSectionOrder) {
                if (mSectionMap.containsKey(key))
                    newlySortedSections.put(key, mSectionMap.get(key));
            }

            if (newlySortedSections.size() != mSectionMap.size()) {
                for (int key : mSectionMap.keySet()) {
                    if (!newlySortedSections.containsKey(key))
                        newlySortedSections.put(key, mSectionMap.get(key));
                }
            }

            mSectionMap = newlySortedSections;
        }

        int newSectionPosition = getHeaderPosition(sectionKey);
        int itemsInSection = section.getItemCount();
        notifyItemRangeInserted(newSectionPosition, itemsInSection);

        if (newSectionPosition == 0 && mSectionMap.size() > 1) {
            notifyItemChanged(itemsInSection);
        }
    }

    public void moveProfile(int profileId, int toSection, int position) {
        if (LOG_V)
            Log.v(LOG_TAG, "Moving profile (ID: " + profileId + "; Section key: " + toSection + "; Position: " + position + ")");

        int previousPosition = getProfilePosition(profileId);
        Profile profile = getProfile(previousPosition);
        removeProfileSilent(profileId);
        addProfileSilent(toSection, position, profile);

        int newPosition = getProfilePosition(profileId);
        if (LOG_D)
            Log.d(LOG_TAG, "Profile moved (Old position: " + previousPosition + "; new position: " + newPosition + ")");

        notifyItemMoved(previousPosition, newPosition);
    }

    private void removeProfileSilent(int profileId) {

        for (ProfilesSection section : mSectionMap.values()) {
            int numberOfProfiles = section.getProfileCount();

            for (int i = 0; i < numberOfProfiles; i++) {
                if (section.getProfile(i).getId() == profileId) {
                    section.removeProfile(i);
                    break;
                }
            }

        }
    }

    public void removeProfile(int profileId) {
        if (LOG_V)
            Log.v(LOG_TAG, "Removing profile w/ ID " + profileId);

        int oldPosition = getProfilePosition(profileId);

        removeProfileSilent(profileId);
        notifyItemRemoved(oldPosition);
    }

    public void removeSection(int sectionKey) {
        if (LOG_V) Log.v(LOG_TAG, "Removing section w/ key: " + sectionKey);

        int startRange = getHeaderPosition(sectionKey);
        int itemCount = Objects.requireNonNull(mSectionMap.get(sectionKey)).getItemCount(); //plus 1 is for header

        mSectionMap.remove(sectionKey);

        if (LOG_I)
            Log.i(LOG_TAG, "Removes section (Key: " + sectionKey + "; Position: " + startRange + "; ItemCount: " + itemCount + ")");

        notifyItemRangeRemoved(startRange, itemCount);

        if (startRange == 0) notifyItemChanged(0);
    }

    private Profile getProfile(int position) {
        int evaluatingPosition = 0;

        for (ProfilesSection section : mSectionMap.values()) {
            evaluatingPosition++;

            int profileCount = section.getProfileCount();
            for (int i = 0; i < profileCount; i++) {
                if (evaluatingPosition == position) {
                    if (LOG_V)
                        Log.v(LOG_TAG, "Profile w/ ID: " + section.getProfile(i).getId() + " is at position: " + position);
                    return section.getProfile(i);
                }

                evaluatingPosition++;
            }
        }

        throw new AssertionError("Specify a position where profile exists. Specified position: " + position);
    }

    @SuppressWarnings("unused")
    private int getItemHeaderIndex(int position) {
        if (LOG_V) Log.v(LOG_TAG, "Getting item's header index at position: " + position);

        int headerIndex = 0;

        int evaluatingPosition = 0;
        for (ProfilesSection section : mSectionMap.values()) {
            evaluatingPosition += section.getItemCount();

            if (evaluatingPosition > position) {
                if (LOG_I) Log.i(LOG_TAG, "Header index: " + headerIndex);
                return headerIndex;
            }

            headerIndex++;
        }

        return headerIndex;
    }

    @SuppressWarnings("WeakerAccess")
    public int getHeaderPosition(int sectionKey) {
        int headerPosition = 0;

        Iterator<ProfilesSection> iterator = mSectionMap.values().iterator();
        for (int key : mSectionMap.keySet()) {
            if (key == sectionKey) {
                break;
            }

            headerPosition += iterator.next().getItemCount();
        }

        if (LOG_I) Log.i(LOG_TAG, "Section header position: " + headerPosition);

        return headerPosition;
    }

    public int getProfilePosition(int profileId) {
        int position = 0;
        for (ProfilesSection section : mSectionMap.values()) {
            position++;

            int profileCount = section.getProfileCount();
            for (int i = 0; i < profileCount; i++) {
                if (section.getProfile(i).getId() == profileId) {
                    if (LOG_V) Log.v(LOG_TAG, "Profile w/ ID is at position: " + position);
                    return position;
                }

                position++;
            }
        }

        throw new AssertionError("No profile w/ ID " + profileId + " is attached to this adapter");
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        notifyItemChanged(getProfilePosition(profileId));
    }

    @Override
    public void onProfileNameUpdated(int profileId, String newName, String previousName) {
        notifyItemChanged(getProfilePosition(profileId));
    }

    @Override
    public void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar) {
        notifyItemChanged(getProfilePosition(profileId));
    }

    public void setSectionOrder(int[] sectionKeysInOrder) {
        mSectionOrder = sectionKeysInOrder;
    }

    @Deprecated
    // This method is deprecated because main activity no longer uses ProfileViewsAdapter#itemCount to
    // determine which section to remove
    public void recreateFromContext(Context context) {
        mContext = context;
        mProfileManager = ProfileManager.getProfileManager(context);

        ProfilesSection[] sections = mSectionMap.values().toArray(new ProfilesSection[0]);
        Integer[] keys = mSectionMap.keySet().toArray(new Integer[0]);

        mSectionMap = new HashMap<>();
        for (int i = 0; i < sections.length; i++) {
            ArrayList<Profile> profiles = new ArrayList<>();
            for (int i2 = 0; i2 < sections[i].getProfileCount(); i2++) {
                profiles.add(mProfileManager.getProfileById(sections[i].getProfile(i2).getId()));
            }

            mSectionMap.put(keys[i], new ProfilesSection(profiles, sections[i].getTitle()));
        }

        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mContainerLinearLayout;
        private TextView mTitleTextView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            mContainerLinearLayout = itemView.findViewById(R.id.ll_section_header_container);
            mTitleTextView = itemView.findViewById(R.id.tv_profiles_section_title);
        }
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private PersonaView mProfileView;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);

            if (LOG_V) Log.v(LOG_TAG, "Initializing profile view holder");

            if (LOG_I) Log.i(LOG_TAG, "Holder position: " + getLayoutPosition());
            if (LOG_I) Log.i(LOG_TAG, "Holder view type: " + getItemViewType());
            mProfileView = itemView.findViewById(R.id.pv_profile_template);
            if (mProfileView.getAvatarImageDrawable() != null)
                Log.wtf(LOG_TAG, "Profile view has avatar loaded");

            mProfileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchProfileDetailsActivity(getProfile(getLayoutPosition()).getId());
                }
            });

            mProfileView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showMoreOptions();
                    return true;
                }
            });

            ImageView customAccessoryView = CommonUtilities.generateCustomAccessoryView(mContext, R.drawable.ic_more_vertical);
            customAccessoryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoreOptions();
                }
            });

            mProfileView.setCustomAccessoryView(customAccessoryView);
        }

        void launchProfileDetailsActivity(int profileId) {
            Intent showDetailsIntent = new Intent(mContext, ProfileDetailsActivity.class);
            showDetailsIntent.putExtra(EXTRA_PROFILE_ID, profileId);
            mContext.startActivity(showDetailsIntent);
        }

        private void showMoreOptions() {
            Calendar startTime = null;
            if (LOG_D) startTime = Calendar.getInstance();

            final Profile profile = getProfile(getLayoutPosition());
            final int profileId = profile.getId();

            ArrayList<BottomSheetItem> items = new ArrayList<>();

            if (ProfileManager.isPinned(profileId))
                items.add(new BottomSheetItem(R.id.popup_menu_unpin, R.drawable.ic_pinned, mContext.getString(R.string.unpin)));
            else
                items.add(new BottomSheetItem(R.id.popup_menu_pin, R.drawable.ic_unpinned, mContext.getString(R.string.pin)));
            items.add(new BottomSheetItem(R.id.popup_menu_modify, R.drawable.ic_edit, mContext.getString(R.string.modify)));
            items.add(new BottomSheetItem(R.id.popup_menu_delete, R.drawable.ic_remove, mContext.getString(R.string.delete)));

            Integer avatarId = mProfileView.getAvatarImageResourceId();
            BottomSheetItem header = new BottomSheetItem(R.id.popup_menu_header, avatarId == null ? R.drawable.ic_person : avatarId, mProfileView.getTitle(), mProfileView.getSubtitle());

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext, items, header);

            bottomSheetDialog.setOnItemClickListener(new BottomSheetItem.OnClickListener() {
                @Override
                public void onBottomSheetItemClick(@NotNull BottomSheetItem bottomSheetItem) {
                    if (bottomSheetItem.getId() == R.id.popup_menu_pin) {
                        mProfileManager.pinProfile(profileId, true);
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_unpin) {
                        mProfileManager.pinProfile(profileId, false);
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_modify) {
                        ProfileInfoInputDialog.newInstance(mContext, REQUEST_MODIFY_PROFILE_INFO, mContext.getString(R.string.modify), profile).show(((FragmentActivity) mContext).getSupportFragmentManager(), mContext.getString(R.string.modify_profile_dialog_tag));
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_delete) {
                        mProfileManager.removeProfile(profileId);
                    }
                }
            });

            bottomSheetDialog.show();

            if (LOG_D) {
                Log.d(LOG_TAG_PERFORMANCE, "It took " + (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())
                        + " milliseconds to show popup menu");
            }
        }

    }

}
