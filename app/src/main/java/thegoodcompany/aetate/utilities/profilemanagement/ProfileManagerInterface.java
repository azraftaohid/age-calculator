package thegoodcompany.aetate.utilities.profilemanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.tagmanagement.Tag;

public class ProfileManagerInterface {
    public interface OnProfileAddedListener {
        void onProfileAdded(@NonNull Profile profile);
    }

    public interface OnProfileUpdatedListener {
        void onProfileAvatarChanged(int profileId, @Nullable Avatar newAvatar, @Nullable Avatar previousAvatar);

        void onProfileNameChanged(int profileId, @NonNull String newName, String previousName);

        void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay);
    }

    public interface OnProfileRemovedListener {
        void onProfileRemoved(@NonNull Profile profile, List<Tag> removedTags);
    }

    public interface OnProfilePinnedListener {
        void onProfilePinned(int profileId, boolean isPinned);
    }

    public interface Updatable {
        String getName();

        Birthday getBirthday();

        @Nullable
        Avatar getAvatar();

        void updateName(@NonNull String newName);

        void updateBirthday(int newBirthYear, int newBirthMonth, int newBirthDay);

        void updateAvatar(@Nullable Avatar newAvatar);
    }

}
