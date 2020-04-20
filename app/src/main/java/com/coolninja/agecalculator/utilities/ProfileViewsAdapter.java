package com.coolninja.agecalculator.utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.coolninja.agecalculator.R;
import com.coolninja.agecalculator.ui.ProfileDetailsActivity;
import com.coolninja.agecalculator.utilities.codes.Error;
import com.coolninja.agecalculator.utilities.profilemanagement.Profile;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManager;
import com.coolninja.agecalculator.utilities.profilemanagement.ProfileManagerInterface;
import com.microsoft.officeuifabric.bottomsheet.BottomSheetDialog;
import com.microsoft.officeuifabric.bottomsheet.BottomSheetItem;
import com.microsoft.officeuifabric.persona.PersonaView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.coolninja.agecalculator.ui.MainActivity.LOG_D;
import static com.coolninja.agecalculator.ui.MainActivity.LOG_V;
import static com.coolninja.agecalculator.utilities.Age.DAY;
import static com.coolninja.agecalculator.utilities.Age.MONTH;
import static com.coolninja.agecalculator.utilities.Age.YEAR;
import static com.coolninja.agecalculator.utilities.codes.Extra.EXTRA_PROFILE_ID;

public class ProfileViewsAdapter extends RecyclerView.Adapter<ProfileViewsAdapter.ProfileViewHolder> implements
        ProfileManagerInterface.onProfileUpdatedListener {

    private static int adapterCount = 0;

    private static String LOG_TAG = ProfileViewsAdapter.class.getSimpleName();
    private static String LOG_TAG_PERFORMANCE = ProfileViewsAdapter.class.getSimpleName() + ".performance";

    private Context mContext;
    private LayoutInflater mInflater;
    private ProfileManager mProfileManager;
    private ArrayList<Profile> mProfiles;

    private int mAdapterNumber;

    public ProfileViewsAdapter(Context context, ProfileManager profileManager, ArrayList<Profile> profiles) {
        if (LOG_V) Log.v(LOG_TAG, "Initializing profile views adapter");

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mProfileManager = profileManager;
        mProfiles = profiles;

        mAdapterNumber = adapterCount++;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.profile_view, parent, false);

        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        if (LOG_V) Log.i(LOG_TAG, "Binding profile view with adapter, position: " + position);
        Profile profile = mProfiles.get(position);
        holder.mProfileView.setName(profile.getName());

        Avatar avatar = profile.getAvatar();
        if (avatar != null)
            holder.mProfileView.setAvatarImageBitmap(profile.getAvatar().getBitmap());

        long[] age = profile.getAge().get(Age.MODE_YEAR_MONTH_DAY);
        holder.mProfileView.setSubtitle(String.format(Locale.ENGLISH, mContext.getString(R.string.display_years_months_days),
                age[YEAR], age[MONTH], age[DAY]));
    }

    @Override
    public int getItemCount() {
        int size = mProfiles.size();
        if (LOG_V) Log.v(LOG_TAG, "Total items in profile views adapter: " + size);
        return size;
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

    public void addProfile(Profile profile) {
        if (LOG_V)
            Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " to adapter " + mAdapterNumber);

        mProfiles.add(profile);
        this.notifyItemInserted(getItemCount() - 1);
    }

    public void addProfile(int position, Profile profile) {
        if (LOG_V)
            Log.v(LOG_TAG, "Adding profile w/ ID " + profile.getId() + " at position " + position + " to adapter " + mAdapterNumber);

        mProfiles.add(position, profile);
        this.notifyItemInserted(position);
    }

    public void removeProfile(int profileId) {
        if (LOG_V)
            Log.v(LOG_TAG, "Removing profile w/ ID " + profileId + " from adapter " + mAdapterNumber);

        int position = getProfilePosition(profileId);
        if (LOG_V) Log.v(LOG_TAG, "Removing profile view at position: " + position);
        mProfiles.remove(position);
        notifyItemRemoved(position);
    }

    public void refresh() {
        notifyItemRangeChanged(0, mProfiles.size());
    }

    private int getProfilePosition(int profileId) {
        int size = getItemCount();
        for (int i = 0; i < size; i++) {
            if (mProfiles.get(i).getId() == profileId) {
                return i;
            }
        }

        Log.e(LOG_TAG, "No profile w/ ID " + profileId + " exists in adapter " + mAdapterNumber);
        return Error.NOT_FOUND;
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private PersonaView mProfileView;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);

            if (LOG_V) Log.v(LOG_TAG, "Initializing profile view holder");

            mProfileView = itemView.findViewById(R.id.pv_profile_template);

            mProfileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchProfileDetailsActivity(mProfiles.get(getLayoutPosition()).getId());
                }
            });

            mProfileView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showMoreOptions();
                    return true;
                }
            });

            ImageView customAccessoryView = getCustomAccessoryView(mContext.getDrawable(R.drawable.ic_more_vertical));
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
            Calendar startTime;
            if (LOG_D) startTime = Calendar.getInstance();

            final Profile profile = mProfiles.get(getLayoutPosition());
            final int profileId = profile.getId();
            assert mProfileView.getCustomAccessoryView() != null : "Profile view for profile w/ ID " + profileId
                    + " must have a custom accessory view";


            ArrayList<BottomSheetItem> items = new ArrayList<>();

            if (ProfileManager.isPinned(profileId)) items.add(new BottomSheetItem(R.id.popup_menu_unpin, R.drawable.ic_pinned, mContext.getString(R.string.unpin)));
            else items.add(new BottomSheetItem(R.id.popup_menu_pin, R.drawable.ic_unpinned, mContext.getString(R.string.pin)));
            items.add(new BottomSheetItem(R.id.popup_menu_rename, R.drawable.ic_edit,  mContext.getString(R.string.rename)));
            items.add(new BottomSheetItem(R.id.popup_menu_change_dob, R.drawable.ic_calendar, mContext.getString(R.string.change_date_of_birth)));
            items.add(new BottomSheetItem(R.id.popup_menu_delete, R.drawable.ic_remove, mContext.getString(R.string.delete)));

            Integer avatarId = mProfileView.getAvatarImageResourceId();
            BottomSheetItem header = new BottomSheetItem(R.id.popup_menu_header, avatarId == null? R.drawable.ic_person : avatarId, mProfileView.getTitle(), mProfileView.getSubtitle());

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext, items, header);
            bottomSheetDialog.setOnItemClickListener(new BottomSheetItem.OnClickListener() {
                @Override
                public void onBottomSheetItemClick(@NotNull BottomSheetItem bottomSheetItem) {
                    if (bottomSheetItem.getId() == R.id.popup_menu_pin) {
                        mProfileManager.pinProfile(profileId, true);
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_unpin) {
                        mProfileManager.pinProfile(profileId, false);
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_rename) {
                        RenameDialog renameDialog = RenameDialog.newInstance(profile);
                        renameDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), mContext.getString(R.string.rename_dialog_tag));
                    } else if (bottomSheetItem.getId() == R.id.popup_menu_change_dob) {
                        ChangeDateOfBirthDialog changeDateOfBirthDialog = ChangeDateOfBirthDialog.newInstance(profile);
                        changeDateOfBirthDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), mContext.getString(R.string.change_dob_dialog_tag));
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

        private ImageView getCustomAccessoryView(Drawable drawable) {
            final ImageView imageView = new ImageView(mContext);
            imageView.setImageDrawable(drawable);
            imageView.setClickable(true);
            imageView.setFocusable(true);
            imageView.setLongClickable(true);
            return imageView;
        }
    }

}
