package com.coolninja.agecalculator.utilities.profilemanagement;

import java.util.Calendar;

public class ProfileManagerInterface {
    public interface onProfileUpdateListener {
        void onProfileDateOfBirthChange(int profileId, Calendar newDateOfBirth, Calendar previousDateOfBirth);
        void onProfileNameChange(int profileId, String newName, String previousName);
    }

    public interface onProfilePinListener {
        void onProfilePin(int profileId, boolean isPinned);
    }
}
