package thegoodkid.aetate.utilities.list.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodcompany.common.utils.recyclerview.Section;
import thegoodkid.aetate.utilities.profilemanagement.Profile;

public class ProfilesGroup extends Section<HeaderItem, Profile> {
    private ArrayList<Profile> mProfiles;

    public ProfilesGroup(@Nullable HeaderItem sectionHeader, @NonNull ArrayList<Profile> profiles) {
        super(sectionHeader, profiles);

        mProfiles = profiles;
    }

    @Override
    public void addItem(Profile item) {
        super.addItem(item);
    }

    public void addAll(List<Profile> profiles) {
        mProfiles.addAll(profiles);
    }
}
