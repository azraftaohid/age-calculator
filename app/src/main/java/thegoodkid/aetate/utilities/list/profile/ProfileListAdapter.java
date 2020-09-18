package thegoodkid.aetate.utilities.list.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.fluentui.bottomsheet.BottomSheetDialog;
import com.microsoft.fluentui.bottomsheet.BottomSheetItem;
import com.microsoft.fluentui.listitem.ListSubHeaderView;
import com.microsoft.fluentui.persona.PersonaView;
import com.microsoft.fluentui.util.ThemeUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import thegoodcompany.common.utils.CalendarUtils;
import thegoodcompany.common.utils.StringUtils;
import thegoodcompany.common.utils.recyclerview.BaseItem;
import thegoodcompany.common.utils.recyclerview.BaseListAdapter;
import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodkid.aetate.R;
import thegoodkid.aetate.ui.ProfileDetailsActivity;
import thegoodkid.aetate.ui.ProfileInfoInputDialog;
import thegoodkid.aetate.utilities.Avatar;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.CommonUtilities;
import thegoodkid.aetate.utilities.codes.Extra;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;

import static thegoodkid.aetate.utilities.CommonUtilities.createTintedDrawable;
import static thegoodkid.aetate.utilities.CommonUtilities.mutateAndTintDrawable;

