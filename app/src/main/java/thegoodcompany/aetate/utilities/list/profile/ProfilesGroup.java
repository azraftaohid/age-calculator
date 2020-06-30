package thegoodcompany.aetate.utilities.list.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import thegoodcompany.aetate.utilities.profilemanagement.Profile;
import thegoodkid.common.utils.recyclerview.HeaderItem;
import thegoodkid.common.utils.recyclerview.Section;

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
}
