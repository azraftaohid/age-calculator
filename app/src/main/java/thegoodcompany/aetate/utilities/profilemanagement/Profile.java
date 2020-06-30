package thegoodcompany.aetate.utilities.profilemanagement;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodkid.common.utils.recyclerview.BaseItem;

import static thegoodcompany.aetate.utilities.Logging.LOG_V;

public class Profile implements BaseItem, ProfileManagerInterface.Updatable {
    static final String KEY_ID = "profile.id";
    static final String KEY_NAME = "profile.name";
    static final String KEY_BIRTH_YEAR = "profile.dob.year";
    static final String KEY_BIRTH_MONTH = "profile.dob.month";
    static final String KEY_BIRTH_DAY = "profile.dob.day";
    static final String KEY_AVATAR_NAME = "profile.avatar.name";

    private static final String LOG_TAG = Profile.class.getSimpleName();

    private int mId;
    @NonNull
    private String mName;
    @NonNull
    private Birthday mBirthday;
    @Nullable
    private Avatar mAvatar;
    @Nullable
    private ProfileManagerInterface.OnProfileUpdatedListener mOnProfileUpdatedListener;


    public Profile(@NotNull String name, @NotNull Birthday birthday) {
        if (LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mId = ProfileManager.assignProfileId();
        mName = name;
        mBirthday = birthday;
    }

    public Profile(@NotNull String name, @NotNull Birthday birthday, int id) {
        if (LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mId = id;
        mName = name;
        mBirthday = birthday;
    }

    void setOnProfileUpdatedListener(ProfileManagerInterface.OnProfileUpdatedListener listener) {
        mOnProfileUpdatedListener = listener;
    }

    @Override
    public void updateName(@NonNull String newName) {
        if (LOG_V) Log.v(LOG_TAG, "Updating name for profile w/ ID " + mId);

        String prevName = mName;
        mName = newName;

        if (mOnProfileUpdatedListener != null)
            mOnProfileUpdatedListener.onProfileNameChanged(mId, newName, prevName);
    }

    @Override
    public void updateBirthday(int year, int month, int day) {
        if (LOG_V) Log.v(LOG_TAG, "Updating date of birth for profile w/ ID " + mId);

        Birthday prevDateOfBirth = new Birthday(mBirthday.getYear(), mBirthday.getMonthValue(), mBirthday.getYear());

        mBirthday.setDay(day)
                .setMonth(month)
                .setYear(year);

        if (mOnProfileUpdatedListener != null)
            mOnProfileUpdatedListener.onProfileDateOfBirthUpdated(mId, year, month, day, prevDateOfBirth);
    }

    @Override
    public void updateAvatar(@Nullable Avatar newAvatar) {
        if (LOG_V) Log.v(LOG_TAG, "Updating avatar for profile w/ ID " + mId);

        Avatar prevAvatar = null;
        if (mAvatar != null) {
            prevAvatar = Avatar.makeCopy(mAvatar);
        }

        mAvatar = newAvatar;

        if (mOnProfileUpdatedListener != null)
            mOnProfileUpdatedListener.onProfileAvatarChanged(mId, newAvatar, prevAvatar);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Birthday getBirthday() {
        return mBirthday;
    }

    @Override
    public Avatar getAvatar() {
        return mAvatar;
    }

    public int getId() {
        return mId;
    }

    public void setAvatar(@Nullable Avatar avatar) {
        mAvatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile profile = (Profile) o;
        return mId == profile.mId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    public JSONObject toJSONObject() {
        if (LOG_V) Log.v(LOG_TAG, "Generating json object for profile w/ ID: " + mId);

        JSONObject object = new JSONObject();
        try {
            object.put(KEY_ID, mId);
            object.put(KEY_NAME, mName);
            object.put(KEY_BIRTH_YEAR, mBirthday.getYear());
            object.put(KEY_BIRTH_MONTH, mBirthday.getMonthValue());
            object.put(KEY_BIRTH_DAY, mBirthday.getDayOfMonth());

            if (mAvatar != null) object.put(KEY_AVATAR_NAME, mAvatar.getAvatarFileName());
            else object.put(KEY_AVATAR_NAME, null);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "There was an error putting values in json object for profile w/ ID: " + mId);
            e.printStackTrace();
        }

        return object;
    }
}
