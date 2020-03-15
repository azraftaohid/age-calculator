package com.coolninja.agecalculator.utilities.profilemanagement;

import com.coolninja.agecalculator.utilities.Birthday;

public class ProfileManagerInterface {
    public interface onProfileAddedListener {
        void onProfileAdded(Profile profile);
    }

    public interface onProfileUpdatedListener {
        void onProfileDateOfBirthChanged(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay);
        void onProfileNameChanged(int profileId, String newName, String previousName);
    }

    public interface onProfileRemovedListener {
        void onProfileRemoved(int profileId);
    }

    public interface onProfilePinnedListener {
        void onProfilePinned(int profileId, boolean isPinned);
    }
}
