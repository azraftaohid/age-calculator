package thegoodcompany.aetate.utilities.profilemanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;

public class ProfileManagerInterface {
    public interface onProfileAddedListener {
        void onProfileAdded(Profile profile);
    }

    public interface onProfileUpdatedListener {
        void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay);

        void onProfileNameUpdated(int profileId, String newName, String previousName);

        void onProfileAvatarUpdated(int profileId, Avatar newAvatar, Avatar previousAvatar);
    }

    public interface onProfileRemovedListener {
        void onProfileRemoved(int profileId);
    }

    public interface onProfilePinnedListener {
        void onProfilePinned(int profileId, boolean isPinned);
    }

    public interface updatable {
        String getName();

        Birthday getBirthday();

        @Nullable
        Avatar getAvatar();

        void updateName(String newName);

        void updateBirthday(int newBirthYear, int newBirthMonth, int newBirthDay);

        void updateAvatar(@NonNull Avatar newAvatar);
    }
}