public class ProfileListAdapter<K extends Enum<K>> extends BaseListAdapter<K, ProfilesGroup, HeaderItem, Profile,
        RecyclerView.ViewHolder> {

    private Context mContext;

    public ProfileListAdapter(Context context, LinkedHashMap<K, ProfilesGroup> groupMap) {
        super(groupMap);

        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        switch (ViewType.values()[viewType]) {
            case ITEM:
                PersonaView profileView = new PersonaView(mContext);
                ImageButton accessoryView = new ImageButton(mContext);
                accessoryView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ms_ripple_transparent_background_borderless));

                @DrawableRes int accessoryDrawable = R.drawable.ic_fluent_more_vertical_24_regular;
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    LayerDrawable defAvatar = (LayerDrawable) ContextCompat.getDrawable(mContext, R.drawable.img_defaut_avatar);
                    if (defAvatar != null) {
                        Drawable personIcon = defAvatar.findDrawableByLayerId(R.id.default_avatar_person);
                        personIcon = mutateAndTintDrawable(personIcon, ThemeUtil.INSTANCE.getThemeAttrColor(mContext, R.attr.fluentuiForegroundSecondaryIconColor));
                        defAvatar.setDrawableByLayerId(R.id.default_avatar_person, personIcon);
                    }

                    profileView.setAvatarImageDrawable(defAvatar);
                    accessoryView.setImageDrawable(createTintedDrawable(mContext, accessoryDrawable, R.attr.fluentuiBackgroundSecondaryColor));
                } else {
                    accessoryView.setImageResource(accessoryDrawable);
                    profileView.setAvatarImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_defaut_avatar));
                }

                profileView.setLayoutParams(layoutParams);
                profileView.setCustomAccessoryView(accessoryView);

                return new ProfileViewHolder(profileView);
            case HEADER:
                ListSubHeaderView headerView = new ListSubHeaderView(mContext);
                headerView.setLayoutParams(layoutParams);
                return new HeaderViewHolder(headerView);
        }

        throw new IllegalStateException("Unhandled view type: " + ViewType.values()[viewType]);
    }

    @Override
    protected void onBindViewHolder(@NonNull AfterWards runAfterwards, @NonNull RecyclerView.ViewHolder holder, int position) {
        BaseItem item = getItem(position);

        if (holder instanceof ProfileViewHolder) {
            ProfileViewHolder profileHolder = ((ProfileViewHolder) holder);
            Profile profile = (Profile) item;

            setNameOnHolder(profileHolder, profile);
            setAvatarOnHolder(profileHolder, profile);
            setAgeOnHolder(profileHolder, profile);

            View accessoryView = profileHolder.profileView.getCustomAccessoryView();
            if (accessoryView != null)
                accessoryView.setOnClickListener(view -> onProfileMoreClicked(profileHolder.profileView, profile));

            profileHolder.profileView.setOnClickListener(view -> onProfileViewClicked(profile));
            profileHolder.profileView.setOnLongClickListener(view -> onProfileMoreClicked(profileHolder.profileView, profile));
        } else if (holder instanceof HeaderViewHolder) {
            HeaderItem header = (HeaderItem) item;
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;

            viewHolder.headerView.setTitle(header.getTitle());
            viewHolder.headerView.setCustomAccessoryView(header.getAccessoryView());
        }

        runAfterwards.run();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        boolean shouldPassToSuper = true;

        if (!payloads.isEmpty()) {
            BaseItem item = getItem(position);
            shouldPassToSuper = false;

            for (Object payloadObj : payloads) {
                if (!(payloadObj instanceof Payload)) {
                    shouldPassToSuper = true;
                    break;
                }

                switch ((Payload) payloadObj) {
                    case PROFILE_NAME:
                        setNameOnHolder((ProfileViewHolder) holder, (Profile) item);
                        break;
                    case AVATAR:
                        setAvatarOnHolder((ProfileViewHolder) holder, (Profile) item);
                        break;
                    case AGE:
                        setAgeOnHolder((ProfileViewHolder) holder, (Profile) item);
                        break;
                    default:
                        shouldPassToSuper = true;
                }
            }
        }

        if (shouldPassToSuper) super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        if (holder instanceof ProfileViewHolder) {
            ((ProfileViewHolder) holder).profileView.setAvatarImageBitmap(null);
        }
    }

    private void onProfileViewClicked(@NotNull Profile profile) {
        Intent displayDetailsIntent = new Intent(mContext, ProfileDetailsActivity.class);
        displayDetailsIntent.putExtra(Extra.EXTRA_PROFILE_ID, profile.getId());

        mContext.startActivity(displayDetailsIntent);
    }

    private boolean onProfileMoreClicked(PersonaView profileView, @NotNull Profile profile) {
        ArrayList<BottomSheetItem> items = new ArrayList<>();

        if (ProfileManager.isPinned(profile.getId()))
            items.add(new BottomSheetItem(R.id.popup_menu_unpin, R.drawable.ic_fluent_pin_off_24_selector, mContext.getString(R.string.unpin)));
        else
            items.add(new BottomSheetItem(R.id.popup_menu_pin, R.drawable.ic_fluent_pin_24_selector, mContext.getString(R.string.pin)));
        items.add(new BottomSheetItem(R.id.popup_menu_modify, R.drawable.ic_fluent_edit_24_regular, mContext.getString(R.string.modify)));
        items.add(new BottomSheetItem(R.id.popup_menu_delete, R.drawable.ic_fluent_delete_24_regular, mContext.getString(R.string.delete)));

        Integer avatarId = profileView.getAvatarImageResourceId();
        BottomSheetItem header = new BottomSheetItem(R.id.popup_menu_header, avatarId == null ? R.drawable.ic_fluent_person_24_regular : avatarId, profileView.getTitle(), profileView.getSubtitle());

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext, items, header);

        bottomSheetDialog.setOnItemClickListener(bottomSheetItem -> {
            ProfileManager profileManager = ProfileManager.getInstance(mContext);
            if (bottomSheetItem.getId() == R.id.popup_menu_pin) {
                profileManager.pinProfile(profile.getId(), true);
            } else if (bottomSheetItem.getId() == R.id.popup_menu_unpin) {
                profileManager.pinProfile(profile.getId(), false);
            } else if (bottomSheetItem.getId() == R.id.popup_menu_modify) {
                ProfileInfoInputDialog.modifyProfile(profile)
                        .show(((FragmentActivity) mContext).getSupportFragmentManager(),
                                mContext.getString(R.string.modify_profile_dialog_tag));
            } else if (bottomSheetItem.getId() == R.id.popup_menu_delete) {
                profileManager.removeProfile(profile.getId());
            }
        });

        bottomSheetDialog.show();
        return true;
    }

    private void setNameOnHolder(@NonNull ProfileViewHolder holder, @NonNull Profile profile) {
        holder.profileView.setName(profile.getName());
    }

    private void setAgeOnHolder(@NotNull ProfileViewHolder holder, @NotNull Profile profile) {
        Calendar c = Calendar.getInstance();
        Birthday birthday = profile.getBirthday();

        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);

        long[] age = CalendarUtils.calculateIntervals(currentYear, currentMonth, currentDay,
                birthday.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth(), CalendarUtils.MODE_YEAR_MONTH_DAY);

        long[] intervalUntilNextBday = CommonUtilities.calculateDaysLeftForBirthday(currentYear, currentMonth, currentDay,
                profile.getBirthday(), CalendarUtils.MODE_MONTH_DAY);

        int[] appendants = CommonUtilities.createCalendarAppendants();
        String subtitle = StringUtils.joinUnless(mContext.getResources(), age, appendants, ", ", 0);
        String footer = StringUtils.joinUnless(mContext.getResources(), intervalUntilNextBday, appendants, ", ", 0);

        holder.profileView.setSubtitle(subtitle.length() > 0 ? subtitle + mContext.getString(R.string.suffix_age) : subtitle);
        holder.profileView.setFooter(footer.length() > 0 ? footer + mContext.getString(R.string.suffix_birthday) : footer);
    }

    private void setAvatarOnHolder(@NonNull ProfileViewHolder holder, @NotNull Profile profile) {
        Avatar avatar = profile.getAvatar();

        if (avatar != null) holder.profileView.setAvatarImageBitmap(avatar.getBitmap());
    }

    public void refreshAvatar(int profileId) {
        notifyItemChanged(getProfilePos(profileId), Payload.AVATAR);
    }

    public void refreshName(int profileId) {
        notifyItemChanged(getProfilePos(profileId), Payload.PROFILE_NAME);
    }

    public void refreshAge(int profileId) {
        notifyItemChanged(getProfilePos(profileId), Payload.AGE);
    }

    @Override
    public void addItem(K key, Profile profile) {
        super.addItem(key, profile);

        checkStateFull(key, 1);
    }

    @Override
    public void addItem(K key, int position, Profile profile) {
        super.addItem(key, position, profile);

        checkStateFull(key, 1);
    }

    public void addProfileInBatch(K section, List<Profile> profiles) {
        ProfilesGroup group = getSectionMap().get(section);
        if (group == null) return;

        int startPos = getSectionStartPosition(section) + (group.hasHeader() ? 1 : 0) + group.getItemCount();
        group.addAll(profiles);

        notifyItemRangeInserted(startPos, startPos + profiles.size());
        checkStateFull(section, profiles.size());
    }

    @Override
    public void addSection(K sectionKey, ProfilesGroup section) {
        super.addSection(sectionKey, section);

        checkStateFull(sectionKey, section.getItemCount());
    }

    public int getProfilePos(int profileId) {
        Profile profile = ProfileManager.getInstance(mContext).getProfileById(profileId);
        return getItemPosition(profile);
    }

    public int getProfilePos(Profile profile) {
        return getItemPosition(profile);
    }

    @Override
    public boolean moveItem(Profile profile, K destSectionKey, int newPosAtSection) {
        K source = getItemSectionKey(profile);
        if (super.moveItem(profile, destSectionKey, newPosAtSection)) {
            checkStateFull(source, 1);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeItem(Profile profile) {
        K source = super.getItemSectionKey(profile);
        return super.removeItemFromSection(source, profile);
    }

    @Override
    public boolean removeItemFromSection(K sectionKey, Profile profile) {
        ProfilesGroup group = getSectionMap().get(sectionKey);
        if (group == null) return false;

        boolean succeed;
        if (group.getItemCount() == 1 && group.getItem(0).equals(profile)) {
            if ((succeed = removeSection(sectionKey))) queryState();
        } else {
            if ((succeed = super.removeItemFromSection(sectionKey, profile)))
                checkStateFull(sectionKey, 1);
        }

        return succeed;
    }

    @Override
    public boolean removeItemFromSection(K sectionKey, int positionAtSection) {
        boolean succeed = super.removeItemFromSection(sectionKey, positionAtSection);
        checkStateFull(sectionKey, 1);

        return succeed;
    }

    private void checkStateFull(K group, int changedItemCount) {
        if (changedItemCount == 0) return;

        if (mContext instanceof StateListener) {
            if (hasSection(group) && getSectionItemCount(group) == 0) {
                ((StateListener) mContext).onGroupEmptied(group);
            }

            int listItemCount = getListItemCount();
            if (listItemCount == 0) ((StateListener) mContext).onListEmptied();
            else if (listItemCount == changedItemCount)
                ((StateListener) mContext).onFirstProfileAdded();
        }
    }

    public void queryState() {
        if (mContext instanceof StateListener && getListItemCount() == 0) {
            ((StateListener) mContext).onListEmptied();
        }
    }

    private enum Payload {
        AVATAR, PROFILE_NAME, AGE
    }

    public interface StateListener {
        void onFirstProfileAdded();

        void onGroupEmptied(Enum<? extends Enum<?>> group);

        void onListEmptied();
    }

    private static class ProfileViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private PersonaView profileView;

        public ProfileViewHolder(@NonNull PersonaView itemView) {
            super(itemView);

            profileView = itemView;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private ListSubHeaderView headerView;

        public HeaderViewHolder(@NonNull ListSubHeaderView itemView) {
            super(itemView);

            headerView = itemView;
        }
    }
}
