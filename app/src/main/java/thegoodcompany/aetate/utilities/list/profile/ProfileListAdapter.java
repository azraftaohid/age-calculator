package thegoodcompany.aetate.utilities.list.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.fluentui.bottomsheet.BottomSheetDialog;
import com.microsoft.fluentui.bottomsheet.BottomSheetItem;
import com.microsoft.fluentui.listitem.ListSubHeaderView;
import com.microsoft.fluentui.persona.PersonaView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import thegoodcompany.aetate.R;
import thegoodcompany.aetate.ui.ProfileDetailsActivity;
import thegoodcompany.aetate.ui.ProfileInfoInputDialog;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.CommonUtilities;
import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodcompany.aetate.utilities.profilemanagement.ProfileManager;
import thegoodkid.common.utils.CalendarUtils;
import thegoodkid.common.utils.StringUtils;
import thegoodkid.common.utils.recyclerview.BaseItem;
import thegoodkid.common.utils.recyclerview.BaseListAdapter;
import thegoodkid.common.utils.recyclerview.HeaderItem;

import static thegoodcompany.aetate.utilities.codes.Extra.EXTRA_PROFILE_ID;

public class ProfileListAdapter<K extends Enum<K>> extends BaseListAdapter<K, ProfilesGroup, HeaderItem, Profile,
        RecyclerView.ViewHolder> {

    Context context;

    public ProfileListAdapter(Context context, LinkedHashMap<K, ProfilesGroup> groupMap) {
        super(groupMap);

        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        switch (ViewType.values()[viewType]) {
            case ITEM:
                PersonaView profileView = new PersonaView(context);
                profileView.setLayoutParams(layoutParams);
                profileView.setAvatarImageDrawable(context.getDrawable(R.drawable.img_defaut_avatar));
                return new ProfileViewHolder(profileView);
            case HEADER:
                ListSubHeaderView headerView = new ListSubHeaderView(context);
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

            ImageView accessoryView = new ImageView(context);
            Drawable accessoryDrawable = context.getDrawable(R.drawable.ic_fluent_more_vertical_24_regular);

            accessoryView.setOnClickListener(view -> onProfileMoreClicked(profileHolder.profileView, profile));
            accessoryView.setImageDrawable(accessoryDrawable);

            profileHolder.profileView.setOnClickListener(view -> onProfileViewClicked(profile));
            profileHolder.profileView.setOnLongClickListener(view -> onProfileMoreClicked(profileHolder.profileView, profile));
            profileHolder.profileView.setCustomAccessoryView(accessoryView);
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
        Intent displayDetailsIntent = new Intent(context, ProfileDetailsActivity.class);
        displayDetailsIntent.putExtra(EXTRA_PROFILE_ID, profile.getId());

        context.startActivity(displayDetailsIntent);
    }

    private boolean onProfileMoreClicked(PersonaView profileView, @NotNull Profile profile) {
        ArrayList<BottomSheetItem> items = new ArrayList<>();

        if (ProfileManager.isPinned(profile.getId()))
            items.add(new BottomSheetItem(R.id.popup_menu_unpin, R.drawable.ic_fluent_pin_off_24_selector, context.getString(R.string.unpin)));
        else
            items.add(new BottomSheetItem(R.id.popup_menu_pin, R.drawable.ic_fluent_pin_24_selector, context.getString(R.string.pin)));
        items.add(new BottomSheetItem(R.id.popup_menu_modify, R.drawable.ic_fluent_edit_24_regular, context.getString(R.string.modify)));
        items.add(new BottomSheetItem(R.id.popup_menu_delete, R.drawable.ic_fluent_delete_24_regular, context.getString(R.string.delete)));

        Integer avatarId = profileView.getAvatarImageResourceId();
        BottomSheetItem header = new BottomSheetItem(R.id.popup_menu_header, avatarId == null ? R.drawable.ic_fluent_person_24_regular : avatarId, profileView.getTitle(), profileView.getSubtitle());

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, items, header);

        bottomSheetDialog.setOnItemClickListener(bottomSheetItem -> {
            ProfileManager profileManager = ProfileManager.getInstance(context);
            if (bottomSheetItem.getId() == R.id.popup_menu_pin) {
                profileManager.pinProfile(profile.getId(), true);
            } else if (bottomSheetItem.getId() == R.id.popup_menu_unpin) {
                profileManager.pinProfile(profile.getId(), false);
            } else if (bottomSheetItem.getId() == R.id.popup_menu_modify) {
                ProfileInfoInputDialog.newInstance(profile)
                        .show(((FragmentActivity) context).getSupportFragmentManager(),
                                context.getString(R.string.modify_profile_dialog_tag));
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
        String subtitle = StringUtils.joinUnless(context.getResources(), age, appendants, ", ", 0);
        String footer = StringUtils.joinUnless(context.getResources(), intervalUntilNextBday, appendants, ", ", 0);

        holder.profileView.setSubtitle(subtitle.length() > 0 ? subtitle + context.getString(R.string.suffix_age) : subtitle);
        holder.profileView.setFooter(footer.length() > 0 ? footer + context.getString(R.string.suffix_birthday) : footer);
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

        checkStateFull(key);
    }

    @Override
    public void addItem(K key, int position, Profile profile) {
        super.addItem(key, position, profile);

        checkStateFull(key);
    }

    public int getProfilePos(int profileId) {
        Profile profile = ProfileManager.getInstance(context).getProfileById(profileId);
        return getItemPosition(profile);
    }

    public int getProfilePos(Profile profile) {
        return getItemPosition(profile);
    }

    @Override
    public boolean moveItem(Profile profile, K destSectionKey, int newPosAtSection) {
        K source = getItemSectionKey(profile);
        if (super.moveItem(profile, destSectionKey, newPosAtSection)) {
            checkStateFull(source);
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
                checkStateFull(sectionKey);
        }

        return succeed;
    }

    @Override
    public boolean removeItemFromSection(K sectionKey, int positionAtSection) {
        boolean succeed = super.removeItemFromSection(sectionKey, positionAtSection);
        checkStateFull(sectionKey);

        return succeed;
    }

    private void checkStateFull(K group) {
        if (context instanceof StateListener) {
            if (hasSection(group) && getSectionItemCount(group) == 0) {
                ((StateListener) context).onGroupEmptied(group);
            }

            int listItemCount = getListItemCount();
            if (listItemCount == 0) ((StateListener) context).onListEmptied();
            else if (listItemCount == 1) ((StateListener) context).onFirstProfileAdded();
        }
    }

    public void queryState() {
        if (context instanceof StateListener && getListItemCount() == 0) {
            ((StateListener) context).onListEmptied();
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
