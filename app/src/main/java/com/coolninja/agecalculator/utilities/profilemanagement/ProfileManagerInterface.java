package com.coolninja.agecalculator.utilities.profilemanagement;

import com.coolninja.agecalculator.utilities.Birthday;

public class ProfileManagerInterface {
    public interface onProfileAddedListener {
        void onProfileAdded(Profile profile);
    }

    public interface onProfileUpdatedListener {
        void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay);

        void onProfileNameUpdated(int profileId, String newName, String previousName);
    }

    public interface onProfileRemovedListener {
        void onProfileRemoved(int profileId);
    }

    public interface onProfilePinnedListener {
        void onProfilePinned(int profileId, boolean isPinned);
    }

    @SuppressWarnings("unused")
    public interface updatable {
        String getName();

        Birthday getBirthday();

        void updateName(String newName);

        void updateBirthday(int newBirthYear, int newBirthMonth, int newBirthDay);

        String getPreviousName();

        Birthday getPreviousBirthday();
    }
}
