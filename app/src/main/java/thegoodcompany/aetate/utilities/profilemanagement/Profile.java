package thegoodcompany.aetate.utilities.profilemanagement;

import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import thegoodcompany.aetate.utilities.Age;
import thegoodcompany.aetate.utilities.Avatar;
import thegoodcompany.aetate.utilities.Birthday;
import thegoodcompany.aetate.utilities.Error;

import static thegoodcompany.aetate.ui.MainActivity.LOG_V;

public class Profile implements ProfileManagerInterface.updatable {
    static final String ID = "profile.id";
    static final String NAME = "profile.name";
    static final String BIRTH_YEAR = "profile.dob.year";
    static final String BIRTH_MONTH = "profile.dob.month";
    static final String BIRTH_DAY = "profile.dob.day";
    static final String AVATAR_NAME = "profile.avatar.name";

    private static final String LOG_TAG = Profile.class.getSimpleName();
    @SuppressWarnings("UnusedAssignment")
    private int mId = Error.NOT_FOUND.getCode(); //Precaution step; in case newly-added constructors did not set id
    private Birthday mDateOfBirth;

    private String mName;
    private ProfileManagerInterface.onProfileUpdatedListener mOnProfileUpdatedListener;

    @Nullable private Avatar mAvatar;

    public Profile(String name, Birthday birthday, ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener) {
        if (LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mOnProfileUpdatedListener = onProfileUpdatedListener;
        mId = ProfileManager.generateProfileId();
        mName = name;
        mDateOfBirth = birthday;
    }

    Profile(String name, Birthday dateOfBirth, int id, ProfileManagerInterface.onProfileUpdatedListener onProfileUpdatedListener) {
        if (LOG_V) Log.v(LOG_TAG, "Creating a new profile");

        mOnProfileUpdatedListener = onProfileUpdatedListener;
        mId = id;
        mName = name;
        mDateOfBirth = dateOfBirth;
    }

    @Override
    public void updateName(String newName) {
        if (LOG_V) Log.v(LOG_TAG, "Updating name for profile w/ ID " + mId);

        String prevName = mName;
        mName = newName;

        if (mOnProfileUpdatedListener != null)
            mOnProfileUpdatedListener.onProfileNameUpdated(mId, newName, prevName);
    }

    @Override
    public void updateBirthday(int year, int month, int day) {
        if (LOG_V) Log.v(LOG_TAG, "Updating date of birth for profile w/ ID " + mId);

        Birthday prevDateOfBirth = new Birthday(mDateOfBirth.get(Birthday.YEAR), mDateOfBirth.get(Birthday.MONTH), mDateOfBirth.get(Birthday.YEAR));

        mDateOfBirth.set(Birthday.YEAR, year);
        mDateOfBirth.set(Birthday.MONTH, month);
        mDateOfBirth.set(Birthday.DAY, day);

        mOnProfileUpdatedListener.onProfileDateOfBirthUpdated(mId, year, month, day, prevDateOfBirth);
    }

    @Override
    public void updateAvatar(@NotNull Avatar newAvatar) {
        if (LOG_V) Log.v(LOG_TAG, "Updating avatar for profile w/ ID " + mId);

        Avatar prevAvatar = null;
        if (mAvatar != null) {
            prevAvatar = Avatar.makeCopy(mAvatar);
        }

        mAvatar = newAvatar;

        mOnProfileUpdatedListener.onProfileAvatarUpdated(mId, newAvatar, prevAvatar);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Birthday getBirthday() {
        return mDateOfBirth;
    }

    @Override
    public Avatar getAvatar() {
        return mAvatar;
    }

    public int getId() {
        return mId;
    }

    void setId(int id) {
        if (LOG_V) Log.v(LOG_TAG, "Setting ID for profile w/ previous ID " + mId);

        mId = id;
    }

    public void setAvatar(Avatar avatar) {
        mAvatar = avatar;
    }

    public Age getAge() {
        return new Age(mDateOfBirth);
    }

    public JSONObject toJSONObject() {
        if (LOG_V) Log.v(LOG_TAG, "Generating json object for profile w/ ID: " + mId);

        JSONObject object = new JSONObject();
        try {
            object.put(ID, mId);
            object.put(NAME, mName);
            object.put(BIRTH_YEAR, mDateOfBirth.get(Birthday.YEAR));
            object.put(BIRTH_MONTH, mDateOfBirth.get(Birthday.MONTH));
            object.put(BIRTH_DAY, mDateOfBirth.get(Birthday.DAY));

            if (mAvatar != null) object.put(AVATAR_NAME, mAvatar.getAvatarFileName());
            else object.put(AVATAR_NAME, null);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "There was an error putting values in json object for profile w/ ID: " + mId);
            e.printStackTrace();
        }

        return object;
    }
}
